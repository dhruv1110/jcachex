package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.Cache
import io.github.dhruv1110.jcachex.CacheStats

/*
 * Statistics extensions for cache operations.
 *
 * These extensions provide convenient methods for accessing and formatting
 * cache statistics and performance metrics.
 */

/**
 * Constants for statistics formatting.
 */

private object StatisticsConstants {
    const val PERCENT_MULTIPLIER = 100.0
    const val NANOS_PER_MILLI = 1_000_000.0
    const val DECIMAL_PLACES = 2
}

private const val PERCENT_MULTIPLIER = 100.0
private const val NANOSECONDS_TO_MILLISECONDS = 1_000_000.0

/**
 * Returns the hit rate as a percentage.
 */

fun CacheStats.hitRatePercent(): Double = hitRate() * StatisticsConstants.PERCENT_MULTIPLIER

/**
 * Returns the miss rate as a percentage.
 */

fun CacheStats.missRatePercent(): Double = missRate() * StatisticsConstants.PERCENT_MULTIPLIER

/**
 * Returns the average load time in milliseconds.
 */

fun CacheStats.averageLoadTimeMillis(): Double = averageLoadTime() / StatisticsConstants.NANOS_PER_MILLI

/**
 * Returns a formatted string representation of the cache statistics.
 */

fun CacheStats.formatted(): String {
    val formatString = "%.${StatisticsConstants.DECIMAL_PLACES}f"
    return """
        Cache Statistics:
        - Hit Rate: ${formatString.format(hitRatePercent())}%
        - Miss Rate: ${formatString.format(missRatePercent())}%
        - Hits: ${hitCount()}
        - Misses: ${missCount()}
        - Evictions: ${evictionCount()}
        - Loads: ${loadCount()}
        - Load Failures: ${loadFailureCount()}
        - Average Load Time: ${formatString.format(averageLoadTimeMillis())}ms
        """.trimIndent()
}

/**
 * Returns a formatted string with cache statistics.
 */
fun <K, V> Cache<K, V>.statsString(): String {
    val stats = stats()
    return buildString {
        appendLine("Cache Statistics:")
        appendLine("  Hit Rate: %.2f%%".format(stats.hitRate() * PERCENT_MULTIPLIER))
        appendLine("  Miss Rate: %.2f%%".format(stats.missRate() * PERCENT_MULTIPLIER))
        appendLine("  Hit Count: ${stats.hitCount()}")
        appendLine("  Miss Count: ${stats.missCount()}")
        appendLine("  Load Count: ${stats.loadCount()}")
        appendLine("  Eviction Count: ${stats.evictionCount()}")
        appendLine("  Average Load Time: %.2f ms".format(stats.averageLoadTime() / NANOSECONDS_TO_MILLISECONDS))
    }
}
