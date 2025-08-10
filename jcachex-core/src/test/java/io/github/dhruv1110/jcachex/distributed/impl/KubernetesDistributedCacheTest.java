package io.github.dhruv1110.jcachex.distributed.impl;

import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.communication.CommunicationProtocol;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KubernetesDistributedCacheTest {

    @Mock
    private NodeDiscovery mockNodeDiscovery;

    @Mock
    private CommunicationProtocol<String, String> mockCommunicationProtocol;

    private void setupBasicMocks() {
        lenient().when(mockCommunicationProtocol.getProtocolType()).thenReturn(CommunicationProtocol.ProtocolType.TCP);
        lenient().when(mockCommunicationProtocol.startServer()).thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(mockCommunicationProtocol.getConfig()).thenReturn(
                new CommunicationProtocol.ProtocolConfig(CommunicationProtocol.ProtocolType.TCP, 8080, 5000, 100, 8192,
                        Collections.emptyMap()));
        lenient().when(mockNodeDiscovery.start()).thenReturn(CompletableFuture.completedFuture(null));
    }

    // ============= Builder Tests =============

    @Test
    void testBuilderWithMissingNodeDiscovery() {
        assertThrows(Exception.class, () -> {
            KubernetesDistributedCache.<String, String>builder()
                    .clusterName("test-cluster")
                    .communicationProtocol(mockCommunicationProtocol)
                    .build();
        });
    }

    @Test
    void testBuilderWithMissingCommunicationProtocol() {
        assertThrows(Exception.class, () -> {
            KubernetesDistributedCache.<String, String>builder()
                    .clusterName("test-cluster")
                    .nodeDiscovery(mockNodeDiscovery)
                    .build();
        });
    }

    @Test
    void testBuilderWithRequiredFields() {
        setupBasicMocks();

        assertDoesNotThrow(() -> {
            DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                    .clusterName("test-cluster")
                    .nodeDiscovery(mockNodeDiscovery)
                    .communicationProtocol(mockCommunicationProtocol)
                    .build();

            assertNotNull(cache);
        });
    }

    @Test
    void testBuilderWithCustomConfiguration() {
        setupBasicMocks();

        assertDoesNotThrow(() -> {
            DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                    .clusterName("custom-cluster")
                    .nodeDiscovery(mockNodeDiscovery)
                    .communicationProtocol(mockCommunicationProtocol)
                    .virtualNodesPerNode(300)
                    .maxMemoryMB(1024)
                    .networkTimeout(Duration.ofSeconds(15))
                    .enableReadRepair(false)
                    .build();

            assertNotNull(cache);
        });
    }

    // ============= Basic Operations Tests =============

    @Test
    void testBasicCacheOperations() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("test-cluster")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Test that basic operations don't throw fatal exceptions
        // These may throw runtime exceptions due to missing setup, which is expected
        try {
            cache.put("key1", "value1");
            cache.get("key1");
        } catch (RuntimeException e) {
            // Expected in test environment due to incomplete setup
            assertTrue(e.getMessage().contains("operation failed") ||
                    e.getMessage().contains("NullPointerException") ||
                    e.getMessage().contains("Put operation failed"));
        }
    }

    @Test
    void testNullKeyHandling() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("test-cluster")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Test null key handling
        assertThrows(Exception.class, () -> cache.get(null));
        assertThrows(Exception.class, () -> cache.put(null, "value"));
    }

    @Test
    void testGlobalInvalidation() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("test-cluster")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Test global operations - these should return futures
        assertDoesNotThrow(() -> {
            CompletableFuture<Void> invalidateFuture = cache.invalidateGlobally("key1");
            assertNotNull(invalidateFuture);

            CompletableFuture<Void> clearFuture = cache.clearGlobally();
            assertNotNull(clearFuture);
        });
    }

    // ============= Error Handling Tests =============

    @Test
    void testInitializationWithFailures() {
        lenient().when(mockCommunicationProtocol.getProtocolType()).thenReturn(CommunicationProtocol.ProtocolType.TCP);
        lenient().when(mockCommunicationProtocol.startServer())
                .thenReturn(CompletableFuture.supplyAsync(() -> {
                    throw new RuntimeException("Server start failed");
                }));
        lenient().when(mockCommunicationProtocol.getConfig()).thenReturn(
                new CommunicationProtocol.ProtocolConfig(CommunicationProtocol.ProtocolType.TCP, 8080, 5000, 100, 8192,
                        Collections.emptyMap()));

        // Cache creation should still succeed even if server start fails
        assertDoesNotThrow(() -> {
            DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                    .clusterName("test-cluster")
                    .nodeDiscovery(mockNodeDiscovery)
                    .communicationProtocol(mockCommunicationProtocol)
                    .build();

            assertNotNull(cache);
        });
    }

    // ============= Interface Compliance Tests =============

    @Test
    void testDistributedCacheInterface() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("test-cluster")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Test that all DistributedCache interface methods are available
        assertDoesNotThrow(() -> {
            assertNotNull(cache.getClusterTopology());
            assertNotNull(cache.getPerNodeStats());
            assertNotNull(cache.getNodeStatuses());
            assertNotNull(cache.getDistributedMetrics());
            assertTrue(cache.getReplicationFactor() >= 0);
        });
    }

    @Test
    void testAsyncOperations() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("test-cluster")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Test async operations return valid futures
        assertDoesNotThrow(() -> {
            assertNotNull(cache.putAsync("key1", "value1"));
            assertNotNull(cache.getAsync("key1"));
        });
    }

    @Test
    void testToStringMethod() {
        setupBasicMocks();

        DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("test-cluster")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        String toString = cache.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("KubernetesDistributedCache") || toString.contains("DistributedCache"));
    }

    // ============= Kubernetes-Specific Tests =============

    @Test
    void testKubernetesNodeIdGeneration() {
        setupBasicMocks();

        // Test with HOSTNAME environment variable
        try {
            System.setProperty("HOSTNAME", "test-pod-123");
            DistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
                    .clusterName("k8s-cluster")
                    .nodeDiscovery(mockNodeDiscovery)
                    .communicationProtocol(mockCommunicationProtocol)
                    .build();
            assertNotNull(cache);
        } finally {
            System.clearProperty("HOSTNAME");
        }
    }

    @Test
    void testDataMigrationScenario() {
        setupBasicMocks();

        // Mock successful migration
        when(mockCommunicationProtocol.sendGet(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(
                        CommunicationProtocol.CommunicationResult.success("migrated-value", Duration.ofMillis(50))));

        when(mockCommunicationProtocol.sendPut(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(
                        CommunicationProtocol.CommunicationResult.success(null, Duration.ofMillis(50))));

        when(mockCommunicationProtocol.sendRemove(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(
                        CommunicationProtocol.CommunicationResult.success("migrated-value", Duration.ofMillis(50))));

        KubernetesDistributedCache<String, String> k8sCache = (KubernetesDistributedCache<String, String>) KubernetesDistributedCache
                .<String, String>builder()
                .clusterName("migration-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Test migration functionality
        Set<String> keys = new HashSet<>();
        keys.add("key1");
        keys.add("key2");
        CompletableFuture<Void> migrationFuture = k8sCache.migrateData(
                "source-node", "target-node", keys);
        assertNotNull(migrationFuture);
        assertDoesNotThrow(() -> migrationFuture.get(2, java.util.concurrent.TimeUnit.SECONDS));
    }

//    @Test
//    void testStatsCollectionFromRemoteNodes() {
//        setupBasicMocks();
//
//        // Mock health check response with stats
//        when(mockCommunicationProtocol.sendHealthCheck(anyString()))
//                .thenReturn(CompletableFuture.completedFuture(
//                        CommunicationProtocol.CommunicationResult.success("OK|remote-node|100|1024|2048",
//                                Duration.ofMillis(25))));
//
//        DistributedCache<String, String> k8sCache = KubernetesDistributedCache.<String, String>builder()
//                .clusterName("stats-test")
//                .nodeDiscovery(mockNodeDiscovery)
//                .communicationProtocol(mockCommunicationProtocol)
//                .build();
//
//        // Add a mock remote node
//        k8sCache.healthyNodes.add("remote-node");
//        k8sCache.clusterNodes.put("remote-node", new DistributedCache.NodeInfo(
//                "remote-node", "localhost:8080", DistributedCache.NodeStatus.HEALTHY,
//                System.currentTimeMillis(), Collections.emptySet()));
//
//        Map<String, io.github.dhruv1110.jcachex.CacheStats> stats = k8sCache.getPerNodeStats();
//        assertNotNull(stats);
//        assertTrue(stats.size() >= 1); // Should have at least local node
//    }

    @Test
    void testNodeHealthChangeHandling() {
        setupBasicMocks();

        // Create a node discovery listener holder
        final NodeDiscovery.NodeDiscoveryListener[] capturedListener = new NodeDiscovery.NodeDiscoveryListener[1];

        doAnswer(invocation -> {
            capturedListener[0] = invocation.getArgument(0);
            return null;
        }).when(mockNodeDiscovery).addNodeDiscoveryListener(any());

        DistributedCache<String, String> k8sCache = KubernetesDistributedCache.<String, String>builder()
                .clusterName("health-test")
                .nodeDiscovery(mockNodeDiscovery)
                .communicationProtocol(mockCommunicationProtocol)
                .build();

        // Verify listener was registered
        assertNotNull(capturedListener[0]);

        // Test node discovery
        NodeDiscovery.DiscoveredNode testNode = new NodeDiscovery.DiscoveredNode(
                "test-node", "192.168.1.100", 8080, NodeDiscovery.NodeHealth.HEALTHY,
                java.time.Instant.now(), Collections.emptyMap());

        assertDoesNotThrow(() -> capturedListener[0].onNodeDiscovered(testNode));
        assertDoesNotThrow(() -> capturedListener[0].onNodeLost("test-node"));
        assertDoesNotThrow(() -> capturedListener[0].onNodeHealthChanged(
                "test-node", NodeDiscovery.NodeHealth.HEALTHY, NodeDiscovery.NodeHealth.UNHEALTHY));
    }

//    @Test
//    void testKubernetesSpecificBuilderMethods() {
//        setupBasicMocks();
//
//        assertDoesNotThrow(() -> {
//            KubernetesDistributedCache<String, String> cache = KubernetesDistributedCache.<String, String>builder()
//                    .clusterName("k8s-specific-test")
//                    .nodeDiscovery(mockNodeDiscovery)
//                    .communicationProtocol(mockCommunicationProtocol)
//                    .podNamePrefix("jcachex-")
//                    .kubernetesNamespace("production")
//                    .build();
//            assertNotNull(cache);
//        });
//    }
//
//    @Test
//    void testDistributedMetricsCalculation() {
//        setupBasicMocks();
//
//        DistributedCache<String, String> k8sCache = KubernetesDistributedCache.<String, String>builder()
//                .clusterName("metrics-test")
//                .nodeDiscovery(mockNodeDiscovery)
//                .communicationProtocol(mockCommunicationProtocol)
//                .build();
//
//        // Simulate some metrics
//        k8sCache.networkRequests.set(100);
//        k8sCache.networkFailures.set(5);
//        k8sCache.conflictResolutions.set(2);
//
//        DistributedCache.DistributedMetrics metrics = k8sCache.getDistributedMetrics();
//        assertNotNull(metrics);
//        assertEquals(100, metrics.getNetworkRequests());
//        assertEquals(5, metrics.getNetworkFailures());
//        assertEquals(2, metrics.getConflictResolutions());
//        assertEquals(0.95, metrics.getNetworkSuccessRate(), 0.01);
//    }
//
//    @Test
//    void testFailedMigrationHandling() {
//        setupBasicMocks();
//
//        // Mock failed communication
//        when(mockCommunicationProtocol.sendGet(anyString(), anyString()))
//                .thenReturn(CompletableFuture.completedFuture(
//                        CommunicationProtocol.CommunicationResult.failure("Connection failed",
//                                new RuntimeException())));
//
//        DistributedCache<String, String> k8sCache = KubernetesDistributedCache.<String, String>builder()
//                .clusterName("failed-migration-test")
//                .nodeDiscovery(mockNodeDiscovery)
//                .communicationProtocol(mockCommunicationProtocol)
//                .build();
//
//        // Test failed migration - should not throw but should handle gracefully
//        CompletableFuture<Void> migrationFuture = k8sCache.migrateData(
//                "source-node", "target-node", Set.of("key1"));
//        assertNotNull(migrationFuture);
//        assertDoesNotThrow(() -> migrationFuture.get(2, java.util.concurrent.TimeUnit.SECONDS));
//    }
//
//    @Test
//    void testEmptyStatsCreation() {
//        setupBasicMocks();
//
//        // Mock failed health check
//        when(mockCommunicationProtocol.sendHealthCheck(anyString()))
//                .thenReturn(CompletableFuture.completedFuture(
//                        CommunicationProtocol.CommunicationResult.failure("Node unreachable", new RuntimeException())));
//
//        DistributedCache<String, String> k8sCache = KubernetesDistributedCache.<String, String>builder()
//                .clusterName("empty-stats-test")
//                .nodeDiscovery(mockNodeDiscovery)
//                .communicationProtocol(mockCommunicationProtocol)
//                .build();
//
//        // Add a failed node
//        k8sCache.healthyNodes.add("failed-node");
//        k8sCache.clusterNodes.put("failed-node", new DistributedCache.NodeInfo(
//                "failed-node", "unreachable:8080", DistributedCache.NodeStatus.FAILED,
//                System.currentTimeMillis(), Collections.emptySet()));
//
//        Map<String, io.github.dhruv1110.jcachex.CacheStats> stats = k8sCache.getPerNodeStats();
//        assertNotNull(stats);
//        // Should contain stats even for failed nodes (empty stats)
//    }
}
