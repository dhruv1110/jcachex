@file:Suppress("TooManyFunctions")

package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.CacheStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * Constants used in the cache extensions.
 */
private object CacheConstants {
    const val PERCENT_MULTIPLIER = 100.0
    const val NANOS_PER_MILLI = 1_000_000.0
    const val DECIMAL_PLACES = 2
    const val MAX_KEYS_TO_SHOW = 10
}

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
    entries().forEach { entry ->
        action(entry.key, entry.value)
    }
}

/**
 * Returns a list of all keys in the cache.
 *
 * @return a list of all keys
 */
fun <K, V> Cache<K, V>.keysList(): List<K> = keys().toList()

/**
 * Returns a list of all values in the cache.
 *
 * @return a list of all values
 */
fun <K, V> Cache<K, V>.valuesList(): List<V> = values().toList()

// ===== COROUTINE EXTENSIONS =====

/**
 * Asynchronously gets the value for the given key as a Deferred.
 * @param scope The coroutine scope to use for the async operation
 */
fun <K, V> Cache<K, V>.getDeferred(
    key: K,
    scope: CoroutineScope,
): Deferred<V?> = scope.async { get(key) }

/**
 * Asynchronously puts a value for the given key as a Deferred.
 * @param scope The coroutine scope to use for the async operation
 */
fun <K, V> Cache<K, V>.putDeferred(
    key: K,
    value: V,
    scope: CoroutineScope,
): Deferred<Unit> = scope.async { put(key, value) }

/**
 * Asynchronously removes the value for the given key as a Deferred.
 * @param scope The coroutine scope to use for the async operation
 */
fun <K, V> Cache<K, V>.removeDeferred(
    key: K,
    scope: CoroutineScope,
): Deferred<V?> = scope.async { remove(key) }

/**
 * Asynchronously clears the cache as a Deferred.
 * @param scope The coroutine scope to use for the async operation
 */
fun <K, V> Cache<K, V>.clearDeferred(scope: CoroutineScope): Deferred<Unit> = scope.async { clear() }

/**
 * Suspending version of getOrPut using the cache's async loader if available.
 */
suspend fun <K, V> Cache<K, V>.getOrPutAsync(
    key: K,
    compute: suspend (K) -> V,
): V =
    withContext(Dispatchers.IO) {
        get(key) ?: compute(key).also { put(key, it) }
    }

// ===== OPERATOR OVERLOADING =====

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

// ===== COLLECTION-LIKE EXTENSIONS =====

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

// ===== BULK OPERATIONS =====

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

// ===== UTILITY FUNCTIONS =====

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
 * Gets the value for the key or puts and returns the default value.
 */
fun <K, V> Cache<K, V>.getOrPutValue(
    key: K,
    defaultValue: () -> V,
): V = get(key) ?: defaultValue().also { put(key, it) }

// ===== STATISTICS EXTENSIONS =====

/**
 * Returns the hit rate as a percentage.
 */
fun CacheStats.hitRatePercent(): Double = hitRate() * CacheConstants.PERCENT_MULTIPLIER

/**
 * Returns the miss rate as a percentage.
 */
fun CacheStats.missRatePercent(): Double = missRate() * CacheConstants.PERCENT_MULTIPLIER

/**
 * Returns the average load time in milliseconds.
 */
fun CacheStats.averageLoadTimeMillis(): Double = averageLoadTime() / CacheConstants.NANOS_PER_MILLI

/**
 * Returns a formatted string representation of the cache statistics.
 */
fun CacheStats.formatted(): String {
    val formatString = "%.${CacheConstants.DECIMAL_PLACES}f"
    return """
        Cache Statistics:
        - Hit Rate: ${formatString.format(hitRatePercent())}%
        - Miss Rate: ${formatString.format(missRatePercent())}%
        - Hits: ${hitCount()}
        - Misses: ${missCount()}
        - Evictions: ${evictionCount()}
        - Loads: ${loadCount()}
        - Load Failures: ${loadFailureCount()}
        - Average Load Time: ${formatString.format(averageLoadTimeMillis())}ms
        """.trimIndent()
}

// ===== SAFE OPERATIONS =====

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

// ===== CACHE CONFIG DSL =====

/**
 * Creates a cache configuration using a DSL-style builder.
 */
inline fun <K, V> cacheConfig(
    configure: CacheConfigBuilder<K, V>.() -> Unit,
): io.github.dhruv1110.jcachex.CacheConfig<K, V> {
    val builder = CacheConfigBuilder<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * DSL wrapper for CacheConfig.Builder.
 */
@Suppress("TooManyFunctions")
class CacheConfigBuilder<K, V> {
    private val builder = io.github.dhruv1110.jcachex.CacheConfig.builder<K, V>()

    fun maximumSize(size: Long) = apply { builder.maximumSize(size) }

    fun maximumWeight(weight: Long) = apply { builder.maximumWeight(weight) }

    fun expireAfterWrite(duration: java.time.Duration) = apply { builder.expireAfterWrite(duration) }

    fun expireAfterAccess(duration: java.time.Duration) = apply { builder.expireAfterAccess(duration) }

    fun refreshAfterWrite(duration: java.time.Duration) = apply { builder.refreshAfterWrite(duration) }

    fun loader(loader: (K) -> V) = apply { builder.loader(loader) }

    fun asyncLoader(loader: (K) -> java.util.concurrent.CompletableFuture<V>) = apply { builder.asyncLoader(loader) }

    fun weigher(weigher: (K, V) -> Long) = apply { builder.weigher(weigher) }

    fun evictionStrategy(strategy: io.github.dhruv1110.jcachex.eviction.EvictionStrategy<K, V>) =
        apply { builder.evictionStrategy(strategy) }

    fun recordStats(enable: Boolean = true) = apply { builder.recordStats(enable) }

    fun initialCapacity(capacity: Int) = apply { builder.initialCapacity(capacity) }

    fun concurrencyLevel(level: Int) = apply { builder.concurrencyLevel(level) }

    fun weakKeys(enable: Boolean = true) = apply { builder.weakKeys(enable) }

    fun weakValues(enable: Boolean = true) = apply { builder.weakValues(enable) }

    fun softValues(enable: Boolean = true) = apply { builder.softValues(enable) }

    fun directory(dir: String) = apply { builder.directory(dir) }

    fun listener(listener: io.github.dhruv1110.jcachex.CacheEventListener<K, V>) =
        apply { builder.addListener(listener) }

    fun build(): io.github.dhruv1110.jcachex.CacheConfig<K, V> = builder.build()
}

// ===== ADDITIONAL UTILITY FUNCTIONS =====

/**
 * Creates a cache with the given configuration.
 */
fun <K, V> createCache(configure: CacheConfigBuilder<K, V>.() -> Unit): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return io.github.dhruv1110.jcachex.DefaultCache(config)
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
 * Returns a summary of the cache contents.
 */
fun <K, V> Cache<K, V>.summary(): String {
    val keysSummary =
        if (size() <= CacheConstants.MAX_KEYS_TO_SHOW) {
            keysList().toString()
        } else {
            "${keysList().take(CacheConstants.MAX_KEYS_TO_SHOW)}..."
        }

    return """
        Cache Summary:
        - Size: ${size()}
        - Empty: ${isEmpty()}
        - Keys: $keysSummary
        - Stats: ${stats().formatted()}
        """.trimIndent()
}
