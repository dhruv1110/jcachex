@file:Suppress("TooManyFunctions")

package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.CacheConfig
import io.github.dhruv1110.jcachex.CacheStats
import io.github.dhruv1110.jcachex.FrequencySketchType
import io.github.dhruv1110.jcachex.impl.DefaultCache
import io.github.dhruv1110.jcachex.impl.OptimizedCache
import io.github.dhruv1110.jcachex.impl.JITOptimizedCache
import io.github.dhruv1110.jcachex.impl.AllocationOptimizedCache
import io.github.dhruv1110.jcachex.impl.CacheLocalityOptimizedCache
import io.github.dhruv1110.jcachex.impl.ZeroCopyOptimizedCache
import io.github.dhruv1110.jcachex.impl.ReadOnlyOptimizedCache
import io.github.dhruv1110.jcachex.impl.WriteHeavyOptimizedCache
import io.github.dhruv1110.jcachex.impl.JVMOptimizedCache
import io.github.dhruv1110.jcachex.impl.HardwareOptimizedCache
import io.github.dhruv1110.jcachex.impl.MLOptimizedCache
import io.github.dhruv1110.jcachex.impl.ProfiledOptimizedCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * JCacheX Kotlin Extensions
 *
 * This file provides idiomatic Kotlin extensions for the JCacheX cache library,
 * including coroutine support, operator overloading, collection-like operations,
 * and DSL builders for configuration.
 *
 * ## Key Features:
 * - **Coroutine Integration**: Suspending functions and async operations
 * - **Operator Overloading**: Array-like syntax for cache operations
 * - **Collection Extensions**: Familiar collection operations like filter, map, forEach
 * - **DSL Builders**: Type-safe configuration with Kotlin DSL
 * - **Statistics Extensions**: Enhanced statistics with formatting utilities
 * - **Safe Operations**: Result-based error handling
 *
 * ## Quick Start Examples:
 * ```kotlin
 * import io.github.dhruv1110.jcachex.kotlin.*
 *
 * // Create cache with DSL
 * val cache = createCache<String, User> {
 *     maximumSize(1000L)
 *     expireAfterWrite(Duration.ofMinutes(30))
 *     recordStats(true)
 * }
 *
 * // Use operator overloading
 * cache["user123"] = user
 * val user = cache["user123"]
 * cache += "user456" to anotherUser
 *
 * // Coroutine operations
 * val user = cache.getOrPut("user789") {
 *     userService.loadUser("user789")
 * }
 *
 * // Collection-like operations
 * val activeUsers = cache.filterValues { it.isActive }
 * cache.forEach { key, user ->
 *     println("User $key: ${user.name}")
 * }
 * ```
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
 * This suspending function provides a thread-safe way to implement the "check-then-act"
 * pattern common in caching scenarios. If the key is not found, the compute function
 * is called to generate the value, which is then stored in the cache.
 *
 * ## Examples:
 * ```kotlin
 * // Load user from database if not cached
 * val user = cache.getOrPut("user123") {
 *     userRepository.findById("user123")
 * }
 *
 * // Expensive computation with caching
 * val result = cache.getOrPut("computation_$input") {
 *     performExpensiveComputation(input)
 * }
 *
 * // API call with caching
 * val response = cache.getOrPut("api_$endpoint") {
 *     httpClient.get(endpoint).body<ApiResponse>()
 * }
 * ```
 *
 * ## Thread Safety:
 * This function is not atomic - if multiple threads call it simultaneously with the
 * same key, the compute function may be executed multiple times. For atomic behavior,
 * consider using a loading cache with a configured loader function.
 *
 * @param key the key to look up
 * @param compute the suspending function to compute the value if not present
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
 * This is a safe alternative to [Cache.get] that avoids null checks by providing
 * a fallback value when the key is not found in the cache.
 *
 * ## Examples:
 * ```kotlin
 * // Get user preferences with defaults
 * val theme = prefsCache.getOrDefault("theme", "light")
 * val fontSize = prefsCache.getOrDefault("fontSize", 14)
 *
 * // Configuration values with fallbacks
 * val timeout = configCache.getOrDefault("timeout", Duration.ofSeconds(30))
 *
 * // Counters with zero default
 * val visitCount = cache.getOrDefault("visits_$userId", 0)
 * ```
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
 * This provides a convenient way to iterate over all cached entries, similar to
 * the forEach function available on Kotlin collections. The iteration order is
 * not guaranteed and may vary between calls.
 *
 * ## Examples:
 * ```kotlin
 * // Log all cached users
 * userCache.forEach { userId, user ->
 *     logger.info("Cached user: $userId -> ${user.name}")
 * }
 *
 * // Collect statistics
 * var totalSize = 0L
 * cache.forEach { _, value ->
 *     totalSize += value.size
 * }
 *
 * // Validate cache contents
 * cache.forEach { key, value ->
 *     require(value.isValid()) { "Invalid value for key: $key" }
 * }
 * ```
 *
 * ## Performance Note:
 * This operation iterates over all entries in the cache, which may be expensive
 * for large caches. Consider using streaming operations for better performance
 * with large datasets.
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
 * This creates a snapshot of all keys currently in the cache. The returned list
 * is independent of the cache and will not be affected by subsequent cache modifications.
 *
 * ## Examples:
 * ```kotlin
 * // Get all user IDs
 * val allUserIds = userCache.keysList()
 *
 * // Check which keys are present
 * val presentKeys = cache.keysList().filter { it.startsWith("active_") }
 *
 * // Bulk operations on keys
 * val keysToRefresh = cache.keysList().filter { needsRefresh(it) }
 * keysToRefresh.forEach { key -> refreshCacheEntry(key) }
 * ```
 *
 * ## Performance Note:
 * This operation creates a new list and copies all keys, which may be expensive
 * for large caches. Use [Cache.keys] if you only need to iterate once.
 *
 * @return a list of all keys currently in the cache
 */
fun <K, V> Cache<K, V>.keysList(): List<K> = keys().toList()

/**
 * Returns a list of all values in the cache.
 *
 * This creates a snapshot of all values currently in the cache. The returned list
 * is independent of the cache and will not be affected by subsequent cache modifications.
 *
 * ## Examples:
 * ```kotlin
 * // Get all cached users
 * val allUsers = userCache.valuesList()
 *
 * // Analyze cached data
 * val averageSize = cache.valuesList().map { it.size }.average()
 *
 * // Bulk validation
 * val invalidValues = cache.valuesList().filter { !it.isValid() }
 * if (invalidValues.isNotEmpty()) {
 *     logger.warn("Found ${invalidValues.size} invalid cached values")
 * }
 * ```
 *
 * ## Performance Note:
 * This operation creates a new list and copies all values, which may be expensive
 * for large caches. Use [Cache.values] if you only need to iterate once.
 *
 * @return a list of all values currently in the cache
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
 *
 * This operator overload allows you to use familiar array-like syntax for putting
 * values into the cache, making cache operations feel more natural and Kotlin-idiomatic.
 *
 * ## Examples:
 * ```kotlin
 * // Array-like syntax for cache operations
 * cache["user123"] = user
 * cache["session_abc"] = sessionData
 * cache["config_timeout"] = Duration.ofSeconds(30)
 *
 * // Equivalent to traditional put operations
 * cache.put("user123", user)  // Traditional approach
 * cache["user123"] = user     // Kotlin operator approach
 *
 * // Useful in DSL-like contexts
 * fun setupUserCache(cache: Cache<String, User>) {
 *     cache["admin"] = adminUser
 *     cache["guest"] = guestUser
 *     cache["default"] = defaultUser
 * }
 * ```
 *
 * @param key key with which the specified value is to be associated
 * @param value value to be associated with the specified key
 */
operator fun <K, V> Cache<K, V>.set(
    key: K,
    value: V,
) = put(key, value)

/**
 * Checks if the cache contains the given key using 'in' operator.
 *
 * This operator overload enables the use of Kotlin's 'in' operator to check for
 * key presence, providing a more readable alternative to containsKey().
 *
 * ## Examples:
 * ```kotlin
 * // Natural 'in' operator usage
 * if ("user123" in cache) {
 *     val user = cache["user123"]
 *     processUser(user)
 * }
 *
 * // Equivalent to traditional containsKey
 * if (cache.containsKey("user123")) {  // Traditional approach
 *     // ...
 * }
 * if ("user123" in cache) {            // Kotlin operator approach
 *     // ...
 * }
 *
 * // Useful in filtering operations
 * val existingKeys = requestedKeys.filter { it in cache }
 * val missingKeys = requestedKeys.filterNot { it in cache }
 * ```
 *
 * @param key the key to check for presence in the cache
 * @return true if the cache contains the specified key
 */
operator fun <K, V> Cache<K, V>.contains(key: K): Boolean = containsKey(key)

/**
 * Adds a key-value pair to the cache using += operator.
 *
 * This operator overload allows adding entries using the += operator with Pair objects,
 * providing a concise way to add multiple entries or use destructuring.
 *
 * ## Examples:
 * ```kotlin
 * // Add single entry with += operator
 * cache += "user123" to user
 * cache += "session_abc" to sessionData
 *
 * // Bulk operations with pairs
 * val newEntries = listOf(
 *     "user1" to user1,
 *     "user2" to user2,
 *     "user3" to user3
 * )
 * newEntries.forEach { cache += it }
 *
 * // Using with destructuring
 * val userData = loadUserData()
 * cache += userData  // Where userData is a Pair<String, User>
 *
 * // In functional programming contexts
 * userList.map { it.id to it }.forEach { cache += it }
 * ```
 *
 * @param pair a Pair containing the key and value to be added
 */
operator fun <K, V> Cache<K, V>.plusAssign(pair: Pair<K, V>) = put(pair.first, pair.second)

/**
 * Removes a key from the cache using -= operator.
 *
 * This operator overload provides a concise way to remove entries from the cache
 * using the -= operator, making removal operations more expressive.
 *
 * ## Examples:
 * ```kotlin
 * // Remove single entry with -= operator
 * cache -= "user123"
 * cache -= "expired_session"
 *
 * // Bulk removal operations
 * val keysToRemove = listOf("user1", "user2", "user3")
 * keysToRemove.forEach { cache -= it }
 *
 * // Conditional removal
 * if (user.isExpired) {
 *     cache -= user.id
 * }
 *
 * // In cleanup operations
 * expiredKeys.forEach { cache -= it }
 * ```
 *
 * @param key the key to be removed from the cache
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
 *
 * This function provides a Kotlin-idiomatic way to configure cache settings using
 * a type-safe DSL (Domain Specific Language). It's more readable and less verbose
 * than using the traditional builder pattern.
 *
 * ## Examples:
 * ```kotlin
 * // Basic cache configuration
 * val config = cacheConfig<String, User> {
 *     maximumSize(1000L)
 *     expireAfterWrite(Duration.ofMinutes(30))
 *     recordStats(true)
 * }
 *
 * // Advanced configuration with custom eviction and loaders
 * val advancedConfig = cacheConfig<String, ApiResponse> {
 *     maximumSize(500L)
 *     expireAfterAccess(Duration.ofMinutes(10))
 *     refreshAfterWrite(Duration.ofMinutes(5))
 *
 *     // Custom loader
 *     loader { key -> apiClient.fetchData(key) }
 *
 *     // Custom eviction strategy
 *     evictionStrategy(LRUEvictionStrategy())
 *
 *     // Event listeners
 *     listener(object : CacheEventListener<String, ApiResponse> {
 *         override fun onEvict(key: String, value: ApiResponse, reason: EvictionReason) {
 *             logger.info("Evicted $key due to $reason")
 *         }
 *         // ... other methods
 *     })
 * }
 *
 * // Weight-based cache
 * val weightedConfig = cacheConfig<String, LargeObject> {
 *     maximumWeight(1_000_000L)  // 1MB total
 *     weigher { _, value -> value.sizeInBytes }
 *     evictionStrategy(WeightBasedEvictionStrategy(1_000_000L))
 * }
 * ```
 *
 * ## Benefits over Traditional Builder:
 * - **Type Safety**: Full IDE support with auto-completion
 * - **Readability**: Clean, declarative syntax
 * - **Flexibility**: Easy to add conditional configuration
 * - **Kotlin Idiomatic**: Feels natural in Kotlin codebases
 *
 * @param configure the configuration block that sets up the cache
 * @return a configured CacheConfig instance
 */
inline fun <K, V> cacheConfig(
    configure: CacheConfigBuilder<K, V>.() -> Unit,
): io.github.dhruv1110.jcachex.CacheConfig<K, V> {
    val builder = CacheConfigBuilder<K, V>()
    builder.configure()
    return builder.build()
}

/**
 * DSL wrapper for CacheConfig.Builder providing a Kotlin-idiomatic configuration experience.
 *
 * This class wraps the Java CacheConfig.Builder and provides a cleaner, more Kotlin-like
 * interface for configuring cache settings. It supports all the same configuration options
 * as the underlying Java builder but with improved syntax and type safety.
 *
 * ## Usage Pattern:
 * ```kotlin
 * val config = cacheConfig<String, User> {
 *     // All configuration methods are available here
 *     maximumSize(1000L)
 *     expireAfterWrite(Duration.ofMinutes(30))
 *     // ... other configuration options
 * }
 * ```
 *
 * ## Available Configuration Options:
 * - **Size Limits**: `maximumSize()`, `maximumWeight()`, `weigher()`
 * - **Expiration**: `expireAfterWrite()`, `expireAfterAccess()`, `refreshAfterWrite()`
 * - **Loading**: `loader()`, `asyncLoader()`
 * - **Eviction**: `evictionStrategy()`
 * - **References**: `weakKeys()`, `weakValues()`, `softValues()`
 * - **Performance**: `initialCapacity()`, `concurrencyLevel()`, `recordStats()`
 * - **Events**: `listener()`
 * - **Storage**: `directory()`
 *
 * @param K the type of keys maintained by the cache
 * @param V the type of mapped values
 * @see cacheConfig
 * @see io.github.dhruv1110.jcachex.CacheConfig.Builder
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

    fun frequencySketchType(sketchType: FrequencySketchType) = apply { builder.frequencySketchType(sketchType) }

    fun build(): io.github.dhruv1110.jcachex.CacheConfig<K, V> = builder.build()
}

// ===== ADDITIONAL UTILITY FUNCTIONS =====

/**
 * Creates a default cache with the given configuration.
 */
fun <K, V> createCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return DefaultCache(config)
}

/**
 * Creates an optimized cache with advanced eviction strategies.
 */
fun <K, V> createOptimizedCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return OptimizedCache(config)
}

/**
 * Creates a JIT-optimized cache for high-performance scenarios.
 */
fun <K, V> createJITOptimizedCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return JITOptimizedCache(config)
}

/**
 * Creates an allocation-optimized cache for memory-sensitive applications.
 */
fun <K, V> createAllocationOptimizedCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return AllocationOptimizedCache(config)
}

/**
 * Creates a locality-optimized cache for better CPU cache performance.
 */
fun <K, V> createLocalityOptimizedCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return CacheLocalityOptimizedCache(config)
}

/**
 * Creates a zero-copy optimized cache for minimal allocation overhead.
 */
fun <K, V> createZeroCopyOptimizedCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return ZeroCopyOptimizedCache(config)
}

/**
 * Creates a read-only optimized cache for read-heavy workloads.
 */
fun <K, V> createReadOnlyOptimizedCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return ReadOnlyOptimizedCache(config)
}

/**
 * Creates a write-heavy optimized cache for write-intensive workloads.
 */
fun <K, V> createWriteHeavyOptimizedCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return WriteHeavyOptimizedCache(config)
}

/**
 * Creates a JVM-optimized cache with GC-aware optimizations.
 */
fun <K, V> createJVMOptimizedCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return JVMOptimizedCache(config)
}

/**
 * Creates a hardware-optimized cache for specific hardware configurations.
 */
fun <K, V> createHardwareOptimizedCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return HardwareOptimizedCache(config)
}

/**
 * Creates an ML-optimized cache with machine learning-based eviction.
 */
fun <K, V> createMLOptimizedCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return MLOptimizedCache(config)
}

/**
 * Creates a profiled optimized cache for development and testing.
 */
fun <K, V> createProfiledOptimizedCache(
    configure: CacheConfigBuilder<K, V>.() -> Unit
): io.github.dhruv1110.jcachex.Cache<K, V> {
    val config = cacheConfig(configure)
    return ProfiledOptimizedCache(config)
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
