package io.github.dhruv1110.jcachex.distributed;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheStats;
import io.github.dhruv1110.jcachex.distributed.DistributedCache.ConsistencyLevel;
import io.github.dhruv1110.jcachex.distributed.DistributedCache.NodeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for KubernetesDistributedCache implementation.
 */
class KubernetesDistributedCacheTest {

    private DistributedCache<String, String> distributedCache;
    private static final String TEST_CLUSTER = "test-cluster";
    private static final Collection<String> TEST_NODES = Arrays.asList("node-1:8080", "node-2:8080", "node-3:8080");

    @BeforeEach
    void setUp() {
        // Use a random port for tests to avoid conflicts
        int testPort = 8080 + (int) (Math.random() * 1000);
        distributedCache = KubernetesDistributedCache.<String, String>builder()
                .clusterName(TEST_CLUSTER)
                .maxMemoryMB(128) // 128 MB for tests
                .tcpPort(testPort) // Random port for tests
                .virtualNodesPerNode(50) // Fewer vnodes for tests
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .nodeDiscovery(null) // No node discovery for tests
                .build();
    }

    @Nested
    @DisplayName("Basic Cache Operations")
    class BasicOperations {

        @Test
        @DisplayName("Put and get operations work correctly")
        void testPutAndGet() {
            distributedCache.put("key1", "value1");
            String value = distributedCache.get("key1");
            assertEquals("value1", value);
        }

        @Test
        @DisplayName("Remove operation works correctly")
        void testRemove() {
            distributedCache.put("key1", "value1");
            String removed = distributedCache.remove("key1");
            assertEquals("value1", removed);
            assertNull(distributedCache.get("key1"));
        }

        @Test
        @DisplayName("Clear operation works correctly")
        void testClear() {
            distributedCache.put("key1", "value1");
            distributedCache.put("key2", "value2");
            distributedCache.clear();
            assertEquals(0, distributedCache.size());
        }

        @Test
        @DisplayName("Contains key operation works correctly")
        void testContainsKey() {
            distributedCache.put("key1", "value1");
            assertTrue(distributedCache.containsKey("key1"));
            assertFalse(distributedCache.containsKey("key2"));
        }

        @Test
        @DisplayName("Keys operation returns correct keys")
        void testKeys() {
            distributedCache.put("key1", "value1");
            distributedCache.put("key2", "value2");
            Set<String> keys = distributedCache.keys();
            assertTrue(keys.contains("key1"));
            assertTrue(keys.contains("key2"));
            assertEquals(2, keys.size());
        }

        @Test
        @DisplayName("Values operation returns correct values")
        void testValues() {
            distributedCache.put("key1", "value1");
            distributedCache.put("key2", "value2");
            Collection<String> values = distributedCache.values();
            assertTrue(values.contains("value1"));
            assertTrue(values.contains("value2"));
            assertEquals(2, values.size());
        }

        @Test
        @DisplayName("Entries operation returns correct entries")
        void testEntries() {
            distributedCache.put("key1", "value1");
            distributedCache.put("key2", "value2");
            Set<Map.Entry<String, String>> entries = distributedCache.entries();
            assertEquals(2, entries.size());
        }
    }

    @Nested
    @DisplayName("Async Operations")
    class AsyncOperations {

        @Test
        @DisplayName("Async put operation completes successfully")
        void testPutAsync() throws ExecutionException, InterruptedException, TimeoutException {
            CompletableFuture<Void> future = distributedCache.putAsync("key1", "value1");
            future.get(5, TimeUnit.SECONDS);
            assertEquals("value1", distributedCache.get("key1"));
        }

        @Test
        @DisplayName("Async get operation completes successfully")
        void testGetAsync() throws ExecutionException, InterruptedException, TimeoutException {
            distributedCache.put("key1", "value1");
            CompletableFuture<String> future = distributedCache.getAsync("key1");
            String value = future.get(5, TimeUnit.SECONDS);
            assertEquals("value1", value);
        }

        @Test
        @DisplayName("Async remove operation completes successfully")
        void testRemoveAsync() throws ExecutionException, InterruptedException, TimeoutException {
            distributedCache.put("key1", "value1");
            CompletableFuture<String> future = distributedCache.removeAsync("key1");
            String removed = future.get(5, TimeUnit.SECONDS);
            assertEquals("value1", removed);
            assertNull(distributedCache.get("key1"));
        }

        @Test
        @DisplayName("Async clear operation completes successfully")
        void testClearAsync() throws ExecutionException, InterruptedException, TimeoutException {
            distributedCache.put("key1", "value1");
            distributedCache.put("key2", "value2");
            CompletableFuture<Void> future = distributedCache.clearAsync();
            future.get(5, TimeUnit.SECONDS);
            assertEquals(0, distributedCache.size());
        }
    }

    @Nested
    @DisplayName("Consistency Models")
    class ConsistencyModels {

        @Test
        @DisplayName("Strong consistency put operation")
        void testStrongConsistencyPut() throws ExecutionException, InterruptedException, TimeoutException {
            CompletableFuture<Void> future = distributedCache.putWithConsistency("key1", "value1",
                    ConsistencyLevel.STRONG);
            future.get(5, TimeUnit.SECONDS);
            assertEquals("value1", distributedCache.get("key1"));
        }

        @Test
        @DisplayName("Eventual consistency put operation")
        void testEventualConsistencyPut() throws ExecutionException, InterruptedException, TimeoutException {
            CompletableFuture<Void> future = distributedCache.putWithConsistency("key1", "value1",
                    ConsistencyLevel.EVENTUAL);
            future.get(5, TimeUnit.SECONDS);
            assertEquals("value1", distributedCache.get("key1"));
        }

        @Test
        @DisplayName("Session consistency put operation")
        void testSessionConsistencyPut() throws ExecutionException, InterruptedException, TimeoutException {
            CompletableFuture<Void> future = distributedCache.putWithConsistency("key1", "value1",
                    ConsistencyLevel.SESSION);
            future.get(5, TimeUnit.SECONDS);
            assertEquals("value1", distributedCache.get("key1"));
        }

        @Test
        @DisplayName("Monotonic read consistency put operation")
        void testMonotonicReadConsistencyPut() throws ExecutionException, InterruptedException, TimeoutException {
            CompletableFuture<Void> future = distributedCache.putWithConsistency("key1", "value1",
                    ConsistencyLevel.MONOTONIC_READ);
            future.get(5, TimeUnit.SECONDS);
            assertEquals("value1", distributedCache.get("key1"));
        }

        @Test
        @DisplayName("Strong consistency get operation")
        void testStrongConsistencyGet() throws ExecutionException, InterruptedException, TimeoutException {
            distributedCache.put("key1", "value1");
            CompletableFuture<String> future = distributedCache.getWithConsistency("key1", ConsistencyLevel.STRONG);
            String value = future.get(5, TimeUnit.SECONDS);
            assertEquals("value1", value);
        }

        @Test
        @DisplayName("Eventual consistency get operation")
        void testEventualConsistencyGet() throws ExecutionException, InterruptedException, TimeoutException {
            distributedCache.put("key1", "value1");
            CompletableFuture<String> future = distributedCache.getWithConsistency("key1", ConsistencyLevel.EVENTUAL);
            String value = future.get(5, TimeUnit.SECONDS);
            assertEquals("value1", value);
        }
    }

    @Nested
    @DisplayName("Distributed Operations")
    class DistributedOperations {

        @Test
        @DisplayName("Global invalidation of single key")
        void testInvalidateGloballySingleKey() throws ExecutionException, InterruptedException, TimeoutException {
            distributedCache.put("key1", "value1");
            CompletableFuture<Void> future = distributedCache.invalidateGlobally("key1");
            future.get(5, TimeUnit.SECONDS);
            assertNull(distributedCache.get("key1"));
        }

        @Test
        @DisplayName("Global invalidation of multiple keys")
        void testInvalidateGloballyMultipleKeys() throws ExecutionException, InterruptedException, TimeoutException {
            distributedCache.put("key1", "value1");
            distributedCache.put("key2", "value2");
            distributedCache.put("key3", "value3");

            Collection<String> keysToInvalidate = Arrays.asList("key1", "key2");
            CompletableFuture<Void> future = distributedCache.invalidateGlobally(keysToInvalidate);
            future.get(5, TimeUnit.SECONDS);

            assertNull(distributedCache.get("key1"));
            assertNull(distributedCache.get("key2"));
            assertEquals("value3", distributedCache.get("key3"));
        }

        @Test
        @DisplayName("Global clear operation")
        void testClearGlobally() throws ExecutionException, InterruptedException, TimeoutException {
            distributedCache.put("key1", "value1");
            distributedCache.put("key2", "value2");

            CompletableFuture<Void> future = distributedCache.clearGlobally();
            future.get(5, TimeUnit.SECONDS);

            assertEquals(0, distributedCache.size());
        }
    }

    @Nested
    @DisplayName("Cluster Management")
    class ClusterManagement {

        @Test
        @DisplayName("Get cluster topology")
        void testGetClusterTopology() {
            DistributedCache.ClusterTopology topology = distributedCache.getClusterTopology();
            assertNotNull(topology);
            assertEquals(TEST_CLUSTER, topology.getClusterName());
            assertTrue(topology.getHealthyNodeCount() > 0);
        }

        @Test
        @DisplayName("Get per-node statistics")
        void testGetPerNodeStats() {
            Map<String, CacheStats> nodeStats = distributedCache.getPerNodeStats();
            assertNotNull(nodeStats);
            assertFalse(nodeStats.isEmpty());
        }

        @Test
        @DisplayName("Get node statuses")
        void testGetNodeStatuses() {
            Map<String, NodeStatus> nodeStatuses = distributedCache.getNodeStatuses();
            assertNotNull(nodeStatuses);
            assertFalse(nodeStatuses.isEmpty());
        }

        @Test
        @DisplayName("Add node to cluster")
        void testAddNode() throws ExecutionException, InterruptedException, TimeoutException {
            String newNode = "new-node:8080";
            CompletableFuture<Void> future = distributedCache.addNode(newNode);
            future.get(5, TimeUnit.SECONDS);

            DistributedCache.ClusterTopology topology = distributedCache.getClusterTopology();
            assertTrue(topology.getHealthyNodeCount() > 3);
        }

        @Test
        @DisplayName("Remove node from cluster")
        void testRemoveNode() throws ExecutionException, InterruptedException, TimeoutException {
            // First add a node
            String nodeToRemove = "temp-node:8080";
            distributedCache.addNode(nodeToRemove).get(5, TimeUnit.SECONDS);

            // Then remove it
            String nodeId = "node-" + nodeToRemove.hashCode();
            CompletableFuture<Void> future = distributedCache.removeNode(nodeId);
            future.get(5, TimeUnit.SECONDS);

            DistributedCache.ClusterTopology topology = distributedCache.getClusterTopology();
            assertTrue(topology.getHealthyNodeCount() >= 3);
        }

        @Test
        @DisplayName("Rebalance cluster")
        void testRebalance() throws ExecutionException, InterruptedException, TimeoutException {
            CompletableFuture<Void> future = distributedCache.rebalance();
            future.get(5, TimeUnit.SECONDS);
            // Rebalance should complete without error
            assertTrue(future.isDone());
            assertFalse(future.isCancelled());
        }
    }

    @Nested
    @DisplayName("Configuration and Metrics")
    class ConfigurationAndMetrics {

        @Test
        @DisplayName("Get consistency level")
        void testGetConsistencyLevel() {
            assertEquals(ConsistencyLevel.EVENTUAL, distributedCache.getConsistencyLevel());
        }

        @Test
        @DisplayName("Get replication factor")
        void testGetReplicationFactor() {
            assertEquals(2, distributedCache.getReplicationFactor());
        }

        @Test
        @DisplayName("Get distributed metrics")
        void testGetDistributedMetrics() {
            DistributedCache.DistributedMetrics metrics = distributedCache.getDistributedMetrics();
            assertNotNull(metrics);
            assertTrue(metrics.getNetworkSuccessRate() >= 0.0);
            assertTrue(metrics.getNetworkSuccessRate() <= 1.0);
        }

        @Test
        @DisplayName("Read repair configuration")
        void testReadRepairConfiguration() {
            distributedCache.setReadRepairEnabled(true);
            assertTrue(distributedCache.isReadRepairEnabled());

            distributedCache.setReadRepairEnabled(false);
            assertFalse(distributedCache.isReadRepairEnabled());
        }

        @Test
        @DisplayName("Cache configuration")
        void testCacheConfiguration() {
            CacheConfig<String, String> config = distributedCache.config();
            assertNotNull(config);
        }

        @Test
        @DisplayName("Cache statistics")
        void testCacheStatistics() {
            CacheStats stats = distributedCache.stats();
            assertNotNull(stats);
            assertTrue(stats.hitCount() >= 0);
            assertTrue(stats.missCount() >= 0);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Builder with all parameters")
        void testBuilderWithAllParameters() {
            DistributedCache<String, String> cache = DistributedCache.<String, String>builder()
                    .clusterName("test-cluster")
                    .nodes("node1:8080", "node2:8080")
                    .replicationFactor(3)
                    .consistencyLevel(ConsistencyLevel.STRONG)
                    .partitionCount(128)
                    .networkTimeout(Duration.ofSeconds(10))
                    .enableReadRepair(true)
                    .enableAutoDiscovery(true)
                    .gossipInterval(Duration.ofSeconds(5))
                    .maxReconnectAttempts(5)
                    .compressionEnabled(true)
                    .encryptionEnabled(true)
                    .build();

            assertNotNull(cache);
            assertEquals("test-cluster", cache.getClusterTopology().getClusterName());
            assertEquals(3, cache.getReplicationFactor());
            assertEquals(ConsistencyLevel.STRONG, cache.getConsistencyLevel());
            assertTrue(cache.isReadRepairEnabled());
        }

        @Test
        @DisplayName("Builder with minimal parameters")
        void testBuilderWithMinimalParameters() {
            DistributedCache<String, String> cache = DistributedCache.<String, String>builder()
                    .clusterName("minimal-cluster")
                    .nodes("node1:8080")
                    .build();

            assertNotNull(cache);
            assertEquals("minimal-cluster", cache.getClusterTopology().getClusterName());
            assertEquals(2, cache.getReplicationFactor()); // Default
            assertEquals(ConsistencyLevel.EVENTUAL, cache.getConsistencyLevel()); // Default
        }

        @Test
        @DisplayName("Builder with collection of nodes")
        void testBuilderWithNodeCollection() {
            Collection<String> nodes = Arrays.asList("node1:8080", "node2:8080", "node3:8080");
            DistributedCache<String, String> cache = DistributedCache.<String, String>builder()
                    .clusterName("collection-cluster")
                    .nodes(nodes)
                    .build();

            assertNotNull(cache);
            assertEquals("collection-cluster", cache.getClusterTopology().getClusterName());
            assertTrue(cache.getClusterTopology().getHealthyNodeCount() >= 3);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Operations with null keys are handled gracefully")
        void testNullKeyHandling() {
            assertDoesNotThrow(() -> distributedCache.put(null, "value"));
            assertDoesNotThrow(() -> distributedCache.get(null));
            assertDoesNotThrow(() -> distributedCache.remove(null));
        }

        @Test
        @DisplayName("Operations with null values are handled gracefully")
        void testNullValueHandling() {
            assertDoesNotThrow(() -> distributedCache.put("key", null));
            distributedCache.put("key", "value");
            assertDoesNotThrow(() -> distributedCache.put("key", null));
        }

        @Test
        @DisplayName("Consistency operations with null keys are handled gracefully")
        void testConsistencyOperationsWithNullKeys() {
            assertDoesNotThrow(() -> {
                try {
                    distributedCache.putWithConsistency(null, "value", ConsistencyLevel.STRONG).get(5,
                            TimeUnit.SECONDS);
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    // Expected for null keys
                }
            });
        }
    }

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrentOperations {

        @Test
        @DisplayName("Concurrent put operations")
        void testConcurrentPuts() throws InterruptedException {
            int numThreads = 10;
            int numOperations = 100;
            Thread[] threads = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < numOperations; j++) {
                        String key = "key-" + threadId + "-" + j;
                        String value = "value-" + threadId + "-" + j;
                        distributedCache.put(key, value);
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertTrue(distributedCache.size() > 0);
        }

        @Test
        @DisplayName("Concurrent get operations")
        void testConcurrentGets() throws InterruptedException {
            // First populate the cache
            for (int i = 0; i < 100; i++) {
                distributedCache.put("key-" + i, "value-" + i);
            }

            int numThreads = 10;
            Thread[] threads = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        String key = "key-" + j;
                        String value = distributedCache.get(key);
                        if (value != null) {
                            assertEquals("value-" + j, value);
                        }
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
        }
    }
}
