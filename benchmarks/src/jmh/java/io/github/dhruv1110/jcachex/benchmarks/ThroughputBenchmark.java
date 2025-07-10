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

    @Setup(Level.Iteration)
    public void setupIteration() {
        // Pre-populate caches for read operations
        for (int i = 0; i < OPERATIONS_COUNT; i++) {
            String key = getSequentialKey(i);
            String value = getSequentialValue(i);

            jcacheXCache.put(key, value);
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
    // Single-threaded throughput
    // ===============================

    @Benchmark
    @Threads(1)
    public String jcacheXGetThroughput(ThreadState state) {
        return jcacheXCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(1)
    public void jcacheXPutThroughput(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXCache.put(getRandomKey(idx), getRandomValue(idx));
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

    // ===============================
    // Multi-threaded throughput (4 threads)
    // ===============================

    @Benchmark
    @Threads(4)
    public String jcacheXGetThroughput4T(ThreadState state) {
        return jcacheXCache.get(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    @Threads(4)
    public void jcacheXPutThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXCache.put(getRandomKey(idx), getRandomValue(idx));
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

    // ===============================
    // Realistic mixed workload throughput
    // ===============================

    @Benchmark
    @Threads(1)
    public void jcacheXMixedThroughput(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) { // 80% reads
            String value = jcacheXCache.get(getRandomKey(idx));
            bh.consume(value);
        } else { // 20% writes
            jcacheXCache.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

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

    // ===============================
    // Multi-threaded mixed workload
    // ===============================

    @Benchmark
    @Threads(4)
    public void jcacheXMixedThroughput4T(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 80) {
            String value = jcacheXCache.get(getRandomKey(idx));
            bh.consume(value);
        } else {
            jcacheXCache.put(getRandomKey(idx), getRandomValue(idx));
        }
    }

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
}
