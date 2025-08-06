package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.JCacheXBuilder
import io.github.dhruv1110.jcachex.profiles.ProfileName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DSLExtensionsTest {

    @Test
    fun `test cacheConfig function`() {
        val config = cacheConfig<String, String> {
            maximumSize(100)
            recordStats(true)
            expireAfterWrite(Duration.ofMinutes(5))
        }

        assertNotNull(config)
        assertEquals(100, config.maximumSize)
    }


    @Test
    fun `test createCacheWithProfile with ProfileName`() {
        val cache = createCacheWithProfile<String, String>(ProfileName.DEFAULT) {
            maximumSize(150)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createCache basic`() {
        val cache = createCache<String, String>()

        assertNotNull(cache)
        assertEquals(0, cache.size())
        assertTrue(cache.isEmpty())
    }

    @Test
    fun `test createCache with configuration`() {
        val cache = createCache<String, String> {
            maximumSize(100)
            recordStats(true)
            expireAfterWrite(Duration.ofMinutes(10))
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createSmartCache`() {
        val cache = createSmartCache<String, String> {
            maximumSize(200)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createReadHeavyCache`() {
        val cache = createReadHeavyCache<String, String> {
            maximumSize(300)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createWriteHeavyCache`() {
        val cache = createWriteHeavyCache<String, String> {
            maximumSize(250)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createMemoryEfficientCache`() {
        val cache = createMemoryEfficientCache<String, String> {
            maximumSize(150)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createHighPerformanceCache`() {
        val cache = createHighPerformanceCache<String, String> {
            maximumSize(500)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createSessionCache`() {
        val cache = createSessionCache<String, String> {
            maximumSize(1000)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createApiCache`() {
        val cache = createApiCache<String, String> {
            maximumSize(2000)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createComputeCache`() {
        val cache = createComputeCache<String, String> {
            maximumSize(800)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createMachineLearningCache`() {
        val cache = createMachineLearningCache<String, String> {
            maximumSize(1500)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createUltraLowLatencyCache`() {
        val cache = createUltraLowLatencyCache<String, String> {
            maximumSize(100)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createHardwareOptimizedCache`() {
        val cache = createHardwareOptimizedCache<String, String> {
            maximumSize(300)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createCache with basic configuration options`() {
        val cache = createCache<String, String> {
            maximumWeight(400)
            expireAfterWrite(Duration.ofHours(1))
            expireAfterAccess(Duration.ofMinutes(30))
            refreshAfterWrite(Duration.ofMinutes(15))
            loader { key -> "loaded_$key" }
            weigher { key, value -> key.length.toLong() + value.length.toLong() }
            recordStats(true)
            listener(object : io.github.dhruv1110.jcachex.CacheEventListener<String, String> {
                override fun onPut(key: String, value: String) {
                    // Test listener
                }

                override fun onRemove(key: String, value: String) {
                    // Test listener
                }

                override fun onEvict(key: String, value: String, reason: io.github.dhruv1110.jcachex.EvictionReason) {
                    // Test listener
                }

                override fun onExpire(key: String, value: String) {
                    // Test listener
                }

                override fun onLoad(key: String, value: String) {
                    // Test listener
                }

                override fun onLoadError(key: String, error: Throwable) {
                    // Test listener
                }

                override fun onClear() {
                    // Test listener
                }
            })
        }
        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createCache with different types`() {
        val stringCache = createCache<String, String> {
            maximumSize(100)
            recordStats(true)
        }

        val intCache = createCache<Int, String> {
            maximumSize(200)
            recordStats(true)
        }

        val userCache = createCache<String, User> {
            maximumSize(150)
            recordStats(true)
        }

        assertNotNull(stringCache)
        assertNotNull(intCache)
        assertNotNull(userCache)

        assertEquals(0, stringCache.size())
        assertEquals(0, intCache.size())
        assertEquals(0, userCache.size())
    }

    @Test
    fun `test createCache with complex objects`() {
        data class ComplexObject(val id: Int, val name: String, val data: Map<String, Any>)

        val cache = createCache<String, ComplexObject> {
            maximumSize(100)
            recordStats(true)
            weigher { key, value -> key.length.toLong() + value.name.length.toLong() }
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createCache with nullable types`() {
        val cache = createCache<String, String?> {
            maximumSize(100)
            recordStats(true)
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createCache with primitive types`() {
        val intCache = createCache<Int, Int> {
            maximumSize(100)
            recordStats(true)
        }

        val longCache = createCache<Long, String> {
            maximumSize(100)
            recordStats(true)
        }

        val doubleCache = createCache<Double, Boolean> {
            maximumSize(100)
            recordStats(true)
        }

        assertNotNull(intCache)
        assertNotNull(longCache)
        assertNotNull(doubleCache)
    }

    @Test
    fun `test createCache with collections`() {
        val listCache = createCache<String, List<String>> {
            maximumSize(100)
            recordStats(true)
        }

        val mapCache = createCache<String, Map<String, Int>> {
            maximumSize(100)
            recordStats(true)
        }

        val setCache = createCache<String, Set<String>> {
            maximumSize(100)
            recordStats(true)
        }

        assertNotNull(listCache)
        assertNotNull(mapCache)
        assertNotNull(setCache)
    }

    @Test
    fun `test createCache with custom loader`() {
        val cache = createCache<String, String> {
            maximumSize(100)
            recordStats(true)
            loader { key -> "computed_$key" }
        }

        assertNotNull(cache)

        // Test that loader works
        val value = cache.get("test_key")
        assertEquals("computed_test_key", value)
    }

    @Test
    fun `test createCache with async loader`() {
        val cache = createCache<String, String> {
            maximumSize(100)
            recordStats(true)
            asyncLoader { key -> java.util.concurrent.CompletableFuture.completedFuture("async_$key") }
        }

        assertNotNull(cache)

        // Test that async loader works
        val future = cache.getAsync("test_key")
        val value = future.get()
        assertEquals("async_test_key", value)
    }

    @Test
    fun `test createCache with weigher`() {
        val cache = createCache<String, String> {
            maximumWeight(1000)
            recordStats(true)
            weigher { key, value -> key.length.toLong() + value.length.toLong() }
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createCache with expiration`() {
        val cache = createCache<String, String> {
            maximumSize(100)
            recordStats(true)
            expireAfterWrite(Duration.ofMinutes(5))
            expireAfterAccess(Duration.ofMinutes(2))
            refreshAfterWrite(Duration.ofMinutes(1))
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createCache with listener`() {
        var evictionCount = 0

        val cache = createCache<String, String> {
            maximumSize(2) // Small size to trigger eviction
            recordStats(true)
            listener(object : io.github.dhruv1110.jcachex.CacheEventListener<String, String> {
                override fun onPut(key: String, value: String) {
                    // Test listener
                }

                override fun onRemove(key: String, value: String) {
                    // Test listener
                }

                override fun onEvict(key: String, value: String, reason: io.github.dhruv1110.jcachex.EvictionReason) {
                    evictionCount++
                }

                override fun onExpire(key: String, value: String) {
                    // Test listener
                }

                override fun onLoad(key: String, value: String) {
                    // Test listener
                }

                override fun onLoadError(key: String, error: Throwable) {
                    // Test listener
                }

                override fun onClear() {
                    // Test listener
                }
            })
        }

        assertNotNull(cache)

        // Add entries to trigger eviction
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3") // This should trigger eviction

        assertTrue(evictionCount > 0)
    }

    @Test
    fun `test legacy createUnifiedCache`() {
        val cache = createCache(fun JCacheXBuilder<String, String>.() {
            maximumSize(100)
            recordStats(true)
        })

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    @Test
    fun `test createCache with different profiles`() {
        val profiles = listOf(
            ProfileName.DEFAULT,
            ProfileName.READ_HEAVY,
            ProfileName.WRITE_HEAVY,
            ProfileName.MEMORY_EFFICIENT,
            ProfileName.HIGH_PERFORMANCE,
            ProfileName.API_CACHE,
            ProfileName.COMPUTE_CACHE,
            ProfileName.HARDWARE_OPTIMIZED,
            ProfileName.SESSION_CACHE,
            ProfileName.ZERO_COPY
        )

        profiles.forEach { profile ->
            val cache = createCacheWithProfile<String, String>(profile) {
                maximumSize(100)
                recordStats(true)
            }

            assertNotNull(cache)
            assertEquals(0, cache.size())
        }
    }

    @Test
    fun `test createCache with complex configuration chain`() {
        val cache = createCache<String, ComplexObject> {
            maximumWeight(1000)
            expireAfterWrite(Duration.ofHours(2))
            expireAfterAccess(Duration.ofMinutes(30))
            refreshAfterWrite(Duration.ofMinutes(15))
            loader { key -> ComplexObject(key.hashCode(), "loaded_$key", mapOf("key" to "value")) }
            weigher { key, value -> key.length.toLong() + value.name.length.toLong() }
            recordStats(true)
            listener(object : io.github.dhruv1110.jcachex.CacheEventListener<String, ComplexObject> {
                override fun onPut(key: String, value: ComplexObject) {
                    // Test listener
                }

                override fun onRemove(key: String, value: ComplexObject) {
                    // Test listener
                }

                override fun onEvict(
                    key: String,
                    value: ComplexObject,
                    reason: io.github.dhruv1110.jcachex.EvictionReason
                ) {
                    // Test listener
                }

                override fun onExpire(key: String, value: ComplexObject) {
                    // Test listener
                }

                override fun onLoad(key: String, value: ComplexObject) {
                    // Test listener
                }

                override fun onLoadError(key: String, error: Throwable) {
                    // Test listener
                }

                override fun onClear() {
                    // Test listener
                }
            })
        }

        assertNotNull(cache)
        assertEquals(0, cache.size())
    }

    // Helper data class for testing
    data class User(val id: Int, val name: String, val email: String)
    data class ComplexObject(val id: Int, val name: String, val data: Map<String, Any>)
}
