package io.github.dhruv1110.jcachex.impl.base;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEntry;
import io.github.dhruv1110.jcachex.FrequencySketch;
import io.github.dhruv1110.jcachex.concurrent.AccessBuffer;
import io.github.dhruv1110.jcachex.concurrent.AccessBuffer.AccessRecord;
import io.github.dhruv1110.jcachex.concurrent.AccessBuffer.AccessType;
import io.github.dhruv1110.jcachex.eviction.WindowTinyLFUEvictionStrategy;
import io.github.dhruv1110.jcachex.impl.base.ConcurrentCacheBase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Base class for optimized cache implementations providing performance-oriented
 * features.
 *
 * This class extends ConcurrentCacheBase and adds:
 * - Lock-free read operations where possible
 * - Frequency sketching for access patterns
 * - Batched operations to reduce contention
 * - Adaptive eviction strategies
 * - Performance monitoring and optimization
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public abstract class OptimizedCacheBase<K, V> extends ConcurrentCacheBase<K, V> {

    // Advanced eviction strategy
    protected final WindowTinyLFUEvictionStrategy<K, V> windowTinyLFUStrategy;

    // Access tracking and frequency analysis
    protected final AccessBuffer<K> accessBuffer;
    protected final FrequencySketch frequencySketch;

    // Performance monitoring
    protected final AtomicReference<State> operationState;
    protected final AtomicLong version;
    protected final ReentrantReadWriteLock maintenanceLock;

    // Configuration constants
    protected static final int DEFAULT_DRAIN_THRESHOLD = 64;
    protected static final long DEFAULT_DRAIN_INTERVAL_NANOS = 1_000_000; // 1ms
    protected static final int FREQUENCY_SKETCH_SIZE = 1024;

    /**
     * Internal state for operation coordination.
     */
    protected enum State {
        ACTIVE, MAINTENANCE, SHUTDOWN
    }

    /**
     * Constructor for optimized cache implementations.
     *
     * @param config the cache configuration
     */
    protected OptimizedCacheBase(CacheConfig<K, V> config) {
        super(config);

        // Initialize advanced eviction strategy (use WindowTinyLFU as fallback for
        // optimization)
        this.windowTinyLFUStrategy = new WindowTinyLFUEvictionStrategy<>(maximumSize);

        // Initialize access tracking
        this.accessBuffer = new AccessBuffer<>(DEFAULT_DRAIN_THRESHOLD, DEFAULT_DRAIN_INTERVAL_NANOS);
        this.frequencySketch = new FrequencySketch(FREQUENCY_SKETCH_SIZE);

        // Initialize state management
        this.operationState = new AtomicReference<>(State.ACTIVE);
        this.version = new AtomicLong(0);
        this.maintenanceLock = new ReentrantReadWriteLock();

        // Schedule performance optimization tasks
        scheduleOptimizationTasks();
    }

    /**
     * Optimized get operation with lock-free reads where possible.
     */
    @Override
    protected V doGet(K key) {
        if (operationState.get() == State.SHUTDOWN) {
            return null;
        }

        // Lock-free read path
        CacheEntry<V> entry = data.get(key);
        if (entry == null) {
            V loadedValue = loadValue(key);
            // Record as miss even if load succeeded
            recordGetStatistics(false);
            return loadedValue;
        }

        // Check if entry is expired
        if (isEntryExpired(entry)) {
            // Schedule async removal
            recordAccess(key, AccessType.EVICT, 0);
            V loadedValue = loadValue(key);
            // Record as miss even if load succeeded
            recordGetStatistics(false);
            return loadedValue;
        }

        // Record access asynchronously
        int frequency = frequencySketch.frequency(key);
        recordAccess(key, AccessType.READ, frequency);

        // Update access information
        entry.incrementAccessCount();

        // Update expiration time for access-based expiration
        if (config.getExpireAfterAccess() != null) {
            entry.updateExpirationOnAccess(config.getExpireAfterAccess());
        }

        // Use configured eviction strategy (from parent class) instead of hardcoded
        // WindowTinyLFU
        evictionStrategy.update(key, entry);

        // Record hit
        recordGetStatistics(true);
        return entry.getValue();
    }

    /**
     * Optimized put operation with batched processing.
     */
    @Override
    protected void doPut(K key, V value) {
        if (operationState.get() == State.SHUTDOWN) {
            return;
        }

        // Create new entry
        CacheEntry<V> newEntry = createCacheEntry(key, value);

        // Update frequency sketch before putting
        frequencySketch.increment(key);

        // Atomic put operation
        CacheEntry<V> oldEntry = data.put(key, newEntry);

        if (oldEntry == null) {
            currentSize.incrementAndGet();
            currentWeight.addAndGet(newEntry.getWeight());
        } else {
            currentWeight.addAndGet(newEntry.getWeight() - oldEntry.getWeight());
            notifyListeners(listener -> listener.onRemove(key, oldEntry.getValue()));
        }

        // Record access
        int frequency = frequencySketch.frequency(key);
        recordAccess(key, AccessType.WRITE, frequency);

        // Notify listeners
        notifyListeners(listener -> listener.onPut(key, value));

        // Use configured eviction strategy (from parent class) instead of hardcoded
        // WindowTinyLFU
        evictionStrategy.update(key, newEntry);

        // Immediately enforce size limit if exceeded
        while (isSizeLimitReached()) {
            if (shouldAvoidEvictingNewEntries()) {
                if (!performSingleEviction(key)) {
                    break; // No more entries to evict
                }
            } else {
                if (!performSingleEviction()) {
                    break; // No more entries to evict
                }
            }
        }
    }

    /**
     * Performs a single eviction and returns true if successful.
     */
    private boolean performSingleEviction() {
        return performSingleEviction(null);
    }

    /**
     * Performs a single eviction avoiding the specified key and returns true if
     * successful.
     */
    private boolean performSingleEviction(K keyToAvoid) {
        K keyToEvict = evictionStrategy.selectEvictionCandidate(data);
        if (keyToEvict == null) {
            return false;
        }

        // If we should avoid evicting this key, try to find another candidate
        if (keyToAvoid != null && keyToEvict.equals(keyToAvoid)) {
            // Try to find another candidate by temporarily removing the key to avoid
            if (data.size() > 1) {
                // Create a temporary map without the key to avoid
                Map<K, CacheEntry<V>> tempData = new HashMap<>(data);
                tempData.remove(keyToAvoid);
                K alternateKey = evictionStrategy.selectEvictionCandidate(tempData);
                if (alternateKey == null) {
                    return false;
                }
                keyToEvict = alternateKey;
            } else {
                return false; // Can't evict if it's the only entry and we want to avoid it
            }
        }

        final K finalKeyToEvict = keyToEvict;
        ReentrantReadWriteLock.WriteLock lock = getWriteLock(finalKeyToEvict);
        lock.lock();
        try {
            CacheEntry<V> removed = data.remove(finalKeyToEvict);
            if (removed != null) {
                currentSize.decrementAndGet();
                currentWeight.addAndGet(-removed.getWeight());
                evictionStrategy.remove(finalKeyToEvict);

                // Record eviction statistics
                if (statsEnabled) {
                    stats.getEvictionCount().incrementAndGet();
                }

                notifyListeners(listener -> listener.onEvict(finalKeyToEvict, removed.getValue(),
                        io.github.dhruv1110.jcachex.EvictionReason.SIZE));
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Enhanced clear operation with versioning.
     */
    @Override
    protected void doClear() {
        if (operationState.get() == State.SHUTDOWN) {
            return;
        }

        maintenanceLock.writeLock().lock();
        try {
            data.clear();
            evictionStrategy.clear();
            accessBuffer.clear();
            frequencySketch.clear();
            currentSize.set(0);
            currentWeight.set(0);
            version.incrementAndGet();
            notifyListeners(listener -> listener.onClear());
        } finally {
            maintenanceLock.writeLock().unlock();
        }
    }

    /**
     * Optimized size enforcement using advanced eviction strategies.
     */
    @Override
    protected void enforceSize() {
        if (isSizeLimitReached()) {
            scheduleEviction();
        }
    }

    /**
     * Records access for frequency analysis and batched processing.
     */
    protected void recordAccess(K key, AccessType type, int frequency) {
        if (operationState.get() == State.ACTIVE) {
            accessBuffer.recordAccess(key, type, frequency);
        }
    }

    /**
     * Schedules eviction using the optimized strategy.
     */
    protected void scheduleEviction() {
        // Simplified eviction - just perform immediate eviction
        while (isSizeLimitReached()) {
            if (!performSingleEviction()) {
                break; // No more entries to evict
            }
        }
    }

    /**
     * Drains the access buffer and updates eviction state.
     */
    protected void drainAccessBuffer() {
        accessBuffer.drainToHandler(this::processAccessRecord);
    }

    /**
     * Processes an access record for eviction strategy updates.
     */
    protected void processAccessRecord(AccessRecord<K> record) {
        K key = record.key;
        CacheEntry<V> entry = data.get(key);

        if (entry != null) {
            switch (record.type) {
                case READ:
                    evictionStrategy.update(key, entry);
                    break;
                case WRITE:
                    evictionStrategy.update(key, entry);
                    break;
                case EVICT:
                    // Remove expired entry
                    if (isEntryExpired(entry)) {
                        if (data.remove(key, entry)) {
                            currentSize.decrementAndGet();
                            currentWeight.addAndGet(-entry.getWeight());
                            evictionStrategy.remove(key);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Schedules periodic optimization tasks.
     */
    protected void scheduleOptimizationTasks() {
        // Schedule periodic maintenance
        scheduler.scheduleAtFixedRate(this::performPeriodicMaintenance,
                10, 10, java.util.concurrent.TimeUnit.SECONDS);

        // Schedule access buffer draining
        scheduler.scheduleAtFixedRate(this::drainAccessBuffer,
                1, 1, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * Performs periodic maintenance and optimization.
     */
    protected void performPeriodicMaintenance() {
        if (operationState.get() == State.ACTIVE) {
            // Clean up expired entries
            cleanupExpiredEntries();

            // Reset frequency sketch much less frequently to preserve LFU tracking
            // Only clear after many more operations and only if using WindowTinyLFU
            if (version.get() % 100000 == 0 && evictionStrategy instanceof WindowTinyLFUEvictionStrategy) {
                frequencySketch.clear();
            }
        }
    }

    /**
     * Returns performance metrics for monitoring.
     */
    public String getPerformanceMetrics() {
        return String.format(
                "Version: %d, Size: %d, State: %s, AccessBuffer: %d pending",
                version.get(), currentSize.get(), operationState.get(), accessBuffer.getPendingAccessCount());
    }

    /**
     * Shuts down the optimized cache and cleans up resources.
     */
    @Override
    public void shutdown() {
        if (operationState.compareAndSet(State.ACTIVE, State.SHUTDOWN)) {
            // Drain any remaining access records
            drainAccessBuffer();

            // Clear all data structures
            accessBuffer.clear();
            frequencySketch.clear();
            evictionStrategy.clear();
        }

        // Call parent shutdown
        super.shutdown();
    }

    /**
     * Hook for subclasses to perform custom optimization.
     */
    protected void performCustomOptimization() {
        // Override in subclasses for specific optimizations
    }

    /**
     * Returns the current operation state.
     */
    protected State getOperationState() {
        return operationState.get();
    }

    /**
     * Returns the current version for optimistic concurrency control.
     */
    protected long getVersion() {
        return version.get();
    }

    /**
     * Determines if we should avoid evicting newly added entries.
     * FILO and FIFO strategies should not avoid evicting new entries.
     */
    protected boolean shouldAvoidEvictingNewEntries() {
        String strategyName = evictionStrategy.getClass().getSimpleName();
        return !strategyName.contains("FILO") && !strategyName.contains("FIFO");
    }
}
