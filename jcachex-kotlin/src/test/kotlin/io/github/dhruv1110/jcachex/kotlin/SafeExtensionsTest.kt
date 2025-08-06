package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SafeExtensionsTest {
    private lateinit var cache: Cache<String, String>

    @BeforeEach
    fun setUp() {
        cache = createCache {
            maximumSize(100)
            recordStats(true)
        }
    }

    @Test
    fun `test safeGet`() {
        cache["key1"] = "value1"
        cache["key2"] = "value2"

        val result1 = cache.safeGet("key1")
        val result2 = cache.safeGet("key2")
        val result3 = cache.safeGet("nonexistent")

        assertTrue(result1.isSuccess)
        assertEquals("value1", result1.getOrNull())

        assertTrue(result2.isSuccess)
        assertEquals("value2", result2.getOrNull())

        assertTrue(result3.isSuccess)
        assertNull(result3.getOrNull())
    }

    @Test
    fun `test getOrNull`() {
        cache["key1"] = "value1"

        val result1 = cache.getOrNull("key1")
        val result2 = cache.getOrNull("nonexistent")

        assertTrue(result1.isSuccess)
        assertEquals("value1", result1.getOrNull())

        assertTrue(result2.isSuccess)
        assertNull(result2.getOrNull())
    }

    @Test
    fun `test putOrNull`() {
        val result1 = cache.putOrNull("key1", "value1")
        val result2 = cache.putOrNull("key2", "value2")

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)

        assertEquals("value1", cache["key1"])
        assertEquals("value2", cache["key2"])
    }

    @Test
    fun `test removeOrNull`() {
        cache["key1"] = "value1"
        cache["key2"] = "value2"

        val result1 = cache.removeOrNull("key1")
        val result2 = cache.removeOrNull("key2")
        val result3 = cache.removeOrNull("nonexistent")

        assertTrue(result1.isSuccess)
        assertEquals("value1", result1.getOrNull())

        assertTrue(result2.isSuccess)
        assertEquals("value2", result2.getOrNull())

        assertTrue(result3.isSuccess)
        assertNull(result3.getOrNull())

        // Verify keys were removed
        assertNull(cache["key1"])
        assertNull(cache["key2"])
    }

    @Test
    fun `test ifContains`() {
        cache["key1"] = "value1"
        cache["key2"] = "value2"

        val actions = mutableListOf<String>()

        cache.ifContains("key1") { value ->
            actions.add("action1: $value")
        }

        cache.ifContains("key2") { value ->
            actions.add("action2: $value")
        }

        cache.ifContains("nonexistent") { value ->
            actions.add("action3: $value")
        }

        assertEquals(2, actions.size)
        assertTrue(actions.contains("action1: value1"))
        assertTrue(actions.contains("action2: value2"))
        assertFalse(actions.contains("action3: value3"))
    }

    @Test
    fun `test ifNotContains`() {
        cache["key1"] = "value1"

        val actions = mutableListOf<String>()

        cache.ifNotContains("key1") {
            actions.add("action1")
        }

        cache.ifNotContains("key2") {
            actions.add("action2")
        }

        cache.ifNotContains("nonexistent") {
            actions.add("action3")
        }

        assertEquals(2, actions.size)
        assertFalse(actions.contains("action1"))
        assertTrue(actions.contains("action2"))
        assertTrue(actions.contains("action3"))
    }

    @Test
    fun `test safe operations with null values`() {
        val nullableCache = createCache<String, String?> {
            maximumSize(100)
            recordStats(true)
        }

        nullableCache["key1"] = null
        nullableCache["key2"] = "value2"

        val result1 = nullableCache.safeGet("key1")
        val result2 = nullableCache.safeGet("key2")
        val result3 = nullableCache.safeGet("nonexistent")

        assertTrue(result1.isSuccess)
        assertNull(result1.getOrNull())

        assertTrue(result2.isSuccess)
        assertEquals("value2", result2.getOrNull())

        assertTrue(result3.isSuccess)
        assertNull(result3.getOrNull())
    }

    @Test
    fun `test safe operations with complex objects`() {
        data class User(val id: Int, val name: String)

        val userCache = createCache<String, User> {
            maximumSize(100)
            recordStats(true)
        }

        val user1 = User(1, "John")
        val user2 = User(2, "Jane")

        val putResult1 = userCache.putOrNull("user1", user1)
        val putResult2 = userCache.putOrNull("user2", user2)

        assertTrue(putResult1.isSuccess)
        assertTrue(putResult2.isSuccess)

        val getResult1 = userCache.safeGet("user1")
        val getResult2 = userCache.safeGet("user2")
        val getResult3 = userCache.safeGet("nonexistent")

        assertTrue(getResult1.isSuccess)
        assertEquals(user1, getResult1.getOrNull())

        assertTrue(getResult2.isSuccess)
        assertEquals(user2, getResult2.getOrNull())

        assertTrue(getResult3.isSuccess)
        assertNull(getResult3.getOrNull())
    }

    @Test
    fun `test ifContains with complex objects`() {
        data class Product(val id: String, val name: String, val price: Double)

        val productCache = createCache<String, Product> {
            maximumSize(100)
            recordStats(true)
        }

        val product1 = Product("p1", "Laptop", 999.99)
        val product2 = Product("p2", "Mouse", 29.99)

        productCache["p1"] = product1
        productCache["p2"] = product2

        val expensiveProducts = mutableListOf<Product>()
        val cheapProducts = mutableListOf<Product>()

        productCache.ifContains("p1") { product ->
            if (product.price > 500) {
                expensiveProducts.add(product)
            }
        }

        productCache.ifContains("p2") { product ->
            if (product.price <= 50) {
                cheapProducts.add(product)
            }
        }

        assertEquals(1, expensiveProducts.size)
        assertEquals(product1, expensiveProducts[0])

        assertEquals(1, cheapProducts.size)
        assertEquals(product2, cheapProducts[0])
    }

    @Test
    fun `test safe operations with bulk operations`() {
        cache.putAll(mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "key3" to "value3"
        ))

        val results = mutableListOf<Result<String?>>()

        cache.keysList().forEach { key ->
            results.add(cache.safeGet(key))
        }

        assertEquals(3, results.size)
        assertTrue(results.all { it.isSuccess })

        val values = results.mapNotNull { it.getOrNull() }
        assertEquals(3, values.size)
        assertTrue(values.contains("value1"))
        assertTrue(values.contains("value2"))
        assertTrue(values.contains("value3"))
    }

    @Test
    fun `test safe operations with error handling`() {
        // Create a cache that might throw exceptions
        val problematicCache = createCache<String, String> {
            maximumSize(1) // Very small size to trigger eviction
            recordStats(true)
        }

        // Fill the cache to trigger eviction
        problematicCache["key1"] = "value1"
        problematicCache["key2"] = "value2" // This might trigger eviction

        val result = problematicCache.safeGet("key1")

        // Should not throw exception, should return Result
        assertNotNull(result)
    }

    @Test
    fun `test ifContains with side effects`() {
        var counter = 0

        cache["key1"] = "value1"
        cache["key2"] = "value2"

        cache.ifContains("key1") { value ->
            counter += value.length
        }

        cache.ifContains("key2") { value ->
            counter += value.length
        }

        cache.ifContains("nonexistent") { value ->
            counter += 1000 // Should not execute
        }

        assertEquals(12, counter) // "value1".length + "value2".length = 5 + 5 = 10
    }

    @Test
    fun `test ifNotContains with side effects`() {
        var counter = 0

        cache["key1"] = "value1"

        cache.ifNotContains("key1") {
            counter += 100 // Should not execute
        }

        cache.ifNotContains("key2") {
            counter += 50
        }

        cache.ifNotContains("key3") {
            counter += 25
        }

        assertEquals(75, counter) // 50 + 25 = 75
    }

    @Test
    fun `test safe operations with concurrent access`() {
        val threads = List(10) { threadId ->
            Thread {
                repeat(100) { i ->
                    val key = "key_${threadId}_$i"
                    val value = "value_$i"

                    val putResult = cache.putOrNull(key, value)
                    assertTrue(putResult.isSuccess)

                    val getResult = cache.safeGet(key)
                    assertTrue(getResult.isSuccess)
                    assertEquals(value, getResult.getOrNull())
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // Verify all operations completed successfully
        assertEquals(1000, cache.size())
    }

    @Test
    fun `test safe operations with different types`() {
        val intCache = createCache<Int, String> {
            maximumSize(100)
            recordStats(true)
        }

        val putResult = intCache.putOrNull(1, "one")
        assertTrue(putResult.isSuccess)

        val getResult = intCache.safeGet(1)
        assertTrue(getResult.isSuccess)
        assertEquals("one", getResult.getOrNull())

        intCache.ifContains(1) { value ->
            assertEquals("one", value)
        }

        intCache.ifNotContains(2) {
            // This should execute
        }
    }

    @Test
    fun `test safe operations with empty cache`() {
        val emptyCache = createCache<String, String> {
            maximumSize(100)
            recordStats(true)
        }

        val getResult = emptyCache.safeGet("any_key")
        assertTrue(getResult.isSuccess)
        assertNull(getResult.getOrNull())

        val removeResult = emptyCache.removeOrNull("any_key")
        assertTrue(removeResult.isSuccess)
        assertNull(removeResult.getOrNull())

        var actionExecuted = false
        emptyCache.ifContains("any_key") {
            actionExecuted = true
        }
        assertFalse(actionExecuted)

        emptyCache.ifNotContains("any_key") {
            actionExecuted = true
        }
        assertTrue(actionExecuted)
    }
}
