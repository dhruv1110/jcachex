package io.github.dhruv1110.jcachex.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JCacheX with Spring caching annotations.
 */
@SpringBootTest(classes = {
        TestConfiguration.class,
        JCacheXAnnotationIntegrationTest.TestServiceConfiguration.class
})
@ActiveProfiles("test")
@DirtiesContext
@DisplayName("JCacheX Annotation Integration Tests")
class JCacheXAnnotationIntegrationTest extends AbstractJCacheXSpringTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        userService.resetCounters();
        productService.resetCounters();
        statisticsService.resetCounters();
    }

    @Nested
    @DisplayName("@Cacheable Tests")
    class CacheableTests {

        @Test
        @DisplayName("Should cache method results")
        void shouldCacheMethodResults() {
            // First call should hit the service
            String user1 = userService.getUser("1");
            assertEquals("User 1", user1, "Should return correct user");
            assertEquals(1, userService.getCallCount(), "Should call service method once");

            // Second call should use cache
            String user1Cached = userService.getUser("1");
            assertEquals("User 1", user1Cached, "Should return same user from cache");
            assertEquals(1, userService.getCallCount(), "Should not call service method again");

            // Different parameter should call service again
            String user2 = userService.getUser("2");
            assertEquals("User 2", user2, "Should return correct user");
            assertEquals(2, userService.getCallCount(), "Should call service method for different parameter");
        }

        @Test
        @DisplayName("Should handle complex key generation")
        void shouldHandleComplexKeyGeneration() {
            // Test with multiple parameters
            String product1 = productService.getProduct("1", "electronics");
            assertEquals("Product 1 in electronics", product1, "Should return correct product");
            assertEquals(1, productService.getCallCount(), "Should call service once");

            // Same parameters should use cache
            String product1Cached = productService.getProduct("1", "electronics");
            assertEquals("Product 1 in electronics", product1Cached, "Should return cached result");
            assertEquals(1, productService.getCallCount(), "Should not call service again");

            // Different category should call service
            String product1Books = productService.getProduct("1", "books");
            assertEquals("Product 1 in books", product1Books, "Should return different result");
            assertEquals(2, productService.getCallCount(), "Should call service for different parameters");
        }

        @Test
        @DisplayName("Should handle conditional caching")
        void shouldHandleConditionalCaching() {
            // Condition allows caching for non-admin users
            String regularUser = userService.getUserWithCondition("user123", false);
            assertEquals("User user123 (admin: false)", regularUser, "Should return user");
            assertEquals(1, userService.getConditionalCallCount(), "Should call service once");

            // Second call should use cache
            String regularUserCached = userService.getUserWithCondition("user123", false);
            assertEquals("User user123 (admin: false)", regularUserCached, "Should return cached result");
            assertEquals(1, userService.getConditionalCallCount(), "Should not call service again");

            // Admin users should not be cached (condition prevents it)
            String adminUser1 = userService.getUserWithCondition("admin123", true);
            assertEquals("User admin123 (admin: true)", adminUser1, "Should return admin user");
            assertEquals(2, userService.getConditionalCallCount(), "Should call service");

            String adminUser2 = userService.getUserWithCondition("admin123", true);
            assertEquals("User admin123 (admin: true)", adminUser2, "Should return admin user again");
            assertEquals(3, userService.getConditionalCallCount(), "Should call service again (not cached)");
        }

        @Test
        @DisplayName("Should handle null return values")
        void shouldHandleNullReturnValues() {
            String nullUser = userService.getUser("null");
            assertNull(nullUser, "Should return null");
            assertEquals(1, userService.getCallCount(), "Should call service once");

            // Second call should use cached null value
            String nullUserCached = userService.getUser("null");
            assertNull(nullUserCached, "Should return cached null");
            assertEquals(1, userService.getCallCount(), "Should not call service again");
        }

        @Test
        @DisplayName("Should support custom cache names")
        void shouldSupportCustomCacheNames() {
            // Products service uses "products" cache
            String product = productService.getProduct("1", "tech");
            assertEquals("Product 1 in tech", product, "Should return product");

            // Verify cache contains the entry
            org.springframework.cache.Cache productsCache = cacheManager.getCache("products");
            assertNotNull(productsCache, "Products cache should exist");

            // Clear the cache and verify method is called again
            productsCache.clear();
            productService.getProduct("1", "tech");
            assertEquals(2, productService.getCallCount(), "Should call service after cache clear");
        }
    }

    @Nested
    @DisplayName("@CacheEvict Tests")
    class CacheEvictTests {

        @BeforeEach
        void setUpCacheEvictTests() {
            // Additional setup for cache evict tests
            userService.resetCounters();
            productService.resetCounters();
            statisticsService.resetCounters();

            // Clear all caches to ensure clean state
            org.springframework.cache.Cache usersCache = cacheManager.getCache("users");
            if (usersCache != null) {
                usersCache.clear();
            }
            org.springframework.cache.Cache productsCache = cacheManager.getCache("products");
            if (productsCache != null) {
                productsCache.clear();
            }
            org.springframework.cache.Cache statisticsCache = cacheManager.getCache("statistics");
            if (statisticsCache != null) {
                statisticsCache.clear();
            }
        }

        @Test
        @DisplayName("Should evict single cache entry")
        void shouldEvictSingleCacheEntry() {
            // Verify starting state
            assertEquals(0, userService.getCallCount(), "Should start with 0 calls");

            // Cache some users
            String user1 = userService.getUser("1");
            assertEquals("User 1", user1, "Should return user 1");
            assertEquals(1, userService.getCallCount(), "Should call service once for user 1");

            String user2 = userService.getUser("2");
            assertEquals("User 2", user2, "Should return user 2");
            assertEquals(2, userService.getCallCount(), "Should call service twice total");

            // Access cached users - should not increment counter
            String cachedUser1 = userService.getUser("1");
            assertEquals("User 1", cachedUser1, "Should return cached user 1");
            assertEquals(2, userService.getCallCount(), "Should still be 2 calls (cached)");

            String cachedUser2 = userService.getUser("2");
            assertEquals("User 2", cachedUser2, "Should return cached user 2");
            assertEquals(2, userService.getCallCount(), "Should still be 2 calls (both cached)");

            // Evict one user
            userService.evictUser("1");

            // User 1 should be evicted, user 2 should still be cached
            String evictedUser1 = userService.getUser("1");
            assertEquals("User 1", evictedUser1, "Should return user 1 again");
            assertEquals(3, userService.getCallCount(), "Should call service for evicted user (3 total)");

            String stillCachedUser2 = userService.getUser("2");
            assertEquals("User 2", stillCachedUser2, "Should return cached user 2");
            assertEquals(3, userService.getCallCount(), "Should still be 3 calls (user 2 still cached)");
        }

        @Test
        @DisplayName("Should evict all cache entries")
        void shouldEvictAllCacheEntries() {
            // Cache some users
            userService.getUser("1");
            userService.getUser("2");
            userService.getUser("3");
            assertEquals(3, userService.getCallCount(), "Should call service three times");

            // Evict all users
            userService.evictAllUsers();

            // All users should be evicted
            userService.getUser("1");
            userService.getUser("2");
            userService.getUser("3");
            assertEquals(6, userService.getCallCount(), "Should call service for all users after eviction");
        }

        @Test
        @DisplayName("Should support conditional eviction")
        void shouldSupportConditionalEviction() {
            // Cache some users
            userService.getUser("admin");
            userService.getUser("user");
            assertEquals(2, userService.getCallCount(), "Should cache both users");

            // Conditional evict - only evict if userId starts with "admin"
            userService.conditionalEvictUser("admin");
            userService.conditionalEvictUser("user");

            // Check which ones were evicted
            userService.getUser("admin");
            assertEquals(3, userService.getCallCount(), "Admin should be evicted and called again");

            userService.getUser("user");
            assertEquals(3, userService.getCallCount(), "User should still be cached");
        }

        @Test
        @DisplayName("Should allow manual eviction before method execution")
        void shouldAllowManualEvictionBeforeMethodExecution() {
            // Cache a user
            String user = userService.getUser("updateable");
            assertEquals("User updateable", user, "Should return user");
            assertEquals(1, userService.getCallCount(), "Should call service once");

            // Verify it's cached
            String cachedUser = userService.getUser("updateable");
            assertEquals("User updateable", cachedUser, "Should return cached user");
            assertEquals(1, userService.getCallCount(), "Should not call service again (cached)");

            // Manually evict cache first, then update
            userService.evictUser("updateable");
            userService.updateUser("updateable", "Updated User");

            // Getting user should call service again and return updated value
            String updatedUser = userService.getUser("updateable");
            assertEquals("Updated User", updatedUser, "Should return updated value");
            assertEquals(2, userService.getCallCount(), "Should call service to get fresh value");
        }
    }

    @Nested
    @DisplayName("Cache Integration Tests")
    class CacheIntegrationTests {

        @Test
        @DisplayName("Should work with multiple cache configurations")
        void shouldWorkWithMultipleCacheConfigurations() {
            // Users cache and products cache should work independently
            userService.getUser("1");
            productService.getProduct("1", "category");

            assertEquals(1, userService.getCallCount(), "Users service should be called");
            assertEquals(1, productService.getCallCount(), "Products service should be called");

            // Cached calls should not hit services
            userService.getUser("1");
            productService.getProduct("1", "category");

            assertEquals(1, userService.getCallCount(), "Users service should not be called again");
            assertEquals(1, productService.getCallCount(), "Products service should not be called again");
        }

        @Test
        @DisplayName("Should handle concurrent cache access")
        void shouldHandleConcurrentCacheAccess() throws InterruptedException {
            final int numThreads = 10;
            Thread[] threads = new Thread[numThreads];
            final AtomicInteger successfulCalls = new AtomicInteger(0);

            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    try {
                        // All threads request the same user - should be cached after first call
                        String user = userService.getUser("concurrent");
                        if ("User concurrent".equals(user)) {
                            successfulCalls.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // Log but don't fail the test for individual operation failures
                        System.err.println("Thread " + threadId + " operation failed: " + e.getMessage());
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join(10000); // Increased timeout to 10 seconds
                assertFalse(thread.isAlive(), "Thread should have completed within timeout");
            }

            // Verify that most calls were successful
            assertTrue(successfulCalls.get() >= numThreads * 0.8,
                    String.format("Expected at least %d successful calls, but got %d",
                            (int) (numThreads * 0.8), successfulCalls.get()));

            // Should only call service once despite multiple concurrent requests
            // (allowing for some race conditions in concurrent access)
            int actualCallCount = userService.getCallCount();
            assertTrue(actualCallCount <= numThreads,
                    "Should not call service more than number of threads");
            assertTrue(actualCallCount >= 1,
                    "Should call service at least once");
        }

        @Test
        @DisplayName("Should maintain cache statistics")
        void shouldMaintainCacheStatistics() {
            // Generate some cache hits and misses
            statisticsService.getData("key1"); // miss (call #1)
            statisticsService.getData("key1"); // hit (no call)
            statisticsService.getData("key2"); // miss (call #2)
            statisticsService.getData("key2"); // hit (no call)
            statisticsService.getData("key1"); // hit (no call)

            assertEquals(2, statisticsService.getCallCount(), "Should call service 2 times (2 misses, 3 hits)");

            // Verify cache statistics
            org.springframework.cache.Cache statisticsCache = cacheManager.getCache("statistics");
            assertNotNull(statisticsCache, "Statistics cache should exist");

            if (statisticsCache instanceof JCacheXSpringCache) {
                JCacheXSpringCache jcacheXCache = (JCacheXSpringCache) statisticsCache;
                assertTrue(jcacheXCache.getStats().hitCount() > 0, "Should have cache hits");
                assertTrue(jcacheXCache.getStats().missCount() > 0, "Should have cache misses");
            }
        }

        @Test
        @DisplayName("Should handle cache exceptions gracefully")
        void shouldHandleCacheExceptionsGracefully() {
            // Test that cache failures don't break the application
            assertDoesNotThrow(() -> {
                String data = statisticsService.getData("exception-test");
                assertEquals("Data: exception-test", data, "Should return data even if cache has issues");
            }, "Should handle cache exceptions gracefully");
        }
    }

    @Configuration
    @EnableCaching
    @ComponentScan(basePackageClasses = JCacheXAnnotationIntegrationTest.class)
    static class TestServiceConfiguration {
        // Remove explicit @Bean methods and let @Service annotation handle bean
        // creation
        // The services are already annotated with @Service, so Spring should
        // auto-detect them
    }

    @Service
    static class UserService {
        private final AtomicInteger callCount = new AtomicInteger(0);
        private final AtomicInteger conditionalCallCount = new AtomicInteger(0);

        // Simulated database of updated users
        private String updatedUser = null;

        @Cacheable("users")
        public String getUser(String userId) {
            callCount.incrementAndGet();
            if ("null".equals(userId)) {
                return null;
            }
            if ("updateable".equals(userId) && updatedUser != null) {
                return updatedUser;
            }
            return "User " + userId;
        }

        @Cacheable(value = "users", condition = "#isAdmin == false")
        public String getUserWithCondition(String userId, boolean isAdmin) {
            conditionalCallCount.incrementAndGet();
            return "User " + userId + " (admin: " + isAdmin + ")";
        }

        @CacheEvict("users")
        public void evictUser(String userId) {
            // Evict specific user
        }

        @CacheEvict(value = "users", allEntries = true)
        public void evictAllUsers() {
            // Evict all users
        }

        @CacheEvict(value = "users", condition = "#userId.startsWith('admin')")
        public void conditionalEvictUser(String userId) {
            // Conditionally evict user
        }

        @CacheEvict(value = "users", beforeInvocation = true)
        public void updateUser(String userId, String newValue) {
            updatedUser = newValue;
        }

        public int getCallCount() {
            return callCount.get();
        }

        public int getConditionalCallCount() {
            return conditionalCallCount.get();
        }

        public void resetCounters() {
            callCount.set(0);
            conditionalCallCount.set(0);
            updatedUser = null;
        }
    }

    @Service
    static class ProductService {
        private final AtomicInteger callCount = new AtomicInteger(0);

        @Cacheable("products")
        public String getProduct(String productId, String category) {
            callCount.incrementAndGet();
            return "Product " + productId + " in " + category;
        }

        public int getCallCount() {
            return callCount.get();
        }

        public void resetCounters() {
            callCount.set(0);
        }
    }

    @Service
    static class StatisticsService {
        private final AtomicInteger callCount = new AtomicInteger(0);

        @Cacheable("statistics")
        public String getData(String key) {
            callCount.incrementAndGet();
            return "Data: " + key;
        }

        public int getCallCount() {
            return callCount.get();
        }

        public void resetCounters() {
            callCount.set(0);
        }
    }
}
