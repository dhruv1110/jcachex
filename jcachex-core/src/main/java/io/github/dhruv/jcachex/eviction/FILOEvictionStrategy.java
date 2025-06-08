package io.github.dhruv.jcachex.eviction;

import io.github.dhruv.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * First In, Last Out (FILO) eviction strategy.
 * This strategy evicts the entry that was inserted last.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class FILOEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, Long> insertionOrder = new ConcurrentHashMap<>();
    private final AtomicLong insertionCounter = new AtomicLong(0);

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        return entries.entrySet().stream()
                .max((e1, e2) -> Long.compare(
                        insertionOrder.getOrDefault(e1.getKey(), Long.MIN_VALUE),
                        insertionOrder.getOrDefault(e2.getKey(), Long.MIN_VALUE)))
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
