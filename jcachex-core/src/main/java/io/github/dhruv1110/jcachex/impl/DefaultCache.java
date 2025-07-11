package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEntry;
import io.github.dhruv1110.jcachex.CacheEventListener;

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
            data.forEach((key, entry) -> {
                if (entry.isExpired()) {
                    // Handle expired entries
                    if (data.remove(key, entry)) {
                        currentSize.decrementAndGet();
                        currentWeight.addAndGet(-entry.getWeight());
                        evictionStrategy.remove(key);
                        notifyListeners(listener -> listener.onExpire(key, entry.getValue()));
                    }
                } else if (config.getRefreshAfterWrite() != null) {
                    long refreshThresholdNanos = entry.getCreationTimeNanos() + config.getRefreshAfterWrite().toNanos();
                    if (currentTimeNanos > refreshThresholdNanos) {
                        // Trigger async refresh and update the cache entry
                        scheduler.execute(() -> {
                            V newValue = loadValue(key);
                            if (newValue != null) {
                                // Replace the old entry with the refreshed value
                                put(key, newValue);
                                notifyListeners(listener -> listener.onLoad(key, newValue));
                            }
                        });
                    }
                }
            });
        }, 0, REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Closes this cache and releases any resources associated with it.
     */
    @Override
    public void close() {
        shutdown();
    }
}
