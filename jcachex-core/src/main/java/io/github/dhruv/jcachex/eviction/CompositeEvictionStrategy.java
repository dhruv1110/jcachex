package io.github.dhruv.jcachex.eviction;

import io.github.dhruv.jcachex.CacheEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Composite eviction strategy that combines multiple strategies.
 * This strategy applies each strategy in order until a candidate is found.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class CompositeEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final List<EvictionStrategy<K, V>> strategies;

    public CompositeEvictionStrategy(List<EvictionStrategy<K, V>> strategies) {
        this.strategies = new ArrayList<>(strategies);
    }

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        for (EvictionStrategy<K, V> strategy : strategies) {
            K candidate = strategy.selectEvictionCandidate(entries);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
        for (EvictionStrategy<K, V> strategy : strategies) {
            strategy.update(key, entry);
        }
    }

    @Override
    public void remove(K key) {
        for (EvictionStrategy<K, V> strategy : strategies) {
            strategy.remove(key);
        }
    }

    @Override
    public void clear() {
        for (EvictionStrategy<K, V> strategy : strategies) {
            strategy.clear();
        }
    }
}
