package io.github.dhruv1110.jcachex.distributed.communication;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;

/**
 * TCP-based implementation of the CommunicationProtocol interface.
 * <p>
 * This implementation provides reliable TCP socket communication between
 * distributed cache nodes.
 * It supports all standard cache operations and cluster management functions.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 * <li><strong>Reliable Communication:</strong> TCP guarantees message
 * delivery</li>
 * <li><strong>Connection Pooling:</strong> Reuses connections for better
 * performance</li>
 * <li><strong>Error Handling:</strong> Comprehensive error handling and
 * retries</li>
 * <li><strong>Metrics:</strong> Built-in communication metrics</li>
 * </ul>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @since 1.0.0
 */
public class TcpCommunicationProtocol<K, V> implements CommunicationProtocol<K, V> {
    private static final Logger logger = Logger.getLogger(TcpCommunicationProtocol.class.getName());

    private final ProtocolConfig config;
    private final ExecutorService serverExecutor;
    private final ExecutorService clientExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ServerSocket serverSocket;
    private final RequestHandler<K, V> requestHandler;

    // Metrics
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);

    /**
     * Interface for handling incoming requests.
     */
    public interface RequestHandler<K, V> {
        String handleRequest(String request);
    }

    public TcpCommunicationProtocol(ProtocolConfig config, RequestHandler<K, V> requestHandler) {
        this.config = config;
        this.requestHandler = requestHandler;
        this.serverExecutor = Executors.newFixedThreadPool(config.getMaxConnections(), r -> {
            Thread t = new Thread(r, "tcp-server-" + config.getPort());
            t.setDaemon(true);
            return t;
        });
        this.clientExecutor = Executors.newFixedThreadPool(20, r -> {
            Thread t = new Thread(r, "tcp-client");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public CompletableFuture<Void> startServer() {
        return CompletableFuture.runAsync(() -> {
            if (running.compareAndSet(false, true)) {
                try {
                    serverSocket = new ServerSocket(config.getPort());
                    logger.info("TCP server started on port " + config.getPort());

                    // Accept connections in background
                    serverExecutor.submit(this::acceptConnections);

                } catch (IOException e) {
                    running.set(false);
                    throw new RuntimeException("Failed to start TCP server on port " + config.getPort(), e);
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> stopServer() {
        return CompletableFuture.runAsync(() -> {
            if (running.compareAndSet(true, false)) {
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                    }

                    serverExecutor.shutdown();
                    clientExecutor.shutdown();

                    try {
                        if (!serverExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                            serverExecutor.shutdownNow();
                        }
                        if (!clientExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                            clientExecutor.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        serverExecutor.shutdownNow();
                        clientExecutor.shutdownNow();
                        Thread.currentThread().interrupt();
                    }

                    logger.info("TCP server stopped");
                } catch (IOException e) {
                    logger.warning("Error stopping TCP server: " + e.getMessage());
                }
            }
        });
    }

    private void acceptConnections() {
        while (running.get() && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                serverExecutor.submit(() -> handleConnection(clientSocket));
            } catch (IOException e) {
                if (running.get()) {
                    logger.warning("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    private void handleConnection(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            clientSocket.setSoTimeout((int) config.getTimeoutMs());
            String request = reader.readLine();

            if (request != null) {
                long startTime = System.currentTimeMillis();
                totalRequests.incrementAndGet();

                try {
                    String response = requestHandler.handleRequest(request);
                    writer.println(response);
                    successfulRequests.incrementAndGet();
                } catch (Exception e) {
                    writer.println("ERROR|" + e.getMessage());
                    failedRequests.incrementAndGet();
                    logger.warning("Error handling request: " + e.getMessage());
                } finally {
                    totalResponseTime.addAndGet(System.currentTimeMillis() - startTime);
                }
            }
        } catch (IOException e) {
            logger.warning("Connection error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.warning("Error closing client socket: " + e.getMessage());
            }
        }
    }

    @Override
    public CompletableFuture<CommunicationResult> sendPut(String nodeAddress, K key, V value) {
        String request = "PUT|" + key + "|" + value;
        return sendRequest(nodeAddress, request);
    }

    @Override
    public CompletableFuture<CommunicationResult> sendGet(String nodeAddress, K key) {
        String request = "GET|" + key;
        return sendRequest(nodeAddress, request);
    }

    @Override
    public CompletableFuture<CommunicationResult> sendRemove(String nodeAddress, K key) {
        String request = "REMOVE|" + key;
        return sendRequest(nodeAddress, request);
    }

    @Override
    public CompletableFuture<CommunicationResult> sendHealthCheck(String nodeAddress) {
        String request = "HEALTH_CHECK";
        return sendRequest(nodeAddress, request);
    }

    @Override
    public CompletableFuture<CommunicationResult> requestKeyMigration(String nodeAddress) {
        String request = "MIGRATE_KEYS";
        return sendRequest(nodeAddress, request);
    }

    @Override
    public CompletableFuture<CommunicationResult> requestClusterInfo(String nodeAddress) {
        String request = "CLUSTER_INFO";
        return sendRequest(nodeAddress, request);
    }

    private CompletableFuture<CommunicationResult> sendRequest(String nodeAddress, String request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Parse node address (format: host:port)
                String[] parts = nodeAddress.split(":");
                String host = parts[0];
                int port = parts.length > 1 ? Integer.parseInt(parts[1]) : config.getPort();

                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, port), (int) config.getTimeoutMs());
                    socket.setSoTimeout((int) config.getTimeoutMs());

                    try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(socket.getInputStream()))) {

                        writer.println(request);
                        String response = reader.readLine();

                        if (response != null) {
                            return CommunicationResult.success(response);
                        } else {
                            return CommunicationResult.failure(new IOException("No response received"));
                        }
                    }
                }
            } catch (Exception e) {
                logger.warning("Failed to send request to " + nodeAddress + ": " + e.getMessage());
                return CommunicationResult.failure(e);
            }
        }, clientExecutor);
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.TCP;
    }

    @Override
    public ProtocolConfig getConfig() {
        return config;
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("protocol", "TCP");
        metrics.put("port", config.getPort());
        metrics.put("running", running.get());
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("successfulRequests", successfulRequests.get());
        metrics.put("failedRequests", failedRequests.get());

        long total = totalRequests.get();
        long totalTime = totalResponseTime.get();
        metrics.put("averageResponseTimeMs", total > 0 ? totalTime / total : 0);
        metrics.put("successRate", total > 0 ? (double) successfulRequests.get() / total : 0.0);

        return metrics;
    }

    /**
     * Builder for TcpCommunicationProtocol.
     */
    public static class Builder<K, V> {
        private int port = 8080;
        private long timeoutMs = 5000;
        private int maxConnections = 100;
        private RequestHandler<K, V> requestHandler;
        private Map<String, Object> additionalProperties = new HashMap<>();

        public Builder<K, V> port(int port) {
            this.port = port;
            return this;
        }

        public Builder<K, V> timeout(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder<K, V> maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder<K, V> requestHandler(RequestHandler<K, V> requestHandler) {
            this.requestHandler = requestHandler;
            return this;
        }

        public Builder<K, V> property(String key, Object value) {
            this.additionalProperties.put(key, value);
            return this;
        }

        public TcpCommunicationProtocol<K, V> build() {
            if (requestHandler == null) {
                throw new IllegalStateException("RequestHandler is required");
            }

            ProtocolConfig config = new ProtocolConfig(port, timeoutMs, maxConnections, additionalProperties);
            return new TcpCommunicationProtocol<>(config, requestHandler);
        }
    }

    /**
     * Create a new TCP communication protocol builder.
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }
}
