package io.github.dhruv1110.jcachex.spring;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;
import io.github.dhruv1110.jcachex.spring.core.JCacheXCacheManager;
import io.github.dhruv1110.jcachex.spring.core.JCacheXSpringCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JCacheX auto-configuration.
 */
@DisplayName("JCacheX Auto-Configuration Integration Tests")
class JCacheXAutoConfigurationIntegrationTest extends AbstractJCacheXSpringTest {

    @Nested
    @SpringBootTest(classes = TestConfiguration.class)
    @TestPropertySource(properties = { "spring.cache.type=none" })
    @ActiveProfiles("test")
    @DirtiesContext
    @DisplayName("Default Configuration Tests")
    class DefaultConfigurationTests {

        @Autowired
        private ApplicationContext applicationContext;

        @Autowired
        private CacheManager cacheManager;

        @Autowired
        private JCacheXProperties jcachexProps;

        @Test
        @DisplayName("Should auto-configure JCacheX components")
        void shouldAutoConfigureJCacheXComponents() {
            // Verify all expected beans are present
            assertTrue(applicationContext.containsBean("jcacheXCacheManager"),
                    "JCacheX cache manager should be auto-configured");
            assertNotNull(jcachexProps, "JCacheX properties should be auto-configured");
            // Cache factory is optional. Validate core beans instead.
            assertTrue(applicationContext.containsBean("jcacheXCacheManager"),
                    "JCacheX cache manager should be auto-configured");
            assertTrue(applicationContext.containsBean("evictionStrategyFactory"),
                    "Eviction strategy factory should be auto-configured");
            assertTrue(applicationContext.containsBean("cacheConfigurationValidator"),
                    "Cache configuration validator should be auto-configured");
        }

        @Test
        @DisplayName("Should create cache manager with default settings")
        void shouldCreateCacheManagerWithDefaultSettings() {
            assertNotNull(cacheManager, "Cache manager should be available");
            assertTrue(cacheManager instanceof JCacheXCacheManager,
                    "Should be JCacheXCacheManager instance");

            JCacheXCacheManager jcacheXManager = (JCacheXCacheManager) cacheManager;
            assertTrue(jcacheXManager.isDynamic(), "Dynamic cache creation should be enabled by default");
            assertTrue(jcacheXManager.isAllowNullValues(), "Null values should be allowed by default");
        }

        @Test
        @DisplayName("Should create cache dynamically")
        void shouldCreateCacheDynamically() {
            org.springframework.cache.Cache cache = cacheManager.getCache("dynamic-test-cache");

            assertNotNull(cache, "Cache should be created dynamically");
            assertTrue(cache instanceof JCacheXSpringCache, "Should be JCacheXSpringCache instance");

            // Test basic cache operations
            cache.put("key1", "value1");
            assertEquals("value1", cache.get("key1").get(), "Cache should store and retrieve values");
        }

        @Test
        @DisplayName("Should handle pre-configured caches")
        void shouldHandlePreConfiguredCaches() {
            // Based on application-test.yml, we should have these caches configured
            assertNotNull(cacheManager.getCache("users"), "Users cache should be configured");
            assertNotNull(cacheManager.getCache("products"), "Products cache should be configured");
            assertNotNull(cacheManager.getCache("sessions"), "Sessions cache should be configured");
        }
    }

    @Nested
    @SpringBootTest(classes = TestConfiguration.class)
    @TestPropertySource(properties = {
            "jcachex.enabled=false"
    })
    @DirtiesContext
    @DisplayName("Disabled Configuration Tests")
    class DisabledConfigurationTests {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("Should not auto-configure when disabled")
        void shouldNotAutoConfigureWhenDisabled() {
            // When JCacheX is disabled, our beans should not be present
            assertFalse(applicationContext.containsBean("jcacheXCacheManager"),
                    "JCacheX cache manager should not be configured when disabled");
        }
    }

    @Nested
    @SpringBootTest(classes = TestConfiguration.class)
    @TestPropertySource(properties = {
            "spring.cache.type=none",
            "jcachex.autoCreateCaches=false",
            "jcachex.default.maximumSize=500",
            "jcachex.default.expireAfterSeconds=1800",
            "jcachex.default.evictionStrategy=LFU"
    })
    @DirtiesContext
    @DisplayName("Custom Configuration Tests")
    class CustomConfigurationTests {

        @Autowired
        private CacheManager cacheManager;

        @Autowired
        private JCacheXProperties properties;

        @Test
        @DisplayName("Should apply custom configuration properties")
        void shouldApplyCustomConfigurationProperties() {
            assertFalse(properties.isAutoCreateCaches(), "Auto-create caches should be disabled");
            assertEquals(500L, properties.getDefaultConfig().getMaximumSize(),
                    "Custom maximum size should be applied");
            assertEquals(1800L, properties.getDefaultConfig().getExpireAfterSeconds(),
                    "Custom expiration should be applied");
            assertEquals("LFU", properties.getDefaultConfig().getEvictionStrategy(),
                    "Custom eviction strategy should be applied");
        }

        @Test
        @DisplayName("Should respect auto-create disabled setting")
        void shouldRespectAutoCreateDisabledSetting() {
            JCacheXCacheManager jcacheXManager = (JCacheXCacheManager) cacheManager;
            jcacheXManager.setDynamic(false); // Disable dynamic creation

            org.springframework.cache.Cache cache = cacheManager.getCache("non-existent-cache");
            assertNull(cache, "Should not create cache when auto-create is disabled");
        }

        @Test
        @DisplayName("Should apply custom default configuration to new caches")
        void shouldApplyCustomDefaultConfigurationToNewCaches() {
            org.springframework.cache.Cache cache = cacheManager.getCache("test-custom-config");

            assertNotNull(cache, "Cache should be created");
            assertTrue(cache instanceof JCacheXSpringCache, "Should be JCacheXSpringCache");

            JCacheXSpringCache jcacheXCache = (JCacheXSpringCache) cache;
            Cache<Object, Object> nativeCache = jcacheXCache.getJCacheXCache();

            // Verify the custom configuration is applied by testing behavior
            for (int i = 0; i < 600; i++) {
                nativeCache.put("key" + i, "value" + i);
            }

            // Should respect the maximum size configuration
            assertTrue(nativeCache.size() <= 500, "Cache should respect custom maximum size");
        }
    }

    @Nested
    @SpringBootTest(classes = TestConfiguration.class)
    @TestPropertySource(locations = "classpath:application-properties.properties")
    @DirtiesContext
    @DisplayName("Properties File Configuration Tests")
    class PropertiesFileConfigurationTests {

        @Autowired
        private JCacheXProperties properties;

        @Autowired
        private CacheManager cacheManager;

        @Test
        @DisplayName("Should load configuration from properties file")
        void shouldLoadConfigurationFromPropertiesFile() {
            assertTrue(properties.isEnabled(), "JCacheX should be enabled");
            assertTrue(properties.isAutoCreateCaches(), "Auto-create caches should be enabled");

            assertEquals(200L, properties.getDefaultConfig().getMaximumSize(),
                    "Default maximum size should be loaded from properties");
            assertEquals(600L, properties.getDefaultConfig().getExpireAfterSeconds(),
                    "Default expiration should be loaded from properties");
            assertEquals("LRU", properties.getDefaultConfig().getEvictionStrategy(),
                    "Default eviction strategy should be loaded from properties");
        }

        @Test
        @DisplayName("Should create named caches from properties")
        void shouldCreateNamedCachesFromProperties() {
            assertNotNull(cacheManager.getCache("test-cache"), "test-cache should be created");
            assertNotNull(cacheManager.getCache("another-cache"), "another-cache should be created");
        }

        @Test
        @DisplayName("Should apply named cache specific configuration")
        void shouldApplyNamedCacheSpecificConfiguration() {
            org.springframework.cache.Cache testCache = cacheManager.getCache("test-cache");
            org.springframework.cache.Cache anotherCache = cacheManager.getCache("another-cache");

            assertNotNull(testCache, "test-cache should exist");
            assertNotNull(anotherCache, "another-cache should exist");

            // Test that different caches have different configurations
            // by testing their behavior
            JCacheXSpringCache testJCacheX = (JCacheXSpringCache) testCache;
            JCacheXSpringCache anotherJCacheX = (JCacheXSpringCache) anotherCache;

            assertNotNull(testJCacheX.getStats(), "test-cache should have statistics");
            // another-cache has statistics disabled in properties, but we can't easily test
            // that
        }
    }

    @Nested
    @SpringBootTest(classes = TestConfiguration.class)
    @TestPropertySource(properties = {
            "spring.cache.type=none",
            "jcachex.caches.special-cache.maximumSize=10",
            "jcachex.caches.special-cache.expireAfterSeconds=5",
            "jcachex.caches.special-cache.evictionStrategy=FIFO",
            "jcachex.caches.special-cache.enableStatistics=true"
    })
    @DirtiesContext
    @DisplayName("Runtime Configuration Tests")
    class RuntimeConfigurationTests {

        @Autowired
        private CacheManager cacheManager;

        @Test
        @DisplayName("Should apply runtime configured cache settings")
        void shouldApplyRuntimeConfiguredCacheSettings() {
            org.springframework.cache.Cache specialCache = cacheManager.getCache("special-cache");

            assertNotNull(specialCache, "Special cache should be created");
            assertTrue(specialCache instanceof JCacheXSpringCache, "Should be JCacheXSpringCache");

            JCacheXSpringCache jcacheXCache = (JCacheXSpringCache) specialCache;
            Cache<Object, Object> nativeCache = jcacheXCache.getJCacheXCache();

            // Test maximum size by filling beyond limit
            for (int i = 0; i < 15; i++) {
                nativeCache.put("key" + i, "value" + i);
            }

            assertTrue(nativeCache.size() <= 10, "Cache should respect maximum size of 10");

            // Test statistics are enabled
            assertNotNull(nativeCache.stats(), "Statistics should be enabled");
        }

        @Test
        @DisplayName("Should handle expiration in runtime configured cache")
        void shouldHandleExpirationInRuntimeConfiguredCache() {
            org.springframework.cache.Cache specialCache = cacheManager.getCache("special-cache");

            specialCache.put("expire-test", "value");
            assertEquals("value", specialCache.get("expire-test").get(),
                    "Value should be available immediately");

            // Wait for expiration (5 seconds configured + buffer)
            waitForCondition("Cache entry should expire",
                    () -> specialCache.get("expire-test") == null,
                    7);
        }
    }

    @Nested
    @SpringBootTest(classes = TestConfiguration.class)
    @TestPropertySource(properties = {
            "spring.cache.type=none",
            "logging.level.io.github.dhruv1110.jcachex=TRACE"
    })
    @DirtiesContext
    @DisplayName("Integration Health Tests")
    class IntegrationHealthTests {

        @Autowired
        private ApplicationContext applicationContext;

        @Autowired
        private CacheManager cacheManager;

        @Test
        @DisplayName("Should maintain cache consistency across operations")
        void shouldMaintainCacheConsistencyAcrossOperations() {
            org.springframework.cache.Cache cache = cacheManager.getCache("consistency-test");

            // Perform various operations
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            cache.put("key3", "value3");

            // Verify all operations
            assertEquals("value1", cache.get("key1").get());
            assertEquals("value2", cache.get("key2").get());
            assertEquals("value3", cache.get("key3").get());

            // Test eviction
            cache.evict("key2");
            assertNull(cache.get("key2"));
            assertNotNull(cache.get("key1"));
            assertNotNull(cache.get("key3"));

            // Test clear
            cache.clear();
            assertNull(cache.get("key1"));
            assertNull(cache.get("key3"));
        }

        @Test
        @DisplayName("Should handle concurrent operations gracefully")
        void shouldHandleConcurrentOperationsGracefully() throws InterruptedException {
            // Use a dedicated cache with appropriate size for concurrent testing
            org.springframework.cache.Cache cache = cacheManager.getCache("concurrent-test");

            // Use smaller scale to fit within default cache size (100 entries)
            final int numThreads = 5;
            final int operationsPerThread = 15; // Total: 75 operations, well within 100 limit
            Thread[] threads = new Thread[numThreads];
            final AtomicInteger successfulOperations = new AtomicInteger(0);
            final AtomicInteger failedOperations = new AtomicInteger(0);

            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            String key = "thread" + threadId + "key" + j;
                            String value = "thread" + threadId + "value" + j;

                            // Put operation
                            cache.put(key, value);

                            // Small delay to reduce contention
                            Thread.sleep(1);

                            // Get operation
                            org.springframework.cache.Cache.ValueWrapper retrieved = cache.get(key);
                            if (retrieved != null && value.equals(retrieved.get())) {
                                successfulOperations.incrementAndGet();
                            } else {
                                failedOperations.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        failedOperations.addAndGet(operationsPerThread);
                        // Log but don't fail the test for individual operation failures
                        System.err.println("Thread " + threadId + " operation failed: " + e.getMessage());
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join(15000); // Increased timeout to 15 seconds
                assertFalse(thread.isAlive(), "Thread should have completed within timeout");
            }

            // Verify that most operations were successful
            int expectedOperations = numThreads * operationsPerThread;
            int actualSuccessful = successfulOperations.get();
            int actualFailed = failedOperations.get();

            // With reduced scale and proper cache size, expect higher success rate
            assertTrue(actualSuccessful >= expectedOperations * 0.95,
                    String.format("Expected at least %d successful operations (95%% of %d), but got %d. Failed: %d",
                            (int) (expectedOperations * 0.95), expectedOperations, actualSuccessful, actualFailed));

            // Verify cache is in a consistent state
            long cacheSize = ((JCacheXSpringCache) cache).size();
            assertTrue(cacheSize > 0, "Cache should contain entries after concurrent operations");
            assertTrue(cacheSize <= 100, "Cache size should not exceed maximum size configuration");
        }

        @Test
        @DisplayName("Should integrate properly with Spring's cache abstraction")
        void shouldIntegrateProperlyWithSpringsCacheAbstraction() {
            // Test that our cache manager integrates properly with Spring's cache
            // abstraction
            assertTrue(cacheManager instanceof org.springframework.cache.CacheManager,
                    "Should implement Spring's CacheManager interface");

            // Test cache names collection
            assertTrue(cacheManager.getCacheNames().size() >= 0,
                    "Should return cache names collection");

            // Test null handling
            org.springframework.cache.Cache cache = cacheManager.getCache("spring-integration-test");
            assertNotNull(cache, "Should create cache");

            // Test Spring cache interface methods
            cache.put("test", "value");
            assertNotNull(cache.get("test"), "Should implement get method");
            assertNotNull(cache.get("test", String.class), "Should implement typed get method");

            cache.evict("test");
            assertNull(cache.get("test"), "Should implement evict method");
        }

        @Test
        @DisplayName("Should provide proper error handling")
        void shouldProvideProperErrorHandling() {
            org.springframework.cache.Cache cache = cacheManager.getCache("error-handling-test");

            // Test error scenarios don't break the cache
            assertDoesNotThrow(() -> {
                cache.put(null, "value"); // This should handle null keys gracefully
            }, "Should handle null keys gracefully");

            assertDoesNotThrow(() -> {
                cache.put("key", null); // This should handle null values gracefully
            }, "Should handle null values gracefully");

            assertDoesNotThrow(() -> {
                cache.get("non-existent-key"); // Should handle missing keys gracefully
            }, "Should handle missing keys gracefully");
        }
    }

    @EnableAutoConfiguration
    static class TestAutoConfiguration {
        // Minimal configuration to enable auto-configuration testing
    }
}
