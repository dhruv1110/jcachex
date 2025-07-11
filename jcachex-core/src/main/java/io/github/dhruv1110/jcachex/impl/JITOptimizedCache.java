package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEntry;
import io.github.dhruv1110.jcachex.CacheStats;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.AbstractMap;
import java.util.concurrent.CompletableFuture;
import java.time.Instant;
import java.time.Duration;

/**
 * JIT-optimized cache implementation focused on hot path performance.
 *
 * Key optimizations:
 * - Method inlining hints via small methods
 * - Monomorphic call sites
 * - Branch prediction optimization
 * - Minimal object allocation in hot paths
 * - Cache-friendly data structures
 */
public final class JITOptimizedCache<K, V> implements Cache<K, V> {

    // Core data structures - final for JIT optimization
    private final ConcurrentHashMap<K, OptimizedEntry<V>> data;
    private final AtomicLong hits;
    private final AtomicLong misses;
    private final AtomicLong size;
    private final long maxSize;
    private final boolean statsEnabled;

    // Hot path optimization - separate read/write paths
    private final ReadPath<K, V> readPath;
    private final WritePath<K, V> writePath;

    // Entry eviction strategy - simple LRU for JIT optimization
    private final AtomicReference<OptimizedEntry<V>> lruHead;
    private final AtomicReference<OptimizedEntry<V>> lruTail;

    public JITOptimizedCache(CacheConfig<K, V> config) {
        this.maxSize = config.getMaximumSize();
        this.statsEnabled = config.isRecordStats();

        // Initialize with power-of-2 size for better hash distribution
        int initialCapacity = Math.max(16, Integer.highestOneBit((int) maxSize * 2));
        this.data = new ConcurrentHashMap<>(initialCapacity, 0.75f, 16);

        this.hits = new AtomicLong(0);
        this.misses = new AtomicLong(0);
        this.size = new AtomicLong(0);

        // Initialize separate hot paths
        this.readPath = new ReadPath<>(this);
        this.writePath = new WritePath<>(this);

        // Initialize LRU chain
        this.lruHead = new AtomicReference<>(null);
        this.lruTail = new AtomicReference<>(null);
    }

    @Override
    public final V get(K key) {
        // Hot path optimization - delegate to specialized read path
        return readPath.get(key);
    }

    @Override
    public final void put(K key, V value) {
        // Hot path optimization - delegate to specialized write path
        writePath.put(key, value);
    }

    @Override
    public final V remove(K key) {
        return writePath.remove(key);
    }

    @Override
    public final void clear() {
        data.clear();
        size.set(0);
        lruHead.set(null);
        lruTail.set(null);
    }

    @Override
    public final long size() {
        return size.get();
    }

    @Override
    public final boolean containsKey(K key) {
        return data.containsKey(key);
    }

    @Override
    public final CacheStats stats() {
        CacheStats stats = new CacheStats();
        stats.getHitCount().set(hits.get());
        stats.getMissCount().set(misses.get());
        return stats;
    }

    @Override
    public final CacheConfig<K, V> config() {
        return CacheConfig.<K, V>builder()
                .maximumSize(maxSize)
                .recordStats(statsEnabled)
                .build();
    }

    @Override
    public final Set<K> keys() {
        return data.keySet();
    }

    @Override
    public final Collection<V> values() {
        return data.values().stream()
                .map(OptimizedEntry::getValue)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public final Set<Map.Entry<K, V>> entries() {
        return data.entrySet().stream()
                .map(e -> (Map.Entry<K, V>) new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getValue()))
                .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public final CompletableFuture<V> getAsync(K key) {
        return CompletableFuture.completedFuture(get(key));
    }

    @Override
    public final CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.runAsync(() -> put(key, value));
    }

    @Override
    public final CompletableFuture<V> removeAsync(K key) {
        return CompletableFuture.completedFuture(remove(key));
    }

    @Override
    public final CompletableFuture<Void> clearAsync() {
        return CompletableFuture.runAsync(this::clear);
    }

    // Specialized read path for JIT optimization
    private static final class ReadPath<K, V> {
        private final JITOptimizedCache<K, V> cache;

        ReadPath(JITOptimizedCache<K, V> cache) {
            this.cache = cache;
        }

        // Inline-friendly hot path
        final V get(K key) {
            if (key == null)
                return null;

            // Single hash lookup - monomorphic call site
            OptimizedEntry<V> entry = cache.data.get(key);

            if (entry == null) {
                // Branch prediction: misses are less common
                if (cache.statsEnabled) {
                    cache.misses.incrementAndGet();
                }
                return null;
            }

            // Fast path for non-expired entries
            if (!entry.isExpired()) {
                // Update access time without allocation
                entry.updateAccessTime();

                // Update LRU position
                updateLRUPosition(entry);

                if (cache.statsEnabled) {
                    cache.hits.incrementAndGet();
                }
                return entry.getValue();
            }

            // Slow path for expired entries
            return handleExpiredEntry(key, entry);
        }

        private final V handleExpiredEntry(K key, OptimizedEntry<V> entry) {
            // Remove expired entry
            cache.data.remove(key, entry);
            cache.size.decrementAndGet();

            if (cache.statsEnabled) {
                cache.misses.incrementAndGet();
            }
            return null;
        }

        private final void updateLRUPosition(OptimizedEntry<V> entry) {
            // Lock-free LRU update using CAS
            OptimizedEntry<V> currentHead = cache.lruHead.get();
            if (currentHead != entry) {
                entry.next.set(currentHead);
                if (currentHead != null) {
                    currentHead.prev.set(entry);
                }
                cache.lruHead.set(entry);
            }
        }
    }

    // Specialized write path for JIT optimization
    private static final class WritePath<K, V> {
        private final JITOptimizedCache<K, V> cache;

        WritePath(JITOptimizedCache<K, V> cache) {
            this.cache = cache;
        }

        final void put(K key, V value) {
            if (key == null || value == null)
                return;

            OptimizedEntry<V> newEntry = new OptimizedEntry<>(value);
            OptimizedEntry<V> existing = cache.data.put(key, newEntry);

            if (existing == null) {
                // New entry
                long currentSize = cache.size.incrementAndGet();

                // Add to LRU head
                addToLRUHead(newEntry);

                // Check if eviction needed
                if (currentSize > cache.maxSize) {
                    evictLRU();
                }
            } else {
                // Update existing entry
                updateLRUPosition(newEntry);
            }
        }

        final V remove(K key) {
            if (key == null)
                return null;

            OptimizedEntry<V> removed = cache.data.remove(key);
            if (removed != null) {
                cache.size.decrementAndGet();
                removeFromLRU(removed);
                return removed.getValue();
            }
            return null;
        }

        private final void addToLRUHead(OptimizedEntry<V> entry) {
            OptimizedEntry<V> currentHead = cache.lruHead.get();
            entry.next.set(currentHead);
            if (currentHead != null) {
                currentHead.prev.set(entry);
            } else {
                cache.lruTail.set(entry);
            }
            cache.lruHead.set(entry);
        }

        private final void updateLRUPosition(OptimizedEntry<V> entry) {
            // Move to head for recently accessed
            removeFromLRU(entry);
            addToLRUHead(entry);
        }

        private final void removeFromLRU(OptimizedEntry<V> entry) {
            OptimizedEntry<V> prev = entry.prev.get();
            OptimizedEntry<V> next = entry.next.get();

            if (prev != null) {
                prev.next.set(next);
            } else {
                cache.lruHead.set(next);
            }

            if (next != null) {
                next.prev.set(prev);
            } else {
                cache.lruTail.set(prev);
            }

            entry.prev.set(null);
            entry.next.set(null);
        }

        private final void evictLRU() {
            OptimizedEntry<V> tail = cache.lruTail.get();
            if (tail != null) {
                // Find the key to remove - requires reverse lookup
                K keyToRemove = findKeyForEntry(tail);
                if (keyToRemove != null) {
                    cache.data.remove(keyToRemove, tail);
                    cache.size.decrementAndGet();
                    removeFromLRU(tail);
                }
            }
        }

        private final K findKeyForEntry(OptimizedEntry<V> entry) {
            // Reverse lookup - not optimal, but needed for LRU
            for (Map.Entry<K, OptimizedEntry<V>> mapEntry : cache.data.entrySet()) {
                if (mapEntry.getValue() == entry) {
                    return mapEntry.getKey();
                }
            }
            return null;
        }
    }

    // Memory-optimized entry with minimal allocation
    private static final class OptimizedEntry<V> {
        private final V value;
        private final long creationTime;
        private volatile long accessTime;
        private final AtomicReference<OptimizedEntry<V>> next;
        private final AtomicReference<OptimizedEntry<V>> prev;

        OptimizedEntry(V value) {
            this.value = value;
            this.creationTime = System.currentTimeMillis();
            this.accessTime = creationTime;
            this.next = new AtomicReference<>();
            this.prev = new AtomicReference<>();
        }

        final V getValue() {
            return value;
        }

        final void updateAccessTime() {
            accessTime = System.currentTimeMillis();
        }

        final boolean isExpired() {
            // No expiration by default for performance
            return false;
        }

        final long getAccessTime() {
            return accessTime;
        }

        final long getCreationTime() {
            return creationTime;
        }
    }
}
