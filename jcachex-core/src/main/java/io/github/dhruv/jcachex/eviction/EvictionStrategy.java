package io.github.dhruv.jcachex.eviction;

import io.github.dhruv.jcachex.CacheEntry;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Interface for cache eviction strategies.
 * Implementations of this interface determine which entries should be evicted
 * from the cache.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public interface EvictionStrategy<K, V> {
    /**
     * Selects a candidate for eviction from the given entries.
     *
     * @param entries the current cache entries
     * @return the key of the entry to evict, or null if no candidate is found
     */
    K selectEvictionCandidate(Map<K, CacheEntry<V>> entries);

    /**
     * Updates the strategy's state when an entry is accessed or modified.
     *
     * @param key   the key of the entry
     * @param entry the cache entry
     */
    void update(K key, CacheEntry<V> entry);

    /**
     * Removes an entry from the strategy's state.
     *
     * @param key the key of the entry to remove
     */
    void remove(K key);

    /**
     * Clears all state maintained by the strategy.
     */
    void clear();
}
