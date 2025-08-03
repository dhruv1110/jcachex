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
 * Test suite for CacheLocalityOptimizedCache.
 * Tests cache locality optimizations through standard Cache interface methods.
 */
class CacheLocalityOptimizedCacheTest {

    private CacheLocalityOptimizedCache<String, String> cache;
    private CacheConfig<String, String> config;

    @BeforeEach
    void setUp() {
        config = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .recordStats(true)
                .build();
        cache = new CacheLocalityOptimizedCache<>(config);
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
    void testLocalityOptimization() {
        // Test spatial locality by accessing adjacent keys
        for (int i = 0; i < 20; i++) {
            cache.put("spatial_key" + i, "spatial_value" + i);
        }

        // Access keys in sequence to test spatial locality
        for (int i = 0; i < 20; i++) {
            assertEquals("spatial_value" + i, cache.get("spatial_key" + i));
        }

        // Access keys in reverse order
        for (int i = 19; i >= 0; i--) {
            assertEquals("spatial_value" + i, cache.get("spatial_key" + i));
        }

        // Verify all entries are still there
        assertEquals(20, cache.size());
    }

    @Test
    void testTemporalLocality() {
        // Test temporal locality by accessing same keys repeatedly
        cache.put("temporal_key1", "temporal_value1");
        cache.put("temporal_key2", "temporal_value2");
        cache.put("temporal_key3", "temporal_value3");

        // Access same keys multiple times to test temporal locality
        for (int i = 0; i < 10; i++) {
            assertEquals("temporal_value1", cache.get("temporal_key1"));
            assertEquals("temporal_value2", cache.get("temporal_key2"));
            assertEquals("temporal_value3", cache.get("temporal_key3"));
        }

        // Values should remain consistent
        assertEquals("temporal_value1", cache.get("temporal_key1"));
        assertEquals("temporal_value2", cache.get("temporal_key2"));
        assertEquals("temporal_value3", cache.get("temporal_key3"));
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
        CacheLocalityOptimizedCache<String, String> cacheNoStats = new CacheLocalityOptimizedCache<>(configNoStats);

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
        // Test size management with locality optimization
        for (int i = 0; i < 120; i++) {
            cache.put("size_key" + i, "size_value" + i);
        }

        // Cache should respect maximum size
        assertTrue(cache.size() <= 100);

        // Recent entries should be more likely to remain due to locality optimization
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
    void testLocalityPattern() {
        // Test specific locality access patterns
        for (int i = 0; i < 50; i++) {
            cache.put("pattern_key" + i, "pattern_value" + i);
        }

        // Access in blocks to test spatial locality
        for (int block = 0; block < 5; block++) {
            for (int i = block * 10; i < (block + 1) * 10; i++) {
                assertEquals("pattern_value" + i, cache.get("pattern_key" + i));
            }
        }

        // Verify data is still accessible after locality-aware access
        for (int i = 0; i < 50; i++) {
            assertEquals("pattern_value" + i, cache.get("pattern_key" + i));
        }
    }

    @Test
    void testMemoryLayout() {
        // Test memory layout optimization with different access patterns
        for (int i = 0; i < 30; i++) {
            cache.put("layout_key" + i, "layout_value" + i);
        }

        // Access in different patterns
        // Sequential access
        for (int i = 0; i < 30; i++) {
            cache.get("layout_key" + i);
        }

        // Random access
        for (int i = 29; i >= 0; i -= 3) {
            cache.get("layout_key" + i);
        }

        // Verify all entries are still accessible
        for (int i = 0; i < 30; i++) {
            assertEquals("layout_value" + i, cache.get("layout_key" + i));
        }
    }
}
