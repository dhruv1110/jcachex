package io.github.dpflux.jcachex.eviction;

import io.github.dpflux.jcachex.Cache;
import io.github.dpflux.jcachex.CacheConfig;
import io.github.dpflux.jcachex.DefaultCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LFUEvictionStrategyTest {

    private LFUEvictionStrategy<String, String> strategy;
    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        strategy = new LFUEvictionStrategy<>();
        CacheConfig<String, String> cacheConfig = new CacheConfig.Builder<String, String>()
                .maximumSize(2L)
                .evictionStrategy(strategy)
                .build();
        cache = new DefaultCache<>(cacheConfig);
        cache.put("A", "ValueA"); // used count = 1
        cache.put("B", "ValueB"); // used count = 1
    }

    @Test
    void testSelectEvictionCandidate() {
        cache.put("A", "ValueA1"); // used count = 2
        cache.get("A"); // used count = 3
        cache.get("B"); // used count = 2
        cache.put("C", "ValueC"); // used count = 1

        assertTrue(cache.containsKey("A"), "Cache should contain key 'A'.");
        assertTrue(cache.containsKey("B"), "Cache should contain key 'B'.");
        assertFalse(cache.containsKey("C"), "Cache should not contain key 'C'.");
    }

    @Test
    void testClearAccessCounts() {
        cache.clear();
        cache.put("A", "ValueA"); // used count = 1
        cache.put("B", "ValueB"); // used count = 1
        cache.get("B"); // used count = 2
        cache.put("C", "ValueC"); // used count = 1

        assertEquals(2, cache.size(), "Cache size should be 2 after adding C.");
        assertTrue(cache.containsKey("B"), "Cache should contain key 'B'.");
    }
}
