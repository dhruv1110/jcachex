package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * First In, First Out (FIFO) eviction strategy.
 * <p>
 * This strategy evicts the entry that was inserted first.
 * Optimized implementation with minimal object allocation.
 * </p>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class FIFOEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, Long> insertionOrder = new ConcurrentHashMap<>();
    private final AtomicLong insertionCounter = new AtomicLong(0);

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        if (entries.isEmpty()) {
            return null;
        }

        K candidate = null;
        long minOrder = Long.MAX_VALUE;

        // Efficient iteration without stream overhead
        for (K key : entries.keySet()) {
            Long order = insertionOrder.get(key);
            long insertOrder = order != null ? order : 0L;

            if (insertOrder < minOrder) {
                minOrder = insertOrder;
                candidate = key;
            }
        }

        return candidate;
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
        insertionOrder.putIfAbsent(key, insertionCounter.incrementAndGet());
    }

    @Override
    public void remove(K key) {
        insertionOrder.remove(key);
    }

    @Override
    public void clear() {
        insertionOrder.clear();
        insertionCounter.set(0);
    }
}
