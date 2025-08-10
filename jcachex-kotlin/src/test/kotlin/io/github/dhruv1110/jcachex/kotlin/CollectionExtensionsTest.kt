@file:Suppress("WildcardImport", "ktlint:standard:no-wildcard-imports")

package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CollectionExtensionsTest {
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
    fun `test asSequence`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"))

        val sequence = cache.asSequence()
        val entries = sequence.toList()

        assertEquals(3, entries.size)
        assertTrue(entries.any { it.key == "key1" && it.value == "value1" })
        assertTrue(entries.any { it.key == "key2" && it.value == "value2" })
        assertTrue(entries.any { it.key == "key3" && it.value == "value3" })
    }

    @Test
    fun `test toMap`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2"))

        val map = cache.toMap()

        assertEquals(2, map.size)
        assertEquals("value1", map["key1"])
        assertEquals("value2", map["key2"])
    }

    @Test
    fun `test toMutableMap`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2"))

        val mutableMap = cache.toMutableMap()

        assertEquals(2, mutableMap.size)
        assertEquals("value1", mutableMap["key1"])
        assertEquals("value2", mutableMap["key2"])

        // Test that it's mutable
        mutableMap["key3"] = "value3"
        assertEquals(3, mutableMap.size)
        assertEquals("value3", mutableMap["key3"])
    }

    @Test
    fun `test filterKeys`() {
        cache.putAll(
            mapOf(
                "user1" to "John",
                "user2" to "Jane",
                "admin1" to "Admin",
                "system1" to "System",
            ),
        )

        val userKeys = cache.filterKeys { it.startsWith("user") }
        val adminKeys = cache.filterKeys { it.startsWith("admin") }

        assertEquals(2, userKeys.size)
        assertEquals("John", userKeys["user1"])
        assertEquals("Jane", userKeys["user2"])

        assertEquals(1, adminKeys.size)
        assertEquals("Admin", adminKeys["admin1"])
    }

    @Test
    fun `test filterValues`() {
        cache.putAll(
            mapOf(
                "key1" to "short",
                "key2" to "verylongvalue",
                "key3" to "medium",
                "key4" to "tiny",
            ),
        )

        val shortValues = cache.filterValues { it.length <= 6 }
        val longValues = cache.filterValues { it.length > 10 }

        assertEquals(3, shortValues.size)
        assertTrue(shortValues.containsValue("short"))
        assertTrue(shortValues.containsValue("medium"))
        assertTrue(shortValues.containsValue("tiny"))

        assertEquals(1, longValues.size)
        assertEquals("verylongvalue", longValues["key2"])
    }

    @Test
    fun `test filter`() {
        cache.putAll(
            mapOf(
                "user1" to "John",
                "user2" to "Jane",
                "admin1" to "Admin",
                "system1" to "System",
            ),
        )

        val userEntries = cache.filter { key, value -> key.startsWith("user") && value.length == 4 }

        assertEquals(2, userEntries.size)
        assertEquals("John", userEntries["user1"])
        assertEquals("Jane", userEntries["user2"])
    }

    @Test
    fun `test mapValues`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2"))

        val upperCaseValues = cache.mapValues { it.uppercase() }

        assertEquals(2, upperCaseValues.size)
        assertEquals("VALUE1", upperCaseValues["key1"])
        assertEquals("VALUE2", upperCaseValues["key2"])
    }

    @Test
    fun `test mapKeys`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2"))

        val prefixedKeys = cache.mapKeys { "prefix_$it" }

        assertEquals(2, prefixedKeys.size)
        assertEquals("value1", prefixedKeys["prefix_key1"])
        assertEquals("value2", prefixedKeys["prefix_key2"])
    }

    @Test
    fun `test map`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2"))

        val transformed = cache.map { key, value -> key.uppercase() to value.uppercase() }

        assertEquals(2, transformed.size)
        assertEquals("VALUE1", transformed["KEY1"])
        assertEquals("VALUE2", transformed["KEY2"])
    }

    @Test
    fun `test getAll`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"))

        val result = cache.getAll(listOf("key1", "key2", "nonexistent"))

        assertEquals(3, result.size)
        assertEquals("value1", result["key1"])
        assertEquals("value2", result["key2"])
        assertNull(result["nonexistent"])
    }

    @Test
    fun `test getAllPresent`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"))

        val result = cache.getAllPresent(listOf("key1", "key2", "nonexistent"))

        assertEquals(2, result.size)
        assertEquals("value1", result["key1"])
        assertEquals("value2", result["key2"])
        assertFalse(result.containsKey("nonexistent"))
    }

    @Test
    fun `test find`() {
        cache.putAll(
            mapOf(
                "user1" to "John",
                "user2" to "Jane",
                "admin1" to "Admin",
            ),
        )

        val found = cache.find { key, value -> key.startsWith("user") && value.length == 4 }

        assertNotNull(found)
        assertTrue(found!!.key.startsWith("user"))
        assertEquals(4, found.value.length)
    }

    @Test
    fun `test find returns null when no match`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2"))

        val found = cache.find { key, value -> key == "nonexistent" }

        assertNull(found)
    }

    @Test
    fun `test findAll`() {
        cache.putAll(
            mapOf(
                "user1" to "John",
                "user2" to "Jane",
                "admin1" to "Admin",
                "system1" to "System",
            ),
        )

        val userEntries = cache.findAll { key, value -> key.startsWith("user") }

        assertEquals(2, userEntries.size)
        assertTrue(userEntries.any { it.key == "user1" && it.value == "John" })
        assertTrue(userEntries.any { it.key == "user2" && it.value == "Jane" })
    }

    @Test
    fun `test count`() {
        cache.putAll(
            mapOf(
                "user1" to "John",
                "user2" to "Jane",
                "admin1" to "Admin",
                "system1" to "System",
            ),
        )

        val userCount = cache.count { key, value -> key.startsWith("user") }
        val shortNameCount = cache.count { key, value -> value.length <= 4 }

        assertEquals(2, userCount)
        assertEquals(2, shortNameCount) // John, Jane, Admin
    }

    @Test
    fun `test any`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2"))

        val hasUser = cache.any { key, value -> key.startsWith("user") }
        val hasLongValue = cache.any { key, value -> value.length > 5 }

        assertFalse(hasUser)
        assertTrue(hasLongValue)

        cache["user1"] = "John"
        val hasUserAfter = cache.any { key, value -> key.startsWith("user") }
        assertTrue(hasUserAfter)
    }

    @Test
    fun `test all`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2"))

        val allHaveValue = cache.all { key, value -> value.startsWith("value") }
        val allHaveKey = cache.all { key, value -> key.startsWith("key") }

        assertTrue(allHaveValue)
        assertTrue(allHaveKey)

        cache["other"] = "different"
        val allHaveValueAfter = cache.all { key, value -> value.startsWith("value") }
        assertFalse(allHaveValueAfter)
    }

    @Test
    fun `test groupBy`() {
        cache.putAll(
            mapOf(
                "user1" to "John",
                "user2" to "Jane",
                "admin1" to "Admin",
                "system1" to "System",
            ),
        )

        val groupedByPrefix = cache.groupBy { key, value -> key.substring(0, 4) }

        assertEquals(3, groupedByPrefix.size)
        assertEquals(2, groupedByPrefix["user"]?.size)
        assertEquals(1, groupedByPrefix["admi"]?.size)
        assertEquals(1, groupedByPrefix["syst"]?.size)
    }

    @Test
    fun `test partition`() {
        cache.putAll(
            mapOf(
                "user1" to "John",
                "user2" to "Jane",
                "admin1" to "Admin",
                "system1" to "System",
            ),
        )

        val (users, others) = cache.partition { key, value -> key.startsWith("user") }

        assertEquals(2, users.size)
        assertEquals(2, others.size)

        assertTrue(users.all { it.key.startsWith("user") })
        assertTrue(others.none { it.key.startsWith("user") })
    }

    @Test
    fun `test minByOrNull`() {
        cache.putAll(
            mapOf(
                "key1" to "short",
                "key2" to "verylongvalue",
                "key3" to "medium",
            ),
        )

        val shortest = cache.minByOrNull { key, value -> value.length }

        assertNotNull(shortest)
        assertEquals("short", shortest!!.value)
    }

    @Test
    fun `test maxByOrNull`() {
        cache.putAll(
            mapOf(
                "key1" to "short",
                "key2" to "verylongvalue",
                "key3" to "medium",
            ),
        )

        val longest = cache.maxByOrNull { key, value -> value.length }

        assertNotNull(longest)
        assertEquals("verylongvalue", longest!!.value)
    }

    @Test
    fun `test forEach`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2"))

        val collected = mutableListOf<Pair<String, String>>()
        cache.forEach { key, value ->
            collected.add(key to value)
        }

        assertEquals(2, collected.size)
        assertTrue(collected.contains("key1" to "value1"))
        assertTrue(collected.contains("key2" to "value2"))
    }

    @Test
    fun `test keysList`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"))

        val keys = cache.keysList()

        assertEquals(3, keys.size)
        assertTrue(keys.contains("key1"))
        assertTrue(keys.contains("key2"))
        assertTrue(keys.contains("key3"))
    }

    @Test
    fun `test valuesList`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"))

        val values = cache.valuesList()

        assertEquals(3, values.size)
        assertTrue(values.contains("value1"))
        assertTrue(values.contains("value2"))
        assertTrue(values.contains("value3"))
    }

    @Test
    fun `test isEmpty and isNotEmpty`() {
        assertTrue(cache.isEmpty())
        assertFalse(cache.isNotEmpty())

        cache["key1"] = "value1"

        assertFalse(cache.isEmpty())
        assertTrue(cache.isNotEmpty())
    }

    @Test
    fun `test containsValue`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2"))

        assertTrue(cache.containsValue("value1"))
        assertTrue(cache.containsValue("value2"))
        assertFalse(cache.containsValue("nonexistent"))
    }

    @Test
    fun `test putAll`() {
        val map = mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3")

        cache.putAll(map)

        assertEquals(3, cache.size())
        assertEquals("value1", cache["key1"])
        assertEquals("value2", cache["key2"])
        assertEquals("value3", cache["key3"])
    }

    @Test
    fun `test removeAll`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"))

        val removed = cache.removeAll(listOf("key1", "key3", "nonexistent"))

        assertEquals(3, removed.size)
        assertEquals("value1", removed[0])

        assertEquals(1, cache.size())
        assertEquals("value2", cache["key2"])
    }

    @Test
    fun `test retainAll`() {
        cache.putAll(mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"))

        cache.retainAll(listOf("key1", "key3"))

        assertEquals(2, cache.size())
        assertEquals("value1", cache["key1"])
        assertEquals("value3", cache["key3"])
        assertNull(cache["key2"])
    }

    @Test
    fun `test removeIf`() {
        cache.putAll(
            mapOf(
                "user1" to "John",
                "user2" to "Jane",
                "admin1" to "Admin",
                "system1" to "System",
            ),
        )

        val removedCount = cache.removeIf { key, value -> key.startsWith("user") }

        assertEquals(2, removedCount)
        assertEquals(2, cache.size())
        assertNull(cache["user1"])
        assertNull(cache["user2"])
        assertEquals("Admin", cache["admin1"])
        assertEquals("System", cache["system1"])
    }

    @Test
    fun `test getOrDefault`() {
        cache["key1"] = "value1"

        assertEquals("value1", cache.getOrDefault("key1", "default"))
        assertEquals("default", cache.getOrDefault("nonexistent", "default"))
    }

    @Test
    fun `test getOrPutValue`() {
        cache["key1"] = "value1"

        assertEquals("value1", cache.getOrPutValue("key1") { "default" })
        assertEquals("default", cache.getOrPutValue("key2") { "default" })
        assertEquals("default", cache["key2"])
    }
}
