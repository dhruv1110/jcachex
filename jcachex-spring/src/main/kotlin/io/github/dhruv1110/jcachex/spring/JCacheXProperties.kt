package io.github.dhruv1110.jcachex.spring

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for JCacheX in Spring Boot applications.
 *
 * This class provides type-safe configuration binding for JCacheX cache settings
 * through Spring Boot's configuration properties mechanism. It allows you to configure
 * cache behavior through application.yml, application.properties, or environment variables.
 *
 * ## Application Properties Examples:
 *
 * ### YAML Configuration:
 * ```yaml
 * jcachex:
 *   default:
 *     maximumSize: 1000
 *     expireAfterSeconds: 1800  # 30 minutes
 *     enableStatistics: true
 *     enableJmx: false
 *   caches:
 *     users:
 *       maximumSize: 5000
 *       expireAfterSeconds: 3600  # 1 hour
 *       enableStatistics: true
 *       enableJmx: true
 *     sessions:
 *       maximumSize: 10000
 *       expireAfterSeconds: 1800  # 30 minutes
 *       enableStatistics: false
 *     apiResponses:
 *       maximumSize: 2000
 *       expireAfterSeconds: 300   # 5 minutes
 *       enableStatistics: true
 * ```
 *
 * ### Properties File Configuration:
 * ```properties
 * # Default cache configuration
 * jcachex.default.maximumSize=1000
 * jcachex.default.expireAfterSeconds=1800
 * jcachex.default.enableStatistics=true
 * jcachex.default.enableJmx=false
 *
 * # Named cache configurations
 * jcachex.caches.users.maximumSize=5000
 * jcachex.caches.users.expireAfterSeconds=3600
 * jcachex.caches.users.enableStatistics=true
 *
 * jcachex.caches.sessions.maximumSize=10000
 * jcachex.caches.sessions.expireAfterSeconds=1800
 * jcachex.caches.sessions.enableStatistics=false
 * ```
 *
 * ### Environment Variables:
 * ```bash
 * # Default configuration
 * JCACHEX_DEFAULT_MAXIMUMSIZE=1000
 * JCACHEX_DEFAULT_EXPIREAFTERSECONDS=1800
 * JCACHEX_DEFAULT_ENABLESTATISTICS=true
 *
 * # Named cache configuration
 * JCACHEX_CACHES_USERS_MAXIMUMSIZE=5000
 * JCACHEX_CACHES_USERS_EXPIREAFTERSECONDS=3600
 * ```
 *
 * ## Configuration Precedence:
 * 1. **Named cache configuration** - Specific cache settings override defaults
 * 2. **Default configuration** - Applied to all caches unless overridden
 * 3. **Annotation parameters** - Runtime annotation parameters take highest precedence
 *
 * ## Spring Configuration Class:
 * ```kotlin
 * @Configuration
 * @EnableCaching
 * @EnableConfigurationProperties(JCacheXProperties::class)
 * class CacheConfiguration {
 *
 *     @Bean
 *     fun cacheManager(properties: JCacheXProperties): CacheManager {
 *         return JCacheXCacheManager(properties)
 *     }
 * }
 * ```
 *
 * ## Profile-Specific Configuration:
 * ```yaml
 * # application.yml
 * jcachex:
 *   default:
 *     maximumSize: 1000
 *     expireAfterSeconds: 1800
 *
 * ---
 * # application-development.yml
 * spring:
 *   profiles: development
 * jcachex:
 *   default:
 *     maximumSize: 100      # Smaller cache for development
 *     expireAfterSeconds: 300 # Shorter expiration for development
 *     enableStatistics: true  # Enable debugging
 *
 * ---
 * # application-production.yml
 * spring:
 *   profiles: production
 * jcachex:
 *   default:
 *     maximumSize: 10000    # Larger cache for production
 *     expireAfterSeconds: 3600 # Longer expiration for production
 *     enableStatistics: true
 *     enableJmx: true       # Enable monitoring
 * ```
 *
 * ## Dynamic Configuration:
 * ```kotlin
 * @Component
 * @ConfigurationProperties("jcachex")
 * @RefreshScope  // Enables configuration refresh without restart
 * class DynamicJCacheXProperties : JCacheXProperties()
 * ```
 *
 * ## Validation Example:
 * ```kotlin
 * @Component
 * @Validated
 * class ValidatedCacheProperties {
 *
 *     @Valid
 *     @Autowired
 *     lateinit var jcacheXProperties: JCacheXProperties
 *
 *     @PostConstruct
 *     fun validateConfiguration() {
 *         require(jcacheXProperties.default.maximumSize != null) {
 *             "Default maximum size must be configured"
 *         }
 *
 *         jcacheXProperties.caches.forEach { (name, config) ->
 *             require(config.maximumSize != null && config.maximumSize!! > 0) {
 *                 "Cache '$name' must have a positive maximum size"
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * ## Best Practices:
 * - **Environment-Specific**: Use different configurations for dev/test/prod environments
 * - **Reasonable Defaults**: Set sensible default values that work for most use cases
 * - **Monitoring**: Enable statistics in production for cache performance monitoring
 * - **Size Limits**: Always set maximum size to prevent memory issues
 * - **Expiration**: Configure appropriate expiration times based on data volatility
 * - **JMX**: Enable JMX monitoring in production for operational visibility
 *
 * ## Performance Tuning:
 * ```yaml
 * # High-performance configuration
 * jcachex:
 *   default:
 *     maximumSize: 50000
 *     expireAfterSeconds: 7200    # 2 hours
 *     enableStatistics: false     # Disable for maximum performance
 *   caches:
 *     hotData:
 *       maximumSize: 100000
 *       expireAfterSeconds: 14400  # 4 hours
 *     coldData:
 *       maximumSize: 10000
 *       expireAfterSeconds: 1800   # 30 minutes
 * ```
 *
 * @property default Default cache configuration that applies to all caches unless overridden
 * @property caches Named cache configurations that override the default configuration
 *
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see JCacheXCacheable
 * @see JCacheXCacheEvict
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "jcachex")
data class JCacheXProperties(
    /**
     * Default cache configuration that applies to all caches unless overridden.
     *
     * This configuration serves as the baseline for all caches created by JCacheX.
     * Individual cache configurations in the `caches` map will override these defaults
     * for specific named caches.
     *
     * ## Example:
     * ```yaml
     * jcachex:
     *   default:
     *     maximumSize: 1000
     *     expireAfterSeconds: 1800
     *     enableStatistics: true
     * ```
     */
    val default: JCacheXProperties.CacheConfig = JCacheXProperties.CacheConfig(),
    /**
     * Named cache configurations that override the default configuration.
     *
     * This map allows you to specify custom configuration for specific caches by name.
     * The key is the cache name (as used in @JCacheXCacheable annotations), and the
     * value is the custom configuration for that cache.
     *
     * ## Example:
     * ```yaml
     * jcachex:
     *   caches:
     *     users:           # Cache name
     *       maximumSize: 5000
     *       expireAfterSeconds: 3600
     *     sessions:        # Another cache name
     *       maximumSize: 10000
     *       expireAfterSeconds: 1800
     * ```
     */
    val caches: Map<String, JCacheXProperties.CacheConfig> = emptyMap(),
) {
    /**
     * Configuration for a single cache instance.
     *
     * This class defines all the configurable properties for an individual cache.
     * These properties control cache behavior including size limits, expiration,
     * and monitoring features.
     *
     * ## Property Descriptions:
     * - **maximumSize**: Controls memory usage by limiting entry count
     * - **expireAfterSeconds**: Ensures data freshness by automatic expiration
     * - **enableStatistics**: Provides performance monitoring capabilities
     * - **enableJmx**: Enables operational monitoring through JMX
     *
     * ## Example Usage:
     * ```yaml
     * # In application.yml
     * jcachex:
     *   caches:
     *     myCache:
     *       maximumSize: 2000
     *       expireAfterSeconds: 900  # 15 minutes
     *       enableStatistics: true
     *       enableJmx: false
     * ```
     *
     * @property maximumSize Maximum number of entries the cache can hold
     * @property expireAfterSeconds Time in seconds after which an entry will expire
     * @property enableStatistics Whether to enable statistics collection for this cache
     * @property enableJmx Whether to enable JMX monitoring for this cache
     */
    data class CacheConfig(
        /**
         * Maximum number of entries the cache can hold.
         *
         * When this limit is reached, the cache will evict entries according to its
         * eviction strategy (typically LRU). Setting this to null means unlimited size,
         * which should be used with caution to avoid memory issues.
         *
         * ## Recommended Values:
         * - **Small applications**: 100-1,000 entries
         * - **Medium applications**: 1,000-10,000 entries
         * - **Large applications**: 10,000-100,000 entries
         *
         * ## Example:
         * ```yaml
         * maximumSize: 5000  # Cache up to 5,000 entries
         * ```
         */
        val maximumSize: Long? = null,
        /**
         * Time in seconds after which an entry will expire.
         *
         * Entries will be automatically removed from the cache after this duration
         * has elapsed since they were created or last updated. Setting this to null
         * means entries never expire based on time.
         *
         * ## Common Patterns:
         * - **Session data**: 1800 seconds (30 minutes)
         * - **User profiles**: 3600 seconds (1 hour)
         * - **Configuration**: 7200 seconds (2 hours)
         * - **Static data**: 86400 seconds (24 hours)
         *
         * ## Example:
         * ```yaml
         * expireAfterSeconds: 1800  # Expire after 30 minutes
         * ```
         */
        val expireAfterSeconds: Long? = null,
        /**
         * Whether to enable statistics collection for this cache.
         *
         * When enabled, the cache will track hit/miss rates, eviction counts,
         * and other performance metrics. This has a small performance overhead
         * but provides valuable insights for monitoring and optimization.
         *
         * ## Use Cases:
         * - **Development**: Always enable for debugging
         * - **Testing**: Enable to verify cache effectiveness
         * - **Production**: Enable for performance monitoring
         * - **High-performance**: Disable if every nanosecond matters
         *
         * ## Example:
         * ```yaml
         * enableStatistics: true  # Enable performance monitoring
         * ```
         */
        val enableStatistics: Boolean = false,
        /**
         * Whether to enable JMX monitoring for this cache.
         *
         * When enabled, cache statistics and management operations will be
         * exposed through JMX, allowing monitoring and management through
         * tools like JConsole, VisualVM, or enterprise monitoring systems.
         *
         * ## Benefits:
         * - **Operational Visibility**: Monitor cache performance in real-time
         * - **Management**: Clear caches or adjust settings at runtime
         * - **Alerting**: Set up alerts based on cache metrics
         * - **Debugging**: Investigate cache behavior in production
         *
         * ## Example:
         * ```yaml
         * enableJmx: true  # Enable JMX monitoring
         * ```
         */
        val enableJmx: Boolean = false,
    )
}
