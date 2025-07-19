package io.github.dhruv1110.jcachex.distributed.communication;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

/**
 * Simple interface for inter-node communication protocols in distributed cache.
 * <p>
 * This interface provides a clean, focused API for distributed cache
 * communication
 * without unnecessary complexity. Currently supports TCP-based communication
 * with plans for additional protocols as needed.
 * </p>
 *
 * <h3>Supported Operations:</h3>
 * <ul>
 * <li><strong>PUT:</strong> Store key-value pair on remote node</li>
 * <li><strong>GET:</strong> Retrieve value for key from remote node</li>
 * <li><strong>REMOVE:</strong> Delete key from remote node</li>
 * <li><strong>HEALTH_CHECK:</strong> Check if remote node is healthy</li>
 * <li><strong>INVALIDATION:</strong> Broadcast invalidation to multiple
 * nodes</li>
 * <li><strong>MIGRATION:</strong> Request key migration during rebalancing</li>
 * <li><strong>CLUSTER_INFO:</strong> Get cluster topology information</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Create TCP communication protocol
 * CommunicationProtocol<String, User> protocol = TcpCommunicationProtocol.<String, User>builder()
 *         .port(8080)
 *         .timeout(5000)
 *         .maxConnections(100)
 *         .build();
 *
 * // Cache operations
 * protocol.sendPut("node-2:8080", "user123", user);
 * User retrieved = protocol.sendGet("node-2:8080", "user123").get().getResult();
 *
 * // Broadcasting
 * protocol.broadcastInvalidation(Arrays.asList("node-1:8080", "node-2:8080"), "user123");
 * }</pre>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @since 1.0.0
 */
public interface CommunicationProtocol<K, V> {

    /**
     * Communication protocol types supported by the system.
     */
    enum ProtocolType {
        TCP // Currently only TCP is implemented
    }

    /**
     * Result of a communication operation with type safety.
     */
    class CommunicationResult<T> implements Serializable {
        private static final long serialVersionUID = 1L;

        private final boolean success;
        private final T result;
        private final String errorMessage;
        private final Exception error;
        private final Instant timestamp;
        private final Duration latency;

        public CommunicationResult(boolean success, T result, String errorMessage, Exception error, Duration latency) {
            this.success = success;
            this.result = result;
            this.errorMessage = errorMessage;
            this.error = error;
            this.timestamp = Instant.now();
            this.latency = latency;
        }

        public static <T> CommunicationResult<T> success(T result, Duration latency) {
            return new CommunicationResult<>(true, result, null, null, latency);
        }

        public static <T> CommunicationResult<T> failure(String errorMessage, Exception error) {
            return new CommunicationResult<>(false, null, errorMessage, error, Duration.ZERO);
        }

        public boolean isSuccess() {
            return success;
        }

        public T getResult() {
            return result;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Exception getError() {
            return error;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public Duration getLatency() {
            return latency;
        }
    }

    /**
     * Simple configuration for the communication protocol.
     */
    class ProtocolConfig {
        private final ProtocolType protocolType;
        private final int port;
        private final long timeoutMs;
        private final int maxConnections;
        private final int bufferSize;
        private final Map<String, Object> additionalProperties;

        public ProtocolConfig(ProtocolType protocolType, int port, long timeoutMs,
                int maxConnections, int bufferSize, Map<String, Object> additionalProperties) {
            this.protocolType = protocolType;
            this.port = port;
            this.timeoutMs = timeoutMs;
            this.maxConnections = maxConnections;
            this.bufferSize = bufferSize;
            this.additionalProperties = additionalProperties;
        }

        // Getters
        public ProtocolType getProtocolType() {
            return protocolType;
        }

        public int getPort() {
            return port;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }
    }

    // ============= Lifecycle Methods =============

    /**
     * Start the communication protocol server to listen for incoming requests.
     */
    CompletableFuture<Void> startServer();

    /**
     * Stop the communication protocol server.
     */
    CompletableFuture<Void> stopServer();

    // ============= Single Node Operations =============

    /**
     * Send a PUT request to store a key-value pair on a remote node.
     */
    CompletableFuture<CommunicationResult<Void>> sendPut(String nodeAddress, K key, V value);

    /**
     * Send a GET request to retrieve a value from a remote node.
     */
    CompletableFuture<CommunicationResult<V>> sendGet(String nodeAddress, K key);

    /**
     * Send a REMOVE request to delete a key from a remote node.
     */
    CompletableFuture<CommunicationResult<V>> sendRemove(String nodeAddress, K key);

    /**
     * Send a health check request to a remote node.
     */
    CompletableFuture<CommunicationResult<String>> sendHealthCheck(String nodeAddress);

    // ============= Multi-Node Broadcasting =============

    /**
     * Broadcast invalidation for a single key to multiple nodes.
     */
    CompletableFuture<Map<String, CommunicationResult<Void>>> broadcastInvalidation(
            Collection<String> nodeAddresses, K key);

    /**
     * Broadcast invalidation for multiple keys to multiple nodes.
     */
    CompletableFuture<Map<String, CommunicationResult<Void>>> broadcastBatchInvalidation(
            Collection<String> nodeAddresses, Collection<K> keys);

    /**
     * Broadcast cache clear to multiple nodes.
     */
    CompletableFuture<Map<String, CommunicationResult<Void>>> broadcastClear(
            Collection<String> nodeAddresses);

    // ============= Cluster Management =============

    /**
     * Request key migration from a remote node during cluster rebalancing.
     */
    CompletableFuture<CommunicationResult<Map<K, V>>> requestKeyMigration(
            String nodeAddress, Collection<K> keys);

    /**
     * Request cluster information from a remote node.
     */
    CompletableFuture<CommunicationResult<String>> requestClusterInfo(String nodeAddress);

    // ============= Protocol Information =============

    /**
     * Get the protocol type.
     */
    ProtocolType getProtocolType();

    /**
     * Get the protocol configuration.
     */
    ProtocolConfig getConfig();

    /**
     * Check if the protocol is currently running.
     */
    boolean isRunning();

    /**
     * Get protocol-specific metrics.
     */
    Map<String, Object> getMetrics();
}
