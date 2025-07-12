package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.CacheConfig
import io.github.dhruv1110.jcachex.UnifiedCacheBuilder
import io.github.dhruv1110.jcachex.profiles.CacheProfile

/**
 * DSL extensions for cache configuration.
 *
 * These extensions provide DSL-style builders for cache configuration
 * with type-safe, Kotlin-idiomatic syntax.
 */

/**
 * Creates a cache configuration using a DSL-style builder.
 */
inline fun <K, V> cacheConfig(
    configure: CacheConfigBuilder<K, V>.() -> Unit,
): CacheConfig<K, V> {
    val builder = CacheConfigBuilder<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache using the UnifiedCacheBuilder with profile support.
 */
inline fun <K, V> createCacheWithProfile(
    profile: CacheProfile<*, *>,
    configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {},
): Cache<K, V> {
    val builder = UnifiedCacheBuilder.forProfile<K, V>(profile)
    val scope = UnifiedCacheBuilderScope(builder)
    scope.configure()
    return builder.build()
}

/**
 * Creates a cache using the UnifiedCacheBuilder with the DEFAULT profile.
 */
inline fun <K, V> createUnifiedCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = UnifiedCacheBuilder.create<K, V>()
    val scope = UnifiedCacheBuilderScope(builder)
    scope.configure()
    return builder.build()
}

/**
 * Creates a cache with smart defaults based on workload characteristics.
 */
inline fun <K, V> createSmartCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit): Cache<K, V> {
    val builder = UnifiedCacheBuilder.withSmartDefaults<K, V>()
    val scope = UnifiedCacheBuilderScope(builder)
    scope.configure()
    return builder.build()
}
