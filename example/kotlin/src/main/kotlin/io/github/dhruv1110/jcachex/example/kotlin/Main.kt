package io.github.dhruv1110.jcachex.example.kotlin

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.eviction.LRUEvictionStrategy
import io.github.dhruv1110.jcachex.kotlin.createCache
import io.github.dhruv1110.jcachex.kotlin.formatted
import io.github.dhruv1110.jcachex.kotlin.getOrPut
import io.github.dhruv1110.jcachex.kotlin.hitRatePercent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Duration

private const val CACHE_SIZE = 100L
private const val EXPIRE_MINUTES = 5L
private const val DELAY_MILLIS = 100L

fun main() {
    println("=== JCacheX Kotlin Extensions Demo ===")

    runBlocking {
        val cache = createTestCache()

        demonstrateBasicOperations(cache)
        demonstrateCacheStatistics(cache)
    }
}

private fun createTestCache(): Cache<String, String> {
    println("\n1. Creating cache with DSL:")
    return createCache<String, String> {
        maximumSize(CACHE_SIZE)
        expireAfterWrite(
            Duration.ofMinutes(EXPIRE_MINUTES),
        )
        evictionStrategy(LRUEvictionStrategy())
        recordStats(true)
    }
}

private suspend fun demonstrateBasicOperations(cache: Cache<String, String>) {
    println("\n2. Basic operations:")

    // Basic cache operations
    cache.put("key1", "value1")
    cache.put("key2", "value2")

    println("cache.get('key1') = ${cache.get("key1")}")
    println("cache.containsKey('key2') = ${cache.containsKey("key2")}")

    // Coroutine support with getOrPut
    val computedValue =
        cache
            .getOrPut("computed_key") {
                delay(DELAY_MILLIS)
                "computed_value_${System.currentTimeMillis()}"
            }
    println("Computed value: $computedValue")

    println("Cache size: ${cache.size()}")
}

private fun demonstrateCacheStatistics(cache: Cache<String, String>) {
    println("\n3. Cache statistics:")
    val stats = cache.stats()
    println("Hit rate: ${"%.2f".format(stats.hitRatePercent())}%")
    println("Total operations: ${stats.hitCount() + stats.missCount()}")
    println("\nFormatted stats:")
    println(stats.formatted())
}
