package io.github.dhruv1110.jcachex.eviction;

import io.github.dhruv1110.jcachex.CacheEntry;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Optimized Idle Time-based eviction strategy.
 * <p>
 * This strategy evicts entries based on their last access time, prioritizing
 * entries that have been idle for the longest time. While finding the oldest
 * idle entry is inherently O(n), this implementation optimizes the operation
 * by avoiding stream operations and using efficient iteration.
 * </p>
 *
 * <h3>Performance Characteristics:</h3>
 * <ul>
 * <li><strong>Update Operation:</strong> O(1) - constant time access
 * tracking</li>
 * <li><strong>Eviction Selection:</strong> O(n) - linear scan to find oldest
 * idle entry</li>
 * <li><strong>Memory Overhead:</strong> One timestamp per entry</li>
 * <li><strong>Thread Safety:</strong> Read-write lock for optimal concurrent
 * performance</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 */
public class IdleTimeEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    private final ConcurrentHashMap<K, Instant> lastAccessTime = new ConcurrentHashMap<>();
    private final Duration maxIdleTime;

    // Thread safety
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public IdleTimeEvictionStrategy(Duration maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    @Override
    public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
        readLock.lock();
        try {
            if (entries.isEmpty()) {
                return null;
            }

            Instant now = Instant.now();
            K candidate = null;
            Instant oldestAccessTime = null;

            // First pass: find entries that exceed idle time
            for (Map.Entry<K, CacheEntry<V>> entry : entries.entrySet()) {
                K key = entry.getKey();
                Instant lastAccess = lastAccessTime.getOrDefault(key, Instant.EPOCH);

                if (Duration.between(lastAccess, now).compareTo(maxIdleTime) >= 0) {
                    // This entry is idle beyond the threshold
                    if (oldestAccessTime == null || lastAccess.isBefore(oldestAccessTime)) {
                        oldestAccessTime = lastAccess;
                        candidate = key;
                    }
                }
            }

            // If no idle entries found, return the oldest accessed entry
            if (candidate == null) {
                for (Map.Entry<K, CacheEntry<V>> entry : entries.entrySet()) {
                    K key = entry.getKey();
                    Instant lastAccess = lastAccessTime.getOrDefault(key, Instant.EPOCH);

                    if (oldestAccessTime == null || lastAccess.isBefore(oldestAccessTime)) {
                        oldestAccessTime = lastAccess;
                        candidate = key;
                    }
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
            lastAccessTime.put(key, Instant.now());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(K key) {
        writeLock.lock();
        try {
            lastAccessTime.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            lastAccessTime.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns the last access time for a specific key.
     *
     * @param key the key to check
     * @return the last access time, or Instant.EPOCH if not found
     */
    public Instant getLastAccessTime(K key) {
        readLock.lock();
        try {
            return lastAccessTime.getOrDefault(key, Instant.EPOCH);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the maximum idle time threshold.
     *
     * @return the maximum idle time
     */
    public Duration getMaxIdleTime() {
        return maxIdleTime;
    }

    /**
     * Checks if a key is currently idle beyond the threshold.
     *
     * @param key the key to check
     * @return true if the key is idle beyond the threshold
     */
    public boolean isIdle(K key) {
        readLock.lock();
        try {
            Instant lastAccess = lastAccessTime.getOrDefault(key, Instant.EPOCH);
            Instant now = Instant.now();
            return Duration.between(lastAccess, now).compareTo(maxIdleTime) >= 0;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the idle time for a specific key.
     *
     * @param key the key to check
     * @return the idle duration
     */
    public Duration getIdleTime(K key) {
        readLock.lock();
        try {
            Instant lastAccess = lastAccessTime.getOrDefault(key, Instant.EPOCH);
            return Duration.between(lastAccess, Instant.now());
        } finally {
            readLock.unlock();
        }
    }
}
