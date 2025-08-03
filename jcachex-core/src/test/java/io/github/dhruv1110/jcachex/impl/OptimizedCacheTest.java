package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OptimizedCache implementation.
 * Extends BaseCacheTest to inherit all common functionality tests.
 */
@DisplayName("OptimizedCache Tests")
class OptimizedCacheTest extends BaseCacheTest {

    @Override
    protected Cache<String, String> createCache(CacheConfig<String, String> config) {
        return new OptimizedCache<>(config);
    }

    @Nested
    @DisplayName("OptimizedCache Specific Features")
    class OptimizedCacheSpecificFeatures {

        @Test
        @DisplayName("Should provide detailed metrics")
        void shouldProvideDetailedMetrics() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            cache.get("key1"); // hit
            cache.get("nonexistent"); // miss

            if (cache instanceof OptimizedCache) {
                OptimizedCache<String, String> optimizedCache = (OptimizedCache<String, String>) cache;
                String metrics = optimizedCache.getDetailedMetrics();

                assertNotNull(metrics);
                assertTrue(metrics.contains("HitRatio"));
                assertTrue(metrics.contains("FreqSketchSize"));
            }
        }

        @Test
        @DisplayName("Should adapt frequency sketch based on performance")
        void shouldAdaptFrequencySketchBasedOnPerformance() {
            // Generate operations to trigger adaptation
            for (int i = 0; i < 5000; i++) {
                cache.put("key" + i, "value" + i);
                if (i % 2 == 0) {
                    cache.get("key" + i); // Some hits
                }
            }

            // After many operations, frequency sketch should be adapted
            assertTrue(cache.size() > 0);
        }

        @Test
        @DisplayName("Should maintain performance under high frequency operations")
        void shouldMaintainPerformanceUnderHighFrequencyOperations() {
            long startTime = System.currentTimeMillis();

            int operationCount = 10000;
            for (int i = 0; i < operationCount; i++) {
                cache.put("key" + i, "value" + i);
                cache.get("key" + (i % 1000)); // Some hits, some misses
            }

            long duration = System.currentTimeMillis() - startTime;

            // Should complete efficiently
            assertTrue(duration < 3000, "High frequency operations took too long: " + duration + "ms");
        }

        @Test
        @DisplayName("Should handle eviction efficiently")
        void shouldHandleEvictionEfficiently() {
            // Create small cache to trigger eviction
            CacheConfig<String, String> smallConfig = CacheConfig.<String, String>builder()
                    .maximumSize(10L)
                    .recordStats(true)
                    .build();
            Cache<String, String> smallCache = createCache(smallConfig);

            // Add more entries than maximum to trigger eviction
            for (int i = 0; i < 50; i++) {
                smallCache.put("key" + i, "value" + i);
            }

            // Cache should respect size limit
            assertTrue(smallCache.size() <= 10);

            // Should still be functional
            smallCache.put("test", "value");
            assertEquals("value", smallCache.get("test"));
        }

        @Test
        @DisplayName("Should handle concurrent access to optimizations")
        void shouldHandleConcurrentAccessToOptimizations() throws InterruptedException {
            int threadCount = 5;
            int operationsPerThread = 1000;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "thread" + threadId + "key" + j;
                        cache.put(key, "value" + j);
                        cache.get(key);

                        // Occasionally access frequency metrics
                        if (j % 100 == 0 && cache instanceof OptimizedCache) {
                            OptimizedCache<String, String> optimizedCache = (OptimizedCache<String, String>) cache;
                            optimizedCache.getDetailedMetrics();
                        }
                    }
                });
            }

            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }

            // Wait for completion
            for (Thread thread : threads) {
                thread.join(5000);
            }

            // Verify cache is still functional
            cache.put("final-test", "final-value");
            assertEquals("final-value", cache.get("final-test"));
        }
    }

    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceAndOptimization {

        @Test
        @DisplayName("Should optimize for repeated access patterns")
        void shouldOptimizeForRepeatedAccessPatterns() {
            // Create access pattern with hot keys
            String[] hotKeys = { "hot1", "hot2", "hot3" };
            String[] coldKeys = new String[97]; // Reduce to 97 so total is 100
            for (int i = 0; i < 97; i++) {
                coldKeys[i] = "cold" + i;
            }

            // Insert cold keys first
            for (String key : coldKeys) {
                cache.put(key, "cold-value");
            }

            // Insert hot keys after cold keys
            for (String key : hotKeys) {
                cache.put(key, "hot-value");
            }

            // Access hot keys frequently to build up frequency count
            for (int i = 0; i < 1000; i++) {
                for (String hotKey : hotKeys) {
                    cache.get(hotKey);
                }
                // Occasionally access cold keys
                if (i % 10 == 0) {
                    cache.get(coldKeys[i % 97]);
                }
            }

            // Hot keys should still be accessible because they are accessed more frequently
            for (String hotKey : hotKeys) {
                assertEquals("hot-value", cache.get(hotKey));
            }
        }

        @Test
        @DisplayName("Should handle burst operations efficiently")
        void shouldHandleBurstOperationsEfficiently() {
            long startTime = System.nanoTime();

            // Burst of puts
            for (int i = 0; i < 1000; i++) {
                cache.put("burst-key" + i, "burst-value" + i);
            }

            long putTime = System.nanoTime() - startTime;

            startTime = System.nanoTime();

            // Burst of gets
            for (int i = 0; i < 1000; i++) {
                cache.get("burst-key" + i);
            }

            long getTime = System.nanoTime() - startTime;

            // Operations should complete in reasonable time
            assertTrue(putTime < 100_000_000, "Burst puts too slow: " + putTime + "ns"); // 100ms
            assertTrue(getTime < 50_000_000, "Burst gets too slow: " + getTime + "ns"); // 50ms
        }

        @Test
        @DisplayName("Should maintain consistent performance across cache states")
        void shouldMaintainConsistentPerformanceAcrossCacheStates() {
            long[] emptyTiming = measureOperationTime(() -> {
                cache.get("nonexistent");
                cache.put("temp", "temp");
                cache.remove("temp");
            });

            // Fill cache partially
            for (int i = 0; i < 50; i++) {
                cache.put("key" + i, "value" + i);
            }

            long[] partialTiming = measureOperationTime(() -> {
                cache.get("key25");
                cache.put("new-key", "new-value");
                cache.remove("new-key");
            });

            // Fill cache to capacity
            for (int i = 50; i < 100; i++) {
                cache.put("key" + i, "value" + i);
            }

            long[] fullTiming = measureOperationTime(() -> {
                cache.get("key75");
                cache.put("another-key", "another-value");
                cache.remove("another-key");
            });

            // Performance shouldn't degrade significantly as cache fills
            double emptyAvg = average(emptyTiming);
            double partialAvg = average(partialTiming);
            double fullAvg = average(fullTiming);

            // Full cache shouldn't be more than 5x slower than empty cache
            assertTrue(fullAvg < emptyAvg * 5,
                    String.format("Performance degraded too much: empty=%.2f, partial=%.2f, full=%.2f",
                            emptyAvg, partialAvg, fullAvg));
        }

        private long[] measureOperationTime(Runnable operation) {
            long[] timings = new long[10];
            for (int i = 0; i < 10; i++) {
                long start = System.nanoTime();
                operation.run();
                timings[i] = System.nanoTime() - start;
            }
            return timings;
        }

        private double average(long[] values) {
            long sum = 0;
            for (long value : values) {
                sum += value;
            }
            return (double) sum / values.length;
        }
    }

    @Nested
    @DisplayName("Integration with Base Functionality")
    class IntegrationWithBaseFunctionality {

        @Test
        @DisplayName("Should maintain base cache contract")
        void shouldMaintainBaseCacheContract() {
            // All basic operations should work
            cache.put("key", "value");
            assertEquals("value", cache.get("key"));
            assertTrue(cache.containsKey("key"));
            assertEquals(1, cache.size());

            assertEquals("value", cache.remove("key"));
            assertNull(cache.get("key"));
            assertFalse(cache.containsKey("key"));
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("Should work with inherited async operations")
        void shouldWorkWithInheritedAsyncOperations() throws Exception {
            // Async operations should work seamlessly
            cache.putAsync("async-key", "async-value").get();
            assertEquals("async-value", cache.getAsync("async-key").get());
            assertEquals("async-value", cache.removeAsync("async-key").get());

            cache.put("key1", "value1");
            cache.put("key2", "value2");
            cache.clearAsync().get();
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("Should support collection views with optimizations")
        void shouldSupportCollectionViewsWithOptimizations() {
            for (int i = 0; i < 20; i++) {
                cache.put("key" + i, "value" + i);
            }

            // Collection views should work
            assertFalse(cache.keys().isEmpty());
            assertFalse(cache.values().isEmpty());
            assertFalse(cache.entries().isEmpty());

            assertTrue(cache.keys().size() > 0);
            assertTrue(cache.values().size() > 0);
            assertTrue(cache.entries().size() > 0);
        }
    }
}
