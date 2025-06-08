package io.github.dhruv.jcachex.kotlin

import io.github.dhruv.jcachex.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gets the value for the given key, or computes and stores it if not present.
 *
 * @param key the key to look up
 * @param compute the function to compute the value if not present
 * @return the value associated with the key
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
 *
 * @param key the key to look up
 * @param defaultValue the value to return if the key is not present
 * @return the value associated with the key, or the default value
 */
fun <K, V> Cache<K, V>.getOrDefault(
    key: K,
    defaultValue: V,
): V = get(key) ?: defaultValue

/**
 * Performs the given action on each entry in the cache.
 *
 * @param action the action to perform on each entry
 */
fun <K, V> Cache<K, V>.forEach(action: (K, V) -> Unit) {
    // Note: This is a placeholder implementation
    // Actual implementation would depend on how entries are stored in the cache
    // and would need to be implemented by concrete cache classes
}

/**
 * Returns a list of all keys in the cache.
 *
 * @return a list of all keys
 */
fun <K, V> Cache<K, V>.keys(): List<K> {
    // Note: This is a placeholder implementation
    // Actual implementation would depend on how entries are stored in the cache
    // and would need to be implemented by concrete cache classes
    return emptyList()
}

/**
 * Returns a list of all values in the cache.
 *
 * @return a list of all values
 */
fun <K, V> Cache<K, V>.values(): List<V> {
    // Note: This is a placeholder implementation
    // Actual implementation would depend on how entries are stored in the cache
    // and would need to be implemented by concrete cache classes
    return emptyList()
}
