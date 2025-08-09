package io.github.dhruv1110.jcachex;

import io.github.dhruv1110.jcachex.eviction.LRUEvictionStrategy;
import io.github.dhruv1110.jcachex.exceptions.CacheConfigurationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for CacheConfig class and its builder.
 */
class CacheConfigTest {

    @Nested
    @DisplayName("CacheConfig Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Basic configuration getters")
        void testBasicGetters() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumSize(100L)
                    .recordStats(true)
                    .initialCapacity(32)
                    .concurrencyLevel(8)
                    .directory("/tmp/cache")
                    .build();

            assertEquals(100L, config.getMaximumSize());
            assertTrue(config.isRecordStats());
            assertEquals(32, config.getInitialCapacity());
            assertEquals(8, config.getConcurrencyLevel());
            assertEquals("/tmp/cache", config.getDirectory());
        }

        @Test
        @DisplayName("Weight-based configuration getters")
        void testWeightGetters() {
            BiFunction<String, String, Long> weigher = (key, value) -> (long) value.length();
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumWeight(500L)
                    .weigher(weigher)
                    .build();

            assertEquals(500L, config.getMaximumWeight());
            assertEquals(weigher, config.getWeigher());
            assertNull(config.getMaximumSize());
        }

        @Test
        @DisplayName("Expiration configuration getters")
        void testExpirationGetters() {
            Duration writeExpiry = Duration.ofMinutes(30);
            Duration accessExpiry = Duration.ofMinutes(15);
            Duration refreshDuration = Duration.ofMinutes(10);

            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .expireAfterWrite(writeExpiry)
                    .expireAfterAccess(accessExpiry)
                    .refreshAfterWrite(refreshDuration)
                    .build();

            assertEquals(writeExpiry, config.getExpireAfterWrite());
            assertEquals(accessExpiry, config.getExpireAfterAccess());
            assertEquals(refreshDuration, config.getRefreshAfterWrite());
        }

        @Test
        @DisplayName("Reference configuration getters")
        void testReferenceGetters() {
            CacheConfig<String, String> weakKeysConfig = CacheConfig.<String, String>builder()
                    .weakKeys(true)
                    .build();

            CacheConfig<String, String> weakValuesConfig = CacheConfig.<String, String>builder()
                    .weakValues(true)
                    .build();

            CacheConfig<String, String> softValuesConfig = CacheConfig.<String, String>builder()
                    .softValues(true)
                    .build();

            assertTrue(weakKeysConfig.isWeakKeys());
            assertFalse(weakKeysConfig.isWeakValues());
            assertFalse(weakKeysConfig.isSoftValues());

            assertFalse(weakValuesConfig.isWeakKeys());
            assertTrue(weakValuesConfig.isWeakValues());
            assertFalse(weakValuesConfig.isSoftValues());

            assertFalse(softValuesConfig.isWeakKeys());
            assertFalse(softValuesConfig.isWeakValues());
            assertTrue(softValuesConfig.isSoftValues());
        }

        @Test
        @DisplayName("Loader configuration getters")
        void testLoaderGetters() {
            Function<String, String> loader = key -> "loaded_" + key;
            Function<String, CompletableFuture<String>> asyncLoader = key -> CompletableFuture
                    .completedFuture("async_" + key);

            CacheConfig<String, String> loaderConfig = CacheConfig.<String, String>builder()
                    .loader(loader)
                    .build();

            CacheConfig<String, String> asyncLoaderConfig = CacheConfig.<String, String>builder()
                    .asyncLoader(asyncLoader)
                    .build();

            assertEquals(loader, loaderConfig.getLoader());
            assertNull(loaderConfig.getAsyncLoader());

            assertEquals(asyncLoader, asyncLoaderConfig.getAsyncLoader());
            assertNull(asyncLoaderConfig.getLoader());
        }

        @Test
        @DisplayName("Eviction strategy getter")
        void testEvictionStrategyGetter() {
            LRUEvictionStrategy<String, String> strategy = new LRUEvictionStrategy<>();
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .evictionStrategy(strategy)
                    .build();

            assertEquals(strategy, config.getEvictionStrategy());
        }

        @Test
        @DisplayName("Event listeners getter")
        void testEventListenersGetter() {
            CacheEventListener<String, String> listener1 = CacheEventListener.noOp();
            CacheEventListener<String, String> listener2 = CacheEventListener.noOp();

            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .addListener(listener1)
                    .addListener(listener2)
                    .build();

            assertEquals(2, config.getListeners().size());
            assertTrue(config.getListeners().contains(listener1));
            assertTrue(config.getListeners().contains(listener2));
        }

        @Test
        @DisplayName("Null configuration getters")
        void testNullGetters() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .build();

            assertNull(config.getMaximumSize());
            assertNull(config.getMaximumWeight());
            assertNull(config.getWeigher());
            assertNull(config.getExpireAfterWrite());
            assertNull(config.getExpireAfterAccess());
            assertNull(config.getRefreshAfterWrite());
            assertNotNull(config.getEvictionStrategy()); // Default eviction strategy is now set
            assertTrue(config
                    .getEvictionStrategy() instanceof io.github.dhruv1110.jcachex.eviction.WindowTinyLFUEvictionStrategy);
            assertNull(config.getLoader());
            assertNull(config.getAsyncLoader());
            assertNull(config.getDirectory());
            assertFalse(config.isWeakKeys());
            assertFalse(config.isWeakValues());
            assertFalse(config.isSoftValues());
            assertEquals(16, config.getInitialCapacity()); // Default is 16
            assertEquals(16, config.getConcurrencyLevel()); // Default is 16
            assertTrue(config.getListeners().isEmpty());
        }
    }

    @Nested
    @DisplayName("CacheConfig Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Builder method chaining")
        void testBuilderMethodChaining() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumSize(100L)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .recordStats(true)
                    .initialCapacity(32)
                    .concurrencyLevel(8)
                    .directory("/tmp/cache")
                    .weakKeys(true)
                    .build();

            assertEquals(100L, config.getMaximumSize());
            assertEquals(Duration.ofMinutes(30), config.getExpireAfterWrite());
            assertTrue(config.isRecordStats());
            assertEquals(32, config.getInitialCapacity());
            assertEquals(8, config.getConcurrencyLevel());
            assertEquals("/tmp/cache", config.getDirectory());
            assertTrue(config.isWeakKeys());
        }

        @Test
        @DisplayName("Builder with TimeUnit expiration methods")
        void testBuilderWithTimeUnitMethods() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .expireAfterWrite(30, TimeUnit.MINUTES)
                    .expireAfterAccess(15, TimeUnit.MINUTES)
                    .build();

            assertEquals(Duration.ofMinutes(30), config.getExpireAfterWrite());
            assertEquals(Duration.ofMinutes(15), config.getExpireAfterAccess());
        }

        @Test
        @DisplayName("Builder with reference configurations")
        void testBuilderReferenceConfigurations() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .weakKeys(true)
                    .weakValues(false)
                    .softValues(true)
                    .build();

            assertTrue(config.isWeakKeys());
            assertFalse(config.isWeakValues());
            assertTrue(config.isSoftValues());
        }

        @Test
        @DisplayName("Builder with multiple listeners")
        void testBuilderMultipleListeners() {
            CacheEventListener<String, String> listener1 = CacheEventListener.noOp();
            CacheEventListener<String, String> listener2 = CacheEventListener.noOp();
            CacheEventListener<String, String> listener3 = CacheEventListener.noOp();

            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .addListener(listener1)
                    .addListener(listener2)
                    .addListener(listener3)
                    .build();

            assertEquals(3, config.getListeners().size());
            assertTrue(config.getListeners().contains(listener1));
            assertTrue(config.getListeners().contains(listener2));
            assertTrue(config.getListeners().contains(listener3));
        }

        @Test
        @DisplayName("Builder with edge case values")
        void testBuilderEdgeCaseValues() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumSize(1L) // Minimum size
                    .expireAfterWrite(Duration.ofNanos(1)) // Minimum duration
                    .expireAfterAccess(Duration.ofNanos(1)) // Minimum duration
                    .refreshAfterWrite(Duration.ofNanos(1)) // Minimum duration
                    .initialCapacity(1)
                    .concurrencyLevel(1)
                    .recordStats(false)
                    .build();

            assertEquals(1L, config.getMaximumSize());
            assertEquals(Duration.ofNanos(1), config.getExpireAfterWrite());
            assertEquals(Duration.ofNanos(1), config.getExpireAfterAccess());
            assertEquals(Duration.ofNanos(1), config.getRefreshAfterWrite());
            assertEquals(1, config.getInitialCapacity());
            assertEquals(1, config.getConcurrencyLevel());
            assertFalse(config.isRecordStats());
        }

        @Test
        @DisplayName("Builder weight configuration")
        void testBuilderWeightConfiguration() {
            BiFunction<String, String, Long> weigher = (key, value) -> (long) (key.length() + value.length());
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumWeight(1000L)
                    .weigher(weigher)
                    .build();

            assertEquals(1000L, config.getMaximumWeight());
            assertEquals(weigher, config.getWeigher());
        }

        @Test
        @DisplayName("Builder loader configuration")
        void testBuilderLoaderConfiguration() {
            Function<String, String> loader = key -> "loaded_" + key;
            Function<String, CompletableFuture<String>> asyncLoader = key -> CompletableFuture
                    .completedFuture("async_" + key);

            CacheConfig<String, String> loaderConfig = CacheConfig.<String, String>builder()
                    .loader(loader)
                    .refreshAfterWrite(Duration.ofMinutes(5))
                    .build();

            CacheConfig<String, String> asyncLoaderConfig = CacheConfig.<String, String>builder()
                    .asyncLoader(asyncLoader)
                    .build();

            assertEquals(loader, loaderConfig.getLoader());
            assertEquals(Duration.ofMinutes(5), loaderConfig.getRefreshAfterWrite());
            assertEquals(asyncLoader, asyncLoaderConfig.getAsyncLoader());
        }
    }

    @Nested
    @DisplayName("CacheConfig Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Invalid maximum size validation")
        void testInvalidMaximumSizeValidation() {
            assertThrows(CacheConfigurationException.class, () -> {
                CacheConfig.<String, String>builder()
                        .maximumSize(0L)
                        .build();
            });

            assertThrows(CacheConfigurationException.class, () -> {
                CacheConfig.<String, String>builder()
                        .maximumSize(-1L)
                        .build();
            });

            assertThrows(CacheConfigurationException.class, () -> {
                CacheConfig.<String, String>builder()
                        .maximumSize(-100L)
                        .build();
            });
        }

        @Test
        @DisplayName("Invalid maximum weight validation")
        void testInvalidMaximumWeightValidation() {
            assertThrows(CacheConfigurationException.class, () -> {
                CacheConfig.<String, String>builder()
                        .maximumWeight(0L)
                        .build();
            });

            assertThrows(CacheConfigurationException.class, () -> {
                CacheConfig.<String, String>builder()
                        .maximumWeight(-1L)
                        .build();
            });

            assertThrows(CacheConfigurationException.class, () -> {
                CacheConfig.<String, String>builder()
                        .maximumWeight(-100L)
                        .build();
            });
        }

        @Test
        @DisplayName("Missing weigher validation")
        void testMissingWeigherValidation() {
            assertThrows(CacheConfigurationException.class, () -> {
                CacheConfig.<String, String>builder()
                        .maximumWeight(100L)
                        // Missing weigher
                        .build();
            });
        }

        @Test
        @DisplayName("Conflicting size and weight validation")
        void testConflictingSizeAndWeightValidation() {
            assertThrows(CacheConfigurationException.class, () -> {
                CacheConfig.<String, String>builder()
                        .maximumSize(100L)
                        .maximumWeight(1000L)
                        .weigher((key, value) -> 1L)
                        .build();
            });
        }

        @Test
        @DisplayName("Conflicting weak and soft values validation")
        void testConflictingWeakAndSoftValuesValidation() {
            assertThrows(CacheConfigurationException.class, () -> {
                CacheConfig.<String, String>builder()
                        .weakValues(true)
                        .softValues(true)
                        .build();
            });
        }

        @Test
        @DisplayName("Conflicting loader and async loader validation")
        void testConflictingLoaderAndAsyncLoaderValidation() {
            assertThrows(CacheConfigurationException.class, () -> {
                CacheConfig.<String, String>builder()
                        .loader(key -> "loaded_" + key)
                        .asyncLoader(key -> CompletableFuture.completedFuture("async_" + key))
                        .build();
            });
        }

        @Test
        @DisplayName("Negative expiration duration validation")
        void testNegativeExpirationDurationValidation() {
            assertThrows(CacheConfigurationException.class, () -> {
                CacheConfig.<String, String>builder()
                        .expireAfterWrite(Duration.ofMinutes(-1))
                        .build();
            });
        }

        @Test
        @DisplayName("Valid edge case configurations")
        void testValidEdgeCaseConfigurations() {
            // All these should be valid
            assertDoesNotThrow(() -> {
                CacheConfig.<String, String>builder()
                        .maximumSize(1L)
                        .build();
            });

            assertDoesNotThrow(() -> {
                CacheConfig.<String, String>builder()
                        .maximumWeight(1L)
                        .weigher((key, value) -> 1L)
                        .build();
            });

            assertDoesNotThrow(() -> {
                CacheConfig.<String, String>builder()
                        .expireAfterWrite(Duration.ofNanos(1))
                        .build();
            });

            assertDoesNotThrow(() -> {
                CacheConfig.<String, String>builder()
                        .expireAfterAccess(Duration.ofNanos(1))
                        .build();
            });

            assertDoesNotThrow(() -> {
                CacheConfig.<String, String>builder()
                        .refreshAfterWrite(Duration.ofNanos(1))
                        .build();
            });
        }
    }

    @Nested
    @DisplayName("CacheConfig Factory Methods Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("newBuilder factory method")
        void testNewBuilderFactoryMethod() {
            CacheConfig.Builder<String, String> builder = CacheConfig.newBuilder();
            assertNotNull(builder);

            CacheConfig<String, String> config = builder
                    .maximumSize(100L)
                    .recordStats(true)
                    .build();

            assertEquals(100L, config.getMaximumSize());
            assertTrue(config.isRecordStats());
        }

        @Test
        @DisplayName("builder factory method")
        void testBuilderFactoryMethod() {
            CacheConfig.Builder<String, String> builder = CacheConfig.builder();
            assertNotNull(builder);

            CacheConfig<String, String> config = builder
                    .maximumSize(200L)
                    .recordStats(false)
                    .build();

            assertEquals(200L, config.getMaximumSize());
            assertFalse(config.isRecordStats());
        }

        @Test
        @DisplayName("Both factory methods return different instances")
        void testFactoryMethodsReturnDifferentInstances() {
            CacheConfig.Builder<String, String> builder1 = CacheConfig.newBuilder();
            CacheConfig.Builder<String, String> builder2 = CacheConfig.builder();

            assertNotSame(builder1, builder2);
        }
    }

    @Nested
    @DisplayName("CacheConfig Builder Null Handling Tests")
    class NullHandlingTests {

        @Test
        @DisplayName("Builder accepts null values for optional parameters")
        void testBuilderNullHandling() {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .maximumSize(null)
                    .maximumWeight(null)
                    .weigher(null)
                    .expireAfterWrite(null)
                    .expireAfterAccess(null)
                    .refreshAfterWrite(null)
                    .evictionStrategy(null)
                    .loader(null)
                    .asyncLoader(null)
                    .directory(null)
                    .build();

            assertNull(config.getMaximumSize());
            assertNull(config.getMaximumWeight());
            assertNull(config.getWeigher());
            assertNull(config.getExpireAfterWrite());
            assertNull(config.getExpireAfterAccess());
            assertNull(config.getRefreshAfterWrite());
            assertNotNull(config.getEvictionStrategy()); // Default eviction strategy is now applied
            assertTrue(config
                    .getEvictionStrategy() instanceof io.github.dhruv1110.jcachex.eviction.WindowTinyLFUEvictionStrategy);
            assertNull(config.getLoader());
            assertNull(config.getAsyncLoader());
            assertNull(config.getDirectory());
        }

        @Test
        @DisplayName("Builder validation with null maximum size")
        void testBuilderValidationWithNullMaximumSize() {
            assertDoesNotThrow(() -> {
                CacheConfig.<String, String>builder()
                        .maximumSize(null)
                        .build();
            });
        }

        @Test
        @DisplayName("Builder validation with null maximum weight")
        void testBuilderValidationWithNullMaximumWeight() {
            assertDoesNotThrow(() -> {
                CacheConfig.<String, String>builder()
                        .maximumWeight(null)
                        .build();
            });
        }
    }
}
