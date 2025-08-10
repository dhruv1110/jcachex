package io.github.dhruv1110.jcachex.impl.base;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEntry;
import io.github.dhruv1110.jcachex.CacheStats;
import io.github.dhruv1110.jcachex.internal.util.ConfigurationProvider;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract base class for all cache implementations providing common
 * functionality.
 *
 * This class implements the common patterns found across cache implementations:
 * - Parameter validation
 * - Statistics tracking
 * - Configuration management
 * - Async operation wrappers
 * - Template methods for extension points
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public abstract class AbstractCacheBase<K, V> implements Cache<K, V> {

    // Core components - final to enforce immutability
    protected final CacheConfig<K, V> config;
    protected final CacheStats stats;
    protected final boolean statsEnabled;
    protected final long maximumSize;

    // Statistics tracking
    protected final AtomicLong hitCount;
    protected final AtomicLong missCount;

    // Configuration derived values
    protected final boolean hasExpiration;
    protected final boolean hasMaximumSize;
    protected final boolean hasMaximumWeight;

    /**
     * Constructor for all cache implementations.
     *
     * @param config the cache configuration
     * @throws IllegalArgumentException if config is null
     */
    protected AbstractCacheBase(CacheConfig<K, V> config) {
        // Validate configuration using utility
        ConfigurationProvider.validateConfiguration(config);

        this.config = config;
        this.stats = new CacheStats();
        this.statsEnabled = config.isRecordStats();
        this.maximumSize = ConfigurationProvider.getSafeMaximumSize(config);

        // Initialize statistics
        this.hitCount = new AtomicLong(0);
        this.missCount = new AtomicLong(0);

        // Cache configuration flags for performance using utilities
        this.hasExpiration = ConfigurationProvider.hasExpiration(config);
        this.hasMaximumSize = config.getMaximumSize() != null;
        this.hasMaximumWeight = config.getMaximumWeight() != null;
    }

    /**
     * Template method for get operation with common validation and statistics.
     */
    @Override
    public final V get(K key) {
        if (!validateKey(key)) {
            return null;
        }

        V value = doGet(key);
        // Statistics are now recorded in doGet() method
        return value;
    }

    /**
     * Template method for put operation with common validation.
     */
    @Override
    public final void put(K key, V value) {
        if (!validateKey(key) || !validateValue(value)) {
            return;
        }

        doPut(key, value);
        recordPutStatistics();
    }

    /**
     * Template method for remove operation with common validation.
     */
    @Override
    public final V remove(K key) {
        if (!validateKey(key)) {
            return null;
        }

        V removedValue = doRemove(key);
        recordRemoveStatistics(removedValue != null);
        return removedValue;
    }

    /**
     * Template method for clear operation with statistics recording.
     */
    @Override
    public final void clear() {
        long sizeBefore = size();
        doClear();
        recordClearStatistics(sizeBefore);
    }

    /**
     * Common implementation of containsKey with validation.
     */
    @Override
    public final boolean containsKey(K key) {
        if (!validateKey(key)) {
            return false;
        }
        return doContainsKey(key);
    }

    /**
     * Common implementation of stats() method.
     */
    @Override
    public final CacheStats stats() {
        if (statsEnabled) {
            updateStatsFromCounters();
            return stats.snapshot();
        }
        return stats;
    }

    /**
     * Common implementation of config() method.
     */
    @Override
    public final CacheConfig<K, V> config() {
        return config;
    }

    // Async operation wrappers - common across all implementations

    @Override
    public CompletableFuture<V> getAsync(K key) {
        if (!validateKey(key)) {
            return CompletableFuture.completedFuture(null);
        }

        // Use async loader if available
        if (config.getAsyncLoader() != null) {
            // Check if value exists in cache first
            V cachedValue = doGet(key);
            if (cachedValue != null) {
                // Statistics already recorded in doGet()
                return CompletableFuture.completedFuture(cachedValue);
            }

            // Load asynchronously - statistics already recorded in doGet()
            return config.getAsyncLoader().apply(key).thenApply(value -> {
                if (value != null) {
                    put(key, value);
                }
                return value;
            });
        }

        // Fallback to synchronous get
        return CompletableFuture.completedFuture(get(key));
    }

    @Override
    public CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.runAsync(() -> put(key, value));
    }

    @Override
    public CompletableFuture<V> removeAsync(K key) {
        return CompletableFuture.completedFuture(remove(key));
    }

    @Override
    public CompletableFuture<Void> clearAsync() {
        return CompletableFuture.runAsync(this::clear);
    }

    // Abstract methods for implementation-specific behavior

    /**
     * Implementation-specific get operation.
     *
     * @param key the key (already validated)
     * @return the value or null if not found/expired
     */
    protected abstract V doGet(K key);

    /**
     * Implementation-specific put operation.
     *
     * @param key   the key (already validated)
     * @param value the value (already validated)
     */
    protected abstract void doPut(K key, V value);

    /**
     * Implementation-specific remove operation.
     *
     * @param key the key (already validated)
     * @return the removed value or null if not found
     */
    protected abstract V doRemove(K key);

    /**
     * Implementation-specific clear operation.
     */
    protected abstract void doClear();

    /**
     * Implementation-specific containsKey operation.
     *
     * @param key the key (already validated)
     * @return true if key exists and is not expired
     */
    protected abstract boolean doContainsKey(K key);

    // Utility methods for common operations

    /**
     * Validates that a key is not null.
     *
     * @param key the key to validate
     * @return true if key is valid
     */
    protected boolean validateKey(K key) {
        return key != null;
    }

    /**
     * Validates that a value is not null.
     *
     * @param value the value to validate
     * @return true if value is valid
     */
    protected boolean validateValue(V value) {
        return true; // Allow null values to be stored
    }

    /**
     * Records statistics for get operations.
     *
     * @param hit true if the operation was a hit
     */
    protected void recordGetStatistics(boolean hit) {
        if (statsEnabled) {
            if (hit) {
                hitCount.incrementAndGet();
                stats.getHitCount().incrementAndGet();
            } else {
                missCount.incrementAndGet();
                stats.getMissCount().incrementAndGet();
            }
        }
    }

    /**
     * Records statistics for put operations.
     */
    protected void recordPutStatistics() {
        // Override in subclasses for specific put statistics
    }

    /**
     * Records statistics for remove operations.
     *
     * @param removed true if an entry was actually removed
     */
    protected void recordRemoveStatistics(boolean removed) {
        // Override in subclasses for specific remove statistics
    }

    /**
     * Records statistics for clear operations.
     *
     * @param sizeBefore the size before clearing
     */
    protected void recordClearStatistics(long sizeBefore) {
        // Override in subclasses for specific clear statistics
    }

    /**
     * Updates the stats object from the atomic counters.
     */
    protected void updateStatsFromCounters() {
        // Stats are already updated in recordGetStatistics
        // This method is kept for compatibility but no longer needed
    }

    /**
     * Checks if an entry is expired based on cache configuration.
     *
     * @param entry the cache entry to check
     * @return true if the entry is expired
     */
    protected boolean isEntryExpired(CacheEntry<V> entry) {
        if (!hasExpiration || entry == null) {
            return false;
        }

        return entry.isExpired();
    }

    /**
     * Creates a cache entry with proper timestamp and expiration settings.
     *
     * @param value the value to wrap
     * @return a new cache entry
     */
    protected CacheEntry<V> createCacheEntry(V value) {
        Instant expirationTime = null;
        if (config.getExpireAfterWrite() != null) {
            expirationTime = Instant.now().plus(config.getExpireAfterWrite());
        } else if (config.getExpireAfterAccess() != null) {
            expirationTime = Instant.now().plus(config.getExpireAfterAccess());
        }

        long weight = config.getWeigher() != null ? config.getWeigher().apply(null, value) : 1L;
        return new CacheEntry<>(value, weight, expirationTime);
    }

    /**
     * Checks if the cache has reached its maximum size or weight.
     *
     * @return true if size or weight limit is exceeded
     */
    protected boolean isSizeLimitReached() {
        if (hasMaximumSize && size() > maximumSize) {
            return true;
        }

        if (hasMaximumWeight && config.getMaximumWeight() != null) {
            return getCurrentWeight() > config.getMaximumWeight();
        }

        return false;
    }

    /**
     * Calculates the current total weight of entries in the cache.
     * Default implementation returns size as weight.
     * Subclasses should override if they track weight separately.
     *
     * @return the current total weight
     */
    protected long getCurrentWeight() {
        return size(); // Default implementation
    }

    /**
     * Hook for subclasses to perform size enforcement.
     * Called after operations that might exceed size limits.
     */
    protected void enforceSize() {
        // Default implementation does nothing
        // Override in subclasses for specific size enforcement
    }
}
