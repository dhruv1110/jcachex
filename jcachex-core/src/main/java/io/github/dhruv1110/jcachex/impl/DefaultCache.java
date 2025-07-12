package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEventListener;
import io.github.dhruv1110.jcachex.impl.base.ConcurrentCacheBase;

import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the Cache interface.
 * <p>
 * This implementation is thread-safe and supports various cache configurations
 * including eviction strategies, expiration policies, and event listeners.
 * It provides both synchronous and asynchronous operations for maximum
 * flexibility.
 * </p>
 *
 * <p>
 * <strong>Performance Optimizations:</strong>
 * This implementation uses striped locking to reduce contention, optimized
 * expiration checks using nanoTime, and minimizes object allocation in hot
 * paths.
 * </p>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @see CacheConfig
 * @see CacheEventListener
 * @since 1.0.0
 */
public class DefaultCache<K, V> extends ConcurrentCacheBase<K, V> implements AutoCloseable {
    private static final long REFRESH_INTERVAL_SECONDS = 1L;

    /**
     * Creates a new DefaultCache with the specified configuration.
     */
    public DefaultCache(CacheConfig<K, V> config) {
        super(config);

        if (config.getRefreshAfterWrite() != null) {
            scheduleRefresh();
        }
    }

    /**
     * Performs DefaultCache-specific refresh operations for entries that support
     * refresh.
     */
    private void performRefreshOperations() {
        if (config.getRefreshAfterWrite() != null) {
            long currentTimeNanos = System.nanoTime();
            data.forEach((key, entry) -> {
                long refreshThresholdNanos = entry.getCreationTimeNanos() + config.getRefreshAfterWrite().toNanos();
                if (currentTimeNanos > refreshThresholdNanos) {
                    // Refresh asynchronously without blocking
                    scheduler.execute(() -> {
                        V newValue = loadValue(key);
                        if (newValue != null) {
                            // Replace the old entry with the refreshed value
                            put(key, newValue);
                            notifyListeners(listener -> listener.onLoad(key, newValue));
                        }
                    });
                }
            });
        }
    }

    /**
     * Schedule refresh operations for entries that support refresh.
     */
    protected void scheduleRefresh() {
        scheduler.scheduleAtFixedRate(() -> {
            long currentTimeNanos = System.nanoTime();
            data.forEach((key, entry) -> processEntry(key, entry, currentTimeNanos));
        }, 0, REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Processes a single cache entry for expiration and refresh.
     */
    private void processEntry(K key, CacheEntry<V> entry, long currentTimeNanos) {
        if (entry.isExpired()) {
            removeExpiredEntryDuringRefresh(key, entry);
        } else if (needsRefresh(entry, currentTimeNanos)) {
            scheduleEntryRefresh(key);
        }
    }

    /**
     * Removes an expired entry during scheduled refresh operations.
     */
    private void removeExpiredEntryDuringRefresh(K key, CacheEntry<V> entry) {
        if (data.remove(key, entry)) {
            updateStatsAfterRemoval(key, entry);
            notifyListeners(listener -> listener.onExpire(key, entry.getValue()));
        }
    }

    /**
     * Checks if an entry needs to be refreshed.
     */
    private boolean needsRefresh(CacheEntry<V> entry, long currentTimeNanos) {
        if (config.getRefreshAfterWrite() == null) {
            return false;
        }

        long refreshThresholdNanos = entry.getCreationTimeNanos() + config.getRefreshAfterWrite().toNanos();
        return currentTimeNanos > refreshThresholdNanos;
    }

    /**
     * Schedules asynchronous refresh for an entry.
     */
    private void scheduleEntryRefresh(K key) {
        scheduler.execute(() -> {
            V newValue = loadValue(key);
            if (newValue != null) {
                put(key, newValue);
                notifyListeners(listener -> listener.onLoad(key, newValue));
            }
        });
    }

    /**
     * Updates statistics after removing an entry.
     */
    private void updateStatsAfterRemoval(K key, CacheEntry<V> entry) {
        currentSize.decrementAndGet();
        currentWeight.addAndGet(-entry.getWeight());
        evictionStrategy.remove(key);
    }

    /**
     * Closes this cache and releases any resources associated with it.
     */
    @Override
    public void close() {
        shutdown();
    }
}
