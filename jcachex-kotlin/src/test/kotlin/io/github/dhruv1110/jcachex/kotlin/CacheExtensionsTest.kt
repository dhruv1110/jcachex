package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.eviction.LRUEvictionStrategy
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("LargeClass")
class CacheExtensionsTest {
    private lateinit var cache: Cache<String, String>

    @BeforeEach
    fun setUp() {
        cache =
            createUnifiedCache {
                maximumSize(100)
                expireAfterWrite(Duration.ofMinutes(5))
                recordStats(true)
            }
    }

    @Test
    fun `test DSL cache creation`() {
        val testCache =
            createUnifiedCache<String, Int> {
                maximumSize(50)
                recordStats(true)
            }

        assertNotNull(testCache)
        assertEquals(0, testCache.size())
        assertTrue(testCache.isEmpty())
    }

    @Test
    fun `test operator overloading`() {
        // Test array-like access
        cache["key1"] = "value1"
        assertEquals("value1", cache["key1"])

        // Test += operator
        cache += "key2" to "value2"
        assertEquals("value2", cache["key2"])

        // Test in operator
        assertTrue("key1" in cache)
        assertFalse("nonexistent" in cache)

        // Test -= operator
        cache -= "key1"
        assertFalse("key1" in cache)
    }

    @Test
    fun `test collection-like operations`() {
        cache.putAll(
            mapOf(
                "user1" to "John",
                "user2" to "Jane",
                "admin1" to "Admin",
            ),
        )

        assertEquals(3, cache.size())
        assertFalse(cache.isEmpty())
        assertTrue(cache.isNotEmpty())

        // Test filtering
        val users = cache.filterKeys { it.startsWith("user") }
        assertEquals(2, users.size)
        assertTrue(users.containsKey("user1"))
        assertTrue(users.containsKey("user2"))

        val shortNames = cache.filterValues { it.length <= 4 }
        assertEquals(2, shortNames.size)
        assertTrue(shortNames.containsValue("John"))
        assertTrue(shortNames.containsValue("Jane"))

        // Test mapping
        val upperCaseValues = cache.mapValues { it.uppercase() }
        assertEquals("JOHN", upperCaseValues["user1"])
        assertEquals("JANE", upperCaseValues["user2"])

        // Test containsValue
        assertTrue(cache.containsValue("John"))
        assertFalse(cache.containsValue("NonExistent"))
    }

    @Test
    fun `test bulk operations`() {
        cache.putAll(
            mapOf(
                "a" to "1",
                "b" to "2",
                "c" to "3",
            ),
        )

        // Test getAll
        val allValues = cache.getAll(listOf("a", "b", "d"))
        assertEquals(3, allValues.size)
        assertEquals("1", allValues["a"])
        assertEquals("2", allValues["b"])
        assertNull(allValues["d"])

        // Test getAllPresent
        val presentValues = cache.getAllPresent(listOf("a", "b", "d"))
        assertEquals(2, presentValues.size)
        assertEquals("1", presentValues["a"])
        assertEquals("2", presentValues["b"])
        assertFalse(presentValues.containsKey("d"))

        // Test removeAll
        val removedValues = cache.removeAll(listOf("a", "b"))
        assertEquals(2, removedValues.size)
        assertEquals("1", removedValues[0])
        assertEquals("2", removedValues[1])

        // Test retainAll
        cache.putAll(mapOf("x" to "10", "y" to "20", "z" to "30"))
        cache.retainAll(listOf("x", "y"))
        assertEquals(2, cache.size())
        assertTrue(cache.containsKey("x"))
        assertTrue(cache.containsKey("y"))
        assertFalse(cache.containsKey("z"))
    }

    @Test
    fun `test utility functions`() {
        cache.putAll(
            mapOf(
                "short" to "a",
                "medium" to "hello",
                "long" to "verylongvalue",
            ),
        )

        // Test find
        val found = cache.find { _, value -> value.length > 5 }
        assertNotNull(found)
        assertTrue(found!!.value.length > 5)

        // Test findAll
        val allLong = cache.findAll { _, value -> value.length > 3 }
        assertEquals(2, allLong.size)

        // Test count
        val longCount = cache.count { _, value -> value.length > 3 }
        assertEquals(2, longCount)

        // Test any
        assertTrue(cache.any { _, value -> value.length > 10 })
        assertFalse(cache.any { _, value -> value.length > 20 })

        // Test all
        assertTrue(cache.all { _, value -> value.length > 0 })
        assertFalse(cache.all { _, value -> value.length > 5 })
    }

    @Test
    fun `test advanced computations`() {
        // Test computeIfAbsent
        val computed =
            cache.computeIfAbsent("new_key") {
                "computed_new_key"
            }
        assertEquals("computed_new_key", computed)
        assertEquals("computed_new_key", cache["new_key"])

        // Test computeIfPresent
        cache["existing"] = "original"
        val modified =
            cache.computeIfPresent("existing") { key, value ->
                "${value}_modified"
            }
        assertEquals("original_modified", modified)
        assertEquals("original_modified", cache["existing"])

        // Test compute
        val newValue =
            cache.compute("compute_key") { key, value ->
                if (value == null) "new_$key" else "${value}_updated"
            }
        assertEquals("new_compute_key", newValue)

        // Test merge
        cache["merge_key"] = "original"
        val merged =
            cache.merge("merge_key", "_suffix") { existing, new ->
                "$existing$new"
            }
        assertEquals("original_suffix", merged)
    }

    @Test
    fun `test safe operations`() {
        cache["safe_key"] = "safe_value"

        // Test getOrNull
        val safeResult = cache.getOrNull("safe_key")
        assertTrue(safeResult.isSuccess)
        assertEquals("safe_value", safeResult.getOrNull())

        // Test putOrNull
        val putResult = cache.putOrNull("new_safe_key", "new_safe_value")
        assertTrue(putResult.isSuccess)

        // Test ifContains
        var containsExecuted = false
        cache.ifContains("safe_key") { value ->
            containsExecuted = true
            assertEquals("safe_value", value)
        }
        assertTrue(containsExecuted)

        // Test ifNotContains
        var notContainsExecuted = false
        cache.ifNotContains("nonexistent") {
            notContainsExecuted = true
        }
        assertTrue(notContainsExecuted)
    }

    @Test
    fun `test statistics extensions`() {
        // Perform some operations to generate stats
        cache["key1"] = "value1"
        cache["key2"] = "value2"
        cache.get("key1") // hit
        cache.get("key1") // hit
        cache.get("nonexistent") // miss

        val stats = cache.stats()

        // Test percentage methods
        val hitRate = stats.hitRatePercent()
        assertTrue(hitRate > 0)
        assertTrue(hitRate <= 100)

        val missRate = stats.missRatePercent()
        assertTrue(missRate >= 0)
        assertTrue(missRate < 100)

        assertEquals(100.0, hitRate + missRate, 0.01)

        // Test formatted output
        val formatted = stats.formatted()
        assertTrue(formatted.contains("Hit Rate:"))
        assertTrue(formatted.contains("Miss Rate:"))
        assertTrue(formatted.contains("Hits:"))
        assertTrue(formatted.contains("Misses:"))
    }

    @Test
    fun `test sequence operations`() {
        cache.putAll(
            mapOf(
                "short" to "a",
                "medium" to "hello",
                "verylongkey" to "value",
            ),
        )

        val longKeyValues =
            cache.asSequence()
                .filter { it.key.length > 5 }
                .map { "${it.key}=${it.value}" }
                .toList()

        assertEquals(2, longKeyValues.size)
        assertTrue(longKeyValues.any { it.startsWith("medium=") })
        assertTrue(longKeyValues.any { it.startsWith("verylongkey=") })
    }

    @Test
    fun `test batch operations`() {
        val result =
            cache.batch {
                put("batch1", "value1")
                put("batch2", "value2")
                put("batch3", "value3")
            }

        assertEquals(cache, result)
        assertEquals(3, cache.size())
        assertEquals("value1", cache["batch1"])
        assertEquals("value2", cache["batch2"])
        assertEquals("value3", cache["batch3"])
    }

    @Test
    fun `test measure time`() {
        val (result, timeNanos) =
            cache.measureTime {
                put("timed_key", "timed_value")
                get("timed_key")
            }

        assertEquals("timed_value", result)
        assertTrue(timeNanos > 0)
    }

    @Test
    fun `test conversion operations`() {
        cache.putAll(
            mapOf(
                "a" to "1",
                "b" to "2",
                "c" to "3",
            ),
        )

        // Test toMap
        val map = cache.toMap()
        assertEquals(3, map.size)
        assertEquals("1", map["a"])
        assertEquals("2", map["b"])
        assertEquals("3", map["c"])

        // Test toMutableMap
        val mutableMap = cache.toMutableMap()
        assertEquals(3, mutableMap.size)
        mutableMap["d"] = "4"
        assertEquals(4, mutableMap.size)
        assertEquals(3, cache.size()) // Original cache unchanged
    }

    @Test
    fun `test grouping and partitioning`() {
        cache.putAll(
            mapOf(
                "user1" to "John",
                "user2" to "Jane",
                "admin1" to "Admin",
                "admin2" to "SuperAdmin",
            ),
        )

        // Test groupBy
        val grouped = cache.groupBy { key, _ -> key.substring(0, 4) }
        assertEquals(2, grouped.size)
        assertTrue(grouped.containsKey("user"))
        assertTrue(grouped.containsKey("admi"))
        assertEquals(2, grouped["user"]!!.size)
        assertEquals(2, grouped["admi"]!!.size)

        // Test partition
        val (users, admins) = cache.partition { key, _ -> key.startsWith("user") }
        assertEquals(2, users.size)
        assertEquals(2, admins.size)
    }

    @Test
    fun `test min and max operations`() {
        cache.putAll(
            mapOf(
                "a" to "short",
                "b" to "medium",
                "c" to "verylongvalue",
            ),
        )

        val minEntry = cache.minByOrNull { _, value -> value.length }
        assertNotNull(minEntry)
        assertEquals("short", minEntry!!.value)

        val maxEntry = cache.maxByOrNull { _, value -> value.length }
        assertNotNull(maxEntry)
        assertEquals("verylongvalue", maxEntry!!.value)
    }

    @Test
    fun `test summary`() {
        cache.putAll(
            mapOf(
                "key1" to "value1",
                "key2" to "value2",
            ),
        )

        val summary = cache.summary()
        assertTrue(summary.contains("Size: 2"))
        assertTrue(summary.contains("Empty: false"))
        assertTrue(summary.contains("Cache Summary:"))
    }

    @Test
    fun `test coroutine extensions`() =
        runBlocking {
            // Test getDeferred
            cache["async_key"] = "async_value"
            val deferred = cache.getDeferred("async_key", this)
            assertEquals("async_value", deferred.await())

            // Test putDeferred
            cache.putDeferred("new_async_key", "new_async_value", this).await()
            assertEquals("new_async_value", cache["new_async_key"])

            // Test removeDeferred
            val removedValue = cache.removeDeferred("async_key", this).await()
            assertEquals("async_value", removedValue)
            assertFalse(cache.containsKey("async_key"))

            // Test clearDeferred
            cache.clearDeferred(this).await()
            assertTrue(cache.isEmpty())
        }

    @Test
    fun `test suspending getOrPut`() =
        runBlocking {
            var computeCount = 0
            val value1 =
                cache.getOrPut("suspend_key") {
                    computeCount++
                    delay(10)
                    "computed_value"
                }

            assertEquals("computed_value", value1)
            assertEquals(1, computeCount)

            // Second call should return cached value
            val value2 =
                cache.getOrPut("suspend_key") {
                    computeCount++
                    delay(10)
                    "computed_value_2"
                }

            assertEquals("computed_value", value2)
            assertEquals(1, computeCount) // Should not increment
        }

    @Test
    fun `test suspending getOrPutAsync`() =
        runBlocking {
            val value =
                cache.getOrPutAsync("async_compute_key") { key ->
                    delay(10)
                    "computed_for_$key"
                }

            assertEquals("computed_for_async_compute_key", value)
            assertEquals("computed_for_async_compute_key", cache["async_compute_key"])
        }

    @Test
    fun `test removeIf`() {
        cache.putAll(
            mapOf(
                "keep1" to "short",
                "remove1" to "verylongvalue",
                "keep2" to "also",
                "remove2" to "anotherlongvalue",
            ),
        )

        val removedCount = cache.removeIf { _, value -> value.length > 8 }
        assertEquals(2, removedCount)
        assertEquals(2, cache.size())
        assertTrue(cache.containsKey("keep1"))
        assertTrue(cache.containsKey("keep2"))
        assertFalse(cache.containsKey("remove1"))
        assertFalse(cache.containsKey("remove2"))
    }

    @Test
    fun `test replace operations`() {
        cache["replace_key"] = "original"

        // Test replace
        val replaced = cache.replace("replace_key", "new_value")
        assertEquals("new_value", replaced)
        assertEquals("new_value", cache["replace_key"])

        // Test replace with condition
        val conditionalReplaced = cache.replace("replace_key", "new_value", "final_value")
        assertTrue(conditionalReplaced)
        assertEquals("final_value", cache["replace_key"])

        // Test replace with wrong condition
        val wrongReplace = cache.replace("replace_key", "wrong_value", "should_not_replace")
        assertFalse(wrongReplace)
        assertEquals("final_value", cache["replace_key"])
    }

    @Test
    fun `test replaceAll`() {
        cache.putAll(
            mapOf(
                "a" to "1",
                "b" to "2",
                "c" to "3",
            ),
        )

        cache.replaceAll { key, value -> "$key=$value" }

        assertEquals("a=1", cache["a"])
        assertEquals("b=2", cache["b"])
        assertEquals("c=3", cache["c"])
    }

    @Test
    fun `test getOrDefault`() {
        cache["existing"] = "value"

        assertEquals("value", cache.getOrDefault("existing", "default"))
        assertEquals("default", cache.getOrDefault("nonexistent", "default"))
    }

    @Test
    fun `test getOrPutValue`() {
        var computeCount = 0
        val value1 =
            cache.getOrPutValue("compute_key") {
                computeCount++
                "computed"
            }

        assertEquals("computed", value1)
        assertEquals(1, computeCount)

        val value2 =
            cache.getOrPutValue("compute_key") {
                computeCount++
                "computed_again"
            }

        assertEquals("computed", value2)
        assertEquals(1, computeCount) // Should not increment
    }
}
