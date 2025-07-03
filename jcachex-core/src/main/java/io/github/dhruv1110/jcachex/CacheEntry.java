package io.github.dhruv1110.jcachex;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a cache entry containing a value along with metadata for cache
 * management.
 * <p>
 * This class encapsulates both the cached value and important metadata such as
 * creation time,
 * last access time, access count, weight, and expiration information. This
 * metadata is used
 * by eviction strategies, expiration policies, and statistics collection.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li><strong>Value Storage:</strong> Holds the actual cached value</li>
 * <li><strong>Access Tracking:</strong> Maintains access count and last access
 * time for LRU/LFU eviction</li>
 * <li><strong>Weight Support:</strong> Stores entry weight for weight-based
 * eviction strategies</li>
 * <li><strong>Expiration:</strong> Tracks creation time and expiration time for
 * TTL policies</li>
 * <li><strong>Thread Safety:</strong> Access count updates are atomic and
 * thread-safe</li>
 * </ul>
 *
 * <h3>Usage in Cache Operations:</h3>
 *
 * <pre>{@code
 * // Cache entries are created internally when putting values
 * cache.put("user123", user); // Creates CacheEntry<User> internally
 *
 * // Eviction strategies use entry metadata
 * // LRU strategy checks lastAccessTime
 * // LFU strategy checks accessCount
 * // Weight-based strategy checks weight
 *
 * // Expiration policies use creation and expiration times
 * if (entry.isExpired()) {
 *     cache.remove(key); // Entry is automatically removed
 * }
 * }</pre>
 *
 * <h3>Integration with Eviction Strategies:</h3>
 *
 * <pre>{@code
 * // Custom eviction strategy example
 * public class CustomEvictionStrategy<K, V> implements EvictionStrategy<K, V> { @Override
 *     public K selectEvictionCandidate(Map<K, CacheEntry<V>> entries) {
 *         return entries.entrySet().stream()
 *                 .min((e1, e2) -> {
 *                     CacheEntry<V> entry1 = e1.getValue();
 *                     CacheEntry<V> entry2 = e2.getValue();
 *
 *                     // Evict entry with lowest access count
 *                     return Long.compare(entry1.getAccessCount(), entry2.getAccessCount());
 *                 })
 *                 .map(Map.Entry::getKey)
 *                 .orElse(null);
 *     }
 *     // ... other methods
 * }
 * }</pre>
 *
 * @param <V> the type of the cached value
 * @see io.github.dhruv1110.jcachex.eviction.EvictionStrategy
 * @see CacheConfig
 * @since 1.0.0
 */
public class CacheEntry<V> {
    private final V value;
    private final long weight;
    private final Instant expirationTime;
    private final AtomicLong accessCount;
    private volatile Instant lastAccessTime;
    private final Instant creationTime;

    public CacheEntry(V value, long weight, Instant expirationTime) {
        this.value = value;
        this.weight = weight;
        this.expirationTime = expirationTime;
        this.accessCount = new AtomicLong(0);
        this.lastAccessTime = Instant.now();
        this.creationTime = Instant.now();
    }

    public V getValue() {
        return value;
    }

    public long getWeight() {
        return weight;
    }

    public boolean isExpired() {
        return expirationTime != null && Instant.now().isAfter(expirationTime);
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public long getAccessCount() {
        return accessCount.get();
    }

    public void incrementAccessCount() {
        accessCount.incrementAndGet();
        lastAccessTime = Instant.now();
    }

    public Instant getLastAccessTime() {
        return lastAccessTime;
    }

    public Instant getCreationTime() {
        return creationTime;
    }
}
