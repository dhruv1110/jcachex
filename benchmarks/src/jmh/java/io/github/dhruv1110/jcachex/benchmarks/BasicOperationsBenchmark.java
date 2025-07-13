package io.github.dhruv1110.jcachex.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;
import java.time.Duration;

/**
 * Benchmark for basic cache operations: get, put, remove operations.
 * Tests all 12 JCacheX cache profiles against industry-leading implementations.
 */
public class BasicOperationsBenchmark extends BaseBenchmark {

    @State(Scope.Thread)
    public static class ThreadState {
        int index = 0;
        ThreadLocalRandom random = ThreadLocalRandom.current();

        public int nextIndex() {
            return index++ % OPERATIONS_COUNT;
        }

        public int randomIndex() {
            return random.nextInt(OPERATIONS_COUNT);
        }
    }

    // ===============================
    // PUT operations benchmarks - All 12 JCacheX Profiles
    // ===============================

    // Core Profiles (5)
    @Benchmark
    public void jcacheXDefaultPut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXDefault.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void jcacheXReadHeavyPut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXReadHeavy.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void jcacheXWriteHeavyPut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXWriteHeavy.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void jcacheXMemoryEfficientPut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXMemoryEfficient.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void jcacheXHighPerformancePut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXHighPerformance.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    // Specialized Profiles (3)
    @Benchmark
    public void jcacheXSessionCachePut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXSessionCache.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void jcacheXApiCachePut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXApiCache.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void jcacheXComputeCachePut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXComputeCache.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    // Advanced Profiles (4)
    @Benchmark
    public void jcacheXMlOptimizedPut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXMlOptimized.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void jcacheXZeroCopyPut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXZeroCopy.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void jcacheXHardwareOptimizedPut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXHardwareOptimized.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void jcacheXDistributedPut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXDistributed.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    // Industry-leading cache implementations PUT benchmarks
    @Benchmark
    public void caffeinePut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        caffeineCache.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void cache2kPut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        cache2kCache.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void ehcachePut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        ehcacheCache.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    @Benchmark
    public void jcachePut(ThreadState state, Blackhole bh) {
        if (jcacheCache != null) {
            int idx = state.nextIndex();
            jcacheCache.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void concurrentMapPut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        concurrentMap.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

    // ===============================
    // GET operations benchmarks - All 12 JCacheX Profiles
    // ===============================

    @Setup(Level.Invocation)
    public void setupGet() {
        // Pre-populate all caches for GET benchmarks
        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            String key = getSequentialKey(i);
            String value = getSequentialValue(i);

            // JCacheX Core Profiles
            jcacheXDefault.put(key, value);
            jcacheXReadHeavy.put(key, value);
            jcacheXWriteHeavy.put(key, value);
            jcacheXMemoryEfficient.put(key, value);
            jcacheXHighPerformance.put(key, value);

            // JCacheX Specialized Profiles
            jcacheXSessionCache.put(key, value);
            jcacheXApiCache.put(key, value);
            jcacheXComputeCache.put(key, value);

            // JCacheX Advanced Profiles
            jcacheXMlOptimized.put(key, value);
            jcacheXZeroCopy.put(key, value);
            jcacheXHardwareOptimized.put(key, value);
            jcacheXDistributed.put(key, value);

            // Industry implementations
            caffeineCache.put(key, value);
            cache2kCache.put(key, value);
            ehcacheCache.put(key, value);
            if (jcacheCache != null) {
                jcacheCache.put(key, value);
            }
            concurrentMap.put(key, value);
        }
    }

    // Core Profiles GET benchmarks (5)
    @Benchmark
    public String jcacheXDefaultGet(ThreadState state) {
        return jcacheXDefault.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXReadHeavyGet(ThreadState state) {
        return jcacheXReadHeavy.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXWriteHeavyGet(ThreadState state) {
        return jcacheXWriteHeavy.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXMemoryEfficientGet(ThreadState state) {
        return jcacheXMemoryEfficient.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXHighPerformanceGet(ThreadState state) {
        return jcacheXHighPerformance.get(getRandomKey(state.randomIndex()));
    }

    // Specialized Profiles GET benchmarks (3)
    @Benchmark
    public String jcacheXSessionCacheGet(ThreadState state) {
        return jcacheXSessionCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXApiCacheGet(ThreadState state) {
        return jcacheXApiCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXComputeCacheGet(ThreadState state) {
        return jcacheXComputeCache.get(getRandomKey(state.randomIndex()));
    }

    // Advanced Profiles GET benchmarks (4)
    @Benchmark
    public String jcacheXMlOptimizedGet(ThreadState state) {
        return jcacheXMlOptimized.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXZeroCopyGet(ThreadState state) {
        return jcacheXZeroCopy.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXHardwareOptimizedGet(ThreadState state) {
        return jcacheXHardwareOptimized.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXDistributedGet(ThreadState state) {
        return jcacheXDistributed.get(getRandomKey(state.randomIndex()));
    }

    // Industry implementations GET benchmarks
    @Benchmark
    public String caffeineGet(ThreadState state) {
        return caffeineCache.getIfPresent(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String cache2kGet(ThreadState state) {
        return cache2kCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String ehcacheGet(ThreadState state) {
        return ehcacheCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheGet(ThreadState state) {
        if (jcacheCache != null) {
            return jcacheCache.get(getRandomKey(state.randomIndex()));
        }
        return null;
    }

    @Benchmark
    public String concurrentMapGet(ThreadState state) {
        return concurrentMap.get(getRandomKey(state.randomIndex()));
    }

    // ===============================
    // REMOVE operations benchmarks - All 12 JCacheX Profiles
    // ===============================

    // Core Profiles REMOVE benchmarks (5)
    @Benchmark
    public String jcacheXDefaultRemove(ThreadState state) {
        return jcacheXDefault.remove(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXReadHeavyRemove(ThreadState state) {
        return jcacheXReadHeavy.remove(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXWriteHeavyRemove(ThreadState state) {
        return jcacheXWriteHeavy.remove(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXMemoryEfficientRemove(ThreadState state) {
        return jcacheXMemoryEfficient.remove(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXHighPerformanceRemove(ThreadState state) {
        return jcacheXHighPerformance.remove(getRandomKey(state.randomIndex()));
    }

    // Specialized Profiles REMOVE benchmarks (3)
    @Benchmark
    public String jcacheXSessionCacheRemove(ThreadState state) {
        return jcacheXSessionCache.remove(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXApiCacheRemove(ThreadState state) {
        return jcacheXApiCache.remove(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXComputeCacheRemove(ThreadState state) {
        return jcacheXComputeCache.remove(getRandomKey(state.randomIndex()));
    }

    // Advanced Profiles REMOVE benchmarks (4)
    @Benchmark
    public String jcacheXMlOptimizedRemove(ThreadState state) {
        return jcacheXMlOptimized.remove(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXZeroCopyRemove(ThreadState state) {
        return jcacheXZeroCopy.remove(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXHardwareOptimizedRemove(ThreadState state) {
        return jcacheXHardwareOptimized.remove(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public String jcacheXDistributedRemove(ThreadState state) {
        return jcacheXDistributed.remove(getRandomKey(state.randomIndex()));
    }

    // Industry implementations REMOVE benchmarks
    @Benchmark
    public void caffeineRemove(ThreadState state, Blackhole bh) {
        String result = caffeineCache.asMap().remove(getRandomKey(state.randomIndex()));
        bh.consume(result);
    }

    @Benchmark
    public String cache2kRemove(ThreadState state) {
        String key = getRandomKey(state.randomIndex());
        return cache2kCache.peekAndRemove(key);
    }

    @Benchmark
    public String ehcacheRemove(ThreadState state) {
        String key = getRandomKey(state.randomIndex());
        String value = ehcacheCache.get(key);
        ehcacheCache.remove(key);
        return value;
    }

    @Benchmark
    public String jcacheRemove(ThreadState state) {
        if (jcacheCache != null) {
            String key = getRandomKey(state.randomIndex());
            String value = jcacheCache.get(key);
            jcacheCache.remove(key);
            return value;
        }
        return null;
    }

    @Benchmark
    public String concurrentMapRemove(ThreadState state) {
        return concurrentMap.remove(getRandomKey(state.randomIndex()));
    }

    // ===============================
    // Mixed workload benchmarks (80% read, 20% write) - All 12 JCacheX Profiles
    // ===============================

    // Core Profiles Mixed benchmarks (5)
    @Benchmark
    public void jcacheXDefaultMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            // 80% reads
            String result = jcacheXDefault.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            // 20% writes
            jcacheXDefault.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void jcacheXReadHeavyMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = jcacheXReadHeavy.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            jcacheXReadHeavy.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void jcacheXWriteHeavyMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = jcacheXWriteHeavy.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            jcacheXWriteHeavy.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void jcacheXMemoryEfficientMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = jcacheXMemoryEfficient.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            jcacheXMemoryEfficient.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void jcacheXHighPerformanceMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = jcacheXHighPerformance.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            jcacheXHighPerformance.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    // Specialized Profiles Mixed benchmarks (3)
    @Benchmark
    public void jcacheXSessionCacheMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = jcacheXSessionCache.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            jcacheXSessionCache.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void jcacheXApiCacheMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = jcacheXApiCache.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            jcacheXApiCache.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void jcacheXComputeCacheMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = jcacheXComputeCache.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            jcacheXComputeCache.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    // Advanced Profiles Mixed benchmarks (4)
    @Benchmark
    public void jcacheXMlOptimizedMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = jcacheXMlOptimized.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            jcacheXMlOptimized.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void jcacheXZeroCopyMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = jcacheXZeroCopy.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            jcacheXZeroCopy.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void jcacheXHardwareOptimizedMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = jcacheXHardwareOptimized.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            jcacheXHardwareOptimized.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void jcacheXDistributedMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = jcacheXDistributed.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            jcacheXDistributed.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    // Industry implementations Mixed benchmarks
    @Benchmark
    public void caffeineMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            // 80% reads
            String result = caffeineCache.getIfPresent(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            // 20% writes
            caffeineCache.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void cache2kMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = cache2kCache.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            cache2kCache.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void ehcacheMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = ehcacheCache.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            ehcacheCache.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }

    @Benchmark
    public void concurrentMapMixed(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        if (state.random.nextDouble() < 0.8) {
            String result = concurrentMap.get(getRandomKey(state.randomIndex()));
            bh.consume(result);
        } else {
            concurrentMap.put(getSequentialKey(idx), getSequentialValue(idx));
            bh.consume(idx);
        }
    }
}
