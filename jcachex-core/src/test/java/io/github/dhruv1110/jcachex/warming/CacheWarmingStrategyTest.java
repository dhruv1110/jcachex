package io.github.dhruv1110.jcachex.warming;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for CacheWarmingStrategy interface.
 */
class CacheWarmingStrategyTest {

    @Test
    @DisplayName("CacheWarmingStrategy interface exists")
    void testCacheWarmingStrategyInterfaceExists() {
        // Just verify the interface exists and can be referenced
        assertNotNull(CacheWarmingStrategy.class);
        assertTrue(CacheWarmingStrategy.class.isInterface());
    }

    @Test
    @DisplayName("WarmingResult class exists")
    void testWarmingResultClassExists() {
        // Just verify the class exists and can be referenced
        assertNotNull(WarmingResult.class);
        assertFalse(WarmingResult.class.isInterface());
    }

    @Test
    @DisplayName("WarmingContext interface exists")
    void testWarmingContextInterfaceExists() {
        // Just verify the interface exists and can be referenced
        assertNotNull(WarmingContext.class);
        assertTrue(WarmingContext.class.isInterface());
    }
}
