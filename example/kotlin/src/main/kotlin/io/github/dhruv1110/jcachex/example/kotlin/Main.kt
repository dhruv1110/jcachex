package io.github.dhruv1110.jcachex.example.kotlin

import io.github.dhruv1110.jcachex.kotlin.*
import io.github.dhruv1110.jcachex.FrequencySketchType
import io.github.dhruv1110.jcachex.eviction.EvictionStrategy
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Duration

data class User(
    val id: String,
    val name: String,
    val email: String,
    val preferences: Map<String, String> = emptyMap()
)

fun main() {
    println("=== JCacheX Kotlin Advanced Features Demo ===\n")

    runBlocking {
        // Demonstrate basic usage with new default
        demonstrateBasicUsage()

        // Demonstrate specialized cache types
        demonstrateSpecializedCacheTypes()

        // Demonstrate enhanced eviction strategies
        demonstrateEnhancedEvictionStrategies()

        // Demonstrate frequency sketch options
        demonstrateFrequencySketchOptions()

        // Demonstrate async operations with coroutines
        demonstrateAsyncOperations()

        // Demonstrate performance testing
        demonstratePerformanceTesting()
    }

    println("\n=== Demo Complete ===")
}

private suspend fun demonstrateBasicUsage() {
    println("=== Basic Usage (TinyWindowLFU Default) ===")

    // Create cache with Kotlin DSL (TinyWindowLFU is now default)
    val userCache = createCache<String, User> {
        maximumSize(1000)
        expireAfterWrite(Duration.ofHours(2))
        expireAfterAccess(Duration.ofMinutes(30))
        frequencySketchType(FrequencySketchType.BASIC)
        recordStats(true)
    }

    // Basic operations with operator overloading
    userCache["user:123"] = User("123", "Alice Johnson", "alice@example.com")
    userCache["user:456"] = User("456", "Bob Wilson", "bob@example.com")

    val user = userCache["user:123"]
    println("Retrieved user: ${user?.name}")

    // Test cache operations
    val cachedUser = userCache["user:456"]
    println("Retrieved cached user: ${cachedUser?.name}")

    printCacheStats(userCache, "Basic Cache")
    println()
}

private suspend fun demonstrateSpecializedCacheTypes() {
    println("=== Specialized Cache Types ===")

    // Read-optimized cache (fastest GET performance)
    val productCache = createReadOnlyOptimizedCache<String, String> {
        maximumSize(5000)
        expireAfterWrite(Duration.ofHours(2))
        recordStats(true)
    }

    // Write-optimized cache
    val sessionCache = createWriteHeavyOptimizedCache<String, String> {
        maximumSize(10000)
        expireAfterAccess(Duration.ofMinutes(30))
        recordStats(true)
    }

    // Memory-optimized cache
    val configCache = createAllocationOptimizedCache<String, String> {
        maximumSize(100)
        expireAfterWrite(Duration.ofHours(12))
        recordStats(true)
    }

    // High-performance cache
    val performanceCache = createJITOptimizedCache<String, String> {
        maximumSize(1000)
        expireAfterWrite(Duration.ofMinutes(15))
        recordStats(true)
    }

    // Test specialized caches
    testCacheType(productCache, "ReadOnly Optimized", "Expected ~11.5ns GET")
    testCacheType(performanceCache, "JIT Optimized", "Expected ~24.6ns GET, ~63.8ns PUT")

    // Show cache type identification
    println("Cache Types:")
    println("Product cache type: ${productCache.cacheType}")
    println("Performance cache type: ${performanceCache.cacheType}")
    println()
}

private suspend fun demonstrateEnhancedEvictionStrategies() {
    println("=== Enhanced Eviction Strategies ===")

    // Enhanced LRU with frequency sketch
    val enhancedLRUCache = createCache<String, String> {
        maximumSize(50)
        evictionStrategy(EvictionStrategy.ENHANCED_LRU<String, String>())
        frequencySketchType(FrequencySketchType.BASIC)
        recordStats(true)
    }

    // Enhanced LFU with frequency buckets
    val enhancedLFUCache = createCache<String, String> {
        maximumSize(50)
        evictionStrategy(EvictionStrategy.ENHANCED_LFU<String, String>())
        frequencySketchType(FrequencySketchType.OPTIMIZED)
        recordStats(true)
    }

    // TinyWindowLFU (default) - hybrid approach
    val tinyWindowLFUCache = createCache<String, String> {
        maximumSize(50)
        evictionStrategy(EvictionStrategy.TINY_WINDOW_LFU<String, String>())
        recordStats(true)
    }

    // Test with skewed access patterns
    testSkewedAccessPattern(enhancedLRUCache, "Enhanced LRU")
    testSkewedAccessPattern(enhancedLFUCache, "Enhanced LFU")
    testSkewedAccessPattern(tinyWindowLFUCache, "TinyWindowLFU")
    println()
}

private suspend fun demonstrateFrequencySketchOptions() {
    println("=== Frequency Sketch Options ===")

    // No frequency sketch - minimal overhead
    val noSketchCache = createCache<String, String> {
        maximumSize(100)
        evictionStrategy(EvictionStrategy.ENHANCED_LRU<String, String>())
        frequencySketchType(FrequencySketchType.NONE)
        recordStats(true)
    }

    // Basic frequency sketch - balanced approach (default)
    val basicSketchCache = createCache<String, String> {
        maximumSize(100)
        evictionStrategy(EvictionStrategy.ENHANCED_LRU<String, String>())
        frequencySketchType(FrequencySketchType.BASIC)
        recordStats(true)
    }

    // Optimized frequency sketch - maximum accuracy
    val optimizedSketchCache = createCache<String, String> {
        maximumSize(100)
        evictionStrategy(EvictionStrategy.ENHANCED_LFU<String, String>())
        frequencySketchType(FrequencySketchType.OPTIMIZED)
        recordStats(true)
    }

    println("Created caches with different frequency sketch types:")
    println("- NONE: Minimal overhead, pure algorithm")
    println("- BASIC: Balanced accuracy and memory usage (default)")
    println("- OPTIMIZED: Maximum accuracy for complex patterns")
    println()
}

private suspend fun demonstrateAsyncOperations() {
    println("=== Async Operations with Coroutines ===")

    val userCache = createJITOptimizedCache<String, User> {
        maximumSize(1000)
        expireAfterWrite(Duration.ofHours(1))
        recordStats(true)
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

    // Async cache-aside pattern with getOrPut
    suspend fun getUser(userId: String): User? {
        return userCache.getOrPut(userId) {
            loadUserFromDatabase(userId)
        }
    }

    // Test async operations
    val user1 = getUser("user123")
    println("Loaded user: ${user1?.name}")

    // Second access should be from cache
    val user2 = getUser("user123")
    println("Cached user: ${user2?.name}")

    printCacheStats(userCache, "Async User Cache")
    println()
}

private suspend fun demonstratePerformanceTesting() {
    println("=== Performance Testing ===")

    // Create different cache types for comparison
    val defaultCache = createCache<String, String> {
        maximumSize(1000)
        recordStats(true)
    }

    val jitCache = createJITOptimizedCache<String, String> {
        maximumSize(1000)
        recordStats(true)
    }

    val localityCache = createLocalityOptimizedCache<String, String> {
        maximumSize(1000)
        recordStats(true)
    }

    val zeroCopyCache = createZeroCopyOptimizedCache<String, String> {
        maximumSize(1000)
        recordStats(true)
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

    fun measureTime(block: () -> Unit): Long {
        val startTime = System.nanoTime()
        block()
        return System.nanoTime() - startTime
    }

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
    println()
}

private suspend fun testCacheType(cache: io.github.dhruv1110.jcachex.Cache<String, String>, typeName: String, expectedPerformance: String) {
    // Warm up
    repeat(100) { i ->
        cache["key$i"] = "value$i"
    }

    // Test basic functionality
    val value = cache["key1"]
    println("$typeName: Retrieved value = $value")
    println("$typeName: $expectedPerformance")
}

private suspend fun testSkewedAccessPattern(cache: io.github.dhruv1110.jcachex.Cache<String, String>, strategyName: String) {
    // Fill cache beyond capacity to trigger eviction
    repeat(100) { i ->
        cache["key$i"] = "value$i"
    }

    // Create hot/cold access pattern (80-20 rule)
    repeat(1000) { i ->
        // Hot keys (first 10 keys) - 80% of accesses
        if (i % 5 < 4) {
            cache["key${i % 10}"]
        } else {
            // Cold keys - 20% of accesses
            cache["key${10 + (i % 40)}"]
        }
    }

    with(cache.stats()) {
        println("$strategyName - Hit rate: ${(hitRate() * 100).toInt()}%, Evictions: ${evictionCount()}")
    }
}

private fun printCacheStats(cache: io.github.dhruv1110.jcachex.Cache<*, *>, cacheName: String) {
    with(cache.stats()) {
        println("$cacheName Statistics:")
        println("  Size: ${cache.size()}")
        println("  Hit Rate: ${(hitRate() * 100).toInt()}%")
        println("  Requests: ${hitCount() + missCount()}")
        println("  Evictions: ${evictionCount()}")
    }
}
