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
    private final Instant expirationTime; // Keep Instant for compatibility with Duration-based expiration
    private final long expirationTimeNanos; // Add nanos for faster expiration checks
    private final AtomicLong accessCount;
    private volatile long lastAccessTimeNanos;
    private volatile Instant lastAccessTime; // Keep for compatibility
    private final long creationTimeNanos;
    private final Instant creationTime; // Keep for compatibility

    public CacheEntry(V value, long weight, Instant expirationTime) {
        this.value = value;
        this.weight = weight;
        this.expirationTime = expirationTime;
        this.expirationTimeNanos = calculateExpirationNanos(expirationTime);
        this.accessCount = new AtomicLong(0);
        long currentNanos = System.nanoTime();
        Instant now = Instant.now();
        this.lastAccessTimeNanos = currentNanos;
        this.lastAccessTime = now;
        this.creationTimeNanos = currentNanos;
        this.creationTime = now;
    }

    private static long calculateExpirationNanos(Instant expirationTime) {
        if (expirationTime == null) {
            return -1L; // Special value for "no expiration"
        }

        try {
            // Handle extreme cases first
            if (expirationTime.equals(Instant.MAX)) {
                return Long.MAX_VALUE;
            }
            if (expirationTime.equals(Instant.MIN)) {
                return 1L; // Special value for "expired far in the past"
            }

            long currentTimeMillis = System.currentTimeMillis();
            long expirationMillis = expirationTime.toEpochMilli();
            long deltaMillis = expirationMillis - currentTimeMillis;

            // Clamp to reasonable bounds to avoid overflow
            if (deltaMillis > 365L * 24 * 60 * 60 * 1000) { // > 1 year
                return Long.MAX_VALUE;
            } else if (deltaMillis < -365L * 24 * 60 * 60 * 1000) { // < -1 year
                return 1L; // Expired far in the past
            } else {
                long result = System.nanoTime() + deltaMillis * 1_000_000L;
                // Ensure we don't return our special values accidentally
                if (result <= 1L)
                    return 2L;
                return result;
            }
        } catch (Exception e) {
            // Handle any overflow or other issues gracefully
            return expirationTime.isAfter(Instant.now()) ? Long.MAX_VALUE : 1L;
        }
    }

    public V getValue() {
        return value;
    }

    public long getWeight() {
        return weight;
    }

    public boolean isExpired() {
        if (expirationTimeNanos == -1L) {
            // No expiration set
            return false;
        }
        if (expirationTimeNanos == 1L) {
            // Expired far in the past (like Instant.MIN)
            return true;
        }
        return System.nanoTime() > expirationTimeNanos;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public long getAccessCount() {
        return accessCount.get();
    }

    public void incrementAccessCount() {
        accessCount.incrementAndGet();
        lastAccessTimeNanos = System.nanoTime();
        lastAccessTime = Instant.now();
    }

    public Instant getLastAccessTime() {
        return lastAccessTime;
    }

    public long getLastAccessTimeNanos() {
        return lastAccessTimeNanos;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public long getCreationTimeNanos() {
        return creationTimeNanos;
    }
}
