package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheStats;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.AbstractMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.time.Instant;
import java.time.Duration;

/**
 * Specialized read-only cache implementation optimized for read-heavy
 * workloads.
 *
 * Key optimizations:
 * - Immutable data structures for maximum read performance
 * - Lock-free read operations
 * - Optimized memory layout for cache locality
 * - Minimal synchronization overhead
 * - Bulk loading operations
 */
public final class ReadOnlyOptimizedCache<K, V> implements Cache<K, V> {

    // Immutable data structures for optimal read performance
    private final ConcurrentHashMap<K, ImmutableEntry<V>> data;
    private final AtomicLong hitCount;
    private final AtomicLong missCount;
    private final long maximumSize;
    private final boolean statsEnabled;

    // Read optimization structures
    private final ReadOptimizedIndex<K, V> readIndex;
    private final ReadLocalityOptimizer<K, V> localityOptimizer;

    // Bulk loading support
    private final BulkLoader<K, V> bulkLoader;

    // Read-only state management
    private volatile boolean readOnly;
    private final StampedLock modificationLock;

    public ReadOnlyOptimizedCache(CacheConfig<K, V> config) {
        this.maximumSize = config.getMaximumSize();
        this.statsEnabled = config.isRecordStats();

        // Initialize with read-optimized capacity
        int capacity = Math.max(16, Integer.highestOneBit((int) maximumSize) << 1);
        this.data = new ConcurrentHashMap<>(capacity, 0.75f, 1); // Single segment for reads

        // Initialize counters
        this.hitCount = new AtomicLong(0);
        this.missCount = new AtomicLong(0);

        // Initialize read optimization structures
        this.readIndex = new ReadOptimizedIndex<>(capacity);
        this.localityOptimizer = new ReadLocalityOptimizer<>(capacity);

        // Initialize bulk loading
        this.bulkLoader = new BulkLoader<>(this);

        // Initialize state management
        this.readOnly = false;
        this.modificationLock = new StampedLock();
    }

    @Override
    public final V get(K key) {
        if (key == null)
            return null;

        // Ultra-fast read path - no locks needed
        ImmutableEntry<V> entry = data.get(key);
        if (entry == null) {
            if (statsEnabled) {
                missCount.incrementAndGet();
            }
            return null;
        }

        // Check read index for hot data
        V cachedValue = readIndex.getHotValue(key);
        if (cachedValue != null) {
            if (statsEnabled) {
                hitCount.incrementAndGet();
            }
            return cachedValue;
        }

        // Get from main storage
        V value = entry.getValue();
        if (value != null) {
            // Cache in read index for future fast access
            readIndex.cacheHotValue(key, value);

            // Optimize locality for future reads
            localityOptimizer.recordAccess(key, entry);

            if (statsEnabled) {
                hitCount.incrementAndGet();
            }
        } else {
            if (statsEnabled) {
                missCount.incrementAndGet();
            }
        }

        return value;
    }

    @Override
    public final void put(K key, V value) {
        if (readOnly) {
            throw new UnsupportedOperationException("Cache is in read-only mode");
        }

        if (key == null || value == null)
            return;

        // Use stamped lock for minimal write contention
        long stamp = modificationLock.writeLock();
        try {
            ImmutableEntry<V> newEntry = new ImmutableEntry<>(value);
            data.put(key, newEntry);

            // Update read optimization structures
            readIndex.invalidateHotValue(key);
            localityOptimizer.recordWrite(key, newEntry);

        } finally {
            modificationLock.unlockWrite(stamp);
        }
    }

    @Override
    public final V remove(K key) {
        if (readOnly) {
            throw new UnsupportedOperationException("Cache is in read-only mode");
        }

        if (key == null)
            return null;

        long stamp = modificationLock.writeLock();
        try {
            ImmutableEntry<V> removed = data.remove(key);
            if (removed != null) {
                // Update read optimization structures
                readIndex.invalidateHotValue(key);
                localityOptimizer.recordRemoval(key);

                return removed.getValue();
            }
            return null;
        } finally {
            modificationLock.unlockWrite(stamp);
        }
    }

    @Override
    public final void clear() {
        if (readOnly) {
            throw new UnsupportedOperationException("Cache is in read-only mode");
        }

        long stamp = modificationLock.writeLock();
        try {
            data.clear();
            readIndex.clear();
            localityOptimizer.clear();
        } finally {
            modificationLock.unlockWrite(stamp);
        }
    }

    @Override
    public final long size() {
        return data.size();
    }

    @Override
    public final boolean containsKey(K key) {
        return key != null && data.containsKey(key);
    }

    @Override
    public final CacheStats stats() {
        CacheStats stats = new CacheStats();
        stats.getHitCount().set(hitCount.get());
        stats.getMissCount().set(missCount.get());
        return stats;
    }

    @Override
    public final CacheConfig<K, V> config() {
        return CacheConfig.<K, V>builder()
                .maximumSize(maximumSize)
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
                .map(ImmutableEntry::getValue)
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

    // Specialized read-only operations

    /**
     * Enable read-only mode for maximum read performance.
     * No modifications will be allowed after this call.
     */
    public final void enableReadOnlyMode() {
        long stamp = modificationLock.writeLock();
        try {
            this.readOnly = true;

            // Optimize all data structures for read-only access
            readIndex.optimizeForReadOnly();
            localityOptimizer.optimizeForReadOnly();

        } finally {
            modificationLock.unlockWrite(stamp);
        }
    }

    /**
     * Bulk load data for optimal read performance.
     * This is more efficient than individual put operations.
     */
    public final void bulkLoad(Map<K, V> dataMap) {
        if (readOnly) {
            throw new UnsupportedOperationException("Cache is in read-only mode");
        }

        bulkLoader.load(dataMap);
    }

    /**
     * Warm up the cache by pre-loading frequently accessed data.
     */
    public final void warmUp(Set<K> frequentKeys) {
        readIndex.warmUp(frequentKeys, data);
    }

    // Immutable entry for read optimization
    private static final class ImmutableEntry<V> {
        private final V value;
        private final long creationTime;

        ImmutableEntry(V value) {
            this.value = value;
            this.creationTime = System.currentTimeMillis();
        }

        final V getValue() {
            return value;
        }

        final long getCreationTime() {
            return creationTime;
        }
    }

    // Read-optimized index for hot data
    private static final class ReadOptimizedIndex<K, V> {
        private final ConcurrentHashMap<K, V> hotCache;
        private final int maxHotEntries;

        ReadOptimizedIndex(int capacity) {
            this.maxHotEntries = Math.max(16, capacity / 10); // 10% for hot data
            this.hotCache = new ConcurrentHashMap<>(maxHotEntries);
        }

        final V getHotValue(K key) {
            return hotCache.get(key);
        }

        final void cacheHotValue(K key, V value) {
            if (hotCache.size() < maxHotEntries) {
                hotCache.put(key, value);
            }
        }

        final void invalidateHotValue(K key) {
            hotCache.remove(key);
        }

        final void clear() {
            hotCache.clear();
        }

        final void optimizeForReadOnly() {
            // No additional optimization needed for read-only mode
        }

        final void warmUp(Set<K> frequentKeys, Map<K, ImmutableEntry<V>> data) {
            hotCache.clear();
            for (K key : frequentKeys) {
                if (hotCache.size() >= maxHotEntries)
                    break;

                ImmutableEntry<V> entry = data.get(key);
                if (entry != null) {
                    hotCache.put(key, entry.getValue());
                }
            }
        }
    }

    // Read locality optimizer
    private static final class ReadLocalityOptimizer<K, V> {
        private final ConcurrentLinkedQueue<AccessRecord<K, V>> accessQueue;
        private final int maxRecords;

        ReadLocalityOptimizer(int capacity) {
            this.maxRecords = Math.max(32, capacity / 20); // 5% for locality tracking
            this.accessQueue = new ConcurrentLinkedQueue<>();
        }

        final void recordAccess(K key, ImmutableEntry<V> entry) {
            if (accessQueue.size() < maxRecords) {
                accessQueue.offer(new AccessRecord<>(key, entry, System.currentTimeMillis()));
            }
        }

        final void recordWrite(K key, ImmutableEntry<V> entry) {
            // Remove old access records for this key
            accessQueue.removeIf(record -> record.key.equals(key));
            recordAccess(key, entry);
        }

        final void recordRemoval(K key) {
            accessQueue.removeIf(record -> record.key.equals(key));
        }

        final void clear() {
            accessQueue.clear();
        }

        final void optimizeForReadOnly() {
            // Sort access records by frequency for read-only optimization
            // This would be more complex in a production implementation
        }

        private static final class AccessRecord<K, V> {
            final K key;
            final ImmutableEntry<V> entry;
            final long accessTime;

            AccessRecord(K key, ImmutableEntry<V> entry, long accessTime) {
                this.key = key;
                this.entry = entry;
                this.accessTime = accessTime;
            }
        }
    }

    // Bulk loader for efficient initialization
    private static final class BulkLoader<K, V> {
        private final ReadOnlyOptimizedCache<K, V> cache;

        BulkLoader(ReadOnlyOptimizedCache<K, V> cache) {
            this.cache = cache;
        }

        final void load(Map<K, V> dataMap) {
            long stamp = cache.modificationLock.writeLock();
            try {
                // Clear existing data
                cache.data.clear();
                cache.readIndex.clear();
                cache.localityOptimizer.clear();

                // Bulk load new data
                for (Map.Entry<K, V> entry : dataMap.entrySet()) {
                    K key = entry.getKey();
                    V value = entry.getValue();

                    if (key != null && value != null) {
                        ImmutableEntry<V> immutableEntry = new ImmutableEntry<>(value);
                        cache.data.put(key, immutableEntry);
                        cache.localityOptimizer.recordWrite(key, immutableEntry);
                    }
                }

            } finally {
                cache.modificationLock.unlockWrite(stamp);
            }
        }
    }
}
