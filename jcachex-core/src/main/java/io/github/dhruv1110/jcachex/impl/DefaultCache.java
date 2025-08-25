package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEntry;
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

    // Use a simple deadline queue to avoid full-map scans on every tick
    private final java.util.concurrent.ConcurrentSkipListMap<Long, K> refreshDeadlines = new java.util.concurrent.ConcurrentSkipListMap<>();

    /**
     * Creates a new DefaultCache with the specified configuration.
     */
    public DefaultCache(CacheConfig<K, V> config) {
        super(config);
        // Parent schedules refresh; we only provide the refreshEntries() implementation
    }

    /**
     * Performs DefaultCache-specific refresh operations for entries that support
     * refresh.
     */
    private void performRefreshOperations() {
        if (config.getRefreshAfterWrite() == null) {
            return;
        }
        long now = System.nanoTime();
        java.util.NavigableMap<Long, K> due = refreshDeadlines.headMap(now, true);
        if (due.isEmpty()) {
            return;
        }
        java.util.List<java.util.Map.Entry<Long, K>> batch = new java.util.ArrayList<>(Math.min(1024, due.size()));
        for (java.util.Map.Entry<Long, K> e : due.entrySet()) {
            batch.add(e);
            if (batch.size() >= 1024)
                break;
        }
        for (java.util.Map.Entry<Long, K> e : batch) {
            refreshDeadlines.remove(e.getKey(), e.getValue());
            K key = e.getValue();
            CacheEntry<V> entry = data.get(key);
            if (entry == null)
                continue;
            if (needsRefresh(entry, now)) {
                scheduleEntryRefresh(key);
            } else if (entry.isExpired()) {
                removeExpiredEntryDuringRefresh(key, entry);
            } else {
                // re-schedule if not yet due
                long next = entry.getCreationTimeNanos() + config.getRefreshAfterWrite().toNanos();
                refreshDeadlines.put(next, key);
            }
        }
    }

    // Use parent's scheduler; this method gets called once per tick
    @Override
    protected void refreshEntries() {
        if (config.getRefreshAfterWrite() == null) {
            return;
        }
        long now = System.nanoTime();
        // Seed deadlines for all entries and handle due ones in this tick
        data.forEach((key, entry) -> processEntry(key, entry, now));
        performRefreshOperations();
    }

    /**
     * Processes a single cache entry for expiration and refresh.
     */
    private void processEntry(K key, CacheEntry<V> entry, long currentTimeNanos) {
        if (entry.isExpired()) {
            removeExpiredEntryDuringRefresh(key, entry);
        } else if (needsRefresh(entry, currentTimeNanos)) {
            scheduleEntryRefresh(key);
        } else if (config.getRefreshAfterWrite() != null) {
            long deadline = entry.getCreationTimeNanos() + config.getRefreshAfterWrite().toNanos();
            refreshDeadlines.putIfAbsent(deadline, key);
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
