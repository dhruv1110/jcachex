package io.github.dhruv1110.jcachex.kotlin

import io.github.dhruv1110.jcachex.CacheStats

/**
 * Statistics extensions for cache operations.
 *
 * These extensions provide enhanced statistics formatting and utilities
 * for better monitoring and debugging of cache performance.
 */

/**
 * Constants for statistics formatting.
 */
private object StatisticsConstants {
    const val PERCENT_MULTIPLIER = 100.0
    const val NANOS_PER_MILLI = 1_000_000.0
    const val DECIMAL_PLACES = 2
}

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
