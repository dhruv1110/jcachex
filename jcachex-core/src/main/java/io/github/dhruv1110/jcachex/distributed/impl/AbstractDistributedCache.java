package io.github.dhruv1110.jcachex.distributed.impl;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.JCacheXBuilder;
import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.communication.CommunicationProtocol;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Abstract base class for distributed cache implementations.
 * <p>
 * This class provides common functionality for all distributed cache types
 * including:
 * consistent hashing with virtual nodes, memory management, metrics tracking,
 * and pluggable communication protocols.
 * </p>
 *
 * <h3>Common Features:</h3>
 * <ul>
 * <li><strong>Consistent Hashing:</strong> Virtual nodes for even data
 * distribution</li>
 * <li><strong>Memory Management:</strong> Per-node memory limits with
 * eviction</li>
 * <li><strong>Metrics:</strong> Comprehensive metrics collection</li>
 * <li><strong>Communication:</strong> Pluggable communication protocols</li>
 * <li><strong>Node Management:</strong> Dynamic cluster topology
 * management</li>
 * </ul>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @since 1.0.0
 */
public abstract class AbstractDistributedCache<K, V> implements DistributedCache<K, V> {
    private static final Logger logger = Logger.getLogger(AbstractDistributedCache.class.getName());

    // Configuration
    protected final String clusterName;
    protected final int partitionCount;
    protected final Duration networkTimeout;
    protected final long maxMemoryBytes;
    protected final boolean readRepairEnabled;
    protected final String currentNodeId;

    // Core components
    protected final Cache<K, V> localCache;
    protected final ConsistentHashRing hashRing;
    protected final CommunicationProtocol<K, V> communicationProtocol;
    protected final NodeDiscovery nodeDiscovery;

    // Executors
    protected final ExecutorService distributionExecutor;
    protected final ScheduledExecutorService discoveryScheduler;

    // State management
    protected final AtomicBoolean isRunning = new AtomicBoolean(false);
    protected final Set<String> healthyNodes = new CopyOnWriteArraySet<>();
    protected final Map<String, NodeInfo> clusterNodes = new ConcurrentHashMap<>();
    protected final AtomicLong topologyVersion = new AtomicLong(0);

    // Memory management
    protected final AtomicLong currentMemoryBytes = new AtomicLong(0);

    // Metrics
    protected final AtomicLong networkRequests = new AtomicLong(0);
    protected final AtomicLong networkFailures = new AtomicLong(0);
    protected final AtomicLong replicationLag = new AtomicLong(0);
    protected final AtomicLong conflictResolutions = new AtomicLong(0);
    protected final Map<String, AtomicLong> perNodeLatencies = new ConcurrentHashMap<>();

    protected AbstractDistributedCache(Builder<K, V> builder) {
        this.clusterName = builder.clusterName;
        this.partitionCount = builder.partitionCount;
        this.networkTimeout = builder.networkTimeout;
        this.maxMemoryBytes = builder.maxMemoryBytes;
        this.readRepairEnabled = builder.readRepairEnabled;
        this.nodeDiscovery = builder.nodeDiscovery;
        this.communicationProtocol = builder.communicationProtocol;

        // Initialize current node ID
        this.currentNodeId = generateNodeId();

        // Initialize local cache
        this.localCache = JCacheXBuilder.<K, V>forHighPerformance().build();

        // Initialize consistent hash ring with virtual nodes
        this.hashRing = new ConsistentHashRing(builder.virtualNodesPerNode);

        // Initialize executors
        this.distributionExecutor = Executors.newFixedThreadPool(20, r -> {
            Thread t = new Thread(r, "distribution-worker");
            t.setDaemon(true);
            return t;
        });
        this.discoveryScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "discovery-" + clusterName);
            t.setDaemon(true);
            return t;
        });

        // Initialize cluster and communication
        initializeCluster();
        initializeCommunication();
        this.isRunning.set(true);

        logger.info("AbstractDistributedCache initialized for cluster: " + clusterName +
                ", nodeId: " + currentNodeId + ", protocol: " + communicationProtocol.getProtocolType());
    }

    /**
     * Generate a unique node ID for this cache instance.
     * Subclasses can override this for platform-specific node identification.
     */
    protected String generateNodeId() {
        return "node-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    /**
     * Initialize cluster-specific functionality.
     * Subclasses should implement this for their specific discovery mechanism.
     */
    protected abstract void initializeCluster();

    /**
     * Initialize communication protocol.
     */
    private void initializeCommunication() {
        if (communicationProtocol != null) {
            // Set the local cache instance for handling incoming requests
            communicationProtocol.setLocalCache(this.localCache);

            communicationProtocol.startServer().thenRun(() -> {
                logger.info("Communication protocol started: " + communicationProtocol.getProtocolType());
            }).exceptionally(throwable -> {
                logger.severe("Failed to start communication protocol: " + throwable.getMessage());
                return null;
            });
        }
    }

    // ============= Enhanced Consistent Hash Ring with VNodes =============

    protected static class ConsistentHashRing {
        private final int virtualNodesPerNode;
        private final TreeMap<Long, String> ring = new TreeMap<>();
        private final Map<String, Set<Long>> nodeToVNodes = new ConcurrentHashMap<>();
        private static final Logger logger = Logger.getLogger(ConsistentHashRing.class.getName());

        public ConsistentHashRing(int virtualNodesPerNode) {
            this.virtualNodesPerNode = virtualNodesPerNode;
            logger.info(
                    "Initialized ConsistentHashRing with " + virtualNodesPerNode + " virtual nodes per physical node");
        }

        public synchronized NodeUpdateResult addNode(String nodeId) {
            if (nodeToVNodes.containsKey(nodeId)) {
                logger.warning("Node " + nodeId + " already exists in the ring");
                return new NodeUpdateResult(Collections.emptyMap(), Collections.emptySet());
            }

            Set<Long> vnodes = new HashSet<>();
            Map<Long, String> affectedRanges = new HashMap<>();

            // Add virtual nodes for this physical node
            for (int i = 0; i < virtualNodesPerNode; i++) {
                long vnode = hash(nodeId + ":" + i);
                vnodes.add(vnode);

                // Find the previous owner of this range
                Map.Entry<Long, String> successor = ring.ceilingEntry(vnode);
                if (successor == null) {
                    successor = ring.firstEntry();
                }

                if (successor != null) {
                    affectedRanges.put(vnode, successor.getValue());
                }

                ring.put(vnode, nodeId);
            }

            nodeToVNodes.put(nodeId, vnodes);

            logger.info("Added node " + nodeId + " with " + vnodes.size() + " virtual nodes. Affected ranges: "
                    + affectedRanges.size());
            return new NodeUpdateResult(affectedRanges, Collections.singleton(nodeId));
        }

        public synchronized NodeUpdateResult removeNode(String nodeId) {
            Set<Long> vnodes = nodeToVNodes.remove(nodeId);
            if (vnodes == null) {
                logger.warning("Node " + nodeId + " not found in the ring");
                return new NodeUpdateResult(Collections.emptyMap(), Collections.emptySet());
            }

            Map<Long, String> affectedRanges = new HashMap<>();
            Set<String> newOwners = new HashSet<>();

            for (Long vnode : vnodes) {
                ring.remove(vnode);

                // Find the new owner of this range
                Map.Entry<Long, String> successor = ring.ceilingEntry(vnode);
                if (successor == null) {
                    successor = ring.firstEntry();
                }

                if (successor != null) {
                    affectedRanges.put(vnode, successor.getValue());
                    newOwners.add(successor.getValue());
                }
            }

            logger.info("Removed node " + nodeId + " with " + vnodes.size() +
                    " virtual nodes. Data will be redistributed to " + newOwners.size() + " nodes");
            return new NodeUpdateResult(affectedRanges, newOwners);
        }

        public synchronized String getNodeForKey(String key) {
            if (ring.isEmpty()) {
                return null;
            }
            long hash = hash(key);
            Map.Entry<Long, String> entry = ring.ceilingEntry(hash);
            if (entry == null)
                entry = ring.firstEntry();
            return entry.getValue();
        }

        public synchronized NodeUpdateResult rebalance(Set<String> availableNodes) {
            logger.info("Rebalancing ring with nodes: " + availableNodes);

            Set<String> currentNodes = new HashSet<>(nodeToVNodes.keySet());
            Set<String> nodesToAdd = new HashSet<>(availableNodes);
            Set<String> nodesToRemove = new HashSet<>(currentNodes);

            nodesToAdd.removeAll(currentNodes);
            nodesToRemove.removeAll(availableNodes);

            Map<Long, String> allAffectedRanges = new HashMap<>();
            Set<String> allAffectedNodes = new HashSet<>();

            // Remove nodes that are no longer available
            for (String nodeId : nodesToRemove) {
                NodeUpdateResult result = removeNode(nodeId);
                allAffectedRanges.putAll(result.affectedRanges);
                allAffectedNodes.addAll(result.affectedNodes);
            }

            // Add new nodes
            for (String nodeId : nodesToAdd) {
                NodeUpdateResult result = addNode(nodeId);
                allAffectedRanges.putAll(result.affectedRanges);
                allAffectedNodes.addAll(result.affectedNodes);
            }

            logger.info("Rebalancing complete. Affected ranges: " + allAffectedRanges.size() +
                    ", Affected nodes: " + allAffectedNodes.size());
            return new NodeUpdateResult(allAffectedRanges, allAffectedNodes);
        }

        private long hash(String input) {
            // Using FNV-1a hash for better distribution
            long hash = 2166136261L;
            for (byte b : input.getBytes()) {
                hash ^= b;
                hash *= 16777619L;
            }
            return Math.abs(hash);
        }
    }

    /**
     * Result of node update operations in the consistent hash ring.
     */
    protected static class NodeUpdateResult {
        final Map<Long, String> affectedRanges; // vnode -> new owner
        final Set<String> affectedNodes; // nodes that need data migration

        NodeUpdateResult(Map<Long, String> affectedRanges, Set<String> affectedNodes) {
            this.affectedRanges = affectedRanges;
            this.affectedNodes = affectedNodes;
        }
    }

    // ============= Memory Management =============

    protected void recordMemoryUsage(long bytes) {
        long newTotal = currentMemoryBytes.addAndGet(bytes);
        if (newTotal > maxMemoryBytes) {
            long excess = newTotal - maxMemoryBytes;
            evictToFreeMemory(excess);
        }
    }

    protected void recordMemoryRemoval(long bytes) {
        currentMemoryBytes.addAndGet(-bytes);
    }

    protected void evictToFreeMemory(long bytesToFree) {
        long freedBytes = 0;
        // Prefer evicting using cache's own eviction policy if available via entries()
        // sampling
        int sample = 0;
        for (Map.Entry<K, V> entry : localCache.entries()) {
            if (freedBytes >= bytesToFree)
                break;
            K key = entry.getKey();
            V removed = localCache.remove(key);
            if (removed != null) {
                freedBytes += estimateSize(key, removed);
            }
            if (++sample >= 1024 && freedBytes < bytesToFree) {
                // Avoid long scans; give up early and log
                break;
            }
        }
        currentMemoryBytes.addAndGet(-freedBytes);
        logger.info("Evicted " + freedBytes + " bytes to free memory (target: " + bytesToFree + ")");
    }

    protected long estimateSize(K key, V value) {
        // Simple estimation - can be improved with more sophisticated sizing
        int keySize = key != null ? key.toString().length() * 2 : 0; // UTF-16
        int valueSize = value != null ? value.toString().length() * 2 : 0;
        return keySize + valueSize + 64; // Base overhead
    }

    // ============= Common Cache Operations =============

    @Override
    public CompletableFuture<V> getAsync(K key) {
        return CompletableFuture.supplyAsync(() -> get(key), distributionExecutor);
    }

    @Override
    public CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.runAsync(() -> put(key, value), distributionExecutor);
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            String ownerNode = hashRing.getNodeForKey(key.toString());

            if (currentNodeId.equals(ownerNode)) {
                // Local cache hit
                logger.info("Cache hit for key " + key + " on local node " + currentNodeId);
                return localCache.get(key);
            } else {
                // Remote cache - need to fetch from owner node
                logger.info("Cache miss for key " + key + ". Forwarding to owner node " + ownerNode);
                return getFromRemoteNode(ownerNode, key);
            }
        } catch (Exception e) {
            logger.warning("Failed to get key " + key + ": " + e.getMessage());
            return null;
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
    public CompletableFuture<Void> clearGlobally() {
        return CompletableFuture.runAsync(() -> {
            localCache.clear();
            currentMemoryBytes.set(0);

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

    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            String ownerNode = hashRing.getNodeForKey(key.toString());

            if (currentNodeId.equals(ownerNode)) {
                // Store locally
                localCache.put(key, value);
                recordMemoryUsage(estimateSize(key, value));
                logger.info("Stored key " + key + " locally on node " + currentNodeId);
            } else {
                // Forward to owner node
                putToRemoteNode(ownerNode, key, value);
                logger.info("Forwarded key " + key + " to owner node " + ownerNode);
            }
        } catch (Exception e) {
            logger.warning("Failed to put key " + key + ": " + e.getMessage());
            throw new RuntimeException("Put operation failed", e);
        }
    }

    public V remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            String ownerNode = hashRing.getNodeForKey(key.toString());

            if (currentNodeId.equals(ownerNode)) {
                // Remove locally
                V removedValue = localCache.remove(key);
                if (removedValue != null) {
                    recordMemoryRemoval(estimateSize(key, removedValue));
                }
                return removedValue;
            } else {
                // Forward to owner node
                return removeFromRemoteNode(ownerNode, key);
            }
        } catch (Exception e) {
            logger.warning("Failed to remove key " + key + ": " + e.getMessage());
            return null;
        }
    }

    // ============= Remote Operations =============

    protected V getFromRemoteNode(String nodeAddress, K key) {
        try {
            CommunicationProtocol.CommunicationResult<V> result = communicationProtocol.sendGet(nodeAddress, key).get(
                    networkTimeout.toMillis(),
                    TimeUnit.MILLISECONDS);
            networkRequests.incrementAndGet();

            if (result.isSuccess()) {
                return result.getResult();
            } else {
                networkFailures.incrementAndGet();
            }
        } catch (Exception e) {
            networkFailures.incrementAndGet();
            logger.warning("Failed to get from remote node " + nodeAddress + ": " + e.getMessage());
        }
        return null;
    }

    protected void putToRemoteNode(String nodeAddress, K key, V value) throws Exception {
        CommunicationProtocol.CommunicationResult<Void> result = communicationProtocol.sendPut(nodeAddress, key, value)
                .get(
                        networkTimeout.toMillis(),
                        TimeUnit.MILLISECONDS);
        networkRequests.incrementAndGet();

        if (!result.isSuccess()) {
            networkFailures.incrementAndGet();
            throw new RuntimeException("Remote put failed: " + result.getError());
        }
    }

    protected V removeFromRemoteNode(String nodeAddress, K key) {
        try {
            CommunicationProtocol.CommunicationResult<V> result = communicationProtocol.sendRemove(nodeAddress, key)
                    .get(
                            networkTimeout.toMillis(),
                            TimeUnit.MILLISECONDS);
            networkRequests.incrementAndGet();

            if (result.isSuccess()) {
                return result.getResult();
            } else {
                networkFailures.incrementAndGet();
            }
        } catch (Exception e) {
            networkFailures.incrementAndGet();
            logger.warning("Failed to remove from remote node " + nodeAddress + ": " + e.getMessage());
        }
        return null;
    }

    // ============= Abstract Builder =============

    public abstract static class Builder<K, V> {
        protected String clusterName = "distributed-cache-cluster";
        protected int partitionCount = 256;
        protected int virtualNodesPerNode = 150;
        protected Duration networkTimeout = Duration.ofSeconds(5);
        protected long maxMemoryBytes = 512L * 1024 * 1024; // 512 MB default
        protected boolean readRepairEnabled = true;
        protected CacheConfig<K, V> cacheConfig;
        protected NodeDiscovery nodeDiscovery;
        protected CommunicationProtocol<K, V> communicationProtocol;

        public Builder<K, V> clusterName(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public Builder<K, V> partitionCount(int partitionCount) {
            this.partitionCount = partitionCount;
            return this;
        }

        public Builder<K, V> virtualNodesPerNode(int virtualNodesPerNode) {
            this.virtualNodesPerNode = virtualNodesPerNode;
            return this;
        }

        public Builder<K, V> networkTimeout(Duration networkTimeout) {
            this.networkTimeout = networkTimeout;
            return this;
        }

        public Builder<K, V> maxMemoryMB(long maxMemoryMB) {
            this.maxMemoryBytes = maxMemoryMB * 1024 * 1024;
            return this;
        }

        public Builder<K, V> enableReadRepair(boolean enabled) {
            this.readRepairEnabled = enabled;
            return this;
        }

        public Builder<K, V> cacheConfig(CacheConfig<K, V> cacheConfig) {
            this.cacheConfig = cacheConfig;
            return this;
        }

        public Builder<K, V> nodeDiscovery(NodeDiscovery nodeDiscovery) {
            this.nodeDiscovery = nodeDiscovery;
            return this;
        }

        public Builder<K, V> communicationProtocol(CommunicationProtocol<K, V> communicationProtocol) {
            this.communicationProtocol = communicationProtocol;
            return this;
        }

        public abstract DistributedCache<K, V> build();
    }
}
