package io.github.dhruv1110.jcachex.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation that indicates a method should trigger cache eviction using
 * JCacheX.
 *
 * This annotation provides declarative cache eviction for Spring Boot
 * applications,
 * allowing cached entries to be automatically removed when data is modified or
 * becomes
 * invalid. It integrates seamlessly with Spring's caching infrastructure and
 * provides
 * fine-grained control over which entries are evicted.
 *
 * <h2>Basic Usage Examples:</h2>
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;Service
 *     public class UserService {
 *
 *         &#64;JCacheXCacheable(cacheName = "users")
 *         public User findUserById(String id) {
 *             return userRepository.findById(id);
 *         }
 *
 *         &#64;JCacheXCacheEvict(cacheName = "users")
 *         public User updateUser(User user) {
 *             User updated = userRepository.save(user);
 *             // Cache entry for this user will be evicted automatically
 *             return updated;
 *         }
 *
 *         @JCacheXCacheEvict(cacheName = "users", allEntries = true)
 *         public void clearAllUsers() {
 *             userRepository.deleteAll();
 *             // All entries in the "users" cache will be evicted
 *         }
 *     }
 * }
 * </pre>
 *
 * <h2>Advanced Configuration Examples:</h2>
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;Service
 *     public class UserService {
 *
 *         // Evict specific user by ID
 *         &#64;JCacheXCacheEvict(cacheName = "users", key = "#userId")
 *         public void deleteUser(String userId) {
 *             userRepository.deleteById(userId);
 *         }
 *
 *         // Evict multiple related caches
 *         &#64;JCacheXCacheEvict(cacheName = "users", key = "#user.id")
 *         &#64;JCacheXCacheEvict(cacheName = "userProfiles", key = "#user.id")
 *         @JCacheXCacheEvict(cacheName = "userPermissions", key = "#user.id")
 *         public User updateUserRole(User user, Role newRole) {
 *             user.setRole(newRole);
 *             return userRepository.save(user);
 *         }
 *
 *         // Conditional eviction
 *         &#64;JCacheXCacheEvict(cacheName = "userStats", condition = "#user.isActive()", key = "#user.id")
 *         public void updateUserActivity(User user) {
 *             // Only evict cache for active users
 *             userRepository.updateLastActivity(user.getId());
 *         }
 *
 *         // Evict before method execution
 *         &#64;JCacheXCacheEvict(cacheName = "calculations", beforeInvocation = true, key = "#input")
 *         public Result recalculate(String input) {
 *             // Cache is evicted before method runs
 *             // This ensures fresh calculation even if method fails
 *             return performCalculation(input);
 *         }
 *     }
 * }
 * </pre>
 *
 * <h2>Multi-Cache Eviction:</h2>
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;Service
 *     public class ProductService {
 *
 *         // Evict from multiple caches simultaneously
 *         &#64;JCacheXCacheEvict(cacheName = "products", key = "#product.id")
 *         &#64;JCacheXCacheEvict(cacheName = "productCategories", key = "#product.categoryId")
 *         @JCacheXCacheEvict(cacheName = "productSearch", allEntries = true)
 *         public Product updateProduct(Product product) {
 *             Product updated = productRepository.save(product);
 *             // This will evict:
 *             // - Specific product from products cache
 *             // - Category products from productCategories cache
 *             // - All entries from productSearch cache
 *             return updated;
 *         }
 *     }
 * }
 * </pre>
 *
 * <h2>Scheduled Cache Maintenance:</h2>
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;Service
 *     public class CacheMaintenanceService {
 *
 *         &#64;Scheduled(fixedRate = 3600000) // Every hour
 *         &#64;JCacheXCacheEvict(cacheName = "temporaryData", allEntries = true)
 *         public void cleanupTemporaryCache() {
 *             // Periodically clear temporary cache
 *             logger.info("Cleared temporary cache");
 *         }
 *
 *         &#64;Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
 *         &#64;JCacheXCacheEvict(cacheName = "reports", allEntries = true)
 *         @JCacheXCacheEvict(cacheName = "analytics", allEntries = true)
 *         public void clearDailyReports() {
 *             // Clear report caches daily
 *             logger.info("Cleared daily report caches");
 *         }
 *     }
 * }
 * </pre>
 *
 * <h2>SpEL Expression Examples:</h2>
 *
 * <pre>
 * {@code
 * // Method parameters
 * &#64;JCacheXCacheEvict(key = "#userId + '_' + #type")
 * public void updateUserData(String userId, DataType type, Object data) { ... }
 *
 * // Object properties
 * &#64;JCacheXCacheEvict(key = "#user.id + '_profile'")
 * public void updateUserProfile(User user, UserProfile profile) { ... }
 *
 * // Complex conditions
 * &#64;JCacheXCacheEvict(
 *     condition = "#user.role == 'ADMIN' and #critical",
 *     key = "#user.departmentId"
 * )
 * public void updateDepartmentData(User user, boolean critical, DepartmentData data) { ... }
 * }
 * </pre>
 *
 * <h2>Error Handling:</h2>
 *
 * <pre>{@code @Service
 * public class DataService { @JCacheXCacheEvict(cacheName = "sensitiveData", beforeInvocation = true // Evict even if method fails
 *     )
 *     public void updateSensitiveData(SensitiveData data) {
 *         // Cache is evicted before method runs
 *         // This ensures data consistency even if update fails
 *         try {
 *             dataRepository.save(data);
 * } catch (Exception e) {
 * // Cache was already evicted, so no stale data remains
 * throw e;
 * }
 * }
 * }
 * }</pre>
 *
 * <h2>Best Practices:</h2>
 * <ul>
 * <li><strong>Granular Eviction</strong>: Use specific keys rather than
 * clearing entire caches when possible</li>
 * <li><strong>Timing</strong>: Use {@code beforeInvocation = true} for critical
 * operations to ensure consistency</li>
 * <li><strong>Multiple Caches</strong>: Consider all related caches that may
 * need eviction when data changes</li>
 * <li><strong>Performance</strong>: Be mindful of eviction frequency and its
 * impact on cache effectiveness</li>
 * <li><strong>Testing</strong>: Verify eviction behavior in integration
 * tests</li>
 * <li><strong>Monitoring</strong>: Monitor cache hit rates to ensure eviction
 * strategies are effective</li>
 * </ul>
 *
 * @see JCacheXCacheable
 * @see JCacheXProperties
 * @see org.springframework.cache.annotation.CacheEvict
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JCacheXCacheEvict {

    /**
     * The name of the cache to evict from. If empty, defaults to the fully
     * qualified method name.
     *
     * @return the cache name
     */
    String cacheName() default "";

    /**
     * The SpEL expression to compute the cache key to evict. If empty, all method
     * parameters are used.
     *
     * @return the SpEL expression for the cache key
     */
    String key() default "";

    /**
     * The SpEL expression to determine if eviction should occur. Empty means always
     * evict.
     *
     * @return the SpEL expression for the condition
     */
    String condition() default "";

    /**
     * Whether to evict all entries in the cache rather than just the computed key.
     *
     * @return true to evict all entries, false to evict only the computed key
     */
    boolean allEntries() default false;

    /**
     * Whether to evict before the method is invoked. If false, eviction
     * happens after successful method execution.
     *
     * @return true to evict before invocation, false to evict after
     */
    boolean beforeInvocation() default false;
}
