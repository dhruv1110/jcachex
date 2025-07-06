package io.github.dhruv1110.jcachex.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Annotation that indicates a method's result should be cached using JCacheX.
 *
 * This annotation provides declarative caching for Spring Boot applications,
 * allowing
 * method results to be automatically cached and retrieved without manual cache
 * management.
 * It integrates seamlessly with Spring's caching infrastructure while providing
 * JCacheX-specific configuration options.
 *
 * <h2>Basic Usage Examples:</h2>
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;Service
 *     public class UserService {
 *
 *         @JCacheXCacheable(cacheName = "users")
 *         public User findUserById(String id) {
 *             // This method will be cached - subsequent calls with the same id
 *             // will return the cached result instead of executing the method
 *             return userRepository.findById(id);
 *         }
 *
 *         &#64;JCacheXCacheable(cacheName = "userProfiles", expireAfterWrite = 30, expireAfterWriteUnit = TimeUnit.MINUTES)
 *         public UserProfile getUserProfile(String userId) {
 *             return buildUserProfile(userId);
 *         }
 *     }
 * }
 * </pre>
 *
 * <h2>Advanced Configuration Examples:</h2>
 * 
 * <pre>{@code @Service
 * public class ApiService {
 *
 *     // Custom cache key using SpEL @JCacheXCacheable(cacheName = "apiResponses", key = "#endpoint + '_' + #version", expireAfterWrite = 5, expireAfterWriteUnit = TimeUnit.MINUTES)
 *     public ApiResponse callApi(String endpoint, String version) {
 *         return httpClient.get(endpoint + "?v=" + version);
 *     }
 *
 *     // Conditional caching @JCacheXCacheable(cacheName = "calculations", condition = "#input > 100", // Only cache for large inputs
 *             unless = "#result.isEmpty()" // Don't cache empty results
 *     )
 *     public List<String> performCalculation(int input) {
 *         return heavyComputation(input);
 *     }
 *
 *     // Size-limited cache @JCacheXCacheable(cacheName = "limitedCache", maximumSize = 1000, expireAfterWrite = 1, expireAfterWriteUnit = TimeUnit.HOURS)
 *     public ExpensiveData getExpensiveData(String key) {
 *         return computeExpensiveData(key);
 *     }
 * }
 * }</pre>
 *
 * <h2>Spring Configuration:</h2>
 * <pre>{@code
 * &#64;Configuration
 * &#64;EnableCaching
 * public class CacheConfig {
 *
 *     @Bean
 *     public CacheManager cacheManager() {
 *         return new JCacheXCacheManager();
 *     }
 * }
 * }
 * </pre>
 *
 * <h2>Application Properties:</h2>
 * 
 * <pre>{@code
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
 * }</pre>
 *
 * <h2>SpEL Expression Examples:</h2>
 * <pre>{@code
 * // Method parameters
 * &#64;JCacheXCacheable(key = "#userId + '_' + #type")
 * public UserData getUserData(String userId, DataType type) { ... }
 *
 * // Object properties
 * &#64;JCacheXCacheable(key = "#user.id + '_' + #user.role")
 * public Set<Permission> getPermissions(User user) { ... }
 *
 * // Method name and parameters
 * @JCacheXCacheable(key = "#root.methodName + '_' + #p0")
 * public Result computeResult(String input) { ... }
 *
 * // Complex conditions
 * &#64;JCacheXCacheable(
 *     condition = "#user.isActive() and #includeDetails",
 *     unless = "#result == null or #result.isEmpty()"
 * )
 * public UserDetails getUserDetails(User user, boolean includeDetails) { ... }
 * }
 * </pre>
 *
 * <h2>Best Practices:</h2>
 * <ul>
 * <li><strong>Cache Key Design</strong>: Use meaningful, unique cache keys that
 * include all relevant parameters</li>
 * <li><strong>Expiration Strategy</strong>: Set appropriate expiration times
 * based on data freshness requirements</li>
 * <li><strong>Cache Size</strong>: Configure maximum cache size to prevent
 * memory issues</li>
 * <li><strong>Conditional Caching</strong>: Use {@code condition} and
 * {@code unless} to avoid caching inappropriate results</li>
 * <li><strong>Testing</strong>: Verify cache behavior in integration tests</li>
 * <li><strong>Monitoring</strong>: Enable statistics to monitor cache
 * performance</li>
 * </ul>
 *
 * @see JCacheXCacheEvict
 * @see JCacheXProperties
 * @see org.springframework.cache.annotation.Cacheable
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JCacheXCacheable {

    /**
     * The name of the cache to use. If empty, defaults to the fully qualified
     * method name.
     *
     * @return the cache name
     */
    String cacheName() default "";

    /**
     * The SpEL expression to compute the cache key. If empty, all method parameters
     * are used.
     *
     * @return the SpEL expression for the cache key
     */
    String key() default "";

    /**
     * The SpEL expression to determine if caching should occur. Empty means always
     * cache.
     *
     * @return the SpEL expression for the condition
     */
    String condition() default "";

    /**
     * The SpEL expression to determine if caching should NOT occur. Empty means
     * never skip caching.
     *
     * @return the SpEL expression for the unless condition
     */
    String unless() default "";

    /**
     * The time after which the entry should expire after being written.
     *
     * @return the expiration time, -1 means no expiration
     */
    long expireAfterWrite() default -1;

    /**
     * The time unit for expireAfterWrite.
     *
     * @return the time unit for expiration
     */
    TimeUnit expireAfterWriteUnit() default TimeUnit.SECONDS;

    /**
     * The maximum number of entries in the cache. -1 means no limit.
     *
     * @return the maximum cache size
     */
    long maximumSize() default -1;
}
