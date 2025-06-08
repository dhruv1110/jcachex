package io.github.dhruv.jcachex.eviction;

import io.github.dhruv.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Least Frequently Used (LFU) eviction strategy.
 * This strategy evicts the entry that was accessed least frequently.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class LFUEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, AtomicLong> accessCounts = new ConcurrentHashMap<>();

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        return entries.entrySet().stream()
                .min((e1, e2) -> Long.compare(
                        accessCounts.getOrDefault(e1.getKey(), new AtomicLong(Long.MAX_VALUE)).get(),
                        accessCounts.getOrDefault(e2.getKey(), new AtomicLong(Long.MAX_VALUE)).get()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
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
