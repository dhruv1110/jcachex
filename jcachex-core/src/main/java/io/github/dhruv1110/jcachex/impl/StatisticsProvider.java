package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheStats;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for common statistics handling patterns.
 *
 * This class provides static methods for creating and managing cache
 * statistics,
 * eliminating duplication across cache implementations.
 */
public final class StatisticsProvider {

    private StatisticsProvider() {
        // Utility class - no instances
    }

    /**
     * Creates a basic CacheStats object from hit and miss counters.
     * This pattern is used by many cache implementations.
     *
     * @param hitCount  the hit counter
     * @param missCount the miss counter
     * @return a populated CacheStats object
     */
    public static CacheStats createBasicStats(AtomicLong hitCount, AtomicLong missCount) {
        CacheStats stats = new CacheStats();
        stats.getHitCount().set(hitCount.get());
        stats.getMissCount().set(missCount.get());
        return stats;
    }

    /**
     * Creates a comprehensive CacheStats object with all counters.
     *
     * @param hitCount      the hit counter
     * @param missCount     the miss counter
     * @param loadCount     the load counter
     * @param loadTime      the total load time in nanoseconds
     * @param evictionCount the eviction counter
     * @return a fully populated CacheStats object
     */
    public static CacheStats createComprehensiveStats(
            AtomicLong hitCount,
            AtomicLong missCount,
            AtomicLong loadCount,
            AtomicLong loadTime,
            AtomicLong evictionCount) {
        CacheStats stats = new CacheStats();
        stats.getHitCount().set(hitCount.get());
        stats.getMissCount().set(missCount.get());
        stats.getLoadCount().set(loadCount.get());
        stats.getTotalLoadTime().set(loadTime.get());
        stats.getEvictionCount().set(evictionCount.get());
        return stats;
    }

    /**
     * Calculates the hit ratio from hit and miss counts.
     *
     * @param hitCount  the number of hits
     * @param missCount the number of misses
     * @return the hit ratio between 0.0 and 1.0
     */
    public static double calculateHitRatio(long hitCount, long missCount) {
        long total = hitCount + missCount;
        return total > 0 ? (double) hitCount / total : 0.0;
    }

    /**
     * Calculates the miss ratio from hit and miss counts.
     *
     * @param hitCount  the number of hits
     * @param missCount the number of misses
     * @return the miss ratio between 0.0 and 1.0
     */
    public static double calculateMissRatio(long hitCount, long missCount) {
        long total = hitCount + missCount;
        return total > 0 ? (double) missCount / total : 0.0;
    }

    /**
     * Calculates the average load time.
     *
     * @param totalLoadTime the total load time in nanoseconds
     * @param loadCount     the number of loads
     * @return the average load time in nanoseconds, or 0.0 if no loads
     */
    public static double calculateAverageLoadTime(long totalLoadTime, long loadCount) {
        return loadCount > 0 ? (double) totalLoadTime / loadCount : 0.0;
    }

    /**
     * Records a cache hit in a thread-safe manner.
     *
     * @param hitCount     the hit counter to increment
     * @param statsEnabled whether statistics recording is enabled
     */
    public static void recordHit(AtomicLong hitCount, boolean statsEnabled) {
        if (statsEnabled) {
            hitCount.incrementAndGet();
        }
    }

    /**
     * Records a cache miss in a thread-safe manner.
     *
     * @param missCount    the miss counter to increment
     * @param statsEnabled whether statistics recording is enabled
     */
    public static void recordMiss(AtomicLong missCount, boolean statsEnabled) {
        if (statsEnabled) {
            missCount.incrementAndGet();
        }
    }

    /**
     * Records a cache load operation.
     *
     * @param loadCount          the load counter to increment
     * @param loadTime           the total load time counter to update
     * @param operationTimeNanos the time this operation took in nanoseconds
     * @param statsEnabled       whether statistics recording is enabled
     */
    public static void recordLoad(AtomicLong loadCount, AtomicLong loadTime,
            long operationTimeNanos, boolean statsEnabled) {
        if (statsEnabled) {
            loadCount.incrementAndGet();
            loadTime.addAndGet(operationTimeNanos);
        }
    }

    /**
     * Records a cache eviction.
     *
     * @param evictionCount the eviction counter to increment
     * @param statsEnabled  whether statistics recording is enabled
     */
    public static void recordEviction(AtomicLong evictionCount, boolean statsEnabled) {
        if (statsEnabled) {
            evictionCount.incrementAndGet();
        }
    }

    /**
     * Resets all statistics counters to zero.
     *
     * @param counters the counters to reset
     */
    public static void resetCounters(AtomicLong... counters) {
        for (AtomicLong counter : counters) {
            counter.set(0);
        }
    }

    /**
     * Creates a formatted statistics summary string.
     *
     * @param hitCount      the number of hits
     * @param missCount     the number of misses
     * @param loadCount     the number of loads
     * @param evictionCount the number of evictions
     * @return a formatted statistics summary
     */
    public static String formatStatsSummary(long hitCount, long missCount,
            long loadCount, long evictionCount) {
        long totalRequests = hitCount + missCount;
        double hitRatio = calculateHitRatio(hitCount, missCount);

        return String.format(
                "Cache Stats: requests=%d, hits=%d (%.1f%%), misses=%d, loads=%d, evictions=%d",
                totalRequests, hitCount, hitRatio * 100, missCount, loadCount, evictionCount);
    }

    /**
     * Creates a detailed performance metrics string.
     *
     * @param hitCount      the number of hits
     * @param missCount     the number of misses
     * @param loadCount     the number of loads
     * @param totalLoadTime the total load time in nanoseconds
     * @param evictionCount the number of evictions
     * @param cacheSize     the current cache size
     * @return a detailed performance summary
     */
    public static String formatDetailedMetrics(long hitCount, long missCount,
            long loadCount, long totalLoadTime,
            long evictionCount, long cacheSize) {
        double hitRatio = calculateHitRatio(hitCount, missCount);
        double avgLoadTime = calculateAverageLoadTime(totalLoadTime, loadCount);

        return String.format(
                "Cache Metrics: size=%d, hitRatio=%.3f, avgLoadTime=%.2fms, " +
                        "hits=%d, misses=%d, loads=%d, evictions=%d",
                cacheSize, hitRatio, avgLoadTime / 1_000_000.0,
                hitCount, missCount, loadCount, evictionCount);
    }
}
