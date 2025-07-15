import io.github.dhruv1110.jcachex.*;
import java.time.Duration;
import java.util.logging.Logger;

public class ProductionReady {
    private static final Logger logger = Logger.getLogger(ProductionReady.class.getName());

    public static void main(String[] args) {
        // Complete production setup with monitoring and error handling
        Cache<String, User> userCache = createProductionUserCache();

        // Demonstrate production-ready usage
        demonstrateProductionUsage(userCache);

        // Show monitoring capabilities
        showMonitoringCapabilities(userCache);

        // Demonstrate error handling
        demonstrateErrorHandling(userCache);
    }

    private static Cache<String, User> createProductionUserCache() {
        return JCacheXBuilder.forReadHeavyWorkload()
                .name("users")
                .maximumSize(50000L)
                .expireAfterWrite(Duration.ofHours(2))
                .expireAfterAccess(Duration.ofHours(4))
                .recordStats(true)
                .evictionListener(ProductionReady::onEviction)
                .loadingCache(ProductionReady::loadUserFromDatabase)
                .build();
    }

    private static void demonstrateProductionUsage(Cache<String, User> cache) {
        System.out.println("🚀 Production Cache Demo");
        System.out.println("========================");

        // Simulate user access patterns
        String[] userIds = { "user1", "user2", "user3", "user1", "user2", "user1" };

        for (String userId : userIds) {
            long startTime = System.nanoTime();
            User user = cache.get(userId);
            long endTime = System.nanoTime();

            System.out.printf("✅ User %s retrieved in %dns: %s%n",
                    userId, (endTime - startTime), user.getName());
        }
    }

    private static void showMonitoringCapabilities(Cache<String, User> cache) {
        System.out.println("\n📊 Production Monitoring");
        System.out.println("========================");

        CacheStats stats = cache.stats();
        System.out.printf("Cache size: %d%n", cache.size());
        System.out.printf("Hit rate: %.2f%%%n", stats.hitRate() * 100);
        System.out.printf("Miss rate: %.2f%%%n", stats.missRate() * 100);
        System.out.printf("Eviction count: %d%n", stats.evictionCount());
        System.out.printf("Average load time: %.2fms%n", stats.averageLoadPenalty() / 1_000_000.0);
        System.out.printf("Total requests: %d%n", stats.requestCount());

        // In production, you would send these metrics to your monitoring system
        // Examples: Prometheus, Grafana, CloudWatch, etc.
    }

    private static void demonstrateErrorHandling(Cache<String, User> cache) {
        System.out.println("\n🛡️ Production Error Handling");
        System.out.println("=============================");

        try {
            // Simulate error scenarios
            User user = cache.get("error-user");
            System.out.println("✅ Error handling: " + user.getName());
        } catch (Exception e) {
            System.out.println("❌ Handled error gracefully: " + e.getMessage());

            // In production: log to centralized logging, send alerts, etc.
            logger.warning("Cache operation failed for user: error-user - " + e.getMessage());
        }
    }

    // Production-ready eviction listener
    private static void onEviction(String key, User user, RemovalCause cause) {
        logger.info(String.format("Evicted user %s (name: %s) due to %s",
                key, user != null ? user.getName() : "null", cause));

        // In production, you might:
        // 1. Log to centralized logging (ELK, Splunk, etc.)
        // 2. Send metrics to monitoring systems
        // 3. Trigger alerts for unexpected evictions
        // 4. Implement custom eviction handling logic
    }

    // Production-ready cache loader with error handling
    private static User loadUserFromDatabase(String userId) {
        // Simulate database call with realistic timing
        try {
            Thread.sleep(10); // Simulate DB latency

            if ("error-user".equals(userId)) {
                throw new RuntimeException("Database connection failed");
            }

            logger.info("Loading user " + userId + " from database");
            return new User(userId, "User " + userId, userId + "@example.com");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Database load interrupted", e);
        }
    }

    // Production-ready User class
    static class User {
        private final String id;
        private final String name;
        private final String email;

        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public String toString() {
            return String.format("User{id='%s', name='%s', email='%s'}", id, name, email);
        }
    }
}
