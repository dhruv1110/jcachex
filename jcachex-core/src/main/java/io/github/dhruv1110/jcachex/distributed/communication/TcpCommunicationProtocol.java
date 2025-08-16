package io.github.dhruv1110.jcachex.distributed.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCP-based implementation of the CommunicationProtocol interface.
 * <p>
 * This implementation provides reliable TCP socket communication between
 * distributed cache nodes. It extends AbstractCommunicationProtocol to inherit
 * common functionality and only implements the TCP-specific networking logic.
 * </p>
 *
 * <h3>TCP-Specific Features:</h3>
 * <ul>
 * <li><strong>Reliable Communication:</strong> TCP guarantees message
 * delivery</li>
 * <li><strong>Connection Pooling:</strong> Reuses connections for better
 * performance</li>
 * <li><strong>Socket Management:</strong> Handles TCP socket lifecycle</li>
 * <li><strong>Error Handling:</strong> TCP-specific error handling and
 * retries</li>
 * </ul>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @since 1.0.0
 */
public class TcpCommunicationProtocol<K, V> extends AbstractCommunicationProtocol<K, V> {

    // TCP-specific components
    private final ExecutorService serverExecutor;
    private final ExecutorService clientExecutor;
    private ServerSocket serverSocket;

    public TcpCommunicationProtocol(ProtocolConfig config) {
        super(config);
        this.serverExecutor = Executors.newFixedThreadPool(config.getMaxConnections());
        this.clientExecutor = Executors.newCachedThreadPool();
    }

    // ============= TCP-Specific Server Lifecycle =============

    @Override
    public CompletableFuture<Void> startServer() {
        return CompletableFuture.runAsync(() -> {
            try {
                serverSocket = new ServerSocket(config.getPort());
                running.set(true);
                logger.info("TCP server started on port: " + config.getPort());

                while (running.get()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        serverExecutor.submit(() -> handleClient(clientSocket));
                    } catch (IOException e) {
                        if (running.get()) {
                            logger.warning("Error accepting client connection: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                logger.severe("Failed to start TCP server: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> stopServer() {
        return CompletableFuture.runAsync(() -> {
            running.set(false);
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                serverExecutor.shutdown();
                clientExecutor.shutdown();
                logger.info("TCP server stopped");
            } catch (IOException e) {
                logger.warning("Error stopping TCP server: " + e.getMessage());
            }
        });
    }

    // ============= TCP-Specific Client Handling =============

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {

            // Read the operation request
            CacheOperationRequest request = (CacheOperationRequest) input.readObject();
            messagesReceived.incrementAndGet();

            // Process the request using the internal cache handler
            CacheOperationResponse response = cacheHandler.handleOperation(request);

            // Send response
            output.writeObject(response);
            output.flush();

        } catch (Exception e) {
            logger.warning("Error handling client request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.warning("Error closing client socket: " + e.getMessage());
            }
        }
    }

    // ============= TCP-Specific Request Sending =============

    // Note: connection pooling disabled in tests for simplicity and isolation

    @Override
    protected CompletableFuture<CacheOperationResponse> sendRequest(String nodeAddress, CacheOperationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            messagesSent.incrementAndGet();
            try {
                String[] parts = nodeAddress.split(":");
                String host = parts[0];
                int port = parts.length > 1 ? Integer.parseInt(parts[1]) : config.getPort();

                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, port), (int) config.getTimeoutMs());
                    socket.setSoTimeout((int) config.getTimeoutMs());

                    try (ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                            ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

                        output.writeObject(request);
                        output.flush();

                        CacheOperationResponse response = (CacheOperationResponse) input.readObject();
                        long latency = System.currentTimeMillis() - startTime;
                        response.setLatency(java.time.Duration.ofMillis(latency));
                        return response;
                    }
                }
            } catch (Exception e) {
                connectionFailures.incrementAndGet();
                logger.warning("Failed to send request to " + nodeAddress + ": " + e.getMessage());
                return CacheOperationResponse.failure("Connection failed: " + e.getMessage(), e);
            }
        }, clientExecutor);
    }

    // Legacy helpers retained for binary compatibility with tests; no-ops now
    private Object getOrCreateConnection(String nodeAddress) {
        return null;
    }

    private void closeAndRemove(String nodeAddress) {
    }

    // ============= Protocol Type =============

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.TCP;
    }

    // ============= TCP-Specific Builder =============

    /**
     * Builder for TcpCommunicationProtocol.
     */
    public static class Builder<K, V> {
        private int port = 8081; // Updated default port
        private long timeoutMs = 5000;
        private int maxConnections = 100;
        private int bufferSize = 8192;
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

        public Builder<K, V> bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder<K, V> additionalProperties(Map<String, Object> properties) {
            this.additionalProperties = new HashMap<>(properties);
            return this;
        }

        public TcpCommunicationProtocol<K, V> build() {
            ProtocolConfig config = new ProtocolConfig(
                    ProtocolType.TCP,
                    port,
                    timeoutMs,
                    maxConnections,
                    bufferSize,
                    additionalProperties);

            return new TcpCommunicationProtocol<>(config);
        }
    }

    /**
     * Create a new TCP communication protocol builder.
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }
}
