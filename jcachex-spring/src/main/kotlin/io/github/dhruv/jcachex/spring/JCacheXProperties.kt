package io.github.dhruv.jcachex.spring

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for JCacheX in Spring Boot applications.
 * These properties can be configured in application.yml or application.properties.
 */
@ConfigurationProperties(prefix = "jcachex")
data class JCacheXProperties(
    /**
     * Default cache configuration that applies to all caches unless overridden.
     */
    val default: JCacheXProperties.CacheConfig = JCacheXProperties.CacheConfig(),
    /**
     * Named cache configurations that override the default configuration.
     * The key is the cache name.
     */
    val caches: Map<String, JCacheXProperties.CacheConfig> = emptyMap(),
) {
    /**
     * Configuration for a single cache.
     */
    data class CacheConfig(
        /**
         * Maximum number of entries the cache can hold.
         * If not specified, the cache will grow unbounded.
         */
        val maximumSize: Long? = null,
        /**
         * Time in seconds after which an entry will expire.
         * If not specified, entries will not expire.
         */
        val expireAfterSeconds: Long? = null,
        /**
         * Whether to enable statistics collection for this cache.
         * Defaults to false.
         */
        val enableStatistics: Boolean = false,
        /**
         * Whether to enable JMX monitoring for this cache.
         * Defaults to false.
         */
        val enableJmx: Boolean = false,
    )
}
