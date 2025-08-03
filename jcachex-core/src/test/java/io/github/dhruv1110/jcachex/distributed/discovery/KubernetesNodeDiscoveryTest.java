package io.github.dhruv1110.jcachex.distributed.discovery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KubernetesNodeDiscoveryTest {

    @Test
    void testGetDiscoveryType() {
        NodeDiscovery discovery = NodeDiscovery.kubernetes()
                .namespace("default")
                .build();
        assertEquals(NodeDiscovery.DiscoveryType.KUBERNETES, discovery.getDiscoveryType());
    }

    @Test
    void testBuilderWithValidConfiguration() {
        assertDoesNotThrow(() -> {
            NodeDiscovery discovery = NodeDiscovery.kubernetes()
                    .namespace("test-namespace")
                    .labelSelector("app=test")
                    .discoveryInterval(Duration.ofSeconds(10))
                    .build();

            assertNotNull(discovery);
            assertEquals(NodeDiscovery.DiscoveryType.KUBERNETES, discovery.getDiscoveryType());
        });
    }

    @Test
    void testBuilderWithMinimalConfiguration() {
        assertDoesNotThrow(() -> {
            NodeDiscovery discovery = NodeDiscovery.kubernetes()
                    .namespace("default")
                    .build();

            assertNotNull(discovery);
            assertEquals(NodeDiscovery.DiscoveryType.KUBERNETES, discovery.getDiscoveryType());
        });
    }

    @Test
    void testBuilderWithAllOptions() {
        assertDoesNotThrow(() -> {
            NodeDiscovery discovery = NodeDiscovery.kubernetes()
                    .namespace("custom-namespace")
                    .labelSelector("environment=production")
                    .discoveryInterval(Duration.ofSeconds(15))
                    .healthCheckInterval(Duration.ofSeconds(5))
                    .maxRetries(5)
                    .connectionTimeout(Duration.ofSeconds(10))
                    .useServiceAccount(true)
                    .build();

            assertNotNull(discovery);
            assertEquals(NodeDiscovery.DiscoveryType.KUBERNETES, discovery.getDiscoveryType());
        });
    }

    @Test
    void testBuilderWithKubeConfig() {
        assertDoesNotThrow(() -> {
            NodeDiscovery discovery = NodeDiscovery.kubernetes()
                    .namespace("test")
                    .kubeConfigPath("/tmp/kubeconfig")
                    .useServiceAccount(false)
                    .build();

            assertNotNull(discovery);
            assertEquals(NodeDiscovery.DiscoveryType.KUBERNETES, discovery.getDiscoveryType());
        });
    }

    @Test
    void testStartAndStopOperations() {
        NodeDiscovery discovery = NodeDiscovery.kubernetes()
                .namespace("test")
                .build();

        // These should not throw exceptions even if Kubernetes API is not available
        assertDoesNotThrow(() -> {
            discovery.start();
            discovery.stop();
        });
    }

    @Test
    void testListenerManagement() {
        NodeDiscovery discovery = NodeDiscovery.kubernetes()
                .namespace("test")
                .build();

        NodeDiscovery.NodeDiscoveryListener listener = new NodeDiscovery.NodeDiscoveryListener() {
            @Override
            public void onNodeDiscovered(NodeDiscovery.DiscoveredNode node) {
            }

            @Override
            public void onNodeLost(String nodeId) {
            }

            @Override
            public void onNodeHealthChanged(String nodeId, NodeDiscovery.NodeHealth oldHealth,
                    NodeDiscovery.NodeHealth newHealth) {
            }
        };

        // These should not throw exceptions
        assertDoesNotThrow(() -> {
            discovery.addNodeDiscoveryListener(listener);
            discovery.removeNodeDiscoveryListener(listener);
        });
    }

    @Test
    void testDiscoveryStatsBasic() {
        NodeDiscovery discovery = NodeDiscovery.kubernetes()
                .namespace("test")
                .build();

        assertDoesNotThrow(() -> {
            NodeDiscovery.DiscoveryStats stats = discovery.getDiscoveryStats();
            assertNotNull(stats);
            assertTrue(stats.getAverageDiscoveryTime() >= 0);
        });
    }

    @Test
    void testConfigurationProperties() {
        NodeDiscovery discovery = NodeDiscovery.kubernetes()
                .namespace("custom-namespace")
                .labelSelector("environment=production")
                .discoveryInterval(Duration.ofSeconds(15))
                .build();

        // Basic validation that object was created with configuration
        assertNotNull(discovery);
        assertEquals(NodeDiscovery.DiscoveryType.KUBERNETES, discovery.getDiscoveryType());
    }

    @Test
    void testErrorHandling() {
        // Test with various configuration values - should not throw during construction
        assertDoesNotThrow(() -> {
            NodeDiscovery discovery = NodeDiscovery.kubernetes()
                    .namespace("test")
                    .discoveryInterval(Duration.ofMillis(100)) // Very short interval
                    .healthCheckInterval(Duration.ofMillis(50))
                    .maxRetries(0) // No retries
                    .connectionTimeout(Duration.ofMillis(1)) // Very short timeout
                    .build();

            // Should handle config gracefully
            assertNotNull(discovery);
        });
    }

    @Test
    void testToStringMethod() {
        NodeDiscovery discovery = NodeDiscovery.kubernetes()
                .namespace("test")
                .build();

        String toString = discovery.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("KubernetesNodeDiscovery") || toString.contains("NodeDiscovery"));
    }

    @Test
    void testDiscoveredNodeClass() {
        // Test the DiscoveredNode class functionality
        NodeDiscovery.DiscoveredNode node = new NodeDiscovery.DiscoveredNode(
                "test-node",
                "192.168.1.1",
                8080,
                NodeDiscovery.NodeHealth.HEALTHY,
                java.time.Instant.now(),
                java.util.Collections.emptyMap());

        assertEquals("test-node", node.getNodeId());
        assertEquals("192.168.1.1", node.getAddress());
        assertEquals(8080, node.getPort());
        assertEquals("192.168.1.1:8080", node.getFullAddress());
        assertEquals(NodeDiscovery.NodeHealth.HEALTHY, node.getHealth());
        assertNotNull(node.getLastSeen());
        assertNotNull(node.getMetadata());
        assertNotNull(node.toString());
    }

    @Test
    void testAsyncOperations() {
        NodeDiscovery discovery = NodeDiscovery.kubernetes()
                .namespace("test")
                .build();

        // Test that async methods return non-null futures (even if they may fail later)
        assertDoesNotThrow(() -> {
            assertNotNull(discovery.start());
            assertNotNull(discovery.discoverNodes());
            assertNotNull(discovery.checkNodeHealth("unknown-node"));
            assertNotNull(discovery.stop());
        });
    }
}
