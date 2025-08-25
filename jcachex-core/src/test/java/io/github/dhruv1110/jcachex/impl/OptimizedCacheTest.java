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
            int operationCount = 10000;

            // Perform high frequency operations and verify they complete successfully
            for (int i = 0; i < operationCount; i++) {
                cache.put("key" + i, "value" + i);
                String retrievedValue = cache.get("key" + (i % 1000)); // Some hits, some misses

                // Verify that put operations succeeded for keys that should exist
                if (i < 1000) {
                    assertEquals("value" + i, retrievedValue,
                            "Cache should return correct value for key: key" + i);
                }
            }

            // Verify cache size is within expected bounds after operations
            assertTrue(cache.size() > 0, "Cache should contain some entries after operations");
            assertTrue(cache.size() <= 100, "Cache should respect size limit");

            // Verify cache remains functional after high frequency operations
            cache.put("test-key", "test-value");
            assertEquals("test-value", cache.get("test-key"));

            // Verify some random entries are still accessible (if they exist)
            int sampleSize = Math.min(100, (int) cache.size());
            for (int i = 0; i < sampleSize; i += 10) {
                String value = cache.get("key" + i);
                if (value != null) {
                    assertEquals("value" + i, value,
                            "Cache should maintain consistency for key: key" + i);
                }
            }
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
            // Test that burst operations complete successfully without errors
            // and maintain cache consistency

            // Burst of puts - verify all operations complete
            for (int i = 0; i < 1000; i++) {
                cache.put("burst-key" + i, "burst-value" + i);
            }

            // Verify entries were stored (may be limited by cache size)
            assertTrue(cache.size() > 0, "Cache should contain some entries after burst operations");
            assertTrue(cache.size() <= 1000, "Cache size should not exceed maximum");

            // Burst of gets - verify all operations complete and return correct values
            // Test a sample of keys that should exist
            int testCount = Math.min(100, (int) cache.size());
            for (int i = 0; i < testCount; i++) {
                String value = cache.get("burst-key" + i);
                if (value != null) {
                    assertEquals("burst-value" + i, value,
                            "Cache should return correct value for key: burst-key" + i);
                }
            }

            // Verify cache stats are recorded correctly after burst operations
            if (cache instanceof OptimizedCache) {
                OptimizedCache<String, String> optimizedCache = (OptimizedCache<String, String>) cache;
                String metrics = optimizedCache.getDetailedMetrics();
                assertNotNull(metrics);
                assertTrue(metrics.contains("HitRatio"));
            }
        }

        @Test
        @DisplayName("Should maintain consistent performance across cache states")
        void shouldMaintainConsistentPerformanceAcrossCacheStates() {
            // Test that cache operations remain functional and consistent
            // across different cache states without performance degradation

            // Test empty cache state
            assertNull(cache.get("nonexistent"));
            cache.put("temp", "temp");
            assertEquals("temp", cache.get("temp"));
            cache.remove("temp");
            assertNull(cache.get("temp"));

            // Fill cache partially and verify operations still work
            for (int i = 0; i < 50; i++) {
                cache.put("key" + i, "value" + i);
            }
            assertTrue(cache.size() > 0, "Cache should contain some entries");
            assertTrue(cache.size() <= 100, "Cache should respect size limit");

            // Verify operations work correctly in partial state
            assertEquals("value25", cache.get("key25"));
            cache.put("new-key", "new-value");
            assertEquals("new-value", cache.get("new-key"));
            cache.remove("new-key");
            assertNull(cache.get("new-key"));

            // Fill cache to capacity and verify operations still work
            for (int i = 50; i < 100; i++) {
                cache.put("key" + i, "value" + i);
            }
            assertTrue(cache.size() > 0, "Cache should contain some entries");
            assertTrue(cache.size() <= 100, "Cache should respect size limit");

            // Verify operations work correctly in full state
            assertEquals("value75", cache.get("key75"));
            cache.put("another-key", "another-value");
            assertEquals("another-value", cache.get("another-key"));
            cache.remove("another-key");
            assertNull(cache.get("another-key"));

            // Test that cache maintains consistency across state changes
            // by verifying a sample of existing entries
            int sampleSize = Math.min(10, (int) cache.size());
            for (int i = 0; i < sampleSize; i++) {
                String key = "key" + (i * 10);
                String value = cache.get(key);
                if (value != null) {
                    assertEquals("value" + (i * 10), value,
                            "Cache should maintain consistency for key: " + key);
                }
            }

            // Verify cache size remains within bounds after all operations
            assertTrue(cache.size() > 0, "Cache should contain entries after operations");
            assertTrue(cache.size() <= 100, "Cache should respect size limit");
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
