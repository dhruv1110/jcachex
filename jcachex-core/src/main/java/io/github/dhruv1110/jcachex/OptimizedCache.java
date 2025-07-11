package io.github.dhruv1110.jcachex;

import io.github.dhruv1110.jcachex.concurrent.AccessBuffer;
import io.github.dhruv1110.jcachex.concurrent.AccessBuffer.AccessRecord;
import io.github.dhruv1110.jcachex.concurrent.AccessBuffer.AccessType;
import io.github.dhruv1110.jcachex.eviction.EvictionStrategy;
import io.github.dhruv1110.jcachex.eviction.WindowTinyLFUEvictionStrategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.time.Instant;

/**
 * High-performance cache implementation with Caffeine-inspired optimizations.
 * <p>
 * This implementation provides:
 * <ul>
 * <li><strong>Lock-Free Reads:</strong> Uses atomic operations and access
 * buffers</li>
 * <li><strong>Window TinyLFU:</strong> Advanced eviction with frequency-based
 * admission control</li>
 * <li><strong>Batched Operations:</strong> Reduces contention through batch
 * processing</li>
 * <li><strong>Adaptive Sizing:</strong> Automatically adjusts to workload
 * patterns</li>
 * </ul>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @since 1.0.0
 */
public class OptimizedCache<K, V> implements Cache<K, V> {

    // Cache data storage
    private final ConcurrentHashMap<K, CacheEntry<V>> data;
    private final CacheConfig<K, V> config;
    private final CacheStats stats;

    // Eviction and access tracking
    private final WindowTinyLFUEvictionStrategy<K, V> evictionStrategy;
    private final AccessBuffer<K> accessBuffer;
    private final FrequencySketch frequencySketch;

    // Lifecycle management
    private final AtomicReference<State> state;
    private final AtomicLong version;
    private final ReentrantReadWriteLock maintenanceLock;

    // Configuration constants
    private static final int DEFAULT_DRAIN_THRESHOLD = 64;
    private static final long DEFAULT_DRAIN_INTERVAL_NANOS = 1_000_000; // 1ms
    private static final int FREQUENCY_SKETCH_SIZE = 1024;

    private enum State {
        ACTIVE, MAINTENANCE, SHUTDOWN
    }

    /**
     * Creates a new optimized cache with the specified configuration.
     *
     * @param config the cache configuration
     */
    public OptimizedCache(CacheConfig<K, V> config) {
        this.config = config;
        this.data = new ConcurrentHashMap<>(config.getInitialCapacity());
        this.stats = new CacheStats();

        // Initialize eviction strategy
        this.evictionStrategy = new WindowTinyLFUEvictionStrategy<K, V>(
                config.getMaximumSize());

        // Initialize access tracking
        this.accessBuffer = new AccessBuffer<>(DEFAULT_DRAIN_THRESHOLD, DEFAULT_DRAIN_INTERVAL_NANOS);
        this.frequencySketch = new FrequencySketch(FREQUENCY_SKETCH_SIZE);

        // Initialize state
        this.state = new AtomicReference<>(State.ACTIVE);
        this.version = new AtomicLong(0);
        this.maintenanceLock = new ReentrantReadWriteLock();
    }

    @Override
    public V get(K key) {
        if (key == null || state.get() == State.SHUTDOWN) {
            return null;
        }

        // Lock-free read path
        CacheEntry<V> entry = data.get(key);
        if (entry == null) {
            stats.recordMiss();
            return null;
        }

        // Check if entry is expired
        if (entry.isExpired()) {
            stats.recordMiss();
            // Schedule async removal
            recordAccess(key, AccessType.EVICT, 0);
            return null;
        }

        // Record access asynchronously
        int frequency = frequencySketch.frequency(key);
        recordAccess(key, AccessType.READ, frequency);

        stats.recordHit();
        entry.incrementAccessCount();

        return entry.getValue();
    }

    @Override
    public void put(K key, V value) {
        if (key == null || value == null || state.get() == State.SHUTDOWN) {
            return;
        }

        // Create new entry
        CacheEntry<V> newEntry = createEntry(value);

        // Atomic put operation
        data.put(key, newEntry);

        // Update frequency sketch
        frequencySketch.increment(key);

        // Record access
        int frequency = frequencySketch.frequency(key);
        recordAccess(key, AccessType.WRITE, frequency);

        // Schedule eviction if needed
        if (data.size() > config.getMaximumSize()) {
            scheduleEviction();
        }
    }

    @Override
    public V remove(K key) {
        if (key == null || state.get() == State.SHUTDOWN) {
            return null;
        }

        CacheEntry<V> removed = data.remove(key);
        if (removed != null) {
            // Record eviction
            recordAccess(key, AccessType.EVICT, 0);
            return removed.getValue();
        }

        return null;
    }

    @Override
    public void clear() {
        if (state.get() == State.SHUTDOWN) {
            return;
        }

        maintenanceLock.writeLock().lock();
        try {
            data.clear();
            evictionStrategy.clear();
            accessBuffer.clear();
            frequencySketch.clear();
            version.incrementAndGet();
        } finally {
            maintenanceLock.writeLock().unlock();
        }
    }

    @Override
    public long size() {
        return data.size();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null || state.get() == State.SHUTDOWN) {
            return false;
        }

        CacheEntry<V> entry = data.get(key);
        if (entry == null || entry.isExpired()) {
            return false;
        }

        // Record access without frequency update
        recordAccess(key, AccessType.READ, frequencySketch.frequency(key));

        return true;
    }

    @Override
    public CacheStats stats() {
        return stats;
    }

    @Override
    public CacheConfig<K, V> config() {
        return config;
    }

    @Override
    public Set<K> keys() {
        return data.keySet();
    }

    @Override
    public Collection<V> values() {
        return data.values().stream()
                .map(CacheEntry::getValue)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return data.entrySet().stream()
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
                .collect(java.util.stream.Collectors.toSet());
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

    /**
     * Creates a cache entry with expiration and weight information.
     *
     * @param value the value to cache
     * @return the cache entry
     */
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

    /**
     * Records a cache access asynchronously.
     *
     * @param key       the cache key
     * @param type      the access type
     * @param frequency the current frequency estimate
     */
    private void recordAccess(K key, AccessType type, int frequency) {
        if (state.get() == State.ACTIVE) {
            accessBuffer.recordAccess(key, type, frequency);

            // Periodic maintenance
            if (accessBuffer.needsDraining()) {
                schedulePeriodicMaintenance();
            }
        }
    }

    /**
     * Schedules eviction to be performed asynchronously.
     */
    private void scheduleEviction() {
        if (state.compareAndSet(State.ACTIVE, State.MAINTENANCE)) {
            try {
                performEviction();
            } finally {
                state.set(State.ACTIVE);
            }
        }
    }

    /**
     * Performs eviction using the Window TinyLFU strategy.
     */
    private void performEviction() {
        maintenanceLock.writeLock().lock();
        try {
            int currentSize = data.size();
            int targetSize = config.getMaximumSize().intValue();

            if (currentSize <= targetSize) {
                return;
            }

            // Calculate number of entries to evict
            int evictCount = currentSize - targetSize;

            // Evict entries one by one using the strategy
            for (int i = 0; i < evictCount; i++) {
                K candidate = evictionStrategy.selectEvictionCandidate(data);
                if (candidate != null) {
                    data.remove(candidate);
                    evictionStrategy.remove(candidate);
                    recordAccess(candidate, AccessType.EVICT, 0);
                } else {
                    break;
                }
            }

            version.incrementAndGet();
        } finally {
            maintenanceLock.writeLock().unlock();
        }
    }

    /**
     * Schedules periodic maintenance tasks.
     */
    private void schedulePeriodicMaintenance() {
        if (state.compareAndSet(State.ACTIVE, State.MAINTENANCE)) {
            try {
                performPeriodicMaintenance();
            } finally {
                state.set(State.ACTIVE);
            }
        }
    }

    /**
     * Performs periodic maintenance including access buffer draining.
     */
    private void performPeriodicMaintenance() {
        // Drain access buffer
        accessBuffer.drainToHandler(this::processAccessRecord);

        // Update frequency sketch
        frequencySketch.ensureCapacity(data.size());

        // Clean up expired entries
        cleanupExpiredEntries();
    }

    /**
     * Processes an access record from the buffer.
     *
     * @param record the access record
     */
    private void processAccessRecord(AccessRecord<K> record) {
        switch (record.type) {
            case READ:
                evictionStrategy.recordAccess(record.key, data.get(record.key));
                break;
            case WRITE:
                evictionStrategy.recordAccess(record.key, data.get(record.key));
                break;
            case EVICT:
                evictionStrategy.recordRemoval(record.key);
                break;
        }
    }

    /**
     * Cleans up expired entries.
     */
    private void cleanupExpiredEntries() {
        data.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                evictionStrategy.recordRemoval(entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * Shuts down the cache and releases resources.
     */
    public void shutdown() {
        if (state.compareAndSet(State.ACTIVE, State.SHUTDOWN) ||
                state.compareAndSet(State.MAINTENANCE, State.SHUTDOWN)) {

            maintenanceLock.writeLock().lock();
            try {
                // Drain remaining accesses
                accessBuffer.forceDrain(this::processAccessRecord);

                // Shutdown components
                accessBuffer.shutdown();

                // Clear data
                data.clear();
                evictionStrategy.clear();

            } finally {
                maintenanceLock.writeLock().unlock();
            }
        }
    }

    /**
     * Returns performance metrics.
     *
     * @return performance metrics
     */
    public String getPerformanceMetrics() {
        return String.format(
                "Cache Performance Metrics:\n" +
                        "  Size: %d/%d\n" +
                        "  Hit Rate: %.2f%%\n" +
                        "  Access Buffer: %d pending, %d stripes, %d contention\n" +
                        "  Frequency Sketch: %d capacity\n" +
                        "  Version: %d",
                data.size(), config.getMaximumSize(),
                stats.getHitRate() * 100,
                accessBuffer.getPendingAccessCount(),
                accessBuffer.getStripeCount(),
                accessBuffer.getContentionLevel(),
                frequencySketch.getCapacity(),
                version.get());
    }
}
