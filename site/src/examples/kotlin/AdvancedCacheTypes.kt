import io.github.dhruv1110.jcachex.kotlin.*
import io.github.dhruv1110.jcachex.eviction.FrequencySketchType
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

data class User(
    val id: String,
    val name: String,
    val email: String,
    val preferences: Map<String, String> = emptyMap()
)

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: String
)

data class UserSession(
    val userId: String,
    val timestamp: Long,
    val metadata: Map<String, String> = emptyMap()
)

class AdvancedCacheTypesExample {

    fun demonstrateAll() {
        println("=== JCacheX Advanced Cache Types Demo ===")

        // Demonstrate specialized cache types
        demonstrateReadOptimizedCache()
        demonstrateWriteOptimizedCache()
        demonstrateMemoryOptimizedCache()
        demonstrateHighPerformanceCache()

        // Demonstrate frequency sketch options
        demonstrateFrequencySketchOptions()

        // Demonstrate custom configurations
        demonstrateCustomConfigurations()

        // Performance comparison
        performanceComparison()
    }

    private fun demonstrateReadOptimizedCache() {
        println("\n=== Read-Optimized Cache (ReadOnly) ===")

        // Best for read-heavy scenarios - expected ~11.5ns GET performance
        val productCache = createReadOnlyOptimizedCache<String, Product> {
            maxSize = 5000
            expireAfterWrite = 2.hours
            recordStats = true
        }

        // Populate with product data
        repeat(1000) { i ->
            productCache["product-$i"] = Product(
                id = "product-$i",
                name = "Product $i",
                price = 10.0 + i,
                category = "Category ${i % 10}"
            )
        }

        // Measure read performance
        val startTime = System.nanoTime()
        repeat(10000) { i ->
            productCache["product-${i % 1000}"]
        }
        val endTime = System.nanoTime()

        println("Read-optimized GET time: ${(endTime - startTime) / 10000}ns per operation")
        println("Expected: ~11.5ns (1.6x faster than Caffeine)")

        with(productCache.stats()) {
            println("Hit rate: ${(hitRate() * 100).toInt()}%")
            println("Cache size: ${productCache.size()}")
        }
    }

    private fun demonstrateWriteOptimizedCache() {
        println("\n=== Write-Optimized Cache ===")

        // Best for write-heavy scenarios - expected ~393.5ns PUT performance
        val sessionCache = createWriteHeavyOptimizedCache<String, UserSession> {
            maxSize = 10000
            expireAfterAccess = 30.minutes
            recordStats = true
        }

        // Measure write performance
        val startTime = System.nanoTime()
        repeat(10000) { i ->
            sessionCache["session-$i"] = UserSession(
                userId = "user-$i",
                timestamp = System.currentTimeMillis(),
                metadata = mapOf("ip" to "192.168.1.$i", "browser" to "Chrome")
            )
        }
        val endTime = System.nanoTime()

        println("Write-optimized PUT time: ${(endTime - startTime) / 10000}ns per operation")
        println("Expected: ~393.5ns (WriteHeavy optimized)")
        println("Cache size: ${sessionCache.size()}")
    }

    private fun demonstrateMemoryOptimizedCache() {
        println("\n=== Memory-Optimized Cache (Allocation) ===")

        // Minimizes GC pressure - expected ~39.7ns GET, ~88.5ns PUT
        val configCache = createAllocationOptimizedCache<String, String> {
            maxSize = 100
            expireAfterWrite = 12.hours
            recordStats = true
        }

        // Test memory efficiency
        repeat(100) { i ->
            configCache["config-$i"] = "value-$i".repeat(10) // Larger values
        }

        println("Memory-optimized cache size: ${configCache.size()}")
        println("Expected: Minimal GC pressure, ~39.7ns GET, ~88.5ns PUT")

        with(configCache.stats()) {
            println("Average load time: ${averageLoadTime()}ms")
            println("Eviction count: ${evictionCount()}")
        }
    }

    private fun demonstrateHighPerformanceCache() {
        println("\n=== High-Performance Cache (JIT Optimized) ===")

        // Balanced performance - expected ~24.6ns GET, ~63.8ns PUT
        val userCache = createJITOptimizedCache<String, User> {
            maxSize = 1000
            expireAfterWrite = 1.hours
            recordStats = true
        }

        // Performance test
        repeat(1000) { i ->
            userCache["user-$i"] = User(
                id = "user-$i",
                name = "User $i",
                email = "user$i@example.com",
                preferences = mapOf("theme" to "dark", "lang" to "en")
            )
        }

        // Measure balanced performance
        val startTime = System.nanoTime()
        repeat(10000) { i ->
            userCache["user-${i % 1000}"]
        }
        val endTime = System.nanoTime()

        println("JIT-optimized GET time: ${(endTime - startTime) / 10000}ns per operation")
        println("Expected: ~24.6ns (balanced performance)")

        with(userCache.stats()) {
            println("Hit rate: ${(hitRate() * 100).toInt()}%")
            println("Request count: ${requestCount()}")
        }
    }

    private fun demonstrateFrequencySketchOptions() {
        println("\n=== Frequency Sketch Options ===")

        // No frequency sketch - minimal overhead
        val noSketchCache = cache<String, String> {
            maxSize = 1000
            evictionStrategy = EvictionStrategy.ENHANCED_LRU
            frequencySketchType = FrequencySketchType.NONE
            recordStats = true
        }

        // Basic frequency sketch - balanced approach (default)
        val basicSketchCache = cache<String, String> {
            maxSize = 1000
            evictionStrategy = EvictionStrategy.ENHANCED_LRU
            frequencySketchType = FrequencySketchType.BASIC
            recordStats = true
        }

        // Optimized frequency sketch - maximum accuracy
        val optimizedSketchCache = cache<String, String> {
            maxSize = 1000
            evictionStrategy = EvictionStrategy.ENHANCED_LFU
            frequencySketchType = FrequencySketchType.OPTIMIZED
            recordStats = true
        }

        // Test different access patterns
        testAccessPattern(noSketchCache, "No Sketch")
        testAccessPattern(basicSketchCache, "Basic Sketch")
        testAccessPattern(optimizedSketchCache, "Optimized Sketch")
    }

    private fun testAccessPattern(cache: Cache<String, String>, description: String) {
        // Populate cache
        repeat(2000) { i ->
            cache["key-$i"] = "value-$i"
        }

        // Hot keys pattern (80-20 rule)
        repeat(1000) {
            repeat(10) { j ->
                cache["key-${j % 100}"] // Access first 100 keys repeatedly
            }
        }

        // Cold keys pattern
        (100..1999).forEach { i ->
            cache["key-$i"] // Access once
        }

        with(cache.stats()) {
            println("$description - Hit rate: ${(hitRate() * 100).toInt()}%")
        }
    }

    private fun demonstrateCustomConfigurations() {
        println("\n=== Custom Cache Configurations ===")

        // Ultra-fast cache with locality optimization
        val localityCache = createCacheLocalityOptimizedCache<String, String> {
            maxSize = 1000
            expireAfterWrite = 30.minutes
            recordStats = true
        }

        // Hardware-optimized cache
        val hardwareCache = createHardwareOptimizedCache<String, String> {
            maxSize = 1000
            expireAfterWrite = 30.minutes
            recordStats = true
        }

        // Test locality optimization (expected ~9.7ns GET)
        repeat(1000) { i ->
            localityCache["key-$i"] = "value-$i"
        }

        val startTime = System.nanoTime()
        repeat(10000) { i ->
            localityCache["key-${i % 1000}"]
        }
        val endTime = System.nanoTime()

        println("Locality-optimized GET time: ${(endTime - startTime) / 10000}ns per operation")
        println("Expected: ~9.7ns (1.9x faster than Caffeine)")

        // Show cache type identification
        println("\nCache type identification:")
        println("Locality cache type: ${localityCache.cacheType}")
        println("Hardware cache type: ${hardwareCache.cacheType}")
    }

    private fun performanceComparison() {
        println("\n=== Performance Comparison ===")

        // Create different cache types for comparison
        val defaultCache = createDefaultCache<String, String> {
            maxSize = 1000
            recordStats = true
        }

        val jitCache = createJITOptimizedCache<String, String> {
            maxSize = 1000
            recordStats = true
        }

        val localityCache = createCacheLocalityOptimizedCache<String, String> {
            maxSize = 1000
            recordStats = true
        }

        val zeroCopyCache = createZeroCopyOptimizedCache<String, String> {
            maxSize = 1000
            recordStats = true
        }

        // Warm up caches
        repeat(1000) { i ->
            val key = "key-$i"
            val value = "value-$i"
            defaultCache[key] = value
            jitCache[key] = value
            localityCache[key] = value
            zeroCopyCache[key] = value
        }

        // Measure GET performance
        val iterations = 100000

        val defaultTime = measureTime {
            repeat(iterations) { i ->
                defaultCache["key-${i % 1000}"]
            }
        }

        val jitTime = measureTime {
            repeat(iterations) { i ->
                jitCache["key-${i % 1000}"]
            }
        }

        val localityTime = measureTime {
            repeat(iterations) { i ->
                localityCache["key-${i % 1000}"]
            }
        }

        val zeroCopyTime = measureTime {
            repeat(iterations) { i ->
                zeroCopyCache["key-${i % 1000}"]
            }
        }

        println("Performance Results (GET operations):")
        println("Default Cache: ${defaultTime / iterations}ns per operation")
        println("JIT Optimized: ${jitTime / iterations}ns per operation")
        println("Locality Optimized: ${localityTime / iterations}ns per operation")
        println("ZeroCopy Optimized: ${zeroCopyTime / iterations}ns per operation")

        println("\nExpected ranges:")
        println("Default: ~40.4ns")
        println("JIT: ~24.6ns")
        println("Locality: ~9.7ns")
        println("ZeroCopy: ~7.9ns (fastest)")
    }

    private fun measureTime(block: () -> Unit): Long {
        val startTime = System.nanoTime()
        block()
        return System.nanoTime() - startTime
    }
}

// Demonstration with coroutines
suspend fun demonstrateAsyncCaching() {
    println("\n=== Async Caching with Coroutines ===")

    val userCache = createJITOptimizedCache<String, User> {
        maxSize = 1000
        expireAfterWrite = 1.hours
        recordStats = true
    }

    // Simulate async data loading
    suspend fun loadUserFromDatabase(userId: String): User {
        delay(100) // Simulate database delay
        return User(
            id = userId,
            name = "User $userId",
            email = "$userId@example.com",
            preferences = mapOf("theme" to "dark", "notifications" to "enabled")
        )
    }

    // Async cache-aside pattern
    suspend fun getUser(userId: String): User? {
        return userCache[userId] ?: run {
            val user = loadUserFromDatabase(userId)
            userCache[userId] = user
            user
        }
    }

    // Test async operations
    val user = getUser("user123")
    println("Loaded user: ${user?.name}")

    // Second access should be from cache
    val cachedUser = getUser("user123")
    println("Cached user: ${cachedUser?.name}")

    with(userCache.stats()) {
        println("Hit rate: ${(hitRate() * 100).toInt()}%")
        println("Miss count: ${missCount()}")
    }
}

fun main() {
    val example = AdvancedCacheTypesExample()
    example.demonstrateAll()

    // Demonstrate async caching
    runBlocking {
        demonstrateAsyncCaching()
    }
}
