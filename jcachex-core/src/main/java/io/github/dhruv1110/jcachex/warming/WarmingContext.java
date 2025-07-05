package io.github.dhruv1110.jcachex.warming;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheStats;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Context for cache warming operations.
 * <p>
 * This interface provides warming strategies with access to the cache and
 * additional context needed for intelligent warming decisions.
 * </p>
 *
 * <h3>Usage Example:</h3>
 *
 * <pre>
 * {
 *     &#64;code
 *     CacheWarmingStrategy<String, User> strategy = new MyWarmingStrategy<>();
 *
 *     strategy.warmCache(
 *             new WarmingContext<String, User>() {
 *                 &#64;Override
 *                 public Cache<String, User> getCache() {
 *                     return userCache;
 *                 }
 *
 *                 @Override
 *                 public Set<String> getFrequentlyAccessedKeys() {
 *                     return recentlyAccessedKeys;
 *                 }
 *             },
 *             userService::loadUser);
 * }
 * </pre>
 *
 * @param <K> the type of cache keys
 * @param <V> the type of cache values
 * @since 1.0.0
 */
public interface WarmingContext<K, V> {

    /**
     * Returns the cache to be warmed.
     *
     * @return the cache instance
     */
    Cache<K, V> getCache();

    /**
     * Returns keys that are frequently accessed.
     * <p>
     * This can be used by warming strategies to prioritize which keys to warm.
     * The implementation should return keys that are most likely to be accessed
     * in the near future.
     * </p>
     *
     * @return set of frequently accessed keys
     */
    default Set<K> getFrequentlyAccessedKeys() {
        return Collections.emptySet();
    }

    /**
     * Returns access patterns for cache keys.
     * <p>
     * This provides detailed access statistics that can be used for predictive
     * warming strategies.
     * </p>
     *
     * @return map of keys to their access patterns
     */
    default Map<K, AccessPattern> getAccessPatterns() {
        return Collections.emptyMap();
    }

    /**
     * Returns the current cache statistics.
     *
     * @return current cache statistics
     */
    default CacheStats getCurrentStats() {
        return getCache().stats();
    }

    /**
     * Checks if a key should be warmed based on context.
     * <p>
     * This allows the context to provide hints about which keys are worth
     * warming based on application-specific logic.
     * </p>
     *
     * @param key the key to check
     * @return true if the key should be warmed
     */
    default boolean shouldWarm(K key) {
        return true;
    }

    /**
     * Warms a single key-value pair.
     * <p>
     * This method allows the warming strategy to delegate the actual warming
     * operation to the context, which may have additional logic for handling
     * the warming process.
     * </p>
     *
     * @param key   the key to warm
     * @param value the value to cache
     * @return CompletableFuture that completes when warming is done
     */
    default CompletableFuture<Void> warm(K key, V value) {
        return CompletableFuture.runAsync(() -> getCache().put(key, value));
    }

    /**
     * Warms multiple key-value pairs.
     * <p>
     * This method allows for bulk warming operations that may be more efficient
     * than warming keys individually.
     * </p>
     *
     * @param entries the entries to warm
     * @return CompletableFuture that completes when all warming is done
     */
    default CompletableFuture<Void> warmBulk(Map<K, V> entries) {
        return CompletableFuture.runAsync(() -> {
            Cache<K, V> cache = getCache();
            for (Map.Entry<K, V> entry : entries.entrySet()) {
                cache.put(entry.getKey(), entry.getValue());
            }
        });
    }

    /**
     * Represents access patterns for a cache key.
     */
    class AccessPattern {
        private final long accessCount;
        private final long lastAccessTime;
        private final double accessFrequency;
        private final boolean isHotSpot;

        public AccessPattern(long accessCount, long lastAccessTime, double accessFrequency, boolean isHotSpot) {
            this.accessCount = accessCount;
            this.lastAccessTime = lastAccessTime;
            this.accessFrequency = accessFrequency;
            this.isHotSpot = isHotSpot;
        }

        public long getAccessCount() {
            return accessCount;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }

        public double getAccessFrequency() {
            return accessFrequency;
        }

        public boolean isHotSpot() {
            return isHotSpot;
        }
    }
}
