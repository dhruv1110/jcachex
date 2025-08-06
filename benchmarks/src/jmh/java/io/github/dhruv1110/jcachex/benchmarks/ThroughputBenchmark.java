package io.github.dhruv1110.jcachex.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark for measuring throughput (operations per second) under sustained
 * load.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class ThroughputBenchmark extends BaseBenchmark {

    @Setup(Level.Iteration)
    public void setupIteration() {
        // Pre-populate caches with WARMUP_SET_SIZE entries (realistic production
        // scenario)
        // Working set is larger than cache capacity, forcing evictions during
        // benchmarks
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

    @Benchmark
    @Threads(1)
    public String jcacheXDefaultGetThroughput(ThreadState state) {
        return jcacheXDefault.get(getRandomKey(state.randomIndex()));
    }

    // ===============================
    // Single-threaded throughput
    // ===============================

    @Benchmark
    @Threads(1)
    public void jcacheXDefaultPutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXDefault.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    // ===============================
    // JCacheX Other Profiles - Single-threaded throughput
    // ===============================

    @Benchmark
    @Threads(1)
    public String jcacheXReadHeavyGetThroughput(ThreadState state) {
        return jcacheXReadHeavy.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void jcacheXReadHeavyPutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXReadHeavy.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String jcacheXWriteHeavyGetThroughput(ThreadState state) {
        return jcacheXWriteHeavy.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void jcacheXWriteHeavyPutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXWriteHeavy.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String jcacheXMemoryEfficientGetThroughput(ThreadState state) {
        return jcacheXMemoryEfficient.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void jcacheXMemoryEfficientPutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXMemoryEfficient.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String jcacheXHighPerformanceGetThroughput(ThreadState state) {
        return jcacheXHighPerformance.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void jcacheXHighPerformancePutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXHighPerformance.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String jcacheXSessionCacheGetThroughput(ThreadState state) {
        return jcacheXSessionCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void jcacheXSessionCachePutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXSessionCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String jcacheXApiCacheGetThroughput(ThreadState state) {
        return jcacheXApiCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void jcacheXApiCachePutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXApiCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String jcacheXComputeCacheGetThroughput(ThreadState state) {
        return jcacheXComputeCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void jcacheXComputeCachePutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXComputeCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String jcacheXMlOptimizedGetThroughput(ThreadState state) {
        return jcacheXMlOptimized.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void jcacheXMlOptimizedPutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXMlOptimized.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String jcacheXZeroCopyGetThroughput(ThreadState state) {
        return jcacheXZeroCopy.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void jcacheXZeroCopyPutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXZeroCopy.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String jcacheXHardwareOptimizedGetThroughput(ThreadState state) {
        return jcacheXHardwareOptimized.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void jcacheXHardwareOptimizedPutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXHardwareOptimized.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String caffeineGetThroughput(ThreadState state) {
        return caffeineCache.getIfPresent(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void caffeinePutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        caffeineCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String cache2kGetThroughput(ThreadState state) {
        return cache2kCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void cache2kPutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        cache2kCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String ehcacheGetThroughput(ThreadState state) {
        return ehcacheCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void ehcachePutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        ehcacheCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public String concurrentMapGetThroughput(ThreadState state) {
        return concurrentMap.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void concurrentMapPutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        concurrentMap.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String jcacheXDefaultGetThroughput4T(ThreadState state) {
        return jcacheXDefault.get(getRandomKey(state.randomIndex()));
    }

    // ===============================
    // Multi-threaded throughput (4 threads)
    // ===============================

    @Benchmark
    @Threads(4)
    public void jcacheXDefaultPutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXDefault.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    // ===============================
    // JCacheX Other Profiles - Multi-threaded throughput (4 threads)
    // ===============================

    @Benchmark
    @Threads(4)
    public String jcacheXReadHeavyGetThroughput4T(ThreadState state) {
        return jcacheXReadHeavy.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void jcacheXReadHeavyPutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXReadHeavy.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String jcacheXWriteHeavyGetThroughput4T(ThreadState state) {
        return jcacheXWriteHeavy.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void jcacheXWriteHeavyPutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXWriteHeavy.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String jcacheXMemoryEfficientGetThroughput4T(ThreadState state) {
        return jcacheXMemoryEfficient.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void jcacheXMemoryEfficientPutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXMemoryEfficient.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String jcacheXHighPerformanceGetThroughput4T(ThreadState state) {
        return jcacheXHighPerformance.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void jcacheXHighPerformancePutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXHighPerformance.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String jcacheXSessionCacheGetThroughput4T(ThreadState state) {
        return jcacheXSessionCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void jcacheXSessionCachePutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXSessionCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String jcacheXApiCacheGetThroughput4T(ThreadState state) {
        return jcacheXApiCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void jcacheXApiCachePutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXApiCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String jcacheXComputeCacheGetThroughput4T(ThreadState state) {
        return jcacheXComputeCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void jcacheXComputeCachePutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXComputeCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String jcacheXMlOptimizedGetThroughput4T(ThreadState state) {
        return jcacheXMlOptimized.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void jcacheXMlOptimizedPutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXMlOptimized.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String jcacheXZeroCopyGetThroughput4T(ThreadState state) {
        return jcacheXZeroCopy.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void jcacheXZeroCopyPutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXZeroCopy.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String jcacheXHardwareOptimizedGetThroughput4T(ThreadState state) {
        return jcacheXHardwareOptimized.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void jcacheXHardwareOptimizedPutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXHardwareOptimized.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String caffeineGetThroughput4T(ThreadState state) {
        return caffeineCache.getIfPresent(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void caffeinePutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        caffeineCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String cache2kGetThroughput4T(ThreadState state) {
        return cache2kCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void cache2kPutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        cache2kCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String ehcacheGetThroughput4T(ThreadState state) {
        return ehcacheCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void ehcachePutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        ehcacheCache.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(4)
    public String concurrentMapGetThroughput4T(ThreadState state) {
        return concurrentMap.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void concurrentMapPutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        concurrentMap.put(getRandomKey(idx), getRandomValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    @Threads(1)
    public void jcacheXMixedThroughput(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) { // 80% reads
            String value = jcacheXDefault.get(getRandomKey(idx));
            bh.consume(value);
        } else { // 20% writes
            jcacheXDefault.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

    // ===============================
    // Realistic mixed workload throughput
    // ===============================

    @Benchmark
    @Threads(1)
    public void caffeineMixedThroughput(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) {
            String value = caffeineCache.getIfPresent(getRandomKey(idx));
            bh.consume(value);
        } else {
            caffeineCache.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

    @Benchmark
    @Threads(1)
    public void cache2kMixedThroughput(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) {
            String value = cache2kCache.get(getRandomKey(idx));
            bh.consume(value);
        } else {
            cache2kCache.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

    @Benchmark
    @Threads(1)
    public void ehcacheMixedThroughput(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) {
            String value = ehcacheCache.get(getRandomKey(idx));
            bh.consume(value);
        } else {
            ehcacheCache.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

    @Benchmark
    @Threads(1)
    public void concurrentMapMixedThroughput(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) {
            String value = concurrentMap.get(getRandomKey(idx));
            bh.consume(value);
        } else {
            concurrentMap.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

    @Benchmark
    @Threads(4)
    public void jcacheXMixedThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) {
            String value = jcacheXDefault.get(getRandomKey(idx));
            bh.consume(value);
        } else {
            jcacheXDefault.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

    // ===============================
    // Multi-threaded mixed workload
    // ===============================

    @Benchmark
    @Threads(4)
    public void caffeineMixedThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) {
            String value = caffeineCache.getIfPresent(getRandomKey(idx));
            bh.consume(value);
        } else {
            caffeineCache.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

    @Benchmark
    @Threads(4)
    public void cache2kMixedThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) {
            String value = cache2kCache.get(getRandomKey(idx));
            bh.consume(value);
        } else {
            cache2kCache.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

    @Benchmark
    @Threads(4)
    public void ehcacheMixedThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) {
            String value = ehcacheCache.get(getRandomKey(idx));
            bh.consume(value);
        } else {
            ehcacheCache.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

    @Benchmark
    @Threads(4)
    public void concurrentMapMixedThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) {
            String value = concurrentMap.get(getRandomKey(idx));
            bh.consume(value);
        } else {
            concurrentMap.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

    @State(Scope.Thread)
    public static class ThreadState {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int counter = 0;

        public int nextIndex() {
            return counter++ % OPERATIONS_COUNT;
        }

        public int randomIndex() {
            return random.nextInt(OPERATIONS_COUNT);
        }
    }
}
