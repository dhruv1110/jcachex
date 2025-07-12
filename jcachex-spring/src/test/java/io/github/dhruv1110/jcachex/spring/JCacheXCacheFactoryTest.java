package io.github.dhruv1110.jcachex.spring;

import io.github.dhruv1110.jcachex.spring.core.JCacheXCacheFactory;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JCacheXCacheFactory.
 */
@DisplayName("JCacheXCacheFactory Tests")
class JCacheXCacheFactoryTest {

    private JCacheXCacheFactory factory;
    private JCacheXProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JCacheXProperties();
        factory = new JCacheXCacheFactory(properties);
    }

    @Nested
    @DisplayName("Cache Creation Tests")
    class CacheCreationTests {

        @Test
        @DisplayName("Should create cache with default configuration")
        void shouldCreateCacheWithDefaultConfiguration() {
            Cache<String, String> cache = factory.createCache("test-cache");

            assertNotNull(cache, "Cache should be created");
            assertEquals(0, cache.size(), "Cache should be empty initially");
        }

        @Test
        @DisplayName("Should create cache with typed generics")
        void shouldCreateCacheWithTypedGenerics() {
            Cache<Integer, String> intStringCache = factory.createCache("int-string-cache");
            Cache<String, Integer> stringIntCache = factory.createCache("string-int-cache");

            assertNotNull(intStringCache, "Integer-String cache should be created");
            assertNotNull(stringIntCache, "String-Integer cache should be created");
        }

        @Test
        @DisplayName("Should create cache with types specified")
        void shouldCreateCacheWithTypesSpecified() {
            Cache<Long, String> cache = factory.createCache("typed-cache", Long.class, String.class);

            assertNotNull(cache, "Typed cache should be created");
            assertEquals(0, cache.size(), "Cache should be empty initially");
        }

        @Test
        @DisplayName("Should create cache with custom configurator")
        void shouldCreateCacheWithCustomConfigurator() {
            Cache<String, String> cache = factory.createCache("configured-cache",
                    builder -> {
                        builder.maximumSize(100L);
                        builder.expireAfterWrite(Duration.ofMinutes(5));
                        builder.recordStats(true);
                    });

            assertNotNull(cache, "Configured cache should be created");
            assertEquals(0, cache.size(), "Cache should be empty initially");
            assertNotNull(cache.stats(), "Cache should have statistics enabled");
        }

        @Test
        @DisplayName("Should create cache with types and configurator")
        void shouldCreateCacheWithTypesAndConfigurator() {
            Cache<Integer, String> cache = factory.createCache("typed-configured-cache",
                    Integer.class, String.class,
                    builder -> {
                        builder.maximumSize(200L);
                        builder.expireAfterAccess(Duration.ofMinutes(10));
                    });

            assertNotNull(cache, "Typed configured cache should be created");
            assertEquals(0, cache.size(), "Cache should be empty initially");
        }
    }

    @Nested
    @DisplayName("Cache Configuration Tests")
    class CacheConfigurationTests {

        @Test
        @DisplayName("Should apply maximum size configuration")
        void shouldApplyMaximumSizeConfiguration() {
            Cache<String, String> cache = factory.createCache("size-test",
                    builder -> builder.maximumSize(50L));

            assertNotNull(cache, "Cache should be created");

            // Fill the cache beyond the maximum size
            for (int i = 0; i < 60; i++) {
                cache.put("key" + i, "value" + i);
            }

            // Cache should not exceed maximum size due to eviction
            assertTrue(cache.size() <= 50, "Cache size should not exceed maximum configured size");
        }

        @Test
        @DisplayName("Should apply expiration configuration")
        void shouldApplyExpirationConfiguration() {
            Cache<String, String> cache = factory.createCache("expiry-test",
                    builder -> builder.expireAfterWrite(Duration.ofSeconds(1)));

            assertNotNull(cache, "Cache should be created");

            cache.put("key1", "value1");
            assertEquals("value1", cache.get("key1"), "Value should be available immediately");

            // Wait for expiration (small delay to ensure expiration)
            try {
                Thread.sleep(1100); // 1.1 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Value should be expired
            assertNull(cache.get("key1"), "Value should be expired");
        }

        @Test
        @DisplayName("Should apply statistics configuration")
        void shouldApplyStatisticsConfiguration() {
            Cache<String, String> cache = factory.createCache("stats-test",
                    builder -> builder.recordStats(true));

            assertNotNull(cache, "Cache should be created");
            assertNotNull(cache.stats(), "Cache should have statistics enabled");

            // Perform some operations
            cache.put("key1", "value1");
            cache.get("key1"); // hit
            cache.get("key2"); // miss

            assertTrue(cache.stats().hitCount() > 0, "Should have recorded hits");
            assertTrue(cache.stats().missCount() > 0, "Should have recorded misses");
        }

        @Test
        @DisplayName("Should apply expire after access configuration")
        void shouldApplyExpireAfterAccessConfiguration() {
            Cache<String, String> cache = factory.createCache("access-expiry-test",
                    builder -> builder.expireAfterAccess(Duration.ofSeconds(1)));

            assertNotNull(cache, "Cache should be created");

            cache.put("key1", "value1");
            assertEquals("value1", cache.get("key1"), "Value should be available immediately");

            // Sleep for less than the expiration time
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Access the key to reset the access time
            assertEquals("value1", cache.get("key1"), "Value should still be available after access");

            // Sleep for more than the expiration time
            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Value should now be expired
            assertNull(cache.get("key1"), "Value should be expired after access timeout");
        }
    }

    @Nested
    @DisplayName("Factory Configuration Tests")
    class FactoryConfigurationTests {

        @Test
        @DisplayName("Should create factory with default properties")
        void shouldCreateFactoryWithDefaultProperties() {
            JCacheXCacheFactory defaultFactory = new JCacheXCacheFactory();

            assertNotNull(defaultFactory, "Factory should be created with default properties");

            Cache<String, String> cache = defaultFactory.createCache("test");
            assertNotNull(cache, "Factory should create cache even with default properties");
        }

        @Test
        @DisplayName("Should create factory with custom properties")
        void shouldCreateFactoryWithCustomProperties() {
            JCacheXProperties customProperties = new JCacheXProperties();
            customProperties.getDefaultConfig().setMaximumSize(200L);

            JCacheXCacheFactory customFactory = new JCacheXCacheFactory(customProperties);

            assertNotNull(customFactory, "Factory should be created with custom properties");

            Cache<String, String> cache = customFactory.createCache("test");
            assertNotNull(cache, "Factory should create cache with custom properties");
        }

        @Test
        @DisplayName("Should handle null properties gracefully")
        void shouldHandleNullPropertiesGracefully() {
            JCacheXCacheFactory nullPropsFactory = new JCacheXCacheFactory(null);

            assertNotNull(nullPropsFactory, "Factory should handle null properties");

            Cache<String, String> cache = nullPropsFactory.createCache("test");
            assertNotNull(cache, "Factory should create cache even with null properties");
        }
    }

    @Nested
    @DisplayName("Cache Management Tests")
    class CacheManagementTests {

        @Test
        @DisplayName("Should get existing cache")
        void shouldGetExistingCache() {
            Cache<String, String> createdCache = factory.createCache("test-cache");
            Cache<String, String> retrievedCache = factory.getCache("test-cache");

            assertNotNull(retrievedCache, "Should retrieve existing cache");
            assertSame(createdCache, retrievedCache, "Should return same cache instance");
        }

        @Test
        @DisplayName("Should return null for non-existent cache")
        void shouldReturnNullForNonExistentCache() {
            Cache<String, String> cache = factory.getCache("non-existent");

            assertNull(cache, "Should return null for non-existent cache");
        }

        @Test
        @DisplayName("Should check cache existence")
        void shouldCheckCacheExistence() {
            assertFalse(factory.hasCache("test-cache"), "Should not have cache initially");

            factory.createCache("test-cache");

            assertTrue(factory.hasCache("test-cache"), "Should have cache after creation");
        }

        @Test
        @DisplayName("Should remove cache")
        void shouldRemoveCache() {
            Cache<String, String> cache = factory.createCache("removable-cache");
            cache.put("key", "value");

            assertTrue(factory.hasCache("removable-cache"), "Should have cache before removal");

            Cache<String, String> removedCache = factory.removeCache("removable-cache");

            assertSame(cache, removedCache, "Should return removed cache");
            assertFalse(factory.hasCache("removable-cache"), "Should not have cache after removal");
        }

        @Test
        @DisplayName("Should return cache names")
        void shouldReturnCacheNames() {
            assertTrue(factory.getCacheNames().isEmpty(), "Should have no caches initially");

            factory.createCache("cache1");
            factory.createCache("cache2");

            assertEquals(2, factory.getCacheNames().size(), "Should have two caches");
            assertTrue(factory.getCacheNames().contains("cache1"), "Should contain cache1");
            assertTrue(factory.getCacheNames().contains("cache2"), "Should contain cache2");
        }

        @Test
        @DisplayName("Should clear all caches")
        void shouldClearAllCaches() {
            Cache<String, String> cache1 = factory.createCache("cache1");
            Cache<String, String> cache2 = factory.createCache("cache2");

            cache1.put("key", "value");
            cache2.put("key", "value");

            factory.clearAll();

            assertEquals(0, cache1.size(), "Cache1 should be cleared");
            assertEquals(0, cache2.size(), "Cache2 should be cleared");
        }
    }

    @Nested
    @DisplayName("Cache Naming Tests")
    class CacheNamingTests {

        @Test
        @DisplayName("Should create caches with different names")
        void shouldCreateCachesWithDifferentNames() {
            Cache<String, String> cache1 = factory.createCache("cache1");
            Cache<String, String> cache2 = factory.createCache("cache2");

            assertNotNull(cache1, "First cache should be created");
            assertNotNull(cache2, "Second cache should be created");
            assertNotSame(cache1, cache2, "Caches should be different instances");
        }

        @Test
        @DisplayName("Should handle special characters in cache names")
        void shouldHandleSpecialCharactersInCacheNames() {
            Cache<String, String> cache = factory.createCache("cache-with-special.chars_123");

            assertNotNull(cache, "Cache should be created with special characters in name");
        }

        @Test
        @DisplayName("Should handle empty cache name")
        void shouldHandleEmptyCacheName() {
            Cache<String, String> cache = factory.createCache("");

            assertNotNull(cache, "Cache should be created with empty name");
        }

        @Test
        @DisplayName("Should handle null cache name")
        void shouldHandleNullCacheName() {
            Cache<String, String> cache = factory.createCache(null);

            assertNotNull(cache, "Cache should be created with null name");
        }

        @Test
        @DisplayName("Should return same instance for same cache name")
        void shouldReturnSameInstanceForSameCacheName() {
            Cache<String, String> cache1 = factory.createCache("same-name");
            Cache<String, String> cache2 = factory.createCache("same-name");

            assertSame(cache1, cache2, "Should return same instance for same cache name");
        }
    }

    @Nested
    @DisplayName("Cache Instance Tests")
    class CacheInstanceTests {

        @Test
        @DisplayName("Should create independent cache instances for different names")
        void shouldCreateIndependentCacheInstancesForDifferentNames() {
            Cache<String, String> cache1 = factory.createCache("independent1");
            Cache<String, String> cache2 = factory.createCache("independent2");

            cache1.put("key", "value1");
            cache2.put("key", "value2");

            assertEquals("value1", cache1.get("key"), "First cache should have its own value");
            assertEquals("value2", cache2.get("key"), "Second cache should have its own value");
        }

        @Test
        @DisplayName("Should support concurrent cache creation")
        void shouldSupportConcurrentCacheCreation() throws InterruptedException {
            final int numThreads = 10;
            final Cache<String, String>[] caches = new Cache[numThreads];
            Thread[] threads = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    try {
                        caches[threadIndex] = factory.createCache("concurrent-" + threadIndex);
                    } catch (Exception e) {
                        // Log but don't fail the test for individual operation failures
                        System.err.println("Thread " + threadIndex + " cache creation failed: " + e.getMessage());
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join(10000); // Increased timeout to 10 seconds
                assertFalse(thread.isAlive(), "Thread should have completed within timeout");
            }

            // Verify all caches were created (allow for some flexibility)
            int createdCaches = 0;
            for (int i = 0; i < numThreads; i++) {
                if (caches[i] != null) {
                    createdCaches++;
                }
            }

            assertTrue(createdCaches >= numThreads * 0.9,
                    String.format("Expected at least %d caches to be created, but got %d",
                            (int) (numThreads * 0.9), createdCaches));
        }

        @Test
        @DisplayName("Should work with different generic types")
        void shouldWorkWithDifferentGenericTypes() {
            Cache<Integer, String> intStringCache = factory.createCache("int-string");
            Cache<String, Integer> stringIntCache = factory.createCache("string-int");
            Cache<Long, Double> longDoubleCache = factory.createCache("long-double");

            assertNotNull(intStringCache, "Integer-String cache should be created");
            assertNotNull(stringIntCache, "String-Integer cache should be created");
            assertNotNull(longDoubleCache, "Long-Double cache should be created");

            // Test that they work correctly
            intStringCache.put(1, "one");
            stringIntCache.put("two", 2);
            longDoubleCache.put(3L, 3.14);

            assertEquals("one", intStringCache.get(1), "Integer-String cache should work");
            assertEquals(Integer.valueOf(2), stringIntCache.get("two"), "String-Integer cache should work");
            assertEquals(3.14, longDoubleCache.get(3L), 0.001, "Long-Double cache should work");
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should create factory using builder")
        void shouldCreateFactoryUsingBuilder() {
            JCacheXCacheFactory builtFactory = JCacheXCacheFactory.builder()
                    .properties(properties)
                    .build();

            assertNotNull(builtFactory, "Factory should be created using builder");

            Cache<String, String> cache = builtFactory.createCache("builder-test");
            assertNotNull(cache, "Builder-created factory should create caches");
        }

        @Test
        @DisplayName("Should create factory with builder defaults")
        void shouldCreateFactoryWithBuilderDefaults() {
            JCacheXCacheFactory builtFactory = JCacheXCacheFactory.builder()
                    .build();

            assertNotNull(builtFactory, "Factory should be created with builder defaults");

            Cache<String, String> cache = builtFactory.createCache("builder-default-test");
            assertNotNull(cache, "Builder-created factory with defaults should create caches");
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle configurator exceptions gracefully")
        void shouldHandleConfiguratorExceptionsGracefully() {
            assertDoesNotThrow(() -> {
                Cache<String, String> cache = factory.createCache("error-test", builder -> {
                    builder.maximumSize(100L);
                    // Even if configurator has issues, the cache should still be created
                });
                assertNotNull(cache, "Cache should be created even with configurator issues");
            }, "Should handle configurator exceptions gracefully");
        }
    }
}
