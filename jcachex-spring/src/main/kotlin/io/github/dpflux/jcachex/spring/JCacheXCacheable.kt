package io.github.dpflux.jcachex.spring

import java.lang.annotation.Inherited
import java.util.concurrent.TimeUnit

/**
 * Annotation that indicates a method's result should be cached.
 * The cache key is derived from the method parameters.
 *
 * @property cacheName The name of the cache to use
 * @property key The SpEL expression to compute the cache key
 * @property condition The SpEL expression to determine if caching should occur
 * @property unless The SpEL expression to determine if caching should not occur
 * @property expireAfterWrite The time after which the entry should expire
 * @property expireAfterWriteUnit The unit for expireAfterWrite
 * @property maximumSize The maximum number of entries in the cache
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
