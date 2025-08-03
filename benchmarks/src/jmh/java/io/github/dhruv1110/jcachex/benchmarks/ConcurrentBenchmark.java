package io.github.dhruv1110.jcachex.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark for concurrent cache operations under multi-threaded load.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(Threads.MAX) // Use all available threads
public class ConcurrentBenchmark extends BaseBenchmark {

    private static final String HOT_KEY = "hotkey";

    @Setup(Level.Iteration)
    public void setupIteration() {
        // Pre-populate caches with WARMUP_SET_SIZE for realistic concurrent scenarios
        // Cache capacity will be exceeded during concurrent operations, testing
        // eviction performance
        for (int i = 0; i < WARMUP_SET_SIZE; i++) {
            String key = getSequentialKey(i);
            String value = getSequentialValue(i);

            // Populate all JCacheX cache profiles
            jcacheXDefault.put(key, value);
            jcacheXReadHeavy.put(key, value);
            jcacheXWriteHeavy.put(key, value);
            jcacheXMemoryEfficient.put(key, value);
            jcacheXHighPerformance.put(key, value);
            jcacheXSessionCache.put(key, value);
            jcacheXApiCache.put(key, value);
            jcacheXComputeCache.put(key, value);
            jcacheXMlOptimized.put(key, value);
            jcacheXZeroCopy.put(key, value);
            jcacheXHardwareOptimized.put(key, value);
            caffeineCache.put(key, value);
            cache2kCache.put(key, value);
            ehcacheCache.put(key, value);
            if (jcacheCache != null) {
                jcacheCache.put(key, value);
            }
            concurrentMap.put(key, value);
        }
    }

    // ===============================
    // Concurrent READ-HEAVY workload (90% reads, 10% writes)
    // ===============================

    @Benchmark
    @Group("readHeavy")
    @GroupThreads(9) // 9 reader threads
    public String jcacheXReadHeavy(ThreadState state) {
        return jcacheXDefault.get(getRandomKey(state.getIndex()));
    }

    @Benchmark
    @Group("readHeavy")
    @GroupThreads(1) // 1 writer thread
    public void jcacheXWriteHeavy(ThreadState state, Blackhole bh) {
        int idx = state.getIndex();
        jcacheXDefault.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Group("readHeavyCaffeine")
    @GroupThreads(9)
    public String caffeineReadHeavy(ThreadState state) {
        return caffeineCache.getIfPresent(getRandomKey(state.getIndex()));
    }

    @Benchmark
    @Group("readHeavyCaffeine")
    @GroupThreads(1)
    public void caffeineWriteHeavy(ThreadState state, Blackhole bh) {
        int idx = state.getIndex();
        caffeineCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Group("readHeavyCache2k")
    @GroupThreads(9)
    public String cache2kReadHeavy(ThreadState state) {
        return cache2kCache.get(getRandomKey(state.getIndex()));
    }

    @Benchmark
    @Group("readHeavyCache2k")
    @GroupThreads(1)
    public void cache2kWriteHeavy(ThreadState state, Blackhole bh) {
        int idx = state.getIndex();
        cache2kCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Group("readHeavyEhcache")
    @GroupThreads(9)
    public String ehcacheReadHeavy(ThreadState state) {
        return ehcacheCache.get(getRandomKey(state.getIndex()));
    }

    @Benchmark
    @Group("readHeavyEhcache")
    @GroupThreads(1)
    public void ehcacheWriteHeavy(ThreadState state, Blackhole bh) {
        int idx = state.getIndex();
        ehcacheCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Group("readHeavyConcurrentMap")
    @GroupThreads(9)
    public String concurrentMapReadHeavy(ThreadState state) {
        return concurrentMap.get(getRandomKey(state.getIndex()));
    }

    @Benchmark
    @Group("readHeavyConcurrentMap")
    @GroupThreads(1)
    public void concurrentMapWriteHeavy(ThreadState state, Blackhole bh) {
        int idx = state.getIndex();
        concurrentMap.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    // ===============================
    // Concurrent WRITE-HEAVY workload (30% reads, 70% writes)
    // ===============================

    @Benchmark
    @Group("writeHeavy")
    @GroupThreads(3) // 3 reader threads
    public String jcacheXReadLight(ThreadState state) {
        return jcacheXDefault.get(getRandomKey(state.getIndex()));
    }

    @Benchmark
    @Group("writeHeavy")
    @GroupThreads(7) // 7 writer threads
    public void jcacheXWriteLight(ThreadState state, Blackhole bh) {
        int idx = state.getIndex();
        jcacheXDefault.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Group("writeHeavyCaffeine")
    @GroupThreads(3)
    public String caffeineReadLight(ThreadState state) {
        return caffeineCache.getIfPresent(getRandomKey(state.getIndex()));
    }

    @Benchmark
    @Group("writeHeavyCaffeine")
    @GroupThreads(7)
    public void caffeineWriteLight(ThreadState state, Blackhole bh) {
        int idx = state.getIndex();
        caffeineCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Group("writeHeavyCache2k")
    @GroupThreads(3)
    public String cache2kReadLight(ThreadState state) {
        return cache2kCache.get(getRandomKey(state.getIndex()));
    }

    @Benchmark
    @Group("writeHeavyCache2k")
    @GroupThreads(7)
    public void cache2kWriteLight(ThreadState state, Blackhole bh) {
        int idx = state.getIndex();
        cache2kCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Group("writeHeavyEhcache")
    @GroupThreads(3)
    public String ehcacheReadLight(ThreadState state) {
        return ehcacheCache.get(getRandomKey(state.getIndex()));
    }

    @Benchmark
    @Group("writeHeavyEhcache")
    @GroupThreads(7)
    public void ehcacheWriteLight(ThreadState state, Blackhole bh) {
        int idx = state.getIndex();
        ehcacheCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Group("writeHeavyConcurrentMap")
    @GroupThreads(3)
    public String concurrentMapReadLight(ThreadState state) {
        return concurrentMap.get(getRandomKey(state.getIndex()));
    }

    @Benchmark
    @Group("writeHeavyConcurrentMap")
    @GroupThreads(7)
    public void concurrentMapWriteLight(ThreadState state, Blackhole bh) {
        int idx = state.getIndex();
        concurrentMap.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    // ===============================
    // High contention scenarios (same key accessed by multiple threads)
    // ===============================

    @Benchmark
    public String jcacheXHighContention(ThreadState state) {
        // 80% chance to access the hot key, 20% random key
        String key = state.random.nextInt(100) < 80 ? HOT_KEY : getRandomKey(state.getIndex());
        return jcacheXDefault.get(key);
    }

    @Benchmark
    public String caffeineHighContention(ThreadState state) {
        String key = state.random.nextInt(100) < 80 ? HOT_KEY : getRandomKey(state.getIndex());
        return caffeineCache.getIfPresent(key);
    }

    @Benchmark
    public String cache2kHighContention(ThreadState state) {
        String key = state.random.nextInt(100) < 80 ? HOT_KEY : getRandomKey(state.getIndex());
        return cache2kCache.get(key);
    }

    @Benchmark
    public String ehcacheHighContention(ThreadState state) {
        String key = state.random.nextInt(100) < 80 ? HOT_KEY : getRandomKey(state.getIndex());
        return ehcacheCache.get(key);
    }

    @Benchmark
    public String concurrentMapHighContention(ThreadState state) {
        String key = state.random.nextInt(100) < 80 ? HOT_KEY : getRandomKey(state.getIndex());
        return concurrentMap.get(key);
    }

    @State(Scope.Thread)
    public static class ThreadState {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int baseIndex;

        @Setup(Level.Trial)
        public void setup() {
            // Each thread gets a different base index to reduce key collisions
            baseIndex = random.nextInt(1000) * 1000;
        }

        public int getIndex() {
            return baseIndex + random.nextInt(OPERATIONS_COUNT);
        }
    }
}
