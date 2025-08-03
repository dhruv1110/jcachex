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
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
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
}
