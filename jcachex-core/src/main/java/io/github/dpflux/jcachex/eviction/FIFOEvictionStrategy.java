package io.github.dpflux.jcachex.eviction;

import io.github.dpflux.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * First In, First Out (FIFO) eviction strategy.
 * This strategy evicts the entry that was inserted first.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class FIFOEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, Long> insertionOrder = new ConcurrentHashMap<>();
    private final AtomicLong insertionCounter = new AtomicLong(0);

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        return entries.entrySet().stream()
                .min((e1, e2) -> Long.compare(
                        insertionOrder.getOrDefault(e1.getKey(), Long.MAX_VALUE),
                        insertionOrder.getOrDefault(e2.getKey(), Long.MAX_VALUE)))
                .map(Map.Entry::getKey)
                .orElse(null);
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
