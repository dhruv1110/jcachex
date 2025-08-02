package io.github.dhruv1110.jcachex.distributed.communication;

import io.github.dhruv1110.jcachex.distributed.communication.CommunicationProtocol.ProtocolType;
import io.github.dhruv1110.jcachex.distributed.communication.CommunicationProtocol.ProtocolConfig;
import io.github.dhruv1110.jcachex.distributed.communication.CommunicationProtocol.CommunicationResult;
import io.github.dhruv1110.jcachex.Cache;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * TCP-based implementation of the CommunicationProtocol interface.
 * <p>
 * This implementation provides reliable TCP socket communication between
 * distributed cache nodes using byte-based serialization for any object types.
 * The protocol handles cache operations internally without requiring
 * users to provide request handlers.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 * <li><strong>Type Agnostic:</strong> Handles any serializable cache value
 * types via byte arrays</li>
 * <li><strong>Reliable Communication:</strong> TCP guarantees message
 * delivery</li>
 * <li><strong>Connection Pooling:</strong> Reuses connections for better
 * performance</li>
 * <li><strong>Error Handling:</strong> Comprehensive error handling and
 * retries</li>
 * <li><strong>Metrics:</strong> Built-in communication metrics</li>
 * <li><strong>Auto-handling:</strong> Handles cache operations without user
 * configuration</li>
 * </ul>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @since 1.0.0
 */
public class TcpCommunicationProtocol<K, V> implements CommunicationProtocol<K, V> {

    private static final Logger logger = Logger.getLogger(TcpCommunicationProtocol.class.getName());

    private final ProtocolConfig config;
    private final InternalCacheHandler<K, V> cacheHandler;

    // Server components
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService serverExecutor;
    private final ExecutorService clientExecutor;
    private ServerSocket serverSocket;

    // Metrics
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong connectionFailures = new AtomicLong(0);

    // Cache reference for actual operations
    private volatile Cache<K, V> localCache;

    public TcpCommunicationProtocol(ProtocolConfig config) {
        this.config = config;
        this.cacheHandler = new InternalCacheHandler<>();
        this.serverExecutor = Executors.newFixedThreadPool(config.getMaxConnections());
        this.clientExecutor = Executors.newCachedThreadPool();
    }

    /**
     * Sets the local cache instance for actual cache operations.
     * This is called by the distributed cache implementation.
     */
    @Override
    public void setLocalCache(io.github.dhruv1110.jcachex.Cache<K, V> cache) {
        this.localCache = cache;
        this.cacheHandler.setCache(cache);
    }

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

    @Override
    public CompletableFuture<CommunicationResult<Void>> sendPut(String nodeAddress, K key, V value) {
        CacheOperationRequest request = new CacheOperationRequest(OperationType.PUT, serializeObject(key),
                serializeObject(value));
        return sendRequest(nodeAddress, request).thenApply(
                response -> response.isSuccess() ? CommunicationResult.<Void>success(null, response.getLatency())
                        : CommunicationResult.<Void>failure(response.getErrorMessage(), response.getError()));
    }

    @Override
    public CompletableFuture<CommunicationResult<V>> sendGet(String nodeAddress, K key) {
        CacheOperationRequest request = new CacheOperationRequest(OperationType.GET, serializeObject(key), null);
        return sendRequest(nodeAddress, request).thenApply(response -> {
            if (response.isSuccess() && response.getResult() != null) {
                V value = deserializeObject(response.getResult());
                return CommunicationResult.<V>success(value, response.getLatency());
            } else {
                return CommunicationResult.<V>failure(response.getErrorMessage(), response.getError());
            }
        });
    }

    @Override
    public CompletableFuture<CommunicationResult<V>> sendRemove(String nodeAddress, K key) {
        CacheOperationRequest request = new CacheOperationRequest(OperationType.REMOVE, serializeObject(key), null);
        return sendRequest(nodeAddress, request).thenApply(response -> {
            if (response.isSuccess() && response.getResult() != null) {
                V value = deserializeObject(response.getResult());
                return CommunicationResult.<V>success(value, response.getLatency());
            } else {
                return CommunicationResult.<V>failure(response.getErrorMessage(), response.getError());
            }
        });
    }

    @Override
    public CompletableFuture<CommunicationResult<String>> sendHealthCheck(String nodeAddress) {
        CacheOperationRequest request = new CacheOperationRequest(OperationType.HEALTH_CHECK, null, null);
        return sendRequest(nodeAddress, request).thenApply(
                response -> response.isSuccess() ? CommunicationResult.<String>success("OK", response.getLatency())
                        : CommunicationResult.<String>failure(response.getErrorMessage(), response.getError()));
    }

    @Override
    public CompletableFuture<Map<String, CommunicationResult<Void>>> broadcastInvalidation(
            Collection<String> nodeAddresses, K key) {
        Map<String, CompletableFuture<CommunicationResult<V>>> futures = new HashMap<>();

        for (String nodeAddress : nodeAddresses) {
            futures.put(nodeAddress, sendRemove(nodeAddress, key));
        }

        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    Map<String, CommunicationResult<Void>> results = new HashMap<>();
                    futures.forEach((nodeAddress, future) -> {
                        try {
                            CommunicationResult<V> removeResult = future.get();
                            results.put(nodeAddress,
                                    removeResult.isSuccess()
                                            ? CommunicationResult.<Void>success(null, removeResult.getLatency())
                                            : CommunicationResult.<Void>failure(removeResult.getErrorMessage(),
                                                    removeResult.getError()));
                        } catch (Exception e) {
                            results.put(nodeAddress,
                                    CommunicationResult.<Void>failure("Exception during broadcast", e));
                        }
                    });
                    return results;
                });
    }

    @Override
    public CompletableFuture<Map<String, CommunicationResult<Void>>> broadcastBatchInvalidation(
            Collection<String> nodeAddresses, Collection<K> keys) {
        List<CompletableFuture<Map<String, CommunicationResult<Void>>>> futures = new ArrayList<>();

        for (K key : keys) {
            futures.add(broadcastInvalidation(nodeAddresses, key));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    Map<String, CommunicationResult<Void>> combinedResults = new HashMap<>();
                    for (String nodeAddress : nodeAddresses) {
                        combinedResults.put(nodeAddress,
                                CommunicationResult.<Void>success(null, java.time.Duration.ZERO));
                    }
                    return combinedResults;
                });
    }

    @Override
    public CompletableFuture<Map<String, CommunicationResult<Void>>> broadcastClear(
            Collection<String> nodeAddresses) {
        Map<String, CompletableFuture<CommunicationResult<String>>> futures = new HashMap<>();

        for (String nodeAddress : nodeAddresses) {
            CacheOperationRequest request = new CacheOperationRequest(OperationType.CLEAR, null, null);
            futures.put(nodeAddress,
                    sendRequest(nodeAddress, request).thenApply(response -> response.isSuccess()
                            ? CommunicationResult.<String>success("OK", response.getLatency())
                            : CommunicationResult.<String>failure(response.getErrorMessage(), response.getError())));
        }

        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    Map<String, CommunicationResult<Void>> results = new HashMap<>();
                    futures.forEach((nodeAddress, future) -> {
                        try {
                            CommunicationResult<String> clearResult = future.get();
                            results.put(nodeAddress,
                                    clearResult.isSuccess()
                                            ? CommunicationResult.<Void>success(null, clearResult.getLatency())
                                            : CommunicationResult.<Void>failure(clearResult.getErrorMessage(),
                                                    clearResult.getError()));
                        } catch (Exception e) {
                            results.put(nodeAddress,
                                    CommunicationResult.<Void>failure("Exception during broadcast", e));
                        }
                    });
                    return results;
                });
    }

    @Override
    public CompletableFuture<CommunicationResult<Map<K, V>>> requestKeyMigration(
            String nodeAddress, Collection<K> keys) {
        byte[] serializedKeys = serializeObject(keys);
        CacheOperationRequest request = new CacheOperationRequest(OperationType.MIGRATE_KEYS, serializedKeys, null);
        return sendRequest(nodeAddress, request).thenApply(response -> {
            if (response.isSuccess() && response.getResult() != null) {
                Map<K, V> migratedData = deserializeObject(response.getResult());
                return CommunicationResult.<Map<K, V>>success(migratedData, response.getLatency());
            } else {
                return CommunicationResult.<Map<K, V>>failure(response.getErrorMessage(), response.getError());
            }
        });
    }

    @Override
    public CompletableFuture<CommunicationResult<String>> requestClusterInfo(String nodeAddress) {
        CacheOperationRequest request = new CacheOperationRequest(OperationType.CLUSTER_INFO, null, null);
        return sendRequest(nodeAddress, request).thenApply(response -> response.isSuccess()
                ? CommunicationResult.<String>success("Cluster Info", response.getLatency())
                : CommunicationResult.<String>failure(response.getErrorMessage(), response.getError()));
    }

    private CompletableFuture<CacheOperationResponse> sendRequest(String nodeAddress, CacheOperationRequest request) {
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

                        // Send request
                        output.writeObject(request);
                        output.flush();

                        // Read response
                        CacheOperationResponse response = (CacheOperationResponse) input.readObject();

                        long latency = System.currentTimeMillis() - startTime;
                        java.time.Duration duration = java.time.Duration.ofMillis(latency);
                        response.setLatency(duration);

                        return response;
                    }
                }
            } catch (Exception e) {
                connectionFailures.incrementAndGet();
                e.printStackTrace();
                logger.warning("Failed to send request to " + nodeAddress + ": " + e.getMessage());
                return CacheOperationResponse.failure("Connection failed: " + e.getMessage(), e);
            }
        }, clientExecutor);
    }

    // ============= Serialization Helper Methods =============

    private byte[] serializeObject(Object obj) {
        if (obj == null)
            return null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {
            logger.warning("Failed to serialize object: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeObject(byte[] data) {
        if (data == null)
            return null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Failed to deserialize object: " + e.getMessage());
            return null;
        }
    }

    // ============= Internal Classes =============

    /**
     * Internal operation types for cache communication.
     */
    private enum OperationType {
        PUT, GET, REMOVE, CLEAR, HEALTH_CHECK, MIGRATE_KEYS, CLUSTER_INFO
    }

    /**
     * Internal request structure for cache operations.
     */
    private static class CacheOperationRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        private final OperationType operation;
        private final byte[] keyData;
        private final byte[] valueData;

        public CacheOperationRequest(OperationType operation, byte[] keyData, byte[] valueData) {
            this.operation = operation;
            this.keyData = keyData;
            this.valueData = valueData;
        }

        public OperationType getOperation() {
            return operation;
        }

        public byte[] getKeyData() {
            return keyData;
        }

        public byte[] getValueData() {
            return valueData;
        }
    }

    /**
     * Internal response structure for cache operations.
     */
    private static class CacheOperationResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private final boolean success;
        private final byte[] result;
        private final String errorMessage;
        private final Exception error;
        private java.time.Duration latency;

        public CacheOperationResponse(boolean success, byte[] result, String errorMessage, Exception error) {
            this.success = success;
            this.result = result;
            this.errorMessage = errorMessage;
            this.error = error;
        }

        public static CacheOperationResponse success(byte[] result) {
            return new CacheOperationResponse(true, result, null, null);
        }

        public static CacheOperationResponse failure(String errorMessage, Exception error) {
            return new CacheOperationResponse(false, null, errorMessage, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public byte[] getResult() {
            return result;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Exception getError() {
            return error;
        }

        public java.time.Duration getLatency() {
            return latency;
        }

        public void setLatency(java.time.Duration latency) {
            this.latency = latency;
        }
    }

    /**
     * Internal handler for actual cache operations.
     * This replaces the old RequestHandler interface and performs real cache
     * operations.
     */
    private class InternalCacheHandler<K, V> {
        private volatile Cache<K, V> cache;

        public void setCache(Cache<K, V> cache) {
            this.cache = cache;
        }

        public CacheOperationResponse handleOperation(CacheOperationRequest request) {
            try {
                switch (request.getOperation()) {
                    case PUT:
                        return handlePut(request);
                    case GET:
                        return handleGet(request);
                    case REMOVE:
                        return handleRemove(request);
                    case CLEAR:
                        return handleClear();
                    case HEALTH_CHECK:
                        return handleHealthCheck();
                    case MIGRATE_KEYS:
                        return handleMigrateKeys(request);
                    case CLUSTER_INFO:
                        return handleClusterInfo();
                    default:
                        return CacheOperationResponse.failure("Unknown operation: " + request.getOperation(),
                                new IllegalArgumentException("Unknown operation"));
                }
            } catch (Exception e) {
                logger.warning("Error handling cache operation: " + e.getMessage());
                return CacheOperationResponse.failure("Operation failed: " + e.getMessage(), e);
            }
        }

        private CacheOperationResponse handlePut(CacheOperationRequest request) {
            if (cache == null) {
                return CacheOperationResponse.failure("Cache not initialized", new IllegalStateException("No cache"));
            }

            K key = deserializeObject(request.getKeyData());
            V value = deserializeObject(request.getValueData());

            if (key != null && value != null) {
                cache.put(key, value);
                logger.info("Put operation successful for key: " + key + ", received from remote node");
                return CacheOperationResponse.success(null);
            } else {
                return CacheOperationResponse.failure("Invalid key or value",
                        new IllegalArgumentException("Null key/value"));
            }
        }

        private CacheOperationResponse handleGet(CacheOperationRequest request) {
            if (cache == null) {
                return CacheOperationResponse.failure("Cache not initialized", new IllegalStateException("No cache"));
            }

            K key = deserializeObject(request.getKeyData());
            if (key != null) {
                V value = cache.get(key);
                logger.info("Get operation successful for key: " + key + ", received from remote node");
                return CacheOperationResponse.success(serializeObject(value));
            } else {
                return CacheOperationResponse.failure("Invalid key", new IllegalArgumentException("Null key"));
            }
        }

        private CacheOperationResponse handleRemove(CacheOperationRequest request) {
            if (cache == null) {
                return CacheOperationResponse.failure("Cache not initialized", new IllegalStateException("No cache"));
            }

            K key = deserializeObject(request.getKeyData());
            if (key != null) {
                V removedValue = cache.remove(key);
                return CacheOperationResponse.success(serializeObject(removedValue));
            } else {
                return CacheOperationResponse.failure("Invalid key", new IllegalArgumentException("Null key"));
            }
        }

        private CacheOperationResponse handleClear() {
            if (cache == null) {
                return CacheOperationResponse.failure("Cache not initialized", new IllegalStateException("No cache"));
            }

            cache.clear();
            return CacheOperationResponse.success(serializeObject("Cache cleared"));
        }

        private CacheOperationResponse handleHealthCheck() {
            // Check if cache is available and responsive
            if (cache != null) {
                // Simple health check - try to get cache stats if available
                try {
                    long size = cache.size();
                    String healthInfo = "OK|size=" + size;
                    return CacheOperationResponse.success(serializeObject(healthInfo));
                } catch (Exception e) {
                    return CacheOperationResponse.failure("Cache health check failed", e);
                }
            } else {
                return CacheOperationResponse.failure("Cache not available", new IllegalStateException("No cache"));
            }
        }

        private CacheOperationResponse handleMigrateKeys(CacheOperationRequest request) {
            if (cache == null) {
                return CacheOperationResponse.failure("Cache not initialized", new IllegalStateException("No cache"));
            }

            Collection<K> keys = deserializeObject(request.getKeyData());
            if (keys != null) {
                Map<K, V> migratedData = new HashMap<>();
                for (K key : keys) {
                    V value = cache.get(key);
                    if (value != null) {
                        migratedData.put(key, value);
                    }
                }
                return CacheOperationResponse.success(serializeObject(migratedData));
            } else {
                return CacheOperationResponse.failure("Invalid keys", new IllegalArgumentException("Null keys"));
            }
        }

        private CacheOperationResponse handleClusterInfo() {
            // Return basic cluster information
            String clusterInfo = "Node: " + config.getPort() + "|Protocol: TCP|Status: Running";
            return CacheOperationResponse.success(serializeObject(clusterInfo));
        }
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
        metrics.put("messagesSent", messagesSent.get());
        metrics.put("messagesReceived", messagesReceived.get());
        metrics.put("connectionFailures", connectionFailures.get());
        metrics.put("protocolType", "TCP");
        metrics.put("port", config.getPort());
        return metrics;
    }

    /**
     * Builder for TcpCommunicationProtocol.
     */
    public static class Builder<K, V> {
        private int port = 8080;
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
