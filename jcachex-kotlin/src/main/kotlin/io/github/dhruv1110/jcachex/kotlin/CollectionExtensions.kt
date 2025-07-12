@file:Suppress("TooManyFunctions")

package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 * Collection-like extensions for cache operations.
 *
 * These extensions provide familiar collection operations such as filtering,
 * mapping, and searching, making caches feel more like native Kotlin collections.
 */

/**
 * Returns a sequence of all entries in the cache for lazy evaluation.
 */
fun <K, V> Cache<K, V>.asSequence(): Sequence<Map.Entry<K, V>> = entries().asSequence()

/**
 * Converts the cache to a regular Map.
 */

fun <K, V> Cache<K, V>.toMap(): Map<K, V> = entries().associate { it.key to it.value }

/**
 * Converts the cache to a mutable Map.
 */

fun <K, V> Cache<K, V>.toMutableMap(): MutableMap<K, V> = entries().associate { it.key to it.value }.toMutableMap()

/**
 * Filters entries by key predicate.
 */

fun <K, V> Cache<K, V>.filterKeys(predicate: (K) -> Boolean): Map<K, V> =
    entries().filter { predicate(it.key) }.associate { it.key to it.value }

/**
 * Filters entries by value predicate.
 */

fun <K, V> Cache<K, V>.filterValues(predicate: (V) -> Boolean): Map<K, V> =
    entries().filter { predicate(it.value) }.associate { it.key to it.value }

/**
 * Filters entries by key-value predicate.
 */

fun <K, V> Cache<K, V>.filter(predicate: (K, V) -> Boolean): Map<K, V> =
    entries().filter { predicate(it.key, it.value) }.associate { it.key to it.value }

/**
 * Maps values to a new type.
 */

fun <K, V, R> Cache<K, V>.mapValues(transform: (V) -> R): Map<K, R> =
    entries().associate { it.key to transform(it.value) }

/**
 * Maps keys to a new type.
 */

fun <K, V, R> Cache<K, V>.mapKeys(transform: (K) -> R): Map<R, V> =
    entries().associate { transform(it.key) to it.value }

/**
 * Maps both keys and values to new types.
 */

fun <K, V, R, S> Cache<K, V>.map(transform: (K, V) -> Pair<R, S>): Map<R, S> =
    entries().associate { transform(it.key, it.value) }

/**
 * Gets multiple values at once.
 */

fun <K, V> Cache<K, V>.getAll(keys: Collection<K>): Map<K, V?> = keys.associateWith { get(it) }

/**
 * Gets multiple values at once, filtering out null values.
 */

fun <K, V> Cache<K, V>.getAllPresent(keys: Collection<K>): Map<K, V> =
    keys.mapNotNull { key -> get(key)?.let { key to it } }.toMap()

/**
 * Finds the first entry that matches the given predicate.
 */

fun <K, V> Cache<K, V>.find(predicate: (K, V) -> Boolean): Map.Entry<K, V>? =
    entries().find { predicate(it.key, it.value) }

/**
 * Finds all entries that match the given predicate.
 */

fun <K, V> Cache<K, V>.findAll(predicate: (K, V) -> Boolean): List<Map.Entry<K, V>> =
    entries().filter { predicate(it.key, it.value) }

/**
 * Counts the number of entries that match the given predicate.
 */

fun <K, V> Cache<K, V>.count(predicate: (K, V) -> Boolean): Int = entries().count { predicate(it.key, it.value) }

/**
 * Checks if any entry matches the given predicate.
 */

fun <K, V> Cache<K, V>.any(predicate: (K, V) -> Boolean): Boolean = entries().any { predicate(it.key, it.value) }

/**
 * Checks if all entries match the given predicate.
 */

fun <K, V> Cache<K, V>.all(predicate: (K, V) -> Boolean): Boolean = entries().all { predicate(it.key, it.value) }

/**
 * Groups entries by the result of the given function.
 */

fun <K, V, R> Cache<K, V>.groupBy(keySelector: (K, V) -> R): Map<R, List<Map.Entry<K, V>>> =
    entries().groupBy { keySelector(it.key, it.value) }

/**
 * Partitions entries into two lists based on the given predicate.
 */

fun <K, V> Cache<K, V>.partition(predicate: (K, V) -> Boolean): Pair<List<Map.Entry<K, V>>, List<Map.Entry<K, V>>> =
    entries().partition { predicate(it.key, it.value) }

/**
 * Returns the minimum entry based on the given comparator.
 */

fun <K, V, R : Comparable<R>> Cache<K, V>.minByOrNull(selector: (K, V) -> R): Map.Entry<K, V>? =
    entries().minByOrNull { selector(it.key, it.value) }

/**
 * Returns the maximum entry based on the given comparator.
 */

fun <K, V, R : Comparable<R>> Cache<K, V>.maxByOrNull(selector: (K, V) -> R): Map.Entry<K, V>? =
    entries().maxByOrNull { selector(it.key, it.value) }

/**
 * Performs the given action on each entry in the cache.
 */

fun <K, V> Cache<K, V>.forEach(action: (K, V) -> Unit) {
    entries().forEach { entry ->
        action(entry.key, entry.value)
    }
}

/**
 * Returns a list of all keys in the cache.
 */

fun <K, V> Cache<K, V>.keysList(): List<K> = keys().toList()

/**
 * Returns a list of all values in the cache.
 */

fun <K, V> Cache<K, V>.valuesList(): List<V> = values().toList()

/**
 * Returns true if the cache is empty.
 */

fun <K, V> Cache<K, V>.isEmpty(): Boolean = size() == 0L

/**
 * Returns true if the cache is not empty.
 */

fun <K, V> Cache<K, V>.isNotEmpty(): Boolean = size() > 0L

/**
 * Returns true if the cache contains the given value.
 */

fun <K, V> Cache<K, V>.containsValue(value: V): Boolean = values().contains(value)

/**
 * Puts all entries from the given map into the cache.
 */

fun <K, V> Cache<K, V>.putAll(map: Map<K, V>) {
    map.forEach { (key, value) -> put(key, value) }
}

/**
 * Removes all entries with the given keys from the cache.
 */

fun <K, V> Cache<K, V>.removeAll(keys: Collection<K>): List<V?> = keys.map { remove(it) }

/**
 * Retains only the entries with the given keys.
 */

fun <K, V> Cache<K, V>.retainAll(keysToKeep: Collection<K>) {
    val keysToRemove = keys() - keysToKeep.toSet()
    keysToRemove.forEach { remove(it) }
}

/**
 * Removes all entries that match the given predicate.
 */

fun <K, V> Cache<K, V>.removeIf(predicate: (K, V) -> Boolean): Int {
    var removed = 0
    entries().forEach { entry ->
        if (predicate(entry.key, entry.value)) {
            remove(entry.key)
            removed++
        }
    }
    return removed
}

/**
 * Gets the value for the given key, or computes and stores it if not present.
 * This suspending function provides a thread-safe way to implement the "check-then-act" pattern.
 */

suspend fun <K, V> Cache<K, V>.getOrPut(
    key: K,
    compute: suspend () -> V,
): V =
    get(key) ?: withContext(Dispatchers.IO) {
        compute().also { put(key, it) }
    }

/**
 * Gets the value for the given key, or returns the default value if not present.
 */

fun <K, V> Cache<K, V>.getOrDefault(
    key: K,
    defaultValue: V,
): V = get(key) ?: defaultValue

/**
 * Gets the value for the key or puts and returns the default value.
 */

fun <K, V> Cache<K, V>.getOrPutValue(
    key: K,
    defaultValue: () -> V,
): V = get(key) ?: defaultValue().also { put(key, it) }
