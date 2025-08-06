package io.github.dhruv1110.jcachex.distributed;

import io.github.dhruv1110.jcachex.distributed.communication.CommunicationProtocol;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;
import io.github.dhruv1110.jcachex.distributed.impl.KubernetesDistributedCache;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for distributed cache functionality.
 * Tests end-to-end scenarios with multiple components working together.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DistributedCacheIntegrationTest {

    @Mock
    private NodeDiscovery mockNodeDiscovery;

    @Mock
    private CommunicationProtocol<String, String> mockCommunicationProtocol;

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
        lenient().when(mockNodeDiscovery.stop())
                .thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(mockNodeDiscovery.getDiscoveryType())
                .thenReturn(NodeDiscovery.DiscoveryType.KUBERNETES);
    }

    // ============= Basic Integration Tests =============

    @Test
    @Order(1)
    void testCacheInitializationAndShutdown() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("integration-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .partitionCount(8)
                .virtualNodesPerNode(50)
                .build();

        assertNotNull(cache);
        assertNotNull(cache.getClusterTopology());
        assertEquals("integration-test", cache.getClusterTopology().getClusterName());

        // Verify initialization calls
        verify(mockNodeDiscovery, times(1)).start();
        verify(mockCommunicationProtocol, times(1)).startServer();
    }

    // ============= Node Discovery Integration Tests =============

    @Test
    @Order(2)
    void testNodeDiscoveryLifecycle() {
        setupBasicMocks();

        final AtomicInteger discoveredCount = new AtomicInteger(0);
        final AtomicInteger lostCount = new AtomicInteger(0);
        final CountDownLatch listenerRegistered = new CountDownLatch(1);

        // Capture the listener registration
        doAnswer(invocation -> {
            NodeDiscovery.NodeDiscoveryListener listener = invocation.getArgument(0);
            assertNotNull(listener);
            listenerRegistered.countDown();
            return null;
        }).when(mockNodeDiscovery).addNodeDiscoveryListener(any());

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("discovery-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        try {
            assertTrue(listenerRegistered.await(2, TimeUnit.SECONDS));
            verify(mockNodeDiscovery, times(1)).addNodeDiscoveryListener(any());
        } catch (InterruptedException e) {
            fail("Listener registration timed out");
        }
    }

    // ============= Communication Protocol Integration Tests =============

    @Test
    @Order(3)
    void testCommunicationProtocolIntegration() {
        setupBasicMocks();

        // Mock successful communication
        when(mockCommunicationProtocol.sendGet(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(
                        CommunicationProtocol.CommunicationResult.success("remote-value", Duration.ofMillis(50))));

        when(mockCommunicationProtocol.sendPut(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(
                        CommunicationProtocol.CommunicationResult.success(null, Duration.ofMillis(75))));

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("communication-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Test that communication is set up
        assertNotNull(cache);
        verify(mockCommunicationProtocol, times(1)).startServer();
    }

    // ============= Error Handling Integration Tests =============

    @Test
    @Order(4)
    void testGracefulFailureHandling() {
        setupBasicMocks();

        // Mock communication failures
        when(mockCommunicationProtocol.sendGet(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(
                        CommunicationProtocol.CommunicationResult.failure("Network timeout",
                                new RuntimeException("Connection failed"))));

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("failure-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .networkTimeout(Duration.ofSeconds(1))
                .build();

        // Test that cache handles failures gracefully
        assertDoesNotThrow(() -> {
            CompletableFuture<String> future = cache.getAsync("failing-key");
            assertNotNull(future);
        });
    }

    @Test
    @Order(5)
    void testSlowNodeDiscoveryHandling() {
        setupBasicMocks();

        // Mock slow node discovery
        when(mockNodeDiscovery.start()).thenReturn(
                CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(500); // Simulate slow startup
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }));

        // Cache should still initialize despite slow discovery
        assertDoesNotThrow(() -> {
            DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                    .clusterName("slow-discovery-test")
                    .nodeDiscovery(mockNodeDiscovery)
                    .communicationProtocol(mockCommunicationProtocol)
                    .build();
            assertNotNull(cache);
        });
    }

    // ============= Cluster Topology Integration Tests =============

    @Test
    @Order(6)
    void testClusterTopologyUpdates() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("topology-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .partitionCount(16)
                .build();

        // Verify initial topology
        DistributedCache.ClusterTopology initialTopology = cache.getClusterTopology();
        assertNotNull(initialTopology);
        assertEquals("topology-test", initialTopology.getClusterName());
        assertEquals(16, initialTopology.getPartitionCount());
        assertTrue(initialTopology.getVersion() >= 0);
    }

    // ============= Metrics Integration Tests =============

    @Test
    @Order(7)
    void testMetricsCollection() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("metrics-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Test metrics are available
        DistributedCache.DistributedMetrics metrics = cache.getDistributedMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getNetworkRequests() >= 0);
        assertTrue(metrics.getNetworkFailures() >= 0);
        assertTrue(metrics.getNetworkSuccessRate() >= 0.0 && metrics.getNetworkSuccessRate() <= 1.0);
        assertNotNull(metrics.getPerNodeLatencies());
    }

    @Test
    @Order(8)
    void testNodeStatsCollection() {
        setupBasicMocks();

        // Mock health check for stats collection
        when(mockCommunicationProtocol.sendHealthCheck(anyString()))
                .thenReturn(CompletableFuture.completedFuture(
                        CommunicationProtocol.CommunicationResult.success("OK|node|50|1024|2048",
                                Duration.ofMillis(25))));

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("stats-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        Map<String, io.github.dhruv1110.jcachex.CacheStats> nodeStats = cache.getPerNodeStats();
        assertNotNull(nodeStats);
        assertFalse(nodeStats.isEmpty()); // Should at least have local node stats
    }

    // ============= Configuration Integration Tests =============

    @Test
    @Order(9)
    void testDifferentConfigurations() {
        setupBasicMocks();

        // Test minimal configuration
        DistributedCache<String, String> minimalCache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("minimal")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();
        assertNotNull(minimalCache);

        // Test full configuration
        DistributedCache<String, String> fullCache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("full-config")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .partitionCount(32)
                .virtualNodesPerNode(200)
                .maxMemoryMB(256)
                .networkTimeout(Duration.ofSeconds(10))
                .enableReadRepair(true)
//                .maxReconnectAttempts(5)
                .build();
        assertNotNull(fullCache);

        // Both should have different configurations
        assertNotEquals(minimalCache.getClusterTopology().getPartitionCount(),
                fullCache.getClusterTopology().getPartitionCount());
    }

    // ============= Async Operations Integration Tests =============

    @Test
    @Order(10)
    void testAsyncOperationsIntegration() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("async-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Test async operations return valid futures
        CompletableFuture<Void> putFuture = cache.putAsync("async-key", "async-value");
        assertNotNull(putFuture);

        CompletableFuture<String> getFuture = cache.getAsync("async-key");
        assertNotNull(getFuture);

        CompletableFuture<Void> invalidateFuture = cache.invalidateGlobally("async-key");
        assertNotNull(invalidateFuture);

        CompletableFuture<Void> clearFuture = cache.clearGlobally();
        assertNotNull(clearFuture);

        // Test that operations can be chained
        assertDoesNotThrow(() -> {
            CompletableFuture<Void> chainedFuture = putFuture
                    .thenCompose(v -> getFuture)
                    .thenCompose(v -> invalidateFuture);
            assertNotNull(chainedFuture);
        });
    }

    // ============= Multi-Node Simulation Tests =============

    @Test
    @Order(11)
    void testMultiNodeSimulation() {
        setupBasicMocks();

        // Create multiple cache instances to simulate different nodes
        DistributedCache<String, String> node1 = KubernetesDistributedCache.<String, String>builder()
                .clusterName("multi-node-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        DistributedCache<String, String> node2 = KubernetesDistributedCache.<String, String>builder()
                .clusterName("multi-node-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Both nodes should be in the same cluster
        assertEquals(node1.getClusterTopology().getClusterName(),
                node2.getClusterTopology().getClusterName());

        // Both should have valid metrics
        assertNotNull(node1.getDistributedMetrics());
        assertNotNull(node2.getDistributedMetrics());
    }

    // ============= Resource Management Tests =============

    @Test
    @Order(12)
    void testResourceManagement() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("resource-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .maxMemoryMB(32) // Small memory limit for testing
                .build();

        assertNotNull(cache);

        // Test that cache has proper resource limits
        DistributedCache.ClusterTopology topology = cache.getClusterTopology();
        assertNotNull(topology);

        // Test that multiple operations don't exhaust resources
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                cache.putAsync("key-" + i, "value-" + i);
                cache.getAsync("key-" + i);
            }
        });
    }

    @Test
    @Order(13)
    void testConcurrentOperations() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("concurrent-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Test concurrent async operations
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final int index = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                cache.putAsync("concurrent-key-" + index, "concurrent-value-" + index);
                cache.getAsync("concurrent-key-" + index);
            });
            futures.add(future);
        }

        // All operations should complete without throwing
        assertDoesNotThrow(() -> {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(10, TimeUnit.SECONDS);
        });
    }
}
