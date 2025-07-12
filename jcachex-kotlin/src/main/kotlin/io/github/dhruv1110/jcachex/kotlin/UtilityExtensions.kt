@file:Suppress("TooManyFunctions")

package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.impl.AllocationOptimizedCache
import io.github.dhruv1110.jcachex.impl.CacheLocalityOptimizedCache
import io.github.dhruv1110.jcachex.impl.ReadOnlyOptimizedCache
import io.github.dhruv1110.jcachex.impl.WriteHeavyOptimizedCache
import io.github.dhruv1110.jcachex.impl.ZeroCopyOptimizedCache
import io.github.dhruv1110.jcachex.impl.DefaultCache
import io.github.dhruv1110.jcachex.impl.OptimizedCache
import io.github.dhruv1110.jcachex.impl.JITOptimizedCache
import io.github.dhruv1110.jcachex.impl.JVMOptimizedCache
import io.github.dhruv1110.jcachex.impl.HardwareOptimizedCache
import io.github.dhruv1110.jcachex.impl.MLOptimizedCache
import io.github.dhruv1110.jcachex.impl.ProfiledOptimizedCache

/**
 * Utility extensions for cache operations.
 *
 * These extensions provide utility functions for advanced cache operations
 * including computation, merging, batch operations, and performance measurement.
 */

/**
 * Computes a value for the given key if it's not present in the cache.
 */
fun <K, V> Cache<K, V>.computeIfAbsent(
    key: K,
    compute: (K) -> V,
): V = get(key) ?: compute(key).also { put(key, it) }

/**
 * Computes a new value for the given key if it's present in the cache.
 */
fun <K, V> Cache<K, V>.computeIfPresent(
    key: K,
    compute: (K, V) -> V,
): V? =
    get(key)?.let { currentValue ->
        compute(key, currentValue).also { put(key, it) }
    }

/**
 * Computes a value for the given key regardless of whether it's present.
 */
fun <K, V> Cache<K, V>.compute(
    key: K,
    compute: (K, V?) -> V?,
): V? {
    val currentValue = get(key)
    val newValue = compute(key, currentValue)
    return if (newValue != null) {
        put(key, newValue)
        newValue
    } else {
        remove(key)
        null
    }
}

/**
 * Merges the given value with the existing value for the key.
 */
fun <K, V> Cache<K, V>.merge(
    key: K,
    value: V,
    merge: (V, V) -> V,
): V {
    val currentValue = get(key)
    val newValue = if (currentValue != null) merge(currentValue, value) else value
    put(key, newValue)
    return newValue
}

/**
 * Replaces the value for the given key if it exists.
 */
fun <K, V> Cache<K, V>.replace(
    key: K,
    value: V,
): V? =
    if (containsKey(key)) {
        put(key, value)
        value
    } else {
        null
    }

/**
 * Replaces the value for the given key if it equals the expected value.
 */
fun <K, V> Cache<K, V>.replace(
    key: K,
    oldValue: V,
    newValue: V,
): Boolean =
    if (get(key) == oldValue) {
        put(key, newValue)
        true
    } else {
        false
    }

/**
 * Replaces all values using the given transformation function.
 */
fun <K, V> Cache<K, V>.replaceAll(transform: (K, V) -> V) {
    entries().forEach { entry ->
        put(entry.key, transform(entry.key, entry.value))
    }
}

/**
 * Performs batch operations on the cache.
 */
inline fun <K, V> Cache<K, V>.batch(operations: Cache<K, V>.() -> Unit): Cache<K, V> {
    operations()
    return this
}

/**
 * Executes the given block and measures the time taken.
 */
inline fun <K, V, R> Cache<K, V>.measureTime(block: Cache<K, V>.() -> R): Pair<R, Long> {
    val startTime = System.nanoTime()
    val result = block()
    val endTime = System.nanoTime()
    return result to (endTime - startTime)
}

/**
 * Extension function to get cache type information.
 */
val <K, V> Cache<K, V>.cacheType: String
    get() = when (this) {
        is DefaultCache -> "Default"
        is OptimizedCache -> "Optimized"
        is JITOptimizedCache -> "JIT-Optimized"
        is AllocationOptimizedCache -> "Allocation-Optimized"
        is CacheLocalityOptimizedCache -> "Locality-Optimized"
        is ZeroCopyOptimizedCache -> "Zero-Copy-Optimized"
        is ReadOnlyOptimizedCache -> "Read-Only-Optimized"
        is WriteHeavyOptimizedCache -> "Write-Heavy-Optimized"
        is JVMOptimizedCache -> "JVM-Optimized"
        is HardwareOptimizedCache -> "Hardware-Optimized"
        is MLOptimizedCache -> "ML-Optimized"
        is ProfiledOptimizedCache -> "Profiled-Optimized"
        else -> "Unknown"
    }

/**
 * Returns a summary of the cache contents.
 */
@Suppress("MagicNumber")
fun <K, V> Cache<K, V>.summary(): String {
    val maxKeysToShow = 10
    val keysSummary = if (size() <= maxKeysToShow) {
        keysList().toString()
    } else {
        "${keysList().take(maxKeysToShow)}..."
    }

    return """
        Cache Summary:
        - Size: ${size()}
        - Empty: ${isEmpty()}
        - Keys: $keysSummary
        - Stats: ${stats().formatted()}
        """.trimIndent()
}
