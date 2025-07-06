package io.github.dhruv1110.jcachex.spring;

import io.github.dhruv1110.jcachex.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JCacheXCacheManager.
 */
@DisplayName("JCacheXCacheManager Tests")
class JCacheXCacheManagerTest {

    private JCacheXCacheManager cacheManager;
    private JCacheXProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JCacheXProperties();
        cacheManager = new JCacheXCacheManager(properties);
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should create cache manager with default properties")
        void shouldCreateCacheManagerWithDefaultProperties() {
            JCacheXCacheManager defaultManager = new JCacheXCacheManager();
            assertNotNull(defaultManager, "Cache manager should be created");
            assertTrue(defaultManager.getCacheNames().isEmpty(), "Should have no caches initially");
        }

        @Test
        @DisplayName("Should create cache manager with custom properties")
        void shouldCreateCacheManagerWithCustomProperties() {
            assertNotNull(cacheManager, "Cache manager should be created");
            assertTrue(cacheManager.getCacheNames().isEmpty(), "Should have no caches initially");
        }

        @Test
        @DisplayName("Should handle null properties gracefully")
        void shouldHandleNullPropertiesGracefully() {
            JCacheXCacheManager nullPropsManager = new JCacheXCacheManager(null);
            assertNotNull(nullPropsManager, "Cache manager should handle null properties");
        }
    }

    @Nested
    @DisplayName("Cache Creation Tests")
    class CacheCreationTests {

        @Test
        @DisplayName("Should create cache dynamically when requested")
        void shouldCreateCacheDynamicallyWhenRequested() {
            org.springframework.cache.Cache cache = cacheManager.getCache("dynamic-cache");

            assertNotNull(cache, "Cache should be created dynamically");
            assertEquals("dynamic-cache", cache.getName(), "Cache name should match");
            assertTrue(cacheManager.getCacheNames().contains("dynamic-cache"), "Cache name should be in collection");
        }

        @Test
        @DisplayName("Should return same cache instance for same name")
        void shouldReturnSameCacheInstanceForSameName() {
            org.springframework.cache.Cache cache1 = cacheManager.getCache("test-cache");
            org.springframework.cache.Cache cache2 = cacheManager.getCache("test-cache");

            assertSame(cache1, cache2, "Should return same cache instance");
        }

        @Test
        @DisplayName("Should create different caches for different names")
        void shouldCreateDifferentCachesForDifferentNames() {
            org.springframework.cache.Cache cache1 = cacheManager.getCache("cache1");
            org.springframework.cache.Cache cache2 = cacheManager.getCache("cache2");

            assertNotSame(cache1, cache2, "Should create different cache instances");
            assertEquals("cache1", cache1.getName(), "First cache name should be correct");
            assertEquals("cache2", cache2.getName(), "Second cache name should be correct");
        }

        @Test
        @DisplayName("Should disable dynamic creation when configured")
        void shouldDisableDynamicCreationWhenConfigured() {
            cacheManager.setDynamic(false);

            org.springframework.cache.Cache cache = cacheManager.getCache("non-existent");
            assertNull(cache, "Should not create cache when dynamic creation is disabled");
        }
    }

    @Nested
    @DisplayName("Configuration-Based Cache Creation Tests")
    class ConfigurationBasedCacheCreationTests {

        @Test
        @DisplayName("Should initialize caches from configuration")
        void shouldInitializeCachesFromConfiguration() {
            // Setup configuration with named caches
            Map<String, JCacheXProperties.CacheConfig> caches = new HashMap<>();
            caches.put("users", new JCacheXProperties.CacheConfig());
            caches.put("products", new JCacheXProperties.CacheConfig());
            properties.setCaches(caches);

            cacheManager.initializeCaches();

            Collection<String> cacheNames = cacheManager.getCacheNames();
            assertTrue(cacheNames.contains("users"), "Users cache should be initialized");
            assertTrue(cacheNames.contains("products"), "Products cache should be initialized");
        }

        @Test
        @DisplayName("Should apply configuration to created caches")
        void shouldApplyConfigurationToCreatedCaches() {
            // Setup cache with specific configuration
            JCacheXProperties.CacheConfig userCacheConfig = new JCacheXProperties.CacheConfig();
            userCacheConfig.setMaximumSize(1000L);
            userCacheConfig.setExpireAfterSeconds(300L);
            userCacheConfig.setEnableStatistics(true);

            Map<String, JCacheXProperties.CacheConfig> caches = new HashMap<>();
            caches.put("users", userCacheConfig);
            properties.setCaches(caches);

            cacheManager.initializeCaches();

            org.springframework.cache.Cache springCache = cacheManager.getCache("users");
            assertNotNull(springCache, "Users cache should exist");

            // Verify it's a JCacheXSpringCache
            assertTrue(springCache instanceof JCacheXSpringCache, "Should be JCacheXSpringCache instance");

            JCacheXSpringCache jcacheXSpringCache = (JCacheXSpringCache) springCache;
            Cache<Object, Object> nativeCache = jcacheXSpringCache.getJCacheXCache();
            assertNotNull(nativeCache, "Native cache should exist");
        }

        @Test
        @DisplayName("Should use default configuration for unconfigured caches")
        void shouldUseDefaultConfigurationForUnconfiguredCaches() {
            // Set default configuration
            JCacheXProperties.CacheConfig defaultConfig = properties.getDefaultConfig();
            defaultConfig.setMaximumSize(500L);
            defaultConfig.setEnableStatistics(true);

            org.springframework.cache.Cache cache = cacheManager.getCache("unconfigured-cache");
            assertNotNull(cache, "Cache should be created with default configuration");

            assertTrue(cache instanceof JCacheXSpringCache, "Should be JCacheXSpringCache instance");
        }
    }

    @Nested
    @DisplayName("Cache Access Tests")
    class CacheAccessTests {

        @Test
        @DisplayName("Should return native cache when requested")
        void shouldReturnNativeCacheWhenRequested() {
            org.springframework.cache.Cache springCache = cacheManager.getCache("test-cache");
            Cache<Object, Object> nativeCache = cacheManager.getNativeCache("test-cache");

            assertNotNull(nativeCache, "Native cache should be available");
            assertSame(((JCacheXSpringCache) springCache).getJCacheXCache(), nativeCache,
                    "Native cache should be the same instance");
        }

        @Test
        @DisplayName("Should return null for non-existent native cache")
        void shouldReturnNullForNonExistentNativeCache() {
            Cache<Object, Object> nativeCache = cacheManager.getNativeCache("non-existent");
            assertNull(nativeCache, "Should return null for non-existent cache");
        }

        @Test
        @DisplayName("Should return all cache names")
        void shouldReturnAllCacheNames() {
            cacheManager.getCache("cache1");
            cacheManager.getCache("cache2");
            cacheManager.getCache("cache3");

            Collection<String> cacheNames = cacheManager.getCacheNames();
            assertEquals(3, cacheNames.size(), "Should have 3 caches");
            assertTrue(cacheNames.contains("cache1"), "Should contain cache1");
            assertTrue(cacheNames.contains("cache2"), "Should contain cache2");
            assertTrue(cacheNames.contains("cache3"), "Should contain cache3");
        }
    }

    @Nested
    @DisplayName("Null Value Handling Tests")
    class NullValueHandlingTests {

        @Test
        @DisplayName("Should allow null values by default")
        void shouldAllowNullValuesByDefault() {
            assertTrue(cacheManager.isAllowNullValues(), "Should allow null values by default");

            org.springframework.cache.Cache cache = cacheManager.getCache("null-test");
            assertTrue(cache instanceof JCacheXSpringCache, "Should be JCacheXSpringCache");

            // This should not throw an exception
            cache.put("key", null);
            assertNull(cache.get("key").get(), "Should store and retrieve null values");
        }

        @Test
        @DisplayName("Should respect null value configuration")
        void shouldRespectNullValueConfiguration() {
            cacheManager.setAllowNullValues(false);
            assertFalse(cacheManager.isAllowNullValues(), "Should not allow null values when configured");

            // Note: The actual null value handling is in JCacheXSpringCache,
            // this test verifies the configuration is stored correctly
        }
    }

    @Nested
    @DisplayName("Dynamic Cache Management Tests")
    class DynamicCacheManagementTests {

        @Test
        @DisplayName("Should enable dynamic cache creation by default")
        void shouldEnableDynamicCacheCreationByDefault() {
            assertTrue(cacheManager.isDynamic(), "Dynamic cache creation should be enabled by default");
        }

        @Test
        @DisplayName("Should allow disabling dynamic cache creation")
        void shouldAllowDisablingDynamicCacheCreation() {
            cacheManager.setDynamic(false);
            assertFalse(cacheManager.isDynamic(), "Dynamic cache creation should be disabled");
        }

        @Test
        @DisplayName("Should update cache names collection dynamically")
        void shouldUpdateCacheNamesCollectionDynamically() {
            assertTrue(cacheManager.getCacheNames().isEmpty(), "Should start with no caches");

            cacheManager.getCache("cache1");
            assertEquals(1, cacheManager.getCacheNames().size(), "Should have 1 cache after creation");

            cacheManager.getCache("cache2");
            assertEquals(2, cacheManager.getCacheNames().size(), "Should have 2 caches after creation");
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work as Spring CacheManager")
        void shouldWorkAsSpringCacheManager() {
            CacheManager springCacheManager = cacheManager;

            org.springframework.cache.Cache cache = springCacheManager.getCache("integration-test");
            assertNotNull(cache, "Should get cache through CacheManager interface");

            // Test basic cache operations through Spring interface
            cache.put("key1", "value1");
            assertEquals("value1", cache.get("key1").get(), "Should store and retrieve values");

            cache.evict("key1");
            assertNull(cache.get("key1"), "Should evict values");

            cache.put("key2", "value2");
            cache.clear();
            assertNull(cache.get("key2"), "Should clear all values");
        }

        @Test
        @DisplayName("Should handle concurrent cache access")
        void shouldHandleConcurrentCacheAccess() throws InterruptedException {
            final int numThreads = 10;
            final int cacheOpsPerThread = 100;
            Thread[] threads = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < cacheOpsPerThread; j++) {
                        String cacheName = "cache-" + threadId;
                        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                        assertNotNull(cache, "Cache should be created for thread " + threadId);

                        cache.put("key-" + j, "value-" + threadId + "-" + j);
                        assertNotNull(cache.get("key-" + j), "Value should be retrievable");
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join(5000); // 5 second timeout
            }

            assertEquals(numThreads, cacheManager.getCacheNames().size(),
                    "Should have created caches for all threads");
        }
    }
}
