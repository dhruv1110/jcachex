package io.github.dpflux.jcachex.eviction;

import io.github.dpflux.jcachex.Cache;
import io.github.dpflux.jcachex.CacheConfig;
import io.github.dpflux.jcachex.DefaultCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FILOEvictionStrategyTest {
    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        CacheConfig<String, String> cacheConfig = new CacheConfig.Builder<String, String>()
            .maximumSize(2L)
            .evictionStrategy(new FILOEvictionStrategy<>())
            .build();
        cache = new DefaultCache<>(cacheConfig);
        cache.put("A", "ValueA"); // order = 1
        cache.put("B", "ValueB"); // order = 2
    }

    @Test
    void testSelectEvictionCandidate() {
        cache.put("A", "NewValueA"); // order unchanged = 1
        cache.put("C", "ValueC"); // order = 3

        assertTrue(cache.containsKey("A"), "Cache should contain key 'A'.");
        assertTrue(cache.containsKey("B"), "Cache should contain key 'B'.");
        assertFalse(cache.containsKey("C"), "Cache should not contain key 'C' after eviction.");
    }

    @Test
    void testClearAccessCounts() {
        cache.clear();
        cache.put("C", "ValueC"); // order = 1
        cache.put("D", "ValueD"); // order = 2
        cache.put("E", "ValueE"); // order = 3
        assertFalse(cache.containsKey("A"), "Cache should not contain key 'A' after clear.");
        assertFalse(cache.containsKey("B"), "Cache should not contain key 'B' after clear.");
        assertFalse(cache.containsKey("E"), "Cache should not contain key 'E' after eviction.");
    }
}
