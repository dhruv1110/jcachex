import io.github.dhruv1110.jcachex.*;
import io.github.dhruv1110.jcachex.eviction.FrequencySketchType;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class AdvancedCacheBuilderExample {

    public static void main(String[] args) {
        // Demonstrate all cache types with their optimal use cases
        demonstrateReadHeavyWorkload();
        demonstrateWriteHeavyWorkload();
        demonstrateMemoryConstrainedEnvironment();
        demonstrateHighPerformanceSetup();
        demonstrateFrequencySketchOptions();
        demonstrateCustomConfiguration();

        // Performance comparison
        performanceComparison();
    }

    private static void demonstrateReadHeavyWorkload() {
        System.out.println("=== Read-Heavy Workload Optimization ===");

        // Best for read-heavy scenarios - ReadOnly optimized with advanced frequency
        // tracking
        Cache<String, Product> productCache = CacheBuilder.forReadHeavyWorkload()
                .maximumSize(5000L)
                .expireAfterWrite(Duration.ofHours(2))
                .build();

        // Simulate read-heavy workload
        for (int i = 0; i < 1000; i++) {
            productCache.put("product-" + i, new Product("Product " + i, "Description " + i));
        }

        // Measure read performance
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            productCache.get("product-" + (i % 1000));
        }
        long endTime = System.nanoTime();

        System.out.println("Read-heavy cache GET time: " + (endTime - startTime) / 10000 + "ns per operation");
        System.out.println("Expected: ~11.5ns (ReadOnly optimized)");

        CacheStats stats = productCache.stats();
        System.out.println("Hit rate: " + String.format("%.2f%%", stats.hitRate() * 100));
        System.out.println();
    }

    private static void demonstrateWriteHeavyWorkload() {
        System.out.println("=== Write-Heavy Workload Optimization ===");

        // Best for write-heavy scenarios
        Cache<String, UserSession> sessionCache = CacheBuilder.forWriteHeavyWorkload()
                .maximumSize(10000L)
                .expireAfterAccess(Duration.ofMinutes(30))
                .build();

        // Simulate write-heavy workload
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            sessionCache.put("session-" + i, new UserSession("user-" + i, System.currentTimeMillis()));
        }
        long endTime = System.nanoTime();

        System.out.println("Write-heavy cache PUT time: " + (endTime - startTime) / 10000 + "ns per operation");
        System.out.println("Expected: ~393.5ns (WriteHeavy optimized)");
        System.out.println();
    }

    private static void demonstrateMemoryConstrainedEnvironment() {
        System.out.println("=== Memory-Constrained Environment ===");

        // Minimizes memory allocations and GC pressure
        Cache<String, Configuration> configCache = CacheBuilder.forMemoryConstrainedEnvironment()
                .maximumSize(100L)
                .expireAfterWrite(Duration.ofHours(12))
                .build();

        // Test memory efficiency
        for (int i = 0; i < 100; i++) {
            configCache.put("config-" + i, new Configuration("key-" + i, "value-" + i));
        }

        System.out.println("Memory-constrained cache size: " + configCache.size());
        System.out.println("Expected: Minimal GC pressure with allocation optimization");
        System.out.println();
    }

    private static void demonstrateHighPerformanceSetup() {
        System.out.println("=== High-Performance Setup ===");

        // Ultra-fast cache for high-performance scenarios
        Cache<String, String> fastCache = CacheBuilder.forHighPerformance()
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofMinutes(15))
                .build();

        // Measure performance
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            fastCache.put("key-" + i, "value-" + i);
        }
        long endTime = System.nanoTime();

        System.out.println("High-performance PUT time: " + (endTime - startTime) / 1000 + "ns per operation");
        System.out.println("Expected: ~63.8ns (JIT optimized)");
        System.out.println();
    }

    private static void demonstrateFrequencySketchOptions() {
        System.out.println("=== Frequency Sketch Options ===");

        // No frequency sketch - minimal overhead
        Cache<String, String> noSketchCache = CacheBuilder.newBuilder()
                .cacheType(CacheType.DEFAULT)
                .maximumSize(1000L)
                .evictionStrategy(EvictionStrategy.ENHANCED_LRU)
                .frequencySketchType(FrequencySketchType.NONE)
                .recordStats(true)
                .build();

        // Basic frequency sketch - balanced approach
        Cache<String, String> basicSketchCache = CacheBuilder.newBuilder()
                .cacheType(CacheType.DEFAULT)
                .maximumSize(1000L)
                .evictionStrategy(EvictionStrategy.ENHANCED_LRU)
                .frequencySketchType(FrequencySketchType.BASIC)
                .recordStats(true)
                .build();

        // Optimized frequency sketch - maximum accuracy
        Cache<String, String> optimizedSketchCache = CacheBuilder.newBuilder()
                .cacheType(CacheType.DEFAULT)
                .maximumSize(1000L)
                .evictionStrategy(EvictionStrategy.ENHANCED_LFU)
                .frequencySketchType(FrequencySketchType.OPTIMIZED)
                .recordStats(true)
                .build();

        // Test different access patterns
        testAccessPattern(noSketchCache, "No Sketch");
        testAccessPattern(basicSketchCache, "Basic Sketch");
        testAccessPattern(optimizedSketchCache, "Optimized Sketch");
        System.out.println();
    }

    private static void testAccessPattern(Cache<String, String> cache, String description) {
        // Simulate skewed access pattern
        for (int i = 0; i < 2000; i++) {
            cache.put("key-" + i, "value-" + i);
        }

        // Hot keys (frequently accessed)
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 10; j++) {
                cache.get("key-" + (i % 100)); // Access first 100 keys repeatedly
            }
        }

        // Cold keys (rarely accessed)
        for (int i = 100; i < 2000; i++) {
            cache.get("key-" + i); // Access once
        }

        CacheStats stats = cache.stats();
        System.out.println(description + " - Hit rate: " + String.format("%.2f%%", stats.hitRate() * 100));
    }

    private static void demonstrateCustomConfiguration() {
        System.out.println("=== Custom Configuration ===");

        // Fully customized cache with specific requirements
        Cache<String, User> userCache = CacheBuilder.newBuilder()
                .cacheType(CacheType.LOCALITY_OPTIMIZED)
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofHours(1))
                .expireAfterAccess(Duration.ofMinutes(30))
                .evictionStrategy(EvictionStrategy.TINY_WINDOW_LFU)
                .frequencySketchType(FrequencySketchType.OPTIMIZED)
                .recordStats(true)
                .build();

        // Test custom configuration
        for (int i = 0; i < 500; i++) {
            userCache.put("user-" + i, new User("User " + i, "user" + i + "@example.com"));
        }

        System.out.println("Custom cache size: " + userCache.size());
        System.out.println("Uses: LocalityOptimized + TinyWindowLFU + Optimized FrequencySketch");
        System.out.println();
    }

    private static void performanceComparison() {
        System.out.println("=== Performance Comparison ===");

        // Create different cache types for comparison
        Cache<String, String> defaultCache = CacheBuilder.newBuilder()
                .cacheType(CacheType.DEFAULT)
                .maximumSize(1000L)
                .build();

        Cache<String, String> jitCache = CacheBuilder.newBuilder()
                .cacheType(CacheType.JIT_OPTIMIZED)
                .maximumSize(1000L)
                .build();

        Cache<String, String> localityCache = CacheBuilder.newBuilder()
                .cacheType(CacheType.LOCALITY_OPTIMIZED)
                .maximumSize(1000L)
                .build();

        // Warm up caches
        for (int i = 0; i < 1000; i++) {
            String key = "key-" + i;
            String value = "value-" + i;
            defaultCache.put(key, value);
            jitCache.put(key, value);
            localityCache.put(key, value);
        }

        // Measure GET performance
        int iterations = 100000;

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            defaultCache.get("key-" + (i % 1000));
        }
        long defaultTime = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            jitCache.get("key-" + (i % 1000));
        }
        long jitTime = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            localityCache.get("key-" + (i % 1000));
        }
        long localityTime = System.nanoTime() - startTime;

        System.out.println("Performance Results (GET operations):");
        System.out.println("Default Cache: " + defaultTime / iterations + "ns per operation");
        System.out.println("JIT Optimized: " + jitTime / iterations + "ns per operation");
        System.out.println("Locality Optimized: " + localityTime / iterations + "ns per operation");
        System.out.println();

        System.out.println("Expected ranges:");
        System.out.println("Default: ~40.4ns");
        System.out.println("JIT: ~24.6ns");
        System.out.println("Locality: ~9.7ns (fastest)");
    }

    // Helper classes
    static class Product {
        private final String name;
        private final String description;

        public Product(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    static class UserSession {
        private final String userId;
        private final long timestamp;

        public UserSession(String userId, long timestamp) {
            this.userId = userId;
            this.timestamp = timestamp;
        }

        public String getUserId() {
            return userId;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    static class Configuration {
        private final String key;
        private final String value;

        public Configuration(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    static class User {
        private final String name;
        private final String email;

        public User(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }
}
