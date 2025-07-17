package io.github.dhruv1110.jcachex.example.distributed;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheFactory;
import io.github.dhruv1110.jcachex.JCacheXBuilder;
import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.DistributedCache.ConsistencyLevel;
import io.github.dhruv1110.jcachex.observability.MetricsRegistry;
import io.github.dhruv1110.jcachex.resilience.CircuitBreaker;
import io.github.dhruv1110.jcachex.warming.CacheWarmingStrategy;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Comprehensive example demonstrating JCacheX distributed caching capabilities.
 *
 * This example shows:
 * 1. Seamless migration from local to distributed caching
 * 2. Multiple consistency models
 * 3. Environment-aware adaptive caching
 * 4. Integration with advanced features
 * 5. Production-ready configurations
 */
public class DistributedCacheExample {

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 JCacheX Distributed Caching Examples");
        System.out.println("========================================");

        // Example 1: Local to Distributed Migration
        demonstrateSeamlessScaling();

        // Example 2: Multiple Consistency Models
        demonstrateConsistencyModels();

        // Example 3: Environment-Aware Caching
        demonstrateAdaptiveCaching();

        // Example 4: Production-Ready Configuration
        demonstrateProductionSetup();

        // Example 5: Advanced Features Integration
        demonstrateAdvancedFeatures();

        System.out.println("\n✅ All examples completed successfully!");
    }

    /**
     * Demonstrates seamless scaling from local to distributed caching
     * with the same API - JCacheX's key differentiator.
     */
    private static void demonstrateSeamlessScaling() {
        System.out.println("\n1. 🔄 Seamless Local-to-Distributed Migration");
        System.out.println("   Same API, different scale!");

        // Start with local cache
        Cache<String, User> localCache = JCacheXBuilder.<String, User>create()
                .name("users")
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .build();

        // Use the cache
        localCache.put("user1", new User("John", "john@example.com"));
        User user = localCache.get("user1");
        System.out.println("   📦 Local cache: " + user.name + " (" + user.email + ")");

        // Scale to distributed with ZERO code changes
        DistributedCache<String, User> distributedCache = CacheFactory.<String, User>distributed()
                .name("users")
                .clusterName("user-service")
                .nodes("cache-1:8080", "cache-2:8080", "cache-3:8080")
                .replicationFactor(2)
                .consistencyLevel(DistributedCache.ConsistencyLevel.EVENTUAL)
                .maximumSize(10000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .create();

        // Same API calls work identically
        distributedCache.put("user2", new User("Jane", "jane@example.com"));
        User distributedUser = distributedCache.get("user2");
        System.out.println("   🌐 Distributed cache: " + distributedUser.name + " (" + distributedUser.email + ")");

        // Show cluster information
        System.out.println("   🏢 Cluster: " + distributedCache.getClusterTopology().getClusterName());
        System.out.println("   💚 Healthy nodes: " + distributedCache.getClusterTopology().getHealthyNodeCount());
    }

    /**
     * Demonstrates multiple consistency models for different use cases.
     */
    private static void demonstrateConsistencyModels() {
        System.out.println("\n2. ⚖️ Multiple Consistency Models");
        System.out.println("   Choose the right consistency for your use case!");

        DistributedCache<String, Object> cache = CacheFactory.<String, Object>distributed()
                .name("multi-consistency")
                .clusterName("consistency-demo")
                .nodes("node-1:8080", "node-2:8080", "node-3:8080")
                .replicationFactor(2)
                .consistencyLevel(DistributedCache.ConsistencyLevel.EVENTUAL) // Default
                .create();

        // Strong consistency for financial data
        Account account = new Account("123456", 1000.0);
        cache.putWithConsistency("account-123", account, DistributedCache.ConsistencyLevel.STRONG);
        System.out.println("   💰 Strong consistency: Account balance = $" + account.balance);

        // Eventual consistency for user preferences
        UserPreferences prefs = new UserPreferences("dark-mode", "metric");
        cache.putWithConsistency("prefs-456", prefs, DistributedCache.ConsistencyLevel.EVENTUAL);
        System.out.println("   ⚙️ Eventual consistency: Theme = " + prefs.theme);

        // Session consistency for shopping carts
        ShoppingCart cart = new ShoppingCart("user-789", 3);
        cache.putWithConsistency("cart-789", cart, DistributedCache.ConsistencyLevel.SESSION);
        System.out.println("   🛒 Session consistency: Cart items = " + cart.itemCount);

        // Demonstrate async operations with consistency
        CompletableFuture<Void> asyncOperation = cache.putWithConsistency("async-key", "async-value",
                DistributedCache.ConsistencyLevel.EVENTUAL);
        asyncOperation.thenRun(() -> System.out.println("   🔄 Async operation completed"));
    }

    /**
     * Demonstrates environment-aware adaptive caching that chooses
     * local vs distributed based on environment.
     */
    private static void demonstrateAdaptiveCaching() {
        System.out.println("\n3. 🌍 Environment-Aware Adaptive Caching");
        System.out.println("   Automatically choose local or distributed!");

        // Simulate different environments
        simulateEnvironment("development");
        simulateEnvironment("production");
    }

    private static void simulateEnvironment(String environment) {
        // Set environment variable (normally done by deployment)
        System.setProperty("ENVIRONMENT", environment);

        Cache<String, String> adaptiveCache = JCacheXBuilder.<String, String>withSmartDefaults()
                .name("adaptive-cache")
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .build();

        adaptiveCache.put("env-test", "value-" + environment);
        String value = adaptiveCache.get("env-test");

        String cacheType = (adaptiveCache instanceof DistributedCache) ? "distributed" : "local";
        System.out.println("   📊 Environment: " + environment + " → " + cacheType + " cache (" + value + ")");
    }

    /**
     * Demonstrates production-ready configuration with all features enabled.
     */
    private static void demonstrateProductionSetup() {
        System.out.println("\n4. 🏭 Production-Ready Configuration");
        System.out.println("   All features working together!");

        // Create production-ready cache with all features
        DistributedCache<String, User> productionCache = CacheFactory.<String, User>distributed()
                .name("production-users")
                .clusterName("production-cluster")
                .nodes("prod-cache-1:8080", "prod-cache-2:8080", "prod-cache-3:8080")
                .replicationFactor(2)
                .consistencyLevel(DistributedCache.ConsistencyLevel.EVENTUAL)
                .maximumSize(100000L)
                .expireAfterWrite(Duration.ofHours(1))
                .enableWarming(true)
                .enableObservability(true)
                .enableResilience(true)
                .create();

        // Simulate production usage
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Simulate concurrent users
        for (int i = 0; i < 100; i++) {
            final int userId = i;
            executor.submit(() -> {
                String key = "user-" + userId;
                User user = new User("User" + userId, "user" + userId + "@example.com");
                productionCache.put(key, user);

                // Simulate reads
                User retrieved = productionCache.get(key);
                if (retrieved != null && userId % 20 == 0) {
                    System.out.println("   👤 Cached user: " + retrieved.name);
                }
            });
        }

        // Wait a bit for operations to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Show statistics
        System.out.println("   📊 Cache size: " + productionCache.size());
        System.out.println("   📈 Hit rate: " + String.format("%.2f%%",
                productionCache.stats().hitRate() * 100));

        executor.shutdown();
    }

    /**
     * Demonstrates advanced features integration.
     */
    private static void demonstrateAdvancedFeatures() {
        System.out.println("\n5. 🌟 Advanced Features Integration");
        System.out.println("   Warming, Observability, and Resilience!");

        // Create cache with custom warming strategy
        DistributedCache<String, String> advancedCache = CacheFactory.<String, String>distributed()
                .name("advanced-cache")
                .clusterName("advanced-cluster")
                .nodes("adv-1:8080", "adv-2:8080")
                .replicationFactor(2)
                .consistencyLevel(DistributedCache.ConsistencyLevel.EVENTUAL)
                .maximumSize(10000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .enableWarming(true)
                .enableObservability(true)
                .enableResilience(true)
                .create();

        // Demonstrate global operations
        advancedCache.put("global-key", "global-value");

        // Invalidate globally across all nodes
        advancedCache.invalidateGlobally("old-key")
                .thenRun(() -> System.out.println("   🗑️ Global invalidation completed"));

        // Show cluster topology
        DistributedCache.ClusterTopology topology = advancedCache.getClusterTopology();
        System.out.println("   🏢 Cluster: " + topology.getClusterName());
        System.out.println("   💚 Healthy nodes: " + topology.getHealthyNodeCount());
        System.out.println("   📊 Partitions: " + topology.getPartitionCount());

        // Show distributed metrics
        DistributedCache.DistributedMetrics metrics = advancedCache.getDistributedMetrics();
        System.out.println("   📡 Network requests: " + metrics.getNetworkRequests());
        System.out.println("   ✅ Success rate: " + String.format("%.2f%%",
                metrics.getNetworkSuccessRate() * 100));
        System.out.println("   ⚡ Avg latency: " + String.format("%.2f ms",
                metrics.getAverageNetworkLatency()));
    }

    // Helper classes for examples
    static class User {
        String name;
        String email;

        User(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }

    static class Account {
        String accountId;
        double balance;

        Account(String accountId, double balance) {
            this.accountId = accountId;
            this.balance = balance;
        }
    }

    static class UserPreferences {
        String theme;
        String units;

        UserPreferences(String theme, String units) {
            this.theme = theme;
            this.units = units;
        }
    }

    static class ShoppingCart {
        String userId;
        int itemCount;

        ShoppingCart(String userId, int itemCount) {
            this.userId = userId;
            this.itemCount = itemCount;
        }
    }
}
