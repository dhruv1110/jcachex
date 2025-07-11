package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.eviction.LRUEvictionStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ReadOnlyOptimizedCache focusing on read optimization features.
 */
class ReadOnlyOptimizedCacheTest {

    private ReadOnlyOptimizedCache<String, String> cache;
    private CacheConfig<String, String> config;

    @BeforeEach
    void setUp() {
        config = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .evictionStrategy(new LRUEvictionStrategy<String, String>())
                .recordStats(true)
                .build();
        cache = new ReadOnlyOptimizedCache<>(config);
    }

    @AfterEach
    void tearDown() {
        // ReadOnlyOptimizedCache doesn't have shutdown method
        // Just clear the cache if not in read-only mode
        if (cache != null) {
            try {
                cache.clear();
            } catch (UnsupportedOperationException e) {
                // Ignore if in read-only mode
            }
        }
    }

    @Test
    void testBasicCacheOperations() {
        // Test put and get
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));

        // Test multiple entries
        for (int i = 0; i < 10; i++) {
            cache.put("key" + i, "value" + i);
        }

        for (int i = 0; i < 10; i++) {
            assertEquals("value" + i, cache.get("key" + i));
        }
    }

    @Test
    void testReadOnlyModeOperations() {
        // Add some initial data
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // Switch to read-only mode
        cache.enableReadOnlyMode();

        // Read operations should still work
        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));

        // Write operations should fail
        assertThrows(UnsupportedOperationException.class, () -> cache.put("key3", "value3"));
        assertThrows(UnsupportedOperationException.class, () -> cache.remove("key1"));
        assertThrows(UnsupportedOperationException.class, () -> cache.clear());
    }

    @Test
    void testReadHeavyOptimization() {
        // Test that read-heavy optimization works
        cache.put("hotkey", "hotvalue");

        // Perform many reads to trigger optimization
        for (int i = 0; i < 1000; i++) {
            assertEquals("hotvalue", cache.get("hotkey"));
        }

        // Verify the value is still correct
        assertEquals("hotvalue", cache.get("hotkey"));
    }

    @Test
    void testConcurrentReads() {
        // Test concurrent read operations
        cache.put("shared", "sharedvalue");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger successfulReads = new AtomicInteger(0);

        for (int t = 0; t < 10; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 100; i++) {
                        if ("sharedvalue".equals(cache.get("shared"))) {
                            successfulReads.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(1000, successfulReads.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Thread interrupted");
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testBulkLoadOperations() {
        // Test bulk loading functionality
        java.util.Map<String, String> bulkData = new java.util.HashMap<>();
        for (int i = 0; i < 20; i++) {
            bulkData.put("bulk_key_" + i, "bulk_value_" + i);
        }

        cache.bulkLoad(bulkData);

        // Verify all bulk-loaded data is present
        for (int i = 0; i < 20; i++) {
            assertEquals("bulk_value_" + i, cache.get("bulk_key_" + i));
        }
    }

    @Test
    void testCacheLocalityOptimization() {
        // Test that cache locality optimization works
        // Fill cache with data that should benefit from locality optimization
        for (int i = 0; i < 50; i++) {
            cache.put("locality_key_" + i, "locality_value_" + i);
        }

        // Access keys in patterns that should benefit from locality
        for (int round = 0; round < 10; round++) {
            for (int i = 0; i < 50; i += 5) {
                assertEquals("locality_value_" + i, cache.get("locality_key_" + i));
            }
        }

        // Verify all values are still correct
        for (int i = 0; i < 50; i++) {
            assertEquals("locality_value_" + i, cache.get("locality_key_" + i));
        }
    }

    @Test
    void testNullHandling() {
        // Test null key handling
        assertNull(cache.get(null));

        // Test null value handling
        cache.put("key1", null);
        assertNull(cache.get("key1"));
    }

    @Test
    void testSizeManagement() {
        // ReadOnlyOptimizedCache doesn't enforce size limits during writes
        // It's designed for read-heavy workloads and expects size management
        // to be done at application level or during bulk loading

        // Add entries beyond the configured maximum size
        for (int i = 0; i < 150; i++) {
            cache.put("key" + i, "value" + i);
        }

        // Verify all entries are stored (no automatic eviction)
        assertEquals(150, cache.size());

        // Verify all entries can be retrieved
        for (int i = 0; i < 150; i++) {
            assertEquals("value" + i, cache.get("key" + i));
        }
    }

    @Test
    void testConfigurationRespect() {
        // Test that the cache respects the provided configuration
        assertTrue(cache.config().isRecordStats());
        assertEquals(100L, cache.config().getMaximumSize());
    }

    @Test
    void testWarmUpOperations() {
        // Test cache warm-up functionality
        for (int i = 0; i < 20; i++) {
            cache.put("warmup_key_" + i, "warmup_value_" + i);
        }

        // Create set of frequent keys for warm-up
        java.util.Set<String> frequentKeys = new java.util.HashSet<>();
        for (int i = 0; i < 5; i++) {
            frequentKeys.add("warmup_key_" + i);
        }

        // Warm up the cache
        cache.warmUp(frequentKeys);

        // Verify all data is still accessible
        for (int i = 0; i < 20; i++) {
            assertEquals("warmup_value_" + i, cache.get("warmup_key_" + i));
        }
    }

    @Test
    void testReadIndexOptimization() {
        // Test that the read index optimization works
        // This is an indirect test of the internal read optimization structures

        // Add entries that should benefit from read indexing
        for (int i = 0; i < 20; i++) {
            cache.put("indexed_key_" + i, "indexed_value_" + i);
        }

        // Access some keys frequently to build up read index
        for (int round = 0; round < 50; round++) {
            for (int i = 0; i < 5; i++) {
                assertEquals("indexed_value_" + i, cache.get("indexed_key_" + i));
            }
        }

        // All reads should still work correctly
        for (int i = 0; i < 20; i++) {
            assertEquals("indexed_value_" + i, cache.get("indexed_key_" + i));
        }
    }

    @Test
    void testReadOnlyBehaviorAfterEnabling() {
        // Test that read-only mode persists
        cache.put("key1", "value1");

        // Enable read-only mode
        cache.enableReadOnlyMode();

        // Reads should still work
        assertEquals("value1", cache.get("key1"));

        // Bulk load should fail in read-only mode
        java.util.Map<String, String> bulkData = new java.util.HashMap<>();
        bulkData.put("bulk_key", "bulk_value");
        assertThrows(UnsupportedOperationException.class, () -> cache.bulkLoad(bulkData));
    }
}
