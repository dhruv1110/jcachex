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
 * Test suite for HardwareOptimizedCache.
 * Tests hardware-specific optimizations, SIMD operations, and bulk operations.
 */
class HardwareOptimizedCacheTest {

    private HardwareOptimizedCache<String, String> cache;
    private CacheConfig<String, String> config;

    @BeforeEach
    void setUp() {
        config = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .recordStats(true)
                .build();
        cache = new HardwareOptimizedCache<>(config);
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
    void testBulkOperations() {
        // Test bulk get operation
        String[] keys = new String[] { "bulk_key1", "bulk_key2", "bulk_key3", "nonexistent" };

        // Add some entries
        cache.put("bulk_key1", "bulk_value1");
        cache.put("bulk_key2", "bulk_value2");
        cache.put("bulk_key3", "bulk_value3");

        // Test bulk get
        Map<String, String> result = cache.getAll(keys);
        assertNotNull(result);
        assertEquals("bulk_value1", result.get("bulk_key1"));
        assertEquals("bulk_value2", result.get("bulk_key2"));
        assertEquals("bulk_value3", result.get("bulk_key3"));
        assertNull(result.get("nonexistent"));

        // Test bulk put
        String[] newKeys = new String[] { "bulk_key4", "bulk_key5", "bulk_key6" };
        String[] newValues = new String[] { "bulk_value4", "bulk_value5", "bulk_value6" };

        cache.putAll(newKeys, newValues);

        // Verify bulk put worked
        assertEquals("bulk_value4", cache.get("bulk_key4"));
        assertEquals("bulk_value5", cache.get("bulk_key5"));
        assertEquals("bulk_value6", cache.get("bulk_key6"));
        assertEquals(6, cache.size());
    }

    @Test
    void testHardwareOptimizations() {
        // Test that hardware optimizations don't break basic functionality
        for (int i = 0; i < 50; i++) {
            cache.put("hw_key" + i, "hw_value" + i);
        }

        // Test sequential access (good for prefetching)
        for (int i = 0; i < 50; i++) {
            assertEquals("hw_value" + i, cache.get("hw_key" + i));
        }

        // Test random access patterns
        for (int i = 49; i >= 0; i -= 2) {
            assertEquals("hw_value" + i, cache.get("hw_key" + i));
        }

        assertEquals(50, cache.size());
    }

    @Test
    void testSIMDOperations() {
        // Test SIMD-optimized operations with bulk operations
        int batchSize = 16; // Common SIMD batch size
        String[] keys = new String[batchSize];
        String[] values = new String[batchSize];

        // Create batch data
        for (int i = 0; i < batchSize; i++) {
            keys[i] = "simd_key" + i;
            values[i] = "simd_value" + i;
        }

        // Test SIMD bulk put
        cache.putAll(keys, values);

        // Verify all entries were stored
        for (int i = 0; i < batchSize; i++) {
            assertEquals("simd_value" + i, cache.get("simd_key" + i));
        }

        // Test SIMD bulk get
        Map<String, String> result = cache.getAll(keys);
        assertEquals(batchSize, result.size());
        for (int i = 0; i < batchSize; i++) {
            assertEquals("simd_value" + i, result.get("simd_key" + i));
        }
    }

    @Test
    void testCacheLineOptimization() {
        // Test cache line optimization with sequential access
        for (int i = 0; i < 64; i++) { // 64 entries to fill cache lines
            cache.put("line_key" + i, "line_value" + i);
        }

        // Access in cache-line friendly patterns
        for (int i = 0; i < 64; i += 8) { // Access every 8th entry
            for (int j = 0; j < 8; j++) {
                assertEquals("line_value" + (i + j), cache.get("line_key" + (i + j)));
            }
        }

        assertEquals(64, cache.size());
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

        // Test concurrent access patterns
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < 10; j++) {
                    cache.put("thread" + threadId + "_key" + j, "thread" + threadId + "_value" + j);
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // Verify some entries exist after concurrent access
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
        HardwareOptimizedCache<String, String> cacheNoStats = new HardwareOptimizedCache<>(configNoStats);

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
        // Test size management with hardware optimizations
        for (int i = 0; i < 120; i++) {
            cache.put("size_key" + i, "size_value" + i);
        }

        // Cache should respect maximum size
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
    void testHardwareSpecificFeatures() {
        // Test hardware-specific optimizations
        cache.put("hw_feature1", "hw_value1");
        cache.put("hw_feature2", "hw_value2");
        cache.put("hw_feature3", "hw_value3");

        // Test that hardware optimizations maintain correctness
        assertEquals("hw_value1", cache.get("hw_feature1"));
        assertEquals("hw_value2", cache.get("hw_feature2"));
        assertEquals("hw_value3", cache.get("hw_feature3"));

        // Test bulk operations with hardware features
        String[] keys = { "hw_feature1", "hw_feature2", "hw_feature3" };
        Map<String, String> result = cache.getAll(keys);

        assertEquals(3, result.size());
        assertEquals("hw_value1", result.get("hw_feature1"));
        assertEquals("hw_value2", result.get("hw_feature2"));
        assertEquals("hw_value3", result.get("hw_feature3"));
    }

    @Test
    void testPrefetchingOptimizations() {
        // Test prefetching optimizations with sequential access
        for (int i = 0; i < 32; i++) {
            cache.put("prefetch_key" + i, "prefetch_value" + i);
        }

        // Sequential access should trigger prefetching
        for (int i = 0; i < 32; i++) {
            assertEquals("prefetch_value" + i, cache.get("prefetch_key" + i));
        }

        // Test stride access patterns
        for (int i = 0; i < 32; i += 4) {
            assertEquals("prefetch_value" + i, cache.get("prefetch_key" + i));
        }

        assertEquals(32, cache.size());
    }

    @Test
    void testBulkOperationEdgeCases() {
        // Test bulk operations with edge cases

        // Empty arrays
        Map<String, String> result = cache.getAll(new String[0]);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        cache.putAll(new String[0], new String[0]);
        assertEquals(0, cache.size());

        // Null arrays
        result = cache.getAll(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        cache.putAll(null, null);
        assertEquals(0, cache.size());

        // Mismatched array lengths
        cache.putAll(new String[] { "key1", "key2" }, new String[] { "value1" });
        assertEquals(0, cache.size()); // Should handle gracefully
    }

    @Test
    void testHardwareOptimizedEviction() {
        // Test that eviction works with hardware optimizations
        for (int i = 0; i < 150; i++) {
            cache.put("evict_key" + i, "evict_value" + i);
        }

        // Cache should respect size limit
        assertTrue(cache.size() <= 100);

        // Some entries should still be accessible
        boolean foundSomeEntry = false;
        for (int i = 0; i < 150; i++) {
            if (cache.get("evict_key" + i) != null) {
                foundSomeEntry = true;
                break;
            }
        }
        assertTrue(foundSomeEntry, "Should find some entries after eviction");
    }
}
