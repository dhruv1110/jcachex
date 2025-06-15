package io.github.dpflux.jcachex;

import io.github.dpflux.jcachex.eviction.EvictionStrategy;
import io.github.dpflux.jcachex.eviction.LRUEvictionStrategy;

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
 * This implementation is thread-safe and supports various cache configurations
 * including eviction strategies, expiration policies, and event listeners.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
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
     *
     * @param config the cache configuration to use
     * @throws IllegalArgumentException if config is null
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
            Instant now = Instant.now();
            entries.forEach((key, entry) -> {
                if (entry.isExpired()) {
                    remove(key);
                    notifyListeners(listener -> listener.onExpire(key, entry.getValue()));
                } else if (config.getRefreshAfterWrite() != null &&
                        entry.getCreationTime().plus(config.getRefreshAfterWrite()).isBefore(now)) {
                    CompletableFuture.runAsync(() -> loadValue(key));
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
