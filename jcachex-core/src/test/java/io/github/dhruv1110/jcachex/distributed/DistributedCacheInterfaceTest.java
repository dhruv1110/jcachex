package io.github.dhruv1110.jcachex.distributed;

import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for DistributedCache interface and its inner classes.
 * Tests the data structures and utility classes used by distributed caches.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DistributedCacheInterfaceTest {

    // ============= NodeInfo Tests =============

    @Test
    @Order(1)
    void testNodeInfoCreation() {
        String nodeId = "test-node-1";
        String address = "192.168.1.100:8080";
        DistributedCache.NodeStatus status = DistributedCache.NodeStatus.HEALTHY;
        long lastSeen = System.currentTimeMillis();
        Set<Integer> partitions = new HashSet<>();
        partitions.add(1);
        partitions.add(2);
        partitions.add(3);

        DistributedCache.NodeInfo nodeInfo = new DistributedCache.NodeInfo(
                nodeId, address, status, lastSeen, partitions);

        assertEquals(nodeId, nodeInfo.getNodeId());
        assertEquals(address, nodeInfo.getAddress());
        assertEquals(status, nodeInfo.getStatus());
        assertEquals(lastSeen, nodeInfo.getLastSeen());
        assertEquals(partitions, nodeInfo.getPartitions());
    }

    @Test
    @Order(2)
    void testNodeStatusEnum() {
        // Test all node status values are available
        assertNotNull(DistributedCache.NodeStatus.HEALTHY);
        assertNotNull(DistributedCache.NodeStatus.DEGRADED);
        assertNotNull(DistributedCache.NodeStatus.UNREACHABLE);
        assertNotNull(DistributedCache.NodeStatus.FAILED);

        // Test enum methods
        DistributedCache.NodeStatus[] statuses = DistributedCache.NodeStatus.values();
        assertEquals(4, statuses.length);

        assertEquals(DistributedCache.NodeStatus.HEALTHY,
                DistributedCache.NodeStatus.valueOf("HEALTHY"));
    }

    // ============= ClusterTopology Tests =============

    @Test
    @Order(3)
    void testClusterTopologyCreation() {
        String clusterName = "test-cluster";
        Set<DistributedCache.NodeInfo> nodes = new HashSet<>();

        // Add some test nodes
        nodes.add(new DistributedCache.NodeInfo("node-1", "host1:8080",
                DistributedCache.NodeStatus.HEALTHY, System.currentTimeMillis(),
                Collections.singleton(1)));
        nodes.add(new DistributedCache.NodeInfo("node-2", "host2:8080",
                DistributedCache.NodeStatus.HEALTHY, System.currentTimeMillis(),
                Collections.singleton(2)));

        int partitionCount = 16;
        long version = 12345L;

        DistributedCache.ClusterTopology topology = new DistributedCache.ClusterTopology(
                clusterName, nodes, partitionCount, version);

        assertEquals(clusterName, topology.getClusterName());
        assertEquals(nodes, topology.getNodes());
        assertEquals(partitionCount, topology.getPartitionCount());
        assertEquals(version, topology.getVersion());
    }

    @Test
    @Order(4)
    void testClusterTopologyHealthyNodeCount() {
        Set<DistributedCache.NodeInfo> nodes = new HashSet<>();

        // Add healthy nodes
        nodes.add(new DistributedCache.NodeInfo("healthy-1", "host1:8080",
                DistributedCache.NodeStatus.HEALTHY, System.currentTimeMillis(),
                Collections.emptySet()));
        nodes.add(new DistributedCache.NodeInfo("healthy-2", "host2:8080",
                DistributedCache.NodeStatus.HEALTHY, System.currentTimeMillis(),
                Collections.emptySet()));

        // Add unhealthy nodes
        nodes.add(new DistributedCache.NodeInfo("degraded-1", "host3:8080",
                DistributedCache.NodeStatus.DEGRADED, System.currentTimeMillis(),
                Collections.emptySet()));

        DistributedCache.ClusterTopology topology = new DistributedCache.ClusterTopology(
                "health-test", nodes, 16, 1);

        assertEquals(2, topology.getHealthyNodeCount());
        assertEquals(3, topology.getNodes().size());
    }

    // ============= DistributedMetrics Tests =============

    @Test
    @Order(5)
    void testDistributedMetricsCreation() {
        long networkRequests = 1000L;
        long networkFailures = 50L;
        double averageNetworkLatency = 25.5;
        long replicationLag = 100L;
        long conflictResolutions = 5L;
        Map<String, Long> perNodeLatencies = new HashMap<>();
        perNodeLatencies.put("node-1", 20L);
        perNodeLatencies.put("node-2", 30L);

        DistributedCache.DistributedMetrics metrics = new DistributedCache.DistributedMetrics(
                networkRequests, networkFailures, averageNetworkLatency,
                replicationLag, conflictResolutions, perNodeLatencies);

        assertEquals(networkRequests, metrics.getNetworkRequests());
        assertEquals(networkFailures, metrics.getNetworkFailures());
        assertEquals(averageNetworkLatency, metrics.getAverageNetworkLatency(), 0.01);
        assertEquals(replicationLag, metrics.getReplicationLag());
        assertEquals(conflictResolutions, metrics.getConflictResolutions());
        assertEquals(perNodeLatencies, metrics.getPerNodeLatencies());
    }

    @Test
    @Order(6)
    void testNetworkSuccessRateCalculation() {
        // Test perfect success rate
        DistributedCache.DistributedMetrics perfectMetrics = new DistributedCache.DistributedMetrics(
                100L, 0L, 10.0, 0L, 0L, Collections.emptyMap());
        assertEquals(1.0, perfectMetrics.getNetworkSuccessRate(), 0.01);

        // Test 95% success rate
        DistributedCache.DistributedMetrics goodMetrics = new DistributedCache.DistributedMetrics(
                100L, 5L, 15.0, 0L, 0L, Collections.emptyMap());
        assertEquals(0.95, goodMetrics.getNetworkSuccessRate(), 0.01);

        // Test no requests (should return 1.0)
        DistributedCache.DistributedMetrics noRequestsMetrics = new DistributedCache.DistributedMetrics(
                0L, 0L, 0.0, 0L, 0L, Collections.emptyMap());
        assertEquals(1.0, noRequestsMetrics.getNetworkSuccessRate(), 0.01);
    }
}
