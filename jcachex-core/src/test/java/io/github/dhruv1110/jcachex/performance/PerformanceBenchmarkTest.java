//package io.github.dhruv1110.jcachex.performance;
//
//import io.github.dhruv1110.jcachex.Cache;
//import io.github.dhruv1110.jcachex.CacheConfig;
//import io.github.dhruv1110.jcachex.DefaultCache;
//import io.github.dhruv1110.jcachex.eviction.LRUEvictionStrategy;
//import io.github.dhruv1110.jcachex.eviction.LFUEvictionStrategy;
//import io.github.dhruv1110.jcachex.eviction.FIFOEvictionStrategy;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Timeout;
//
//import java.time.Duration;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Performance benchmark tests for JCacheX.
// * <p>
// * This test suite provides comprehensive performance benchmarking capabilities
// * to validate JCacheX's performance characteristics under various scenarios:
// * </p>
// * <ul>
// * <li>Single-threaded throughput testing</li>
// * <li>Multi-threaded concurrent access testing</li>
// * <li>Memory efficiency validation</li>
// * <li>Eviction strategy performance comparison</li>
// * <li>Large dataset handling capabilities</li>
// * <li>Async operations performance</li>
// * </ul>
// */
//@DisplayName("JCacheX Performance Benchmark Tests")
//public class PerformanceBenchmarkTest {
//
//    private static final int SINGLE_THREAD_OPERATIONS = 25_000;
//    private static final int MULTI_THREAD_OPERATIONS = 10_000;
//    private static final int CONCURRENT_THREADS = 5;
//    private static final int LARGE_DATASET_SIZE = 100_000;
//    private static final int CACHE_SIZE = 5_000;
//
//    private Cache<String, String> cache;
//    private Random random;
//
//    @BeforeEach
//    void setUp() {
//        random = new Random(42); // Fixed seed for reproducible results
//        setupDefaultCache();
//    }
//
//    private void setupDefaultCache() {
//        CacheConfig<String, String> config = CacheConfig.<String, String>newBuilder()
//                .maximumSize((long) CACHE_SIZE)
//                .evictionStrategy(new LRUEvictionStrategy<>())
//                .recordStats(true)
//                .build();
//        cache = new DefaultCache<>(config);
//    }
//
//    @Test
//    @DisplayName("Single-threaded PUT performance benchmark")
//    @Timeout(value = 10, unit = TimeUnit.SECONDS)
//    void testSingleThreadedPutPerformance() {
//        // Warm up JVM
//        for (int i = 0; i < 1000; i++) {
//            cache.put("warmup-" + i, "value-" + i);
//        }
//        cache.clear();
//
//        // Benchmark actual operations
//        long startTime = System.nanoTime();
//
//        for (int i = 0; i < SINGLE_THREAD_OPERATIONS; i++) {
//            cache.put("key-" + i, "value-" + i);
//        }
//
//        long endTime = System.nanoTime();
//        long duration = endTime - startTime;
//        double operationsPerSecond = (double) SINGLE_THREAD_OPERATIONS / (duration / 1_000_000_000.0);
//
//        System.out.printf("Single-threaded PUT: %.2f ops/sec (%.2f ms total)%n",
//                operationsPerSecond, duration / 1_000_000.0);
//
//        // Validate performance meets minimum threshold
//        assertTrue(operationsPerSecond > 10_000,
//                "PUT operations should exceed 10,000 ops/sec, got: " + operationsPerSecond);
//        assertEquals(SINGLE_THREAD_OPERATIONS, cache.size());
//    }
//
//    @Test
//    @DisplayName("Single-threaded GET performance benchmark")
//    @Timeout(value = 10, unit = TimeUnit.SECONDS)
//    void testSingleThreadedGetPerformance() {
//        // Populate cache
//        for (int i = 0; i < SINGLE_THREAD_OPERATIONS; i++) {
//            cache.put("key-" + i, "value-" + i);
//        }
//
//        // Warm up
//        for (int i = 0; i < 1000; i++) {
//            cache.get("key-" + (i % SINGLE_THREAD_OPERATIONS));
//        }
//
//        // Benchmark GET operations
//        long startTime = System.nanoTime();
//
//        for (int i = 0; i < SINGLE_THREAD_OPERATIONS; i++) {
//            String value = cache.get("key-" + i);
//            assertNotNull(value);
//        }
//
//        long endTime = System.nanoTime();
//        long duration = endTime - startTime;
//        double operationsPerSecond = (double) SINGLE_THREAD_OPERATIONS / (duration / 1_000_000_000.0);
//
//        System.out.printf("Single-threaded GET: %.2f ops/sec (%.2f ms total)%n",
//                operationsPerSecond, duration / 1_000_000.0);
//
//        // GET operations should be faster than PUT
//        assertTrue(operationsPerSecond > 20_000,
//                "GET operations should exceed 20,000 ops/sec, got: " + operationsPerSecond);
//        assertEquals(1.0, cache.stats().hitRate(), 0.01);
//    }
//
//    @Test
//    @DisplayName("Multi-threaded concurrent access performance")
//    @Timeout(value = 30, unit = TimeUnit.SECONDS)
//    void testMultiThreadedConcurrentAccess() throws InterruptedException {
//        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
//
//        // Pre-populate cache with some data
//        for (int i = 0; i < CACHE_SIZE / 2; i++) {
//            cache.put("initial-" + i, "value-" + i);
//        }
//
//        long startTime = System.nanoTime();
//
//        // Submit concurrent tasks
//        List<CompletableFuture<Void>> futures = IntStream.range(0, CONCURRENT_THREADS)
//                .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
//                    Random threadRandom = ThreadLocalRandom.current();
//                    for (int i = 0; i < MULTI_THREAD_OPERATIONS; i++) {
//                        String key = "thread-" + threadId + "-key-" + i;
//                        String value = "thread-" + threadId + "-value-" + i;
//
//                        // Mix of operations: 60% GET, 30% PUT, 10% REMOVE
//                        int operation = threadRandom.nextInt(10);
//                        if (operation < 6) {
//                            // GET operation
//                            cache.get(key);
//                        } else if (operation < 9) {
//                            // PUT operation
//                            cache.put(key, value);
//                        } else {
//                            // REMOVE operation
//                            cache.remove(key);
//                        }
//                    }
//                }, executor))
//                .collect(Collectors.toList());
//
//        // Wait for all tasks to complete
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//        long endTime = System.nanoTime();
//        long duration = endTime - startTime;
//        double totalOperations = CONCURRENT_THREADS * MULTI_THREAD_OPERATIONS;
//        double operationsPerSecond = totalOperations / (duration / 1_000_000_000.0);
//
//        System.out.printf("Multi-threaded (%d threads): %.2f ops/sec (%.2f ms total)%n",
//                CONCURRENT_THREADS, operationsPerSecond, duration / 1_000_000.0);
//
//        // Validate concurrent performance
//        assertTrue(operationsPerSecond > 25_000,
//                "Concurrent operations should exceed 25,000 ops/sec, got: " + operationsPerSecond);
//
//        executor.shutdown();
//        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
//    }
//
//    @Test
//    @DisplayName("Async operations performance benchmark")
//    @Timeout(value = 15, unit = TimeUnit.SECONDS)
//    void testAsyncOperationsPerformance() {
//        final int asyncOps = 10_000;
//
//        long startTime = System.nanoTime();
//
//        // Async PUT operations
//        List<CompletableFuture<Void>> putFutures = IntStream.range(0, asyncOps)
//                .mapToObj(i -> cache.putAsync("async-key-" + i, "async-value-" + i))
//                .collect(Collectors.toList());
//
//        CompletableFuture.allOf(putFutures.toArray(new CompletableFuture[0])).join();
//
//        // Async GET operations
//        List<CompletableFuture<String>> getFutures = IntStream.range(0, asyncOps)
//                .mapToObj(i -> cache.getAsync("async-key-" + i))
//                .collect(Collectors.toList());
//
//        CompletableFuture.allOf(getFutures.toArray(new CompletableFuture[0])).join();
//
//        long endTime = System.nanoTime();
//        long duration = endTime - startTime;
//        double operationsPerSecond = (double) (asyncOps * 2) / (duration / 1_000_000_000.0);
//
//        System.out.printf("Async operations: %.2f ops/sec (%.2f ms total)%n",
//                operationsPerSecond, duration / 1_000_000.0);
//
//        // Validate all async operations completed successfully
//        long completedGets = getFutures.stream()
//                .mapToLong(future -> future.join() != null ? 1 : 0)
//                .sum();
//
//        assertEquals(asyncOps, completedGets);
//        assertTrue(operationsPerSecond > 30_000,
//                "Async operations should exceed 30,000 ops/sec, got: " + operationsPerSecond);
//    }
//
//    @Test
//    @DisplayName("Eviction strategy performance comparison")
//    @Timeout(value = 20, unit = TimeUnit.SECONDS)
//    void testEvictionStrategyPerformance() {
//        final int testSize = 50_000;
//        final int cacheSize = 10_000;
//
//        // Test LRU Strategy
//        Cache<String, String> lruCache = createCacheWithStrategy(new LRUEvictionStrategy<>(), cacheSize);
//        long lruTime = benchmarkEvictionStrategy(lruCache, "LRU", testSize);
//
//        // Test LFU Strategy
//        Cache<String, String> lfuCache = createCacheWithStrategy(new LFUEvictionStrategy<>(), cacheSize);
//        long lfuTime = benchmarkEvictionStrategy(lfuCache, "LFU", testSize);
//
//        // Test FIFO Strategy
//        Cache<String, String> fifoCache = createCacheWithStrategy(new FIFOEvictionStrategy<>(), cacheSize);
//        long fifoTime = benchmarkEvictionStrategy(fifoCache, "FIFO", testSize);
//
//        // Validate that all strategies complete within reasonable time
//        assertTrue(lruTime < 5000, "LRU eviction should complete within 5 seconds");
//        assertTrue(lfuTime < 5000, "LFU eviction should complete within 5 seconds");
//        assertTrue(fifoTime < 5000, "FIFO eviction should complete within 5 seconds");
//    }
//
//    private Cache<String, String> createCacheWithStrategy(
//            io.github.dhruv1110.jcachex.eviction.EvictionStrategy<String, String> strategy,
//            int maxSize) {
//        CacheConfig<String, String> config = CacheConfig.<String, String>newBuilder()
//                .maximumSize((long) maxSize)
//                .evictionStrategy(strategy)
//                .recordStats(true)
//                .build();
//        return new DefaultCache<>(config);
//    }
//
//    private long benchmarkEvictionStrategy(Cache<String, String> testCache, String strategyName, int operations) {
//        long startTime = System.nanoTime();
//
//        for (int i = 0; i < operations; i++) {
//            testCache.put("key-" + i, "value-" + i);
//        }
//
//        long endTime = System.nanoTime();
//        long duration = endTime - startTime;
//        double operationsPerSecond = (double) operations / (duration / 1_000_000_000.0);
//
//        System.out.printf("%s eviction: %.2f ops/sec (%.2f ms total)%n",
//                strategyName, operationsPerSecond, duration / 1_000_000.0);
//
//        return duration / 1_000_000; // Return milliseconds
//    }
//
//    @Test
//    @DisplayName("Memory efficiency validation")
//    @Timeout(value = 30, unit = TimeUnit.SECONDS)
//    void testMemoryEfficiency() {
//        // Force garbage collection before test
//        System.gc();
//        Thread.yield();
//
//        Runtime runtime = Runtime.getRuntime();
//        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
//
//        // Create cache with weight-based eviction
//        CacheConfig<String, String> config = CacheConfig.<String, String>newBuilder()
//                .maximumWeight(1_000_000L) // 1MB worth of data
//                .weigher((key, value) -> (long) (key.length() + value.length()))
//                .evictionStrategy(new LRUEvictionStrategy<>())
//                .recordStats(true)
//                .build();
//
//        try (DefaultCache<String, String> weightedCache = new DefaultCache<>(config)) {
//            // Fill cache with data
//            for (int i = 0; i < 50_000; i++) {
//                String key = "memory-test-key-" + i;
//                String value = "memory-test-value-" + i + "-" + new String(new char[20]).replace('\0', 'x');
//                weightedCache.put(key, value);
//            }
//
//            // Force GC and measure memory usage
//            System.gc();
//            Thread.yield();
//
//            long peakMemory = runtime.totalMemory() - runtime.freeMemory();
//            long memoryUsed = peakMemory - initialMemory;
//
//            System.out.printf("Memory efficiency test: %d bytes used, cache size: %d%n",
//                    memoryUsed, weightedCache.size());
//
//            // Validate memory usage is reasonable (should be less than 10MB for this test)
//            assertTrue(memoryUsed < 10_000_000,
//                    "Memory usage should be reasonable, got: " + memoryUsed + " bytes");
//
//            // Validate cache is functioning correctly
//            assertNotNull(weightedCache.get("memory-test-key-0"));
//            assertTrue(weightedCache.stats().hitRate() > 0.0);
//        }
//    }
//
//    @Test
//    @DisplayName("Large dataset handling performance")
//    @Timeout(value = 60, unit = TimeUnit.SECONDS)
//    void testLargeDatasetPerformance() {
//        // Test with a cache that can handle large datasets
//        CacheConfig<String, String> config = CacheConfig.<String, String>newBuilder()
//                .maximumSize(100_000L)
//                .evictionStrategy(new LRUEvictionStrategy<>())
//                .recordStats(true)
//                .build();
//
//        try (DefaultCache<String, String> largeCache = new DefaultCache<>(config)) {
//            final int largeDatasetSize = 200_000;
//
//            long startTime = System.nanoTime();
//
//            // Load large dataset
//            for (int i = 0; i < largeDatasetSize; i++) {
//                largeCache.put("large-key-" + i, "large-value-" + i);
//            }
//
//            long endTime = System.nanoTime();
//            long duration = endTime - startTime;
//            double operationsPerSecond = (double) largeDatasetSize / (duration / 1_000_000_000.0);
//
//            System.out.printf("Large dataset (%d items): %.2f ops/sec (%.2f ms total)%n",
//                    largeDatasetSize, operationsPerSecond, duration / 1_000_000.0);
//
//            // Validate performance and correctness
//            assertTrue(operationsPerSecond > 10_000,
//                    "Large dataset handling should exceed 10,000 ops/sec, got: " + operationsPerSecond);
//            assertEquals(100_000L, largeCache.size()); // Should be limited by max size
//
//            // Test retrieval from large dataset
//            long retrievalStart = System.nanoTime();
//            for (int i = largeDatasetSize - 50_000; i < largeDatasetSize; i++) {
//                assertNotNull(largeCache.get("large-key-" + i));
//            }
//            long retrievalEnd = System.nanoTime();
//            long retrievalDuration = retrievalEnd - retrievalStart;
//            double retrievalOpsPerSec = 50_000.0 / (retrievalDuration / 1_000_000_000.0);
//
//            System.out.printf("Large dataset retrieval: %.2f ops/sec%n", retrievalOpsPerSec);
//            assertTrue(retrievalOpsPerSec > 50_000,
//                    "Large dataset retrieval should exceed 50,000 ops/sec");
//        }
//    }
//
//    @Test
//    @DisplayName("Cache expiration performance")
//    @Timeout(value = 10, unit = TimeUnit.SECONDS)
//    void testExpirationPerformance() {
//        // Create cache with short expiration time
//        CacheConfig<String, String> config = CacheConfig.<String, String>newBuilder()
//                .maximumSize(10_000L)
//                .expireAfterWrite(Duration.ofMillis(100))
//                .recordStats(true)
//                .build();
//
//        try (DefaultCache<String, String> expiringCache = new DefaultCache<>(config)) {
//            // Load data
//            for (int i = 0; i < 5_000; i++) {
//                expiringCache.put("expiring-key-" + i, "expiring-value-" + i);
//            }
//
//            assertEquals(5_000, expiringCache.size());
//
//            // Wait for expiration
//            try {
//                Thread.sleep(200);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                fail("Test interrupted");
//            }
//
//            // Test performance of accessing expired entries
//            long startTime = System.nanoTime();
//            int nullResults = 0;
//
//            for (int i = 0; i < 5_000; i++) {
//                String value = expiringCache.get("expiring-key-" + i);
//                if (value == null) {
//                    nullResults++;
//                }
//            }
//
//            long endTime = System.nanoTime();
//            long duration = endTime - startTime;
//            double operationsPerSecond = 5_000.0 / (duration / 1_000_000_000.0);
//
//            System.out.printf("Expiration handling: %.2f ops/sec, %d expired entries%n",
//                    operationsPerSecond, nullResults);
//
//            // Validate that expired entries are handled efficiently
//            assertTrue(operationsPerSecond > 20_000,
//                    "Expiration handling should exceed 20,000 ops/sec");
//            assertTrue(nullResults > 4_000,
//                    "Most entries should have expired");
//        }
//    }
//
//    @Test
//    @DisplayName("Performance regression detection")
//    void testPerformanceRegression() {
//        // This test establishes performance baselines and can be used to detect
//        // regressions
//        final int baselineOps = 25_000;
//
//        long startTime = System.nanoTime();
//
//        for (int i = 0; i < baselineOps; i++) {
//            cache.put("baseline-key-" + i, "baseline-value-" + i);
//            cache.get("baseline-key-" + i);
//        }
//
//        long endTime = System.nanoTime();
//        long duration = endTime - startTime;
//        double operationsPerSecond = (double) (baselineOps * 2) / (duration / 1_000_000_000.0);
//
//        System.out.printf("Performance baseline: %.2f ops/sec%n", operationsPerSecond);
//
//        // Establish minimum performance threshold
//        assertTrue(operationsPerSecond > 50_000,
//                "Performance regression detected! Expected > 50,000 ops/sec, got: " + operationsPerSecond);
//
//        // Log performance for monitoring
//        System.out.printf("Performance test completed successfully at %.2f ops/sec%n", operationsPerSecond);
//    }
//}
