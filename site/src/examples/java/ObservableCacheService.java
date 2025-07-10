import io.github.dhruv1110.jcachex.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class ObservableCacheService {
    private static final Logger logger = LoggerFactory.getLogger(ObservableCacheService.class);
    private final Cache<String, String> cache;
    private final MeterRegistry meterRegistry;
    private final Timer cacheLoadTimer;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final ScheduledExecutorService scheduler;
    private final AlertService alertService;

    public ObservableCacheService(MeterRegistry meterRegistry, AlertService alertService) {
        this.meterRegistry = meterRegistry;
        this.alertService = alertService;
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Configure cache with observability
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumSize(1000L)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats(true)
                .eventListener(new ObservabilityCacheEventListener())
                .build();

        this.cache = new DefaultCache<>(config);

        // Initialize metrics
        this.cacheLoadTimer = Timer.builder("cache.load.time")
                .description("Time taken to load cache values")
                .register(meterRegistry);

        this.cacheHitCounter = Counter.builder("cache.hits")
                .description("Number of cache hits")
                .register(meterRegistry);

        this.cacheMissCounter = Counter.builder("cache.misses")
                .description("Number of cache misses")
                .register(meterRegistry);

        // Register cache size gauge
        Gauge.builder("cache.size")
                .description("Current cache size")
                .register(meterRegistry, cache, Cache::size);

        // Register hit rate gauge
        Gauge.builder("cache.hit_rate")
                .description("Cache hit rate")
                .register(meterRegistry, cache, c -> c.stats().hitRate());

        // Start periodic metrics collection
        startMetricsCollection();
    }

    public String getValue(String key) {
        String value = cache.get(key);

        if (value != null) {
            cacheHitCounter.increment();
            return value;
        }

        cacheMissCounter.increment();

        // Load with timing
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            value = loadValue(key);
            cache.put(key, value);
            return value;
        } finally {
            sample.stop(cacheLoadTimer);
        }
    }

    private String loadValue(String key) {
        // Simulate expensive operation
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return "loaded-" + key;
    }

    private void startMetricsCollection() {
        scheduler.scheduleAtFixedRate(() -> {
            CacheStats stats = cache.stats();

            // Custom metrics
            meterRegistry.gauge("cache.eviction_count", stats.evictionCount());
            meterRegistry.gauge("cache.load_exception_count", stats.loadExceptionCount());
            meterRegistry.gauge("cache.average_load_time", stats.averageLoadTime());

            // Log detailed metrics
            logCacheMetrics(stats);

            // Health checks
            performHealthChecks(stats);

        }, 0, 30, TimeUnit.SECONDS);
    }

    private void logCacheMetrics(CacheStats stats) {
        MDC.put("cache.size", String.valueOf(cache.size()));
        MDC.put("cache.hit_rate", String.format("%.2f", stats.hitRate()));
        MDC.put("cache.miss_rate", String.format("%.2f", stats.missRate()));
        MDC.put("cache.eviction_count", String.valueOf(stats.evictionCount()));

        logger.info("Cache metrics snapshot");

        MDC.clear();
    }

    private void performHealthChecks(CacheStats stats) {
        // Alert on low hit rate
        if (stats.hitRate() < 0.7) {
            alertService.sendAlert(
                    "Cache hit rate is low: " + String.format("%.2f%%", stats.hitRate() * 100),
                    AlertLevel.WARNING);
        }

        // Alert on high eviction rate
        long totalRequests = stats.requestCount();
        if (totalRequests > 0) {
            double evictionRate = (double) stats.evictionCount() / totalRequests;
            if (evictionRate > 0.1) {
                alertService.sendAlert(
                        "High cache eviction rate: " + String.format("%.2f%%", evictionRate * 100),
                        AlertLevel.WARNING);
            }
        }

        // Alert on slow load times
        if (stats.averageLoadTime() > 500) {
            alertService.sendAlert(
                    "Slow cache load time: " + stats.averageLoadTime() + "ms",
                    AlertLevel.WARNING);
        }
    }

    public CacheHealthReport generateHealthReport() {
        CacheStats stats = cache.stats();

        return CacheHealthReport.builder()
                .cacheSize(cache.size())
                .hitRate(stats.hitRate())
                .missRate(stats.missRate())
                .evictionCount(stats.evictionCount())
                .averageLoadTime(stats.averageLoadTime())
                .requestCount(stats.requestCount())
                .healthScore(calculateHealthScore(stats))
                .timestamp(Instant.now())
                .build();
    }

    private double calculateHealthScore(CacheStats stats) {
        double score = 1.0;

        // Penalize low hit rate
        if (stats.hitRate() < 0.5) {
            score -= 0.4;
        } else if (stats.hitRate() < 0.7) {
            score -= 0.2;
        }

        // Penalize slow load times
        if (stats.averageLoadTime() > 200) {
            score -= 0.2;
        }

        // Penalize high eviction rate
        long totalRequests = stats.requestCount();
        if (totalRequests > 0) {
            double evictionRate = (double) stats.evictionCount() / totalRequests;
            if (evictionRate > 0.05) {
                score -= 0.3;
            }
        }

        return Math.max(0, score);
    }

    private class ObservabilityCacheEventListener implements CacheEventListener<String, String> {
        @Override
        public void onEviction(String key, String value, EvictionReason reason) {
            meterRegistry.counter("cache.evictions", "reason", reason.toString()).increment();

            if (reason == EvictionReason.SIZE) {
                logger.warn("Cache size-based eviction for key: {}", key);
            }
        }

        @Override
        public void onRemoval(String key, String value, EvictionReason reason) {
            meterRegistry.counter("cache.removals", "reason", reason.toString()).increment();
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
