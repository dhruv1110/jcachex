package io.github.dhruv.jcachex;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a cache entry with value, weight, and expiration information.
 *
 * @param <V> the type of the cached value
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
