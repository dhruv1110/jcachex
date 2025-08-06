@file:Suppress("WildcardImport", "ktlint:standard:no-wildcard-imports")

package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.CacheStats
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.atomic.AtomicLong

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatisticsExtensionsTest {
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
    fun `test hitRatePercent`() {
        // Create stats with known values
        val stats = createTestStats(hitCount = 80, missCount = 20)

        val hitRatePercent = stats.hitRatePercent()

        assertEquals(80.0, hitRatePercent, 0.01) // 80 hits out of 100 total = 80%
    }

    @Test
    fun `test missRatePercent`() {
        val stats = createTestStats(hitCount = 75, missCount = 25)

        val missRatePercent = stats.missRatePercent()

        assertEquals(25.0, missRatePercent, 0.01) // 25 misses out of 100 total = 25%
    }

    @Test
    fun `test averageLoadTimeMillis`() {
        val stats =
            createTestStats(
                hitCount = 50,
                missCount = 10,
                loadCount = 10,
                // 1 second in nanoseconds
                totalLoadTime = 1_000_000_000L,
            )

        val averageLoadTimeMillis = stats.averageLoadTimeMillis()

        // 1 second / 10 loads = 100ms per load
        assertEquals(100.0, averageLoadTimeMillis, 0.01)
    }

    @Test
    fun `test formatted statistics`() {
        val stats =
            createTestStats(
                hitCount = 80,
                missCount = 20,
                evictionCount = 5,
                loadCount = 10,
                loadFailureCount = 2,
                // 500ms in nanoseconds
                totalLoadTime = 500_000_000L,
            )

        val formatted = stats.formatted()

        assertTrue(formatted.contains("Cache Statistics:"))
        assertTrue(formatted.contains("Hit Rate: 80.00%"))
        assertTrue(formatted.contains("Miss Rate: 20.00%"))
        assertTrue(formatted.contains("Hits: 80"))
        assertTrue(formatted.contains("Misses: 20"))
        assertTrue(formatted.contains("Evictions: 5"))
        assertTrue(formatted.contains("Loads: 10"))
        assertTrue(formatted.contains("Load Failures: 2"))
        assertTrue(formatted.contains("Average Load Time: 50.00ms"))
    }

    @Test
    fun `test statsString`() {
        // Populate cache to generate some stats
        cache["key1"] = "value1"
        cache["key2"] = "value2"
        cache.get("key1") // Hit
        cache.get("key2") // Hit
        cache.get("nonexistent") // Miss

        val statsString = cache.statsString()

        assertTrue(statsString.contains("Cache Statistics:"))
        assertTrue(statsString.contains("Hit Rate:"))
        assertTrue(statsString.contains("Miss Rate:"))
        assertTrue(statsString.contains("Hit Count:"))
        assertTrue(statsString.contains("Miss Count:"))
        assertTrue(statsString.contains("Load Count:"))
        assertTrue(statsString.contains("Eviction Count:"))
        assertTrue(statsString.contains("Average Load Time:"))
    }

    @Test
    fun `test statistics with zero values`() {
        val stats = createTestStats(hitCount = 0, missCount = 0)

        val hitRatePercent = stats.hitRatePercent()
        val missRatePercent = stats.missRatePercent()
        val averageLoadTimeMillis = stats.averageLoadTimeMillis()

        assertEquals(0.0, hitRatePercent, 0.01)
        assertEquals(0.0, missRatePercent, 0.01)
        assertEquals(0.0, averageLoadTimeMillis, 0.01)
    }

    @Test
    fun `test statistics with high hit rate`() {
        val stats = createTestStats(hitCount = 95, missCount = 5)

        val hitRatePercent = stats.hitRatePercent()
        val missRatePercent = stats.missRatePercent()

        assertEquals(95.0, hitRatePercent, 0.01)
        assertEquals(5.0, missRatePercent, 0.01)
    }

    @Test
    fun `test statistics with high miss rate`() {
        val stats = createTestStats(hitCount = 10, missCount = 90)

        val hitRatePercent = stats.hitRatePercent()
        val missRatePercent = stats.missRatePercent()

        assertEquals(10.0, hitRatePercent, 0.01)
        assertEquals(90.0, missRatePercent, 0.01)
    }

    @Test
    fun `test averageLoadTimeMillis with zero loads`() {
        val stats = createTestStats(hitCount = 100, missCount = 0, loadCount = 0)

        val averageLoadTimeMillis = stats.averageLoadTimeMillis()

        assertEquals(0.0, averageLoadTimeMillis, 0.01)
    }

    @Test
    fun `test averageLoadTimeMillis with high load time`() {
        val stats =
            createTestStats(
                hitCount = 50,
                missCount = 10,
                loadCount = 5,
                // 5 seconds in nanoseconds
                totalLoadTime = 5_000_000_000L,
            )

        val averageLoadTimeMillis = stats.averageLoadTimeMillis()

        assertEquals(1000.0, averageLoadTimeMillis, 0.01) // 5 seconds / 5 loads = 1 second per load
    }

    @Test
    fun `test formatted statistics with zero values`() {
        val stats = createTestStats(hitCount = 0, missCount = 0)

        val formatted = stats.formatted()

        assertTrue(formatted.contains("Hit Rate: 0.00%"))
        assertTrue(formatted.contains("Miss Rate: 0.00%"))
        assertTrue(formatted.contains("Hits: 0"))
        assertTrue(formatted.contains("Misses: 0"))
    }

    @Test
    fun `test statsString with empty cache`() {
        val statsString = cache.statsString()

        assertTrue(statsString.contains("Cache Statistics:"))
        assertTrue(statsString.contains("Hit Rate: 0.00%"))
        assertTrue(statsString.contains("Miss Rate: 0.00%"))
        assertTrue(statsString.contains("Hit Count: 0"))
        assertTrue(statsString.contains("Miss Count: 0"))
    }

    @Test
    fun `test statsString with populated cache`() {
        // Add some data and perform operations
        cache["key1"] = "value1"
        cache["key2"] = "value2"
        cache["key3"] = "value3"

        // Perform some gets to generate hits
        cache.get("key1")
        cache.get("key2")
        cache.get("key3")

        // Perform some misses
        cache.get("nonexistent1")
        cache.get("nonexistent2")

        val statsString = cache.statsString()

        assertTrue(statsString.contains("Cache Statistics:"))
        assertTrue(statsString.contains("Hit Count: 3"))
        assertTrue(statsString.contains("Miss Count: 2"))

        // Hit rate should be 3/(3+2) = 60%
        assertTrue(statsString.contains("Hit Rate: 60.00%"))
        assertTrue(statsString.contains("Miss Rate: 40.00%"))
    }

    @Test
    fun `test statistics precision`() {
        val stats = createTestStats(hitCount = 1, missCount = 3) // 25% hit rate

        val hitRatePercent = stats.hitRatePercent()
        val missRatePercent = stats.missRatePercent()

        assertEquals(25.0, hitRatePercent, 0.01)
        assertEquals(75.0, missRatePercent, 0.01)
    }

    @Test
    fun `test statistics with large numbers`() {
        val stats = createTestStats(hitCount = 1000000, missCount = 100000)

        val hitRatePercent = stats.hitRatePercent()
        val missRatePercent = stats.missRatePercent()

        assertEquals(90.9090909090909, hitRatePercent, 0.01)
        assertEquals(9.09090909090909, missRatePercent, 0.01)
    }

    @Test
    fun `test formatted statistics with all fields`() {
        val stats =
            createTestStats(
                hitCount = 100,
                missCount = 50,
                evictionCount = 10,
                loadCount = 20,
                loadFailureCount = 5,
                // 2 seconds in nanoseconds
                totalLoadTime = 2_000_000_000L,
            )

        val formatted = stats.formatted()

        // Check all fields are present
        assertTrue(formatted.contains("Hit Rate: 66.67%"))
        assertTrue(formatted.contains("Miss Rate: 33.33%"))
        assertTrue(formatted.contains("Hits: 100"))
        assertTrue(formatted.contains("Misses: 50"))
        assertTrue(formatted.contains("Evictions: 10"))
        assertTrue(formatted.contains("Loads: 20"))
        assertTrue(formatted.contains("Load Failures: 5"))
        assertTrue(formatted.contains("Average Load Time: 100.00ms"))
    }

    @Test
    fun `test statistics with evictions`() {
        val stats =
            createTestStats(
                hitCount = 80,
                missCount = 20,
                evictionCount = 15,
            )

        val formatted = stats.formatted()

        assertTrue(formatted.contains("Evictions: 15"))
    }

    @Test
    fun `test statistics with load failures`() {
        val stats =
            createTestStats(
                hitCount = 90,
                missCount = 10,
                loadFailureCount = 3,
            )

        val formatted = stats.formatted()

        assertTrue(formatted.contains("Load Failures: 3"))
    }

    @Test
    fun `test statistics with loads`() {
        val stats =
            createTestStats(
                hitCount = 70,
                missCount = 30,
                loadCount = 25,
            )

        val formatted = stats.formatted()

        assertTrue(formatted.contains("Loads: 25"))
    }

    @Test
    fun `test statistics formatting consistency`() {
        val stats1 = createTestStats(hitCount = 50, missCount = 50)
        val stats2 = createTestStats(hitCount = 50, missCount = 50)

        val formatted1 = stats1.formatted()
        val formatted2 = stats2.formatted()

        assertEquals(formatted1, formatted2)
    }

    @Test
    fun `test statsString consistency`() {
        cache["key1"] = "value1"
        cache.get("key1")
        cache.get("nonexistent")

        val statsString1 = cache.statsString()
        val statsString2 = cache.statsString()

        assertEquals(statsString1, statsString2)
    }

    @Test
    fun `test statistics with very small load times`() {
        val stats =
            createTestStats(
                hitCount = 100,
                missCount = 10,
                loadCount = 5,
                // 1 microsecond in nanoseconds
                totalLoadTime = 1000L,
            )

        val averageLoadTimeMillis = stats.averageLoadTimeMillis()

        assertEquals(0.0002, averageLoadTimeMillis, 0.0001) // 1000ns / 5 loads = 200ns = 0.0002ms
    }

    @Test
    fun `test statistics with very large load times`() {
        val stats =
            createTestStats(
                hitCount = 100,
                missCount = 10,
                loadCount = 2,
                // 10 seconds in nanoseconds
                totalLoadTime = 10_000_000_000L,
            )

        val averageLoadTimeMillis = stats.averageLoadTimeMillis()

        assertEquals(5000.0, averageLoadTimeMillis, 0.01) // 10 seconds / 2 loads = 5 seconds per load
    }

    /**
     * Helper function to create test CacheStats with specific values
     */
    @Suppress("LongParameterList")
    private fun createTestStats(
        hitCount: Long = 0,
        missCount: Long = 0,
        evictionCount: Long = 0,
        loadCount: Long = 0,
        loadFailureCount: Long = 0,
        totalLoadTime: Long = 0,
    ): CacheStats {
        return CacheStats(
            AtomicLong(hitCount),
            AtomicLong(missCount),
            AtomicLong(evictionCount),
            AtomicLong(loadCount),
            AtomicLong(loadFailureCount),
            AtomicLong(totalLoadTime),
        )
    }
}
