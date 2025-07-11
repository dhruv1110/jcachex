package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEntry;
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
 * Test class for AllocationOptimizedCache focusing on allocation optimization
 * features.
 */
class AllocationOptimizedCacheTest {

    private AllocationOptimizedCache<String, String> cache;
    private CacheConfig<String, String> config;

    @BeforeEach
    void setUp() {
        config = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .evictionStrategy(new LRUEvictionStrategy<String, String>())
                .recordStats(true)
                .build();
        cache = new AllocationOptimizedCache<>(config);
    }

    @AfterEach
    void tearDown() {
        if (cache != null) {
            cache.shutdown();
        }
    }

    @Test
    void testBasicCacheOperations() {
        // Test put and get
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));

        // Test overwrite
        cache.put("key1", "value2");
        assertEquals("value2", cache.get("key1"));

        // Test remove
        assertEquals("value2", cache.remove("key1"));
        assertNull(cache.get("key1"));

        // Test non-existent key
        assertNull(cache.get("nonexistent"));
    }

    @Test
    void testCacheEntryCreation() {
        // Test that cache entries are created correctly
        cache.put("key1", "value1");
        assertNotNull(cache.get("key1"));
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
    void testAllocationMetrics() {
        // Test that allocation metrics are available
        String metrics = cache.getAllocationMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.contains("EntryPool"));
        assertTrue(metrics.contains("OpPool"));

        // Add some entries to potentially affect metrics
        for (int i = 0; i < 20; i++) {
            cache.put("key" + i, "value" + i);
        }

        String updatedMetrics = cache.getAllocationMetrics();
        assertNotNull(updatedMetrics);
        assertTrue(updatedMetrics.contains("EntryPool"));
        assertTrue(updatedMetrics.contains("OpPool"));
    }

    @Test
    void testThreadLocalPooling() {
        // Test that thread-local pools are working by performing operations
        // from different threads and ensuring they don't interfere
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int t = 0; t < 5; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    // Each thread performs its own operations
                    for (int i = 0; i < 10; i++) {
                        String key = "thread" + threadId + "_key" + i;
                        String value = "thread" + threadId + "_value" + i;
                        cache.put(key, value);
                        assertEquals(value, cache.get(key));
                    }
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(5, successCount.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Thread interrupted");
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testCustomOptimization() {
        // Test that custom optimization is triggered
        // This is indirect testing since performCustomOptimization is protected

        // Perform many operations to trigger optimization
        for (int i = 0; i < 1500; i++) {
            cache.put("key" + i, "value" + i);
            cache.get("key" + i);
        }

        // Verify cache is still functional after optimization
        // Use keys that should still be in cache after eviction (recent entries)
        assertEquals("value1400", cache.get("key1400"));
        assertEquals("value1450", cache.get("key1450"));
    }

    @Test
    void testShutdownCleanup() {
        // Test that shutdown properly cleans up thread-local pools
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // Get allocation metrics before shutdown
        String metricsBefore = cache.getAllocationMetrics();
        assertNotNull(metricsBefore);

        // Shutdown the cache
        cache.shutdown();

        // After shutdown, operations should not work
        assertNull(cache.get("key1"));

        // Verify shutdown is idempotent
        cache.shutdown(); // Should not throw exception
    }

    @Test
    void testConcurrentOperations() {
        // Test concurrent operations with allocation optimization
        int numThreads = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger totalOperations = new AtomicInteger(0);

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        String key = "concurrent_key_" + threadId + "_" + i;
                        String value = "concurrent_value_" + threadId + "_" + i;

                        // Mix of operations
                        cache.put(key, value);
                        assertEquals(value, cache.get(key));

                        if (i % 10 == 0) {
                            cache.remove(key);
                        }

                        totalOperations.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertEquals(numThreads * operationsPerThread, totalOperations.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Thread interrupted");
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testNullHandling() {
        // Test null key handling
        assertNull(cache.get(null));
        assertNull(cache.remove(null));

        // Test null value handling (should not cause errors)
        cache.put("key1", null);
        assertNull(cache.get("key1"));
    }

    @Test
    void testSizeManagement() {
        // Test that cache respects size limits
        for (int i = 0; i < 150; i++) {
            cache.put("key" + i, "value" + i);
        }

        // Cache should not exceed maximum size
        assertTrue(cache.size() <= 100);

        // Some recent entries should still be present
        boolean foundRecentEntry = false;
        for (int i = 140; i < 150; i++) {
            if (cache.get("key" + i) != null) {
                foundRecentEntry = true;
                break;
            }
        }
        assertTrue(foundRecentEntry, "Should find at least one recent entry");
    }

    @Test
    void testPoolingEfficiency() {
        // Test that object pooling is working by performing many operations
        // This is an indirect test of allocation optimization

        long startTime = System.nanoTime();

        // Perform many operations that should benefit from pooling
        for (int i = 0; i < 1000; i++) {
            cache.put("key" + i, "value" + i);
            cache.get("key" + i);
            if (i % 100 == 0) {
                cache.remove("key" + (i - 50));
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        // Should complete within reasonable time (this is a rough performance check)
        assertTrue(duration < TimeUnit.SECONDS.toNanos(5),
                "Operations took too long: " + duration + " ns");

        // Verify operations were successful
        assertNotNull(cache.get("key999"));
        assertEquals("value999", cache.get("key999"));
    }

    @Test
    void testMetricsContainExpectedInformation() {
        // Test that allocation metrics contain expected information
        cache.put("test", "value");
        String metrics = cache.getAllocationMetrics();

        // Should contain pool information
        assertTrue(metrics.contains("EntryPool"));
        assertTrue(metrics.contains("OpPool"));

        // Should contain numeric values
        assertTrue(metrics.matches(".*\\d+.*"), "Metrics should contain numeric values");
    }

    @Test
    void testEvictionWithAllocationOptimization() {
        // Test that eviction works correctly with allocation optimization

        // Fill cache to capacity
        for (int i = 0; i < 100; i++) {
            cache.put("key" + i, "value" + i);
        }

        // Add more entries to trigger eviction
        for (int i = 100; i < 120; i++) {
            cache.put("key" + i, "value" + i);
        }

        // Cache should still be within limits
        assertTrue(cache.size() <= 100);

        // Some newer entries should be present
        boolean foundNewEntry = false;
        for (int i = 110; i < 120; i++) {
            if (cache.get("key" + i) != null) {
                foundNewEntry = true;
                break;
            }
        }
        assertTrue(foundNewEntry, "Should find at least one new entry after eviction");
    }

    @Test
    void testLargeValueHandling() {
        // Test handling of larger values
        StringBuilder largeValue = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeValue.append("Large value content ");
        }

        String largeValueStr = largeValue.toString();
        cache.put("large", largeValueStr);
        assertEquals(largeValueStr, cache.get("large"));

        // Should still be able to add other entries
        cache.put("small", "small");
        assertEquals("small", cache.get("small"));
    }

    @Test
    void testConfigurationRespect() {
        // Test that the cache respects the provided configuration
        assertTrue(cache.config().isRecordStats());
        assertEquals(100L, cache.config().getMaximumSize());

        // Test with different configuration
        CacheConfig<String, String> smallConfig = CacheConfig.<String, String>builder()
                .maximumSize(5L)
                .evictionStrategy(new LRUEvictionStrategy<String, String>())
                .recordStats(false)
                .build();

        AllocationOptimizedCache<String, String> smallCache = new AllocationOptimizedCache<>(smallConfig);
        try {
            // Fill beyond capacity
            for (int i = 0; i < 10; i++) {
                smallCache.put("key" + i, "value" + i);
            }

            // Should not exceed configured size
            assertTrue(smallCache.size() <= 5);

        } finally {
            smallCache.shutdown();
        }
    }
}
