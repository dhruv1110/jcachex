package io.github.dhruv1110.jcachex.spring;

import io.github.dhruv1110.jcachex.eviction.*;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;
import io.github.dhruv1110.jcachex.spring.utilities.EvictionStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EvictionStrategyFactory.
 */
@DisplayName("EvictionStrategyFactory Tests")
class EvictionStrategyFactoryTest {

    private EvictionStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new EvictionStrategyFactory();
    }

    @Nested
    @DisplayName("Single Strategy Creation Tests")
    class SingleStrategyCreationTests {

        @Test
        @DisplayName("Should create LRU strategy")
        void shouldCreateLRUStrategy() {
            EvictionStrategy<String, String> strategy = factory.createStrategy("LRU");

            assertNotNull(strategy, "LRU strategy should be created");
            assertTrue(strategy instanceof LRUEvictionStrategy, "Should be LRU strategy instance");
        }

        @Test
        @DisplayName("Should create LFU strategy")
        void shouldCreateLFUStrategy() {
            EvictionStrategy<String, String> strategy = factory.createStrategy("LFU");

            assertNotNull(strategy, "LFU strategy should be created");
            assertTrue(strategy instanceof LFUEvictionStrategy, "Should be LFU strategy instance");
        }

        @Test
        @DisplayName("Should create FIFO strategy")
        void shouldCreateFIFOStrategy() {
            EvictionStrategy<String, String> strategy = factory.createStrategy("FIFO");

            assertNotNull(strategy, "FIFO strategy should be created");
            assertTrue(strategy instanceof FIFOEvictionStrategy, "Should be FIFO strategy instance");
        }

        @Test
        @DisplayName("Should create FILO strategy")
        void shouldCreateFILOStrategy() {
            EvictionStrategy<String, String> strategy = factory.createStrategy("FILO");

            assertNotNull(strategy, "FILO strategy should be created");
            assertTrue(strategy instanceof FILOEvictionStrategy, "Should be FILO strategy instance");
        }

        @Test
        @DisplayName("Should create WEIGHT strategy")
        void shouldCreateWEIGHTStrategy() {
            EvictionStrategy<String, String> strategy = factory.createStrategy("WEIGHT");

            assertNotNull(strategy, "WEIGHT strategy should be created");
            assertTrue(strategy instanceof WeightBasedEvictionStrategy,
                    "Should be WeightBasedEvictionStrategy instance");
        }

        @Test
        @DisplayName("Should create IDLE_TIME strategy")
        void shouldCreateIDLETIMEStrategy() {
            EvictionStrategy<String, String> strategy = factory.createStrategy("IDLE_TIME");

            assertNotNull(strategy, "IDLE_TIME strategy should be created");
            assertTrue(strategy instanceof IdleTimeEvictionStrategy, "Should be IdleTimeEvictionStrategy instance");
        }

        @Test
        @DisplayName("Should handle case insensitive strategy names")
        void shouldHandleCaseInsensitiveStrategyNames() {
            EvictionStrategy<String, String> lruLowerCase = factory.createStrategy("lru");
            EvictionStrategy<String, String> lruMixedCase = factory.createStrategy("LrU");
            EvictionStrategy<String, String> lruUpperCase = factory.createStrategy("LRU");

            assertTrue(lruLowerCase instanceof LRUEvictionStrategy, "Lowercase should work");
            assertTrue(lruMixedCase instanceof LRUEvictionStrategy, "Mixed case should work");
            assertTrue(lruUpperCase instanceof LRUEvictionStrategy, "Uppercase should work");
        }
    }

    @Nested
    @DisplayName("Strategy Configuration Tests")
    class StrategyConfigurationTests {

        @Test
        @DisplayName("Should create strategy with configuration")
        void shouldCreateStrategyWithConfiguration() {
            JCacheXProperties.CacheConfig config = new JCacheXProperties.CacheConfig();
            config.setIdleTimeThresholdSeconds(1800L);

            EvictionStrategy<String, String> strategy = factory.createStrategy("IDLE_TIME", config);

            assertNotNull(strategy, "Strategy should be created with configuration");
            assertTrue(strategy instanceof IdleTimeEvictionStrategy, "Should be IdleTimeEvictionStrategy");
        }

        @Test
        @DisplayName("Should handle null configuration gracefully")
        void shouldHandleNullConfigurationGracefully() {
            EvictionStrategy<String, String> strategy = factory.createStrategy("LRU", null);

            assertNotNull(strategy, "Strategy should be created even with null configuration");
            assertTrue(strategy instanceof LRUEvictionStrategy, "Should be LRU strategy");
        }

        @Test
        @DisplayName("Should create WEIGHT strategy with configuration")
        void shouldCreateWEIGHTStrategyWithConfiguration() {
            JCacheXProperties.CacheConfig config = new JCacheXProperties.CacheConfig();
            config.setMaximumWeight(50000L);

            EvictionStrategy<String, String> strategy = factory.createStrategy("WEIGHT", config);

            assertNotNull(strategy, "Weight strategy should be created with configuration");
            assertTrue(strategy instanceof WeightBasedEvictionStrategy, "Should be WeightBasedEvictionStrategy");
        }

        @Test
        @DisplayName("Should create COMPOSITE strategy with configuration")
        void shouldCreateCompositeStrategyWithConfiguration() {
            JCacheXProperties.CacheConfig config = new JCacheXProperties.CacheConfig();
            config.setCompositeStrategies(Arrays.asList("LRU", "LFU"));

            EvictionStrategy<String, String> strategy = factory.createStrategy("COMPOSITE", config);

            assertNotNull(strategy, "Composite strategy should be created with configuration");
            assertTrue(strategy instanceof CompositeEvictionStrategy, "Should be CompositeEvictionStrategy");
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw exception for null strategy name")
        void shouldThrowExceptionForNullStrategyName() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> factory.createStrategy(null),
                    "Should throw exception for null strategy name");
            assertTrue(exception.getMessage().contains("Unknown eviction strategy"));
        }

        @Test
        @DisplayName("Should throw exception for empty strategy name")
        void shouldThrowExceptionForEmptyStrategyName() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> factory.createStrategy(""),
                    "Should throw exception for empty strategy name");
            assertTrue(exception.getMessage().contains("Unknown eviction strategy"));
        }

        @Test
        @DisplayName("Should throw exception for invalid strategy name")
        void shouldThrowExceptionForInvalidStrategyName() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> factory.createStrategy("INVALID"),
                    "Should throw exception for invalid strategy name");
            assertTrue(exception.getMessage().contains("Unknown eviction strategy"));
        }
    }

    @Nested
    @DisplayName("Available Strategies Tests")
    class AvailableStrategiesTests {

        @Test
        @DisplayName("Should return all available strategy names")
        void shouldReturnAllAvailableStrategyNames() {
            Set<String> availableStrategies = factory.getAvailableStrategies();

            assertNotNull(availableStrategies, "Available strategies set should not be null");
            assertFalse(availableStrategies.isEmpty(), "Available strategies set should not be empty");

            // Check that all expected strategies are present
            assertTrue(availableStrategies.contains("LRU"), "Should support LRU");
            assertTrue(availableStrategies.contains("LFU"), "Should support LFU");
            assertTrue(availableStrategies.contains("FIFO"), "Should support FIFO");
            assertTrue(availableStrategies.contains("FILO"), "Should support FILO");
            assertTrue(availableStrategies.contains("WEIGHT"), "Should support WEIGHT");
            assertTrue(availableStrategies.contains("IDLE_TIME"), "Should support IDLE_TIME");
            assertTrue(availableStrategies.contains("COMPOSITE"), "Should support COMPOSITE");
        }

        @Test
        @DisplayName("Should verify strategy availability by creating them")
        void shouldVerifyStrategyAvailabilityByCreatingThem() {
            Set<String> availableStrategies = factory.getAvailableStrategies();

            for (String strategyName : availableStrategies) {
                assertDoesNotThrow(() -> {
                    EvictionStrategy<String, String> strategy = factory.createStrategy(strategyName);
                    assertNotNull(strategy, "Strategy " + strategyName + " should be created successfully");
                }, "Creating strategy " + strategyName + " should not throw exception");
            }
        }
    }

    @Nested
    @DisplayName("Custom Strategy Registration Tests")
    class CustomStrategyRegistrationTests {

        @Test
        @DisplayName("Should register and create custom strategy")
        void shouldRegisterAndCreateCustomStrategy() {
            EvictionStrategyFactory.StrategyProvider customProvider = new EvictionStrategyFactory.StrategyProvider() {
                @Override
                public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                    return new LRUEvictionStrategy<>();
                }
            };

            factory.registerStrategy("CUSTOM", customProvider);

            assertTrue(factory.getAvailableStrategies().contains("CUSTOM"),
                    "Custom strategy should be available");

            EvictionStrategy<String, String> strategy = factory.createStrategy("CUSTOM");
            assertNotNull(strategy, "Custom strategy should be created");
            assertTrue(strategy instanceof LRUEvictionStrategy, "Custom strategy should be LRU");
        }

        @Test
        @DisplayName("Should handle case insensitive custom strategy registration")
        void shouldHandleCaseInsensitiveCustomStrategyRegistration() {
            EvictionStrategyFactory.StrategyProvider customProvider = new EvictionStrategyFactory.StrategyProvider() {
                @Override
                public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                    return new LFUEvictionStrategy<>();
                }
            };

            factory.registerStrategy("myCustom", customProvider);

            assertTrue(factory.getAvailableStrategies().contains("MYCUSTOM"),
                    "Custom strategy should be stored in uppercase");

            EvictionStrategy<String, String> strategy = factory.createStrategy("mycustom");
            assertNotNull(strategy, "Custom strategy should be created with lowercase name");
            assertTrue(strategy instanceof LFUEvictionStrategy, "Custom strategy should be LFU");
        }

        @Test
        @DisplayName("Should override built-in strategies with custom ones")
        void shouldOverrideBuiltInStrategiesWithCustomOnes() {
            EvictionStrategyFactory.StrategyProvider customLRU = new EvictionStrategyFactory.StrategyProvider() {
                @Override
                public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                    return new LFUEvictionStrategy<>();
                }
            };

            factory.registerStrategy("LRU", customLRU);

            EvictionStrategy<String, String> strategy = factory.createStrategy("LRU");
            assertNotNull(strategy, "Overridden strategy should be created");
            assertTrue(strategy instanceof LFUEvictionStrategy, "Should use custom LFU instead of LRU");
        }
    }

    @Nested
    @DisplayName("Factory Instance Tests")
    class FactoryInstanceTests {

        @Test
        @DisplayName("Should create new strategy instances each time")
        void shouldCreateNewStrategyInstancesEachTime() {
            EvictionStrategy<String, String> strategy1 = factory.createStrategy("LRU");
            EvictionStrategy<String, String> strategy2 = factory.createStrategy("LRU");

            assertNotSame(strategy1, strategy2, "Should create different instances");
        }

        @Test
        @DisplayName("Should work with different generic types")
        void shouldWorkWithDifferentGenericTypes() {
            EvictionStrategy<Integer, String> intStringStrategy = factory.createStrategy("LRU");
            EvictionStrategy<String, Integer> stringIntStrategy = factory.createStrategy("LRU");
            EvictionStrategy<Long, Long> longLongStrategy = factory.createStrategy("LRU");

            assertNotNull(intStringStrategy, "Integer-String strategy should be created");
            assertNotNull(stringIntStrategy, "String-Integer strategy should be created");
            assertNotNull(longLongStrategy, "Long-Long strategy should be created");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle strategy name with surrounding whitespace")
        void shouldHandleStrategyNameWithSurroundingWhitespace() {
            // The factory normalizes by doing toUpperCase(), but doesn't trim whitespace
            // So this should fail
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> factory.createStrategy("  LRU  "),
                    "Should reject strategy names with whitespace");
            assertTrue(exception.getMessage().contains("Unknown eviction strategy"));
        }

        @Test
        @DisplayName("Should handle very long strategy names")
        void shouldHandleVeryLongStrategyNames() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("A");
            }
            String longInvalidName = sb.toString();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> factory.createStrategy(longInvalidName),
                    "Should handle very long invalid names");
            assertTrue(exception.getMessage().contains("Unknown eviction strategy"));
        }

        @Test
        @DisplayName("Should handle special characters in strategy names")
        void shouldHandleSpecialCharactersInStrategyNames() {
            String specialCharName = "LRU@#$%";

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> factory.createStrategy(specialCharName),
                    "Should handle special characters in names");
            assertTrue(exception.getMessage().contains("Unknown eviction strategy"));
        }
    }
}
