package io.github.dhruv1110.jcachex.spring

import java.lang.annotation.Inherited
import java.util.concurrent.TimeUnit

/**
 * Annotation that indicates a method's result should be cached using JCacheX.
 *
 * This annotation provides declarative caching for Spring Boot applications, allowing
 * method results to be automatically cached and retrieved without manual cache management.
 * It integrates seamlessly with Spring's caching infrastructure while providing
 * JCacheX-specific configuration options.
 *
 * ## Basic Usage Examples:
 * ```kotlin
 * @Service
 * class UserService {
 *
 *     @JCacheXCacheable(cacheName = "users")
 *     fun findUserById(id: String): User {
 *         // This method will be cached - subsequent calls with the same id
 *         // will return the cached result instead of executing the method
 *         return userRepository.findById(id)
 *     }
 *
 *     @JCacheXCacheable(
 *         cacheName = "userProfiles",
 *         expireAfterWrite = 30,
 *         expireAfterWriteUnit = TimeUnit.MINUTES
 *     )
 *     fun getUserProfile(userId: String): UserProfile {
 *         return buildUserProfile(userId)
 *     }
 * }
 * ```
 *
 * ## Advanced Configuration Examples:
 * ```kotlin
 * @Service
 * class ApiService {
 *
 *     // Custom cache key using SpEL
 *     @JCacheXCacheable(
 *         cacheName = "apiResponses",
 *         key = "#endpoint + '_' + #version",
 *         expireAfterWrite = 5,
 *         expireAfterWriteUnit = TimeUnit.MINUTES
 *     )
 *     fun callApi(endpoint: String, version: String): ApiResponse {
 *         return httpClient.get("$endpoint?v=$version")
 *     }
 *
 *     // Conditional caching
 *     @JCacheXCacheable(
 *         cacheName = "calculations",
 *         condition = "#input > 100",  // Only cache for large inputs
 *         unless = "#result.isEmpty()" // Don't cache empty results
 *     )
 *     fun performCalculation(input: Int): List<String> {
 *         return heavyComputation(input)
 *     }
 *
 *     // Size-limited cache
 *     @JCacheXCacheable(
 *         cacheName = "limitedCache",
 *         maximumSize = 1000,
 *         expireAfterWrite = 1,
 *         expireAfterWriteUnit = TimeUnit.HOURS
 *     )
 *     fun getExpensiveData(key: String): ExpensiveData {
 *         return computeExpensiveData(key)
 *     }
 * }
 * ```
 *
 * ## Spring Configuration:
 * ```kotlin
 * @Configuration
 * @EnableCaching
 * class CacheConfig {
 *
 *     @Bean
 *     fun cacheManager(): CacheManager {
 *         return JCacheXCacheManager()
 *     }
 * }
 * ```
 *
 * ## Application Properties:
 * ```yaml
 * jcachex:
 *   default:
 *     maximumSize: 1000
 *     expireAfterSeconds: 1800  # 30 minutes
 *     enableStatistics: true
 *   caches:
 *     users:
 *       maximumSize: 5000
 *       expireAfterSeconds: 3600  # 1 hour
 *     apiResponses:
 *       maximumSize: 2000
 *       expireAfterSeconds: 300   # 5 minutes
 * ```
 *
 * ## SpEL Expression Examples:
 * ```kotlin
 * // Method parameters
 * @JCacheXCacheable(key = "#userId + '_' + #type")
 * fun getUserData(userId: String, type: DataType): UserData
 *
 * // Object properties
 * @JCacheXCacheable(key = "#user.id + '_' + #user.role")
 * fun getPermissions(user: User): Set<Permission>
 *
 * // Method name and parameters
 * @JCacheXCacheable(key = "#root.methodName + '_' + #p0")
 * fun computeResult(input: String): Result
 *
 * // Complex conditions
 * @JCacheXCacheable(
 *     condition = "#user.isActive() and #includeDetails",
 *     unless = "#result == null or #result.isEmpty()"
 * )
 * fun getUserDetails(user: User, includeDetails: Boolean): UserDetails?
 * ```
 *
 * ## Best Practices:
 * - **Cache Key Design**: Use meaningful, unique cache keys that include all relevant parameters
 * - **Expiration Strategy**: Set appropriate expiration times based on data freshness requirements
 * - **Cache Size**: Configure maximum cache size to prevent memory issues
 * - **Conditional Caching**: Use `condition` and `unless` to avoid caching inappropriate results
 * - **Testing**: Verify cache behavior in integration tests
 * - **Monitoring**: Enable statistics to monitor cache performance
 *
 * @property cacheName The name of the cache to use. If empty, defaults to the fully qualified method name.
 * @property key The SpEL expression to compute the cache key. If empty, all method parameters are used.
 * @property condition The SpEL expression to determine if caching should occur. Empty means always cache.
 * @property unless The SpEL expression to determine if caching should NOT occur. Empty means never skip caching.
 * @property expireAfterWrite The time after which the entry should expire after being written.
 * @property expireAfterWriteUnit The time unit for expireAfterWrite.
 * @property maximumSize The maximum number of entries in the cache. -1 means no limit.
 *
 * @see JCacheXCacheEvict
 * @see JCacheXProperties
 * @see org.springframework.cache.annotation.Cacheable
 * @since 1.0.0
 */
@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class JCacheXCacheable(
    val cacheName: String = "",
    val key: String = "",
    val condition: String = "",
    val unless: String = "",
    val expireAfterWrite: Long = -1,
    val expireAfterWriteUnit: TimeUnit = TimeUnit.SECONDS,
    val maximumSize: Long = -1,
)
