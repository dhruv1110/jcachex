package io.github.dhruv1110.jcachex.benchmarks;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.DefaultCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.openjdk.jmh.annotations.*;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Quick benchmark for fast validation of setup with different JCacheX
 * configurations.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class QuickBenchmark {

    protected static final int CACHE_SIZE = 1000;
    protected static final int OPERATIONS_COUNT = 100;

    // Test data
    protected String[] keys;
    protected String[] values;

    // Cache implementations - now with alternative configuration
    protected Cache<String, String> jcacheXCache;
    protected Cache<String, String> jcacheXBatchCache;
    protected com.github.benmanes.caffeine.cache.Cache<String, String> caffeineCache;
    protected ConcurrentHashMap<String, String> concurrentMap;

    @Setup(Level.Trial)
    public void setupTrial() {
        // Generate test data
        keys = new String[OPERATIONS_COUNT];
        values = new String[OPERATIONS_COUNT];

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            keys[i] = "key" + i;
            values[i] = "value" + i;
        }

        // Setup cache implementations
        setupJCacheX();
        setupJCacheXBatch();
        setupCaffeine();
        setupConcurrentMap();
    }

    private void setupJCacheX() {
        try {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumSize((long) CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false)
                    .build();
            jcacheXCache = new DefaultCache<>(config);
            System.out.println("✓ JCacheX setup successful");
        } catch (Exception e) {
            System.err.println("✗ JCacheX setup failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void setupJCacheXBatch() {
        try {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumSize((long) CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(true) // Enable stats for comparison
                    .build();
            jcacheXBatchCache = new DefaultCache<>(config);
            System.out.println("✓ JCacheX Alt setup successful");
        } catch (Exception e) {
            System.err.println("✗ JCacheX Alt setup failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void setupCaffeine() {
        try {
            caffeineCache = Caffeine.newBuilder()
                    .maximumSize(CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .build();
            System.out.println("✓ Caffeine setup successful");
        } catch (Exception e) {
            System.err.println("✗ Caffeine setup failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void setupConcurrentMap() {
        try {
            concurrentMap = new ConcurrentHashMap<>(CACHE_SIZE);
            System.out.println("✓ ConcurrentMap setup successful");
        } catch (Exception e) {
            System.err.println("✗ ConcurrentMap setup failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        // Pre-populate caches for GET operations
        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            String key = keys[i];
            String value = values[i];

            jcacheXCache.put(key, value);
            jcacheXBatchCache.put(key, value);
            caffeineCache.put(key, value);
            concurrentMap.put(key, value);
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        try {
            if (jcacheXCache != null) {
                jcacheXCache.clear();
            }
            if (jcacheXBatchCache != null) {
                jcacheXBatchCache.clear();
            }
            if (caffeineCache != null) {
                caffeineCache.invalidateAll();
            }
            if (concurrentMap != null) {
                concurrentMap.clear();
            }
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }

    // Quick GET benchmarks
    @Benchmark
    public String jcacheXGet() {
        return jcacheXCache.get(keys[0]);
    }

    @Benchmark
    public String jcacheXBatchGet() {
        return jcacheXBatchCache.get(keys[0]);
    }

    @Benchmark
    public String caffeineGet() {
        return caffeineCache.getIfPresent(keys[0]);
    }

    @Benchmark
    public String concurrentMapGet() {
        return concurrentMap.get(keys[0]);
    }

    // Quick PUT benchmarks
    @Benchmark
    public void jcacheXPut() {
        jcacheXCache.put("testKey", "testValue");
    }

    @Benchmark
    public void jcacheXBatchPut() {
        jcacheXBatchCache.put("testKey", "testValue");
    }

    @Benchmark
    public void caffeinePut() {
        caffeineCache.put("testKey", "testValue");
    }

    @Benchmark
    public void concurrentMapPut() {
        concurrentMap.put("testKey", "testValue");
    }
}
