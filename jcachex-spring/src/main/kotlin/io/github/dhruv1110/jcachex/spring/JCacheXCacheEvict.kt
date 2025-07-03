package io.github.dhruv1110.jcachex.spring

import java.lang.annotation.Inherited

/**
 * Annotation that indicates a method should trigger cache eviction using JCacheX.
 *
 * This annotation provides declarative cache eviction for Spring Boot applications,
 * allowing cached entries to be automatically removed when data is modified or becomes
 * invalid. It integrates seamlessly with Spring's caching infrastructure and provides
 * fine-grained control over which entries are evicted.
 *
 * ## Basic Usage Examples:
 * ```kotlin
 * @Service
 * class UserService {
 *
 *     @JCacheXCacheable(cacheName = "users")
 *     fun findUserById(id: String): User {
 *         return userRepository.findById(id)
 *     }
 *
 *     @JCacheXCacheEvict(cacheName = "users")
 *     fun updateUser(user: User): User {
 *         val updated = userRepository.save(user)
 *         // Cache entry for this user will be evicted automatically
 *         return updated
 *     }
 *
 *     @JCacheXCacheEvict(cacheName = "users", allEntries = true)
 *     fun clearAllUsers() {
 *         userRepository.deleteAll()
 *         // All entries in the "users" cache will be evicted
 *     }
 * }
 * ```
 *
 * ## Advanced Configuration Examples:
 * ```kotlin
 * @Service
 * class UserService {
 *
 *     // Evict specific user by ID
 *     @JCacheXCacheEvict(
 *         cacheName = "users",
 *         key = "#userId"
 *     )
 *     fun deleteUser(userId: String) {
 *         userRepository.deleteById(userId)
 *     }
 *
 *     // Evict multiple related caches
 *     @JCacheXCacheEvict(cacheName = "users", key = "#user.id")
 *     @JCacheXCacheEvict(cacheName = "userProfiles", key = "#user.id")
 *     @JCacheXCacheEvict(cacheName = "userPermissions", key = "#user.id")
 *     fun updateUserRole(user: User, newRole: Role): User {
 *         user.role = newRole
 *         return userRepository.save(user)
 *     }
 *
 *     // Conditional eviction
 *     @JCacheXCacheEvict(
 *         cacheName = "userStats",
 *         condition = "#user.isActive()",
 *         key = "#user.id"
 *     )
 *     fun updateUserActivity(user: User) {
 *         // Only evict cache for active users
 *         userRepository.updateLastActivity(user.id)
 *     }
 *
 *     // Evict before method execution
 *     @JCacheXCacheEvict(
 *         cacheName = "calculations",
 *         beforeInvocation = true,
 *         key = "#input"
 *     )
 *     fun recalculate(input: String): Result {
 *         // Cache is evicted before method runs
 *         // This ensures fresh calculation even if method fails
 *         return performCalculation(input)
 *     }
 * }
 * ```
 *
 * ## Multi-Cache Eviction:
 * ```kotlin
 * @Service
 * class ProductService {
 *
 *     // Evict from multiple caches simultaneously
 *     @JCacheXCacheEvict(cacheName = "products", key = "#product.id")
 *     @JCacheXCacheEvict(cacheName = "productCategories", key = "#product.categoryId")
 *     @JCacheXCacheEvict(cacheName = "productSearch", allEntries = true)
 *     fun updateProduct(product: Product): Product {
 *         val updated = productRepository.save(product)
 *         // This will evict:
 *         // - Specific product from products cache
 *         // - Category products from productCategories cache
 *         // - All entries from productSearch cache
 *         return updated
 *     }
 * }
 * ```
 *
 * ## Scheduled Cache Maintenance:
 * ```kotlin
 * @Service
 * class CacheMaintenanceService {
 *
 *     @Scheduled(fixedRate = 3600000) // Every hour
 *     @JCacheXCacheEvict(cacheName = "temporaryData", allEntries = true)
 *     fun cleanupTemporaryCache() {
 *         // Periodically clear temporary cache
 *         logger.info("Cleared temporary cache")
 *     }
 *
 *     @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
 *     @JCacheXCacheEvict(cacheName = "reports", allEntries = true)
 *     @JCacheXCacheEvict(cacheName = "analytics", allEntries = true)
 *     fun clearDailyReports() {
 *         // Clear report caches daily
 *         logger.info("Cleared daily report caches")
 *     }
 * }
 * ```
 *
 * ## SpEL Expression Examples:
 * ```kotlin
 * // Method parameters
 * @JCacheXCacheEvict(key = "#userId + '_' + #type")
 * fun updateUserData(userId: String, type: DataType, data: Any)
 *
 * // Object properties
 * @JCacheXCacheEvict(key = "#user.id + '_profile'")
 * fun updateUserProfile(user: User, profile: UserProfile)
 *
 * // Complex conditions
 * @JCacheXCacheEvict(
 *     condition = "#user.role == 'ADMIN' and #critical",
 *     key = "#user.departmentId"
 * )
 * fun updateDepartmentData(user: User, critical: Boolean, data: DepartmentData)
 * ```
 *
 * ## Error Handling:
 * ```kotlin
 * @Service
 * class DataService {
 *
 *     @JCacheXCacheEvict(
 *         cacheName = "sensitiveData",
 *         beforeInvocation = true  // Evict even if method fails
 *     )
 *     fun updateSensitiveData(data: SensitiveData) {
 *         // Cache is evicted before method runs
 *         // This ensures data consistency even if update fails
 *         try {
 *             dataRepository.save(data)
 *         } catch (e: Exception) {
 *             // Cache was already evicted, so no stale data remains
 *             throw e
 *         }
 *     }
 * }
 * ```
 *
 * ## Best Practices:
 * - **Granular Eviction**: Use specific keys rather than clearing entire caches when possible
 * - **Timing**: Use `beforeInvocation = true` for critical operations to ensure consistency
 * - **Multiple Caches**: Consider all related caches that may need eviction when data changes
 * - **Performance**: Be mindful of eviction frequency and its impact on cache effectiveness
 * - **Testing**: Verify eviction behavior in integration tests
 * - **Monitoring**: Monitor cache hit rates to ensure eviction strategies are effective
 *
 * @property cacheName The name of the cache to evict from. If empty, defaults to the fully qualified method name.
 * @property key The SpEL expression to compute the cache key to evict. If empty, all method parameters are used.
 * @property condition The SpEL expression to determine if eviction should occur. Empty means always evict.
 * @property allEntries Whether to evict all entries in the cache rather than just the computed key.
 * @property beforeInvocation Whether to evict before the method is invoked. If false, eviction
 *                              happens after successful method execution.
 *
 * @see JCacheXCacheable
 * @see JCacheXProperties
 * @see org.springframework.cache.annotation.CacheEvict
 * @since 1.0.0
 */
@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class JCacheXCacheEvict(
    val cacheName: String = "",
    val key: String = "",
    val condition: String = "",
    val allEntries: Boolean = false,
    val beforeInvocation: Boolean = false,
)
