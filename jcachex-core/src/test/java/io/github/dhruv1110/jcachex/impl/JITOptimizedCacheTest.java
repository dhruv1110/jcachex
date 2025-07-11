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
 * Test class for JITOptimizedCache focusing on JIT optimization features.
 */
class JITOptimizedCacheTest {

    private JITOptimizedCache<String, String> cache;
    private CacheConfig<String, String> config;

    @BeforeEach
    void setUp() {
        config = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .evictionStrategy(new LRUEvictionStrategy<String, String>())
                .recordStats(true)
                .build();
        cache = new JITOptimizedCache<>(config);
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
    void testJITOptimizedReadPath() {
        // Test that the JIT-optimized read path works correctly
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // Multiple reads to trigger JIT optimizations
        for (int i = 0; i < 100; i++) {
            assertEquals("value1", cache.get("key1"));
            assertEquals("value2", cache.get("key2"));
            assertEquals("value3", cache.get("key3"));
        }

        // Verify all values are still correct
        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));
        assertEquals("value3", cache.get("key3"));
    }

    @Test
    void testJITOptimizedWritePath() {
        // Test that the JIT-optimized write path works correctly
        for (int i = 0; i < 50; i++) {
            cache.put("key" + i, "value" + i);
        }

        // Verify all values were written correctly
        for (int i = 0; i < 50; i++) {
            assertEquals("value" + i, cache.get("key" + i));
        }
    }

    @Test
    void testJITWarmupBehavior() {
        // Test that JIT warmup is triggered periodically
        // This is tested indirectly by performing many operations

        // Perform enough operations to trigger warmup (every 10k operations)
        for (int i = 0; i < 15000; i++) {
            cache.put("key" + (i % 1000), "value" + i);
            cache.get("key" + (i % 1000));
        }

        // Verify cache is still functional after warmup
        cache.put("test", "test_value");
        assertEquals("test_value", cache.get("test"));
    }

    @Test
    void testJITMetrics() {
        // Test that JIT-specific metrics are available
        cache.put("test", "value");
        String metrics = cache.getJITMetrics();

        assertNotNull(metrics);
        assertTrue(metrics.contains("ReadPath"));
        assertTrue(metrics.contains("WritePath"));
    }

    @Test
    void testSeparateReadWritePaths() {
        // Test that read and write operations work independently

        // Write operations
        cache.put("write1", "value1");
        cache.put("write2", "value2");

        // Read operations
        assertEquals("value1", cache.get("write1"));
        assertEquals("value2", cache.get("write2"));

        // Remove operations
        assertEquals("value1", cache.remove("write1"));
        assertNull(cache.get("write1"));
        assertEquals("value2", cache.get("write2"));
    }

    @Test
    void testConcurrentReadWriteOptimization() {
        // Test concurrent operations with JIT optimization
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(8);
        AtomicInteger readOperations = new AtomicInteger(0);
        AtomicInteger writeOperations = new AtomicInteger(0);

        // Mix of read and write threads
        for (int t = 0; t < 4; t++) {
            final int threadId = t;
            // Write threads
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 100; i++) {
                        cache.put("thread" + threadId + "_key" + i, "value" + i);
                        writeOperations.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });

            // Read threads
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 100; i++) {
                        cache.get("thread" + (threadId % 2) + "_key" + (i % 50));
                        readOperations.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertEquals(400, writeOperations.get());
            assertEquals(400, readOperations.get());
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

        // Test null key in put (should not cause errors)
        cache.put(null, "value");
        assertNull(cache.get(null));
    }

    @Test
    void testSizeManagement() {
        // Test that cache respects size limits with JIT optimization
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
    void testEvictionWithJITOptimization() {
        // Test that eviction works correctly with JIT optimization

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
    void testPerformanceOptimization() {
        // Test that the JIT optimization improves performance over time
        // This is measured indirectly by ensuring operations complete quickly

        long startTime = System.nanoTime();

        // Perform many operations that should benefit from JIT optimization
        for (int i = 0; i < 1000; i++) {
            cache.put("key" + i, "value" + i);
            cache.get("key" + i);
            if (i % 100 == 0) {
                cache.remove("key" + (i - 50));
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        // Should complete within reasonable time
        assertTrue(duration < TimeUnit.SECONDS.toNanos(3),
                "JIT-optimized operations took too long: " + duration + " ns");

        // Verify operations were successful
        assertNotNull(cache.get("key999"));
        assertEquals("value999", cache.get("key999"));
    }

    @Test
    void testShutdownBehavior() {
        // Test that shutdown works correctly for JIT-optimized cache
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // Verify values are present before shutdown
        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));

        // Shutdown the cache
        cache.shutdown();

        // After shutdown, cache should still be functional but optimizations disabled
        // The cache itself doesn't become non-functional, just the optimizations stop
        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));

        // Verify shutdown is idempotent
        cache.shutdown(); // Should not throw exception
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

        JITOptimizedCache<String, String> smallCache = new JITOptimizedCache<>(smallConfig);
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

    @Test
    void testJITOptimizationWithLargeValues() {
        // Test JIT optimization with larger values
        StringBuilder largeValue = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            largeValue.append("Large value content ");
        }

        String largeValueStr = largeValue.toString();

        // Test that JIT optimization works with large values
        for (int i = 0; i < 20; i++) {
            cache.put("large" + i, largeValueStr + i);
        }

        // Verify all large values are stored correctly
        for (int i = 0; i < 20; i++) {
            assertEquals(largeValueStr + i, cache.get("large" + i));
        }
    }

    @Test
    void testReadPathOptimization() {
        // Test specific read path optimization
        cache.put("hotkey", "hotvalue");

        // Perform many reads to optimize the read path
        for (int i = 0; i < 1000; i++) {
            assertEquals("hotvalue", cache.get("hotkey"));
        }

        // Read path should still work correctly
        assertEquals("hotvalue", cache.get("hotkey"));
    }

    @Test
    void testWritePathOptimization() {
        // Test specific write path optimization

        // Perform many writes to optimize the write path
        for (int i = 0; i < 200; i++) {
            cache.put("writekey" + (i % 10), "writevalue" + i);
        }

        // Verify final values are correct
        for (int i = 0; i < 10; i++) {
            String expectedValue = "writevalue" + (190 + i);
            assertEquals(expectedValue, cache.get("writekey" + i));
        }
    }

    @Test
    void testJITMetricsContent() {
        // Test that JIT metrics contain expected information
        cache.put("test", "value");
        String metrics = cache.getJITMetrics();

        // Should contain path information
        assertTrue(metrics.contains("ReadPath"));
        assertTrue(metrics.contains("WritePath"));

        // Should be a well-formed string
        assertFalse(metrics.trim().isEmpty());
    }
}
