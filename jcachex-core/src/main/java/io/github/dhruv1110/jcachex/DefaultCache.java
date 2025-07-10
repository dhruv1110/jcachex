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
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
 * <p>
 * <strong>Performance Optimizations:</strong>
 * This implementation uses striped locking to reduce contention, optimized
 * expiration checks using nanoTime, and minimizes object allocation in hot
 * paths.
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

    // Striped locking for better concurrency
    private static final int STRIPE_COUNT = 32; // Power of 2 for efficient modulo
    private final ReentrantReadWriteLock[] stripes = new ReentrantReadWriteLock[STRIPE_COUNT];

    // Cached configuration values for hot path optimization
    private final Long maximumSize;
    private final Long maximumWeight;
    private final boolean hasExpiration;

    private static final long REFRESH_INTERVAL_SECONDS = 1L;

    /**
     * Creates a new DefaultCache with the specified configuration.
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

        // Initialize striped locks
        for (int i = 0; i < STRIPE_COUNT; i++) {
            stripes[i] = new ReentrantReadWriteLock();
        }

        // Cache configuration values for performance
        this.maximumSize = config.getMaximumSize();
        this.maximumWeight = config.getMaximumWeight();
        this.hasExpiration = config.getExpireAfterWrite() != null || config.getExpireAfterAccess() != null;

        if (config.getRefreshAfterWrite() != null) {
            scheduleRefresh();
        }
    }

    /**
     * Get the stripe index for a key to enable striped locking
     */
    private int getStripeIndex(K key) {
        return Math.abs(key.hashCode()) & (STRIPE_COUNT - 1);
    }

    /**
     * Get read lock for a specific key
     */
    private ReentrantReadWriteLock.ReadLock getReadLock(K key) {
        return stripes[getStripeIndex(key)].readLock();
    }

    /**
     * Get write lock for a specific key
     */
    private ReentrantReadWriteLock.WriteLock getWriteLock(K key) {
        return stripes[getStripeIndex(key)].writeLock();
    }

    /**
     * Optimized get operation with minimal overhead
     */
    @Override
    public V get(K key) {
        if (key == null) {
            return null;
        }

        CacheEntry<V> entry = entries.get(key);
        if (entry != null) {
            // Fast expiration check using nanoTime
            if (hasExpiration && entry.isExpired()) {
                // Use striped locking for removal
                ReentrantReadWriteLock.WriteLock lock = getWriteLock(key);
                lock.lock();
                try {
                    // Double-check after acquiring lock
                    entry = entries.get(key);
                    if (entry != null && entry.isExpired()) {
                        entries.remove(key);
                        evictionStrategy.remove(key);
                        stats.recordMiss();
                        return loadValue(key);
                    }
                } finally {
                    lock.unlock();
                }
            }

            if (entry != null) {
                // Update access information
                entry.incrementAccessCount();
                evictionStrategy.update(key, entry);
                stats.recordHit();
                return entry.getValue();
            }
        }

        stats.recordMiss();
        return loadValue(key);
    }

    /**
     * Optimized put operation with striped locking
     */
    @Override
    public void put(K key, V value) {
        if (key == null) {
            return;
        }

        CacheEntry<V> entry = createEntry(value);

        ReentrantReadWriteLock.WriteLock lock = getWriteLock(key);
        lock.lock();
        try {
            CacheEntry<V> oldEntry = entries.put(key, entry);
            if (oldEntry != null) {
                notifyListeners(listener -> listener.onRemove(key, oldEntry.getValue()));
            }
            notifyListeners(listener -> listener.onPut(key, value));
            evictionStrategy.update(key, entry);
        } finally {
            lock.unlock();
        }

        // Check eviction outside of striped lock to avoid deadlock
        evictIfNeeded();
    }

    @Override
    public V remove(K key) {
        if (key == null) {
            return null;
        }

        ReentrantReadWriteLock.WriteLock lock = getWriteLock(key);
        lock.lock();
        try {
            CacheEntry<V> entry = entries.remove(key);
            if (entry != null) {
                notifyListeners(listener -> listener.onRemove(key, entry.getValue()));
                evictionStrategy.remove(key);
                return entry.getValue();
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public void clear() {
        // Acquire all write locks to ensure consistency
        for (ReentrantReadWriteLock stripe : stripes) {
            stripe.writeLock().lock();
        }
        try {
            entries.clear();
            evictionStrategy.clear();
            notifyListeners(CacheEventListener::onClear);
        } finally {
            for (int i = stripes.length - 1; i >= 0; i--) {
                stripes[i].writeLock().unlock();
            }
        }
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
        CacheEntry<V> entry = entries.get(key);
        if (entry != null && hasExpiration && entry.isExpired()) {
            // Clean up expired entry
            ReentrantReadWriteLock.WriteLock lock = getWriteLock(key);
            lock.lock();
            try {
                entry = entries.get(key);
                if (entry != null && entry.isExpired()) {
                    entries.remove(key);
                    evictionStrategy.remove(key);
                    return false;
                }
            } finally {
                lock.unlock();
            }
        }
        return entry != null;
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
        Instant expirationTime = null;
        if (config.getExpireAfterWrite() != null) {
            expirationTime = Instant.now().plus(config.getExpireAfterWrite());
        } else if (config.getExpireAfterAccess() != null) {
            expirationTime = Instant.now().plus(config.getExpireAfterAccess());
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
        if (maximumSize != null && entries.size() > maximumSize) {
            evict(EvictionReason.SIZE);
        }

        if (maximumWeight != null) {
            long totalWeight = entries.values().stream()
                    .mapToLong(CacheEntry::getWeight)
                    .sum();
            if (totalWeight > maximumWeight) {
                evict(EvictionReason.WEIGHT);
            }
        }
    }

    private void evict(EvictionReason reason) {
        K candidate = (K) evictionStrategy.selectEvictionCandidate(entries);
        if (candidate != null) {
            ReentrantReadWriteLock.WriteLock lock = getWriteLock(candidate);
            lock.lock();
            try {
                CacheEntry<V> entry = entries.remove(candidate);
                if (entry != null) {
                    evictionStrategy.remove(candidate);
                    stats.recordEviction();
                    notifyListeners(listener -> listener.onEvict(candidate, entry.getValue(), reason));
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void scheduleRefresh() {
        scheduler.scheduleAtFixedRate(() -> {
            long currentTimeNanos = System.nanoTime();
            entries.forEach((key, entry) -> {
                if (entry.isExpired()) {
                    ReentrantReadWriteLock.WriteLock lock = getWriteLock(key);
                    lock.lock();
                    try {
                        CacheEntry<V> currentEntry = entries.get(key);
                        if (currentEntry != null && currentEntry.isExpired()) {
                            entries.remove(key);
                            evictionStrategy.remove(key);
                            notifyListeners(listener -> listener.onExpire(key, currentEntry.getValue()));
                        }
                    } finally {
                        lock.unlock();
                    }
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
