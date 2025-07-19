package io.github.dhruv1110.jcachex.distributed.impl;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.communication.CommunicationProtocol;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;
import io.github.dhruv1110.jcachex.impl.DefaultCache;

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
    protected final ConsistencyLevel defaultConsistencyLevel;
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
        this.defaultConsistencyLevel = builder.consistencyLevel;
        this.partitionCount = builder.partitionCount;
        this.networkTimeout = builder.networkTimeout;
        this.maxMemoryBytes = builder.maxMemoryBytes;
        this.readRepairEnabled = builder.readRepairEnabled;
        this.nodeDiscovery = builder.nodeDiscovery;
        this.communicationProtocol = builder.communicationProtocol;

        // Initialize current node ID
        this.currentNodeId = generateNodeId();

        // Initialize local cache
        this.localCache = new DefaultCache<>(builder.cacheConfig);

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
            if (entry == null) {
                entry = ring.firstEntry();
            }
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

        public synchronized Set<String> getAllNodes() {
            return new HashSet<>(nodeToVNodes.keySet());
        }

        public synchronized int getVirtualNodeCount() {
            return ring.size();
        }

        public synchronized Map<String, Integer> getNodeDistribution() {
            Map<String, Integer> distribution = new HashMap<>();
            for (String nodeId : nodeToVNodes.keySet()) {
                distribution.put(nodeId, nodeToVNodes.get(nodeId).size());
            }
            return distribution;
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
        Iterator<Map.Entry<K, V>> iterator = localCache.entries().iterator();

        while (iterator.hasNext() && freedBytes < bytesToFree) {
            Map.Entry<K, V> entry = iterator.next();
            long entrySize = estimateSize(entry.getKey(), entry.getValue());
            localCache.remove(entry.getKey());
            freedBytes += entrySize;
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
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            String ownerNode = hashRing.getNodeForKey(key.toString());

            if (currentNodeId.equals(ownerNode)) {
                // Local cache hit
                return localCache.get(key);
            } else {
                // Remote cache - need to fetch from owner node
                return getFromRemoteNode(ownerNode, key);
            }
        } catch (Exception e) {
            logger.warning("Failed to get key " + key + ": " + e.getMessage());
            return null;
        }
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
            } else {
                // Forward to owner node
                putToRemoteNode(ownerNode, key, value);
            }
        } catch (Exception e) {
            logger.warning("Failed to put key " + key + ": " + e.getMessage());
            throw new RuntimeException("Put operation failed", e);
        }
    }

    @Override
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

    @SuppressWarnings("unchecked")
    protected V parseValue(String valueStr) {
        return (V) valueStr;
    }

    // ============= Abstract Builder =============

    public abstract static class Builder<K, V> {
        protected String clusterName = "distributed-cache-cluster";
        protected ConsistencyLevel consistencyLevel = ConsistencyLevel.EVENTUAL;
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

        public Builder<K, V> consistencyLevel(ConsistencyLevel consistencyLevel) {
            this.consistencyLevel = consistencyLevel;
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

    // ============= Common Interface Implementations =============

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public void clear() {
        localCache.clear();
        currentMemoryBytes.set(0);

        // Clear remote nodes if this is a global clear
        // Implementation depends on specific requirements
    }

    @Override
    public long size() {
        return localCache.size();
    }

    @Override
    public Set<K> keys() {
        return localCache.keys();
    }

    @Override
    public Collection<V> values() {
        return localCache.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return localCache.entries();
    }

    // Note: getConfig() method removed as it may not be part of the base Cache
    // interface
    // Subclasses can implement this if needed based on their specific requirements

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return defaultConsistencyLevel;
    }

    @Override
    public boolean isReadRepairEnabled() {
        return readRepairEnabled;
    }
}
