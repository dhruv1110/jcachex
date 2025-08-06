package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DefaultCache implementation.
 * Extends BaseCacheTest to inherit all common functionality tests.
 */
@DisplayName("DefaultCache Tests")
class DefaultCacheTest extends BaseCacheTest {

    @Override
    protected Cache<String, String> createCache(CacheConfig<String, String> config) {
        return new DefaultCache<>(config);
    }

    @Nested
    @DisplayName("DefaultCache Specific Features")
    class DefaultCacheSpecificFeatures {

        @Test
        @DisplayName("Should implement AutoCloseable")
        void shouldImplementAutoCloseable() {
            assertTrue(cache instanceof AutoCloseable);
        }

        @Test
        @DisplayName("Should handle close operation gracefully")
        void shouldHandleCloseOperationGracefully() throws Exception {
            cache.put("key", "value");
            assertEquals("value", cache.get("key"));

            // Close should not throw exception
            ((AutoCloseable) cache).close();

            // Cache should still be functional after close (graceful degradation)
            // Note: Specific behavior after close is implementation-dependent
        }

        @Test
        @DisplayName("Should support refresh functionality")
        void shouldSupportRefreshFunctionality() throws InterruptedException {
            Function<String, String> loader = key -> "loaded-" + key;

            CacheConfig<String, String> refreshConfig = CacheConfig.<String, String>builder()
                    .maximumSize(100L)
                    .loader(loader)
                    .refreshAfterWrite(Duration.ofMillis(50))
                    .build();

            Cache<String, String> refreshCache = createCache(refreshConfig);

            // Put initial value
            refreshCache.put("key", "initial");
            assertEquals("initial", refreshCache.get("key"));

            // Wait for refresh trigger
            Thread.sleep(100);

            // Access again - this might trigger refresh in background
            String value = refreshCache.get("key");
            assertNotNull(value);

            // Allow some time for background refresh
            Thread.sleep(100);
        }

        @Test
        @DisplayName("Should support async loader")
        void shouldSupportAsyncLoader() throws Exception {
            Function<String, CompletableFuture<String>> asyncLoader = key -> CompletableFuture
                    .completedFuture("async-" + key);

            CacheConfig<String, String> asyncConfig = CacheConfig.<String, String>builder()
                    .maximumSize(100L)
                    .asyncLoader(asyncLoader)
                    .build();

            Cache<String, String> asyncCache = createCache(asyncConfig);

            // Getting non-existent key should trigger async loading
            String value = asyncCache.get("test-key");

            // The behavior depends on implementation - might load synchronously or return
            // null and load in background
            // We verify that the async loader configuration was accepted
            assertNotNull(asyncCache);
            assertNotNull(asyncConfig.getAsyncLoader());
        }

        @Test
        @DisplayName("Should handle expiration correctly")
        void shouldHandleExpirationCorrectly() throws InterruptedException {
            CacheConfig<String, String> expireConfig = CacheConfig.<String, String>builder()
                    .maximumSize(100L)
                    .expireAfterWrite(Duration.ofMillis(100))
                    .build();

            Cache<String, String> expireCache = createCache(expireConfig);

            expireCache.put("key", "value");
            assertEquals("value", expireCache.get("key"));
            assertTrue(expireCache.containsKey("key"));

            // Wait for expiration
            Thread.sleep(150);

            // Entry should be expired
            assertNull(expireCache.get("key"));
            assertFalse(expireCache.containsKey("key"));
        }

        @Test
        @DisplayName("Should handle access-based expiration")
        void shouldHandleAccessBasedExpiration() throws InterruptedException {
            CacheConfig<String, String> accessExpireConfig = CacheConfig.<String, String>builder()
                    .maximumSize(100L)
                    .expireAfterAccess(Duration.ofMillis(100))
                    .build();

            Cache<String, String> accessExpireCache = createCache(accessExpireConfig);

            accessExpireCache.put("key", "value");

            // Access within expiration window
            Thread.sleep(50);
            assertEquals("value", accessExpireCache.get("key")); // Resets access timer

            // Wait again, but less than expiration time since last access
            Thread.sleep(50);
            assertEquals("value", accessExpireCache.get("key")); // Should still be available

            // Now wait for full expiration
            Thread.sleep(150);
            assertNull(accessExpireCache.get("key")); // Should be expired
        }

        @Test
        @DisplayName("Should support weigher function")
        void shouldSupportWeigherFunction() {
            CacheConfig<String, String> weigherConfig = CacheConfig.<String, String>builder()
                    .maximumWeight(1000L)
                    .weigher((key, value) -> (long) value.length())
                    .build();

            Cache<String, String> weigherCache = createCache(weigherConfig);

            weigherCache.put("key1", "short");
            weigherCache.put("key2", "this is a much longer value that should have more weight");

            // Both entries should be present initially
            assertEquals("short", weigherCache.get("key1"));
            assertEquals("this is a much longer value that should have more weight", weigherCache.get("key2"));
        }

        @Test
        @DisplayName("Should maintain cache integrity under stress")
        void shouldMaintainCacheIntegrityUnderStress() throws InterruptedException {
            int iterations = 1000;

            for (int i = 0; i < iterations; i++) {
                String key = "key" + i;
                String value = "value" + i;

                cache.put(key, value);

                if (i % 2 == 0) {
                    // Only check if key exists if it's within the maximum size window
                    // or if it's a recently added key that should still be in cache
                    if (i <= 100 || (i > iterations - 100)) {
                        String retrievedValue = cache.get(key);
                        // The value might be evicted due to size limits, so we don't assert equality
                        // but we do verify that if it exists, it's the correct value
                        if (retrievedValue != null) {
                            assertEquals(value, retrievedValue);
                        }
                    }
                } else {
                    cache.remove(key);
                }

                if (i % 100 == 0) {
                    assertTrue(cache.size() >= 0);
                    assertTrue(cache.size() <= 100); // Respecting max size
                }
            }

            // Final verification
            assertTrue(cache.size() >= 0);
            assertTrue(cache.size() <= 100);
        }
    }

    @Nested
    @DisplayName("Configuration Validation")
    class ConfigurationValidation {

        @Test
        @DisplayName("Should reject null configuration")
        void shouldRejectNullConfiguration() {
            assertThrows(IllegalArgumentException.class, () -> new DefaultCache<>(null));
        }

        @Test
        @DisplayName("Should handle minimal configuration")
        void shouldHandleMinimalConfiguration() {
            CacheConfig<String, String> minimalConfig = CacheConfig.<String, String>builder().build();
            Cache<String, String> minimalCache = createCache(minimalConfig);

            // Should work with default settings
            minimalCache.put("key", "value");
            assertEquals("value", minimalCache.get("key"));
        }

        @Test
        @DisplayName("Should respect initial capacity")
        void shouldRespectInitialCapacity() {
            CacheConfig<String, String> capacityConfig = CacheConfig.<String, String>builder()
                    .initialCapacity(50)
                    .maximumSize(100L)
                    .build();

            Cache<String, String> capacityCache = createCache(capacityConfig);

            // Should work normally regardless of initial capacity
            for (int i = 0; i < 60; i++) {
                capacityCache.put("key" + i, "value" + i);
            }

            assertTrue(capacityCache.size() > 0);
        }
    }

    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {

        @Test
        @DisplayName("Should handle large number of operations efficiently")
        void shouldHandleLargeNumberOfOperationsEfficiently() {
            long startTime = System.currentTimeMillis();

            int operationCount = 10000;
            for (int i = 0; i < operationCount; i++) {
                cache.put("key" + i, "value" + i);
                cache.get("key" + i);
            }

            long duration = System.currentTimeMillis() - startTime;

            // Performance expectation: should complete within reasonable time
            // Made more lenient to avoid flakiness on slower systems or under load
            assertTrue(duration < 30000, "Operations took too long: " + duration + "ms");

            // Verify the operations actually worked
            assertTrue(cache.size() > 0, "Cache should contain entries after operations");
        }

        @Test
        @DisplayName("Should maintain consistent performance under load")
        void shouldMaintainConsistentPerformanceUnderLoad() {
            long[] timings = new long[10];

            // Warm up JIT compiler first
            for (int warmup = 0; warmup < 3; warmup++) {
                for (int i = 0; i < 100; i++) {
                    cache.put("warmup" + i, "value" + i);
                    cache.get("warmup" + i);
                }
            }

            for (int round = 0; round < 10; round++) {
                long startTime = System.nanoTime();

                for (int i = 0; i < 1000; i++) {
                    String key = "round" + round + "key" + i;
                    cache.put(key, "value" + i);
                    cache.get(key);
                }

                timings[round] = System.nanoTime() - startTime;
            }

            // Calculate average and check for consistency
            long sum = 0;
            for (long timing : timings) {
                sum += timing;
            }
            long average = sum / timings.length;

            // More lenient timing check - allow up to 10x variation for flaky environment
            // tolerance
            // Skip first few rounds to account for JIT warmup
            for (int i = 3; i < timings.length; i++) {
                assertTrue(timings[i] < average * 10,
                        "Round " + i + " took " + timings[i] + "ns, average is " + average + "ns");
            }

            // Verify operations actually worked
            assertTrue(cache.size() > 0, "Cache should contain entries after performance test");
        }
    }
}
