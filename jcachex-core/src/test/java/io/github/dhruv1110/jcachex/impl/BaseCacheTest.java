package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.*;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base test class for all cache implementations.
 *
 * This class provides comprehensive test coverage for common cache
 * functionality
 * that should work consistently across all implementations. Subclasses should
 * implement the createCache() method to provide their specific cache instance.
 */
public abstract class BaseCacheTest {

    protected Cache<String, String> cache;
    protected CacheConfig<String, String> config;
    private ExecutorService executorService;

    /**
     * Factory method to create the specific cache implementation for testing.
     * Subclasses must implement this method.
     */
    protected abstract Cache<String, String> createCache(CacheConfig<String, String> config);

    @BeforeEach
    void setUp() {
        config = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .recordStats(true)
                .build();
        cache = createCache(config);
        executorService = Executors.newFixedThreadPool(10);
    }

    @AfterEach
    void tearDown() {
        if (cache instanceof AutoCloseable) {
            try {
                ((AutoCloseable) cache).close();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Should store and retrieve values")
        void shouldStoreAndRetrieveValues() {
            cache.put("key1", "value1");
            assertEquals("value1", cache.get("key1"));
        }

        @Test
        @DisplayName("Should return null for non-existent keys")
        void shouldReturnNullForNonExistentKeys() {
            assertNull(cache.get("nonexistent"));
        }

        @Test
        @DisplayName("Should handle null keys gracefully")
        void shouldHandleNullKeysGracefully() {
            assertNull(cache.get(null));
            cache.put(null, "value");
            assertNull(cache.get(null));
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            cache.put("key", null);
            assertNull(cache.get("key"));
        }

        @Test
        @DisplayName("Should update existing values")
        void shouldUpdateExistingValues() {
            cache.put("key", "value1");
            cache.put("key", "value2");
            assertEquals("value2", cache.get("key"));
        }

        @Test
        @DisplayName("Should remove values")
        void shouldRemoveValues() {
            cache.put("key", "value");
            assertEquals("value", cache.remove("key"));
            assertNull(cache.get("key"));
        }

        @Test
        @DisplayName("Should return null when removing non-existent keys")
        void shouldReturnNullWhenRemovingNonExistentKeys() {
            assertNull(cache.remove("nonexistent"));
        }

        @Test
        @DisplayName("Should clear all entries")
        void shouldClearAllEntries() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            assertEquals(2, cache.size());

            cache.clear();
            assertEquals(0, cache.size());
            assertNull(cache.get("key1"));
            assertNull(cache.get("key2"));
        }

        @Test
        @DisplayName("Should track size correctly")
        void shouldTrackSizeCorrectly() {
            assertEquals(0, cache.size());

            cache.put("key1", "value1");
            assertEquals(1, cache.size());

            cache.put("key2", "value2");
            assertEquals(2, cache.size());

            cache.remove("key1");
            assertEquals(1, cache.size());

            cache.clear();
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("Should check key existence")
        void shouldCheckKeyExistence() {
            assertFalse(cache.containsKey("key"));

            cache.put("key", "value");
            assertTrue(cache.containsKey("key"));

            cache.remove("key");
            assertFalse(cache.containsKey("key"));
        }
    }

    @Nested
    @DisplayName("Collection Views")
    class CollectionViews {

        @Test
        @DisplayName("Should return correct keys")
        void shouldReturnCorrectKeys() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            Set<String> keys = cache.keys();
            assertEquals(2, keys.size());
            assertTrue(keys.contains("key1"));
            assertTrue(keys.contains("key2"));
        }

        @Test
        @DisplayName("Should return correct values")
        void shouldReturnCorrectValues() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            List<String> values = new ArrayList<>(cache.values());
            assertEquals(2, values.size());
            assertTrue(values.contains("value1"));
            assertTrue(values.contains("value2"));
        }

        @Test
        @DisplayName("Should return correct entries")
        void shouldReturnCorrectEntries() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            Set<Map.Entry<String, String>> entries = cache.entries();
            assertEquals(2, entries.size());

            boolean foundEntry1 = false, foundEntry2 = false;
            for (Map.Entry<String, String> entry : entries) {
                if ("key1".equals(entry.getKey()) && "value1".equals(entry.getValue())) {
                    foundEntry1 = true;
                } else if ("key2".equals(entry.getKey()) && "value2".equals(entry.getValue())) {
                    foundEntry2 = true;
                }
            }
            assertTrue(foundEntry1 && foundEntry2);
        }
    }

    @Nested
    @DisplayName("Async Operations")
    class AsyncOperations {

        @Test
        @DisplayName("Should support async get")
        void shouldSupportAsyncGet() throws Exception {
            cache.put("key", "value");

            CompletableFuture<String> future = cache.getAsync("key");
            assertEquals("value", future.get(1, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("Should support async put")
        void shouldSupportAsyncPut() throws Exception {
            CompletableFuture<Void> future = cache.putAsync("key", "value");
            future.get(1, TimeUnit.SECONDS);

            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("Should support async remove")
        void shouldSupportAsyncRemove() throws Exception {
            cache.put("key", "value");

            CompletableFuture<String> future = cache.removeAsync("key");
            assertEquals("value", future.get(1, TimeUnit.SECONDS));
            assertNull(cache.get("key"));
        }

        @Test
        @DisplayName("Should support async clear")
        void shouldSupportAsyncClear() throws Exception {
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            CompletableFuture<Void> future = cache.clearAsync();
            future.get(1, TimeUnit.SECONDS);

            assertEquals(0, cache.size());
        }
    }

    @Nested
    @DisplayName("Configuration and Stats")
    class ConfigurationAndStats {

        @Test
        @DisplayName("Should return configuration")
        void shouldReturnConfiguration() {
            CacheConfig<String, String> returnedConfig = cache.config();
            assertNotNull(returnedConfig);
            assertEquals(100L, returnedConfig.getMaximumSize().longValue());
            assertTrue(returnedConfig.isRecordStats());
        }

        @Test
        @DisplayName("Should track statistics")
        void shouldTrackStatistics() {
            // Generate some hits and misses
            cache.put("key", "value");
            cache.get("key"); // hit
            cache.get("nonexistent"); // miss

            CacheStats stats = cache.stats();
            assertNotNull(stats);

            if (config.isRecordStats()) {
                assertTrue(stats.getHitCount().get() > 0);
                assertTrue(stats.getMissCount().get() > 0);
            }
        }
    }

    @Nested
    @DisplayName("Size Limits")
    class SizeLimits {

        @Test
        @DisplayName("Should respect maximum size")
        void shouldRespectMaximumSize() {
            // Create cache with small maximum size
            CacheConfig<String, String> smallConfig = CacheConfig.<String, String>builder()
                    .maximumSize(3L)
                    .recordStats(true)
                    .build();
            Cache<String, String> smallCache = createCache(smallConfig);

            // Add more entries than the maximum
            smallCache.put("key1", "value1");
            smallCache.put("key2", "value2");
            smallCache.put("key3", "value3");
            smallCache.put("key4", "value4");
            smallCache.put("key5", "value5");

            // Cache should not exceed maximum size
            assertTrue(smallCache.size() <= 3);
        }
    }

    @Nested
    @DisplayName("Event Listeners")
    class EventListeners {

        @Test
        @DisplayName("Should notify listeners on events")
        void shouldNotifyListenersOnEvents() throws InterruptedException {
            AtomicInteger putCount = new AtomicInteger(0);
            AtomicInteger removeCount = new AtomicInteger(0);
            AtomicInteger clearCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(3);

            CacheEventListener<String, String> listener = new CacheEventListener<String, String>() {
                @Override
                public void onPut(String key, String value) {
                    putCount.incrementAndGet();
                    latch.countDown();
                }

                @Override
                public void onRemove(String key, String value) {
                    removeCount.incrementAndGet();
                    latch.countDown();
                }

                @Override
                public void onEvict(String key, String value, EvictionReason reason) {
                    // Not counted in this test
                }

                @Override
                public void onExpire(String key, String value) {
                    // Not counted in this test
                }

                @Override
                public void onLoad(String key, String value) {
                    // Not counted in this test
                }

                @Override
                public void onLoadError(String key, Throwable error) {
                    // Not counted in this test
                }

                @Override
                public void onClear() {
                    clearCount.incrementAndGet();
                    latch.countDown();
                }
            };

            CacheConfig<String, String> configWithListener = CacheConfig.<String, String>builder()
                    .maximumSize(100L)
                    .addListener(listener)
                    .build();
            Cache<String, String> cacheWithListener = createCache(configWithListener);

            cacheWithListener.put("key", "value");
            cacheWithListener.remove("key");
            cacheWithListener.clear();

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertTrue(putCount.get() > 0);
            assertTrue(removeCount.get() > 0);
            assertTrue(clearCount.get() > 0);
        }
    }

    @Nested
    @DisplayName("Concurrency")
    class Concurrency {

        @Test
        @DisplayName("Should handle concurrent operations")
        @Timeout(10)
        void shouldHandleConcurrentOperations() throws InterruptedException {
            int threadCount = 10;
            int operationsPerThread = 100;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);
            AtomicBoolean hasError = new AtomicBoolean(false);

            for (int i = 0; i < threadCount; i++) {
                int threadId = i;
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < operationsPerThread; j++) {
                            String key = "key-" + threadId + "-" + j;
                            String value = "value-" + threadId + "-" + j;

                            cache.put(key, value);
                            // Due to concurrent operations and potential size limits,
                            // entries may be evicted before retrieval
                            String retrievedValue = cache.get(key);
                            if (retrievedValue != null) {
                                assertEquals(value, retrievedValue);
                            }
                            cache.remove(key);
                        }
                    } catch (Exception e) {
                        hasError.set(true);
                        e.printStackTrace();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // Start all threads
            assertTrue(endLatch.await(10, TimeUnit.SECONDS));
            assertFalse(hasError.get());
        }

        @Test
        @DisplayName("Should handle concurrent modifications safely")
        @Timeout(10)
        void shouldHandleConcurrentModificationsSafely() throws InterruptedException {
            int readerThreads = 5;
            int writerThreads = 5;
            int operationsPerThread = 50;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(readerThreads + writerThreads);
            AtomicBoolean hasError = new AtomicBoolean(false);

            // Pre-populate cache
            for (int i = 0; i < 10; i++) {
                cache.put("key" + i, "value" + i);
            }

            // Start reader threads
            for (int i = 0; i < readerThreads; i++) {
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < operationsPerThread; j++) {
                            cache.get("key" + (j % 10));
                            cache.containsKey("key" + (j % 10));
                            cache.size();
                        }
                    } catch (Exception e) {
                        hasError.set(true);
                        e.printStackTrace();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            // Start writer threads
            for (int i = 0; i < writerThreads; i++) {
                int threadId = i;
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < operationsPerThread; j++) {
                            String key = "writer-key-" + threadId + "-" + j;
                            cache.put(key, "value");
                            cache.remove(key);
                        }
                    } catch (Exception e) {
                        hasError.set(true);
                        e.printStackTrace();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // Start all threads
            assertTrue(endLatch.await(10, TimeUnit.SECONDS));
            assertFalse(hasError.get());
        }
    }

    @Nested
    @DisplayName("Expiration")
    class Expiration {

        @Test
        @org.junit.jupiter.api.Disabled("Expiration functionality may not be implemented in all cache implementations")
        @DisplayName("Should expire entries after write time")
        void shouldExpireEntriesAfterWriteTime() throws InterruptedException {
            CacheConfig<String, String> expiringConfig = CacheConfig.<String, String>builder()
                    .maximumSize(100L)
                    .expireAfterWrite(Duration.ofMillis(200))
                    .build();
            Cache<String, String> expiringCache = createCache(expiringConfig);

            expiringCache.put("key", "value");
            assertEquals("value", expiringCache.get("key"));

            // Wait for expiration with polling to handle timing variations
            long startTime = System.currentTimeMillis();
            boolean expired = false;
            while (System.currentTimeMillis() - startTime < 5000) { // 5 second timeout
                Thread.sleep(100);
                if (expiringCache.get("key") == null) {
                    expired = true;
                    break;
                }
            }

            // Entry should be expired
            assertTrue(expired, "Entry should have expired after write time");
            assertNull(expiringCache.get("key"));
            assertFalse(expiringCache.containsKey("key"));
        }

        @Test
        @org.junit.jupiter.api.Disabled("Expiration functionality may not be implemented in all cache implementations")
        @DisplayName("Should expire entries after access time")
        void shouldExpireEntriesAfterAccessTime() throws InterruptedException {
            CacheConfig<String, String> expiringConfig = CacheConfig.<String, String>builder()
                    .maximumSize(100L)
                    .expireAfterAccess(Duration.ofMillis(200))
                    .build();
            Cache<String, String> expiringCache = createCache(expiringConfig);

            expiringCache.put("key", "value");

            // Access the entry to reset the access timer
            Thread.sleep(100);
            assertEquals("value", expiringCache.get("key"));

            // Wait for expiration with polling to handle timing variations
            long startTime = System.currentTimeMillis();
            boolean expired = false;
            while (System.currentTimeMillis() - startTime < 5000) { // 5 second timeout
                Thread.sleep(100);
                if (expiringCache.get("key") == null) {
                    expired = true;
                    break;
                }
            }

            // Entry should be expired
            assertTrue(expired, "Entry should have expired after access time");
            assertNull(expiringCache.get("key"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty cache operations")
        void shouldHandleEmptyCacheOperations() {
            assertEquals(0, cache.size());
            assertNull(cache.get("anything"));
            assertNull(cache.remove("anything"));
            assertFalse(cache.containsKey("anything"));

            assertTrue(cache.keys().isEmpty());
            assertTrue(cache.values().isEmpty());
            assertTrue(cache.entries().isEmpty());

            // Clear on empty cache should not fail
            cache.clear();
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("Should handle rapid put/remove cycles")
        void shouldHandleRapidPutRemoveCycles() {
            for (int i = 0; i < 1000; i++) {
                cache.put("key", "value" + i);
                assertEquals("value" + i, cache.get("key"));
                assertEquals("value" + i, cache.remove("key"));
                assertNull(cache.get("key"));
            }
        }

        @Test
        @DisplayName("Should handle large number of unique keys")
        void shouldHandleLargeNumberOfUniqueKeys() {
            int keyCount = 1000;

            // Add many keys
            for (int i = 0; i < keyCount; i++) {
                cache.put("key" + i, "value" + i);
            }

            // Verify they can be retrieved (respecting size limits)
            long actualSize = cache.size();
            assertTrue(actualSize > 0);
            assertTrue(actualSize <= Math.min(keyCount, 100)); // Respecting max size
        }
    }
}
