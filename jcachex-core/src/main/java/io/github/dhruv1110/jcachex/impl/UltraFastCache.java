package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEntry;
import io.github.dhruv1110.jcachex.CacheStats;
import io.github.dhruv1110.jcachex.eviction.EvictionStrategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.CompletableFuture;
import java.util.Set;
import java.util.Collection;
import java.util.Map;

/**
 * Ultra-fast cache implementation optimized for maximum throughput.
 *
 * Key optimizations:
 * - Minimal hot path overhead
 * - Lock-free operations where possible
 * - Efficient eviction with O(1) operations
 * - Reduced object allocation
 * - Optimized for CPU cache efficiency
 */
public final class UltraFastCache<K, V> implements Cache<K, V> {

    // Core data structure - optimized for performance
    private final ConcurrentHashMap<K, FastEntry<V>> data;

    // Fast statistics - using LongAdder for high-throughput scenarios
    private final LongAdder hitCount;
    private final LongAdder missCount;

    // Configuration
    private final long maximumSize;
    private final boolean statsEnabled;
    private final EvictionStrategy<K, V> evictionStrategy;

    // Fast eviction tracking
    private final AtomicLong currentSize;
    private final FastEvictionTracker<K> evictionTracker;

    public UltraFastCache(CacheConfig<K, V> config) {
        this.maximumSize = config.getMaximumSize() != null ? config.getMaximumSize() : Long.MAX_VALUE;
        this.statsEnabled = config.isRecordStats();
        this.evictionStrategy = config.getEvictionStrategy() != null ? config.getEvictionStrategy()
                : EvictionStrategy.LRU();

        // Initialize with optimal capacity
        int capacity = calculateOptimalCapacity(maximumSize);
        this.data = new ConcurrentHashMap<>(capacity, 0.75f, 16);

        // Initialize fast statistics
        this.hitCount = new LongAdder();
        this.missCount = new LongAdder();
        this.currentSize = new AtomicLong(0);

        // Initialize fast eviction
        this.evictionTracker = new FastEvictionTracker<>(capacity);
    }

    /**
     * Ultra-fast get operation with minimal overhead.
     */
    @Override
    public V get(K key) {
        if (key == null)
            return null;

        FastEntry<V> entry = data.get(key);
        if (entry != null) {
            V value = entry.getValue();
            if (value != null) {
                // Fast hit path - minimal operations
                if (statsEnabled) {
                    hitCount.increment();
                }
                evictionTracker.recordAccess(key);
                return value;
            }
        }

        // Fast miss path
        if (statsEnabled) {
            missCount.increment();
        }
        return null;
    }

    /**
     * Ultra-fast put operation with efficient eviction.
     */
    @Override
    public void put(K key, V value) {
        if (key == null || value == null)
            return;

        FastEntry<V> newEntry = new FastEntry<>(value);
        FastEntry<V> existing = data.put(key, newEntry);

        if (existing == null) {
            // New entry - check for eviction
            long size = currentSize.incrementAndGet();
            evictionTracker.recordAccess(key);

            if (size > maximumSize) {
                performFastEviction();
            }
        } else {
            // Update existing - just update eviction tracker
            evictionTracker.recordAccess(key);
        }
    }

    /**
     * Fast eviction using O(1) operations.
     */
    private void performFastEviction() {
        K victim = evictionTracker.selectVictim();
        if (victim != null && data.remove(victim) != null) {
            currentSize.decrementAndGet();
            evictionTracker.removeEntry(victim);
        }
    }

    @Override
    public V remove(K key) {
        if (key == null)
            return null;

        FastEntry<V> removed = data.remove(key);
        if (removed != null) {
            currentSize.decrementAndGet();
            evictionTracker.removeEntry(key);
            return removed.getValue();
        }
        return null;
    }

    @Override
    public void clear() {
        data.clear();
        currentSize.set(0);
        evictionTracker.clear();
    }

    @Override
    public long size() {
        return currentSize.get();
    }

    @Override
    public boolean containsKey(K key) {
        return key != null && data.containsKey(key);
    }

    @Override
    public Set<K> keys() {
        return data.keySet();
    }

    @Override
    public Collection<V> values() {
        return data.values().stream()
                .map(FastEntry::getValue)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return data.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getValue()))
                .entrySet();
    }

    @Override
    public CacheStats stats() {
        if (!statsEnabled) {
            return new CacheStats();
        }

        long hits = hitCount.sum();
        long misses = missCount.sum();

        return new CacheStats(
                new AtomicLong(hits),
                new AtomicLong(misses),
                new AtomicLong(0), // evictionCount
                new AtomicLong(0), // loadCount
                new AtomicLong(0), // loadFailureCount
                new AtomicLong(0) // totalLoadTime
        );
    }

    @Override
    public CacheConfig<K, V> config() {
        return CacheConfig.<K, V>builder()
                .maximumSize(maximumSize)
                .recordStats(statsEnabled)
                .evictionStrategy(evictionStrategy)
                .build();
    }

    @Override
    public CompletableFuture<V> getAsync(K key) {
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

    /**
     * Calculate optimal capacity for ConcurrentHashMap.
     */
    private static int calculateOptimalCapacity(long maxSize) {
        if (maxSize <= 0)
            return 16;

        // Next power of 2, capped at reasonable maximum
        long capacity = Math.min(maxSize * 2, Integer.MAX_VALUE / 2);
        return Math.max(16, Integer.highestOneBit((int) capacity) << 1);
    }

    /**
     * Fast cache entry with minimal overhead.
     */
    private static final class FastEntry<V> {
        private final V value;
        private volatile long accessTime;

        FastEntry(V value) {
            this.value = value;
            this.accessTime = System.currentTimeMillis();
        }

        V getValue() {
            return value;
        }

        long getAccessTime() {
            return accessTime;
        }

        void updateAccessTime() {
            accessTime = System.currentTimeMillis();
        }
    }

    /**
     * Fast eviction tracker with O(1) operations.
     */
    private static final class FastEvictionTracker<K> {
        private final ConcurrentHashMap<K, Long> accessTimes;
        private volatile K lastVictim;

        FastEvictionTracker(int capacity) {
            this.accessTimes = new ConcurrentHashMap<>(capacity);
        }

        void recordAccess(K key) {
            accessTimes.put(key, System.currentTimeMillis());
        }

        K selectVictim() {
            // Simple LRU approximation for O(1) performance
            K victim = null;
            long oldestTime = Long.MAX_VALUE;

            // Sample a few entries for victim selection
            int sampleSize = Math.min(5, accessTimes.size());
            int count = 0;

            for (Map.Entry<K, Long> entry : accessTimes.entrySet()) {
                if (count++ >= sampleSize)
                    break;

                if (entry.getValue() < oldestTime) {
                    oldestTime = entry.getValue();
                    victim = entry.getKey();
                }
            }

            return victim;
        }

        void removeEntry(K key) {
            accessTimes.remove(key);
        }

        void clear() {
            accessTimes.clear();
        }
    }
}
