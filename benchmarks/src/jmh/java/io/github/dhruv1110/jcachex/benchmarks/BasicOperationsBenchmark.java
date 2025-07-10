package io.github.dhruv1110.jcachex.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Benchmark for basic cache operations: get, put, remove operations.
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
    // PUT operations benchmarks
    // ===============================

    @Benchmark
    public void jcacheXPut(ThreadState state, Blackhole bh) {
        int idx = state.nextIndex();
        jcacheXCache.put(getSequentialKey(idx), getSequentialValue(idx));
        bh.consume(idx);
    }

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
    // GET operations benchmarks
    // ===============================

    @Setup(Level.Invocation)
    public void setupGet() {
        // Pre-populate caches for GET benchmarks
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

    @Benchmark
    public String jcacheXGet(ThreadState state) {
        return jcacheXCache.get(getRandomKey(state.randomIndex()));
    }

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
    // REMOVE operations benchmarks
    // ===============================

    @Benchmark
    public String jcacheXRemove(ThreadState state) {
        return jcacheXCache.remove(getRandomKey(state.randomIndex()));
    }

    @Benchmark
    public void caffeineRemove(ThreadState state, Blackhole bh) {
        caffeineCache.invalidate(getRandomKey(state.randomIndex()));
        bh.consume(state.randomIndex());
    }

    @Benchmark
    public String cache2kRemove(ThreadState state) {
        String key = getRandomKey(state.randomIndex());
        String value = cache2kCache.get(key);
        cache2kCache.remove(key);
        return value;
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
    // MIXED operations benchmark (realistic workload)
    // ===============================

    @Benchmark
    public void jcacheXMixed(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 70) { // 70% reads
            String value = jcacheXCache.get(getRandomKey(idx));
            bh.consume(value);
        } else if (operation < 95) { // 25% writes
            jcacheXCache.put(getRandomKey(idx), getRandomValue(idx));
        } else { // 5% removes
            String removed = jcacheXCache.remove(getRandomKey(idx));
            bh.consume(removed);
        }
    }

    @Benchmark
    public void caffeineMixed(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 70) { // 70% reads
            String value = caffeineCache.getIfPresent(getRandomKey(idx));
            bh.consume(value);
        } else if (operation < 95) { // 25% writes
            caffeineCache.put(getRandomKey(idx), getRandomValue(idx));
        } else { // 5% removes
            caffeineCache.invalidate(getRandomKey(idx));
        }
    }

    @Benchmark
    public void cache2kMixed(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 70) { // 70% reads
            String value = cache2kCache.get(getRandomKey(idx));
            bh.consume(value);
        } else if (operation < 95) { // 25% writes
            cache2kCache.put(getRandomKey(idx), getRandomValue(idx));
        } else { // 5% removes
            cache2kCache.remove(getRandomKey(idx));
        }
    }

    @Benchmark
    public void ehcacheMixed(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 70) { // 70% reads
            String value = ehcacheCache.get(getRandomKey(idx));
            bh.consume(value);
        } else if (operation < 95) { // 25% writes
            ehcacheCache.put(getRandomKey(idx), getRandomValue(idx));
        } else { // 5% removes
            ehcacheCache.remove(getRandomKey(idx));
        }
    }

    @Benchmark
    public void concurrentMapMixed(ThreadState state, Blackhole bh) {
        int idx = state.randomIndex();
        int operation = state.random.nextInt(100);

        if (operation < 70) { // 70% reads
            String value = concurrentMap.get(getRandomKey(idx));
            bh.consume(value);
        } else if (operation < 95) { // 25% writes
            concurrentMap.put(getRandomKey(idx), getRandomValue(idx));
        } else { // 5% removes
            String removed = concurrentMap.remove(getRandomKey(idx));
            bh.consume(removed);
        }
    }
}
