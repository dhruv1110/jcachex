@file:Suppress("TooManyFunctions")

package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.CacheConfig
import io.github.dhruv1110.jcachex.JCacheXBuilder
import io.github.dhruv1110.jcachex.profiles.CacheProfile
import io.github.dhruv1110.jcachex.profiles.ProfileName

/*
 * DSL extensions for cache configuration.
 *
 * These extensions provide DSL-style builders for cache configuration
 * with type-safe, Kotlin-idiomatic syntax.
 */

/**
 * Creates a cache configuration using a DSL-style builder.
 */
inline fun <K, V> cacheConfig(configure: CacheConfig.Builder<K, V>.() -> Unit): CacheConfig<K, V> {
    val builder = CacheConfig.Builder<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache using the JCacheXBuilder with profile name for type safety.
 */
inline fun <K, V> createCacheWithProfile(
    profileName: ProfileName,
    configure: JCacheXBuilder<K, V>.() -> Unit = {},
): Cache<K, V> {
    val builder = JCacheXBuilder.fromProfile<K, V>(profileName)
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache using the JCacheXBuilder with the DEFAULT profile.
 */
inline fun <K, V> createCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = JCacheXBuilder.create<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache with smart defaults based on workload characteristics.
 */
inline fun <K, V> createSmartCache(configure: JCacheXBuilder<K, V>.() -> Unit): Cache<K, V> {
    val builder = JCacheXBuilder.withSmartDefaults<K, V>()
    builder.configure()
    return builder.build()
}

// ===== CONVENIENCE METHODS FOR COMMON WORKLOADS =====

/**
 * Creates a cache optimized for read-heavy workloads (80%+ reads).
 */
inline fun <K, V> createReadHeavyCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = JCacheXBuilder.forReadHeavyWorkload<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache optimized for write-heavy workloads (50%+ writes).
 */
inline fun <K, V> createWriteHeavyCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = JCacheXBuilder.forWriteHeavyWorkload<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache optimized for memory-constrained environments.
 */
inline fun <K, V> createMemoryEfficientCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = JCacheXBuilder.forMemoryConstrainedEnvironment<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache optimized for maximum performance and throughput.
 */
inline fun <K, V> createHighPerformanceCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = JCacheXBuilder.forHighPerformance<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache optimized for user session storage.
 */
inline fun <K, V> createSessionCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = JCacheXBuilder.forSessionStorage<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache optimized for API response caching.
 */
inline fun <K, V> createApiCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = JCacheXBuilder.forApiResponseCaching<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache optimized for expensive computation results.
 */
inline fun <K, V> createComputeCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = JCacheXBuilder.forComputationCaching<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache optimized for machine learning workloads.
 */
inline fun <K, V> createMachineLearningCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = JCacheXBuilder.forMachineLearning<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache optimized for ultra-low latency requirements.
 */
inline fun <K, V> createUltraLowLatencyCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = JCacheXBuilder.forUltraLowLatency<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * Creates a cache optimized for hardware-specific features.
 */
inline fun <K, V> createHardwareOptimizedCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    val builder = JCacheXBuilder.forHardwareOptimization<K, V>()
    builder.configure()
    return builder.build()
}

// ===== LEGACY SUPPORT =====

/**
 * @deprecated Use createCache instead
 */
@Deprecated(
    "Use createCache instead",
    ReplaceWith("createCache(configure)", "io.github.dhruv1110.jcachex.kotlin.createCache"),
)
inline fun <K, V> createUnifiedCache(configure: JCacheXBuilder<K, V>.() -> Unit = {}): Cache<K, V> {
    return createCache(configure)
}
