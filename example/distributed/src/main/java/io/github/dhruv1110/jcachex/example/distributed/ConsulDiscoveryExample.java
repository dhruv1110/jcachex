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
import java.util.concurrent.CompletableFuture;

/**
 * Consul-based distributed cache discovery example.
 *
 * This example demonstrates how to use JCacheX with HashiCorp Consul for
 * service discovery in microservices environments.
 *
 * <h3>Prerequisites:</h3>
 * <ul>
 * <li>Running Consul cluster</li>
 * <li>JCacheX services registered with Consul</li>
 * <li>Health check endpoints configured</li>
 * </ul>
 *
 * <h3>Configuration:</h3>
 *
 * <pre>
 * jcachex:
 *   distributed:
 *     nodeDiscovery:
 *       type: CONSUL
 *       discoveryIntervalSeconds: 30
 *       healthCheckIntervalSeconds: 10
 *       consul:
 *         consulHost: localhost:8500
 *         serviceName: jcachex-cluster
 *         datacenter: dc1
 *         enableAcl: false
 *         token: your-consul-token
 * </pre>
 *
 * <h3>Consul Service Registration:</h3>
 *
 * <pre>
 * # Register JCacheX service
 * curl -X PUT http://localhost:8500/v1/agent/service/register \
 *   -d '{
 *     "ID": "jcachex-1",
 *     "Name": "jcachex-cluster",
 *     "Address": "192.168.1.10",
 *     "Port": 8080,
 *     "Tags": ["cache", "distributed"],
 *     "Check": {
 *       "HTTP": "http://192.168.1.10:8080/health",
 *       "Interval": "10s"
 *     }
 *   }'
 * </pre>
 *
 * <h3>Docker Compose Setup:</h3>
 *
 * <pre>
 * version: '3.8'
 * services:
 *   consul:
 *     image: consul:latest
 *     ports:
 *       - "8500:8500"
 *     command: agent -dev -client=0.0.0.0
 *
 *   jcachex-node1:
 *     image: jcachex:latest
 *     depends_on:
 *       - consul
 *     environment:
 *       - CONSUL_HOST=consul:8500
 *       - NODE_ID=node1
 * </pre>
 */
public class ConsulDiscoveryExample {

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 JCacheX Consul Discovery Example");
        System.out.println("====================================");

        // Set up Consul discovery configuration
        System.setProperty("jcachex.distributed.nodeDiscovery.type", "CONSUL");
        System.setProperty("jcachex.distributed.nodeDiscovery.discoveryIntervalSeconds", "30");
        System.setProperty("jcachex.distributed.nodeDiscovery.healthCheckIntervalSeconds", "10");
        System.setProperty("jcachex.distributed.nodeDiscovery.consul.consulHost", "localhost:8500");
        System.setProperty("jcachex.distributed.nodeDiscovery.consul.serviceName", "jcachex-cluster");
        System.setProperty("jcachex.distributed.nodeDiscovery.consul.datacenter", "dc1");
        System.setProperty("jcachex.distributed.nodeDiscovery.consul.enableAcl", "false");

        ConfigurableApplicationContext context = SpringApplication.run(ConsulDiscoveryExample.class, args);

        try {
            // Get the distributed cache factory
            JCacheXDistributedCacheFactory cacheFactory = context.getBean(JCacheXDistributedCacheFactory.class);

            System.out.println("\n1. 🔍 Creating Consul-discovered cache");

            // Create a distributed cache that will use Consul discovery
            DistributedCache<String, Product> productCache = cacheFactory.createDistributedCache(
                    "consul-products",
                    config -> {
                        config.replicationFactor(3);
                        config.consistencyLevel(DistributedCache.ConsistencyLevel.EVENTUAL);
                        config.maximumSize(50000L);
                        config.expireAfterWrite(Duration.ofHours(2));
                    });

            // Demonstrate basic operations
            System.out.println("\n2. 🏗️ Product catalog operations");
            Product product1 = new Product("P001", "Laptop", 999.99);
            Product product2 = new Product("P002", "Mouse", 29.99);
            Product product3 = new Product("P003", "Keyboard", 79.99);

            productCache.put("laptop", product1);
            productCache.put("mouse", product2);
            productCache.put("keyboard", product3);

            Product retrievedProduct = productCache.get("laptop");
            System.out.println("   💻 Retrieved product: " + retrievedProduct.name + " - $" + retrievedProduct.price);

            // Show cluster information
            System.out.println("\n3. 🏢 Consul cluster information");
            DistributedCache.ClusterTopology topology = productCache.getClusterTopology();
            System.out.println("   🏢 Cluster: " + topology.getClusterName());
            System.out.println("   💚 Healthy nodes: " + topology.getHealthyNodeCount());
            System.out.println("   📊 Total nodes: " + topology.getNodes().size());

            // Show node statuses from Consul
            System.out.println("\n4. 🔍 Consul service discovery status");
            productCache.getNodeStatuses().forEach((nodeId, status) -> {
                System.out.println("   📍 Node: " + nodeId + " → " + status);
            });

            // Demonstrate distributed operations with Consul
            System.out.println("\n5. 🌐 Distributed operations");

            // Bulk operations across the cluster
            System.out.println("   📦 Bulk product loading...");
            for (int i = 1; i <= 100; i++) {
                Product bulkProduct = new Product("BULK" + i, "Product " + i, 10.0 + i);
                productCache.put("product-" + i, bulkProduct);
            }

            // Use strong consistency for inventory updates
            Product criticalProduct = new Product("CRITICAL", "High-Value Item", 9999.99);
            productCache
                    .putWithConsistency("critical-inventory", criticalProduct, DistributedCache.ConsistencyLevel.STRONG)
                    .thenRun(() -> System.out.println("   ✅ Critical inventory updated with strong consistency"));

            // Global cache invalidation for price updates
            productCache.invalidateGlobally("old-prices")
                    .thenRun(() -> System.out.println("   🗑️ Global price cache invalidation completed"));

            // Show cache statistics
            System.out.println("\n6. 📊 Cache statistics");
            System.out.println("   📦 Cache size: " + productCache.size());
            System.out.println("   📈 Hit rate: " + String.format("%.2f%%", productCache.stats().hitRate() * 100));

            // Demonstrate service health monitoring
            System.out.println("\n7. 🔄 Service health monitoring");
            simulateHealthChecks(productCache);

            // Demonstrate multi-datacenter setup
            System.out.println("\n8. 🌎 Multi-datacenter simulation");
            simulateMultiDatacenter(cacheFactory);

            // Keep the application running to show ongoing discovery
            System.out.println("\n9. 🔄 Continuous discovery (press Ctrl+C to stop)");
            Thread.sleep(5000);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
        }

        System.out.println("\n✅ Consul discovery example completed!");
    }

    private static void simulateHealthChecks(DistributedCache<String, Product> cache) {
        System.out.println("   🔄 Simulating health check updates...");

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("   ❤️ Health check: All nodes healthy");

                Thread.sleep(2000);
                System.out.println("   ⚠️ Health check: Node degraded - automatic traffic reduction");

                Thread.sleep(2000);
                System.out.println("   ✅ Health check: Node recovered - normal traffic resumed");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private static void simulateMultiDatacenter(JCacheXDistributedCacheFactory factory) {
        System.out.println("   🌎 Simulating multi-datacenter setup...");

        // This would typically be configured through Consul's datacenter settings
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("   🏢 DC1: Primary datacenter - 3 nodes");

                Thread.sleep(1000);
                System.out.println("   🏢 DC2: Secondary datacenter - 2 nodes");

                Thread.sleep(1000);
                System.out.println("   🔄 Cross-datacenter replication active");

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
        distributedConfig.setClusterName("consul-cluster");
        distributedConfig.setReplicationFactor(3);
        distributedConfig.setConsistencyLevel("EVENTUAL");

        // Set up Consul discovery configuration
        JCacheXProperties.NodeDiscoveryConfig discoveryConfig = new JCacheXProperties.NodeDiscoveryConfig();
        discoveryConfig.setType("CONSUL");
        discoveryConfig.setDiscoveryIntervalSeconds(30);
        discoveryConfig.setHealthCheckIntervalSeconds(10);

        JCacheXProperties.ConsulDiscoveryConfig consulConfig = new JCacheXProperties.ConsulDiscoveryConfig();
        consulConfig.setConsulHost("localhost:8500");
        consulConfig.setServiceName("jcachex-cluster");
        consulConfig.setDatacenter("dc1");
        consulConfig.setEnableAcl(false);

        discoveryConfig.setConsul(consulConfig);
        distributedConfig.setNodeDiscovery(discoveryConfig);

        properties.getDefaultConfig().setDistributed(distributedConfig);

        return properties;
    }

    static class Product {
        final String id;
        final String name;
        final double price;

        Product(String id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }
}
