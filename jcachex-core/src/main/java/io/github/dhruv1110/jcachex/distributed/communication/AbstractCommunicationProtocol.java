package io.github.dhruv1110.jcachex.distributed.communication;

import io.github.dhruv1110.jcachex.Cache;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Abstract base class for communication protocol implementations.
 * <p>
 * This class provides common functionality shared across all communication
 * protocols,
 * including serialization, broadcast operations, cache handling, and metrics.
 * Concrete implementations only need to provide the transport-specific
 * networking logic.
 * </p>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public abstract class AbstractCommunicationProtocol<K, V> implements CommunicationProtocol<K, V> {

    protected static final Logger logger = Logger.getLogger(AbstractCommunicationProtocol.class.getName());

    // Common state and configuration
    protected final ProtocolConfig config;
    protected final InternalCacheHandler<K, V> cacheHandler;
    protected final AtomicBoolean running = new AtomicBoolean(false);
    protected volatile Cache<K, V> localCache;

    // Common metrics
    protected final AtomicLong messagesSent = new AtomicLong(0);
    protected final AtomicLong messagesReceived = new AtomicLong(0);
    protected final AtomicLong connectionFailures = new AtomicLong(0);

    protected AbstractCommunicationProtocol(ProtocolConfig config) {
        this.config = config;
        this.cacheHandler = new InternalCacheHandler<>();
    }

    // ============= Common Interface Methods =============

    @Override
    public void setLocalCache(Cache<K, V> cache) {
        this.localCache = cache;
        this.cacheHandler.setCache(cache);
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
        metrics.put("protocolType", getProtocolType().toString());
        metrics.put("port", config.getPort());
        return metrics;
    }

    // ============= Common Cache Operations =============

    @Override
    public CompletableFuture<CommunicationResult<Void>> sendPut(String nodeAddress, K key, V value) {
        CacheOperationRequest request = new CacheOperationRequest(OperationType.PUT, serializeObject(key),
                serializeObject(value));
        return sendRequest(nodeAddress, request).thenApply(
                response -> response.isSuccess()
                        ? CommunicationResult.<Void>success(null, response.getLatency())
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
                response -> response.isSuccess()
                        ? CommunicationResult.<String>success("OK", response.getLatency())
                        : CommunicationResult.<String>failure(response.getErrorMessage(), response.getError()));
    }

    @Override
    public CompletableFuture<CommunicationResult<Map<K, V>>> requestKeyMigration(String nodeAddress,
            Collection<K> keys) {
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

    // ============= Common Broadcast Operations =============

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
                                CommunicationResult.<Void>success(null, Duration.ZERO));
                    }
                    return combinedResults;
                });
    }

    @Override
    public CompletableFuture<Map<String, CommunicationResult<Void>>> broadcastClear(Collection<String> nodeAddresses) {
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

    // ============= Abstract Methods (Protocol-Specific) =============

    /**
     * Send a request to a remote node using the specific transport protocol.
     * This method must be implemented by concrete protocol classes.
     */
    protected abstract CompletableFuture<CacheOperationResponse> sendRequest(String nodeAddress,
            CacheOperationRequest request);

    // ============= Common Serialization Methods =============

    protected byte[] serializeObject(Object obj) {
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
    protected <T> T deserializeObject(byte[] data) {
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

    // ============= Common Internal Classes =============

    /**
     * Internal operation types for cache communication.
     */
    protected enum OperationType {
        PUT, GET, REMOVE, CLEAR, HEALTH_CHECK, MIGRATE_KEYS, CLUSTER_INFO
    }

    /**
     * Internal request structure for cache operations.
     */
    protected static class CacheOperationRequest implements Serializable {
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
    protected static class CacheOperationResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private final boolean success;
        private final byte[] result;
        private final String errorMessage;
        private final Exception error;
        private Duration latency;

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

        public Duration getLatency() {
            return latency;
        }

        public void setLatency(Duration latency) {
            this.latency = latency;
        }
    }

    /**
     * Internal handler for actual cache operations.
     */
    protected class InternalCacheHandler<K, V> {
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
            if (cache != null) {
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
            String clusterInfo = "Node: " + config.getPort() + "|Protocol: " + getProtocolType() + "|Status: Running";
            return CacheOperationResponse.success(serializeObject(clusterInfo));
        }
    }
}
