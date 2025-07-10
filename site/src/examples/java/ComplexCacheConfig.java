import io.github.dhruv1110.jcachex.*;
import io.github.dhruv1110.jcachex.eviction.*;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

        // Composite eviction strategy
        CompositeEvictionStrategy<String, Product> evictionStrategy = CompositeEvictionStrategy
                .<String, Product>builder()
                .addStrategy(new LRUEvictionStrategy<>())
                .addStrategy(new WeightBasedEvictionStrategy<>(new ProductWeigher()))
                .addStrategy(new IdleTimeEvictionStrategy<>(Duration.ofHours(2)))
                .build();

        // Advanced cache configuration
        CacheConfig<String, Product> config = CacheConfig.<String, Product>builder()
                .maximumSize(10000L)
                .maximumWeight(1024 * 1024) // 1MB max weight
                .expireAfterWrite(Duration.ofHours(6))
                .expireAfterAccess(Duration.ofHours(2))
                .evictionStrategy(evictionStrategy)
                .eventListener(new ProductionCacheListener<>())
                .recordStats(true)
                .statsRecordingExecutor(executor)
                .cleanupExecutor(executor)
                .build();

        Cache<String, Product> productCache = new DefaultCache<>(config);

        // Cache warming with bulk operations
        warmupCache(productCache);

        // Monitor cache health
        monitorCacheHealth(productCache);

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
}
