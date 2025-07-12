@file:Suppress("TooManyFunctions")

package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.profiles.ProfileRegistry

/**
 * Profile-based cache creation extensions.
 *
 * These extensions provide convenient functions for creating caches
 * optimized for specific workload patterns and use cases.
 */

/**
 * Creates a cache optimized for read-heavy workloads.
 */
inline fun <K, V> createReadHeavyCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> =
    createCacheWithProfile(ProfileRegistry.getProfile("READ_HEAVY"), configure)

/**
 * Creates a cache optimized for write-heavy workloads.
 */
inline fun <K, V> createWriteHeavyCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> =
    createCacheWithProfile(ProfileRegistry.getProfile("WRITE_HEAVY"), configure)

/**
 * Creates a memory-efficient cache for constrained environments.
 */
inline fun <K, V> createMemoryEfficientCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> =
    createCacheWithProfile(ProfileRegistry.getProfile("MEMORY_EFFICIENT"), configure)

/**
 * Creates a high-performance cache for maximum throughput.
 */
inline fun <K, V> createHighPerformanceCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> =
    createCacheWithProfile(ProfileRegistry.getProfile("HIGH_PERFORMANCE"), configure)

/**
 * Creates a cache optimized for session storage.
 */
inline fun <K, V> createSessionCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> =
    createCacheWithProfile(ProfileRegistry.getProfile("SESSION_CACHE"), configure)

/**
 * Creates a cache optimized for API response caching.
 */
inline fun <K, V> createApiCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> =
    createCacheWithProfile(ProfileRegistry.getProfile("API_CACHE"), configure)

/**
 * Creates a cache optimized for expensive computation results.
 */
inline fun <K, V> createComputeCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> =
    createCacheWithProfile(ProfileRegistry.getProfile("COMPUTE_CACHE"), configure)

/**
 * Creates a machine learning optimized cache with predictive capabilities.
 */
inline fun <K, V> createMLOptimizedCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> =
    createCacheWithProfile(ProfileRegistry.getProfile("ML_OPTIMIZED"), configure)

/**
 * Creates a zero-copy optimized cache for minimal memory allocation.
 */
inline fun <K, V> createZeroCopyCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> =
    createCacheWithProfile(ProfileRegistry.getProfile("ZERO_COPY"), configure)

/**
 * Creates a hardware-optimized cache leveraging CPU features.
 */
inline fun <K, V> createHardwareOptimizedCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> =
    createCacheWithProfile(ProfileRegistry.getProfile("HARDWARE_OPTIMIZED"), configure)

/**
 * Creates a distributed cache for cluster environments.
 */
inline fun <K, V> createDistributedCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit = {}): Cache<K, V> =
    createCacheWithProfile(ProfileRegistry.getProfile("DISTRIBUTED"), configure)
