package io.github.dpflux.jcachex.eviction;

import io.github.dpflux.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Weight-based eviction strategy.
 * This strategy evicts entries based on their weight.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class WeightBasedEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, Long> weights = new ConcurrentHashMap<>();
    private final long maxWeight;

    public WeightBasedEvictionStrategy(long maxWeight) {
        this.maxWeight = maxWeight;
    }

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        return entries.entrySet().stream()
                .max((e1, e2) -> Long.compare(
                        weights.getOrDefault(e1.getKey(), 0L),
                        weights.getOrDefault(e2.getKey(), 0L)))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
        weights.put(key, entry.getWeight());
    }

    @Override
    public void remove(K key) {
        weights.remove(key);
    }

    @Override
    public void clear() {
        weights.clear();
    }

    public long getCurrentWeight() {
        return weights.values().stream().mapToLong(Long::longValue).sum();
    }

    public boolean isOverWeight() {
        return getCurrentWeight() > maxWeight;
    }
}
