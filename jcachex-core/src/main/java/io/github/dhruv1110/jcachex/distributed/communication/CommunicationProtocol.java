package io.github.dhruv1110.jcachex.distributed.communication;

import java.util.concurrent.CompletableFuture;
import java.util.Map;

/**
 * Interface for inter-node communication protocols in distributed cache.
 * <p>
 * This abstraction allows different communication mechanisms (TCP, HTTP, gRPC,
 * etc.)
 * to be plugged into the distributed cache system based on user requirements.
 * </p>
 *
 * <h3>Supported Operations:</h3>
 * <ul>
 * <li><strong>PUT:</strong> Store key-value pair on remote node</li>
 * <li><strong>GET:</strong> Retrieve value for key from remote node</li>
 * <li><strong>REMOVE:</strong> Delete key from remote node</li>
 * <li><strong>HEALTH_CHECK:</strong> Check if remote node is healthy</li>
 * <li><strong>MIGRATE_KEYS:</strong> Request key migration during
 * rebalancing</li>
 * <li><strong>CLUSTER_INFO:</strong> Get cluster topology information</li>
 * </ul>
 *
 * <h3>Implementation Examples:</h3>
 * 
 * <pre>{@code
 * // TCP-based communication
 * CommunicationProtocol tcpProtocol = new TcpCommunicationProtocol(8080);
 *
 * // HTTP-based communication
 * CommunicationProtocol httpProtocol = new HttpCommunicationProtocol(8080);
 *
 * // gRPC-based communication
 * CommunicationProtocol grpcProtocol = new GrpcCommunicationProtocol(9090);
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
        TCP,
        HTTP,
        GRPC,
        WEBSOCKET,
        KAFKA,
        RABBITMQ
    }

    /**
     * Result of a communication operation.
     */
    class CommunicationResult {
        private final boolean success;
        private final String response;
        private final Exception error;

        public CommunicationResult(boolean success, String response, Exception error) {
            this.success = success;
            this.response = response;
            this.error = error;
        }

        public static CommunicationResult success(String response) {
            return new CommunicationResult(true, response, null);
        }

        public static CommunicationResult failure(Exception error) {
            return new CommunicationResult(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getResponse() {
            return response;
        }

        public Exception getError() {
            return error;
        }
    }

    /**
     * Configuration for the communication protocol.
     */
    class ProtocolConfig {
        private final int port;
        private final long timeoutMs;
        private final int maxConnections;
        private final Map<String, Object> additionalProperties;

        public ProtocolConfig(int port, long timeoutMs, int maxConnections, Map<String, Object> additionalProperties) {
            this.port = port;
            this.timeoutMs = timeoutMs;
            this.maxConnections = maxConnections;
            this.additionalProperties = additionalProperties;
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

        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }
    }

    /**
     * Start the communication protocol server to listen for incoming requests.
     *
     * @return CompletableFuture that completes when server is started
     */
    CompletableFuture<Void> startServer();

    /**
     * Stop the communication protocol server.
     *
     * @return CompletableFuture that completes when server is stopped
     */
    CompletableFuture<Void> stopServer();

    /**
     * Send a PUT request to store a key-value pair on a remote node.
     *
     * @param nodeAddress the target node address
     * @param key         the key to store
     * @param value       the value to store
     * @return CompletableFuture with the operation result
     */
    CompletableFuture<CommunicationResult> sendPut(String nodeAddress, K key, V value);

    /**
     * Send a GET request to retrieve a value from a remote node.
     *
     * @param nodeAddress the target node address
     * @param key         the key to retrieve
     * @return CompletableFuture with the operation result
     */
    CompletableFuture<CommunicationResult> sendGet(String nodeAddress, K key);

    /**
     * Send a REMOVE request to delete a key from a remote node.
     *
     * @param nodeAddress the target node address
     * @param key         the key to remove
     * @return CompletableFuture with the operation result
     */
    CompletableFuture<CommunicationResult> sendRemove(String nodeAddress, K key);

    /**
     * Send a health check request to a remote node.
     *
     * @param nodeAddress the target node address
     * @return CompletableFuture with the health check result
     */
    CompletableFuture<CommunicationResult> sendHealthCheck(String nodeAddress);

    /**
     * Request key migration from a remote node during cluster rebalancing.
     *
     * @param nodeAddress the target node address
     * @return CompletableFuture with the migration data
     */
    CompletableFuture<CommunicationResult> requestKeyMigration(String nodeAddress);

    /**
     * Request cluster information from a remote node.
     *
     * @param nodeAddress the target node address
     * @return CompletableFuture with the cluster information
     */
    CompletableFuture<CommunicationResult> requestClusterInfo(String nodeAddress);

    /**
     * Get the protocol type.
     *
     * @return the protocol type
     */
    ProtocolType getProtocolType();

    /**
     * Get the protocol configuration.
     *
     * @return the protocol configuration
     */
    ProtocolConfig getConfig();

    /**
     * Check if the protocol is currently running.
     *
     * @return true if running, false otherwise
     */
    boolean isRunning();

    /**
     * Get protocol-specific metrics.
     *
     * @return map of metric names to values
     */
    Map<String, Object> getMetrics();
}
