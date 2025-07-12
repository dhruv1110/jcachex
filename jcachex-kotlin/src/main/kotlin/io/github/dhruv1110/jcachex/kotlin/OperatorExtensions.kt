package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache

/**
 * Operator extensions for cache operations.
 *
 * These extensions provide operator overloading to make cache operations
 * feel more natural and Kotlin-idiomatic.
 */

/**
 * Sets the value for the given key using array access syntax.
 */
operator fun <K, V> Cache<K, V>.set(
    key: K,
    value: V,
) = put(key, value)

/**
 * Checks if the cache contains the given key using 'in' operator.
 */
operator fun <K, V> Cache<K, V>.contains(key: K): Boolean = containsKey(key)

/**
 * Adds a key-value pair to the cache using += operator.
 */
operator fun <K, V> Cache<K, V>.plusAssign(pair: Pair<K, V>) = put(pair.first, pair.second)

/**
 * Removes a key from the cache using -= operator.
 */
operator fun <K, V> Cache<K, V>.minusAssign(key: K) {
    remove(key)
}
