package io.github.dhruv1110.jcachex.distributed;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of NetworkProtocol.
 * <p>
 * This is a basic implementation that can be extended for production use.
 * In a real implementation, this would handle actual network communication.
 * </p>
 */
public class DefaultNetworkProtocol implements NetworkProtocol {

    private final ProtocolBuilder config;
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong bytesTransmitted = new AtomicLong(0);
    private final AtomicLong bytesReceived = new AtomicLong(0);
    private final AtomicLong connectionFailures = new AtomicLong(0);
    private OperationHandler handler;

    public DefaultNetworkProtocol(ProtocolBuilder config) {
        this.config = config;
    }

    @Override
    public CompletableFuture<OperationResponse> send(String nodeId, CacheOperation operation) {
        messagesSent.incrementAndGet();
        // Simulate network operation
        return CompletableFuture.supplyAsync(() -> {
            // In real implementation, this would send over network
            return OperationResponse.success(operation.getOperationId(), "OK");
        });
    }

    @Override
    public CompletableFuture<Map<String, OperationResponse>> broadcast(Collection<String> nodeIds,
            CacheOperation operation) {
        Map<String, OperationResponse> responses = new ConcurrentHashMap<>();
        return CompletableFuture.supplyAsync(() -> {
            for (String nodeId : nodeIds) {
                responses.put(nodeId, OperationResponse.success(operation.getOperationId(), "OK"));
            }
            return responses;
        });
    }

    @Override
    public void registerHandler(OperationHandler handler) {
        this.handler = handler;
    }

    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public NetworkStats getNetworkStats() {
        return new NetworkStats(
                messagesSent.get(),
                messagesReceived.get(),
                bytesTransmitted.get(),
                bytesReceived.get(),
                10.0, // Average latency in ms
                connectionFailures.get());
    }
}
