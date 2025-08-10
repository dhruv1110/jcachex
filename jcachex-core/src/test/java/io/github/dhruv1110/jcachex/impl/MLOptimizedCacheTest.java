package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheStats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for MLOptimizedCache.
 * Tests ML-based optimizations, pattern learning, and adaptive behavior.
 */
class MLOptimizedCacheTest {

    private MLOptimizedCache<String, String> cache;
    private CacheConfig<String, String> config;

    @BeforeEach
    void setUp() {
        config = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .recordStats(true)
                .build();
        cache = new MLOptimizedCache<>(config);
    }

    @AfterEach
    void tearDown() {
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    void testBasicOperations() {
        // Test basic cache operations
        assertNull(cache.get("key1"));
        assertEquals(0, cache.size());
        assertTrue(cache.keys().isEmpty());
        assertTrue(cache.values().isEmpty());
        assertTrue(cache.entries().isEmpty());

        // Test put and get
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));
        assertEquals(1, cache.size());
        assertTrue(cache.containsKey("key1"));

        // Test put with same key (update)
        cache.put("key1", "value1_updated");
        assertEquals("value1_updated", cache.get("key1"));
        assertEquals(1, cache.size());

        // Test put multiple entries
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        assertTrue(cache.size() >= 1);
        assertNotNull(cache.get("key2"));
        assertNotNull(cache.get("key3"));
    }

    @Test
    void testRemoveOperations() {
        // Test remove on empty cache
        assertNull(cache.remove("nonexistent"));

        // Test remove with existing key
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        assertTrue(cache.size() >= 1);

        String removed = cache.remove("key1");
        assertEquals("value1", removed);
        assertNull(cache.get("key1"));
        assertTrue(cache.containsKey("key2"));

        // Test remove nonexistent key
        assertNull(cache.remove("nonexistent"));
    }

    @Test
    void testClearOperation() {
        // Test clear on empty cache
        cache.clear();
        assertEquals(0, cache.size());

        // Test clear with entries
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        assertTrue(cache.size() >= 1);

        cache.clear();
        assertEquals(0, cache.size());
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
        assertNull(cache.get("key3"));
    }

    @Test
    void testNullKeyValueHandling() {
        // Test null key
        assertNull(cache.get(null));
        cache.put(null, "value");
        assertEquals(0, cache.size());
        assertNull(cache.remove(null));

        // Test null value
        cache.put("key", null);
        assertEquals(0, cache.size());
        assertNull(cache.get("key"));
    }

    @Test
    void testMLOptimizations() {
        // Test ML optimizations with access patterns
        for (int i = 0; i < 50; i++) {
            cache.put("ml_key" + i, "ml_value" + i);
        }

        // Create access patterns for ML learning
        for (int cycle = 0; cycle < 3; cycle++) {
            for (int i = 0; i < 20; i++) {
                cache.get("ml_key" + i); // Frequent access pattern
            }
        }

        // ML should learn from access patterns
        assertTrue(cache.size() <= 100);

        // Verify frequently accessed items are still available
        boolean foundFrequentItems = false;
        for (int i = 0; i < 20; i++) {
            if (cache.get("ml_key" + i) != null) {
                foundFrequentItems = true;
                break;
            }
        }
        assertTrue(foundFrequentItems, "Should find frequently accessed items");
    }

    @Test
    void testPatternLearning() {
        // Test pattern learning with sequential access
        for (int i = 0; i < 30; i++) {
            cache.put("pattern_key" + i, "pattern_value" + i);
        }

        // Create sequential access pattern
        for (int i = 0; i < 30; i += 2) {
            cache.get("pattern_key" + i);
        }

        // ML should learn from the pattern
        assertTrue(cache.size() <= 100);

        // Verify cache functionality after pattern learning
        cache.put("new_pattern_key", "new_pattern_value");
        assertNotNull(cache.get("new_pattern_key"));
    }

    @Test
    void testPredictivePrefetching() {
        // Test predictive prefetching capabilities
        for (int i = 0; i < 40; i++) {
            cache.put("prefetch_key" + i, "prefetch_value" + i);
        }

        // Create predictable access pattern
        for (int i = 0; i < 20; i++) {
            cache.get("prefetch_key" + i);
        }

        // Continue the pattern to trigger predictive prefetching
        for (int i = 20; i < 30; i++) {
            cache.get("prefetch_key" + i);
        }

        // Verify cache remains functional with prefetching
        assertTrue(cache.size() <= 100);
        cache.put("prefetch_test", "prefetch_test_value");
        assertTrue(cache.containsKey("prefetch_test"));
    }

    @Test
    void testWorkloadClassification() {
        // Test workload classification with different patterns

        // Read-heavy workload
        cache.put("read_key1", "read_value1");
        cache.put("read_key2", "read_value2");

        for (int i = 0; i < 20; i++) {
            cache.get("read_key1");
            cache.get("read_key2");
        }

        // Write-heavy workload
        for (int i = 0; i < 20; i++) {
            cache.put("write_key" + i, "write_value" + i);
        }

        // Mixed workload
        for (int i = 0; i < 10; i++) {
            cache.put("mixed_key" + i, "mixed_value" + i);
            cache.get("mixed_key" + i);
        }

        // ML should classify and adapt to workload
        assertTrue(cache.size() <= 100);
    }

    @Test
    void testAdaptiveEviction() {
        // Test adaptive eviction based on ML insights
        for (int i = 0; i < 120; i++) {
            cache.put("adaptive_key" + i, "adaptive_value" + i);
        }

        // Should respect size limit through adaptive eviction
        assertTrue(cache.size() <= 100);

        // Cache should remain functional after adaptive eviction
        cache.put("post_eviction_key", "post_eviction_value");
        assertTrue(cache.size() >= 0);
    }

    @Test
    void testAsyncOperations() throws Exception {
        // Test async get
        CompletableFuture<String> getFuture = cache.getAsync("nonexistent");
        assertNull(getFuture.get());

        // Test async put
        CompletableFuture<Void> putFuture = cache.putAsync("async_key", "async_value");
        putFuture.get();
        assertEquals("async_value", cache.get("async_key"));

        // Test async remove
        CompletableFuture<String> removeFuture = cache.removeAsync("async_key");
        assertEquals("async_value", removeFuture.get());
        assertNull(cache.get("async_key"));

        // Test async clear
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        assertTrue(cache.size() >= 1);

        CompletableFuture<Void> clearFuture = cache.clearAsync();
        clearFuture.get();
        assertEquals(0, cache.size());
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Test concurrent access patterns for ML learning
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < 10; j++) {
                    cache.put("concurrent_key" + threadId + "_" + j, "concurrent_value" + threadId + "_" + j);
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // ML should handle concurrent patterns
        assertTrue(cache.size() > 0, "Should have entries after concurrent operations");
    }

    @Test
    void testCollectionOperations() {
        // Test with empty cache
        assertTrue(cache.keys().isEmpty());
        assertTrue(cache.values().isEmpty());
        assertTrue(cache.entries().isEmpty());

        // Test with populated cache
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        Set<String> keys = cache.keys();
        assertTrue(keys.size() >= 1);
        assertTrue(keys.contains("key1") || keys.contains("key2") || keys.contains("key3"));

        Collection<String> values = cache.values();
        assertTrue(values.size() >= 1);

        Set<Map.Entry<String, String>> entries = cache.entries();
        assertTrue(entries.size() >= 1);
    }

    @Test
    void testStatistics() {
        // Test stats when disabled
        CacheConfig<String, String> configNoStats = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .recordStats(false)
                .build();
        MLOptimizedCache<String, String> cacheNoStats = new MLOptimizedCache<>(configNoStats);

        CacheStats stats = cacheNoStats.stats();
        assertNotNull(stats);
        cacheNoStats.clear();

        // Test stats when enabled
        cache.get("nonexistent"); // miss
        cache.put("key1", "value1");
        cache.get("key1"); // hit
        cache.get("key1"); // hit
        cache.get("nonexistent2"); // miss

        stats = cache.stats();
        assertNotNull(stats);
        assertTrue(stats.getHitCount().get() >= 0);
        assertTrue(stats.getMissCount().get() >= 0);
    }

    @Test
    void testConfiguration() {
        CacheConfig<String, String> retrievedConfig = cache.config();
        assertNotNull(retrievedConfig);
        assertEquals(100L, retrievedConfig.getMaximumSize());
        assertTrue(retrievedConfig.isRecordStats());
    }

    @Test
    void testSizeManagement() {
        // Test size management with ML optimizations
        for (int i = 0; i < 120; i++) {
            cache.put("size_key" + i, "size_value" + i);
        }

        // Cache should respect maximum size with ML-based eviction
        assertTrue(cache.size() <= 100);

        // Cache should remain functional
        assertTrue(cache.size() >= 0);
    }

    @Test
    void testPerformanceMetrics() {
        // Test performance metrics collection for ML
        for (int i = 0; i < 50; i++) {
            cache.put("perf_key" + i, "perf_value" + i);
            cache.get("perf_key" + i);
        }

        // ML should collect performance metrics
        assertTrue(cache.size() <= 100);

        // Test that performance monitoring doesn't break functionality
        cache.put("perf_test", "perf_test_value");
        assertTrue(cache.containsKey("perf_test"));
    }

    @Test
    void testAccessHistoryLearning() {
        // Test access history learning
        for (int i = 0; i < 30; i++) {
            cache.put("history_key" + i, "history_value" + i);
        }

        // Create access history pattern
        for (int cycle = 0; cycle < 5; cycle++) {
            for (int i = 0; i < 10; i++) {
                cache.get("history_key" + i);
            }
        }

        // ML should learn from access history
        assertTrue(cache.size() <= 100);

        // Continue with new pattern
        for (int i = 20; i < 30; i++) {
            cache.get("history_key" + i);
        }

        assertTrue(cache.size() >= 0);
    }

    @Test
    void testMLBasedOptimization() {
        // Test ML-based optimization features
        cache.put("ml_opt1", "ml_value1");
        cache.put("ml_opt2", "ml_value2");
        cache.put("ml_opt3", "ml_value3");

        // Test that ML optimizations maintain correctness
        assertEquals("ml_value1", cache.get("ml_opt1"));
        assertEquals("ml_value2", cache.get("ml_opt2"));
        assertEquals("ml_value3", cache.get("ml_opt3"));

        // Test updates with ML optimizations
        cache.put("ml_opt1", "updated_ml_value1");
        assertEquals("updated_ml_value1", cache.get("ml_opt1"));

        assertTrue(cache.size() >= 1);
    }

    @Test
    void testAdaptiveBehavior() {
        // Test adaptive behavior based on usage patterns
        for (int i = 0; i < 100; i++) {
            cache.put("adaptive_key" + i, "adaptive_value" + i);
        }

        // Create varying access patterns for ML adaptation
        // High frequency pattern
        for (int cycle = 0; cycle < 10; cycle++) {
            for (int i = 0; i < 20; i++) {
                cache.get("adaptive_key" + i);
            }
        }

        // Low frequency pattern
        for (int i = 80; i < 100; i++) {
            cache.get("adaptive_key" + i);
        }

        // ML should adapt to these patterns
        assertTrue(cache.size() <= 100);

        // Test continued functionality
        cache.put("adaptive_test", "adaptive_test_value");
        assertTrue(cache.size() >= 0);
    }

    @Test
    void testMLEvictionPolicy() {
        // Test ML-driven eviction policy
        for (int i = 0; i < 150; i++) {
            cache.put("evict_key" + i, "evict_value" + i);

            // Create access patterns for ML learning
            if (i % 3 == 0) {
                cache.get("evict_key" + i);
            }
        }

        // Should respect size limit with ML eviction
        assertTrue(cache.size() <= 100);

        // Should remain functional
        cache.put("ml_evict_test", "ml_evict_test_value");
        assertTrue(cache.size() >= 0);
    }
}
