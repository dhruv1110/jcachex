import io.github.dhruv1110.jcachex.*;
import java.time.Duration;

public class FiveMinutePower {
    public static void main(String[] args) {
        // Show the "magic" of profiles - ONE line gives optimal config
        Cache<String, Product> productCache = JCacheXBuilder
                .forReadHeavyWorkload() // Automatically optimized for read-heavy patterns
                .name("products")
                .maximumSize(10000L)
                .build();

        // Results: 11.5ns GET performance automatically configured!
        // No complex tuning needed - the profile handles it

        // Simulate loading products
        for (int i = 1; i <= 100; i++) {
            productCache.put("product" + i, new Product("Product " + i, 99.99 + i));
        }

        // Lightning-fast retrieval
        long startTime = System.nanoTime();
        Product product = productCache.get("product42");
        long endTime = System.nanoTime();

        System.out.println("✅ Retrieved product: " + product.getName());
        System.out.println("⚡ Time taken: " + (endTime - startTime) + "ns");
        System.out.println("🎯 Hit rate: " + String.format("%.2f%%", productCache.stats().hitRate() * 100));

        // Different workload? Just change the profile!
        Cache<String, Session> sessionCache = JCacheXBuilder
                .forWriteHeavyWorkload() // Optimized for frequent writes
                .name("sessions")
                .maximumSize(5000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .build();

        System.out.println("✅ Multiple optimized caches created with zero complexity!");
    }

    // Simple product class for demonstration
    static class Product {
        private String name;
        private double price;

        public Product(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }
    }

    // Simple session class for demonstration
    static class Session {
        private String userId;
        private long lastAccess;

        public Session(String userId) {
            this.userId = userId;
            this.lastAccess = System.currentTimeMillis();
        }

        public String getUserId() {
            return userId;
        }

        public long getLastAccess() {
            return lastAccess;
        }
    }
}
