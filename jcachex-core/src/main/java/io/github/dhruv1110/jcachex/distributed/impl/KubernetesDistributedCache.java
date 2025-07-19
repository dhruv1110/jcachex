package io.github.dhruv1110.jcachex.distributed.impl;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheStats;
import io.github.dhruv1110.jcachex.distributed.communication.CommunicationProtocol;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery.DiscoveredNode;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery.NodeDiscoveryListener;

import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

/**
 * Kubernetes-specific distributed cache implementation.
 * <p>
 * This implementation extends {@link AbstractDistributedCache} and provides
 * Kubernetes-specific node discovery and identification. It integrates
 * seamlessly
 * with Kubernetes environments using pod names and service discovery.
 * </p>
 *
 * <h3>Kubernetes Integration Features:</h3>
 * <ul>
 * <li><strong>Pod Name Identification:</strong> Uses Kubernetes pod names as
 * node IDs</li>
 * <li><strong>Service Discovery:</strong> Integrates with Kubernetes service
 * discovery</li>
 * <li><strong>Pod Lifecycle:</strong> Handles pod restarts and scaling events
 * gracefully</li>
 * <li><strong>Resource Management:</strong> Respects Kubernetes resource
 * limits</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @since 1.0.0
 */
public class KubernetesDistributedCache<K, V> extends AbstractDistributedCache<K, V> {
    private static final Logger logger = Logger.getLogger(KubernetesDistributedCache.class.getName());

    private KubernetesDistributedCache(Builder<K, V> builder) {
        super(builder);
        logger.info("KubernetesDistributedCache initialized for cluster: " + clusterName +
                ", nodeId: " + currentNodeId);
    }

    @Override
    protected String generateNodeId() {
        // Use Kubernetes pod name if available (from HOSTNAME environment variable)
        String podName = System.getenv("HOSTNAME");
        if (podName != null && !podName.isEmpty()) {
            logger.info("Using Kubernetes pod name as node ID: " + podName);
            return podName;
        }

        // Fallback to hostname-based ID
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String nodeId = "k8s-" + hostname + "-" + System.currentTimeMillis();
            logger.info("Using fallback hostname-based node ID: " + nodeId);
            return nodeId;
        } catch (Exception e) {
            // Last resort: generate based on current time
            String nodeId = "k8s-node-" + System.currentTimeMillis();
            logger.warning("Using generated node ID: " + nodeId);
            return nodeId;
        }
    }

    @Override
    protected void initializeCluster() {
        logger.info("Initializing Kubernetes cluster discovery for: " + clusterName);

        // Set up node discovery listener
        nodeDiscovery.addNodeDiscoveryListener(new NodeDiscoveryListener() {
            @Override
            public void onNodeDiscovered(DiscoveredNode node) {
                logger.info("New Kubernetes node discovered: " + node.getNodeId());
                handleNodeJoined(node);
            }

            @Override
            public void onNodeLost(String nodeId) {
                logger.info("Kubernetes node lost: " + nodeId);
                handleNodeLeft(nodeId);
            }

            @Override
            public void onNodeHealthChanged(String nodeId,
                    NodeDiscovery.NodeHealth oldHealth,
                    NodeDiscovery.NodeHealth newHealth) {
                logger.info("Kubernetes node health changed: " + nodeId +
                        " from " + oldHealth + " to " + newHealth);
                if (newHealth == NodeDiscovery.NodeHealth.HEALTHY) {
                    // Treat healthy nodes as rejoined
                    nodeDiscovery.discoverNodes().thenAccept(nodes -> {
                        nodes.stream()
                                .filter(n -> n.getNodeId().equals(nodeId))
                                .findFirst()
                                .ifPresent(this::onNodeDiscovered);
                    });
                } else {
                    handleNodeLeft(nodeId);
                }
            }
        });

        // Start node discovery
        nodeDiscovery.start();

        logger.info("Kubernetes cluster discovery initialized successfully");
    }

    private void handleNodeJoined(DiscoveredNode node) {
        String nodeId = node.getNodeId();

        // Update cluster state
        clusterNodes.put(nodeId, new NodeInfo(nodeId, node.getAddress() + ":" + node.getPort(),
                NodeStatus.HEALTHY, System.currentTimeMillis(), java.util.Collections.emptySet()));
        healthyNodes.add(nodeId);

        // Update consistent hash ring
        hashRing.addNode(nodeId);
        topologyVersion.incrementAndGet();

        logger.info("Node joined cluster: " + nodeId + " at " + node.getAddress() + ":" + node.getPort());
    }

    private void handleNodeLeft(String nodeId) {
        // Remove from cluster state
        clusterNodes.remove(nodeId);
        healthyNodes.remove(nodeId);

        // Update consistent hash ring
        hashRing.removeNode(nodeId);
        topologyVersion.incrementAndGet();

        logger.info("Node left cluster: " + nodeId);
    }

    @Override
    protected V getFromRemoteNode(String nodeId, K key) {
        if (communicationProtocol == null) {
            logger.warning("No communication protocol configured for remote get");
            return null;
        }

        NodeInfo nodeInfo = clusterNodes.get(nodeId);
        if (nodeInfo == null) {
            logger.warning("Node not found in cluster: " + nodeId);
            return null;
        }

        try {
            networkRequests.incrementAndGet();
            long startTime = System.currentTimeMillis();

            CompletableFuture<CommunicationProtocol.CommunicationResult<V>> future = communicationProtocol.sendGet(
                    nodeInfo.getAddress(),
                    key);

            CommunicationProtocol.CommunicationResult<V> result = future.get(networkTimeout.toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS);

            // Record latency
            long latency = System.currentTimeMillis() - startTime;
            perNodeLatencies.computeIfAbsent(nodeId, k -> new java.util.concurrent.atomic.AtomicLong())
                    .addAndGet(latency);

            if (result.isSuccess() && result.getResult() != null) {
                return result.getResult();
            }
            return null;
        } catch (Exception e) {
            networkFailures.incrementAndGet();
            logger.warning("Failed to get key " + key + " from node " + nodeId + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void putToRemoteNode(String nodeId, K key, V value) {
        if (communicationProtocol == null) {
            logger.warning("No communication protocol configured for remote put");
            return;
        }

        NodeInfo nodeInfo = clusterNodes.get(nodeId);
        if (nodeInfo == null) {
            logger.warning("Node not found in cluster: " + nodeId);
            return;
        }

        try {
            networkRequests.incrementAndGet();
            long startTime = System.currentTimeMillis();

            CompletableFuture<CommunicationProtocol.CommunicationResult<Void>> future = communicationProtocol.sendPut(
                    nodeInfo.getAddress(),
                    key,
                    value);

            CommunicationProtocol.CommunicationResult<Void> result = future.get(networkTimeout.toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS);

            // Record latency
            long latency = System.currentTimeMillis() - startTime;
            perNodeLatencies.computeIfAbsent(nodeId, k -> new java.util.concurrent.atomic.AtomicLong())
                    .addAndGet(latency);

            if (!result.isSuccess()) {
                throw new RuntimeException("Remote put failed: "
                        + (result.getError() != null ? result.getError().getMessage() : "Unknown error"));
            }

        } catch (Exception e) {
            networkFailures.incrementAndGet();
            logger.warning("Failed to put key " + key + " to node " + nodeId + ": " + e.getMessage());
            throw new RuntimeException("Remote put failed", e);
        }
    }

    @Override
    protected V removeFromRemoteNode(String nodeId, K key) {
        if (communicationProtocol == null) {
            logger.warning("No communication protocol configured for remote remove");
            return null;
        }

        NodeInfo nodeInfo = clusterNodes.get(nodeId);
        if (nodeInfo == null) {
            logger.warning("Node not found in cluster: " + nodeId);
            return null;
        }

        try {
            networkRequests.incrementAndGet();
            long startTime = System.currentTimeMillis();

            CompletableFuture<CommunicationProtocol.CommunicationResult<V>> future = communicationProtocol.sendRemove(
                    nodeInfo.getAddress(),
                    key);

            CommunicationProtocol.CommunicationResult<V> result = future.get(networkTimeout.toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS);

            // Record latency
            long latency = System.currentTimeMillis() - startTime;
            perNodeLatencies.computeIfAbsent(nodeId, k -> new java.util.concurrent.atomic.AtomicLong())
                    .addAndGet(latency);

            if (result.isSuccess() && result.getResult() != null) {
                return result.getResult();
            }
            return null;
        } catch (Exception e) {
            networkFailures.incrementAndGet();
            logger.warning("Failed to remove key " + key + " from node " + nodeId + ": " + e.getMessage());
            return null;
        }
    }

    // ============= Kubernetes-Specific Methods =============

    /**
     * Get the current Kubernetes pod name (if running in a pod).
     */
    public String getPodName() {
        return System.getenv("HOSTNAME");
    }

    /**
     * Get the current Kubernetes namespace (if available).
     */
    public String getNamespace() {
        return System.getenv("POD_NAMESPACE");
    }

    /**
     * Check if running inside a Kubernetes pod.
     */
    public boolean isRunningInKubernetes() {
        return System.getenv("KUBERNETES_SERVICE_HOST") != null;
    }

    // ============= DistributedCache Implementation =============

    @Override
    public CompletableFuture<Void> putWithConsistency(K key, V value, ConsistencyLevel consistencyLevel) {
        // Use the base class implementation which handles routing via consistent
        // hashing
        return CompletableFuture.runAsync(() -> put(key, value), distributionExecutor);
    }

    @Override
    public CompletableFuture<V> getWithConsistency(K key, ConsistencyLevel consistencyLevel) {
        // Use the base class implementation which handles routing via consistent
        // hashing
        return CompletableFuture.supplyAsync(() -> get(key), distributionExecutor);
    }

    @Override
    public CompletableFuture<Void> invalidateGlobally(K key) {
        return CompletableFuture.runAsync(() -> {
            // First remove from local cache
            remove(key);

            // Broadcast invalidation to all healthy nodes in parallel
            List<CompletableFuture<Void>> invalidationFutures = new ArrayList<>();

            for (String nodeId : healthyNodes) {
                if (!currentNodeId.equals(nodeId)) { // Skip self
                    CompletableFuture<Void> invalidationFuture = CompletableFuture.runAsync(() -> {
                        try {
                            sendInvalidationToNode(nodeId, key);
                        } catch (Exception e) {
                            logger.warning(
                                    "Failed to invalidate key " + key + " on node " + nodeId + ": " + e.getMessage());
                        }
                    }, distributionExecutor);
                    invalidationFutures.add(invalidationFuture);
                }
            }

            // Wait for all invalidations to complete
            CompletableFuture.allOf(invalidationFutures.toArray(new CompletableFuture[0])).join();

            logger.info("Global invalidation completed for key: " + key + " across " + healthyNodes.size() + " nodes");
        }, distributionExecutor);
    }

    @Override
    public CompletableFuture<Void> invalidateGlobally(java.util.Collection<K> keys) {
        return CompletableFuture.runAsync(() -> {
            // First remove from local cache
            for (K key : keys) {
                remove(key);
            }

            // Broadcast invalidation to all healthy nodes in parallel
            List<CompletableFuture<Void>> invalidationFutures = new ArrayList<>();

            for (String nodeId : healthyNodes) {
                if (!currentNodeId.equals(nodeId)) { // Skip self
                    CompletableFuture<Void> invalidationFuture = CompletableFuture.runAsync(() -> {
                        try {
                            sendBatchInvalidationToNode(nodeId, keys);
                        } catch (Exception e) {
                            logger.warning("Failed to invalidate " + keys.size() + " keys on node " + nodeId + ": "
                                    + e.getMessage());
                        }
                    }, distributionExecutor);
                    invalidationFutures.add(invalidationFuture);
                }
            }

            // Wait for all invalidations to complete
            CompletableFuture.allOf(invalidationFutures.toArray(new CompletableFuture[0])).join();

            logger.info("Global invalidation completed for " + keys.size() + " keys across " + healthyNodes.size()
                    + " nodes");
        }, distributionExecutor);
    }

    @Override
    public CompletableFuture<Void> clearGlobally() {
        return CompletableFuture.runAsync(() -> {
            // First clear local cache
            clear();

            // Broadcast clear to all healthy nodes in parallel
            List<CompletableFuture<Void>> clearFutures = new ArrayList<>();

            for (String nodeId : healthyNodes) {
                if (!currentNodeId.equals(nodeId)) { // Skip self
                    CompletableFuture<Void> clearFuture = CompletableFuture.runAsync(() -> {
                        try {
                            sendClearToNode(nodeId);
                        } catch (Exception e) {
                            logger.warning("Failed to clear cache on node " + nodeId + ": " + e.getMessage());
                        }
                    }, distributionExecutor);
                    clearFutures.add(clearFuture);
                }
            }

            // Wait for all clears to complete
            CompletableFuture.allOf(clearFutures.toArray(new CompletableFuture[0])).join();

            logger.info("Global clear operation completed across " + healthyNodes.size() + " nodes");
        }, distributionExecutor);
    }

    public CompletableFuture<Void> rebalance() {
        return rebalance(healthyNodes);
    }

    public CompletableFuture<Void> rebalance(Set<String> healthyNodes) {
        return CompletableFuture.runAsync(() -> {
            logger.info("Rebalancing Kubernetes cluster with nodes: " + healthyNodes);

            // Update hash ring with current healthy nodes
            NodeUpdateResult result = hashRing.rebalance(healthyNodes);

            // Update topology version
            topologyVersion.incrementAndGet();

            logger.info("Kubernetes cluster rebalancing completed. Affected ranges: " +
                    result.affectedRanges.size());
        }, distributionExecutor);
    }

    public CompletableFuture<Void> migrateData(String fromNode, String toNode, Set<String> keys) {
        return CompletableFuture.runAsync(() -> {
            logger.info("Starting data migration from " + fromNode + " to " + toNode + " for " + keys.size() + " keys");

            if (communicationProtocol == null) {
                logger.severe("No communication protocol configured for data migration");
                return;
            }

            NodeInfo fromNodeInfo = clusterNodes.get(fromNode);
            NodeInfo toNodeInfo = clusterNodes.get(toNode);

            if (fromNodeInfo == null) {
                logger.warning("Source node not found for migration: " + fromNode);
                return;
            }

            if (toNodeInfo == null) {
                logger.warning("Target node not found for migration: " + toNode);
                return;
            }

            int migratedCount = 0;
            int failedCount = 0;

            // Migrate each key from source to target node
            for (String keyStr : keys) {
                try {
                    @SuppressWarnings("unchecked")
                    K key = (K) keyStr;

                    // Get value from source node
                    CompletableFuture<CommunicationProtocol.CommunicationResult<V>> getFuture = communicationProtocol
                            .sendGet(fromNodeInfo.getAddress(), key);

                    CommunicationProtocol.CommunicationResult getResult = getFuture.get(networkTimeout.toMillis(),
                            java.util.concurrent.TimeUnit.MILLISECONDS);

                    if (getResult.isSuccess() && getResult.getResult() != null) {
                        @SuppressWarnings("unchecked")
                        V value = (V) getResult.getResult();

                        // Put value to target node
                        CompletableFuture<CommunicationProtocol.CommunicationResult<Void>> putFuture = communicationProtocol
                                .sendPut(toNodeInfo.getAddress(), key, value);

                        CommunicationProtocol.CommunicationResult<Void> putResult = putFuture.get(
                                networkTimeout.toMillis(),
                                java.util.concurrent.TimeUnit.MILLISECONDS);

                        if (putResult.isSuccess()) {
                            // Remove from source node after successful put
                            CompletableFuture<CommunicationProtocol.CommunicationResult<V>> removeFuture = communicationProtocol
                                    .sendRemove(fromNodeInfo.getAddress(), key);

                            removeFuture.get(networkTimeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
                            migratedCount++;

                            if (migratedCount % 100 == 0) {
                                logger.info(
                                        "Migration progress: " + migratedCount + "/" + keys.size() + " keys migrated");
                            }
                        } else {
                            failedCount++;
                            logger.warning("Failed to put key " + key + " to target node " + toNode);
                        }
                    } else {
                        // Key not found on source node, skip
                        logger.fine("Key " + key + " not found on source node " + fromNode);
                    }

                } catch (Exception e) {
                    failedCount++;
                    logger.warning("Failed to migrate key: " + e.getMessage());
                }
            }

            logger.info("Data migration completed from " + fromNode + " to " + toNode +
                    ". Migrated: " + migratedCount + ", Failed: " + failedCount);
        }, distributionExecutor);
    }

    @Override
    public CompletableFuture<Void> addNode(String nodeAddress) {
        return CompletableFuture.runAsync(() -> {
            String nodeId = "manual-" + nodeAddress.hashCode();

            // Parse address and port
            String[] parts = nodeAddress.split(":");
            String address = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 8080;

            clusterNodes.put(nodeId, new NodeInfo(nodeId, address + ":" + port, NodeStatus.HEALTHY,
                    System.currentTimeMillis(), java.util.Collections.emptySet()));
            healthyNodes.add(nodeId);
            hashRing.addNode(nodeId);
            topologyVersion.incrementAndGet();

            logger.info("Manually added node: " + nodeId + " at " + nodeAddress);
        }, distributionExecutor);
    }

    @Override
    public CompletableFuture<Void> removeNode(String nodeId) {
        return CompletableFuture.runAsync(() -> {
            clusterNodes.remove(nodeId);
            healthyNodes.remove(nodeId);
            hashRing.removeNode(nodeId);
            topologyVersion.incrementAndGet();

            logger.info("Removed node: " + nodeId);
        }, distributionExecutor);
    }

    @Override
    public ClusterTopology getClusterTopology() {
        java.util.Set<NodeInfo> nodeInfos = new java.util.HashSet<>();
        for (NodeInfo node : clusterNodes.values()) {
            nodeInfos.add(node);
        }
        return new ClusterTopology(clusterName, nodeInfos, partitionCount, topologyVersion.get());
    }

    @Override
    public java.util.Map<String, CacheStats> getPerNodeStats() {
        java.util.Map<String, CacheStats> stats = new java.util.concurrent.ConcurrentHashMap<>();

        // Add local node stats
        stats.put(currentNodeId, localCache.stats());

        // Collect stats from remote nodes in parallel
        List<CompletableFuture<Void>> statsFutures = new ArrayList<>();

        for (String nodeId : healthyNodes) {
            if (!currentNodeId.equals(nodeId)) { // Skip self
                CompletableFuture<Void> statsFuture = CompletableFuture.runAsync(() -> {
                    try {
                        CacheStats remoteStats = collectStatsFromNode(nodeId);
                        if (remoteStats != null) {
                            stats.put(nodeId, remoteStats);
                        }
                    } catch (Exception e) {
                        logger.warning("Failed to collect stats from node " + nodeId + ": " + e.getMessage());
                        // Create a placeholder stats object for failed nodes
                        stats.put(nodeId, createEmptyStats());
                    }
                }, distributionExecutor);
                statsFutures.add(statsFuture);
            }
        }

        // Wait for all stats collection to complete (with timeout)
        try {
            CompletableFuture.allOf(statsFutures.toArray(new CompletableFuture[0]))
                    .get(networkTimeout.toMillis() * 2, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.warning("Stats collection timed out for some nodes: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Collect cache statistics from a remote node.
     */
    private CacheStats collectStatsFromNode(String nodeId) {
        if (communicationProtocol == null) {
            logger.warning("No communication protocol configured for stats collection");
            return null;
        }

        NodeInfo nodeInfo = clusterNodes.get(nodeId);
        if (nodeInfo == null) {
            logger.warning("Node not found for stats collection: " + nodeId);
            return null;
        }

        try {
            // Use a special health check to get basic stats
            // In a more sophisticated implementation, you could extend the protocol for
            // detailed stats
            CompletableFuture<CommunicationProtocol.CommunicationResult<String>> future = communicationProtocol
                    .sendHealthCheck(nodeInfo.getAddress());

            CommunicationProtocol.CommunicationResult<String> result = future.get(networkTimeout.toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS);

            if (result.isSuccess() && result.getResult() != null) {
                return parseStatsFromHealthResponse(result.getResult());
            } else {
                logger.warning("Failed to get stats from node " + nodeId);
                return createEmptyStats();
            }

        } catch (Exception e) {
            logger.warning("Failed to collect stats from node " + nodeId + ": " + e.getMessage());
            return createEmptyStats();
        }
    }

    /**
     * Parse basic stats from health check response.
     */
    private CacheStats parseStatsFromHealthResponse(String response) {
        try {
            // Parse response format: "OK|nodeId|size|memoryUsed|maxMemory"
            String[] parts = response.split("\\|");
            if (parts.length >= 3) {
                long size = Long.parseLong(parts[2]);
                // Create basic stats with available information - using hit count as size for
                // now
                return new CacheStats(
                        new java.util.concurrent.atomic.AtomicLong(size), // hitCount (reusing as size)
                        new java.util.concurrent.atomic.AtomicLong(0), // missCount
                        new java.util.concurrent.atomic.AtomicLong(0), // evictionCount
                        new java.util.concurrent.atomic.AtomicLong(0), // loadCount
                        new java.util.concurrent.atomic.AtomicLong(0), // loadFailureCount
                        new java.util.concurrent.atomic.AtomicLong(0) // totalLoadTime
                );
            }
        } catch (Exception e) {
            logger.fine("Failed to parse stats from health response: " + e.getMessage());
        }
        return createEmptyStats();
    }

    /**
     * Create empty stats for nodes that are unreachable.
     */
    private CacheStats createEmptyStats() {
        return new CacheStats(
                new java.util.concurrent.atomic.AtomicLong(0), // hitCount
                new java.util.concurrent.atomic.AtomicLong(0), // missCount
                new java.util.concurrent.atomic.AtomicLong(0), // evictionCount
                new java.util.concurrent.atomic.AtomicLong(0), // loadCount
                new java.util.concurrent.atomic.AtomicLong(0), // loadFailureCount
                new java.util.concurrent.atomic.AtomicLong(0) // totalLoadTime
        );
    }

    @Override
    public java.util.Map<String, NodeStatus> getNodeStatuses() {
        java.util.Map<String, NodeStatus> statuses = new java.util.concurrent.ConcurrentHashMap<>();
        for (String nodeId : healthyNodes) {
            statuses.put(nodeId, NodeStatus.HEALTHY);
        }
        // Mark missing nodes as unhealthy
        for (String nodeId : clusterNodes.keySet()) {
            if (!healthyNodes.contains(nodeId)) {
                statuses.put(nodeId, NodeStatus.FAILED);
            }
        }
        return statuses;
    }

    @Override
    public DistributedMetrics getDistributedMetrics() {
        java.util.Map<String, Long> latencies = new java.util.HashMap<>();
        perNodeLatencies.forEach((node, latency) -> latencies.put(node, latency.get()));

        return new DistributedMetrics(
                networkRequests.get(),
                networkFailures.get(),
                calculateAverageNetworkLatency(),
                replicationLag.get(),
                conflictResolutions.get(),
                latencies);
    }

    @Override
    public int getReplicationFactor() {
        // For true distributed cache, each key exists on exactly one node
        return 1;
    }

    @Override
    public void setReadRepairEnabled(boolean enabled) {
        // Inherited from base class
    }

    // ============= Async Cache Methods =============

    @Override
    public CompletableFuture<V> getAsync(K key) {
        return CompletableFuture.supplyAsync(() -> get(key), distributionExecutor);
    }

    @Override
    public CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.runAsync(() -> put(key, value), distributionExecutor);
    }

    @Override
    public CompletableFuture<V> removeAsync(K key) {
        return CompletableFuture.supplyAsync(() -> remove(key), distributionExecutor);
    }

    @Override
    public CompletableFuture<Void> clearAsync() {
        return CompletableFuture.runAsync(() -> clear(), distributionExecutor);
    }

    // ============= Helper Methods =============

    private double calculateAverageNetworkLatency() {
        if (perNodeLatencies.isEmpty()) {
            return 0.0;
        }
        long total = perNodeLatencies.values().stream().mapToLong(java.util.concurrent.atomic.AtomicLong::get).sum();
        return (double) total / perNodeLatencies.size();
    }

    /**
     * Send invalidation command for a single key to a remote node.
     */
    private void sendInvalidationToNode(String nodeId, K key) {
        if (communicationProtocol == null) {
            logger.warning("No communication protocol configured for invalidation");
            return;
        }

        NodeInfo nodeInfo = clusterNodes.get(nodeId);
        if (nodeInfo == null) {
            logger.warning("Node not found for invalidation: " + nodeId);
            return;
        }

        try {
            networkRequests.incrementAndGet();

            // Use remove operation to invalidate the key on remote node
            CompletableFuture<CommunicationProtocol.CommunicationResult<V>> future = communicationProtocol.sendRemove(
                    nodeInfo.getAddress(),
                    key);

            CommunicationProtocol.CommunicationResult<V> result = future.get(networkTimeout.toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS);

            if (!result.isSuccess()) {
                networkFailures.incrementAndGet();
                logger.warning("Invalidation failed on node " + nodeId + ": " +
                        (result.getError() != null ? result.getError().getMessage() : "Unknown error"));
            }

        } catch (Exception e) {
            networkFailures.incrementAndGet();
            logger.warning("Failed to send invalidation to node " + nodeId + ": " + e.getMessage());
        }
    }

    /**
     * Send batch invalidation command for multiple keys to a remote node.
     */
    private void sendBatchInvalidationToNode(String nodeId, java.util.Collection<K> keys) {
        if (communicationProtocol == null) {
            logger.warning("No communication protocol configured for batch invalidation");
            return;
        }

        // For batch operations, send individual remove commands
        // In a more sophisticated implementation, you could extend the protocol to
        // support batch operations
        for (K key : keys) {
            sendInvalidationToNode(nodeId, key);
        }
    }

    /**
     * Send clear command to a remote node.
     */
    private void sendClearToNode(String nodeId) {
        if (communicationProtocol == null) {
            logger.warning("No communication protocol configured for clear");
            return;
        }

        NodeInfo nodeInfo = clusterNodes.get(nodeId);
        if (nodeInfo == null) {
            logger.warning("Node not found for clear: " + nodeId);
            return;
        }

        try {
            networkRequests.incrementAndGet();

            // Use a special key to signal a clear operation
            // This is a simplified approach; in production you might extend the protocol
            CompletableFuture<CommunicationProtocol.CommunicationResult<Void>> future = communicationProtocol.sendPut(
                    nodeInfo.getAddress(),
                    (K) "__CLEAR_CACHE__", // Special sentinel key
                    (V) "true");

            CommunicationProtocol.CommunicationResult<Void> result = future.get(networkTimeout.toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS);

            if (!result.isSuccess()) {
                networkFailures.incrementAndGet();
                logger.warning("Clear failed on node " + nodeId + ": " +
                        (result.getError() != null ? result.getError().getMessage() : "Unknown error"));
            }

        } catch (Exception e) {
            networkFailures.incrementAndGet();
            logger.warning("Failed to send clear to node " + nodeId + ": " + e.getMessage());
        }
    }

    @Override
    public CacheStats stats() {
        return localCache.stats();
    }

    @Override
    public CacheConfig<K, V> config() {
        return localCache.config();
    }

    // ============= Builder =============

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    public static class Builder<K, V> extends AbstractDistributedCache.Builder<K, V> {

        @Override
        public KubernetesDistributedCache<K, V> build() {
            // Validate Kubernetes-specific requirements
            if (nodeDiscovery == null) {
                throw new IllegalStateException("NodeDiscovery is required for Kubernetes distributed cache");
            }

            if (communicationProtocol == null) {
                throw new IllegalStateException("CommunicationProtocol is required for Kubernetes distributed cache");
            }

            return new KubernetesDistributedCache<>(this);
        }

        // Kubernetes-specific builder methods can be added here if needed
        public Builder<K, V> podNamePrefix(String prefix) {
            // Custom configuration for Kubernetes pod naming
            return this;
        }

        public Builder<K, V> kubernetesNamespace(String namespace) {
            // Custom configuration for Kubernetes namespace
            return this;
        }
    }
}
