package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;
import io.github.dhruv1110.jcachex.FrequencySketchType;

import java.util.Map;

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
     * Enhanced LRU eviction strategy with frequency sketch.
     */
    static <K, V> EvictionStrategy<K, V> ENHANCED_LRU() {
        return new EnhancedLRUEvictionStrategy<>(FrequencySketchType.BASIC, 1000);
    }

    /**
     * Enhanced LFU eviction strategy with frequency sketch.
     */
    static <K, V> EvictionStrategy<K, V> ENHANCED_LFU() {
        return new EnhancedLFUEvictionStrategy<>(FrequencySketchType.BASIC, 1000);
    }

    /**
     * TinyWindowLFU eviction strategy (hybrid approach).
     */
    static <K, V> EvictionStrategy<K, V> TINY_WINDOW_LFU() {
        return new WindowTinyLFUEvictionStrategy<>(1000);
    }

    /**
     * Basic LRU eviction strategy.
     */
    static <K, V> EvictionStrategy<K, V> LRU() {
        return new LRUEvictionStrategy<>();
    }

    /**
     * Basic LFU eviction strategy.
     */
    static <K, V> EvictionStrategy<K, V> LFU() {
        return new LFUEvictionStrategy<>();
    }

    /**
     * FIFO eviction strategy.
     */
    static <K, V> EvictionStrategy<K, V> FIFO() {
        return new FIFOEvictionStrategy<>();
    }

    /**
     * FILO eviction strategy.
     */
    static <K, V> EvictionStrategy<K, V> FILO() {
        return new FILOEvictionStrategy<>();
    }

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
