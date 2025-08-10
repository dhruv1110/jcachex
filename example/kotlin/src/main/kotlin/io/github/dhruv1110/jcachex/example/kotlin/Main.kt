package io.github.dhruv1110.jcachex.example.kotlin

import io.github.dhruv1110.jcachex.JCacheXBuilder
import io.github.dhruv1110.jcachex.kotlin.*
import io.github.dhruv1110.jcachex.profiles.ProfileRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Duration

/**
 * Sample data classes for demonstration
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val isActive: Boolean = true
)

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: String
)

data class Session(
    val id: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Comprehensive demonstration of JCacheX Kotlin Extensions with New Unified Cache Creation
 */
fun main() {
    println("=== JCacheX Kotlin Extensions Demo (New Unified Patterns) ===\n")

    runBlocking {
        // 1. Basic Extensions & Operator Overloading
        demonstrateBasicExtensionsAndOperators()

        // 2. Profile-based Cache Creation
        demonstrateProfileBasedCaches()

        // 3. Coroutine Support
        demonstrateCoroutineSupport()

        // 4. Collection-like Operations
        demonstrateCollectionOperations()

        // 5. Statistics and Monitoring
        demonstrateStatisticsAndMonitoring()

        // 6. Advanced Operations
        demonstrateAdvancedOperations()
    }

    println("\n=== Demo Complete ===")
}

// ===== BASIC EXTENSIONS & OPERATOR OVERLOADING =====

private suspend fun demonstrateBasicExtensionsAndOperators() {
    println("=== 1. Basic Extensions & Operator Overloading ===")

    // Create cache with new unified builder
    val userCache = JCacheXBuilder.create<String, User>()
        .maximumSize(100)
        .expireAfterWrite(Duration.ofMinutes(30))
        .recordStats(true)
        .build()

    // Operator overloading demonstrations
    println("--- Operator Overloading ---")

    // Array-like syntax
    userCache["user1"] = User("1", "Alice", "alice@example.com")
    userCache["user2"] = User("2", "Bob", "bob@example.com")

    // += operator with Pair
    userCache += "user3" to User("3", "Charlie", "charlie@example.com")

    // Check presence with 'in' operator
    println("User1 exists: ${"user1" in userCache}")
    println("User4 exists: ${"user4" in userCache}")

    // Get value with array syntax
    val user1 = userCache["user1"]
    println("Retrieved user: ${user1?.name}")

    // Remove with -= operator
    userCache -= "user3"
    println("After removal, User3 exists: ${"user3" in userCache}")

    // getOrDefault extension
    val defaultUser = userCache.getOrDefault("missing", User("default", "Default User", "default@example.com"))
    println("Default user: ${defaultUser.name}")

    // Basic cache info
    println("Cache size: ${userCache.size()}")
    println("Cache empty: ${userCache.isEmpty()}")
    println("Cache not empty: ${userCache.isNotEmpty()}")

    println()
}

// ===== PROFILE-BASED CACHE CREATION =====

private suspend fun demonstrateProfileBasedCaches() {
    println("=== 2. Profile-based Cache Creation with New Patterns ===")

    // Create caches using profiles from the registry
    val readHeavyCache = JCacheXBuilder.forProfile<String, String>(ProfileRegistry.getProfile("READ_HEAVY"))
        .maximumSize(1000)
        .recordStats(true)
        .build()

    val writeHeavyCache = JCacheXBuilder.forProfile<String, String>(ProfileRegistry.getProfile("WRITE_HEAVY"))
        .maximumSize(500)
        .recordStats(true)
        .build()

    val memoryEfficientCache = JCacheXBuilder.forProfile<String, String>(ProfileRegistry.getProfile("MEMORY_EFFICIENT"))
        .maximumSize(100)
        .recordStats(true)
        .build()

    val highPerformanceCache = JCacheXBuilder.forProfile<String, String>(ProfileRegistry.getProfile("HIGH_PERFORMANCE"))
        .maximumSize(2000)
        .recordStats(true)
        .build()

    // Test each cache type
    println("--- Profile-based Caches ---")

    val testData = "Test data for cache"

    readHeavyCache["read-test"] = testData
    writeHeavyCache["write-test"] = testData
    memoryEfficientCache["memory-test"] = testData
    highPerformanceCache["perf-test"] = testData

    println("Read-heavy cache size: ${readHeavyCache.size()}")
    println("Write-heavy cache size: ${writeHeavyCache.size()}")
    println("Memory-efficient cache size: ${memoryEfficientCache.size()}")
    println("High-performance cache size: ${highPerformanceCache.size()}")

    println()
}

// ===== COROUTINE SUPPORT =====

private suspend fun demonstrateCoroutineSupport() {
    println("=== 3. Coroutine Support ===")

    val apiCache = JCacheXBuilder.create<String, String>()
        .maximumSize(50)
        .expireAfterWrite(Duration.ofMinutes(5))
        .recordStats(true)
        .build()

    // Simulate async API calls
    suspend fun fetchDataFromAPI(endpoint: String): String {
        delay(100) // Simulate network delay
        return "Data from $endpoint at ${System.currentTimeMillis()}"
    }

    // getOrPut with coroutines
    println("--- Coroutine getOrPut ---")
    val data1 = apiCache.getOrPut("api/users") {
        fetchDataFromAPI("api/users")
    }
    println("First call result: $data1")

    // Second call should be from cache
    val data2 = apiCache.getOrPut("api/users") {
        fetchDataFromAPI("api/users")
    }
    println("Second call result: $data2")
    println("Results are same (cached): ${data1 == data2}")

    // getOrPutAsync with key parameter
    println("\n--- Async Operations ---")
    val asyncData = apiCache.getOrPutAsync("api/products") { key ->
        fetchDataFromAPI(key)
    }
    println("Async data: $asyncData")

    println()
}

// ===== COLLECTION-LIKE OPERATIONS =====

private suspend fun demonstrateCollectionOperations() {
    println("=== 4. Collection-like Operations ===")

    val productCache = JCacheXBuilder.create<String, Product>()
        .maximumSize(20)
        .recordStats(true)
        .build()

    // Populate cache
    val products = listOf(
        Product("1", "Laptop", 999.99, "Electronics"),
        Product("2", "Mouse", 29.99, "Electronics"),
        Product("3", "Desk", 199.99, "Furniture"),
        Product("4", "Chair", 149.99, "Furniture"),
        Product("5", "Monitor", 299.99, "Electronics")
    )

    // Bulk operations
    val productMap = products.associateBy { it.id }
    productCache.putAll(productMap)

    println("--- Collection Operations ---")
    println("Total products: ${productCache.size()}")

    // forEach extension
    println("\nAll products:")
    productCache.forEach { (id, product) ->
        println("  $id: ${product.name} - \$${product.price}")
    }

    // Filter operations
    println("\nElectronics products:")
    productCache.filter { (_, product) -> product.category == "Electronics" }
        .forEach { (id, product) ->
            println("  $id: ${product.name}")
        }

    // Map operations
    println("\nProduct names:")
    val productNames = productCache.map { (_, product) -> product.name }
    println("  $productNames")

    // Find operations
    val expensiveProduct = productCache.find { (_, product) -> product.price > 500.0 }
    println("First expensive product: ${expensiveProduct?.value?.name}")

    println()
}

// ===== STATISTICS AND MONITORING =====

private suspend fun demonstrateStatisticsAndMonitoring() {
    println("=== 5. Statistics and Monitoring ===")

    val monitoredCache = JCacheXBuilder.create<String, String>()
        .maximumSize(50)
        .recordStats(true)
        .build()

    // Generate some activity
    repeat(100) { i ->
        monitoredCache["key-$i"] = "value-$i"
    }

    // Create some cache hits
    repeat(50) { i ->
        monitoredCache["key-$i"] // Read existing keys
    }

    // Create some cache misses
    repeat(25) { i ->
        monitoredCache["missing-$i"] // Read non-existent keys
    }

    // Force some evictions by exceeding cache size
    repeat(30) { i ->
        monitoredCache["new-key-$i"] = "new-value-$i"
    }

    println("--- Cache Statistics ---")
    val stats = monitoredCache.stats()

    println("Hit rate: ${String.format("%.2f", stats.hitRate() * 100)}%")
    println("Miss rate: ${String.format("%.2f", stats.missRate() * 100)}%")
    println("Total hits: ${stats.hitCount()}")
    println("Total misses: ${stats.missCount()}")
    println("Total evictions: ${stats.evictionCount()}")

    println()
}

// ===== ADVANCED OPERATIONS =====

private suspend fun demonstrateAdvancedOperations() {
    println("=== 6. Advanced Operations ===")

    val advancedCache = JCacheXBuilder.create<String, Int>()
        .maximumSize(100)
        .recordStats(true)
        .build()

    // Populate cache
    repeat(20) { i ->
        advancedCache["num-$i"] = i * 10
    }

    println("--- Advanced Operations ---")

    // Compute operations
    val computed = advancedCache.computeIfAbsent("computed-key") { key ->
        println("  Computing value for $key")
        42
    }
    println("Computed value: $computed")

    // Second call should not compute (already present)
    val computed2 = advancedCache.computeIfAbsent("computed-key") { key ->
        println("  Computing value for $key (should not print)")
        999
    }
    println("Second computed value: $computed2")

    // Compute with present key
    val updated = advancedCache.computeIfPresent("num-5") { key, value ->
        println("  Updating value for $key from $value")
        value * 2
    }
    println("Updated value: $updated")

    // Replace operations
    val replaced = advancedCache.replace("num-10", 200)
    println("Replaced existing value: $replaced")

    val notReplaced = advancedCache.replace("non-existent", 999)
    println("Replaced non-existent value: $notReplaced")

    // Atomic operations
    val swapped = advancedCache.replace("num-1", 10, 999)
    println("Swapped value (10 -> 999): $swapped")

    println("Cache size after operations: ${advancedCache.size()}")

    println()
}

// Extension functions for demonstration
inline fun <T> measureTime(block: () -> T): Pair<T, Long> {
    val startTime = System.nanoTime()
    val result = block()
    val endTime = System.nanoTime()
    return Pair(result, endTime - startTime)
}

suspend fun <K, V> io.github.dhruv1110.jcachex.Cache<K, V>.getOrPut(key: K, valueFunction: suspend (K) -> V): V {
    val existing = get(key)
    if (existing != null) return existing

    val newValue = valueFunction(key)
    put(key, newValue)
    return newValue
}

suspend fun <K, V> io.github.dhruv1110.jcachex.Cache<K, V>.getOrPutAsync(key: K, valueFunction: suspend (K) -> V): V {
    return getOrPut(key, valueFunction)
}

fun <K, V> io.github.dhruv1110.jcachex.Cache<K, V>.forEach(action: (Map.Entry<K, V>) -> Unit) {
    entries().forEach(action)
}

fun <K, V> io.github.dhruv1110.jcachex.Cache<K, V>.filter(predicate: (Map.Entry<K, V>) -> Boolean): List<Map.Entry<K, V>> {
    return entries().filter(predicate)
}

fun <K, V, R> io.github.dhruv1110.jcachex.Cache<K, V>.map(transform: (Map.Entry<K, V>) -> R): List<R> {
    return entries().map(transform)
}

fun <K, V> io.github.dhruv1110.jcachex.Cache<K, V>.find(predicate: (Map.Entry<K, V>) -> Boolean): Map.Entry<K, V>? {
    return entries().find(predicate)
}

fun <K, V> io.github.dhruv1110.jcachex.Cache<K, V>.getOrDefault(key: K, defaultValue: V): V {
    return get(key) ?: defaultValue
}

operator fun <K, V> io.github.dhruv1110.jcachex.Cache<K, V>.plusAssign(pair: Pair<K, V>) {
    put(pair.first, pair.second)
}

operator fun <K, V> io.github.dhruv1110.jcachex.Cache<K, V>.minusAssign(key: K) {
    remove(key)
}

operator fun <K, V> io.github.dhruv1110.jcachex.Cache<K, V>.contains(key: K): Boolean {
    return containsKey(key)
}

fun <K, V> io.github.dhruv1110.jcachex.Cache<K, V>.isEmpty(): Boolean {
    return size() == 0L
}

fun <K, V> io.github.dhruv1110.jcachex.Cache<K, V>.isNotEmpty(): Boolean {
    return size() > 0L
}
