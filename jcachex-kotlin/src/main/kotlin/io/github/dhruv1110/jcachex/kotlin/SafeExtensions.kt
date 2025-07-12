package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache

/**
 * Safe extensions for cache operations.
 *
 * These extensions provide safe operations using Result types and conditional
 * operations to handle potential errors gracefully.
 */

/**
 * Safely gets a value, returning a Result.
 */
fun <K, V> Cache<K, V>.getOrNull(key: K): Result<V?> = runCatching { get(key) }

/**
 * Safely puts a value, returning a Result.
 */
fun <K, V> Cache<K, V>.putOrNull(
    key: K,
    value: V,
): Result<Unit> = runCatching { put(key, value) }

/**
 * Safely removes a value, returning a Result.
 */
fun <K, V> Cache<K, V>.removeOrNull(key: K): Result<V?> = runCatching { remove(key) }

/**
 * Executes the given action if the cache contains the key.
 */
inline fun <K, V> Cache<K, V>.ifContains(
    key: K,
    action: (V) -> Unit,
) {
    get(key)?.let(action)
}

/**
 * Executes the given action if the cache does not contain the key.
 */
inline fun <K, V> Cache<K, V>.ifNotContains(
    key: K,
    action: () -> Unit,
) {
    if (!containsKey(key)) action()
}
