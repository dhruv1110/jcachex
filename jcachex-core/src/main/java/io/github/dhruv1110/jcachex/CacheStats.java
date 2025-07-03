package io.github.dhruv1110.jcachex;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Objects;

/**
 * Statistics for cache performance monitoring and analysis.
 * <p>
 * This class provides comprehensive metrics about cache usage, including
 * hit/miss rates,
 * eviction counts, load times, and other performance indicators. All statistics
 * are
 * thread-safe and updated atomically during cache operations.
 * </p>
 *
 * <h3>Basic Usage Examples:</h3>
 *
 * <pre>{@code
 * // Enable statistics when creating cache
 * CacheConfig<String, String> config = CacheConfig.<String, String>builder()
 *         .maximumSize(1000L)
 *         .recordStats(true) // Enable statistics collection
 *         .build();
 * Cache<String, String> cache = new DefaultCache<>(config);
 *
 * // Perform some operations
 * cache.put("key1", "value1");
 * cache.get("key1"); // Hit
 * cache.get("key2"); // Miss
 *
 * // Analyze performance
 * CacheStats stats = cache.stats();
 * System.out.println("Hit rate: " + (stats.hitRate() * 100) + "%");
 * System.out.println("Total requests: " + (stats.hitCount() + stats.missCount()));
 * }</pre>
 *
 * <h3>Performance Monitoring Examples:</h3>
 *
 * <pre>{@code
 * // Periodic monitoring
 * ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
 * scheduler.scheduleAtFixedRate(() -> {
 *     CacheStats stats = cache.stats();
 *
 *     // Log key metrics
 *     logger.info("Cache Performance Report:");
 *     logger.info("  Hit Rate: {:.2f}%", stats.hitRate() * 100);
 *     logger.info("  Miss Rate: {:.2f}%", stats.missRate() * 100);
 *     logger.info("  Evictions: {}", stats.evictionCount());
 *     logger.info("  Average Load Time: {:.2f}ms", stats.averageLoadTime() / 1_000_000.0);
 *
 *     // Alert on poor performance
 *     if (stats.hitRate() < 0.8) {
 *         logger.warn("Low cache hit rate: {:.2f}%", stats.hitRate() * 100);
 *     }
 * }, 0, 5, TimeUnit.MINUTES);
 *
 * // Reset statistics for a fresh measurement period
 * stats.reset();
 * }</pre>
 *
 * <h3>Metrics Description:</h3>
 * <ul>
 * <li><strong>Hit Count:</strong> Number of successful cache lookups</li>
 * <li><strong>Miss Count:</strong> Number of cache lookups that didn't find a
 * value</li>
 * <li><strong>Hit Rate:</strong> Ratio of hits to total requests (hits +
 * misses)</li>
 * <li><strong>Miss Rate:</strong> Ratio of misses to total requests (hits +
 * misses)</li>
 * <li><strong>Eviction Count:</strong> Number of entries removed due to
 * size/weight limits</li>
 * <li><strong>Load Count:</strong> Number of times the cache loader was
 * invoked</li>
 * <li><strong>Load Failure Count:</strong> Number of failed load
 * operations</li>
 * <li><strong>Average Load Time:</strong> Average time in nanoseconds for load
 * operations</li>
 * </ul>
 *
 * @see Cache#stats()
 * @see CacheConfig.Builder#recordStats(boolean)
 * @since 1.0.0
 */
public class CacheStats {
    private final AtomicLong hitCount;
    private final AtomicLong missCount;
    private final AtomicLong evictionCount;
    private final AtomicLong loadCount;
    private final AtomicLong loadFailureCount;
    private final AtomicLong totalLoadTime;

    public AtomicLong getHitCount() {
        return hitCount;
    }

    public AtomicLong getMissCount() {
        return missCount;
    }

    public AtomicLong getEvictionCount() {
        return evictionCount;
    }

    public AtomicLong getLoadCount() {
        return loadCount;
    }

    public AtomicLong getLoadFailureCount() {
        return loadFailureCount;
    }

    public AtomicLong getTotalLoadTime() {
        return totalLoadTime;
    }

    public CacheStats() {
        this.hitCount = new AtomicLong(0);
        this.missCount = new AtomicLong(0);
        this.evictionCount = new AtomicLong(0);
        this.loadCount = new AtomicLong(0);
        this.loadFailureCount = new AtomicLong(0);
        this.totalLoadTime = new AtomicLong(0);
    }

    public CacheStats(AtomicLong hitCount, AtomicLong missCount, AtomicLong evictionCount,
            AtomicLong loadCount, AtomicLong loadFailureCount, AtomicLong totalLoadTime) {
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.evictionCount = evictionCount;
        this.loadCount = loadCount;
        this.loadFailureCount = loadFailureCount;
        this.totalLoadTime = totalLoadTime;
    }

    /**
     * Returns the number of successful cache lookups.
     * <p>
     * A hit occurs when a requested key is found in the cache and the cached
     * value is not expired.
     * </p>
     *
     * @return the total number of cache hits
     */
    public long hitCount() {
        return hitCount.get();
    }

    /**
     * Returns the number of cache lookups that didn't find a cached value.
     * <p>
     * A miss occurs when a requested key is not found in the cache or the
     * cached value has expired.
     * </p>
     *
     * @return the total number of cache misses
     */
    public long missCount() {
        return missCount.get();
    }

    /**
     * Returns the number of entries that have been evicted from the cache.
     * <p>
     * Evictions occur when the cache needs to remove entries to stay within
     * configured size or weight limits.
     * </p>
     *
     * @return the total number of evicted entries
     */
    public long evictionCount() {
        return evictionCount.get();
    }

    /**
     * Returns the number of times the cache loader was invoked.
     * <p>
     * This includes both successful and failed load attempts.
     * </p>
     *
     * @return the total number of load operations
     */
    public long loadCount() {
        return loadCount.get();
    }

    /**
     * Returns the number of failed cache load operations.
     * <p>
     * A load failure occurs when the cache loader throws an exception
     * or returns null for a requested key.
     * </p>
     *
     * @return the total number of failed load operations
     */
    public long loadFailureCount() {
        return loadFailureCount.get();
    }

    /**
     * Returns the total time spent loading values, in nanoseconds.
     * <p>
     * This represents the cumulative time spent in all cache loader invocations.
     * Use {@link #averageLoadTime()} for the average load time per operation.
     * </p>
     *
     * @return the total load time in nanoseconds
     */
    public long totalLoadTime() {
        return totalLoadTime.get();
    }

    /**
     * Returns the ratio of successful cache lookups to total requests.
     * <p>
     * The hit rate is calculated as: hits / (hits + misses).
     * A higher hit rate indicates better cache effectiveness.
     * </p>
     *
     * <h3>Example:</h3>
     *
     * <pre>{@code
     * CacheStats stats = cache.stats();
     * double hitRatePercent = stats.hitRate() * 100;
     * System.out.printf("Cache hit rate: %.2f%%\n", hitRatePercent);
     * }</pre>
     *
     * @return the hit rate as a value between 0.0 and 1.0
     */
    public double hitRate() {
        long total = hitCount.get() + missCount.get();
        return total == 0L ? 0.0 : (double) hitCount.get() / total;
    }

    /**
     * Returns the ratio of failed cache lookups to total requests.
     * <p>
     * The miss rate is calculated as: misses / (hits + misses).
     * This is equivalent to: 1.0 - hitRate().
     * </p>
     *
     * <h3>Example:</h3>
     *
     * <pre>{@code
     * CacheStats stats = cache.stats();
     * double missRatePercent = stats.missRate() * 100;
     * if (missRatePercent > 20.0) {
     *     System.out.println("High miss rate detected: " + missRatePercent + "%");
     * }
     * }</pre>
     *
     * @return the miss rate as a value between 0.0 and 1.0
     */
    public double missRate() {
        long total = hitCount.get() + missCount.get();
        return total == 0L ? 0.0 : (double) missCount.get() / total;
    }

    /**
     * Returns the average time spent loading a value, in nanoseconds.
     * <p>
     * This is calculated as: totalLoadTime / loadCount.
     * If no loads have occurred, returns 0.0.
     * </p>
     *
     * <h3>Example:</h3>
     *
     * <pre>{@code
     * CacheStats stats = cache.stats();
     * double avgLoadTimeMs = stats.averageLoadTime() / 1_000_000.0;
     * System.out.printf("Average load time: %.2f ms\n", avgLoadTimeMs);
     * }</pre>
     *
     * @return the average load time in nanoseconds
     */
    public double averageLoadTime() {
        long loads = loadCount.get();
        return loads == 0L ? 0.0 : (double) totalLoadTime.get() / loads;
    }

    public void recordHit() {
        hitCount.incrementAndGet();
    }

    public void recordMiss() {
        missCount.incrementAndGet();
    }

    public void recordEviction() {
        evictionCount.incrementAndGet();
    }

    public void recordLoad(long loadTime) {
        loadCount.incrementAndGet();
        totalLoadTime.addAndGet(loadTime);
    }

    public void recordLoadFailure() {
        loadFailureCount.incrementAndGet();
    }

    public CacheStats snapshot() {
        return new CacheStats(
                new AtomicLong(hitCount.get()),
                new AtomicLong(missCount.get()),
                new AtomicLong(evictionCount.get()),
                new AtomicLong(loadCount.get()),
                new AtomicLong(loadFailureCount.get()),
                new AtomicLong(totalLoadTime.get()));
    }

    public CacheStats reset() {
        hitCount.set(0);
        missCount.set(0);
        evictionCount.set(0);
        loadCount.set(0);
        loadFailureCount.set(0);
        totalLoadTime.set(0);
        return this;
    }

    public static CacheStats empty() {
        return new CacheStats();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CacheStats that = (CacheStats) o;
        return Objects.equals(hitCount.get(), that.hitCount.get()) &&
                Objects.equals(missCount.get(), that.missCount.get()) &&
                Objects.equals(evictionCount.get(), that.evictionCount.get()) &&
                Objects.equals(loadCount.get(), that.loadCount.get()) &&
                Objects.equals(loadFailureCount.get(), that.loadFailureCount.get()) &&
                Objects.equals(totalLoadTime.get(), that.totalLoadTime.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(hitCount.get(), missCount.get(), evictionCount.get(),
                loadCount.get(), loadFailureCount.get(), totalLoadTime.get());
    }

    @Override
    public String toString() {
        return "CacheStats{" +
                "hitCount=" + hitCount.get() +
                ", missCount=" + missCount.get() +
                ", evictionCount=" + evictionCount.get() +
                ", loadCount=" + loadCount.get() +
                ", loadFailureCount=" + loadFailureCount.get() +
                ", totalLoadTime=" + totalLoadTime.get() +
                '}';
    }
}
