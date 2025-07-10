package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Least Frequently Used (LFU) eviction strategy.
 * <p>
 * This strategy evicts the entry that was accessed least frequently.
 * Optimized implementation that minimizes object allocation and provides
 * efficient frequency tracking.
 * </p>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class LFUEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, AtomicLong> accessCounts = new ConcurrentHashMap<>();

    // Reusable AtomicLong for efficient frequency tracking
    private static final AtomicLong ZERO_COUNT = new AtomicLong(0);

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        if (entries.isEmpty()) {
            return null;
        }

        K candidate = null;
        long minCount = Long.MAX_VALUE;

        // Efficient iteration without stream overhead
        for (Map.Entry<K, CacheEntry<V>> entry : entries.entrySet()) {
            K key = entry.getKey();
            AtomicLong count = accessCounts.get(key);
            long frequency = count != null ? count.get() : 0L;

            if (frequency < minCount) {
                minCount = frequency;
                candidate = key;
            }
        }

        return candidate;
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
        // Use computeIfAbsent with a lambda that creates new AtomicLong only when
        // needed
        accessCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    @Override
    public void remove(K key) {
        accessCounts.remove(key);
    }

    @Override
    public void clear() {
        accessCounts.clear();
    }
}
