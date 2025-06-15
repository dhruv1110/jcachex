package io.github.dpflux.jcachex.eviction;

import io.github.dpflux.jcachex.Cache;
import io.github.dpflux.jcachex.CacheConfig;
import io.github.dpflux.jcachex.DefaultCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FIFOEvictionStrategyTest {
    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        CacheConfig<String, String> cacheConfig = new CacheConfig.Builder<String, String>()
            .maximumSize(2L)
            .evictionStrategy(new FIFOEvictionStrategy<>())
            .build();
        cache = new DefaultCache<>(cacheConfig);
        cache.put("A", "ValueA"); // order = 1
        cache.put("B", "ValueB"); // order = 2
    }

    @Test
    void testSelectEvictionCandidate() {
        cache.put("A", "NewValueA"); // order unchanged = 1
        cache.put("C", "ValueC"); // order = 3

        assertFalse(cache.containsKey("A"), "Cache should not contain key 'A' after eviction.");
        assertTrue(cache.containsKey("B"), "Cache should contain key 'B'.");
        assertTrue(cache.containsKey("C"), "Cache should contain key 'C'.");
    }

    @Test
    void testClearAccessCounts() {
        cache.clear();
        cache.put("C", "ValueC"); // order = 1
        cache.put("D", "ValueD"); // order = 2
        cache.put("E", "ValueE"); // order = 3
        assertFalse(cache.containsKey("A"), "Cache should not contain key 'A' after clear.");
        assertFalse(cache.containsKey("B"), "Cache should not contain key 'B' after clear.");
        assertFalse(cache.containsKey("C"), "Cache should not contain key 'C' after eviction.");
    }
}
