package io.github.dpflux.jcachex.eviction;

import io.github.dpflux.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Least Recently Used (LRU) eviction strategy.
 * This strategy evicts the entry that was accessed least recently.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class LRUEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, Long> accessOrder = new ConcurrentHashMap<>();
    private final AtomicLong accessCounter = new AtomicLong(0);

    @Override
    public void update(K key, CacheEntry<V> entry) {
        accessOrder.put(key, accessCounter.incrementAndGet());
    }

    @Override
    public void remove(K key) {
        accessOrder.remove(key);
    }

    @Override
    public void clear() {
        accessOrder.clear();
        accessCounter.set(0); // Reset counter when clearing
    }

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        return entries.entrySet().stream()
                .min((e1, e2) -> Long.compare(
                        accessOrder.getOrDefault(e1.getKey(), 0L), // Never-accessed entries get 0 (highest eviction
                                                                   // priority)
                        accessOrder.getOrDefault(e2.getKey(), 0L)))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
