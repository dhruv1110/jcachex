package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.DefaultCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdleTimeEvictionStrategyTest {
    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        CacheConfig<String, String> cacheConfig = new CacheConfig.Builder<String, String>()
            .maximumSize(2L)
            .evictionStrategy(new IdleTimeEvictionStrategy<>(Duration.ofMillis(500)))
            .build();
        cache = new DefaultCache<>(cacheConfig);
        cache.put("A", "ValueA"); // order = 1
        cache.put("B", "ValueB"); // order = 2
    }

    @Test
    void testAllEntriesReachedMaxIdleTime()  {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            // Code to execute after the delay
            cache.put("C", "ValueC"); // This should trigger eviction
            assertFalse(cache.containsKey("A"), "Cache should not contain key 'A' after eviction.");
            assertTrue(cache.containsKey("B"), "Cache should contain key 'B'.");
            assertTrue(cache.containsKey("C"), "Cache should contain key 'C'.");
        }, 600, TimeUnit.MILLISECONDS);

        // Shutdown the scheduler after use
        scheduler.shutdown();

    }
}
