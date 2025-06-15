package io.github.dpflux.jcachex.eviction;

import io.github.dpflux.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Most Recently Used (MRU) eviction strategy.
 * This strategy evicts the entry that was accessed most recently.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class MRUEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, Long> accessOrder = new ConcurrentHashMap<>();
    private final AtomicLong accessCounter = new AtomicLong(0);

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        return entries.entrySet().stream()
                .max((e1, e2) -> Long.compare(
                        accessOrder.getOrDefault(e1.getKey(), Long.MIN_VALUE),
                        accessOrder.getOrDefault(e2.getKey(), Long.MIN_VALUE)))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

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
        accessCounter.set(0);
    }
}
