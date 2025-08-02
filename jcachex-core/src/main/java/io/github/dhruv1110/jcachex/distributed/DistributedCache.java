package io.github.dhruv1110.jcachex.distributed;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheStats;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Distributed cache interface that extends the base Cache interface with
 * multi-node capabilities.
 * <p>
 * This interface provides seamless scaling from local to distributed caching
 * while maintaining
 * the familiar Cache API. It supports various consistency models, replication
 * strategies, and
 * automatic failover mechanisms.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Seamless Scaling:</strong> Start local, scale to distributed
 * without API changes</li>
 * <li><strong>Multiple Consistency Models:</strong> Strong, eventual, and
 * session consistency</li>
 * <li><strong>Automatic Replication:</strong> Data replicated across nodes for
 * high availability</li>
 * <li><strong>Partition Tolerance:</strong> Continues operating during network
 * partitions</li>
 * <li><strong>Self-Healing:</strong> Automatic node discovery and failure
 * recovery</li>
 * <li><strong>Hybrid Performance:</strong> Local performance with distributed
 * availability</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Basic distributed cache
 * DistributedCache<String, User> cache = DistributedCache.<String, User>builder()
 *         .clusterName("user-service")
 *         .nodes("cache-node-1:8080", "cache-node-2:8080", "cache-node-3:8080")
 *         .replicationFactor(2)
 *         .consistencyLevel(ConsistencyLevel.EVENTUAL)
 *         .build();
 *
 * // Use exactly like a local cache
 * cache.put("user123", user);
 * User retrievedUser = cache.get("user123"); // May come from any node
 *
 * // Distributed-specific operations
 * cache.invalidateGlobally("user123"); // Invalidate across all nodes
 * ClusterTopology topology = cache.getClusterTopology();
 * Map<String, CacheStats> nodeStats = cache.getPerNodeStats();
 * }</pre>
 *
 * <h3>Consistency Models:</h3>
 * <ul>
 * <li><strong>STRONG:</strong> All nodes see the same data immediately (CP
 * model)</li>
 * <li><strong>EVENTUAL:</strong> Nodes eventually converge to the same state
 * (AP model)</li>
 * <li><strong>SESSION:</strong> Guarantees consistency within a
 * session/thread</li>
 * <li><strong>MONOTONIC_READ:</strong> Once a value is read, subsequent reads
 * return same or newer</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @since 1.0.0
 */
public interface DistributedCache<K, V> {

    /**
     * Node health status.
     */
    enum NodeStatus {
        HEALTHY,
        DEGRADED,
        UNREACHABLE,
        FAILED
    }

    /**
     * Puts a value in the cache with the specified consistency level.
     *
     * @param key              the key
     * @param value            the value
     * @return CompletableFuture that completes when the operation is done
     */
    CompletableFuture<Void> putAsync(K key, V value);

    /**
     * Gets a value from the cache with the specified consistency level.
     *
     * @param key              the key
     * @return CompletableFuture containing the value or null if not found
     */
    CompletableFuture<V> getAsync(K key);

    void put(K key, V value);

    V get(K key);

    /**
     * Invalidates a key across all nodes in the cluster.
     *
     * @param key the key to invalidate
     * @return CompletableFuture that completes when invalidation is propagated
     */
    CompletableFuture<Void> invalidateGlobally(K key);

    /**
     * Invalidates multiple keys across all nodes in the cluster.
     *
     * @param keys the keys to invalidate
     * @return CompletableFuture that completes when invalidation is propagated
     */
    CompletableFuture<Void> invalidateGlobally(Collection<K> keys);

    /**
     * Clears all entries across all nodes in the cluster.
     *
     * @return CompletableFuture that completes when clear is propagated
     */
    CompletableFuture<Void> clearGlobally();

    /**
     * Returns the current cluster topology.
     *
     * @return cluster topology information
     */
    ClusterTopology getClusterTopology();

    /**
     * Returns cache statistics for each node in the cluster.
     *
     * @return map of node ID to cache statistics
     */
    Map<String, CacheStats> getPerNodeStats();

    /**
     * Returns the status of each node in the cluster.
     *
     * @return map of node ID to node status
     */
    Map<String, NodeStatus> getNodeStatuses();

    /**
     * Returns the replication factor for this cache.
     *
     * @return number of replicas maintained for each entry
     */
    int getReplicationFactor();

    /**
     * Forces a manual rebalancing of the cache across nodes.
     * <p>
     * This is typically done automatically, but can be triggered manually
     * after adding or removing nodes from the cluster.
     * </p>
     *
     * @return CompletableFuture that completes when rebalancing is done
     */
    CompletableFuture<Void> rebalance();

    /**
     * Returns detailed metrics about distributed operations.
     *
     * @return distributed cache metrics
     */
    DistributedMetrics getDistributedMetrics();

    /**
     * Builder interface for creating distributed caches.
     */
    interface Builder<K, V> {
        Builder<K, V> clusterName(String clusterName);

        Builder<K, V> replicationFactor(int replicationFactor);

        Builder<K, V> partitionCount(int partitionCount);

        Builder<K, V> networkTimeout(Duration networkTimeout);

        Builder<K, V> maxReconnectAttempts(int maxAttempts);

        DistributedCache<K, V> build();
    }

    /**
     * Cluster topology information.
     */
    class ClusterTopology {
        private final String clusterName;
        private final Set<NodeInfo> nodes;
        private final int partitionCount;
        private final long version;

        public ClusterTopology(String clusterName, Set<NodeInfo> nodes, int partitionCount, long version) {
            this.clusterName = clusterName;
            this.nodes = nodes;
            this.partitionCount = partitionCount;
            this.version = version;
        }

        public String getClusterName() {
            return clusterName;
        }

        public Set<NodeInfo> getNodes() {
            return nodes;
        }

        public int getPartitionCount() {
            return partitionCount;
        }

        public long getVersion() {
            return version;
        }

        public int getHealthyNodeCount() {
            return (int) nodes.stream().filter(n -> n.getStatus() == NodeStatus.HEALTHY).count();
        }
    }

    /**
     * Information about a cluster node.
     */
    class NodeInfo {
        private final String nodeId;
        private final String address;
        private final NodeStatus status;
        private final long lastSeen;
        private final Set<Integer> partitions;

        public NodeInfo(String nodeId, String address, NodeStatus status, long lastSeen, Set<Integer> partitions) {
            this.nodeId = nodeId;
            this.address = address;
            this.status = status;
            this.lastSeen = lastSeen;
            this.partitions = partitions;
        }

        public String getNodeId() {
            return nodeId;
        }

        public String getAddress() {
            return address;
        }

        public NodeStatus getStatus() {
            return status;
        }

        public long getLastSeen() {
            return lastSeen;
        }

        public Set<Integer> getPartitions() {
            return partitions;
        }
    }

    /**
     * Metrics specific to distributed cache operations.
     */
    class DistributedMetrics {
        private final long networkRequests;
        private final long networkFailures;
        private final double averageNetworkLatency;
        private final long replicationLag;
        private final long conflictResolutions;
        private final Map<String, Long> perNodeLatencies;

        public DistributedMetrics(long networkRequests, long networkFailures, double averageNetworkLatency,
                long replicationLag, long conflictResolutions, Map<String, Long> perNodeLatencies) {
            this.networkRequests = networkRequests;
            this.networkFailures = networkFailures;
            this.averageNetworkLatency = averageNetworkLatency;
            this.replicationLag = replicationLag;
            this.conflictResolutions = conflictResolutions;
            this.perNodeLatencies = perNodeLatencies;
        }

        public long getNetworkRequests() {
            return networkRequests;
        }

        public long getNetworkFailures() {
            return networkFailures;
        }

        public double getAverageNetworkLatency() {
            return averageNetworkLatency;
        }

        public long getReplicationLag() {
            return replicationLag;
        }

        public long getConflictResolutions() {
            return conflictResolutions;
        }

        public Map<String, Long> getPerNodeLatencies() {
            return perNodeLatencies;
        }

        public double getNetworkSuccessRate() {
            return networkRequests == 0 ? 1.0 : 1.0 - ((double) networkFailures / networkRequests);
        }
    }
}
