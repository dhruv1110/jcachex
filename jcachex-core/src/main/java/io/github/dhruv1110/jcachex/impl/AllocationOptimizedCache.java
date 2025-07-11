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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.time.Instant;
import java.time.Duration;

/**
 * Allocation-optimized cache implementation that minimizes object allocation
 * overhead.
 *
 * Key optimizations:
 * - Thread-local object pools for frequently created objects
 * - Eliminates boxing/unboxing in critical paths
 * - Reduces intermediate object creation
 * - Reuses objects instead of creating new ones
 * - Zero-allocation hot paths where possible
 */
public final class AllocationOptimizedCache<K, V> implements Cache<K, V> {

    // Core data structures
    private final ConcurrentHashMap<K, PooledEntry<V>> data;
    private final AtomicLong hitCount;
    private final AtomicLong missCount;
    private final AtomicLong currentSize;
    private final long maximumSize;
    private final boolean statsEnabled;

    // Object pools for allocation reduction
    private final ThreadLocal<EntryPool<V>> entryPools;
    private final ThreadLocal<OperationContext> operationPools;

    // Entry eviction with minimal allocation
    private final AtomicReference<PooledEntry<V>> evictionHead;
    private final AtomicReference<PooledEntry<V>> evictionTail;

    // Constants for pool sizing
    private static final int ENTRY_POOL_SIZE = 64;
    private static final int OPERATION_POOL_SIZE = 32;
    private static final int MAX_POOL_SIZE = 256;

    public AllocationOptimizedCache(CacheConfig<K, V> config) {
        this.maximumSize = config.getMaximumSize();
        this.statsEnabled = config.isRecordStats();

        // Initialize with optimal sizing
        int capacity = Math.max(16, Integer.highestOneBit((int) maximumSize) << 1);
        this.data = new ConcurrentHashMap<>(capacity, 0.75f, 16);

        // Initialize counters
        this.hitCount = new AtomicLong(0);
        this.missCount = new AtomicLong(0);
        this.currentSize = new AtomicLong(0);

        // Initialize object pools
        this.entryPools = ThreadLocal.withInitial(() -> new EntryPool<>(ENTRY_POOL_SIZE));
        this.operationPools = ThreadLocal.withInitial(() -> new OperationContext(OPERATION_POOL_SIZE));

        // Initialize eviction chain
        this.evictionHead = new AtomicReference<>(null);
        this.evictionTail = new AtomicReference<>(null);
    }

    @Override
    public final V get(K key) {
        if (key == null)
            return null;

        // Use pooled operation context to avoid allocation
        OperationContext ctx = operationPools.get();
        try {
            return getWithContext(key, ctx);
        } finally {
            ctx.reset();
        }
    }

    @Override
    public final void put(K key, V value) {
        if (key == null || value == null)
            return;

        // Use pooled operation context
        OperationContext ctx = operationPools.get();
        try {
            putWithContext(key, value, ctx);
        } finally {
            ctx.reset();
        }
    }

    @Override
    public final V remove(K key) {
        if (key == null)
            return null;

        OperationContext ctx = operationPools.get();
        try {
            return removeWithContext(key, ctx);
        } finally {
            ctx.reset();
        }
    }

    @Override
    public final void clear() {
        data.clear();
        currentSize.set(0);
        evictionHead.set(null);
        evictionTail.set(null);

        // Clear all thread-local pools
        entryPools.remove();
        operationPools.remove();
    }

    @Override
    public final long size() {
        return currentSize.get();
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
                .map(PooledEntry::getValue)
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

    // Zero-allocation get implementation
    private final V getWithContext(K key, OperationContext ctx) {
        PooledEntry<V> entry = data.get(key);

        if (entry == null) {
            // Miss - no allocation needed
            if (statsEnabled) {
                missCount.incrementAndGet();
            }
            return null;
        }

        // Check expiration without allocation
        if (entry.isExpiredAt(ctx.currentTime())) {
            // Remove expired entry
            if (data.remove(key, entry)) {
                currentSize.decrementAndGet();
                returnEntryToPool(entry);
            }

            if (statsEnabled) {
                missCount.incrementAndGet();
            }
            return null;
        }

        // Hit - update access time without allocation
        entry.updateAccessTime(ctx.currentTime());
        moveToEvictionHead(entry);

        if (statsEnabled) {
            hitCount.incrementAndGet();
        }

        return entry.getValue();
    }

    // Zero-allocation put implementation
    private final void putWithContext(K key, V value, OperationContext ctx) {
        // Get pooled entry to avoid allocation
        PooledEntry<V> newEntry = getEntryFromPool();
        newEntry.initialize(value, ctx.currentTime());

        PooledEntry<V> existing = data.put(key, newEntry);

        if (existing == null) {
            // New entry
            long size = currentSize.incrementAndGet();
            addToEvictionHead(newEntry);

            // Check if eviction needed
            if (size > maximumSize) {
                evictFromTail();
            }
        } else {
            // Update existing - return old entry to pool
            returnEntryToPool(existing);
            moveToEvictionHead(newEntry);
        }
    }

    // Zero-allocation remove implementation
    private final V removeWithContext(K key, OperationContext ctx) {
        PooledEntry<V> removed = data.remove(key);
        if (removed != null) {
            currentSize.decrementAndGet();
            removeFromEvictionChain(removed);

            V value = removed.getValue();
            returnEntryToPool(removed);
            return value;
        }
        return null;
    }

    // Object pool management
    private final PooledEntry<V> getEntryFromPool() {
        EntryPool<V> pool = entryPools.get();
        PooledEntry<V> entry = pool.acquire();
        return entry != null ? entry : new PooledEntry<>();
    }

    private final void returnEntryToPool(PooledEntry<V> entry) {
        EntryPool<V> pool = entryPools.get();
        entry.reset();
        pool.release(entry);
    }

    // Eviction chain management without allocation
    private final void addToEvictionHead(PooledEntry<V> entry) {
        PooledEntry<V> currentHead = evictionHead.get();
        entry.nextEviction = currentHead;
        entry.prevEviction = null;

        if (currentHead != null) {
            currentHead.prevEviction = entry;
        } else {
            evictionTail.set(entry);
        }

        evictionHead.set(entry);
    }

    private final void moveToEvictionHead(PooledEntry<V> entry) {
        // Remove from current position
        if (entry.prevEviction != null) {
            entry.prevEviction.nextEviction = entry.nextEviction;
        } else {
            evictionHead.set(entry.nextEviction);
        }

        if (entry.nextEviction != null) {
            entry.nextEviction.prevEviction = entry.prevEviction;
        } else {
            evictionTail.set(entry.prevEviction);
        }

        // Add to head
        addToEvictionHead(entry);
    }

    private final void removeFromEvictionChain(PooledEntry<V> entry) {
        if (entry.prevEviction != null) {
            entry.prevEviction.nextEviction = entry.nextEviction;
        } else {
            evictionHead.set(entry.nextEviction);
        }

        if (entry.nextEviction != null) {
            entry.nextEviction.prevEviction = entry.prevEviction;
        } else {
            evictionTail.set(entry.prevEviction);
        }

        entry.nextEviction = null;
        entry.prevEviction = null;
    }

    private final void evictFromTail() {
        PooledEntry<V> tail = evictionTail.get();
        if (tail != null) {
            // Find key for this entry - requires iteration but only on eviction
            K keyToEvict = findKeyForEntry(tail);
            if (keyToEvict != null) {
                data.remove(keyToEvict, tail);
                currentSize.decrementAndGet();
                removeFromEvictionChain(tail);
                returnEntryToPool(tail);
            }
        }
    }

    private final K findKeyForEntry(PooledEntry<V> entry) {
        // Only called during eviction, so performance impact is minimal
        for (Map.Entry<K, PooledEntry<V>> mapEntry : data.entrySet()) {
            if (mapEntry.getValue() == entry) {
                return mapEntry.getKey();
            }
        }
        return null;
    }

    // Memory-efficient entry with pooling support
    private static final class PooledEntry<V> {
        private V value;
        private long accessTime;
        private long creationTime;
        private long expirationTime;

        // Eviction chain pointers
        PooledEntry<V> nextEviction;
        PooledEntry<V> prevEviction;

        // Pool state
        private boolean inUse;

        PooledEntry() {
            this.inUse = false;
        }

        final void initialize(V value, long currentTime) {
            this.value = value;
            this.accessTime = currentTime;
            this.creationTime = currentTime;
            this.expirationTime = Long.MAX_VALUE; // No expiration by default
            this.inUse = true;
        }

        final void reset() {
            this.value = null;
            this.accessTime = 0;
            this.creationTime = 0;
            this.expirationTime = 0;
            this.nextEviction = null;
            this.prevEviction = null;
            this.inUse = false;
        }

        final V getValue() {
            return value;
        }

        final void updateAccessTime(long currentTime) {
            this.accessTime = currentTime;
        }

        final boolean isExpiredAt(long currentTime) {
            return currentTime > expirationTime;
        }

        final long getAccessTime() {
            return accessTime;
        }

        final boolean isInUse() {
            return inUse;
        }
    }

    // Thread-local object pool for entries
    private static final class EntryPool<V> {
        private final ConcurrentLinkedQueue<PooledEntry<V>> pool;
        private final int maxSize;
        private volatile int currentSize;

        EntryPool(int maxSize) {
            this.pool = new ConcurrentLinkedQueue<>();
            this.maxSize = maxSize;
            this.currentSize = 0;
        }

        final PooledEntry<V> acquire() {
            PooledEntry<V> entry = pool.poll();
            if (entry != null) {
                currentSize--;
            }
            return entry;
        }

        final void release(PooledEntry<V> entry) {
            if (entry != null && !entry.isInUse() && currentSize < maxSize) {
                pool.offer(entry);
                currentSize++;
            }
        }
    }

    // Operation context to avoid allocation of temporary objects
    private static final class OperationContext {
        private long cachedCurrentTime;
        private long lastTimeUpdate;
        private final int maxSize;

        // Reusable objects
        private final Object[] tempObjects;
        private int tempObjectIndex;

        OperationContext(int maxSize) {
            this.maxSize = maxSize;
            this.tempObjects = new Object[maxSize];
            this.tempObjectIndex = 0;
            this.cachedCurrentTime = 0;
            this.lastTimeUpdate = 0;
        }

        final long currentTime() {
            // Cache current time to avoid repeated system calls
            long now = System.nanoTime();
            if (now - lastTimeUpdate > 1_000_000) { // Update every millisecond
                cachedCurrentTime = System.currentTimeMillis();
                lastTimeUpdate = now;
            }
            return cachedCurrentTime;
        }

        final void reset() {
            // Reset temp objects
            for (int i = 0; i < tempObjectIndex; i++) {
                tempObjects[i] = null;
            }
            tempObjectIndex = 0;
        }

        @SuppressWarnings("unchecked")
        final <T> T getTempObject(Class<T> type) {
            if (tempObjectIndex < maxSize) {
                Object obj = tempObjects[tempObjectIndex];
                if (obj == null || !type.isInstance(obj)) {
                    try {
                        obj = type.newInstance();
                        tempObjects[tempObjectIndex] = obj;
                    } catch (Exception e) {
                        return null;
                    }
                }
                tempObjectIndex++;
                return (T) obj;
            }
            return null;
        }
    }
}
