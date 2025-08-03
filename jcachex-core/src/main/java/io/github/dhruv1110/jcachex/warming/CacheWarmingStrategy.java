package io.github.dhruv1110.jcachex.warming;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

/**
 * Interface for cache warming strategies.
 * <p>
 * Cache warming is a critical production feature that preloads frequently
 * accessed
 * data into cache memory before it's needed, reducing cold start latency and
 * improving application performance.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Predictive Loading:</strong> Uses access patterns to predict what
 * to load</li>
 * <li><strong>Scheduled Warming:</strong> Automatic periodic cache warming</li>
 * <li><strong>Priority-based:</strong> Warm most important data first</li>
 * <li><strong>Async Operations:</strong> Non-blocking warming process</li>
 * <li><strong>Failure Resilience:</strong> Graceful handling of warming
 * failures</li>
 * </ul>
 *
 * <h3>Implementation Examples:</h3>
 *
 * <pre>{@code
 * // Pattern-based warming
 * CacheWarmingStrategy<String, User> patternWarming = new PatternBasedWarmingStrategy<>(
 *         accessPatterns -> {
 *             // Predict next likely accessed keys based on patterns
 *             return Stream.of("user1", "user2", "user3")
 *                     .collect(Collectors.toMap(
 *                             Function.identity(),
 *                             k -> WarmingPriority.HIGH));
 *         });
 *
 * // Scheduled warming
 * CacheWarmingStrategy<String, Product> scheduledWarming = new ScheduledWarmingStrategy<>(
 *         Duration.ofMinutes(30), // Warm every 30 minutes
 *         () -> productService.getTopProducts(100));
 *
 * // Time-based warming (e.g., warm different data at different times)
 * Map<LocalTime, Supplier<List<Report>>> warmingSchedule = new HashMap<>();
 * warmingSchedule.put(LocalTime.of(9, 0), () -> reportService.getMorningReports());
 * warmingSchedule.put(LocalTime.of(17, 0), () -> reportService.getEveningReports());
 * CacheWarmingStrategy<String, Report> timeBasedWarming = new TimeBasedWarmingStrategy<>(
 *         warmingSchedule);
 * }</pre>
 *
 * @param <K> the type of cache keys
 * @param <V> the type of cache values
 * @since 1.0.0
 */
public interface CacheWarmingStrategy<K, V> {

    /**
     * Initiates cache warming process.
     *
     * @param cache  the cache to warm
     * @param loader function to load data for warming
     * @return CompletableFuture that completes when warming is done
     */
    CompletableFuture<WarmingResult> warmCache(
            WarmingContext<K, V> cache,
            Function<K, V> loader);

    /**
     * Starts scheduled warming if supported by the strategy.
     *
     * @param cache    the cache to warm
     * @param loader   function to load data
     * @param executor executor service for scheduling
     * @return CompletableFuture for the warming process
     */
    default CompletableFuture<Void> startScheduledWarming(
            WarmingContext<K, V> cache,
            Function<K, V> loader,
            ScheduledExecutorService executor) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Stops any ongoing warming process.
     */
    default void stopWarming() {
        // Default implementation does nothing
    }

    /**
     * Returns the priority for warming a specific key.
     *
     * @param key the key to check
     * @return warming priority for the key
     */
    default WarmingPriority getWarmingPriority(K key) {
        return WarmingPriority.MEDIUM;
    }

    /**
     * Indicates if this strategy supports continuous warming.
     *
     * @return true if continuous warming is supported
     */
    default boolean supportsContinuousWarming() {
        return false;
    }

    /**
     * Warming priority levels.
     */
    enum WarmingPriority {
        LOW(1),
        MEDIUM(2),
        HIGH(3),
        CRITICAL(4);

        private final int level;

        WarmingPriority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}
