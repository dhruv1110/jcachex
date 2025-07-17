package io.github.dhruv1110.jcachex.example.distributed;

import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;
import io.github.dhruv1110.jcachex.spring.distributed.JCacheXDistributedCacheFactory;
import io.github.dhruv1110.jcachex.spring.distributed.NodeDiscoveryFactory;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Gossip protocol-based distributed cache discovery example.
 *
 * This example demonstrates how to use JCacheX with gossip protocol for
 * peer-to-peer node discovery without requiring a central service registry.
 *
 * <h3>Prerequisites:</h3>
 * <ul>
 * <li>Multiple JCacheX instances running</li>
 * <li>Network connectivity between nodes</li>
 * <li>Seed nodes for initial cluster formation</li>
 * </ul>
 *
 * <h3>Configuration:</h3>
 *
 * <pre>
 * jcachex:
 *   distributed:
 *     nodeDiscovery:
 *       type: GOSSIP
 *       discoveryIntervalSeconds: 30
 *       healthCheckIntervalSeconds: 10
 *       gossip:
 *         seedNodes:
 *           - node1:8080
 *           - node2:8080
 *           - node3:8080
 *         gossipIntervalSeconds: 5
 *         gossipFanout: 3
 *         nodeTimeoutSeconds: 60
 * </pre>
 *
 * <h3>Starting Multiple Nodes:</h3>
 *
 * <pre>
 * # Node 1 (seed node)
 * java -jar jcachex-app.jar --server.port=8080 --jcachex.node.id=node1
 *
 * # Node 2
 * java -jar jcachex-app.jar --server.port=8081 --jcachex.node.id=node2
 *
 * # Node 3
 * java -jar jcachex-app.jar --server.port=8082 --jcachex.node.id=node3
 * </pre>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Decentralized:</strong> No single point of failure</li>
 * <li><strong>Self-Healing:</strong> Automatically handles node failures</li>
 * <li><strong>Scalable:</strong> Gossip overhead grows logarithmically</li>
 * <li><strong>Eventual Consistency:</strong> All nodes eventually learn about
 * all others</li>
 * </ul>
 */
public class GossipDiscoveryExample {

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 JCacheX Gossip Discovery Example");
        System.out.println("====================================");

        // Set up Gossip discovery configuration
        System.setProperty("jcachex.distributed.nodeDiscovery.type", "GOSSIP");
        System.setProperty("jcachex.distributed.nodeDiscovery.discoveryIntervalSeconds", "30");
        System.setProperty("jcachex.distributed.nodeDiscovery.healthCheckIntervalSeconds", "10");
        System.setProperty("jcachex.distributed.nodeDiscovery.gossip.gossipIntervalSeconds", "5");
        System.setProperty("jcachex.distributed.nodeDiscovery.gossip.gossipFanout", "3");
        System.setProperty("jcachex.distributed.nodeDiscovery.gossip.nodeTimeoutSeconds", "60");

        ConfigurableApplicationContext context = SpringApplication.run(GossipDiscoveryExample.class, args);

        try {
            // Get the distributed cache factory
            JCacheXDistributedCacheFactory cacheFactory = context.getBean(JCacheXDistributedCacheFactory.class);

            System.out.println("\n1. 🔍 Creating Gossip-discovered cache");

            // Create a distributed cache that will use Gossip discovery
            DistributedCache<String, SessionData> sessionCache = cacheFactory.createDistributedCache(
                    "gossip-sessions",
                    config -> {
                        config.replicationFactor(2);
                        config.consistencyLevel(DistributedCache.ConsistencyLevel.EVENTUAL);
                        config.maximumSize(10000L);
                        config.expireAfterWrite(Duration.ofMinutes(30));
                    });

            // Demonstrate basic operations
            System.out.println("\n2. 🏗️ Session management operations");
            SessionData session1 = new SessionData("sess1", "user123", "192.168.1.100");
            SessionData session2 = new SessionData("sess2", "user456", "192.168.1.101");
            SessionData session3 = new SessionData("sess3", "user789", "192.168.1.102");

            sessionCache.put("session1", session1);
            sessionCache.put("session2", session2);
            sessionCache.put("session3", session3);

            SessionData retrievedSession = sessionCache.get("session1");
            System.out.println("   👤 Retrieved session: " + retrievedSession.sessionId +
                    " for user " + retrievedSession.userId);

            // Show cluster information
            System.out.println("\n3. 🏢 Gossip cluster information");
            DistributedCache.ClusterTopology topology = sessionCache.getClusterTopology();
            System.out.println("   🏢 Cluster: " + topology.getClusterName());
            System.out.println("   💚 Healthy nodes: " + topology.getHealthyNodeCount());
            System.out.println("   📊 Total nodes: " + topology.getNodes().size());

            // Show node statuses from Gossip
            System.out.println("\n4. 🔍 Gossip protocol status");
            sessionCache.getNodeStatuses().forEach((nodeId, status) -> {
                System.out.println("   📍 Node: " + nodeId + " → " + status);
            });

            // Demonstrate distributed operations with Gossip
            System.out.println("\n5. 🌐 Distributed operations");

            // Bulk session operations
            System.out.println("   📦 Bulk session loading...");
            for (int i = 1; i <= 50; i++) {
                SessionData bulkSession = new SessionData("bulk-" + i, "user" + i, "192.168.1." + (100 + i));
                sessionCache.put("bulk-session-" + i, bulkSession);
            }

            // Use session consistency for user operations
            SessionData criticalSession = new SessionData("admin-session", "admin", "192.168.1.1");
            sessionCache.putWithConsistency("admin-session", criticalSession, DistributedCache.ConsistencyLevel.SESSION)
                    .thenRun(() -> System.out.println("   ✅ Admin session stored with session consistency"));

            // Global session invalidation
            sessionCache.invalidateGlobally("expired-sessions")
                    .thenRun(() -> System.out.println("   🗑️ Global session cleanup completed"));

            // Show cache statistics
            System.out.println("\n6. 📊 Cache statistics");
            System.out.println("   📦 Cache size: " + sessionCache.size());
            System.out.println("   📈 Hit rate: " + String.format("%.2f%%", sessionCache.stats().hitRate() * 100));

            // Demonstrate gossip protocol behavior
            System.out.println("\n7. 🔄 Gossip protocol simulation");
            simulateGossipProtocol(sessionCache);

            // Demonstrate network partition tolerance
            System.out.println("\n8. 🌐 Network partition simulation");
            simulateNetworkPartition(sessionCache);

            // Demonstrate node joining and leaving
            System.out.println("\n9. 🔄 Dynamic node management");
            simulateNodeJoinLeave(sessionCache);

            // Keep the application running to show ongoing gossip
            System.out.println("\n10. 🔄 Continuous gossip (press Ctrl+C to stop)");
            Thread.sleep(5000);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
        }

        System.out.println("\n✅ Gossip discovery example completed!");
    }

    private static void simulateGossipProtocol(DistributedCache<String, SessionData> cache) {
        System.out.println("   🔄 Simulating gossip protocol behavior...");

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("   📢 Gossip round 1: Node information exchange");

                Thread.sleep(1000);
                System.out.println("   📢 Gossip round 2: Health status propagation");

                Thread.sleep(1000);
                System.out.println("   📢 Gossip round 3: Cluster membership updates");

                Thread.sleep(1000);
                System.out.println("   ✅ Gossip convergence achieved");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private static void simulateNetworkPartition(DistributedCache<String, SessionData> cache) {
        System.out.println("   🌐 Simulating network partition...");

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("   ⚠️ Network partition detected: Cluster split");

                Thread.sleep(2000);
                System.out.println("   🔄 Partition healing: Nodes reconnecting");

                Thread.sleep(1000);
                System.out.println("   📢 Gossip resync: Merging cluster state");

                Thread.sleep(1000);
                System.out.println("   ✅ Partition healed: Cluster reunified");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private static void simulateNodeJoinLeave(DistributedCache<String, SessionData> cache) {
        System.out.println("   🔄 Simulating dynamic node management...");

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("   ➕ New node joining cluster via gossip");

                Thread.sleep(1000);
                System.out.println("   📢 Node introduction: Spreading join message");

                Thread.sleep(1000);
                System.out.println("   ⚖️ Load rebalancing: Redistributing data");

                Thread.sleep(2000);
                System.out.println("   ➖ Node graceful leave: Cleanup initiated");

                Thread.sleep(1000);
                System.out.println("   🔄 Data migration: Moving replicas");

                Thread.sleep(1000);
                System.out.println("   ✅ Node management completed");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Bean
    public JCacheXProperties jcacheXProperties() {
        JCacheXProperties properties = new JCacheXProperties();

        // Set up distributed configuration
        JCacheXProperties.DistributedConfig distributedConfig = new JCacheXProperties.DistributedConfig();
        distributedConfig.setClusterName("gossip-cluster");
        distributedConfig.setReplicationFactor(2);
        distributedConfig.setConsistencyLevel("EVENTUAL");

        // Set up Gossip discovery configuration
        JCacheXProperties.NodeDiscoveryConfig discoveryConfig = new JCacheXProperties.NodeDiscoveryConfig();
        discoveryConfig.setType("GOSSIP");
        discoveryConfig.setDiscoveryIntervalSeconds(30);
        discoveryConfig.setHealthCheckIntervalSeconds(10);

        JCacheXProperties.GossipDiscoveryConfig gossipConfig = new JCacheXProperties.GossipDiscoveryConfig();
        gossipConfig.setSeedNodes(Arrays.asList("localhost:8080", "localhost:8081", "localhost:8082"));
        gossipConfig.setGossipIntervalSeconds(5);
        gossipConfig.setGossipFanout(3);
        gossipConfig.setNodeTimeoutSeconds(60);

        discoveryConfig.setGossip(gossipConfig);
        distributedConfig.setNodeDiscovery(discoveryConfig);

        properties.getDefaultConfig().setDistributed(distributedConfig);

        return properties;
    }

    static class SessionData {
        final String sessionId;
        final String userId;
        final String ipAddress;
        final long timestamp;

        SessionData(String sessionId, String userId, String ipAddress) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.ipAddress = ipAddress;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
