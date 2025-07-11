package io.github.dhruv1110.jcachex.benchmarks;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.dhruv1110.jcachex.impl.DefaultCache;
import org.openjdk.jmh.annotations.*;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing JCacheX with nanoTime optimizations against other cache
 * implementations
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class OptimizedBenchmark {

    protected static final int CACHE_SIZE = 500; // Smaller for more eviction activity
    protected static final int OPERATIONS_COUNT = 100;

    // Test data
    protected String[] keys;
    protected String[] values;

    // Cache implementations
    protected Cache<String, String> jcacheXDefault;
    protected com.github.benmanes.caffeine.cache.Cache<String, String> caffeineCache;
    protected ConcurrentHashMap<String, String> concurrentMap;

    @Setup(Level.Trial)
    public void setupTrial() {
        System.out.println("Setting up OptimizedBenchmark...");

        keys = new String[OPERATIONS_COUNT];
        values = new String[OPERATIONS_COUNT];

        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            keys[i] = "key" + i;
            values[i] = "value" + i;
        }

        // Setup cache implementations
        setupJCacheXDefault();
        setupCaffeine();
        setupConcurrentMap();
    }

    private void setupJCacheXDefault() {
        try {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumSize((long) CACHE_SIZE)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(false) // Disable stats for pure performance
                    .build();
            jcacheXDefault = new DefaultCache<>(config);
            System.out.println("✓ JCacheX Default setup successful");
        } catch (Exception e) {
            System.err.println("✗ JCacheX Default setup failed: " + e.getMessage());
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

            jcacheXDefault.put(key, value);
            caffeineCache.put(key, value);
            concurrentMap.put(key, value);
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        try {
            if (jcacheXDefault != null)
                jcacheXDefault.clear();
            if (caffeineCache != null)
                caffeineCache.invalidateAll();
            if (concurrentMap != null)
                concurrentMap.clear();
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }

    // GET benchmarks - comparing cached access performance
    @Benchmark
    public String jcacheXDefaultGet() {
        return jcacheXDefault.get(keys[0]);
    }

    @Benchmark
    public String caffeineGet() {
        return caffeineCache.getIfPresent(keys[0]);
    }

    @Benchmark
    public String concurrentMapGet() {
        return concurrentMap.get(keys[0]);
    }

    // PUT benchmarks - testing write performance
    @Benchmark
    public void jcacheXDefaultPut() {
        jcacheXDefault.put("testKey", "testValue");
    }

    @Benchmark
    public void caffeinePut() {
        caffeineCache.put("testKey", "testValue");
    }

    @Benchmark
    public void concurrentMapPut() {
        concurrentMap.put("testKey", "testValue");
    }

    // Bulk operations - testing higher-throughput scenarios
    @Benchmark
    public void jcacheXDefaultBulkPut() {
        for (int i = 0; i < 10; i++) {
            jcacheXDefault.put("bulk" + i, "value" + i);
        }
    }

    @Benchmark
    public void caffeineBulkPut() {
        for (int i = 0; i < 10; i++) {
            caffeineCache.put("bulk" + i, "value" + i);
        }
    }
}
