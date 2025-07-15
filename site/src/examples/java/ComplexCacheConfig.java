import io.github.dhruv1110.jcachex.*;
import io.github.dhruv1110.jcachex.eviction.*;
import io.github.dhruv1110.jcachex.profiles.ProfileName;
import io.github.dhruv1110.jcachex.profiles.WorkloadCharacteristics;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.CompletableFuture;

// Custom cache event listener for monitoring
public class ProductionCacheListener<K, V> implements CacheEventListener<K, V> {
    private final Logger logger = LoggerFactory.getLogger(ProductionCacheListener.class);

    @Override
    public void onEviction(K key, V value, EvictionReason reason) {
        logger.info("Cache eviction: key={}, reason={}", key, reason);

        // Send metrics to monitoring system
        if (reason == EvictionReason.SIZE) {
            incrementMetric("cache.eviction.size");
        } else if (reason == EvictionReason.TIME) {
            incrementMetric("cache.eviction.time");
        }
    }

    @Override
    public void onRemoval(K key, V value, EvictionReason reason) {
        logger.debug("Cache removal: key={}, reason={}", key, reason);
    }

    private void incrementMetric(String metric) {
        // Integration with metrics system (e.g., Micrometer, Prometheus)
        // MetricsRegistry.counter(metric).increment();
    }
}

// Custom weigher for memory-based eviction
public class ProductWeigher implements Weigher<String, Product> {
    @Override
    public int weigh(String key, Product value) {
        // Calculate memory footprint
        int keyWeight = key.length() * 2; // String overhead
        int valueWeight = value.getName().length() * 2 +
                value.getDescription().length() * 2 +
                100; // Object overhead
        return keyWeight + valueWeight;
    }
}

public class ComplexCacheConfigExample {
    public static void main(String[] args) {
        // Custom executor for cache operations
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        // Advanced cache configuration with JCacheXBuilder
        Cache<String, Product> productCache = JCacheXBuilder.forReadHeavyWorkload()
                .name("products")
                .maximumSize(10000L)
                .maximumWeight(1024 * 1024) // 1MB max weight
                .weigher(new ProductWeigher())
                .expireAfterWrite(Duration.ofHours(6))
                .expireAfterAccess(Duration.ofHours(2))
                .listener(new ProductionCacheListener<>())
                .recordStats(true)
                .build();

        // Alternative: Use high-performance profile with custom configuration
        Cache<String, Product> highPerfCache = JCacheXBuilder.forHighPerformance()
                .name("high-performance-products")
                .maximumSize(5000L)
                .expireAfterWrite(Duration.ofHours(4))
                .weigher(new ProductWeigher())
                .listener(new ProductionCacheListener<>())
                .recordStats(true)
                .build();

        // Smart defaults with automatic profile selection
        Cache<String, Product> smartCache = JCacheXBuilder.withSmartDefaults()
                .name("smart-products")
                .maximumSize(8000L)
                .workloadCharacteristics(WorkloadCharacteristics.builder()
                        .readToWriteRatio(7.0) // Read-heavy workload
                        .accessPattern(WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
                        .memoryConstraint(WorkloadCharacteristics.MemoryConstraint.NORMAL)
                        .build())
                .listener(new ProductionCacheListener<>())
                .recordStats(true)
                .build();

        // Ultra-low latency cache for real-time applications
        Cache<String, Product> ultraFastCache = JCacheXBuilder.forUltraLowLatency()
                .name("ultra-fast-products")
                .maximumSize(50000L)
                .recordStats(true)
                .build();

        // Machine learning optimized cache with predictive capabilities
        Cache<String, Product> mlCache = JCacheXBuilder.forMachineLearning()
                .name("ml-products")
                .maximumSize(2000L)
                .expireAfterWrite(Duration.ofHours(8))
                .recordStats(true)
                .build();

        // Async loading cache with refresh-ahead pattern
        Cache<String, Product> asyncCache = JCacheXBuilder.forHighPerformance()
                .name("async-products")
                .maximumSize(3000L)
                .loader(productId -> loadProductFromDatabase(productId))
                .asyncLoader(productId -> loadProductFromDatabaseAsync(productId))
                .refreshAfterWrite(Duration.ofMinutes(30)) // Refresh before expiration
                .expireAfterWrite(Duration.ofHours(2))
                .recordStats(true)
                .build();

        // Cache warming with bulk operations
        warmupCache(productCache);

        // Monitor cache health
        monitorCacheHealth(productCache);

        // Demonstrate different cache patterns
        demonstrateCachePatterns(productCache, highPerfCache, ultraFastCache, mlCache);

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down cache...");
            CacheStats finalStats = productCache.stats();
            System.out.println("Final cache statistics:");
            System.out.println("  Total requests: " + finalStats.requestCount());
            System.out.println("  Hit rate: " + String.format("%.2f%%", finalStats.hitRate() * 100));
            System.out.println("  Eviction count: " + finalStats.evictionCount());

            executor.shutdown();
        }));
    }

    private static void warmupCache(Cache<String, Product> cache) {
        // Simulate cache warming from database
        for (int i = 1; i <= 1000; i++) {
            String productId = "product-" + i;
            Product product = new Product(productId, "Product " + i, "Description for product " + i);
            cache.put(productId, product);
        }
        System.out.println("Cache warmed up with " + cache.size() + " products");
    }

    private static void monitorCacheHealth(Cache<String, Product> cache) {
        // Scheduled health check
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
        monitor.scheduleAtFixedRate(() -> {
            CacheStats stats = cache.stats();
            double hitRate = stats.hitRate();

            System.out.println("Cache health check:");
            System.out.println("  Size: " + cache.size());
            System.out.println("  Hit rate: " + String.format("%.2f%%", hitRate * 100));

            // Alert if hit rate is too low
            if (hitRate < 0.8) {
                System.out.println("WARNING: Cache hit rate is below 80%");
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    private static void demonstrateCachePatterns(
            Cache<String, Product> readHeavyCache,
            Cache<String, Product> highPerfCache,
            Cache<String, Product> ultraFastCache,
            Cache<String, Product> mlCache) {

        System.out.println("\n=== Cache Performance Comparison ===");

        // Test performance of different cache types
        int iterations = 10000;

        // Read-heavy cache optimized for GET operations
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            readHeavyCache.get("product-" + (i % 1000));
        }
        long readHeavyTime = System.nanoTime() - startTime;

        // High-performance cache with balanced optimization
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            highPerfCache.get("product-" + (i % 1000));
        }
        long highPerfTime = System.nanoTime() - startTime;

        // Ultra-fast cache for real-time scenarios
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ultraFastCache.get("product-" + (i % 1000));
        }
        long ultraFastTime = System.nanoTime() - startTime;

        System.out.println("Performance results for " + iterations + " GET operations:");
        System.out
                .println("  Read-heavy cache: " + (readHeavyTime / iterations) + "ns per operation (~11.5ns expected)");
        System.out.println(
                "  High-performance cache: " + (highPerfTime / iterations) + "ns per operation (~24.6ns expected)");
        System.out
                .println("  Ultra-fast cache: " + (ultraFastTime / iterations) + "ns per operation (~7.9ns expected)");

        // Test ML cache features
        System.out.println("\n=== Machine Learning Cache Features ===");
        mlCache.put("ml-product-1", new Product("ml-1", "ML Product 1", "Predictive cached product"));
        Product mlProduct = mlCache.get("ml-product-1");
        System.out.println("ML cache retrieved: " + (mlProduct != null ? mlProduct.getName() : "null"));

        System.out.println("\n=== Cache Statistics Comparison ===");
        printCacheStats("Read-Heavy", readHeavyCache);
        printCacheStats("High-Performance", highPerfCache);
        printCacheStats("Ultra-Fast", ultraFastCache);
        printCacheStats("ML-Optimized", mlCache);
    }

    private static void printCacheStats(String name, Cache<String, Product> cache) {
        CacheStats stats = cache.stats();
        System.out.println(name + " Cache:");
        System.out.println("  Size: " + cache.size());
        System.out.println("  Hit rate: " + String.format("%.2f%%", stats.hitRate() * 100));
        System.out.println("  Request count: " + stats.requestCount());
        System.out.println("  Eviction count: " + stats.evictionCount());
        System.out.println();
    }

    // Mock methods for demonstration
    private static Product loadProductFromDatabase(String productId) {
        // Simulate database call
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
        return new Product(productId, "Loaded Product " + productId, "Loaded from database");
    }

    private static CompletableFuture<Product> loadProductFromDatabaseAsync(String productId) {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate async database call
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
            }
            return new Product(productId, "Async Loaded Product " + productId, "Async loaded from database");
        });
    }

    // Product class for demonstration
    static class Product {
        private final String id;
        private final String name;
        private final String description;

        public Product(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "Product{id='" + id + "', name='" + name + "', description='" + description + "'}";
        }
    }

    // Mock logger for demonstration
    static class LoggerFactory {
        static Logger getLogger(Class<?> clazz) {
            return new Logger();
        }
    }

    static class Logger {
        void info(String message, Object... args) {
            System.out.println("INFO: " + String.format(message.replace("{}", "%s"), args));
        }

        void debug(String message, Object... args) {
            // Debug messages suppressed in this example
        }
    }
}
