@file:Suppress("WildcardImport", "ktlint:standard:no-wildcard-imports")

package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtilityExtensionsTest {
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
    fun `test batch operations`() {
        val result =
            cache.batch {
                put("key1", "value1")
                put("key2", "value2")
                put("key3", "value3")
            }

        // Should return the cache itself
        assertSame(cache, result)

        // Verify operations were performed
        assertEquals("value1", cache["key1"])
        assertEquals("value2", cache["key2"])
        assertEquals("value3", cache["key3"])
    }

    @Test
    fun `test computeIfAbsent`() {
        // Test when key doesn't exist
        val value1 = cache.computeIfAbsent("key1") { "computed_value1" }
        assertEquals("computed_value1", value1)
        assertEquals("computed_value1", cache["key1"])

        // Test when key already exists
        val value2 = cache.computeIfAbsent("key1") { "should_not_be_computed" }
        assertEquals("computed_value1", value2) // Should return existing value

        // Test with different key
        val value3 = cache.computeIfAbsent("key2") { "computed_value2" }
        assertEquals("computed_value2", value3)
        assertEquals("computed_value2", cache["key2"])
    }

    @Test
    fun `test computeIfPresent`() {
        cache["key1"] = "value1"
        cache["key2"] = "value2"

        // Test when key exists
        val result1 = cache.computeIfPresent("key1") { key, value -> "updated_$value" }
        assertEquals("updated_value1", result1)
        assertEquals("updated_value1", cache["key1"])

        // Test when key doesn't exist
        val result2 = cache.computeIfPresent("nonexistent") { key, value -> "should_not_be_computed" }
        assertNull(result2)
        assertNull(cache["nonexistent"])
    }

    @Test
    fun `test compute`() {
        // Test when key doesn't exist
        val result1 = cache.compute("key1") { key, value -> "computed_value" }
        assertEquals("computed_value", result1)
        assertEquals("computed_value", cache["key1"])

        // Test when key exists
        val result2 = cache.compute("key1") { key, value -> "updated_$value" }
        assertEquals("updated_computed_value", result2)
        assertEquals("updated_computed_value", cache["key1"])

        // Test removing key by returning null
        val result3 = cache.compute("key1") { key, value -> null }
        assertNull(result3)
        assertNull(cache["key1"])
    }

    @Test
    fun `test merge`() {
        // Test when key doesn't exist
        val result1 = cache.merge("key1", "value1") { existing, new -> existing + "_" + new }
        assertEquals("value1", result1)
        assertEquals("value1", cache["key1"])

        // Test when key exists
        val result2 = cache.merge("key1", "value2") { existing, new -> existing + "_" + new }
        assertEquals("value1_value2", result2)
        assertEquals("value1_value2", cache["key1"])

        // Test with different merge function
        val result3 = cache.merge("key1", "value3") { existing, new -> new }
        assertEquals("value3", result3)
        assertEquals("value3", cache["key1"])
    }

    @Test
    fun `test replace with key and value`() {
        cache["key1"] = "value1"

        // Test when key exists
        val result1 = cache.replace("key1", "new_value1")
        assertEquals("new_value1", result1)
        assertEquals("new_value1", cache["key1"])

        // Test when key doesn't exist
        val result2 = cache.replace("nonexistent", "new_value2")
        assertNull(result2)
        assertNull(cache["nonexistent"])
    }

    @Test
    fun `test replace with key, oldValue, and newValue`() {
        cache["key1"] = "value1"

        // Test when key exists and old value matches
        val result1 = cache.replace("key1", "value1", "new_value1")
        assertTrue(result1)
        assertEquals("new_value1", cache["key1"])

        // Test when key exists but old value doesn't match
        val result2 = cache.replace("key1", "wrong_value", "new_value2")
        assertFalse(result2)
        assertEquals("new_value1", cache["key1"]) // Should not change

        // Test when key doesn't exist
        val result3 = cache.replace("nonexistent", "value1", "new_value3")
        assertFalse(result3)
        assertNull(cache["nonexistent"])
    }

    @Test
    fun `test replaceAll`() {
        cache.putAll(
            mapOf(
                "key1" to "value1",
                "key2" to "value2",
                "key3" to "value3",
            ),
        )

        cache.replaceAll { key, value -> value.uppercase() }

        assertEquals("VALUE1", cache["key1"])
        assertEquals("VALUE2", cache["key2"])
        assertEquals("VALUE3", cache["key3"])
    }

    @Test
    fun `test measureTime`() {
        val (result, time) =
            cache.measureTime {
                put("key1", "value1")
                put("key2", "value2")
                "operation_completed"
            }

        assertEquals("operation_completed", result)
        assertTrue(time > 0) // Should take some time

        // Verify operations were performed
        assertEquals("value1", cache["key1"])
        assertEquals("value2", cache["key2"])
    }

    @Test
    fun `test summary with empty cache`() {
        val summary = cache.summary()

        assertTrue(summary.contains("Cache Summary:"))
        assertTrue(summary.contains("Size: 0"))
        assertTrue(summary.contains("Empty: true"))
        assertTrue(summary.contains("Keys: []"))
    }

    @Test
    fun `test summary with small cache`() {
        cache.putAll(
            mapOf(
                "key1" to "value1",
                "key2" to "value2",
                "key3" to "value3",
            ),
        )

        val summary = cache.summary()

        assertTrue(summary.contains("Cache Summary:"))
        assertTrue(summary.contains("Size: 3"))
        assertTrue(summary.contains("Empty: false"))
        assertTrue(summary.contains("key1"))
        assertTrue(summary.contains("key2"))
        assertTrue(summary.contains("key3"))
    }

    @Test
    fun `test summary with large cache`() {
        // Add more than 10 entries to test truncation
        repeat(15) { i ->
            cache["key$i"] = "value$i"
        }

        val summary = cache.summary()

        assertTrue(summary.contains("Cache Summary:"))
        assertTrue(summary.contains("Size: 15"))
        assertTrue(summary.contains("Empty: false"))
        assertTrue(summary.contains("...")) // Should be truncated
    }

    @Test
    fun `test computeIfAbsent with complex computation`() {
        var computationCount = 0

        val value1 =
            cache.computeIfAbsent("key1") {
                computationCount++
                "expensive_computation_$computationCount"
            }

        val value2 =
            cache.computeIfAbsent("key1") {
                computationCount++
                "should_not_be_computed"
            }

        assertEquals("expensive_computation_1", value1)
        assertEquals("expensive_computation_1", value2) // Should return cached value
        assertEquals(1, computationCount) // Should only compute once
    }

    @Test
    fun `test computeIfPresent with key parameter`() {
        cache["key1"] = "value1"

        val result =
            cache.computeIfPresent("key1") { key, value ->
                assertEquals("key1", key)
                assertEquals("value1", value)
                "updated_$value"
            }

        assertEquals("updated_value1", result)
        assertEquals("updated_value1", cache["key1"])
    }

    @Test
    fun `test merge with complex merge function`() {
        data class Counter(var count: Int = 0)

        val counter1 = Counter(5)
        val counter2 = Counter(3)

        cache["counter1"] = counter1.toString()
        cache["counter2"] = counter2.toString()

        val result1 =
            cache.merge("counter1", Counter(2).toString()) { existing, new ->
                val existingCount = existing.substringAfter("count=").substringBefore(")").toIntOrNull() ?: 0
                val newCount = new.substringAfter("count=").substringBefore(")").toIntOrNull() ?: 0
                Counter(existingCount + newCount).toString()
            }

        val result2 =
            cache.merge("counter2", Counter(4).toString()) { existing, new ->
                val existingCount = existing.substringAfter("count=").substringBefore(")").toIntOrNull() ?: 0
                val newCount = new.substringAfter("count=").substringBefore(")").toIntOrNull() ?: 0
                Counter(existingCount * newCount).toString()
            }

        assertEquals("Counter(count=7)", result1) // 5 + 2
        assertEquals("Counter(count=12)", result2) // 3 * 4

        assertEquals("Counter(count=7)", cache["counter1"])
        assertEquals("Counter(count=12)", cache["counter2"])
    }

    @Test
    fun `test replaceAll with conditional transformation`() {
        cache.putAll(
            mapOf(
                "user1" to "John",
                "user2" to "Jane",
                "admin1" to "Admin",
                "system1" to "System",
            ),
        )

        cache.replaceAll { key, value ->
            when {
                key.startsWith("user") -> value.uppercase()
                key.startsWith("admin") -> "ADMIN_$value"
                else -> value.lowercase()
            }
        }

        assertEquals("JOHN", cache["user1"])
        assertEquals("JANE", cache["user2"])
        assertEquals("ADMIN_Admin", cache["admin1"])
        assertEquals("system", cache["system1"])
    }

    @Test
    fun `test measureTime with different operations`() {
        val (result1, time1) =
            cache.measureTime {
                put("key1", "value1")
            }

        val (result2, time2) =
            cache.measureTime {
                put("key2", "value2")
                put("key3", "value3")
                put("key4", "value4")
            }

        assertNotNull(result1)
        assertNotNull(result2)
        assertTrue(time1 > 0)
        assertTrue(time2 > 0)
        // Timing can be variable due to JIT compilation and system load
        // Just verify that both operations completed successfully
    }

    @Test
    fun `test batch with nested operations`() {
        val result =
            cache.batch {
                put("key1", "value1")
                batch {
                    put("key2", "value2")
                    put("key3", "value3")
                }
                put("key4", "value4")
            }

        assertSame(cache, result)
        assertEquals("value1", cache["key1"])
        assertEquals("value2", cache["key2"])
        assertEquals("value3", cache["key3"])
        assertEquals("value4", cache["key4"])
    }

    @Test
    fun `test compute with null handling`() {
        cache["key1"] = "value1"

        // Test returning null to remove the key
        val result1 = cache.compute("key1") { key, value -> null }
        assertNull(result1)
        assertNull(cache["key1"])

        // Test computing null for non-existent key
        val result2 = cache.compute("key2") { key, value -> null }
        assertNull(result2)
        assertNull(cache["key2"])
    }

    @Test
    fun `test merge with null values`() {
        val nullableCache =
            createCache<String, String?> {
                maximumSize(100)
                recordStats(true)
            }

        nullableCache["key1"] = null

        val result1 = nullableCache.merge("key1", "value1") { existing, new -> new }
        assertEquals("value1", result1)
        assertEquals("value1", nullableCache["key1"])

        val result2 = nullableCache.merge("key2", "value2") { existing, new -> new }
        assertEquals("value2", result2)
        assertEquals("value2", nullableCache["key2"])
    }

    @Test
    fun `test summary with stats`() {
        cache["key1"] = "value1"
        cache.get("key1") // Generate some stats

        val summary = cache.summary()

        assertTrue(summary.contains("Stats:"))
        assertTrue(summary.contains("Hit Rate:"))
        assertTrue(summary.contains("Miss Rate:"))
    }

    @Test
    fun `test utility functions with different types`() {
        val intCache =
            createCache<Int, String> {
                maximumSize(100)
                recordStats(true)
            }

        val result1 = intCache.computeIfAbsent(1) { "one" }
        assertEquals("one", result1)

        val result2 = intCache.merge(1, "ONE") { existing, new -> existing + "_" + new }
        assertEquals("one_ONE", result2)

        val result3 = intCache.replace(1, "new_one")
        assertEquals("new_one", result3)

        val summary = intCache.summary()
        assertTrue(summary.contains("Size: 1"))
    }
}
