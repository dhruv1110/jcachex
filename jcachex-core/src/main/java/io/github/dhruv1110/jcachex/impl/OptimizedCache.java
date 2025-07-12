package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.impl.base.OptimizedCacheBase;

/**
 * High-performance cache implementation with Caffeine-inspired optimizations.
 * <p>
 * This implementation provides:
 * <ul>
 * <li><strong>Lock-Free Reads:</strong> Uses atomic operations and access
 * buffers</li>
 * <li><strong>Window TinyLFU:</strong> Advanced eviction with frequency-based
 * admission control</li>
 * <li><strong>Batched Operations:</strong> Reduces contention through batch
 * processing</li>
 * <li><strong>Adaptive Sizing:</strong> Automatically adjusts to workload
 * patterns</li>
 * </ul>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @since 1.0.0
 */
public class OptimizedCache<K, V> extends OptimizedCacheBase<K, V> {

    /**
     * Creates a new optimized cache with the specified configuration.
     *
     * @param config the cache configuration
     */
    public OptimizedCache(CacheConfig<K, V> config) {
        super(config);
    }

    /**
     * Performs custom optimization specific to OptimizedCache.
     */
    @Override
    protected void performCustomOptimization() {
        super.performCustomOptimization();

        // Additional optimization logic specific to OptimizedCache
        if (getOperationState() == State.ACTIVE) {
            // Adaptive frequency sketch resizing based on cache performance
            long currentVersion = getVersion();
            if (currentVersion % 5000 == 0) {
                // Every 5000 operations, analyze performance and adjust
                adaptFrequencySketchSize();
            }
        }
    }

    /**
     * Adapts the frequency sketch size based on current cache performance.
     */
    private void adaptFrequencySketchSize() {
        // Analyze hit ratio and adjust frequency sketch accordingly
        long totalHits = hitCount.get();
        long totalMisses = missCount.get();

        if (totalHits + totalMisses > 1000) {
            double hitRatio = (double) totalHits / (totalHits + totalMisses);

            // If hit ratio is low, we might need better frequency tracking
            if (hitRatio < 0.85) {
                // Reset frequency sketch to allow for better adaptation
                frequencySketch.clear();
            }
        }
    }

    /**
     * Returns detailed performance metrics for the optimized cache.
     */
    public String getDetailedMetrics() {
        return String.format("%s, HitRatio: %.2f%%, FreqSketchSize: %d",
                getPerformanceMetrics(),
                calculateHitRatio() * 100,
                FREQUENCY_SKETCH_SIZE);
    }

    /**
     * Calculates the current hit ratio.
     */
    private double calculateHitRatio() {
        long totalHits = hitCount.get();
        long totalMisses = missCount.get();
        long total = totalHits + totalMisses;

        return total > 0 ? (double) totalHits / total : 0.0;
    }
}
