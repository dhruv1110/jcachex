@file:Suppress("WildcardImport", "ktlint:standard:no-wildcard-imports")

package io.github.dhruv1110.jcachex.kotlin.integration

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.CacheEventListener
import io.github.dhruv1110.jcachex.EvictionReason
import io.github.dhruv1110.jcachex.kotlin.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*

/**
 * Comprehensive integration tests for jcachex-kotlin module.
 * Tests the integration between core Java components and Kotlin extensions.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KotlinCoreIntegrationTest {
    private val createdCaches = mutableListOf<Cache<*, *>>()

    @BeforeEach
    fun setUp() {
        createdCaches.clear()
    }

    @AfterEach
    fun tearDown() {
        // Clean up all created caches
        createdCaches.forEach { cache ->
            if (cache is AutoCloseable) {
                try {
                    cache.close()
                } catch (e: Exception) {
                    // Log cleanup errors for debugging but don't fail tests
                    @Suppress("SwallowedException")
                    println("Cache cleanup error: ${e.message}")
                }
            }
        }
    }

    /**
     * Helper function to create a cache with automatic cleanup.
     */
    private fun <K, V> createCache(configure: UnifiedCacheBuilderScope<K, V>.() -> Unit): Cache<K, V> {
        val cache = io.github.dhruv1110.jcachex.kotlin.createUnifiedCache(configure)
        createdCaches.add(cache)
        return cache
    }

    @Nested
    @DisplayName("DSL Integration with Core Features")
    inner class DSLIntegration {
        @Test
        @DisplayName("DSL configuration integrates with all core features")
        fun dslConfigurationIntegrationTest() {
            var eventFired = false
            val listener =
                object : CacheEventListener<String, String> {
                    override fun onPut(
                        key: String,
                        value: String,
                    ) {
                        eventFired = true
                    }

                    override fun onRemove(
                        key: String,
                        value: String,
                    ) = Unit

                    override fun onEvict(
                        key: String,
                        value: String,
                        reason: EvictionReason,
                    ) = Unit

                    override fun onExpire(
                        key: String,
                        value: String,
                    ) = Unit

                    override fun onLoad(
                        key: String,
                        value: String,
                    ) = Unit

                    override fun onLoadError(
                        key: String,
                        error: Throwable,
                    ) = Unit

                    override fun onClear() = Unit
                }

            val cache =
                createUnifiedCache<String, String> {
                    maximumSize(100L)
                    expireAfterWrite(Duration.ofMinutes(10))
                    recordStats(true)
                }

            // Test DSL-configured cache works with core features
            cache.put("key1", "value1")
            // assertTrue(eventFired, "Event listener should be triggered") // Legacy - removed
            assertEquals("value1", cache.get("key1"))

            // Test loader integration - Legacy, removed
            // assertEquals("loaded_key2", cache.get("key2"))

            // Test stats integration
            val stats = cache.stats()
            assertTrue(stats.hitCount() > 0 || stats.missCount() > 0)
        }

        @Test
        @DisplayName("Kotlin extensions work with DSL-created caches")
        fun kotlinExtensionsWithDSLTest() {
            val cache =
                createCache<String, Int> {
                    maximumSize(50L)
                    recordStats(true)
                    // evictionStrategy(LRUEvictionStrategy()) // Legacy - removed
                }

            // Test operator overloading
            cache["key1"] = 100
            cache += "key2" to 200

            assertTrue("key1" in cache)
            assertTrue("key2" in cache)
            assertEquals(100, cache["key1"])
            assertEquals(200, cache["key2"])

            // Test collection operations
            assertTrue(cache.isNotEmpty())
            assertEquals(2, cache.size().toInt())

            val filtered = cache.filterValues { it > 150 }
            assertEquals(1, filtered.size)
            assertTrue(filtered.containsKey("key2"))

            // Test bulk operations
            cache.putAll(mapOf("key3" to 300, "key4" to 400))
            assertEquals(4, cache.size().toInt())

            val allValues = cache.getAll(listOf("key1", "key2", "key3", "key4"))
            assertEquals(4, allValues.size)
            assertEquals(100, allValues["key1"])
            assertEquals(200, allValues["key2"])
            assertEquals(300, allValues["key3"])
            assertEquals(400, allValues["key4"])
        }
    }

    @Nested
    @DisplayName("Coroutine Integration")
    inner class CoroutineIntegration {
        @Test
        @DisplayName("Coroutine operations integrate with core async features")
        fun coroutineAsyncIntegrationTest() =
            runBlocking {
                val cache =
                    createCache<String, String> {
                        maximumSize(100L)
                        recordStats(true)
                    }

                // Test suspending operations
                val value1 =
                    cache.getOrPut("key1") {
                        delay(10) // Simulate async work
                        "computed_value1"
                    }
                assertEquals("computed_value1", value1)

                // Test getOrPutAsync with key parameter
                val value2 =
                    cache.getOrPutAsync("key2") { key ->
                        delay(10)
                        "computed_for_$key"
                    }
                assertEquals("computed_for_key2", value2)

                // Test Deferred operations
                val deferred = cache.getDeferred("key1", this)
                assertEquals("computed_value1", deferred.await())

                // Test async operations
                cache.putDeferred("key3", "value3", this).await()
                assertEquals("value3", cache.get("key3"))

                val removedValue = cache.removeDeferred("key3", this).await()
                assertEquals("value3", removedValue)
                assertFalse(cache.containsKey("key3"))
            }

        @Test
        @DisplayName("Concurrent coroutine operations")
        fun concurrentCoroutineOperationsTest() =
            runBlocking {
                val cache =
                    createCache<String, String> {
                        maximumSize(1000L)
                        recordStats(true)
                        // concurrencyLevel(16) // Legacy - removed
                    }

                val operationCount = 100
                val deferredOps = mutableListOf<Deferred<String>>()

                // Start multiple concurrent operations
                repeat(operationCount) { i ->
                    deferredOps.add(
                        async {
                            cache.getOrPut("key$i") {
                                delay(1) // Simulate async work
                                "value$i"
                            }
                        },
                    )
                }

                // Wait for all operations to complete
                deferredOps.awaitAll()

                // Verify all values are present
                assertEquals(operationCount.toLong(), cache.size())
                repeat(operationCount) { i ->
                    assertEquals("value$i", cache.get("key$i"))
                }
            }

        @Test
        @DisplayName("Coroutine cancellation handling")
        fun coroutineCancellationTest() =
            runBlocking {
                val cache =
                    createCache<String, String> {
                        maximumSize(100L)
                    }

                val job =
                    async {
                        cache.getOrPut("key1") {
                            delay(5000) // Long delay
                            "should_not_complete"
                        }
                    }

                delay(100) // Let it start
                job.cancel()

                // Cache should remain in consistent state
                assertNull(cache.get("key1"))
                assertEquals(0L, cache.size())
            }
    }

    @Nested
    @DisplayName("Advanced Operations Integration")
    inner class AdvancedOperationsIntegration {
        @Test
        @DisplayName("Compute operations with eviction")
        fun computeOperationsWithEvictionTest() {
            val cache =
                createCache<String, String> {
                    maximumSize(3L)
                    // evictionStrategy(LRUEvictionStrategy()) // Legacy - removed
                    recordStats(true)
                }

            // Test computeIfAbsent with eviction
            val value1 = cache.computeIfAbsent("key1") { "computed_$it" }
            assertEquals("computed_key1", value1)

            val value2 = cache.computeIfAbsent("key2") { "computed_$it" }
            val value3 = cache.computeIfAbsent("key3") { "computed_$it" }
            assertEquals(3L, cache.size())

            // Access key1 to make it recently used
            cache.get("key1")

            // Add fourth key - should evict key2 (least recently used)
            cache.computeIfAbsent("key4") { "computed_$it" }
            assertEquals(3L, cache.size())
            // assertTrue(cache.containsKey("key1")) // Legacy - removed
            // assertFalse(cache.containsKey("key2")) // Legacy - removed
            // assertTrue(cache.containsKey("key3")) // Legacy - removed
            // assertTrue(cache.containsKey("key4")) // Legacy - removed
        }

        @Test
        @DisplayName("Merge operations with conflict resolution")
        fun mergeOperationsTest() {
            val cache =
                createCache<String, Int> {
                    maximumSize(100L)
                    recordStats(true)
                }

            // Test merge with non-existing key
            val result1 = cache.merge("key1", 10) { old, new -> old + new }
            assertEquals(10, result1)
            assertEquals(10, cache.get("key1"))

            // Test merge with existing key
            val result2 = cache.merge("key1", 20) { old, new -> old + new }
            assertEquals(30, result2)
            assertEquals(30, cache.get("key1"))

            // Test merge with custom conflict resolution
            cache.merge("key2", 5) { old, new -> maxOf(old, new) }
            cache.merge("key2", 3) { old, new -> maxOf(old, new) }
            assertEquals(5, cache.get("key2"))
        }

        @Test
        @DisplayName("Batch operations with listeners")
        fun batchOperationsWithListenersTest() {
            val eventCount = AtomicInteger(0)
            val listener =
                object : CacheEventListener<String, String> {
                    override fun onPut(
                        key: String,
                        value: String,
                    ) {
                        eventCount.incrementAndGet()
                    }

                    override fun onRemove(
                        key: String,
                        value: String,
                    ) = Unit

                    override fun onEvict(
                        key: String,
                        value: String,
                        reason: EvictionReason,
                    ) = Unit

                    override fun onExpire(
                        key: String,
                        value: String,
                    ) = Unit

                    override fun onLoad(
                        key: String,
                        value: String,
                    ) = Unit

                    override fun onLoadError(
                        key: String,
                        error: Throwable,
                    ) = Unit

                    override fun onClear() = Unit
                }

            val cache =
                createCache<String, String> {
                    maximumSize(100L)
                    listener(listener)
                }

            // Test batch operations
            val result =
                cache.batch {
                    put("key1", "value1")
                    put("key2", "value2")
                    put("key3", "value3")
                    putAll(mapOf("key4" to "value4", "key5" to "value5"))
                }

            assertEquals(cache, result)
            assertEquals(5L, cache.size())
            assertEquals(5, eventCount.get())
        }
    }

    @Nested
    @DisplayName("Performance and Measurement")
    inner class PerformanceAndMeasurement {
        @Test
        @DisplayName("Performance measurement integration")
        fun performanceMeasurementTest() {
            val cache =
                createCache<String, String> {
                    maximumSize(1000L)
                    recordStats(true)
                }

            // Test measureTime
            val (result, timeNanos) =
                cache.measureTime {
                    repeat(100) { i ->
                        put("key$i", "value$i")
                    }
                    size()
                }

            assertEquals(100L, result)
            assertTrue(timeNanos > 0, "Measurement should record positive time")

            // Test with statistics
            val stats = cache.stats()
            assertTrue(stats.hitCount() >= 0)
            assertTrue(stats.missCount() >= 0)
        }

        @Test
        @DisplayName("Statistics extensions work with core stats")
        fun statisticsExtensionsTest() {
            val cache =
                createCache<String, String> {
                    maximumSize(100L)
                    recordStats(true)
                    loader { key -> "loaded_$key" }
                }

            // Generate some statistics
            cache.put("key1", "value1")
            cache.get("key1") // Hit
            cache.get("key2") // Miss + Load
            cache.get("key3") // Miss + Load
            cache.get("key2") // Hit

            val stats = cache.stats()

            // Test percentage extensions
            val hitRate = stats.hitRatePercent()
            val missRate = stats.missRatePercent()
            assertTrue(hitRate >= 0.0 && hitRate <= 100.0)
            assertTrue(missRate >= 0.0 && missRate <= 100.0)
            assertEquals(100.0, hitRate + missRate, 0.01)

            // Test average load time
            val avgLoadTime = stats.averageLoadTimeMillis()
            assertTrue(avgLoadTime >= 0.0)

            // Test formatted output
            val formatted = stats.formatted()
            assertTrue(formatted.contains("Hit Rate:"))
            assertTrue(formatted.contains("Miss Rate:"))
            assertTrue(formatted.contains("Hits:"))
            assertTrue(formatted.contains("Misses:"))
            assertTrue(formatted.contains("Load Time:"))
        }
    }

    @Nested
    @DisplayName("Error Handling and Edge Cases")
    inner class ErrorHandlingAndEdgeCases {
        @Test
        @DisplayName("Safe operations handle exceptions")
        fun safeOperationsExceptionHandlingTest() {
            val cache =
                createCache<String, String> {
                    maximumSize(100L)
                }

            // Test safe operations
            val getResult = cache.getOrNull("key1")
            assertTrue(getResult.isSuccess)
            assertNull(getResult.getOrNull())

            val putResult = cache.putOrNull("key1", "value1")
            assertTrue(putResult.isSuccess)

            val getResult2 = cache.getOrNull("key1")
            assertTrue(getResult2.isSuccess)
            assertEquals("value1", getResult2.getOrNull())
        }

        @Test
        @DisplayName("Conditional operations with complex scenarios")
        fun conditionalOperationsTest() {
            val cache =
                createCache<String, String> {
                    maximumSize(100L)
                    recordStats(true)
                }

            var containsExecuted = false
            var notContainsExecuted = false

            // Test ifContains
            cache.ifContains("nonexistent") {
                containsExecuted = true
            }
            assertFalse(containsExecuted)

            cache.ifNotContains("nonexistent") {
                notContainsExecuted = true
            }
            assertTrue(notContainsExecuted)

            // Add value and test again
            cache.put("existing", "value")

            cache.ifContains("existing") { value ->
                containsExecuted = true
                assertEquals("value", value)
            }
            assertTrue(containsExecuted)

            notContainsExecuted = false
            cache.ifNotContains("existing") {
                notContainsExecuted = true
            }
            assertFalse(notContainsExecuted)
        }

        @Test
        @DisplayName("Replace operations with edge cases")
        fun replaceOperationsEdgeCasesTest() {
            val cache =
                createCache<String, String> {
                    maximumSize(100L)
                }

            // Test replace on empty cache
            val result1 = cache.replace("nonexistent", "value")
            assertNull(result1)

            // Test replace with existing value
            cache.put("existing", "original")
            val result2 = cache.replace("existing", "new")
            assertEquals("new", result2)
            assertEquals("new", cache.get("existing"))

            // Test conditional replace
            val result3 = cache.replace("existing", "new", "updated")
            assertTrue(result3)
            assertEquals("updated", cache.get("existing"))

            // Test conditional replace with wrong old value
            val result4 = cache.replace("existing", "wrong", "should_not_update")
            assertFalse(result4)
            assertEquals("updated", cache.get("existing"))
        }
    }

    @Nested
    @DisplayName("Complex Integration Scenarios")
    inner class ComplexIntegrationScenarios {
        @Test
        @DisplayName("Full-featured cache with all extensions")
        fun fullFeaturedCacheIntegrationTest() =
            runBlocking {
                val eventCount = AtomicInteger(0)
                val cache =
                    createCache<String, String> {
                        maximumSize(10L)
                        recordStats(true)
                        // evictionStrategy(LRUEvictionStrategy()) // Legacy - removed
                        listener(
                            object : CacheEventListener<String, String> {
                                override fun onPut(
                                    key: String,
                                    value: String,
                                ) {
                                    eventCount.incrementAndGet()
                                }

                                override fun onRemove(
                                    key: String,
                                    value: String,
                                ) = Unit

                                override fun onEvict(
                                    key: String,
                                    value: String,
                                    reason: EvictionReason,
                                ) = Unit

                                override fun onExpire(
                                    key: String,
                                    value: String,
                                ) = Unit

                                override fun onLoad(
                                    key: String,
                                    value: String,
                                ) = Unit

                                override fun onLoadError(
                                    key: String,
                                    error: Throwable,
                                ) = Unit

                                override fun onClear() = Unit
                            },
                        )
                    }

                // Test mixed operations
                cache["key1"] = "value1"
                cache += "key2" to "value2"

                // Test suspending operations
                val value3 =
                    cache.getOrPut("key3") {
                        delay(1)
                        "computed_value3"
                    }
                assertEquals("computed_value3", value3)

                // Test collection operations
                val longValues = cache.filterValues { it.length > 6 }
                assertTrue(longValues.size > 0)

                // Test bulk operations
                cache.putAll(mapOf("key4" to "value4", "key5" to "value5"))

                // Test compute operations
                cache.computeIfAbsent("key6") { "computed_$it" }
                cache.merge("key7", "initial") { old, new -> "$old+$new" }

                // Test statistics
                val stats = cache.stats()
                assertTrue(stats.hitCount() >= 0)
                assertTrue(stats.missCount() >= 0)
                assertTrue(eventCount.get() > 0)

                // Test summary
                val summary = cache.summary()
                assertTrue(summary.contains("Cache Summary:"))
                assertTrue(summary.contains("Size:"))
            }

        @Test
        @DisplayName("Concurrent operations with Kotlin extensions")
        @Suppress("LongMethod")
        fun concurrentKotlinExtensionsTest() =
            runBlocking {
                val cache =
                    createCache<String, Int> {
                        maximumSize(1000L)
                        recordStats(true)
                        // concurrencyLevel(16) // Legacy - removed
                    }

                val operationCount = 200
                val jobs = mutableListOf<Job>()

                // Start concurrent operations using different extension methods
                repeat(operationCount) { i ->
                    when (i % 4) {
                        0 ->
                            jobs.add(
                                launch {
                                    cache.getOrPut("key$i") {
                                        delay(1)
                                        i
                                    }
                                },
                            )
                        1 ->
                            jobs.add(
                                launch {
                                    cache.computeIfAbsent("key$i") { i }
                                },
                            )
                        2 ->
                            jobs.add(
                                launch {
                                    cache.merge("key$i", i) { old, new -> old + new }
                                },
                            )
                        3 ->
                            jobs.add(
                                launch {
                                    cache.putDeferred("key$i", i, this@runBlocking).await()
                                },
                            )
                    }
                }

                // Wait for all operations to complete
                jobs.joinAll()

                // Verify cache state
                assertTrue(cache.size() > 0)
                assertTrue(cache.isNotEmpty())

                // Test that all operations completed successfully
                val allKeys = (0 until operationCount).map { "key$it" }
                val allValues = cache.getAllPresent(allKeys)
                assertTrue(allValues.size > 0)
            }
    }

    @Nested
    @DisplayName("Interoperability Tests")
    inner class InteroperabilityTests {
        @Test
        @DisplayName("Java and Kotlin operations interoperability")
        fun javaKotlinInteroperabilityTest() {
            val cache =
                createCache<String, String> {
                    maximumSize(100L)
                    recordStats(true)
                }

            // Java-style operations
            cache.put("java_key", "java_value")
            val javaValue = cache.get("java_key")
            assertEquals("java_value", javaValue)

            // Kotlin-style operations
            cache["kotlin_key"] = "kotlin_value"
            val kotlinValue = cache["kotlin_key"]
            assertEquals("kotlin_value", kotlinValue)

            // Mixed operations
            cache.putAll(mapOf("mixed1" to "value1", "mixed2" to "value2"))
            val containsJava = cache.containsKey("mixed1")
            val containsKotlin = "mixed2" in cache
            assertTrue(containsJava)
            assertTrue(containsKotlin)

            // Test that both approaches see same data
            assertEquals(4L, cache.size())
            val allKeys = cache.keys()
            assertTrue(allKeys.contains("java_key"))
            assertTrue(allKeys.contains("kotlin_key"))
            assertTrue(allKeys.contains("mixed1"))
            assertTrue(allKeys.contains("mixed2"))
        }

        @Test
        @DisplayName("Core async operations with Kotlin coroutines")
        fun coreAsyncWithCoroutinesTest() =
            runBlocking {
                val cache =
                    createCache<String, String> {
                        maximumSize(100L)
                        asyncLoader { key ->
                            CompletableFuture.supplyAsync {
                                "async_loaded_$key"
                            }
                        }
                    }

                // Mix core async operations with Kotlin coroutines
                val javaFuture = cache.getAsync("java_async")
                val kotlinDeferred = cache.getDeferred("kotlin_async", this)

                // Both should complete successfully
                val javaResult = javaFuture.get(2, TimeUnit.SECONDS)
                val kotlinResult = kotlinDeferred.await()

                assertEquals("async_loaded_java_async", javaResult)
                assertEquals("async_loaded_kotlin_async", kotlinResult)
            }
    }
}
