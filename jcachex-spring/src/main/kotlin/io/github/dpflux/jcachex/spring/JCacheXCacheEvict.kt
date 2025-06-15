package io.github.dpflux.jcachex.spring

import java.lang.annotation.Inherited

/**
 * Annotation that indicates a method should trigger a cache eviction.
 * The cache key is derived from the method parameters.
 *
 * @property cacheName The name of the cache to evict from
 * @property key The SpEL expression to compute the cache key
 * @property condition The SpEL expression to determine if eviction should occur
 * @property allEntries Whether to evict all entries in the cache
 * @property beforeInvocation Whether to evict before the method is invoked
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
