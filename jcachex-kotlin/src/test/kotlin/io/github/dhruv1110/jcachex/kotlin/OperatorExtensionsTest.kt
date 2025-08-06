@file:Suppress("WildcardImport", "ktlint:standard:no-wildcard-imports")

package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OperatorExtensionsTest {
    private lateinit var cache: Cache<String, String>

    @BeforeEach
    fun setUp() {
        cache =
            createCache {
                maximumSize(100)
                recordStats(true)
            }
    }

    @Test
    fun `test set operator`() {
        // Test array-like access with set operator
        cache["key1"] = "value1"
        cache["key2"] = "value2"

        assertEquals("value1", cache["key1"])
        assertEquals("value2", cache["key2"])

        // Test overwriting existing values
        cache["key1"] = "new_value1"
        assertEquals("new_value1", cache["key1"])
    }

    @Test
    fun `test contains operator`() {
        cache["key1"] = "value1"
        cache["key2"] = "value2"

        // Test 'in' operator for keys
        assertTrue("key1" in cache)
        assertTrue("key2" in cache)
        assertFalse("nonexistent" in cache)
        assertFalse("key3" in cache)
    }

    @Test
    fun `test plusAssign operator with Pair`() {
        // Test += operator with Pair
        cache += "key1" to "value1"
        cache += "key2" to "value2"
        cache += "key3" to "value3"

        assertEquals("value1", cache["key1"])
        assertEquals("value2", cache["key2"])
        assertEquals("value3", cache["key3"])

        // Test overwriting with +=
        cache += "key1" to "new_value1"
        assertEquals("new_value1", cache["key1"])
    }

    @Test
    fun `test minusAssign operator`() {
        cache["key1"] = "value1"
        cache["key2"] = "value2"
        cache["key3"] = "value3"

        // Test -= operator
        cache -= "key1"
        cache -= "key3"

        assertFalse("key1" in cache)
        assertTrue("key2" in cache)
        assertFalse("key3" in cache)

        // Test removing non-existent key (should not throw)
        cache -= "nonexistent"
    }

    @Test
    fun `test operator chaining`() {
        // Test chaining operators
        cache += "key1" to "value1"
        cache += "key2" to "value2"

        assertTrue("key1" in cache)
        assertTrue("key2" in cache)

        cache -= "key1"
        assertFalse("key1" in cache)
        assertTrue("key2" in cache)

        cache["key3"] = "value3"
        assertTrue("key3" in cache)
    }

    @Test
    fun `test operator with different types`() {
        val intCache =
            createCache<Int, String> {
                maximumSize(100)
                recordStats(true)
            }

        // Test with Int keys
        intCache[1] = "one"
        intCache[2] = "two"

        assertTrue(1 in intCache)
        assertTrue(2 in intCache)
        assertFalse(3 in intCache)

        intCache += 3 to "three"
        assertEquals("three", intCache[3])

        intCache -= 1
        assertFalse(1 in intCache)
    }

    @Test
    fun `test operator with null values`() {
        val nullableCache =
            createCache<String, String?> {
                maximumSize(100)
                recordStats(true)
            }

        nullableCache["key1"] = null
        nullableCache["key2"] = "value2"

        assertTrue("key1" in nullableCache)
        assertTrue("key2" in nullableCache)
        assertNull(nullableCache["key1"])
        assertEquals("value2", nullableCache["key2"])
    }

    @Test
    fun `test operator performance with large cache`() {
        // Test operators with larger dataset
        val largeCache =
            createCache<Int, String> {
                maximumSize(1000)
                recordStats(true)
            }

        // Add many entries using +=
        repeat(100) { i ->
            largeCache += i to "value$i"
        }

        assertEquals(100, largeCache.size())

        // Check some entries exist
        assertTrue(0 in largeCache)
        assertTrue(50 in largeCache)
        assertTrue(99 in largeCache)

        // Remove some entries using -=
        repeat(50) { i ->
            largeCache -= i
        }

        assertEquals(50, largeCache.size())
        assertFalse(0 in largeCache)
        assertFalse(49 in largeCache)
        assertTrue(50 in largeCache)
        assertTrue(99 in largeCache)
    }

    @Test
    fun `test operator with complex objects`() {
        data class User(val id: Int, val name: String)

        val userCache =
            createCache<String, User> {
                maximumSize(100)
                recordStats(true)
            }

        val user1 = User(1, "John")
        val user2 = User(2, "Jane")

        userCache["user1"] = user1
        userCache["user2"] = user2

        assertTrue("user1" in userCache)
        assertTrue("user2" in userCache)
        assertEquals(user1, userCache["user1"])
        assertEquals(user2, userCache["user2"])

        userCache += "user3" to User(3, "Bob")
        assertTrue("user3" in userCache)

        userCache -= "user1"
        assertFalse("user1" in userCache)
    }

    @Test
    fun `test operator edge cases`() {
        // Test with empty string keys
        cache[""] = "empty_key_value"
        assertTrue("" in cache)
        assertEquals("empty_key_value", cache[""])

        // Test with special characters in keys
        cache["key with spaces"] = "value with spaces"
        assertTrue("key with spaces" in cache)
        assertEquals("value with spaces", cache["key with spaces"])

        // Test with very long keys
        val longKey = "a".repeat(1000)
        cache[longKey] = "long_key_value"
        assertTrue(longKey in cache)
        assertEquals("long_key_value", cache[longKey])
    }

    @Test
    fun `test operator with concurrent access`() {
        val concurrentCache =
            createCache<String, String> {
                maximumSize(100)
                recordStats(true)
            }

        // Simulate concurrent access patterns
        val threads =
            List(10) { threadId ->
                Thread {
                    repeat(100) { i ->
                        val key = "key_${threadId}_$i"
                        concurrentCache[key] = "value_$i"
                        assertTrue(key in concurrentCache)
                        concurrentCache -= key
                        assertFalse(key in concurrentCache)
                    }
                }
            }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // After all threads complete, cache should be empty
        assertEquals(0, concurrentCache.size())
    }
}
