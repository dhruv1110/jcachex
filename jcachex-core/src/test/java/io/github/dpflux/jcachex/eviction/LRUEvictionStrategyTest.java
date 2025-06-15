package io.github.dpflux.jcachex.eviction;

import io.github.dpflux.jcachex.Cache;
import io.github.dpflux.jcachex.CacheConfig;
import io.github.dpflux.jcachex.DefaultCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LRUEvictionStrategyTest {
    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        CacheConfig<String, String> cacheConfig = new CacheConfig.Builder<String, String>()
            .maximumSize(2L)
            .evictionStrategy(new LRUEvictionStrategy<>())
            .build();
        cache = new DefaultCache<>(cacheConfig);
        cache.put("A", "ValueA"); // order = 1
        cache.put("B", "ValueB"); // order = 2
    }

    @Test
    void testSelectEvictionCandidate() {
        cache.put("C", "ValueC"); // order = 3

        assertFalse(cache.containsKey("A"), "Cache should not contain key 'A' after eviction.");
        cache.get("B"); // access B, making it most recently used, order = 4
        cache.put("D", "ValueD"); // order = 5, should evict C with order = 3

        assertTrue(cache.containsKey("B"), "Cache should contain key 'B'.");
        assertFalse(cache.containsKey("C"), "Cache should not contain key 'C' after eviction.");
        assertTrue(cache.containsKey("D"), "Cache should contain key 'D'.");
    }

    @Test
    void testClearAccessCounts() {
        cache.get("A"); // access A, making it most recently used, order = 3
        cache.put("C", "ValueC"); // order = 4, should evict B with order = 2

        assertFalse(cache.containsKey("B"), "Cache should not contain key 'B' after eviction.");
        cache.clear();

        cache.put("A", "ValueA"); // order = 1
        cache.put("B", "ValueB"); // order = 2
        cache.put("C", "ValueC"); // order = 3, should evict A with order = 1

        assertFalse(cache.containsKey("A"), "Cache should not contain key 'A' after eviction.");
        assertTrue(cache.containsKey("B"), "Cache should contain key 'B'.");
        assertTrue(cache.containsKey("C"), "Cache should contain key 'C'.");
    }
}
