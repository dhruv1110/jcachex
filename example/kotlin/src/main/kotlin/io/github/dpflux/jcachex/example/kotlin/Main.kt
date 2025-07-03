package io.github.dpflux.jcachex.example.kotlin

import io.github.dhruv1110.jcachex.*
import io.github.dhruv1110.jcachex.kotlin.*
import io.github.dhruv1110.jcachex.eviction.LRUEvictionStrategy
import kotlinx.coroutines.*
import java.time.Duration

fun main() {
    println("=== JCacheX Kotlin Extensions Demo ===")

    runBlocking {
        // Example 1: Creating a cache with DSL
        println("\n1. Creating cache with DSL:")
        val cache = createCache<String, String> {
            maximumSize(100)
            expireAfterWrite(Duration.ofMinutes(5))
            evictionStrategy(LRUEvictionStrategy())
            recordStats(true)
        }

        // Example 2: Using operator overloading
        println("\n2. Using operator overloading:")
        cache["key1"] = "value1"
        cache["key2"] = "value2"
        cache += "key3" to "value3"

        println("cache['key1'] = ${cache["key1"]}")
        println("'key2' in cache = ${"key2" in cache}")

                 // Example 3: Coroutine support
         println("\n3. Coroutine support:")
         val deferredValue = cache.getDeferred("key1", this)
         println("Deferred value: ${deferredValue.await()}")

         cache.putDeferred("async_key", "async_value", this).await()
         println("Async put completed")

        // Example 4: getOrPut with suspending function
        val computedValue = cache.getOrPut("computed_key") {
            delay(100) // Simulate async work
            "computed_value_${System.currentTimeMillis()}"
        }
        println("Computed value: $computedValue")

        // Example 5: Collection-like operations
        println("\n5. Collection-like operations:")
        cache.putAll(mapOf(
            "user1" to "John",
            "user2" to "Jane",
            "user3" to "Bob"
        ))

        println("Cache size: ${cache.size()}")
        println("Is empty: ${cache.isEmpty()}")
        println("Is not empty: ${cache.isNotEmpty()}")

        // Filtering operations
        val usersFiltered = cache.filterKeys { it.startsWith("user") }
        println("Users: $usersFiltered")

        val longValues = cache.filterValues { it.length > 3 }
        println("Long values: $longValues")

        // Mapping operations
        val upperCaseValues = cache.mapValues { it.uppercase() }
        println("Uppercase values: $upperCaseValues")

        // Example 6: Bulk operations
        println("\n6. Bulk operations:")
        val multipleValues = cache.getAll(listOf("user1", "user2", "nonexistent"))
        println("Multiple values: $multipleValues")

        val presentValues = cache.getAllPresent(listOf("user1", "user2", "nonexistent"))
        println("Present values: $presentValues")

        // Example 7: Utility functions
        println("\n7. Utility functions:")
        val foundUser = cache.find { key, value -> value == "John" }
        println("Found user: $foundUser")

        val userCount = cache.count { key, _ -> key.startsWith("user") }
        println("User count: $userCount")

        val hasUsers = cache.any { key, _ -> key.startsWith("user") }
        println("Has users: $hasUsers")

        // Example 8: Safe operations
        println("\n8. Safe operations:")
        val safeValue = cache.getOrNull("safe_key")
        println("Safe get result: $safeValue")

        cache.ifContains("user1") { value ->
            println("User1 exists with value: $value")
        }

        cache.ifNotContains("nonexistent") {
            println("Key 'nonexistent' does not exist")
        }

        // Example 9: Advanced computations
        println("\n9. Advanced computations:")
        val computedIfAbsent = cache.computeIfAbsent("new_key") { key ->
            "computed_for_$key"
        }
        println("Computed if absent: $computedIfAbsent")

        val merged = cache.merge("user1", "_modified") { existing, new ->
            "$existing$new"
        }
        println("Merged value: $merged")

        // Example 10: Statistics
        println("\n10. Cache statistics:")
        val stats = cache.stats()
        println("Hit rate: ${"%.2f".format(stats.hitRatePercent())}%")
        println("Total operations: ${stats.hitCount() + stats.missCount()}")
        println("\nFormatted stats:")
        println(stats.formatted())

        // Example 11: Batch operations
        println("\n11. Batch operations:")
        val (result, timeNanos) = cache.measureTime {
            batch {
                put("batch1", "value1")
                put("batch2", "value2")
                put("batch3", "value3")
            }
        }
        println("Batch operations completed in ${timeNanos / 1_000_000.0} ms")

        // Example 12: Sequence operations (lazy evaluation)
        println("\n12. Sequence operations:")
        val longKeyValues = cache.asSequence()
            .filter { it.key.length > 4 }
            .map { "${it.key}=${it.value}" }
            .take(3)
            .toList()
        println("Long key values: $longKeyValues")

        // Example 13: Summary
        println("\n13. Cache summary:")
        println(cache.summary())

        // Clean up
        cache -= "user1"
        cache.removeAll(listOf("user2", "user3"))
        println("\nAfter cleanup, cache size: ${cache.size()}")
    }
}
