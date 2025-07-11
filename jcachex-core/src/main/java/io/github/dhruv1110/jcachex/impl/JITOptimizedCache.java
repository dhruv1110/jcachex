package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEntry;

/**
 * JIT-optimized cache implementation focused on hot path performance.
 *
 * Key optimizations:
 * - Method inlining hints via small methods
 * - Monomorphic call sites
 * - Branch prediction optimization
 * - Minimal object allocation in hot paths
 * - Cache-friendly data structures
 */
public final class JITOptimizedCache<K, V> extends OptimizedCacheBase<K, V> {

    // Hot path optimization - separate read/write paths
    private final ReadPath<K, V> readPath;
    private final WritePath<K, V> writePath;

    public JITOptimizedCache(CacheConfig<K, V> config) {
        super(config);

        // Initialize separate hot paths for JIT optimization
        this.readPath = new ReadPath<>(this);
        this.writePath = new WritePath<>(this);
    }

    /**
     * Override to use JIT-optimized read path.
     */
    @Override
    protected V doGet(K key) {
        // Delegate to JIT-optimized read path
        return readPath.get(key);
    }

    /**
     * Override to use JIT-optimized write path.
     */
    @Override
    protected void doPut(K key, V value) {
        // Delegate to JIT-optimized write path
        writePath.put(key, value);
    }

    /**
     * Override to use JIT-optimized remove path.
     */
    @Override
    protected V doRemove(K key) {
        return writePath.remove(key);
    }

    /**
     * JIT optimization-specific custom optimization.
     */
    @Override
    protected void performCustomOptimization() {
        super.performCustomOptimization();

        // JIT-specific optimizations
        if (getOperationState() == State.ACTIVE) {
            // Encourage JIT compilation by accessing hot paths
            long version = getVersion();
            if (version % 10000 == 0) {
                // Trigger JIT compilation hints every 10k operations
                triggerJITWarmup();
            }
        }
    }

    /**
     * Triggers JIT warmup by accessing common code paths.
     */
    private void triggerJITWarmup() {
        // Simple operations to encourage JIT compilation
        @SuppressWarnings("unused")
        long size = size();
        @SuppressWarnings("unused")
        boolean hasItems = size > 0;
    }

    /**
     * Specialized read path for JIT optimization.
     */
    private static final class ReadPath<K, V> {
        private final JITOptimizedCache<K, V> cache;

        ReadPath(JITOptimizedCache<K, V> cache) {
            this.cache = cache;
        }

        // Inline-friendly hot path
        final V get(K key) {
            if (key == null)
                return null;

            // Single hash lookup - monomorphic call site
            CacheEntry<V> entry = cache.data.get(key);

            if (entry == null) {
                // Branch prediction: misses are less common
                cache.recordGetStatistics(false);
                return null;
            }

            // Fast path for non-expired entries
            if (!cache.isEntryExpired(entry)) {
                // Update access information
                entry.incrementAccessCount();
                cache.evictionStrategy.update(key, entry);
                cache.recordGetStatistics(true);
                return entry.getValue();
            }

            // Slow path for expired entries
            return handleExpiredEntry(key, entry);
        }

        private final V handleExpiredEntry(K key, CacheEntry<V> entry) {
            // Remove expired entry
            cache.data.remove(key, entry);
            cache.currentSize.decrementAndGet();
            cache.recordGetStatistics(false);
            return null;
        }
    }

    /**
     * Specialized write path for JIT optimization.
     */
    private static final class WritePath<K, V> {
        private final JITOptimizedCache<K, V> cache;

        WritePath(JITOptimizedCache<K, V> cache) {
            this.cache = cache;
        }

        final void put(K key, V value) {
            if (key == null || value == null)
                return;

            CacheEntry<V> newEntry = cache.createCacheEntry(value);
            CacheEntry<V> existing = cache.data.put(key, newEntry);

            if (existing == null) {
                // New entry
                cache.currentSize.incrementAndGet();
            }

            // Update eviction strategy
            cache.evictionStrategy.update(key, newEntry);

            // Check if eviction needed
            cache.enforceSize();
        }

        final V remove(K key) {
            if (key == null)
                return null;

            CacheEntry<V> removed = cache.data.remove(key);
            if (removed != null) {
                cache.currentSize.decrementAndGet();
                cache.evictionStrategy.remove(key);
                return removed.getValue();
            }

            return null;
        }
    }

    /**
     * Returns JIT-specific performance metrics.
     */
    public String getJITMetrics() {
        return String.format("%s, ReadPath: %s, WritePath: %s",
                getPerformanceMetrics(),
                readPath.getClass().getSimpleName(),
                writePath.getClass().getSimpleName());
    }
}
