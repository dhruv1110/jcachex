package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Optimized Weight-based eviction strategy.
 * <p>
 * This strategy evicts entries based on their weight, prioritizing heavier
 * entries
 * for eviction. While finding the heaviest entry is inherently O(n), this
 * implementation
 * optimizes the operation by avoiding stream operations and using efficient
 * iteration.
 * </p>
 *
 * <h3>Performance Characteristics:</h3>
 * <ul>
 * <li><strong>Update Operation:</strong> O(1) - constant time weight
 * tracking</li>
 * <li><strong>Eviction Selection:</strong> O(n) - linear scan to find heaviest
 * entry</li>
 * <li><strong>Memory Overhead:</strong> One weight value per entry</li>
 * <li><strong>Thread Safety:</strong> Read-write lock for optimal concurrent
 * performance</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class WeightBasedEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, Long> weights = new ConcurrentHashMap<>();
    private final long maxWeight;

    // Thread safety
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public WeightBasedEvictionStrategy(long maxWeight) {
        this.maxWeight = maxWeight;
    }

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        readLock.lock();
        try {
            if (entries.isEmpty()) {
                return null;
            }

            K candidate = null;
            long maxEntryWeight = Long.MIN_VALUE;

            // Efficient iteration without stream overhead
            for (Map.Entry<K, CacheEntry<V>> entry : entries.entrySet()) {
                K key = entry.getKey();
                Long weight = weights.get(key);
                long entryWeight = weight != null ? weight : 0L;

                if (entryWeight > maxEntryWeight) {
                    maxEntryWeight = entryWeight;
                    candidate = key;
                }
            }

            return candidate;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void update(K key, CacheEntry<V> entry) {
        writeLock.lock();
        try {
            weights.put(key, entry.getWeight());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(K key) {
        writeLock.lock();
        try {
            weights.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            weights.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns the current total weight of all entries.
     *
     * @return the total weight
     */
    public long getCurrentWeight() {
        readLock.lock();
        try {
            long totalWeight = 0;
            for (Long weight : weights.values()) {
                totalWeight += weight;
            }
            return totalWeight;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Checks if the current weight exceeds the maximum weight.
     *
     * @return true if over weight limit
     */
    public boolean isOverWeight() {
        return getCurrentWeight() > maxWeight;
    }

    /**
     * Returns the maximum weight limit.
     *
     * @return the maximum weight
     */
    public long getMaxWeight() {
        return maxWeight;
    }

    /**
     * Returns the weight of a specific key.
     *
     * @param key the key to check
     * @return the weight, or 0 if not found
     */
    public long getWeight(K key) {
        readLock.lock();
        try {
            return weights.getOrDefault(key, 0L);
        } finally {
            readLock.unlock();
        }
    }
}
