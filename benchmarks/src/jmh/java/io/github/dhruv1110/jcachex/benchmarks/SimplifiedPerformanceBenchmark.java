package io.github.dhruv1110.jcachex.benchmarks;

import io.github.dhruv1110.jcachex.*;

import io.github.dhruv1110.jcachex.impl.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Comprehensive performance benchmark for all JCacheX cache implementations.
 *
 * This benchmark tests all available cache implementations against Caffeine
 * baseline
 * to validate performance improvements and identify the best cache for
 * different use cases.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class SimplifiedPerformanceBenchmark {

    // JCacheX Core implementations
    private Cache<String, String> defaultCache;
    private Cache<String, String> optimizedCache;

    // JCacheX Advanced implementations
    private Cache<String, String> jitOptimizedCache;
    private Cache<String, String> allocationOptimizedCache;
    private Cache<String, String> cacheLocalityOptimizedCache;
    private Cache<String, String> zeroCopyOptimizedCache;
    private Cache<String, String> profiledOptimizedCache;

    // JCacheX Specialized implementations
    private Cache<String, String> readOnlyOptimizedCache;
    private Cache<String, String> writeHeavyOptimizedCache;
    private Cache<String, String> jvmOptimizedCache;
    private Cache<String, String> hardwareOptimizedCache;
    private Cache<String, String> mlOptimizedCache;

    // Baseline comparison
    private LoadingCache<String, String> caffeineCache;

    // Test data
    private String[] keys;
    private String[] values;
    private String[] hotKeys;
    private String[] coldKeys;

    // Test parameters
    private static final int CACHE_SIZE = 10_000;
    private static final int KEY_COUNT = 1_000;
    private static final int HOT_KEY_COUNT = 100;
    private static final int COLD_KEY_COUNT = 900;

    @Setup
    public void setup() {
        // Initialize test data
        setupTestData();

        // Initialize cache implementations
        setupCacheImplementations();

        // Populate caches with initial data
        populateCaches();
    }

    private void setupTestData() {
        keys = new String[KEY_COUNT];
        values = new String[KEY_COUNT];
        hotKeys = new String[HOT_KEY_COUNT];
        coldKeys = new String[COLD_KEY_COUNT];

        // Generate test keys and values
        for (int i = 0; i < KEY_COUNT; i++) {
            keys[i] = "key_" + i;
            values[i] = "value_" + i + "_" + generateRandomString(50);
        }

        // Separate hot and cold keys (80-20 distribution)
        System.arraycopy(keys, 0, hotKeys, 0, HOT_KEY_COUNT);
        System.arraycopy(keys, HOT_KEY_COUNT, coldKeys, 0, COLD_KEY_COUNT);
    }

    private void setupCacheImplementations() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumSize((long) CACHE_SIZE)
                .recordStats(true)
                .build();

        // Initialize JCacheX implementations
        defaultCache = new DefaultCache<>(config);
        optimizedCache = new OptimizedCache<>(config);

        // Initialize advanced implementations (with error handling)
        jitOptimizedCache = createCacheWithFallback(() -> new JITOptimizedCache<>(config), config);
        allocationOptimizedCache = createCacheWithFallback(() -> new AllocationOptimizedCache<>(config), config);
        cacheLocalityOptimizedCache = createCacheWithFallback(() -> new CacheLocalityOptimizedCache<>(config), config);
        zeroCopyOptimizedCache = createCacheWithFallback(() -> new ZeroCopyOptimizedCache<>(config), config);
        profiledOptimizedCache = createCacheWithFallback(() -> new ProfiledOptimizedCache<>(config), config);

        // Initialize specialized implementations
        readOnlyOptimizedCache = createCacheWithFallback(() -> new ReadOnlyOptimizedCache<>(config), config);
        writeHeavyOptimizedCache = createCacheWithFallback(() -> new WriteHeavyOptimizedCache<>(config), config);
        jvmOptimizedCache = createCacheWithFallback(() -> new JVMOptimizedCache<>(config), config);
        hardwareOptimizedCache = createCacheWithFallback(() -> new HardwareOptimizedCache<>(config), config);
        mlOptimizedCache = createCacheWithFallback(() -> new MLOptimizedCache<>(config), config);

        // Initialize Caffeine cache for comparison
        caffeineCache = Caffeine.newBuilder()
                .maximumSize(CACHE_SIZE)
                .recordStats()
                .build(key -> "caffeine_" + key);
    }

    private Cache<String, String> createCacheWithFallback(
            java.util.function.Supplier<Cache<String, String>> cacheSupplier, CacheConfig<String, String> config) {
        try {
            return cacheSupplier.get();
        } catch (Exception e) {
            System.err
                    .println("Failed to create cache implementation, falling back to DefaultCache: " + e.getMessage());
            return new DefaultCache<>(config);
        }
    }

    private void populateCaches() {
        // Populate all caches with initial data
        Cache<String, String>[] caches = new Cache[] {
                defaultCache, optimizedCache, jitOptimizedCache, allocationOptimizedCache,
                cacheLocalityOptimizedCache, zeroCopyOptimizedCache, profiledOptimizedCache,
                readOnlyOptimizedCache, writeHeavyOptimizedCache, jvmOptimizedCache,
                hardwareOptimizedCache, mlOptimizedCache
        };

        for (Cache<String, String> cache : caches) {
            for (int i = 0; i < KEY_COUNT; i++) {
                try {
                    cache.put(keys[i], values[i]);
                } catch (Exception e) {
                    System.err.println("Failed to populate cache: " + e.getMessage());
                }
            }
        }

        // Populate Caffeine cache
        for (int i = 0; i < KEY_COUNT; i++) {
            caffeineCache.put(keys[i], values[i]);
        }
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + ThreadLocalRandom.current().nextInt(26)));
        }
        return sb.toString();
    }

    // =========================
    // GET Operation Benchmarks (Hot Keys)
    // =========================

    @Benchmark
    public String caffeine_get_hot(Blackhole bh) {
        String key = hotKeys[ThreadLocalRandom.current().nextInt(HOT_KEY_COUNT)];
        String value = caffeineCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String default_get_hot(Blackhole bh) {
        String key = hotKeys[ThreadLocalRandom.current().nextInt(HOT_KEY_COUNT)];
        String value = defaultCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String optimized_get_hot(Blackhole bh) {
        String key = hotKeys[ThreadLocalRandom.current().nextInt(HOT_KEY_COUNT)];
        String value = optimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String jit_get_hot(Blackhole bh) {
        String key = hotKeys[ThreadLocalRandom.current().nextInt(HOT_KEY_COUNT)];
        String value = jitOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String allocation_get_hot(Blackhole bh) {
        String key = hotKeys[ThreadLocalRandom.current().nextInt(HOT_KEY_COUNT)];
        String value = allocationOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String locality_get_hot(Blackhole bh) {
        String key = hotKeys[ThreadLocalRandom.current().nextInt(HOT_KEY_COUNT)];
        String value = cacheLocalityOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String zerocopy_get_hot(Blackhole bh) {
        String key = hotKeys[ThreadLocalRandom.current().nextInt(HOT_KEY_COUNT)];
        String value = zeroCopyOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String readonly_get_hot(Blackhole bh) {
        String key = hotKeys[ThreadLocalRandom.current().nextInt(HOT_KEY_COUNT)];
        String value = readOnlyOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String jvm_get_hot(Blackhole bh) {
        String key = hotKeys[ThreadLocalRandom.current().nextInt(HOT_KEY_COUNT)];
        String value = jvmOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String hardware_get_hot(Blackhole bh) {
        String key = hotKeys[ThreadLocalRandom.current().nextInt(HOT_KEY_COUNT)];
        String value = hardwareOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String ml_get_hot(Blackhole bh) {
        String key = hotKeys[ThreadLocalRandom.current().nextInt(HOT_KEY_COUNT)];
        String value = mlOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    // =========================
    // GET Operation Benchmarks (Cold Keys)
    // =========================

    @Benchmark
    public String caffeine_get_cold(Blackhole bh) {
        String key = coldKeys[ThreadLocalRandom.current().nextInt(COLD_KEY_COUNT)];
        String value = caffeineCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String default_get_cold(Blackhole bh) {
        String key = coldKeys[ThreadLocalRandom.current().nextInt(COLD_KEY_COUNT)];
        String value = defaultCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String optimized_get_cold(Blackhole bh) {
        String key = coldKeys[ThreadLocalRandom.current().nextInt(COLD_KEY_COUNT)];
        String value = optimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String jit_get_cold(Blackhole bh) {
        String key = coldKeys[ThreadLocalRandom.current().nextInt(COLD_KEY_COUNT)];
        String value = jitOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String allocation_get_cold(Blackhole bh) {
        String key = coldKeys[ThreadLocalRandom.current().nextInt(COLD_KEY_COUNT)];
        String value = allocationOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String locality_get_cold(Blackhole bh) {
        String key = coldKeys[ThreadLocalRandom.current().nextInt(COLD_KEY_COUNT)];
        String value = cacheLocalityOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String zerocopy_get_cold(Blackhole bh) {
        String key = coldKeys[ThreadLocalRandom.current().nextInt(COLD_KEY_COUNT)];
        String value = zeroCopyOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String readonly_get_cold(Blackhole bh) {
        String key = coldKeys[ThreadLocalRandom.current().nextInt(COLD_KEY_COUNT)];
        String value = readOnlyOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String jvm_get_cold(Blackhole bh) {
        String key = coldKeys[ThreadLocalRandom.current().nextInt(COLD_KEY_COUNT)];
        String value = jvmOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String hardware_get_cold(Blackhole bh) {
        String key = coldKeys[ThreadLocalRandom.current().nextInt(COLD_KEY_COUNT)];
        String value = hardwareOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public String ml_get_cold(Blackhole bh) {
        String key = coldKeys[ThreadLocalRandom.current().nextInt(COLD_KEY_COUNT)];
        String value = mlOptimizedCache.get(key);
        bh.consume(value);
        return value;
    }

    // =========================
    // PUT Operation Benchmarks
    // =========================

    @Benchmark
    public void caffeine_put(Blackhole bh) {
        String key = "put_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String value = "put_value_" + ThreadLocalRandom.current().nextInt(1000);
        caffeineCache.put(key, value);
        bh.consume(key);
        bh.consume(value);
    }

    @Benchmark
    public void default_put(Blackhole bh) {
        String key = "put_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String value = "put_value_" + ThreadLocalRandom.current().nextInt(1000);
        defaultCache.put(key, value);
        bh.consume(key);
        bh.consume(value);
    }

    @Benchmark
    public void optimized_put(Blackhole bh) {
        String key = "put_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String value = "put_value_" + ThreadLocalRandom.current().nextInt(1000);
        optimizedCache.put(key, value);
        bh.consume(key);
        bh.consume(value);
    }

    @Benchmark
    public void jit_put(Blackhole bh) {
        String key = "put_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String value = "put_value_" + ThreadLocalRandom.current().nextInt(1000);
        jitOptimizedCache.put(key, value);
        bh.consume(key);
        bh.consume(value);
    }

    @Benchmark
    public void allocation_put(Blackhole bh) {
        String key = "put_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String value = "put_value_" + ThreadLocalRandom.current().nextInt(1000);
        allocationOptimizedCache.put(key, value);
        bh.consume(key);
        bh.consume(value);
    }

    @Benchmark
    public void locality_put(Blackhole bh) {
        String key = "put_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String value = "put_value_" + ThreadLocalRandom.current().nextInt(1000);
        cacheLocalityOptimizedCache.put(key, value);
        bh.consume(key);
        bh.consume(value);
    }

    @Benchmark
    public void zerocopy_put(Blackhole bh) {
        String key = "put_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String value = "put_value_" + ThreadLocalRandom.current().nextInt(1000);
        zeroCopyOptimizedCache.put(key, value);
        bh.consume(key);
        bh.consume(value);
    }

    @Benchmark
    public void writeheavy_put(Blackhole bh) {
        String key = "put_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String value = "put_value_" + ThreadLocalRandom.current().nextInt(1000);
        writeHeavyOptimizedCache.put(key, value);
        bh.consume(key);
        bh.consume(value);
    }

    @Benchmark
    public void jvm_put(Blackhole bh) {
        String key = "put_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String value = "put_value_" + ThreadLocalRandom.current().nextInt(1000);
        jvmOptimizedCache.put(key, value);
        bh.consume(key);
        bh.consume(value);
    }

    @Benchmark
    public void hardware_put(Blackhole bh) {
        String key = "put_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String value = "put_value_" + ThreadLocalRandom.current().nextInt(1000);
        hardwareOptimizedCache.put(key, value);
        bh.consume(key);
        bh.consume(value);
    }

    @Benchmark
    public void ml_put(Blackhole bh) {
        String key = "put_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
        String value = "put_value_" + ThreadLocalRandom.current().nextInt(1000);
        mlOptimizedCache.put(key, value);
        bh.consume(key);
        bh.consume(value);
    }

    // =========================
    // Mixed Workload Benchmarks
    // =========================

    @Benchmark
    public void caffeine_mixed_workload(Blackhole bh) {
        if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
            // 80% reads
            String key = keys[ThreadLocalRandom.current().nextInt(KEY_COUNT)];
            String value = caffeineCache.get(key);
            bh.consume(value);
        } else {
            // 20% writes
            String key = "mixed_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
            String value = "mixed_value_" + ThreadLocalRandom.current().nextInt(1000);
            caffeineCache.put(key, value);
            bh.consume(key);
            bh.consume(value);
        }
    }

    @Benchmark
    public void default_mixed_workload(Blackhole bh) {
        if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
            // 80% reads
            String key = keys[ThreadLocalRandom.current().nextInt(KEY_COUNT)];
            String value = defaultCache.get(key);
            bh.consume(value);
        } else {
            // 20% writes
            String key = "mixed_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
            String value = "mixed_value_" + ThreadLocalRandom.current().nextInt(1000);
            defaultCache.put(key, value);
            bh.consume(key);
            bh.consume(value);
        }
    }

    @Benchmark
    public void optimized_mixed_workload(Blackhole bh) {
        if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
            // 80% reads
            String key = keys[ThreadLocalRandom.current().nextInt(KEY_COUNT)];
            String value = optimizedCache.get(key);
            bh.consume(value);
        } else {
            // 20% writes
            String key = "mixed_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
            String value = "mixed_value_" + ThreadLocalRandom.current().nextInt(1000);
            optimizedCache.put(key, value);
            bh.consume(key);
            bh.consume(value);
        }
    }

    @Benchmark
    public void jit_mixed_workload(Blackhole bh) {
        if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
            String key = keys[ThreadLocalRandom.current().nextInt(KEY_COUNT)];
            String value = jitOptimizedCache.get(key);
            bh.consume(value);
        } else {
            String key = "mixed_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
            String value = "mixed_value_" + ThreadLocalRandom.current().nextInt(1000);
            jitOptimizedCache.put(key, value);
            bh.consume(key);
            bh.consume(value);
        }
    }

    @Benchmark
    public void allocation_mixed_workload(Blackhole bh) {
        if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
            String key = keys[ThreadLocalRandom.current().nextInt(KEY_COUNT)];
            String value = allocationOptimizedCache.get(key);
            bh.consume(value);
        } else {
            String key = "mixed_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
            String value = "mixed_value_" + ThreadLocalRandom.current().nextInt(1000);
            allocationOptimizedCache.put(key, value);
            bh.consume(key);
            bh.consume(value);
        }
    }

    @Benchmark
    public void locality_mixed_workload(Blackhole bh) {
        if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
            String key = keys[ThreadLocalRandom.current().nextInt(KEY_COUNT)];
            String value = cacheLocalityOptimizedCache.get(key);
            bh.consume(value);
        } else {
            String key = "mixed_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
            String value = "mixed_value_" + ThreadLocalRandom.current().nextInt(1000);
            cacheLocalityOptimizedCache.put(key, value);
            bh.consume(key);
            bh.consume(value);
        }
    }

    @Benchmark
    public void zerocopy_mixed_workload(Blackhole bh) {
        if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
            String key = keys[ThreadLocalRandom.current().nextInt(KEY_COUNT)];
            String value = zeroCopyOptimizedCache.get(key);
            bh.consume(value);
        } else {
            String key = "mixed_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
            String value = "mixed_value_" + ThreadLocalRandom.current().nextInt(1000);
            zeroCopyOptimizedCache.put(key, value);
            bh.consume(key);
            bh.consume(value);
        }
    }

    @Benchmark
    public void jvm_mixed_workload(Blackhole bh) {
        if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
            String key = keys[ThreadLocalRandom.current().nextInt(KEY_COUNT)];
            String value = jvmOptimizedCache.get(key);
            bh.consume(value);
        } else {
            String key = "mixed_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
            String value = "mixed_value_" + ThreadLocalRandom.current().nextInt(1000);
            jvmOptimizedCache.put(key, value);
            bh.consume(key);
            bh.consume(value);
        }
    }

    @Benchmark
    public void hardware_mixed_workload(Blackhole bh) {
        if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
            String key = keys[ThreadLocalRandom.current().nextInt(KEY_COUNT)];
            String value = hardwareOptimizedCache.get(key);
            bh.consume(value);
        } else {
            String key = "mixed_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
            String value = "mixed_value_" + ThreadLocalRandom.current().nextInt(1000);
            hardwareOptimizedCache.put(key, value);
            bh.consume(key);
            bh.consume(value);
        }
    }

    @Benchmark
    public void ml_mixed_workload(Blackhole bh) {
        if (ThreadLocalRandom.current().nextFloat() < 0.8f) {
            String key = keys[ThreadLocalRandom.current().nextInt(KEY_COUNT)];
            String value = mlOptimizedCache.get(key);
            bh.consume(value);
        } else {
            String key = "mixed_key_" + ThreadLocalRandom.current().nextInt(KEY_COUNT);
            String value = "mixed_value_" + ThreadLocalRandom.current().nextInt(1000);
            mlOptimizedCache.put(key, value);
            bh.consume(key);
            bh.consume(value);
        }
    }

    @TearDown
    public void tearDown() {
        printPerformanceStatistics();
        validatePerformanceTargets();
        cleanupResources();
    }

    private void printPerformanceStatistics() {
        System.out.println("\n=== JCacheX Performance Validation Results ===");
        System.out.println("Cache Size: " + CACHE_SIZE);
        System.out.println("Key Count: " + KEY_COUNT);
        System.out.println("Hot Keys: " + HOT_KEY_COUNT);
        System.out.println("Cold Keys: " + COLD_KEY_COUNT);
        System.out.println("\nCache Statistics:");

        // Print stats for each cache if they implement stats()
        try {
            printCacheStats("DefaultCache", defaultCache.stats());
            printCacheStats("OptimizedCache", optimizedCache.stats());
            printCacheStats("JITOptimizedCache", jitOptimizedCache.stats());
            printCacheStats("AllocationOptimizedCache", allocationOptimizedCache.stats());
            printCacheStats("LocalityOptimizedCache", cacheLocalityOptimizedCache.stats());
            printCacheStats("ZeroCopyOptimizedCache", zeroCopyOptimizedCache.stats());
            printCacheStats("ReadOnlyOptimizedCache", readOnlyOptimizedCache.stats());
            printCacheStats("WriteHeavyOptimizedCache", writeHeavyOptimizedCache.stats());
            printCacheStats("JVMOptimizedCache", jvmOptimizedCache.stats());
            printCacheStats("HardwareOptimizedCache", hardwareOptimizedCache.stats());
            printCacheStats("MLOptimizedCache", mlOptimizedCache.stats());
        } catch (Exception e) {
            System.err.println("Error printing cache statistics: " + e.getMessage());
        }
    }

    private void printCacheStats(String name, CacheStats stats) {
        if (stats != null) {
            System.out.println("  " + name + ": " +
                    "hits=" + stats.hitCount() +
                    ", misses=" + stats.missCount() +
                    ", hitRate=" + String.format("%.2f%%", stats.hitRate() * 100));
        }
    }

    private void validatePerformanceTargets() {
        System.out.println("\n=== Performance Target Validation ===");
        System.out.println("Target: GET operations under 20ns");
        System.out.println("Target: PUT operations under 60ns");
        System.out.println("Target: Mixed workload competitive with Caffeine");
        System.out.println("All detailed performance metrics available in benchmark results.");
    }

    private void cleanupResources() {
        try {
            if (defaultCache instanceof AutoCloseable) {
                ((AutoCloseable) defaultCache).close();
            }
            if (optimizedCache instanceof AutoCloseable) {
                ((AutoCloseable) optimizedCache).close();
            }
            // Add cleanup for other caches if they implement AutoCloseable
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SimplifiedPerformanceBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
