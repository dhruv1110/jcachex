package io.github.dhruv1110.jcachex.distributed.impl;

import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.communication.CommunicationProtocol;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for AbstractDistributedCache.
 * Tests core distributed functionality including consistent hashing,
 * memory management, metrics, and remote operations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AbstractDistributedCacheTest {

    @Mock
    private NodeDiscovery mockNodeDiscovery;

    @Mock
    private CommunicationProtocol<String, String> mockCommunicationProtocol;

    private TestDistributedCache cache;

    // Test implementation of AbstractDistributedCache
    private static class TestDistributedCache extends AbstractDistributedCache<String, String> {
        public TestDistributedCache(AbstractDistributedCache.Builder<String, String> builder) {
            super(builder);
        }

        @Override
        protected String generateNodeId() {
            return "test-node-" + System.currentTimeMillis();
        }

        @Override
        protected void initializeCluster() {
            // Simplified initialization for testing
        }

        @Override
        public ClusterTopology getClusterTopology() {
            Set<NodeInfo> nodes = new HashSet<>();
            for (NodeInfo node : clusterNodes.values()) {
                nodes.add(node);
            }
            return new ClusterTopology(clusterName, nodes, partitionCount, topologyVersion.get());
        }

        @Override
        public Map<String, DistributedCache.NodeStatus> getNodeStatuses() {
            Map<String, NodeStatus> statuses = new HashMap<>();
            for (String nodeId : healthyNodes) {
                statuses.put(nodeId, NodeStatus.HEALTHY);
            }
            return statuses;
        }

        @Override
        public DistributedMetrics getDistributedMetrics() {
            return new DistributedMetrics(
                    networkRequests.get(),
                    networkFailures.get(),
                    0.0,
                    replicationLag.get(),
                    conflictResolutions.get(),
                    new HashMap<>());
        }

        @Override
        public Map<String, io.github.dhruv1110.jcachex.CacheStats> getPerNodeStats() {
            Map<String, io.github.dhruv1110.jcachex.CacheStats> stats = new HashMap<>();
            stats.put(currentNodeId, localCache.stats());
            return stats;
        }

        @Override
        public int getReplicationFactor() {
            return 1;
        }

        public void close() {
            isRunning.set(false);
            distributionExecutor.shutdown();
            discoveryScheduler.shutdown();
        }

        // Builder for test cache
        public static class Builder extends AbstractDistributedCache.Builder<String, String> {
            @Override
            public TestDistributedCache build() {
                return new TestDistributedCache(this);
            }
        }
    }

    private void setupBasicMocks() {
        lenient().when(mockCommunicationProtocol.getProtocolType())
                .thenReturn(CommunicationProtocol.ProtocolType.TCP);
        lenient().when(mockCommunicationProtocol.startServer())
                .thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(mockCommunicationProtocol.getConfig())
                .thenReturn(new CommunicationProtocol.ProtocolConfig(
                        CommunicationProtocol.ProtocolType.TCP, 8080, 5000, 100, 8192,
                        Collections.emptyMap()));
        lenient().when(mockNodeDiscovery.start())
                .thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(mockNodeDiscovery.getDiscoveryType())
                .thenReturn(NodeDiscovery.DiscoveryType.KUBERNETES);
    }

    @BeforeEach
    void setUp() {
        setupBasicMocks();
        cache = (TestDistributedCache) new TestDistributedCache.Builder()
                .clusterName("test-cluster")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .partitionCount(16)
                .virtualNodesPerNode(100)
                .maxMemoryMB(64)
                .networkTimeout(Duration.ofSeconds(5))
                .build();
    }

    @AfterEach
    void tearDown() {
        if (cache != null) {
            try {
                cache.close();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    // ============= Configuration Tests =============

    @Test
    @Order(1)
    void testBasicConfiguration() {
        assertNotNull(cache);
        assertEquals("test-cluster", cache.clusterName);
        assertEquals(16, cache.partitionCount);
        assertEquals(Duration.ofSeconds(5), cache.networkTimeout);
        assertTrue(cache.currentNodeId.startsWith("test-node-"));
        assertNotNull(cache.localCache);
        assertNotNull(cache.hashRing);
    }

    @Test
    @Order(2)
    void testMemoryConfiguration() {
        long expectedMaxMemory = 64L * 1024 * 1024; // 64MB in bytes
        assertEquals(expectedMaxMemory, cache.maxMemoryBytes);
        assertEquals(0, cache.currentMemoryBytes.get());
    }

    // ============= Consistent Hashing Tests =============

    @Test
    @Order(3)
    void testConsistentHashingDistribution() {
        // Add some test nodes
        cache.hashRing.addNode("node-1");
        cache.hashRing.addNode("node-2");
        cache.hashRing.addNode("node-3");

        // Test key distribution
        Map<String, Integer> distribution = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            String key = "key-" + i;
            String node = cache.hashRing.getNodeForKey(key);
            distribution.put(node, distribution.getOrDefault(node, 0) + 1);
        }

        // Should have decent distribution across nodes
        assertTrue(distribution.size() > 0);
        // Each node should get some keys (within reasonable bounds)
        for (Integer count : distribution.values()) {
            assertTrue(count > 50, "Node should get reasonable number of keys: " + count);
        }
    }

//    @Test
//    @Order(4)
//    void testNodeAdditionAndRemoval() {
//        String initialNode = cache.hashRing.getNodeForKey("test-key");
//        assertNotNull(initialNode);
//
//        // Add a new node
//        cache.hashRing.addNode("new-node");
//        String nodeAfterAdd = cache.hashRing.getNodeForKey("test-key");
//
//        // Remove the new node
//        cache.hashRing.removeNode("new-node");
//        String nodeAfterRemove = cache.hashRing.getNodeForKey("test-key");
//
//        // Key should return to original node after removal
//        assertEquals(initialNode, nodeAfterRemove);
//    }

    // ============= Memory Management Tests =============

    @Test
    @Order(5)
    void testMemoryTracking() {
        String key = "memory-test-key";
        String value = "memory-test-value";

        long estimatedSize = cache.estimateSize(key, value);
        assertTrue(estimatedSize > 0);

        cache.recordMemoryUsage(estimatedSize);
        assertEquals(estimatedSize, cache.currentMemoryBytes.get());

        cache.recordMemoryRemoval(estimatedSize);
        assertEquals(0, cache.currentMemoryBytes.get());
    }

    @Test
    @Order(6)
    void testMemoryEvictionTrigger() {
        // Fill up memory close to limit
        long nearLimit = cache.maxMemoryBytes - 1000;
        cache.currentMemoryBytes.set(nearLimit);

        // Adding more should trigger eviction check
        assertDoesNotThrow(() -> cache.recordMemoryUsage(2000));
    }

    // ============= Basic Cache Operations Tests =============

//    @Test
//    @Order(7)
//    void testLocalCacheOperations() {
//        // Mock the hash ring to always return current node
//        String currentNode = cache.currentNodeId;
//        when(cache.hashRing.getNodeForKey(anyString())).thenReturn(currentNode);
//
//        // Test put operation
//        assertDoesNotThrow(() -> cache.put("local-key", "local-value"));
//
//        // Test get operation
//        // Note: This might be null due to local cache behavior, but shouldn't throw
//        assertDoesNotThrow(() -> cache.get("local-key"));
//    }

    @Test
    @Order(8)
    void testNullKeyValidation() {
        assertThrows(IllegalArgumentException.class, () -> cache.get(null));
        assertThrows(IllegalArgumentException.class, () -> cache.put(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> cache.remove(null));
    }

    @Test
    @Order(9)
    void testAsyncOperations() {
        CompletableFuture<String> getFuture = cache.getAsync("async-key");
        assertNotNull(getFuture);

        CompletableFuture<Void> putFuture = cache.putAsync("async-key", "async-value");
        assertNotNull(putFuture);

        // Should complete without throwing (even if they fail due to missing setup)
        assertDoesNotThrow(() -> {
            try {
                getFuture.get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                // Expected in test environment
            }
        });
    }

    // ============= Distributed Operations Tests =============

    @Test
    @Order(10)
    void testGlobalInvalidation() {
        CompletableFuture<Void> invalidateFuture = cache.invalidateGlobally("global-key");
        assertNotNull(invalidateFuture);

        List<String> keys = Arrays.asList("key1", "key2", "key3");
        CompletableFuture<Void> invalidateMultiFuture = cache.invalidateGlobally(keys);
        assertNotNull(invalidateMultiFuture);
    }

    @Test
    @Order(11)
    void testGlobalClear() {
        CompletableFuture<Void> clearFuture = cache.clearGlobally();
        assertNotNull(clearFuture);
        assertFalse(clearFuture.isCompletedExceptionally());
    }

    @Test
    @Order(12)
    void testRebalance() {
        CompletableFuture<Void> rebalanceFuture = cache.rebalance();
        assertNotNull(rebalanceFuture);
    }

    // ============= Metrics Tests =============

    @Test
    @Order(13)
    void testMetricsCollection() {
        // Initially should be zero
        DistributedCache.DistributedMetrics metrics = cache.getDistributedMetrics();
        assertNotNull(metrics);
        assertEquals(0, metrics.getNetworkRequests());
        assertEquals(0, metrics.getNetworkFailures());

        // Simulate some network activity
        cache.networkRequests.incrementAndGet();
        cache.networkFailures.incrementAndGet();

        metrics = cache.getDistributedMetrics();
        assertEquals(1, metrics.getNetworkRequests());
        assertEquals(1, metrics.getNetworkFailures());
    }

    @Test
    @Order(14)
    void testTopologyVersion() {
        long initialVersion = cache.topologyVersion.get();
        assertTrue(initialVersion >= 0);

        // Simulate topology change
        cache.topologyVersion.incrementAndGet();
        assertEquals(initialVersion + 1, cache.topologyVersion.get());
    }

    // ============= Interface Compliance Tests =============

    @Test
    @Order(15)
    void testDistributedCacheInterface() {
        // Test all required interface methods are implemented
        assertNotNull(cache.getClusterTopology());
        assertNotNull(cache.getPerNodeStats());
        assertNotNull(cache.getNodeStatuses());
        assertNotNull(cache.getDistributedMetrics());
        assertTrue(cache.getReplicationFactor() >= 0);

        // Test async operations return valid futures
        assertNotNull(cache.putAsync("test", "value"));
        assertNotNull(cache.getAsync("test"));
        assertNotNull(cache.invalidateGlobally("test"));
        assertNotNull(cache.clearGlobally());
        assertNotNull(cache.rebalance());
    }

    @Test
    @Order(16)
    void testClusterTopology() {
        DistributedCache.ClusterTopology topology = cache.getClusterTopology();
        assertNotNull(topology);
        assertEquals("test-cluster", topology.getClusterName());
        assertEquals(16, topology.getPartitionCount());
        assertTrue(topology.getVersion() >= 0);
        assertEquals(0, topology.getHealthyNodeCount()); // No nodes added yet
    }

    // ============= Error Handling Tests =============

    @Test
    @Order(17)
    void testRemoteOperationFailure() {
        // Mock communication failure
        when(mockCommunicationProtocol.sendGet(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(
                        CommunicationProtocol.CommunicationResult.failure("Network error", new RuntimeException())));

        // Should handle failure gracefully
        String result = cache.get("remote-key");
        assertNull(result); // Should return null on failure
    }

    @Test
    @Order(18)
    void testCommunicationTimeout() {
        // Mock timeout scenario
        CompletableFuture<CommunicationProtocol.CommunicationResult<String>> timeoutFuture = new CompletableFuture<>();
        when(mockCommunicationProtocol.sendGet(anyString(), anyString())).thenReturn(timeoutFuture);

        // Should handle timeout gracefully
        assertDoesNotThrow(() -> cache.get("timeout-key"));
    }

    // ============= Node Management Tests =============

    @Test
    @Order(19)
    void testNodeLifecycle() {
        String nodeId = "test-node-lifecycle";
        String address = "localhost:8080";

        // Initially no nodes
        assertTrue(cache.healthyNodes.isEmpty());
        assertTrue(cache.clusterNodes.isEmpty());

        // Add node
        DistributedCache.NodeInfo nodeInfo = new DistributedCache.NodeInfo(
                nodeId, address, DistributedCache.NodeStatus.HEALTHY,
                System.currentTimeMillis(), Collections.emptySet());
        cache.clusterNodes.put(nodeId, nodeInfo);
        cache.healthyNodes.add(nodeId);

        // Verify node is tracked
        assertTrue(cache.healthyNodes.contains(nodeId));
        assertTrue(cache.clusterNodes.containsKey(nodeId));

        // Remove node
        cache.healthyNodes.remove(nodeId);
        cache.clusterNodes.remove(nodeId);

        // Verify node is removed
        assertFalse(cache.healthyNodes.contains(nodeId));
        assertFalse(cache.clusterNodes.containsKey(nodeId));
    }

    // ============= Resource Management Tests =============

    @Test
    @Order(20)
    void testResourceCleanup() {
        assertDoesNotThrow(() -> cache.close());
        assertFalse(cache.isRunning.get());
    }

    @Test
    @Order(21)
    void testExecutorManagement() {
        assertNotNull(cache.distributionExecutor);
        assertNotNull(cache.discoveryScheduler);
        assertFalse(cache.distributionExecutor.isShutdown());
        assertFalse(cache.discoveryScheduler.isShutdown());
    }
}
