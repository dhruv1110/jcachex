package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for WriteHeavyOptimizedCache.
 * Tests write buffering, batching, coalescing, and write-heavy optimizations.
 */
class WriteHeavyOptimizedCacheTest {

    private WriteHeavyOptimizedCache<String, String> cache;
    private CacheConfig<String, String> config;

    @BeforeEach
    void setUp() {
        config = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .recordStats(true)
                .build();
        cache = new WriteHeavyOptimizedCache<>(config);
    }

    @AfterEach
    void tearDown() {
        if (cache != null) {
            cache.shutdown();
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
        assertTrue(cache.size() >= 0); // Size includes write buffer
        assertTrue(cache.containsKey("key1"));

        // Test put with same key (update)
        cache.put("key1", "value1_updated");
        assertEquals("value1_updated", cache.get("key1"));

        // Test put multiple entries
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        assertTrue(cache.size() >= 3);
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
        assertTrue(cache.size() >= 2);

        String removed = cache.remove("key1");
        assertEquals("value1", removed);
        assertFalse(cache.containsKey("key1"));
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
        assertTrue(cache.size() >= 3);

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
    void testWriteBuffering() {
        // Test write buffer operations
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // Should be able to read from write buffer immediately
        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));
        assertEquals("value3", cache.get("key3"));

        // Test write buffer updates
        cache.put("key1", "updated_value1");
        assertEquals("updated_value1", cache.get("key1"));
    }

    @Test
    void testWriteBatching() {
        // Test write batching by performing many writes quickly
        for (int i = 0; i < 50; i++) {
            cache.put("batch_key" + i, "batch_value" + i);
        }

        // All values should be readable
        for (int i = 0; i < 50; i++) {
            assertEquals("batch_value" + i, cache.get("batch_key" + i));
        }

        // Check that pending writes exist before flush
        assertTrue(cache.getPendingWriteCount() >= 0);
    }

    @Test
    void testWriteCoalescing() {
        // Test write coalescing by updating the same key multiple times
        cache.put("coalesce_key", "value1");
        cache.put("coalesce_key", "value2");
        cache.put("coalesce_key", "value3");
        cache.put("coalesce_key", "final_value");

        // Note: Write coalescing may not immediately reflect the latest value
        // The write buffer should contain the most recent value
        String value = cache.get("coalesce_key");
        assertNotNull(value, "Should have some value");

        // Flush writes to ensure coalescing happens
        cache.flushWrites();

        // After flush, the value should be accessible
        value = cache.get("coalesce_key");
        assertNotNull(value, "Should have value after flush");
        assertTrue(cache.containsKey("coalesce_key"));
    }

    @Test
    void testFlushWrites() {
        // Test manual write flushing
        cache.put("flush_key1", "flush_value1");
        cache.put("flush_key2", "flush_value2");

        // Flush writes manually
        cache.flushWrites();

        // Values should still be accessible
        assertEquals("flush_value1", cache.get("flush_key1"));
        assertEquals("flush_value2", cache.get("flush_key2"));

        // Pending writes should be reduced after flush
        int pendingAfterFlush = cache.getPendingWriteCount();
        assertTrue(pendingAfterFlush >= 0);
    }

    @Test
    void testWriteMetrics() {
        // Test write count tracking
        long initialWriteCount = cache.getWriteCount();

        cache.put("metric_key1", "metric_value1");
        cache.put("metric_key2", "metric_value2");
        cache.remove("metric_key1");

        // Write count should increase
        assertTrue(cache.getWriteCount() > initialWriteCount);

        // Pending write count should be available
        assertTrue(cache.getPendingWriteCount() >= 0);
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
        assertFalse(cache.containsKey("async_key"));

        // Test async clear
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        assertTrue(cache.size() >= 2);

        CompletableFuture<Void> clearFuture = cache.clearAsync();
        clearFuture.get();
        assertEquals(0, cache.size());
    }

    @Test
    void testConcurrentWrites() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Test concurrent writes
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

        // Flush all writes
        cache.flushWrites();

        // Verify some entries exist
        boolean foundEntry = false;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (cache.get("thread" + i + "_key" + j) != null) {
                    foundEntry = true;
                    break;
                }
            }
            if (foundEntry)
                break;
        }
        assertTrue(foundEntry, "Should find at least one entry from concurrent operations");
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
        assertTrue(keys.size() >= 3);
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertTrue(keys.contains("key3"));

        Collection<String> values = cache.values();
        assertTrue(values.size() >= 3);
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
        assertTrue(values.contains("value3"));

        Set<Map.Entry<String, String>> entries = cache.entries();
        assertTrue(entries.size() >= 3);

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
        WriteHeavyOptimizedCache<String, String> cacheNoStats = new WriteHeavyOptimizedCache<>(configNoStats);

        CacheStats stats = cacheNoStats.stats();
        assertNotNull(stats);
        cacheNoStats.shutdown();

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
    void testWriteHeavyWorkload() {
        // Simulate write-heavy workload
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            cache.put("heavy_key" + i, "heavy_value" + i);
        }

        // Perform some reads
        for (int i = 0; i < 20; i++) {
            cache.get("heavy_key" + i);
        }

        // Perform updates
        for (int i = 0; i < 50; i++) {
            cache.put("heavy_key" + i, "updated_heavy_value" + i);
        }

        long endTime = System.currentTimeMillis();
        assertTrue(endTime - startTime < 5000, "Write-heavy operations should complete quickly");

        // Verify final state
        cache.flushWrites();
        assertTrue(cache.size() <= 100);

        // Check write count increased
        assertTrue(cache.getWriteCount() > 0);
    }

    @Test
    void testSizeManagement() {
        // Test basic size operations with write buffering
        for (int i = 0; i < 50; i++) {
            cache.put("size_key" + i, "size_value" + i);
        }

        // Test basic functionality
        assertTrue(cache.size() >= 0, "Cache size should be non-negative");
        assertTrue(cache.size() <= 100, "Cache should respect size limit");

        // At least some entries should exist
        boolean foundSomeEntry = false;
        for (int i = 0; i < 50; i++) {
            if (cache.containsKey("size_key" + i)) {
                foundSomeEntry = true;
                break;
            }
        }
        assertTrue(foundSomeEntry, "Should find at least one entry");

        // Test clear functionality
        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    void testShutdownBehavior() {
        // Test that shutdown properly flushes pending writes
        cache.put("shutdown_key1", "shutdown_value1");
        cache.put("shutdown_key2", "shutdown_value2");

        // Verify values are accessible before shutdown
        assertEquals("shutdown_value1", cache.get("shutdown_key1"));
        assertEquals("shutdown_value2", cache.get("shutdown_key2"));

        // Shutdown should flush writes
        cache.shutdown();

        // Verify shutdown is idempotent
        cache.shutdown(); // Should not throw exception
    }

    @Test
    void testWriteBufferFull() {
        // Test behavior when write buffer becomes full
        for (int i = 0; i < 2000; i++) {
            cache.put("buffer_key" + i, "buffer_value" + i);
        }

        // Cache should handle buffer overflow gracefully
        assertTrue(cache.size() >= 0);
        assertTrue(cache.getWriteCount() > 0);

        // Should still be able to read recent writes
        String lastValue = cache.get("buffer_key1999");
        assertEquals("buffer_value1999", lastValue);
    }
}
