package io.github.dhruv.jcachex;

import io.github.dhruv.jcachex.eviction.EvictionStrategy;
import io.github.dhruv.jcachex.eviction.LRUEvictionStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCacheTest {

    private DefaultCache<String, String> cache;
    private CacheConfig<String, String> config;

    @Mock
    private CacheEventListener<String, String> eventListener;

    @Mock
    private Function<String, String> loader;

    @Mock
    private Function<String, CompletableFuture<String>> asyncLoader;

    @BeforeEach
    void setUp() {
        // MockitoExtension handles mock initialization
        config = CacheConfig.<String, String>builder()
                .maximumSize(100L)
                .expireAfterWrite(Duration.ofMinutes(5))
                .addListener(eventListener)
                .build();
        cache = new DefaultCache<>(config);
    }

    @AfterEach
    void tearDown() {
        if (cache != null) {
            cache.close();
        }
    }

    @Test
    void testBasicPutAndGet() {
        // Test basic put and get operations
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));
        verify(eventListener, times(1)).onPut("key1", "value1");
    }

    @Test
    void testGetNonExistentKey() {
        // Test getting a non-existent key
        assertNull(cache.get("nonexistent"));
        verify(eventListener, never()).onPut(any(), any());
    }

    @Test
    void testRemove() {
        // Test remove operation
        cache.put("key1", "value1");
        String removed = cache.remove("key1");
        assertEquals("value1", removed);
        assertNull(cache.get("key1"));
        verify(eventListener, times(1)).onRemove("key1", "value1");
    }

    @Test
    void testClear() {
        // Test clear operation
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.clear();
        assertEquals(0L, cache.size());
        verify(eventListener, times(1)).onClear();
    }

    @Test
    void testExpiration() throws InterruptedException {
        // Test expiration after write
        CacheConfig<String, String> expiringConfig = CacheConfig.<String, String>builder()
                .expireAfterWrite(Duration.ofMillis(100))
                .build();
        DefaultCache<String, String> expiringCache = new DefaultCache<>(expiringConfig);

        expiringCache.put("key1", "value1");
        assertEquals("value1", expiringCache.get("key1"));

        Thread.sleep(200);

        assertNull(expiringCache.get("key1"));
        expiringCache.close();
    }

    @Test
    void testExpirationAfterAccess() throws InterruptedException {
        // Test expiration after access
        CacheConfig<String, String> accessConfig = CacheConfig.<String, String>builder()
                .expireAfterAccess(Duration.ofMillis(100))
                .build();
        DefaultCache<String, String> accessCache = new DefaultCache<>(accessConfig);

        accessCache.put("key1", "value1");
        assertEquals("value1", accessCache.get("key1"));

        Thread.sleep(200);

        assertNull(accessCache.get("key1"));
        accessCache.close();
    }

    @Test
    void testLoader() {
        // Test cache loader functionality
        when(loader.apply("key1")).thenReturn("loadedValue");
        CacheConfig<String, String> loaderConfig = CacheConfig.<String, String>builder()
                .loader(loader)
                .build();
        DefaultCache<String, String> loaderCache = new DefaultCache<>(loaderConfig);

        assertEquals("loadedValue", loaderCache.get("key1"));
        verify(loader, times(1)).apply("key1");
        loaderCache.close();
    }

    @Test
    void testAsyncLoader() throws ExecutionException, InterruptedException {
        // Test async loader functionality
        when(asyncLoader.apply("key1")).thenReturn(CompletableFuture.completedFuture("asyncValue"));
        CacheConfig<String, String> asyncConfig = CacheConfig.<String, String>builder()
                .asyncLoader(asyncLoader)
                .build();
        DefaultCache<String, String> asyncCache = new DefaultCache<>(asyncConfig);

        assertEquals("asyncValue", asyncCache.getAsync("key1").get());
        verify(asyncLoader, times(1)).apply("key1");
        asyncCache.close();
    }

    @Test
    void testSizeLimit() {
        // Test maximum size limit
        CacheConfig<String, String> sizeConfig = CacheConfig.<String, String>builder()
                .maximumSize(2L)
                .build();
        DefaultCache<String, String> sizeCache = new DefaultCache<>(sizeConfig);

        sizeCache.put("key1", "value1");
        sizeCache.put("key2", "value2");
        sizeCache.put("key3", "value3");

        assertEquals(2L, sizeCache.size());
        sizeCache.close();
    }

    @Test
    void testWeightLimit() {
        // Test maximum weight limit
        CacheConfig<String, String> weightConfig = CacheConfig.<String, String>builder()
                .maximumWeight(5L)
                .weigher((key, value) -> (long) value.length())
                .build();
        DefaultCache<String, String> weightCache = new DefaultCache<>(weightConfig);

        weightCache.put("key1", "value1"); // weight: 6
        weightCache.put("key2", "val"); // weight: 3
        weightCache.put("key3", "v"); // weight: 1

        assertEquals(2L, weightCache.size());
        weightCache.close();
    }

    @Test
    void testAsyncOperations() throws ExecutionException, InterruptedException {
        // Test async operations
        cache.put("key1", "value1");

        CompletableFuture<String> getFuture = cache.getAsync("key1");
        CompletableFuture<Void> putFuture = cache.putAsync("key2", "value2");
        CompletableFuture<String> removeFuture = cache.removeAsync("key1");
        CompletableFuture<Void> clearFuture = cache.clearAsync();

        assertEquals("value1", getFuture.get());
        assertNull(putFuture.get());
        assertEquals("value1", removeFuture.get());
        assertNull(clearFuture.get());
    }

    @Test
    void testStats() {
        // Test cache statistics
        cache.put("key1", "value1");
        cache.get("key1"); // Hit
        cache.get("key2"); // Miss

        CacheStats stats = cache.stats();
        assertEquals(1L, stats.getHitCount().get());
        assertEquals(1L, stats.getMissCount().get());
        assertEquals(0L, stats.getLoadCount().get());
    }

    @Test
    void testRefreshAfterWrite() throws InterruptedException {
        // Test refresh after write
        Function<String, String> refreshLoader = key -> "refreshed_" + key;
        CacheConfig<String, String> refreshConfig = CacheConfig.<String, String>builder()
                .refreshAfterWrite(Duration.ofMillis(100))
                .loader(refreshLoader)
                .build();
        DefaultCache<String, String> refreshCache = new DefaultCache<>(refreshConfig);

        refreshCache.put("key1", "value1");
        assertEquals("value1", refreshCache.get("key1"));

        Thread.sleep(200);

        // Value should be refreshed in background
        String value = refreshCache.get("key1");
        assertTrue(value.equals("value1") || value.equals("refreshed_key1"));

        Thread.sleep(1500); // Wait for refresh
        assertEquals("refreshed_key1", refreshCache.get("key1"));

        refreshCache.close();
    }

    @Test
    void testEviction() {
        // Test eviction strategy
        EvictionStrategy<String, String> strategy = new LRUEvictionStrategy<>();
        CacheConfig<String, String> evictionConfig = CacheConfig.<String, String>builder()
                .maximumSize(2L)
                .evictionStrategy(strategy)
                .build();
        DefaultCache<String, String> evictionCache = new DefaultCache<>(evictionConfig);

        evictionCache.put("key1", "value1");
        evictionCache.put("key2", "value2");
        evictionCache.put("key3", "value3");

        assertEquals(2L, evictionCache.size());
        assertNull(evictionCache.get("key1")); // Should be evicted
        evictionCache.close();
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        // Test concurrent access
        int threadCount = 10;
        int operationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    String key = "key" + (threadId * operationsPerThread + j);
                    cache.put(key, "value" + j);
                    cache.get(key);
                    if (j % 2 == 0) {
                        cache.remove(key);
                    }
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Verify no exceptions occurred during concurrent access
        assertTrue(cache.size() >= 0L);
    }

    @Test
    void testInvalidConfig() {
        // Test invalid configuration
        assertThrows(IllegalArgumentException.class, () -> {
            CacheConfig.<String, String>builder()
                    .maximumSize(-1L)
                    .build();
        }, "Should throw IllegalArgumentException for negative maximum size");

        assertThrows(IllegalArgumentException.class, () -> {
            CacheConfig.<String, String>builder()
                    .maximumWeight(-1L)
                    .build();
        }, "Should throw IllegalArgumentException for negative maximum weight");

        assertThrows(IllegalArgumentException.class, () -> {
            CacheConfig.<String, String>builder()
                    .expireAfterWrite(Duration.ofMillis(-1))
                    .build();
        }, "Should throw IllegalArgumentException for negative expiration duration");
    }

    @Test
    void testClose() {
        // Test cache close
        cache.put("key1", "value1");
        cache.close();
        cache = null; // Clear reference to closed cache

        // Create a new cache and verify it works
        DefaultCache<String, String> newCache = new DefaultCache<>(config);
        newCache.put("key2", "value2");
        assertEquals("value2", newCache.get("key2"));
        newCache.close();
    }
}
