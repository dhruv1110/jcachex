package io.github.dhruv1110.jcachex.distributed;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Network protocol abstraction for distributed cache communication.
 * <p>
 * This interface provides a pluggable network layer that supports different
 * protocols (TCP, UDP, HTTP) and serialization formats (Java, JSON, Protocol
 * Buffers).
 * The protocol handles node-to-node communication for cache operations, cluster
 * management, and failure detection.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Protocol Agnostic:</strong> Supports TCP, UDP, HTTP,
 * WebSocket</li>
 * <li><strong>Pluggable Serialization:</strong> Java, JSON, Avro, Protocol
 * Buffers</li>
 * <li><strong>Compression Support:</strong> GZIP, LZ4, Snappy compression</li>
 * <li><strong>Security:</strong> TLS encryption and authentication</li>
 * <li><strong>Flow Control:</strong> Backpressure and rate limiting</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * 
 * <pre>{@code
 * // TCP with Java serialization
 * NetworkProtocol protocol = NetworkProtocol.tcp()
 *         .serialization(SerializationType.JAVA)
 *         .compression(CompressionType.GZIP)
 *         .encryption(true)
 *         .port(8080)
 *         .build();
 *
 * // Send cache operation
 * CacheOperation operation = new PutOperation("key1", "value1");
 * protocol.send("node-2", operation).thenAccept(response -> {
 *     System.out.println("Operation completed: " + response);
 * });
 *
 * // HTTP with JSON for cross-platform compatibility
 * NetworkProtocol httpProtocol = NetworkProtocol.http()
 *         .serialization(SerializationType.JSON)
 *         .port(8080)
 *         .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public interface NetworkProtocol {

    /**
     * Protocol types supported.
     */
    enum ProtocolType {
        TCP, // High performance, reliable
        UDP, // Low latency, best effort
        HTTP, // Cross-platform, firewall friendly
        WEBSOCKET // Bidirectional, real-time
    }

    /**
     * Serialization formats supported.
     */
    enum SerializationType {
        JAVA, // Native Java serialization
        JSON, // Human readable, cross-platform
        AVRO, // Schema evolution support
        PROTOBUF, // Compact, fast
        KRYO // Fast binary serialization
    }

    /**
     * Compression algorithms supported.
     */
    enum CompressionType {
        NONE,
        GZIP,
        LZ4,
        SNAPPY
    }

    /**
     * Sends a cache operation to the specified node.
     *
     * @param nodeId    the target node ID
     * @param operation the operation to send
     * @return CompletableFuture containing the response
     */
    CompletableFuture<OperationResponse> send(String nodeId, CacheOperation operation);

    /**
     * Broadcasts a cache operation to multiple nodes.
     *
     * @param nodeIds   the target node IDs
     * @param operation the operation to broadcast
     * @return CompletableFuture containing responses from all nodes
     */
    CompletableFuture<Map<String, OperationResponse>> broadcast(Collection<String> nodeIds, CacheOperation operation);

    /**
     * Registers a handler for incoming cache operations.
     *
     * @param handler the operation handler
     */
    void registerHandler(OperationHandler handler);

    /**
     * Starts the network protocol.
     *
     * @return CompletableFuture that completes when the protocol is started
     */
    CompletableFuture<Void> start();

    /**
     * Stops the network protocol.
     *
     * @return CompletableFuture that completes when the protocol is stopped
     */
    CompletableFuture<Void> stop();

    /**
     * Returns network statistics.
     *
     * @return network protocol statistics
     */
    NetworkStats getNetworkStats();

    /**
     * Creates a TCP protocol builder.
     *
     * @return TCP protocol builder
     */
    static ProtocolBuilder tcp() {
        return new ProtocolBuilder(ProtocolType.TCP);
    }

    /**
     * Creates a UDP protocol builder.
     *
     * @return UDP protocol builder
     */
    static ProtocolBuilder udp() {
        return new ProtocolBuilder(ProtocolType.UDP);
    }

    /**
     * Creates an HTTP protocol builder.
     *
     * @return HTTP protocol builder
     */
    static ProtocolBuilder http() {
        return new ProtocolBuilder(ProtocolType.HTTP);
    }

    /**
     * Creates a WebSocket protocol builder.
     *
     * @return WebSocket protocol builder
     */
    static ProtocolBuilder websocket() {
        return new ProtocolBuilder(ProtocolType.WEBSOCKET);
    }

    /**
     * Base class for cache operations.
     */
    abstract class CacheOperation implements Serializable {
        private final String operationId;
        private final Instant timestamp;
        private final String sourceNodeId;

        protected CacheOperation(String operationId, String sourceNodeId) {
            this.operationId = operationId;
            this.sourceNodeId = sourceNodeId;
            this.timestamp = Instant.now();
        }

        public String getOperationId() {
            return operationId;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public String getSourceNodeId() {
            return sourceNodeId;
        }

        public abstract OperationType getType();
    }

    /**
     * Types of cache operations.
     */
    enum OperationType {
        PUT, GET, REMOVE, CLEAR, INVALIDATE, HEARTBEAT, TOPOLOGY_UPDATE
    }

    /**
     * Put operation for distributed cache.
     */
    class PutOperation extends CacheOperation {
        private final Object key;
        private final Object value;
        private final DistributedCache.ConsistencyLevel consistencyLevel;

        public PutOperation(String operationId, String sourceNodeId, Object key, Object value,
                DistributedCache.ConsistencyLevel consistencyLevel) {
            super(operationId, sourceNodeId);
            this.key = key;
            this.value = value;
            this.consistencyLevel = consistencyLevel;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public DistributedCache.ConsistencyLevel getConsistencyLevel() {
            return consistencyLevel;
        }

        @Override
        public OperationType getType() {
            return OperationType.PUT;
        }
    }

    /**
     * Get operation for distributed cache.
     */
    class GetOperation extends CacheOperation {
        private final Object key;
        private final DistributedCache.ConsistencyLevel consistencyLevel;

        public GetOperation(String operationId, String sourceNodeId, Object key,
                DistributedCache.ConsistencyLevel consistencyLevel) {
            super(operationId, sourceNodeId);
            this.key = key;
            this.consistencyLevel = consistencyLevel;
        }

        public Object getKey() {
            return key;
        }

        public DistributedCache.ConsistencyLevel getConsistencyLevel() {
            return consistencyLevel;
        }

        @Override
        public OperationType getType() {
            return OperationType.GET;
        }
    }

    /**
     * Invalidate operation for distributed cache.
     */
    class InvalidateOperation extends CacheOperation {
        private final Collection<Object> keys;

        public InvalidateOperation(String operationId, String sourceNodeId, Collection<Object> keys) {
            super(operationId, sourceNodeId);
            this.keys = keys;
        }

        public Collection<Object> getKeys() {
            return keys;
        }

        @Override
        public OperationType getType() {
            return OperationType.INVALIDATE;
        }
    }

    /**
     * Response to a cache operation.
     */
    class OperationResponse implements Serializable {
        private final String operationId;
        private final boolean success;
        private final Object result;
        private final String errorMessage;
        private final Instant timestamp;

        public OperationResponse(String operationId, boolean success, Object result, String errorMessage) {
            this.operationId = operationId;
            this.success = success;
            this.result = result;
            this.errorMessage = errorMessage;
            this.timestamp = Instant.now();
        }

        public static OperationResponse success(String operationId, Object result) {
            return new OperationResponse(operationId, true, result, null);
        }

        public static OperationResponse failure(String operationId, String errorMessage) {
            return new OperationResponse(operationId, false, null, errorMessage);
        }

        public String getOperationId() {
            return operationId;
        }

        public boolean isSuccess() {
            return success;
        }

        public Object getResult() {
            return result;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Handler for incoming cache operations.
     */
    interface OperationHandler {
        CompletableFuture<OperationResponse> handle(CacheOperation operation);
    }

    /**
     * Network protocol statistics.
     */
    class NetworkStats {
        private final long messagesSent;
        private final long messagesReceived;
        private final long bytesTransmitted;
        private final long bytesReceived;
        private final double averageLatency;
        private final long connectionFailures;

        public NetworkStats(long messagesSent, long messagesReceived, long bytesTransmitted,
                long bytesReceived, double averageLatency, long connectionFailures) {
            this.messagesSent = messagesSent;
            this.messagesReceived = messagesReceived;
            this.bytesTransmitted = bytesTransmitted;
            this.bytesReceived = bytesReceived;
            this.averageLatency = averageLatency;
            this.connectionFailures = connectionFailures;
        }

        public long getMessagesSent() {
            return messagesSent;
        }

        public long getMessagesReceived() {
            return messagesReceived;
        }

        public long getBytesTransmitted() {
            return bytesTransmitted;
        }

        public long getBytesReceived() {
            return bytesReceived;
        }

        public double getAverageLatency() {
            return averageLatency;
        }

        public long getConnectionFailures() {
            return connectionFailures;
        }

        public double getThroughput() {
            return (double) (bytesTransmitted + bytesReceived) / 1024 / 1024; // MB/s
        }
    }

    /**
     * Builder for network protocols.
     */
    class ProtocolBuilder {
        private final ProtocolType protocolType;
        private SerializationType serializationType = SerializationType.JAVA;
        private CompressionType compressionType = CompressionType.NONE;
        private boolean encryptionEnabled = false;
        private int port = 8080;
        private int maxConnections = 100;
        private long connectionTimeoutMs = 5000;
        private int bufferSize = 8192;

        public ProtocolBuilder(ProtocolType protocolType) {
            this.protocolType = protocolType;
        }

        public ProtocolBuilder serialization(SerializationType serializationType) {
            this.serializationType = serializationType;
            return this;
        }

        public ProtocolBuilder compression(CompressionType compressionType) {
            this.compressionType = compressionType;
            return this;
        }

        public ProtocolBuilder encryption(boolean enabled) {
            this.encryptionEnabled = enabled;
            return this;
        }

        public ProtocolBuilder port(int port) {
            this.port = port;
            return this;
        }

        public ProtocolBuilder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public ProtocolBuilder connectionTimeout(long timeoutMs) {
            this.connectionTimeoutMs = timeoutMs;
            return this;
        }

        public ProtocolBuilder bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public NetworkProtocol build() {
            // Return a default implementation
            return new DefaultNetworkProtocol(this);
        }
    }
}
