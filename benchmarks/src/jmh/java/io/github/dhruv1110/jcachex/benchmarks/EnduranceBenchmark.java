package io.github.dhruv1110.jcachex.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Endurance benchmark suite designed to test cache performance over extended
 * periods
 * with sustained load, GC pressure, and memory leak detection.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 30, timeUnit = TimeUnit.SECONDS) // 30s warmup
@Measurement(iterations = 5, time = 120, timeUnit = TimeUnit.SECONDS) // 2 minute tests
@Fork(2)
public class EnduranceBenchmark extends BaseBenchmark {

    // ENDURANCE CONFIGURATION - Long running tests
    private static final int ENDURANCE_CACHE_SIZE = 50_000; // 50K cache capacity
    private static final int ENDURANCE_OPERATIONS = 500_000; // 500K working set
    private static final int MEMORY_ALLOCATION_SIZE = 5_000; // 5KB objects
    private static final int GC_PRESSURE_FREQUENCY = 1000; // Every 1000 operations

    // Memory tracking
    private static final AtomicLong totalAllocations = new AtomicLong();
    private static final AtomicLong totalOperations = new AtomicLong();
    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    // Test data for endurance testing
    private String[] enduranceKeys;
    private MemoryIntensiveObject[] enduranceValues;

    @Setup(Level.Trial)
    public void setupEndurance() {
        setupEnduranceData();
        setupEnduranceCaches();
        logInitialMemoryStats();
    }

    private void setupEnduranceData() {
        enduranceKeys = new String[ENDURANCE_OPERATIONS];
        enduranceValues = new MemoryIntensiveObject[ENDURANCE_OPERATIONS];

        for (int i = 0; i < ENDURANCE_OPERATIONS; i++) {
            enduranceKeys[i] = "endurance_key_" + i;
            enduranceValues[i] = new MemoryIntensiveObject(i, MEMORY_ALLOCATION_SIZE);
        }
    }

    private void setupEnduranceCaches() {
        // Pre-populate caches with sustained load data
        for (int i = 0; i < ENDURANCE_CACHE_SIZE; i++) {
            String key = enduranceKeys[i % enduranceKeys.length];
            String value = enduranceValues[i % enduranceValues.length].getData();

            if (jcacheXDefault != null) {
                jcacheXDefault.put(key, value);
            }
            if (jcacheXHighPerformance != null) {
                jcacheXHighPerformance.put(key, value);
            }
            if (jcacheXMemoryEfficient != null) {
                jcacheXMemoryEfficient.put(key, value);
            }
            if (caffeineCache != null) {
                caffeineCache.put(key, value);
            }
            if (concurrentMap != null) {
                concurrentMap.put(key, value);
            }
        }
    }

    private void logInitialMemoryStats() {
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        System.out.println("Initial Heap Memory: " + heapMemory.getUsed() / (1024 * 1024) + " MB");
    }

    // ===================================
    // SUSTAINED LOAD TESTS (Extended Duration)
    // ===================================

    @Benchmark
    @Threads(8)
    public String jcacheXDefaultSustainedLoad(ThreadState state) {
        trackMemoryUsage(state);
        return jcacheXDefault.get(getEnduranceKey(state));
    }

    @Benchmark
    @Threads(8)
    public String jcacheXHighPerformanceSustainedLoad(ThreadState state) {
        trackMemoryUsage(state);
        return jcacheXHighPerformance.get(getEnduranceKey(state));
    }

    @Benchmark
    @Threads(8)
    public String jcacheXMemoryEfficientSustainedLoad(ThreadState state) {
        trackMemoryUsage(state);
        return jcacheXMemoryEfficient.get(getEnduranceKey(state));
    }

    @Benchmark
    @Threads(8)
    public String caffeineSustainedLoad(ThreadState state) {
        trackMemoryUsage(state);
        return caffeineCache.getIfPresent(getEnduranceKey(state));
    }

    @Benchmark
    @Threads(8)
    public String concurrentMapSustainedLoad(ThreadState state) {
        trackMemoryUsage(state);
        return concurrentMap.get(getEnduranceKey(state));
    }

    // ===================================
    // GC PRESSURE TESTS (Memory Allocation Stress)
    // ===================================

    @Benchmark
    @Threads(6)
    public void jcacheXDefaultGCPressure(ThreadState state, Blackhole bh) {
        int idx = state.nextEnduranceIndex();

        // Create memory pressure by allocating large objects
        if (idx % GC_PRESSURE_FREQUENCY == 0) {
            createGCPressure(state, bh);
        }

        jcacheXDefault.put(enduranceKeys[idx], enduranceValues[idx].getData());
        bh.consume(idx);
    }

    @Benchmark
    @Threads(6)
    public void jcacheXMemoryEfficientGCPressure(ThreadState state, Blackhole bh) {
        int idx = state.nextEnduranceIndex();

        if (idx % GC_PRESSURE_FREQUENCY == 0) {
            createGCPressure(state, bh);
        }

        jcacheXMemoryEfficient.put(enduranceKeys[idx], enduranceValues[idx].getData());
        bh.consume(idx);
    }

    @Benchmark
    @Threads(6)
    public void caffeineGCPressure(ThreadState state, Blackhole bh) {
        int idx = state.nextEnduranceIndex();

        if (idx % GC_PRESSURE_FREQUENCY == 0) {
            createGCPressure(state, bh);
        }

        caffeineCache.put(enduranceKeys[idx], enduranceValues[idx].getData());
        bh.consume(idx);
    }

    // ===================================
    // MEMORY LEAK DETECTION TESTS
    // ===================================

    @Benchmark
    @Threads(4)
    public void jcacheXDefaultMemoryLeakTest(ThreadState state, Blackhole bh) {
        int idx = state.nextEnduranceIndex();

        // Continuous put/get/remove cycle to detect leaks
        String key = enduranceKeys[idx];
        String value = enduranceValues[idx].getData();

        jcacheXDefault.put(key, value);
        String retrieved = jcacheXDefault.get(key);

        // Remove every 3rd element to create churn
        if (idx % 3 == 0) {
            jcacheXDefault.remove(key);
        }

        bh.consume(retrieved);

        // Track memory usage periodically
        if (idx % 10000 == 0) {
            logMemoryUsage("JCacheX-Default", idx);
        }
    }

    @Benchmark
    @Threads(4)
    public void caffeineMemoryLeakTest(ThreadState state, Blackhole bh) {
        int idx = state.nextEnduranceIndex();

        String key = enduranceKeys[idx];
        String value = enduranceValues[idx].getData();

        caffeineCache.put(key, value);
        String retrieved = caffeineCache.getIfPresent(key);

        if (idx % 3 == 0) {
            caffeineCache.invalidate(key);
        }

        bh.consume(retrieved);

        if (idx % 10000 == 0) {
            logMemoryUsage("Caffeine", idx);
        }
    }

    // ===================================
    // BURST LOAD TESTS (Sudden Traffic Spikes)
    // ===================================

    @Benchmark
    @Threads(16)
    public String jcacheXDefaultBurstLoad(ThreadState state) {
        // Simulate burst traffic with random access pattern
        String key = getRandomBurstKey(state);

        // Create burst behavior - 90% of requests in 10% of time
        if (state.random.nextDouble() < 0.1) {
            // High activity burst
            for (int i = 0; i < 10; i++) {
                jcacheXDefault.get(getEnduranceKey(state));
            }
        }

        return jcacheXDefault.get(key);
    }

    @Benchmark
    @Threads(16)
    public String caffeineBurstLoad(ThreadState state) {
        String key = getRandomBurstKey(state);

        if (state.random.nextDouble() < 0.1) {
            for (int i = 0; i < 10; i++) {
                caffeineCache.getIfPresent(getEnduranceKey(state));
            }
        }

        return caffeineCache.getIfPresent(key);
    }

    // ===================================
    // UTILITY METHODS
    // ===================================

    private String getEnduranceKey(ThreadState state) {
        return enduranceKeys[state.nextEnduranceIndex()];
    }

    private String getRandomBurstKey(ThreadState state) {
        return enduranceKeys[state.random.nextInt(enduranceKeys.length)];
    }

    private void trackMemoryUsage(ThreadState state) {
        long operations = totalOperations.incrementAndGet();

        if (operations % 50000 == 0) {
            MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
            long usedMemory = heapMemory.getUsed() / (1024 * 1024);
            System.out.println("Operations: " + operations + ", Heap Memory: " + usedMemory + " MB");
        }
    }

    private void createGCPressure(ThreadState state, Blackhole bh) {
        // Allocate temporary objects to create GC pressure
        for (int i = 0; i < 100; i++) {
            String tempData = new String(new char[1000]).replace('\0', 'X');
            bh.consume(tempData);
        }
        totalAllocations.incrementAndGet();
    }

    private void logMemoryUsage(String cacheName, int operationIndex) {
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        long usedMemory = heapMemory.getUsed() / (1024 * 1024);

        System.out.println(String.format("[%s] Operation %d: Memory Usage = %d MB, Max = %d MB",
                cacheName, operationIndex, usedMemory, heapMemory.getMax() / (1024 * 1024)));
    }

    // ===================================
    // THREAD STATE
    // ===================================

    @State(Scope.Thread)
    public static class ThreadState {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int enduranceCounter = 0;

        public int nextEnduranceIndex() {
            return enduranceCounter++ % ENDURANCE_OPERATIONS;
        }
    }

    // ===================================
    // MEMORY INTENSIVE OBJECT
    // ===================================

    private static class MemoryIntensiveObject {
        private final String data;
        private final int id;
        private final long timestamp;
        private final byte[] payload;

        public MemoryIntensiveObject(int id, int size) {
            this.id = id;
            this.timestamp = System.nanoTime();

            // Create memory-intensive string
            StringBuilder sb = new StringBuilder(size);
            for (int i = 0; i < size; i++) {
                sb.append((char) ('A' + (i % 26)));
            }
            this.data = sb.toString();

            // Additional memory allocation
            this.payload = new byte[size / 2];
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (i % 256);
            }
        }

        public String getData() {
            return data + "_" + id + "_" + timestamp + "_" + payload.length;
        }
    }

    @TearDown(Level.Trial)
    public void tearDownEndurance() {
        // Log final memory and GC stats
        logFinalStats();
    }

    private void logFinalStats() {
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        long finalMemory = heapMemory.getUsed() / (1024 * 1024);

        System.out.println("=== Endurance Test Final Stats ===");
        System.out.println("Total Operations: " + totalOperations.get());
        System.out.println("Total Allocations: " + totalAllocations.get());
        System.out.println("Final Heap Memory: " + finalMemory + " MB");

        // Log GC statistics
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.println("GC [" + gcBean.getName() + "]: " +
                    gcBean.getCollectionCount() + " collections, " +
                    gcBean.getCollectionTime() + " ms");
        }
    }
}
