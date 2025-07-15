package io.github.dhruv1110.jcachex.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hardcore benchmark suite designed to stress test cache implementations
 * under extreme conditions that simulate real-world production scenarios.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(3)
public class HardcoreBenchmark extends BaseBenchmark {

    // HARDCORE CONFIGURATION - 10x more demanding than standard benchmarks
    private static final int HARDCORE_CACHE_SIZE = 100_000; // 100K cache capacity
    private static final int HARDCORE_OPERATIONS = 1_000_000; // 1M working set (10x cache size)
    private static final int HARDCORE_WARMUP_SIZE = 80_000; // 80% cache pre-population
    private static final int LARGE_OBJECT_SIZE = 10_000; // 10KB objects
    private static final int HOT_KEYS_COUNT = 100; // For zipfian distribution
    private static final double ZIPFIAN_SKEW = 0.99; // 99% access to 1% of keys

    // Large object arrays for memory pressure testing
    private String[] largeKeys;
    private LargeObject[] largeValues;
    private String[] hotKeys;
    private AtomicLong operationCounter = new AtomicLong();

    @Setup(Level.Trial)
    public void setupHardcore() {
        // Override parent setup with hardcore configuration
        setupLargeObjects();
        setupHotKeys();
        setupHardcoreCaches();
    }

    private void setupLargeObjects() {
        // Create large objects to test memory pressure
        largeKeys = new String[HARDCORE_OPERATIONS];
        largeValues = new LargeObject[HARDCORE_OPERATIONS];

        for (int i = 0; i < HARDCORE_OPERATIONS; i++) {
            largeKeys[i] = "large_key_" + i + "_" + System.nanoTime();
            largeValues[i] = new LargeObject(i, LARGE_OBJECT_SIZE);
        }
    }

    private void setupHotKeys() {
        // Create hot keys for zipfian distribution testing
        hotKeys = new String[HOT_KEYS_COUNT];
        for (int i = 0; i < HOT_KEYS_COUNT; i++) {
            hotKeys[i] = "hot_key_" + i;
        }
    }

    private void setupHardcoreCaches() {
        // Pre-populate caches with hardcore warmup data
        for (int i = 0; i < HARDCORE_WARMUP_SIZE; i++) {
            String key = largeKeys[i % largeKeys.length];
            LargeObject value = largeValues[i % largeValues.length];

            // Only populate JCacheX for hardcore tests
            if (jcacheXDefault != null) {
                jcacheXDefault.put(key, value.getData());
            }
            if (jcacheXHighPerformance != null) {
                jcacheXHighPerformance.put(key, value.getData());
            }
            if (jcacheXMemoryEfficient != null) {
                jcacheXMemoryEfficient.put(key, value.getData());
            }
            if (caffeineCache != null) {
                caffeineCache.put(key, value.getData());
            }
            if (concurrentMap != null) {
                concurrentMap.put(key, value.getData());
            }
        }
    }

    // ===================================
    // EXTREME CONCURRENCY TESTS (32+ threads)
    // ===================================

    @Benchmark
    @Threads(32)
    public String jcacheXDefaultExtremeContention(ThreadState state) {
        return jcacheXDefault.get(getZipfianKey(state));
    }

    @Benchmark
    @Threads(32)
    public String jcacheXHighPerformanceExtremeContention(ThreadState state) {
        return jcacheXHighPerformance.get(getZipfianKey(state));
    }

    @Benchmark
    @Threads(32)
    public String caffeineExtremeContention(ThreadState state) {
        return caffeineCache.getIfPresent(getZipfianKey(state));
    }

    @Benchmark
    @Threads(32)
    public String concurrentMapExtremeContention(ThreadState state) {
        return concurrentMap.get(getZipfianKey(state));
    }

    // ===================================
    // MEMORY PRESSURE TESTS (Large Objects)
    // ===================================

    @Benchmark
    @Threads(8)
    public void jcacheXDefaultMemoryPressure(ThreadState state, Blackhole bh) {
        int idx = state.nextLargeIndex();
        jcacheXDefault.put(largeKeys[idx], largeValues[idx].getData());
        bh.consume(idx);
    }

    @Benchmark
    @Threads(8)
    public void jcacheXMemoryEfficientMemoryPressure(ThreadState state, Blackhole bh) {
        int idx = state.nextLargeIndex();
        jcacheXMemoryEfficient.put(largeKeys[idx], largeValues[idx].getData());
        bh.consume(idx);
    }

    @Benchmark
    @Threads(8)
    public void caffeineMemoryPressure(ThreadState state, Blackhole bh) {
        int idx = state.nextLargeIndex();
        caffeineCache.put(largeKeys[idx], largeValues[idx].getData());
        bh.consume(idx);
    }

    // ===================================
    // ZIPFIAN DISTRIBUTION TESTS (80/20 rule)
    // ===================================

    @Benchmark
    @Threads(16)
    public String jcacheXDefaultZipfian(ThreadState state) {
        return jcacheXDefault.get(getZipfianKey(state));
    }

    @Benchmark
    @Threads(16)
    public String jcacheXReadHeavyZipfian(ThreadState state) {
        return jcacheXReadHeavy.get(getZipfianKey(state));
    }

    @Benchmark
    @Threads(16)
    public String caffeineZipfian(ThreadState state) {
        return caffeineCache.getIfPresent(getZipfianKey(state));
    }

    // ===================================
    // MIXED WORKLOAD TESTS (Complex Patterns)
    // ===================================

    @Benchmark
    @Group("mixedWorkload")
    @GroupThreads(12) // 75% readers
    public String jcacheXDefaultMixedRead(ThreadState state) {
        return jcacheXDefault.get(getZipfianKey(state));
    }

    @Benchmark
    @Group("mixedWorkload")
    @GroupThreads(3) // 18.75% writers
    public void jcacheXDefaultMixedWrite(ThreadState state, Blackhole bh) {
        int idx = state.nextLargeIndex();
        jcacheXDefault.put(largeKeys[idx], largeValues[idx].getData());
        bh.consume(idx);
    }

    @Benchmark
    @Group("mixedWorkload")
    @GroupThreads(1) // 6.25% removals
    public void jcacheXDefaultMixedRemove(ThreadState state, Blackhole bh) {
        jcacheXDefault.remove(getZipfianKey(state));
        bh.consume(state.counter);
    }

    @Benchmark
    @Group("mixedWorkloadCaffeine")
    @GroupThreads(12)
    public String caffeineMixedRead(ThreadState state) {
        return caffeineCache.getIfPresent(getZipfianKey(state));
    }

    @Benchmark
    @Group("mixedWorkloadCaffeine")
    @GroupThreads(3)
    public void caffeineMixedWrite(ThreadState state, Blackhole bh) {
        int idx = state.nextLargeIndex();
        caffeineCache.put(largeKeys[idx], largeValues[idx].getData());
        bh.consume(idx);
    }

    @Benchmark
    @Group("mixedWorkloadCaffeine")
    @GroupThreads(1)
    public void caffeineMixedRemove(ThreadState state, Blackhole bh) {
        caffeineCache.invalidate(getZipfianKey(state));
        bh.consume(state.counter);
    }

    // ===================================
    // EVICTION STRESS TESTS
    // ===================================

    @Benchmark
    @Threads(16)
    public void jcacheXDefaultEvictionStress(ThreadState state, Blackhole bh) {
        // Force constant eviction by accessing 10x cache size
        int idx = state.nextEvictionIndex();
        jcacheXDefault.put(largeKeys[idx], largeValues[idx].getData());
        bh.consume(idx);
    }

    @Benchmark
    @Threads(16)
    public void caffeineEvictionStress(ThreadState state, Blackhole bh) {
        int idx = state.nextEvictionIndex();
        caffeineCache.put(largeKeys[idx], largeValues[idx].getData());
        bh.consume(idx);
    }

    // ===================================
    // UTILITY METHODS
    // ===================================

    private String getZipfianKey(ThreadState state) {
        // 80% of accesses go to 20% of keys (zipfian distribution)
        if (state.random.nextDouble() < ZIPFIAN_SKEW) {
            return hotKeys[state.random.nextInt(HOT_KEYS_COUNT)];
        } else {
            return largeKeys[state.random.nextInt(largeKeys.length)];
        }
    }

    // ===================================
    // THREAD STATE
    // ===================================

    @State(Scope.Thread)
    public static class ThreadState {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int counter = 0;
        int largeCounter = 0;
        int evictionCounter = 0;

        public int nextLargeIndex() {
            return largeCounter++ % HARDCORE_OPERATIONS;
        }

        public int nextEvictionIndex() {
            // Access pattern that forces eviction (10x cache size)
            return evictionCounter++ % (HARDCORE_CACHE_SIZE * 10);
        }
    }

    // ===================================
    // LARGE OBJECT FOR MEMORY PRESSURE
    // ===================================

    private static class LargeObject {
        private final String data;
        private final int id;
        private final long timestamp;

        public LargeObject(int id, int size) {
            this.id = id;
            this.timestamp = System.nanoTime();

            // Create large string object
            StringBuilder sb = new StringBuilder(size);
            for (int i = 0; i < size; i++) {
                sb.append((char) ('A' + (i % 26)));
            }
            this.data = sb.toString();
        }

        public String getData() {
            return data + "_" + id + "_" + timestamp;
        }
    }
}
