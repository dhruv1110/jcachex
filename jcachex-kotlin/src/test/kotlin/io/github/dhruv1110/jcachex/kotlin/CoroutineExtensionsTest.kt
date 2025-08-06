package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoroutineExtensionsTest {
    private lateinit var cache: Cache<String, String>

    @BeforeEach
    fun setUp() {
        cache = createCache {
            maximumSize(100)
            recordStats(true)
        }
    }

    @Test
    fun `test getDeferred`() = runBlocking {
        cache["key1"] = "value1"
        cache["key2"] = "value2"

        val deferred1 = cache.getDeferred("key1", this)
        val deferred2 = cache.getDeferred("key2", this)
        val deferred3 = cache.getDeferred("nonexistent", this)

        assertEquals("value1", deferred1.await())
        assertEquals("value2", deferred2.await())
        assertNull(deferred3.await())
    }

    @Test
    fun `test putDeferred`() = runBlocking {
        val deferred1 = cache.putDeferred("key1", "value1", this)
        val deferred2 = cache.putDeferred("key2", "value2", this)

        deferred1.await()
        deferred2.await()

        assertEquals("value1", cache["key1"])
        assertEquals("value2", cache["key2"])
    }

    @Test
    fun `test removeDeferred`() = runBlocking {
        cache["key1"] = "value1"
        cache["key2"] = "value2"

        val deferred1 = cache.removeDeferred("key1", this)
        val deferred2 = cache.removeDeferred("key2", this)
        val deferred3 = cache.removeDeferred("nonexistent", this)

        assertEquals("value1", deferred1.await())
        assertEquals("value2", deferred2.await())
        assertNull(deferred3.await())

        assertNull(cache["key1"])
        assertNull(cache["key2"])
    }

    @Test
    fun `test clearDeferred`() = runBlocking {
        cache["key1"] = "value1"
        cache["key2"] = "value2"

        assertEquals(2, cache.size())

        val deferred = cache.clearDeferred(this)
        deferred.await()

        assertEquals(0, cache.size())
        assertNull(cache["key1"])
        assertNull(cache["key2"])
    }

    @Test
    fun `test getOrPutAsync`() = runBlocking {
        // Test when key doesn't exist
        val value1 = cache.getOrPutAsync("key1") { "computed_value1" }
        assertEquals("computed_value1", value1)
        assertEquals("computed_value1", cache["key1"])

        // Test when key already exists
        val value2 = cache.getOrPutAsync("key1") { "should_not_be_computed" }
        assertEquals("computed_value1", value2) // Should return existing value

        // Test with async computation
        val value3 = cache.getOrPutAsync("key2") {
            delay(10) // Simulate async work
            "async_computed_value"
        }
        assertEquals("async_computed_value", value3)
        assertEquals("async_computed_value", cache["key2"])
    }

    @Test
    fun `test getOrPutAsync with complex computation`() = runBlocking {
        val counter = AtomicInteger(0)

        val value1 = cache.getOrPutAsync("key1") {
            delay(50) // Simulate expensive computation
            counter.incrementAndGet()
            "expensive_value_${counter.get()}"
        }

        val value2 = cache.getOrPutAsync("key1") {
            delay(50) // This should not be executed
            counter.incrementAndGet()
            "should_not_be_computed"
        }

        assertEquals("expensive_value_1", value1)
        assertEquals("expensive_value_1", value2) // Should return cached value
        assertEquals(1, counter.get()) // Counter should only be incremented once
    }

    @Test
    fun `test concurrent getOrPutAsync`() = runBlocking {
        val counter = AtomicInteger(0)

        // Launch multiple coroutines trying to get the same key
        val deferreds = List(10) { i ->
            async {
                cache.getOrPutAsync("concurrent_key") {
                    delay(100) // Simulate expensive computation
                    counter.incrementAndGet()
                    "concurrent_value_${counter.get()}"
                }
            }
        }

        val results = deferreds.awaitAll()

        // All should return the same value (first one computed)
        val firstResult = results[0]
        results.forEach { result ->
            assertEquals(firstResult, result)
        }

        assertEquals(1, counter.get()) // Should only be computed once
        assertEquals(firstResult, cache["concurrent_key"])
    }

    @Test
    fun `test getOrPutAsync with exception handling`() = runBlocking {
        // Test with computation that throws exception
        val exception = assertThrows(RuntimeException::class.java) {
            runBlocking {
                cache.getOrPutAsync("error_key") {
                    throw RuntimeException("Computation failed")
                }
            }
        }

        assertEquals("Computation failed", exception.message)
        assertNull(cache["error_key"]) // Key should not be cached
    }

    @Test
    fun `test getOrPutAsync with null values`() = runBlocking {
        val nullableCache = createCache<String, String?> {
            maximumSize(100)
            recordStats(true)
        }

        val value1 = nullableCache.getOrPutAsync("null_key") { null }
        assertNull(value1)
        assertNull(nullableCache["null_key"])

        val value2 = nullableCache.getOrPutAsync("null_key") { "should_not_be_computed" }
        assertNull(value2) // Should return cached null value
    }

    @Test
    fun `test deferred operations with different scopes`() = runBlocking {
        val customScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        cache["key1"] = "value1"

        val deferred = cache.getDeferred("key1", customScope)
        val result = deferred.await()

        assertEquals("value1", result)

        customScope.cancel() // Clean up
    }

    @Test
    fun `test multiple deferred operations`() = runBlocking {
        val operations = mutableListOf<Deferred<*>>()

        // Add multiple operations
        repeat(10) { i ->
            operations.add(cache.putDeferred("key$i", "value$i", this))
        }

        // Wait for all operations to complete
        operations.awaitAll()

        // Verify all values were set
        repeat(10) { i ->
            assertEquals("value$i", cache["key$i"])
        }
    }

    @Test
    fun `test deferred operations with timeout`() = runBlocking {
        val deferred = cache.getDeferred("nonexistent", this)

        // Should complete immediately for non-existent key
        val result = withTimeout(1000) {
            deferred.await()
        }

        assertNull(result)
    }

    @Test
    fun `test getOrPutAsync with cancellation`() = runBlocking {
        val job = launch {
            cache.getOrPutAsync("cancelled_key") {
                delay(1000) // Long computation
                "should_not_be_computed"
            }
        }

        delay(10) // Let it start
        job.cancel() // Cancel the job

        // Should not throw exception
        assertNull(cache["cancelled_key"])
    }

    @Test
    fun `test getOrPutAsync with structured concurrency`() = runBlocking {
        val parentJob = Job()
        val parentScope = CoroutineScope(Dispatchers.IO + parentJob)

        val childJob = parentScope.launch {
            cache.getOrPutAsync("structured_key") {
                delay(50)
                "structured_value"
            }
        }

        childJob.join()
        assertEquals("structured_value", cache["structured_key"])

        parentJob.cancel() // Clean up
    }

    @Test
    fun `test performance of deferred operations`() = runBlocking {
        val startTime = System.currentTimeMillis()

        // Perform many deferred operations
        val deferreds = List(100) { i ->
            cache.putDeferred("perf_key$i", "perf_value$i", this)
        }

        deferreds.awaitAll()

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Should complete quickly (less than 1 second)
        assertTrue(duration < 1000)

        // Verify all values were set
        repeat(100) { i ->
            assertEquals("perf_value$i", cache["perf_key$i"])
        }
    }

    @Test
    fun `test getOrPutAsync with different dispatchers`() = runBlocking {
        val ioResult = withContext(Dispatchers.IO) {
            cache.getOrPutAsync("io_key") { "io_value" }
        }

        val defaultResult = withContext(Dispatchers.Default) {
            cache.getOrPutAsync("default_key") { "default_value" }
        }

        assertEquals("io_value", ioResult)
        assertEquals("default_value", defaultResult)
        assertEquals("io_value", cache["io_key"])
        assertEquals("default_value", cache["default_key"])
    }
}
