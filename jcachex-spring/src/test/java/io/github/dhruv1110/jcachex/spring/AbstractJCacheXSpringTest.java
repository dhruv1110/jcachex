package io.github.dhruv1110.jcachex.spring;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheStats;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract base class for JCacheX Spring integration tests.
 * Provides common utilities and setup for testing cache functionality.
 */
@SpringBootTest(classes = TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public abstract class AbstractJCacheXSpringTest {

    @org.springframework.beans.factory.annotation.Autowired
    protected CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear any existing caches before each test
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> {
                org.springframework.cache.Cache cache = cacheManager.getCache(name);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
    }

    /**
     * Helper method to wait for async operations without Thread.sleep
     */
    protected void waitForAsyncOperation(CompletableFuture<?> future, int timeoutSeconds) {
        try {
            future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail("Async operation failed or timed out: " + e.getMessage());
        }
    }

    /**
     * Helper method to wait for a condition to be met
     */
    protected void waitForCondition(String description, BooleanSupplier condition, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                fail("Timeout waiting for condition: " + description);
            }
            try {
                Thread.sleep(10); // Small sleep to avoid busy waiting
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Interrupted while waiting for condition: " + description);
            }
        }
    }

    /**
     * Helper method to verify cache statistics
     */
    protected void verifyCacheStats(Cache<?, ?> cache, long expectedHits, long expectedMisses) {
        CacheStats stats = cache.stats();
        assertNotNull(stats, "Cache statistics should be available");
        assertEquals(expectedHits, stats.hitCount(), "Hit count mismatch");
        assertEquals(expectedMisses, stats.missCount(), "Miss count mismatch");
    }

    /**
     * Helper method to verify cache has expected size
     */
    protected void verifyCacheSize(Cache<?, ?> cache, long expectedSize) {
        assertEquals(expectedSize, cache.size(), "Cache size mismatch");
    }

    /**
     * Helper method to verify cache contains expected key
     */
    @SuppressWarnings("unchecked")
    protected <K> void verifyCacheContains(Cache<K, ?> cache, K key, boolean shouldContain) {
        assertEquals(shouldContain, cache.containsKey(key),
                "Cache " + (shouldContain ? "should" : "should not") + " contain key: " + key);
    }

    /**
     * Helper functional interface for condition checking
     */
    @FunctionalInterface
    protected interface BooleanSupplier {
        boolean getAsBoolean();
    }

    /**
     * Helper method to create a countdown latch and wait for it
     */
    protected void waitForLatch(CountDownLatch latch, int timeoutSeconds) {
        try {
            if (!latch.await(timeoutSeconds, TimeUnit.SECONDS)) {
                fail("Timeout waiting for latch");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for latch");
        }
    }

    /**
     * Helper method to assert that a cache operation completes within a timeout
     */
    protected <T> T assertCompletesWithin(CompletableFuture<T> future, int timeoutSeconds) {
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail("Operation did not complete within " + timeoutSeconds + " seconds: " + e.getMessage());
            return null; // This line will never be reached due to fail()
        }
    }

    /**
     * Helper method to verify Spring Cache wrapper functionality
     */
    protected void verifySpringCache(org.springframework.cache.Cache springCache, String key, Object value) {
        // Test put and get
        springCache.put(key, value);

        org.springframework.cache.Cache.ValueWrapper wrapper = springCache.get(key);
        assertNotNull(wrapper, "Cache should return a value wrapper");
        assertEquals(value, wrapper.get(), "Cache should return the correct value");

        // Test evict
        springCache.evict(key);
        assertNull(springCache.get(key), "Cache should not contain evicted key");
    }
}
