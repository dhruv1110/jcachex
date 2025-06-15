package io.github.dpflux.jcachex.example.kotlin

import io.github.dpflux.jcachex.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration

fun main() = runBlocking {
    // Create a cache with LRU eviction
    val cache = CacheBuilder<String, String>()
        .maximumSize(100)
        .expireAfterWrite(Duration.ofMinutes(5))
        .evictionStrategy(LRUEvictionStrategy())
        .build()

    // Synchronous operations
    cache.put("key1", "value1")
    val value = cache.get("key1")
    println("Value for key1: $value")

    // Asynchronous operations with coroutines
    launch(Dispatchers.IO) {
        val asyncValue = cache.getSuspend("key1")
        println("Async value for key1: $asyncValue")
    }

    // Cache stats
    val stats = cache.stats()
    println("Cache stats: $stats")

    // Cache event listener
    cache.addListener(object : CacheEventListener<String, String> {
        override fun onPut(key: String, value: String) {
            println("Put: $key = $value")
        }

        override fun onRemove(key: String, value: String) {
            println("Remove: $key = $value")
        }

        override fun onEvict(key: String, value: String, reason: EvictionReason) {
            println("Evict: $key = $value ($reason)")
        }
    })

    // Cleanup
    cache.clear()
}
