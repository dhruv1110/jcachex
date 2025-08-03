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
 * Test suite for ProfiledOptimizedCache.
 * Tests profiling, development optimizations, and performance monitoring.
 */
class ProfiledOptimizedCacheTest {

    private ProfiledOptimizedCache<String, String> cache;
    private CacheConfig<String, String> config;

    @BeforeEach
    void setUp() {
        config = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .recordStats(true)
                .build();
        cache = new ProfiledOptimizedCache<>(config);
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
        assertEquals(3, cache.size());
        assertEquals("value2", cache.get("key2"));
        assertEquals("value3", cache.get("key3"));
    }

    @Test
    void testRemoveOperations() {
        // Test remove on empty cache
        assertNull(cache.remove("nonexistent"));

        // Test remove with existing key
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        assertEquals(2, cache.size());

        String removed = cache.remove("key1");
        assertEquals("value1", removed);
        assertEquals(1, cache.size());
        assertNull(cache.get("key1"));
        assertEquals("value2", cache.get("key2"));

        // Test remove nonexistent key
        assertNull(cache.remove("nonexistent"));
        assertEquals(1, cache.size());
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
        assertEquals(3, cache.size());

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
    void testProfilingFeatures() {
        // Test profiling features with various operations
        for (int i = 0; i < 50; i++) {
            cache.put("profile_key" + i, "profile_value" + i);
        }

        // Test profiling during access patterns
        for (int i = 0; i < 50; i++) {
            cache.get("profile_key" + i);
        }

        // Profiling should not affect basic functionality
        assertEquals(50, cache.size());

        // Test updates with profiling
        for (int i = 0; i < 25; i++) {
            cache.put("profile_key" + i, "updated_profile_value" + i);
        }

        // Verify updates worked
        for (int i = 0; i < 25; i++) {
            assertEquals("updated_profile_value" + i, cache.get("profile_key" + i));
        }
    }

    @Test
    void testPerformanceMonitoring() {
        // Test performance monitoring capabilities
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            cache.put("perf_key" + i, "perf_value" + i);
            cache.get("perf_key" + i);
        }

        long endTime = System.currentTimeMillis();

        // Performance monitoring should not significantly impact performance
        assertTrue(endTime - startTime < 5000, "Operations should complete in reasonable time");

        // Verify all operations worked correctly
        for (int i = 0; i < 100; i++) {
            assertEquals("perf_value" + i, cache.get("perf_key" + i));
        }
    }

    @Test
    void testDebuggingFeatures() {
        // Test debugging features
        cache.put("debug_key1", "debug_value1");
        cache.put("debug_key2", "debug_value2");
        cache.put("debug_key3", "debug_value3");

        // Debugging should not affect correctness
        assertEquals("debug_value1", cache.get("debug_key1"));
        assertEquals("debug_value2", cache.get("debug_key2"));
        assertEquals("debug_value3", cache.get("debug_key3"));

        // Test debugging during modifications
        cache.put("debug_key1", "updated_debug_value1");
        assertEquals("updated_debug_value1", cache.get("debug_key1"));

        assertEquals(3, cache.size());
    }

    @Test
    void testStatisticalAnalysis() {
        // Test statistical analysis features
        for (int i = 0; i < 50; i++) {
            cache.put("stats_key" + i, "stats_value" + i);
        }

        // Create access patterns for statistical analysis
        for (int cycle = 0; cycle < 3; cycle++) {
            for (int i = 0; i < 20; i++) {
                cache.get("stats_key" + i);
            }
        }

        // Statistical analysis should not break functionality
        assertTrue(cache.size() <= 100);

        // Test that frequently accessed items are still available
        for (int i = 0; i < 20; i++) {
            assertEquals("stats_value" + i, cache.get("stats_key" + i));
        }
    }

    @Test
    void testInstrumentation() {
        // Test instrumentation capabilities
        for (int i = 0; i < 30; i++) {
            cache.put("instr_key" + i, "instr_value" + i);
        }

        // Instrumentation during various operations
        for (int i = 0; i < 30; i++) {
            cache.get("instr_key" + i);
        }

        // Remove some entries
        for (int i = 0; i < 10; i++) {
            cache.remove("instr_key" + i);
        }

        // Instrumentation should maintain correct state
        assertEquals(20, cache.size());

        // Verify remaining entries
        for (int i = 10; i < 30; i++) {
            assertEquals("instr_value" + i, cache.get("instr_key" + i));
        }
    }

    @Test
    void testDevelopmentOptimizations() {
        // Test development-time optimizations
        for (int i = 0; i < 100; i++) {
            cache.put("dev_key" + i, "dev_value" + i);
        }

        // Development optimizations should respect size limits
        assertTrue(cache.size() <= 100);

        // Test continued functionality with development features
        cache.put("dev_test_key", "dev_test_value");
        assertEquals("dev_test_value", cache.get("dev_test_key"));
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
        assertEquals(2, cache.size());

        CompletableFuture<Void> clearFuture = cache.clearAsync();
        clearFuture.get();
        assertEquals(0, cache.size());
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Test concurrent access with profiling
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

        // Profiling should handle concurrent access
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
        assertEquals(3, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertTrue(keys.contains("key3"));

        Collection<String> values = cache.values();
        assertEquals(3, values.size());
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
        assertTrue(values.contains("value3"));

        Set<Map.Entry<String, String>> entries = cache.entries();
        assertEquals(3, entries.size());

        // Verify entries contain correct key-value pairs
        boolean foundEntry1 = false, foundEntry2 = false, foundEntry3 = false;
        for (Map.Entry<String, String> entry : entries) {
            if (entry.getKey().equals("key1") && entry.getValue().equals("value1")) {
                foundEntry1 = true;
            } else if (entry.getKey().equals("key2") && entry.getValue().equals("value2")) {
                foundEntry2 = true;
            } else if (entry.getKey().equals("key3") && entry.getValue().equals("value3")) {
                foundEntry3 = true;
            }
        }
        assertTrue(foundEntry1 && foundEntry2 && foundEntry3, "All entries should be found");
    }

    @Test
    void testStatistics() {
        // Test stats when disabled
        CacheConfig<String, String> configNoStats = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .recordStats(false)
                .build();
        ProfiledOptimizedCache<String, String> cacheNoStats = new ProfiledOptimizedCache<>(configNoStats);

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
        assertTrue(stats.getHitCount().get() >= 2);
        assertTrue(stats.getMissCount().get() >= 2);
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
        // Test size management with profiling
        for (int i = 0; i < 120; i++) {
            cache.put("size_key" + i, "size_value" + i);
        }

        // Cache should respect maximum size even with profiling
        assertTrue(cache.size() <= 100);

        // Recent entries should be more likely to remain
        boolean foundRecentEntry = false;
        for (int i = 110; i < 120; i++) {
            if (cache.get("size_key" + i) != null) {
                foundRecentEntry = true;
                break;
            }
        }
        assertTrue(foundRecentEntry, "Should find at least one recent entry");
    }

    @Test
    void testProfiledEviction() {
        // Test profiled eviction behavior
        for (int i = 0; i < 150; i++) {
            cache.put("evict_key" + i, "evict_value" + i);

            // Create access patterns for profiling
            if (i % 2 == 0) {
                cache.get("evict_key" + i);
            }
        }

        // Should respect size limit with profiled eviction
        assertTrue(cache.size() <= 100);

        // Should remain functional
        cache.put("evict_test", "evict_test_value");
        assertEquals("evict_test_value", cache.get("evict_test"));
    }

    @Test
    void testProfilingOverhead() {
        // Test that profiling overhead is acceptable
        long startTime = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            cache.put("overhead_key" + i, "overhead_value" + i);
            cache.get("overhead_key" + i);
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        // Profiling overhead should be reasonable (less than 1 second for 1000 ops)
        assertTrue(duration < 1_000_000_000L, "Profiling overhead should be acceptable");

        // Verify correctness
        assertTrue(cache.size() <= 100);
    }

    @Test
    void testDevelopmentMode() {
        // Test development mode features
        cache.put("dev_mode1", "dev_value1");
        cache.put("dev_mode2", "dev_value2");
        cache.put("dev_mode3", "dev_value3");

        // Development mode should maintain correctness
        assertEquals("dev_value1", cache.get("dev_mode1"));
        assertEquals("dev_value2", cache.get("dev_mode2"));
        assertEquals("dev_value3", cache.get("dev_mode3"));

        // Test modifications in development mode
        cache.put("dev_mode1", "updated_dev_value1");
        assertEquals("updated_dev_value1", cache.get("dev_mode1"));

        assertEquals(3, cache.size());
    }

    @Test
    void testProfilingMetrics() {
        // Test profiling metrics collection
        for (int i = 0; i < 50; i++) {
            cache.put("metrics_key" + i, "metrics_value" + i);
        }

        // Generate various access patterns for metrics
        for (int i = 0; i < 25; i++) {
            cache.get("metrics_key" + i);
        }

        // Profiling metrics should not affect functionality
        assertEquals(50, cache.size());

        // Test that metrics collection doesn't break operations
        cache.put("metrics_test", "metrics_test_value");
        assertEquals("metrics_test_value", cache.get("metrics_test"));
    }

    @Test
    void testDebuggingSupport() {
        // Test debugging support features
        for (int i = 0; i < 20; i++) {
            cache.put("debug_support_key" + i, "debug_support_value" + i);
        }

        // Debugging should provide insights without breaking functionality
        for (int i = 0; i < 20; i++) {
            assertEquals("debug_support_value" + i, cache.get("debug_support_key" + i));
        }

        // Test removal with debugging
        for (int i = 0; i < 10; i++) {
            cache.remove("debug_support_key" + i);
        }

        assertEquals(10, cache.size());

        // Verify remaining entries
        for (int i = 10; i < 20; i++) {
            assertEquals("debug_support_value" + i, cache.get("debug_support_key" + i));
        }
    }

    @Test
    void testProfiledCacheIntegrity() {
        // Test that profiling maintains cache integrity
        for (int i = 0; i < 100; i++) {
            cache.put("integrity_key" + i, "integrity_value" + i);
        }

        // Verify all entries initially
        for (int i = 0; i < 100; i++) {
            assertEquals("integrity_value" + i, cache.get("integrity_key" + i));
        }

        // Update some entries
        for (int i = 0; i < 50; i++) {
            cache.put("integrity_key" + i, "updated_integrity_value" + i);
        }

        // Verify updates and untouched entries
        for (int i = 0; i < 50; i++) {
            assertEquals("updated_integrity_value" + i, cache.get("integrity_key" + i));
        }
        for (int i = 50; i < 100; i++) {
            assertEquals("integrity_value" + i, cache.get("integrity_key" + i));
        }

        assertTrue(cache.size() <= 100);
    }
}
