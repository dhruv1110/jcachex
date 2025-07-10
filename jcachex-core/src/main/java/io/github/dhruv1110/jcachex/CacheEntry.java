package io.github.dhruv1110.jcachex;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a cache entry containing a value along with metadata for cache
 * management.
 * <p>
 * This class encapsulates both the cached value and important metadata such as
 * creation time, last access time, access count, weight, and expiration
 * information.
 * This metadata is used by eviction strategies, expiration policies, and
 * statistics collection.
 * </p>
 *
 * <p>
 * <strong>Performance Optimizations:</strong>
 * This implementation prioritizes performance by using nanoTime for
 * time-sensitive
 * operations and maintaining minimal object overhead.
 * </p>
 *
 * @param <V> the type of the cached value
 * @see io.github.dhruv1110.jcachex.eviction.EvictionStrategy
 * @see CacheConfig
 * @since 1.0.0
 */
public class CacheEntry<V> {
    // Core data (ordered for optimal memory layout)
    private final V value;
    private final long weight;

    // Time tracking using nanoTime for performance (no Instant objects for hot
    // paths)
    private final long creationTimeNanos;
    private final long expirationTimeNanos; // -1 = no expiration, 1 = expired
    private volatile long lastAccessTimeNanos;

    // Access tracking - using single AtomicLong instead of separate counter
    private final AtomicLong accessCount;

    // Store original expiration Instant for exact compatibility
    private final Instant originalExpirationTime;

    // Cached Instant objects - created lazily only when needed for compatibility
    private volatile Instant cachedCreationTime;
    private volatile Instant cachedLastAccessTime;

    public CacheEntry(V value, long weight, Instant expirationTime) {
        this.value = value;
        this.weight = weight;
        this.creationTimeNanos = System.nanoTime();
        this.expirationTimeNanos = calculateExpirationNanos(expirationTime);
        this.lastAccessTimeNanos = this.creationTimeNanos;
        this.accessCount = new AtomicLong(0);
        this.originalExpirationTime = expirationTime; // Store original for exact return
        // Instant objects created lazily when needed
    }

    /**
     * Efficiently calculate expiration time in nanoseconds
     */
    private static long calculateExpirationNanos(Instant expirationTime) {
        if (expirationTime == null) {
            return -1L; // No expiration
        }

        try {
            long currentTimeMillis = System.currentTimeMillis();
            long expirationMillis = expirationTime.toEpochMilli();
            long deltaMillis = expirationMillis - currentTimeMillis;

            // Handle extreme cases
            if (deltaMillis > 365L * 24 * 60 * 60 * 1000) { // > 1 year
                return Long.MAX_VALUE;
            } else if (deltaMillis < 0) {
                return 1L; // Already expired
            } else {
                long result = System.nanoTime() + deltaMillis * 1_000_000L;
                return result <= 1L ? 2L : result; // Avoid special values
            }
        } catch (Exception e) {
            return expirationTime.isAfter(Instant.now()) ? Long.MAX_VALUE : 1L;
        }
    }

    public V getValue() {
        return value;
    }

    public long getWeight() {
        return weight;
    }

    /**
     * Fast expiration check using nanoTime
     */
    public boolean isExpired() {
        if (expirationTimeNanos == -1L) {
            return false; // No expiration
        }
        if (expirationTimeNanos == 1L) {
            return true; // Expired marker
        }
        return System.nanoTime() > expirationTimeNanos;
    }

    /**
     * Get expiration time as Instant (returns original Instant for compatibility)
     */
    public Instant getExpirationTime() {
        return originalExpirationTime;
    }

    public long getAccessCount() {
        return accessCount.get();
    }

    /**
     * Optimized access count increment with minimal overhead
     */
    public void incrementAccessCount() {
        accessCount.incrementAndGet();
        lastAccessTimeNanos = System.nanoTime();
        // Clear cached last access time to force regeneration
        cachedLastAccessTime = null;
    }

    /**
     * Get last access time as Instant (created lazily for compatibility)
     */
    public Instant getLastAccessTime() {
        if (cachedLastAccessTime == null) {
            synchronized (this) {
                if (cachedLastAccessTime == null) {
                    // Convert nanoTime back to approximate Instant
                    long deltaMillis = (lastAccessTimeNanos - creationTimeNanos) / 1_000_000L;
                    cachedLastAccessTime = getCreationTime().plusMillis(deltaMillis);
                }
            }
        }
        return cachedLastAccessTime;
    }

    public long getLastAccessTimeNanos() {
        return lastAccessTimeNanos;
    }

    /**
     * Get creation time as Instant (created lazily for compatibility)
     */
    public Instant getCreationTime() {
        if (cachedCreationTime == null) {
            synchronized (this) {
                if (cachedCreationTime == null) {
                    cachedCreationTime = Instant.now().minusNanos(System.nanoTime() - creationTimeNanos);
                }
            }
        }
        return cachedCreationTime;
    }

    public long getCreationTimeNanos() {
        return creationTimeNanos;
    }
}
