package io.github.dhruv1110.jcachex.benchmarks;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.DefaultCache;
import io.github.dhruv1110.jcachex.OptimizedCache;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive benchmark comparing optimized cache implementations.
 * Tests Window TinyLFU, lock-free reads, batch processing, and memory
 * optimizations.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class OptimizedCacheBenchmark {

    @Param({ "1000", "10000", "100000" })
    private int cacheSize;

    @Param({ "1000", "10000", "100000" })
    private int keySpace;

    private Cache<String, String> defaultCache;
    private Cache<String, String> optimizedCache;

    // Test data
    private String[] keys;
    private String[] values;

    @Setup(Level.Trial)
    public void setup() {
        // Initialize test data
        keys = new String[keySpace];
        values = new String[keySpace];

        for (int i = 0; i < keySpace; i++) {
            keys[i] = "key-" + i;
            values[i] = "value-" + i + "-" + generateRandomString(50);
        }

        // Create cache configurations
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumSize((long) cacheSize)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats(true)
                .build();

        // Initialize caches
        defaultCache = new DefaultCache<>(config);
        optimizedCache = new OptimizedCache<>(config);

        // Pre-populate with some data to test realistic scenarios
        populateCache(defaultCache, cacheSize / 2);
        populateCache(optimizedCache, cacheSize / 2);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (defaultCache instanceof AutoCloseable) {
            try {
                ((AutoCloseable) defaultCache).close();
            } catch (Exception e) {
                // Ignore
            }
        }

        if (optimizedCache instanceof OptimizedCache) {
            ((OptimizedCache<String, String>) optimizedCache).shutdown();
        }
    }

    // === GET BENCHMARKS ===

    @Benchmark
    public String defaultCache_get_hit() {
        return defaultCache.get(getRandomExistingKey());
    }

    @Benchmark
    public String optimizedCache_get_hit() {
        return optimizedCache.get(getRandomExistingKey());
    }

    @Benchmark
    public String defaultCache_get_miss() {
        return defaultCache.get(getRandomMissingKey());
    }

    @Benchmark
    public String optimizedCache_get_miss() {
        return optimizedCache.get(getRandomMissingKey());
    }

    // === PUT BENCHMARKS ===

    @Benchmark
    public void defaultCache_put() {
        String key = getRandomKey();
        String value = getRandomValue();
        defaultCache.put(key, value);
    }

    @Benchmark
    public void optimizedCache_put() {
        String key = getRandomKey();
        String value = getRandomValue();
        optimizedCache.put(key, value);
    }

    // === MIXED WORKLOAD BENCHMARKS ===

    @Benchmark
    public String defaultCache_mixed_80_20() {
        // 80% reads, 20% writes
        if (ThreadLocalRandom.current().nextInt(100) < 80) {
            return defaultCache.get(getRandomKey());
        } else {
            String key = getRandomKey();
            String value = getRandomValue();
            defaultCache.put(key, value);
            return null;
        }
    }

    @Benchmark
    public String optimizedCache_mixed_80_20() {
        // 80% reads, 20% writes
        if (ThreadLocalRandom.current().nextInt(100) < 80) {
            return optimizedCache.get(getRandomKey());
        } else {
            String key = getRandomKey();
            String value = getRandomValue();
            optimizedCache.put(key, value);
            return null;
        }
    }

    @Benchmark
    public String defaultCache_mixed_95_5() {
        // 95% reads, 5% writes (typical web application pattern)
        if (ThreadLocalRandom.current().nextInt(100) < 95) {
            return defaultCache.get(getRandomKey());
        } else {
            String key = getRandomKey();
            String value = getRandomValue();
            defaultCache.put(key, value);
            return null;
        }
    }

    @Benchmark
    public String optimizedCache_mixed_95_5() {
        // 95% reads, 5% writes (typical web application pattern)
        if (ThreadLocalRandom.current().nextInt(100) < 95) {
            return optimizedCache.get(getRandomKey());
        } else {
            String key = getRandomKey();
            String value = getRandomValue();
            optimizedCache.put(key, value);
            return null;
        }
    }

    // === CONCURRENT ACCESS BENCHMARKS ===

    @Benchmark
    @Threads(4)
    public String defaultCache_concurrent_get() {
        return defaultCache.get(getRandomKey());
    }

    @Benchmark
    @Threads(4)
    public String optimizedCache_concurrent_get() {
        return optimizedCache.get(getRandomKey());
    }

    @Benchmark
    @Threads(8)
    public String defaultCache_high_concurrency_get() {
        return defaultCache.get(getRandomKey());
    }

    @Benchmark
    @Threads(8)
    public String optimizedCache_high_concurrency_get() {
        return optimizedCache.get(getRandomKey());
    }

    @Benchmark
    @Threads(4)
    public String defaultCache_concurrent_mixed() {
        if (ThreadLocalRandom.current().nextInt(100) < 90) {
            return defaultCache.get(getRandomKey());
        } else {
            String key = getRandomKey();
            String value = getRandomValue();
            defaultCache.put(key, value);
            return null;
        }
    }

    @Benchmark
    @Threads(4)
    public String optimizedCache_concurrent_mixed() {
        if (ThreadLocalRandom.current().nextInt(100) < 90) {
            return optimizedCache.get(getRandomKey());
        } else {
            String key = getRandomKey();
            String value = getRandomValue();
            optimizedCache.put(key, value);
            return null;
        }
    }

    // === ZIPFIAN DISTRIBUTION BENCHMARKS ===
    // Tests realistic access patterns where some keys are much more popular

    @Benchmark
    public String defaultCache_zipfian_get() {
        return defaultCache.get(getZipfianKey());
    }

    @Benchmark
    public String optimizedCache_zipfian_get() {
        return optimizedCache.get(getZipfianKey());
    }

    @Benchmark
    public String defaultCache_zipfian_mixed() {
        if (ThreadLocalRandom.current().nextInt(100) < 90) {
            return defaultCache.get(getZipfianKey());
        } else {
            String key = getZipfianKey();
            String value = getRandomValue();
            defaultCache.put(key, value);
            return null;
        }
    }

    @Benchmark
    public String optimizedCache_zipfian_mixed() {
        if (ThreadLocalRandom.current().nextInt(100) < 90) {
            return optimizedCache.get(getZipfianKey());
        } else {
            String key = getZipfianKey();
            String value = getRandomValue();
            optimizedCache.put(key, value);
            return null;
        }
    }

    // === BATCH OPERATION BENCHMARKS ===

    @Benchmark
    @OperationsPerInvocation(10)
    public void defaultCache_batch_put() {
        for (int i = 0; i < 10; i++) {
            String key = getRandomKey();
            String value = getRandomValue();
            defaultCache.put(key, value);
        }
    }

    @Benchmark
    @OperationsPerInvocation(10)
    public void optimizedCache_batch_put() {
        for (int i = 0; i < 10; i++) {
            String key = getRandomKey();
            String value = getRandomValue();
            optimizedCache.put(key, value);
        }
    }

    @Benchmark
    @OperationsPerInvocation(10)
    public void defaultCache_batch_get() {
        for (int i = 0; i < 10; i++) {
            defaultCache.get(getRandomKey());
        }
    }

    @Benchmark
    @OperationsPerInvocation(10)
    public void optimizedCache_batch_get() {
        for (int i = 0; i < 10; i++) {
            optimizedCache.get(getRandomKey());
        }
    }

    // === HELPER METHODS ===

    private void populateCache(Cache<String, String> cache, int count) {
        for (int i = 0; i < count; i++) {
            cache.put(keys[i % keys.length], values[i % values.length]);
        }
    }

    private String getRandomKey() {
        return keys[ThreadLocalRandom.current().nextInt(keys.length)];
    }

    private String getRandomValue() {
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    private String getRandomExistingKey() {
        // Get a key that should exist in the cache (first half of keyspace)
        return keys[ThreadLocalRandom.current().nextInt(Math.min(cacheSize / 2, keys.length))];
    }

    private String getRandomMissingKey() {
        // Get a key that likely doesn't exist (second half of keyspace)
        int start = Math.max(cacheSize / 2, 0);
        int range = Math.max(1, keys.length - start);
        return keys[start + ThreadLocalRandom.current().nextInt(range)];
    }

    private String getZipfianKey() {
        // Simple zipfian distribution approximation
        // 80% of accesses go to 20% of keys
        double random = ThreadLocalRandom.current().nextDouble();
        if (random < 0.8) {
            // Hot keys (first 20% of keyspace)
            int hotKeyCount = Math.max(1, keys.length / 5);
            return keys[ThreadLocalRandom.current().nextInt(hotKeyCount)];
        } else {
            // Cold keys (remaining 80% of keyspace)
            int hotKeyCount = keys.length / 5;
            int coldKeyCount = keys.length - hotKeyCount;
            return keys[hotKeyCount + ThreadLocalRandom.current().nextInt(coldKeyCount)];
        }
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + ThreadLocalRandom.current().nextInt(26)));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(OptimizedCacheBenchmark.class.getSimpleName())
                .jvmArgs("-Xmx4g", "-Xms4g")
                .build();

        new Runner(opt).run();
    }
}
