package io.github.dhruv1110.jcachex;

import io.github.dhruv1110.jcachex.eviction.EvictionStrategy;
import io.github.dhruv1110.jcachex.eviction.LRUEvictionStrategy;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Default implementation of the Cache interface.
 * <p>
 * This implementation is thread-safe and supports various cache configurations
 * including eviction strategies, expiration policies, and event listeners.
 * It provides both synchronous and asynchronous operations for maximum
 * flexibility.
 * </p>
 *
 * <h3>Basic Usage Examples:</h3>
 *
 * <pre>{@code
 * // Create a simple cache with size limit
 * CacheConfig<String, String> config = CacheConfig.<String, String>builder()
 *         .maximumSize(1000L)
 *         .build();
 * Cache<String, String> cache = new DefaultCache<>(config);
 *
 * // Basic operations
 * cache.put("key1", "value1");
 * String value = cache.get("key1"); // Returns "value1"
 * cache.remove("key1");
 * }</pre>
 *
 * <h3>Advanced Usage Examples:</h3>
 *
 * <pre>{@code
 * // Cache with automatic loading and expiration
 * CacheConfig<String, User> userConfig = CacheConfig.<String, User>builder()
 *         .maximumSize(500L)
 *         .expireAfterWrite(Duration.ofMinutes(30))
 *         .loader(userId -> userService.findById(userId))
 *         .recordStats(true)
 *         .addListener(new CacheEventListener<String, User>() { @Override
 *             public void onEvict(String key, User value, EvictionReason reason) {
 *                 logger.info("User {} evicted due to {}", key, reason);
 *             }
 *             // ... other methods
 *         })
 *         .build();
 *
 * try (DefaultCache<String, User> userCache = new DefaultCache<>(userConfig)) {
 *     // Cache will automatically load users when not present
 *     User user = userCache.get("user123"); // Calls loader if not cached
 *
 *     // Async operations
 *     CompletableFuture<User> futureUser = userCache.getAsync("user456");
 *
 *     // Statistics
 *     CacheStats stats = userCache.stats();
 *     System.out.println("Hit rate: " + stats.hitRate());
 * } // Cache is properly closed, releasing resources
 * }</pre>
 *
 * <h3>Thread Safety:</h3>
 * <p>
 * This implementation is fully thread-safe. All operations can be called
 * concurrently from multiple threads without external synchronization.
 * </p>
 *
 * <h3>Resource Management:</h3>
 * <p>
 * The cache implements {@link AutoCloseable} and should be closed when no
 * longer
 * needed to release background threads and other resources. This is
 * particularly
 * important when using features like automatic refresh.
 * </p>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @see Cache
 * @see CacheConfig
 * @see CacheEventListener
 * @since 1.0.0
 */
public class DefaultCache<K, V> implements Cache<K, V>, AutoCloseable {
    private final CacheConfig<K, V> config;
    private final ConcurrentHashMap<K, CacheEntry<V>> entries;
    private final CacheStats stats;
    private final EvictionStrategy<K, V> evictionStrategy;
    private final ScheduledExecutorService scheduler;
    private static final long REFRESH_INTERVAL_SECONDS = 1L;

    /**
     * Creates a new DefaultCache with the specified configuration.
     * <p>
     * The cache will be initialized with the settings specified in the
     * configuration,
     * including size limits, eviction strategies, expiration policies, and event
     * listeners.
     * Background tasks for refresh and expiration will be started if configured.
     * </p>
     *
     * <h3>Example:</h3>
     *
     * <pre>{@code
     * CacheConfig<String, String> config = CacheConfig.<String, String>builder()
     *         .maximumSize(1000L)
     *         .expireAfterWrite(Duration.ofMinutes(30))
     *         .recordStats(true)
     *         .build();
     *
     * DefaultCache<String, String> cache = new DefaultCache<>(config);
     * }</pre>
     *
     * @param config the cache configuration to use, must not be null
     * @throws IllegalArgumentException if config is null
     * @see CacheConfig
     */
    public DefaultCache(CacheConfig<K, V> config) {
        if (config == null) {
            throw new IllegalArgumentException("Cache configuration cannot be null");
        }

        this.config = config;
        this.entries = new ConcurrentHashMap<>();
        this.stats = new CacheStats();
        this.evictionStrategy = config.getEvictionStrategy() != null ? config.getEvictionStrategy()
                : new LRUEvictionStrategy<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "jcachex-scheduler");
            thread.setDaemon(true);
            return thread;
        });

        if (config.getRefreshAfterWrite() != null) {
            scheduleRefresh();
        }
    }

    /**
     * Returns the value associated with the key in this cache, or null if there is
     * no cached value for the key.
     * <p>
     * If the key is not present and a loader is configured, this method will
     * attempt
     * to load the value using the loader function. If the cached entry has expired,
     * it will be automatically removed and treated as a cache miss.
     * </p>
     *
     * <h3>Examples:</h3>
     *
     * <pre>{@code
     * // Simple get operation
     * String value = cache.get("myKey");
     * if (value != null) {
     *     // Key was found in cache
     *     processValue(value);
     * }
     *
     * // With automatic loading (if loader is configured)
     * User user = userCache.get("userId123"); // May trigger load from database
     * }</pre>
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this cache
     *         contains no mapping for the key
     * @throws RuntimeException if the loader function throws an exception
     */
    @Override
    public V get(K key) {
        if (key == null) {
            return null;
        }

        CacheEntry<V> entry = entries.get(key);
        if (entry != null) {
            if (entry.isExpired()) {
                remove(key);
                stats.recordMiss();
                return null;
            }
            entry.incrementAccessCount();
            evictionStrategy.update(key, entry);
            stats.recordHit();
            return entry.getValue();
        }

        stats.recordMiss();
        return loadValue(key);
    }

    /**
     * Associates the specified value with the specified key in this cache.
     * <p>
     * If the cache previously contained a mapping for the key, the old value is
     * replaced.
     * This operation may trigger eviction of other entries if the cache size or
     * weight
     * limits are exceeded. Event listeners will be notified of the put operation
     * and
     * any evictions that occur as a result.
     * </p>
     *
     * <h3>Examples:</h3>
     *
     * <pre>{@code
     * // Simple put operation
     * cache.put("user123", user);
     *
     * // Bulk loading
     * userIds.forEach(id -> cache.put(id, loadUser(id)));
     *
     * // Conditional put (manual check)
     * if (!cache.containsKey("expensiveComputation")) {
     *     cache.put("expensiveComputation", performExpensiveComputation());
     * }
     * }</pre>
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @throws NullPointerException if key is null (implementation dependent)
     */
    @Override
    public void put(K key, V value) {
        if (key == null) {
            return;
        }

        CacheEntry<V> entry = createEntry(value);
        CacheEntry<V> oldEntry = entries.put(key, entry);
        if (oldEntry != null) {
            notifyListeners(listener -> listener.onRemove(key, oldEntry.getValue()));
        }
        notifyListeners(listener -> listener.onPut(key, value));
        evictionStrategy.update(key, entry);
        evictIfNeeded();
    }

    @Override
    public V remove(K key) {
        if (key == null) {
            return null;
        }

        CacheEntry<V> entry = entries.remove(key);
        if (entry != null) {
            notifyListeners(listener -> listener.onRemove(key, entry.getValue()));
            evictionStrategy.remove(key);
        }
        return entry != null ? entry.getValue() : null;
    }

    @Override
    public void clear() {
        entries.clear();
        evictionStrategy.clear();
        notifyListeners(CacheEventListener::onClear);
    }

    @Override
    public long size() {
        return entries.size();
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            return false;
        }
        return entries.containsKey(key);
    }

    @Override
    public Set<K> keys() {
        return entries.keySet();
    }

    @Override
    public Collection<V> values() {
        return entries.values().stream()
                .map(CacheEntry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return entries.entrySet().stream()
                .map(e -> new Map.Entry<K, V>() {
                    @Override
                    public K getKey() {
                        return e.getKey();
                    }

                    @Override
                    public V getValue() {
                        return e.getValue().getValue();
                    }

                    @Override
                    public V setValue(V value) {
                        throw new UnsupportedOperationException();
                    }
                })
                .collect(Collectors.toSet());
    }

    @Override
    public CacheStats stats() {
        return stats.snapshot();
    }

    @Override
    public CompletableFuture<V> getAsync(K key) {
        return CompletableFuture.supplyAsync(() -> get(key));
    }

    @Override
    public CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.runAsync(() -> put(key, value));
    }

    @Override
    public CompletableFuture<V> removeAsync(K key) {
        return CompletableFuture.supplyAsync(() -> remove(key));
    }

    @Override
    public CompletableFuture<Void> clearAsync() {
        return CompletableFuture.runAsync(this::clear);
    }

    @Override
    public CacheConfig<K, V> config() {
        return config;
    }

    private CacheEntry<V> createEntry(V value) {
        Instant now = Instant.now();
        Instant expirationTime = null;
        if (config.getExpireAfterWrite() != null) {
            expirationTime = now.plus(config.getExpireAfterWrite());
        } else if (config.getExpireAfterAccess() != null) {
            expirationTime = now.plus(config.getExpireAfterAccess());
        }
        long weight = config.getWeigher() != null ? config.getWeigher().apply(null, value) : 1L;
        return new CacheEntry<>(value, weight, expirationTime);
    }

    private V loadValue(K key) {
        long startTime = System.nanoTime();
        try {
            V value;
            if (config.getAsyncLoader() != null) {
                value = config.getAsyncLoader().apply(key).get();
            } else if (config.getLoader() != null) {
                value = config.getLoader().apply(key);
            } else {
                value = null;
            }

            if (value != null) {
                put(key, value);
                stats.recordLoad(System.nanoTime() - startTime);
                notifyListeners(listener -> listener.onLoad(key, value));
                return value;
            }
        } catch (Exception e) {
            stats.recordLoadFailure();
            notifyListeners(listener -> listener.onLoadError(key, e));
        }
        return null;
    }

    private void evictIfNeeded() {
        if (config.getMaximumSize() != null && entries.size() > config.getMaximumSize()) {
            evict(EvictionReason.SIZE);
        }

        if (config.getMaximumWeight() != null) {
            long totalWeight = entries.values().stream()
                    .mapToLong(CacheEntry::getWeight)
                    .sum();
            if (totalWeight > config.getMaximumWeight()) {
                evict(EvictionReason.WEIGHT);
            }
        }
    }

    private void evict(EvictionReason reason) {
        K candidate = (K) evictionStrategy.selectEvictionCandidate(entries);
        if (candidate != null) {
            CacheEntry<V> entry = entries.remove(candidate);
            if (entry != null) {
                evictionStrategy.remove(candidate);
                stats.recordEviction();
                notifyListeners(listener -> listener.onEvict(candidate, entry.getValue(), reason));
            }
        }
    }

    private void scheduleRefresh() {
        scheduler.scheduleAtFixedRate(() -> {
            long currentTimeNanos = System.nanoTime();
            entries.forEach((key, entry) -> {
                if (entry.isExpired()) {
                    remove(key);
                    notifyListeners(listener -> listener.onExpire(key, entry.getValue()));
                } else if (config.getRefreshAfterWrite() != null) {
                    long refreshThresholdNanos = entry.getCreationTimeNanos() + config.getRefreshAfterWrite().toNanos();
                    if (currentTimeNanos > refreshThresholdNanos) {
                        CompletableFuture.runAsync(() -> loadValue(key));
                    }
                }
            });
        }, 0, REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void notifyListeners(java.util.function.Consumer<CacheEventListener<K, V>> action) {
        config.getListeners().forEach(action);
    }

    /**
     * Closes this cache and releases any resources associated with it.
     * This method should be called when the cache is no longer needed
     * to prevent resource leaks.
     */
    @Override
    public void close() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
