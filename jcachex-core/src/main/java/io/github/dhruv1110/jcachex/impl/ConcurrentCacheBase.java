package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEntry;
import io.github.dhruv1110.jcachex.CacheEventListener;
import io.github.dhruv1110.jcachex.eviction.EvictionStrategy;
import io.github.dhruv1110.jcachex.eviction.LRUEvictionStrategy;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Base class for concurrent cache implementations providing thread-safety
 * primitives.
 *
 * This class extends DataBackedCacheBase and adds:
 * - Striped locking for reduced contention
 * - Eviction strategy management
 * - Event listener notification system
 * - Scheduled cleanup operations
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public abstract class ConcurrentCacheBase<K, V> extends DataBackedCacheBase<K, V, CacheEntry<V>> {

    // Eviction strategy
    protected final EvictionStrategy<K, V> evictionStrategy;

    // Striped locking for better concurrency
    protected static final int STRIPE_COUNT = 32; // Power of 2 for efficient modulo
    protected final ReentrantReadWriteLock[] stripes = new ReentrantReadWriteLock[STRIPE_COUNT];

    // Scheduled executor for cleanup and refresh operations
    protected final ScheduledExecutorService scheduler;

    // Size and weight tracking
    protected final AtomicLong currentSize;
    protected final AtomicLong currentWeight;

    // Configuration constants
    private static final long CLEANUP_INTERVAL_SECONDS = 60L;
    private static final long REFRESH_INTERVAL_SECONDS = 1L;

    /**
     * Constructor for concurrent cache implementations.
     *
     * @param config the cache configuration
     */
    protected ConcurrentCacheBase(CacheConfig<K, V> config) {
        super(config);

        // Data storage is now handled by DataBackedCacheBase

        // Initialize eviction strategy
        this.evictionStrategy = config.getEvictionStrategy() != null ? config.getEvictionStrategy()
                : new LRUEvictionStrategy<>();

        // Initialize striped locks
        for (int i = 0; i < STRIPE_COUNT; i++) {
            stripes[i] = new ReentrantReadWriteLock();
        }

        // Initialize scheduler
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "jcachex-scheduler");
            thread.setDaemon(true);
            return thread;
        });

        // Initialize size and weight tracking
        this.currentSize = new AtomicLong(0);
        this.currentWeight = new AtomicLong(0);

        // Schedule cleanup operations
        if (hasExpiration) {
            scheduleCleanup();
        }

        // Schedule refresh operations if needed
        if (config.getRefreshAfterWrite() != null) {
            scheduleRefresh();
        }
    }

    // Implementation of abstract methods from AbstractCacheBase

    @Override
    protected V doGet(K key) {
        CacheEntry<V> entry = data.get(key);
        if (entry != null) {
            // Check expiration
            if (isEntryExpired(entry)) {
                // Remove expired entry
                ReentrantReadWriteLock.WriteLock lock = getWriteLock(key);
                lock.lock();
                try {
                    // Double-check after acquiring lock
                    entry = data.get(key);
                    if (entry != null && isEntryExpired(entry)) {
                        data.remove(key);
                        evictionStrategy.remove(key);
                        currentSize.decrementAndGet();
                        currentWeight.addAndGet(-entry.getWeight());
                        // Cache miss due to expiration, try to load
                        V loadedValue = loadValue(key);
                        // Record as miss even if load succeeded
                        recordGetStatistics(false);
                        return loadedValue;
                    }
                } finally {
                    lock.unlock();
                }
            }

            if (entry != null) {
                // Update access information
                entry.incrementAccessCount();

                // Update expiration time for access-based expiration
                if (config.getExpireAfterAccess() != null) {
                    entry.updateExpirationOnAccess(config.getExpireAfterAccess());
                }

                evictionStrategy.update(key, entry);
                // Record hit
                recordGetStatistics(true);
                return entry.getValue();
            }
        }

        // Cache miss - try to load value
        V loadedValue = loadValue(key);
        // Record as miss even if load succeeded
        recordGetStatistics(false);
        return loadedValue;
    }

    @Override
    protected void doPut(K key, V value) {
        CacheEntry<V> entry = createCacheEntry(key, value);

        ReentrantReadWriteLock.WriteLock lock = getWriteLock(key);
        lock.lock();
        try {
            CacheEntry<V> oldEntry = data.put(key, entry);
            if (oldEntry != null) {
                // Update weight tracking
                currentWeight.addAndGet(entry.getWeight() - oldEntry.getWeight());
                notifyListeners(listener -> listener.onRemove(key, oldEntry.getValue()));
            } else {
                currentSize.incrementAndGet();
                currentWeight.addAndGet(entry.getWeight());
            }
            notifyListeners(listener -> listener.onPut(key, value));
            evictionStrategy.update(key, entry);
        } finally {
            lock.unlock();
        }

        // Check eviction outside of striped lock to avoid deadlock
        if (isSizeLimitReached()) {
            if (shouldAvoidEvictingNewEntries()) {
                evictEntries(key);
            } else {
                evictEntries();
            }
        }
    }

    @Override
    protected V doRemove(K key) {
        ReentrantReadWriteLock.WriteLock lock = getWriteLock(key);
        lock.lock();
        try {
            CacheEntry<V> entry = data.remove(key);
            if (entry != null) {
                currentSize.decrementAndGet();
                currentWeight.addAndGet(-entry.getWeight());
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
    protected void doClear() {
        // Acquire all write locks to ensure consistency
        for (ReentrantReadWriteLock stripe : stripes) {
            stripe.writeLock().lock();
        }
        try {
            data.clear();
            evictionStrategy.clear();
            currentSize.set(0);
            currentWeight.set(0);
            notifyListeners(CacheEventListener::onClear);
        } finally {
            for (int i = stripes.length - 1; i >= 0; i--) {
                stripes[i].writeLock().unlock();
            }
        }
    }

    @Override
    protected boolean doContainsKey(K key) {
        CacheEntry<V> entry = data.get(key);
        if (entry != null && hasExpiration && isEntryExpired(entry)) {
            // Clean up expired entry
            ReentrantReadWriteLock.WriteLock lock = getWriteLock(key);
            lock.lock();
            try {
                entry = data.get(key);
                if (entry != null && isEntryExpired(entry)) {
                    data.remove(key);
                    evictionStrategy.remove(key);
                    currentSize.decrementAndGet();
                    currentWeight.addAndGet(-entry.getWeight());
                    return false;
                }
            } finally {
                lock.unlock();
            }
        }
        return entry != null;
    }

    // Collection view methods are now inherited from DataBackedCacheBase

    @Override
    public long size() {
        return currentSize.get();
    }

    // Entry extraction and validation methods for DataBackedCacheBase

    @Override
    protected V extractValue(CacheEntry<V> entry) {
        return entry.getValue();
    }

    @Override
    protected boolean isValidEntry(CacheEntry<V> entry) {
        return !isEntryExpired(entry);
    }

    // Protected utility methods for subclasses

    /**
     * Get the stripe index for a key to enable striped locking.
     */
    protected int getStripeIndex(K key) {
        return Math.abs(key.hashCode()) & (STRIPE_COUNT - 1);
    }

    /**
     * Get read lock for a specific key.
     */
    protected ReentrantReadWriteLock.ReadLock getReadLock(K key) {
        return stripes[getStripeIndex(key)].readLock();
    }

    /**
     * Get write lock for a specific key.
     */
    protected ReentrantReadWriteLock.WriteLock getWriteLock(K key) {
        return stripes[getStripeIndex(key)].writeLock();
    }

    /**
     * Override size enforcement to use eviction strategy.
     */
    @Override
    protected void enforceSize() {
        if (isSizeLimitReached()) {
            evictEntries();
        }
    }

    /**
     * Evict entries based on the eviction strategy.
     */
    /**
     * Determines if we should avoid evicting newly added entries.
     * FILO and FIFO strategies should not avoid evicting new entries.
     */
    protected boolean shouldAvoidEvictingNewEntries() {
        String strategyName = evictionStrategy.getClass().getSimpleName();
        return !strategyName.contains("FILO") && !strategyName.contains("FIFO");
    }

    protected void evictEntries() {
        // Only evict one entry at a time to avoid over-eviction
        if (isSizeLimitReached()) {
            K candidateKey = evictionStrategy.selectEvictionCandidate(data);
            if (candidateKey != null) {
                ReentrantReadWriteLock.WriteLock lock = getWriteLock(candidateKey);
                lock.lock();
                try {
                    CacheEntry<V> removed = data.remove(candidateKey);
                    if (removed != null) {
                        currentSize.decrementAndGet();
                        currentWeight.addAndGet(-removed.getWeight());
                        evictionStrategy.remove(candidateKey);

                        // Record eviction statistics
                        if (statsEnabled) {
                            stats.getEvictionCount().incrementAndGet();
                        }

                        notifyListeners(listener -> listener.onEvict(candidateKey, removed.getValue(),
                                io.github.dhruv1110.jcachex.EvictionReason.SIZE));
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * Evict entries based on the eviction strategy, avoiding a specific key.
     */
    protected void evictEntries(K keyToAvoid) {
        // Only evict one entry at a time to avoid over-eviction
        if (isSizeLimitReached()) {
            K candidateKey = evictionStrategy.selectEvictionCandidate(data);

            // If the selected key is the one we want to avoid, try to find another
            if (candidateKey != null && keyToAvoid != null && candidateKey.equals(keyToAvoid)) {
                // Try to find an alternative eviction candidate
                K alternativeKey = null;
                for (K candidate : data.keySet()) {
                    if (!candidate.equals(keyToAvoid)) {
                        alternativeKey = candidate;
                        break;
                    }
                }
                candidateKey = alternativeKey;
            }

            final K keyToEvict = candidateKey;
            if (keyToEvict != null && (keyToAvoid == null || !keyToEvict.equals(keyToAvoid))) {
                ReentrantReadWriteLock.WriteLock lock = getWriteLock(keyToEvict);
                lock.lock();
                try {
                    CacheEntry<V> removed = data.remove(keyToEvict);
                    if (removed != null) {
                        currentSize.decrementAndGet();
                        currentWeight.addAndGet(-removed.getWeight());
                        evictionStrategy.remove(keyToEvict);

                        // Record eviction statistics
                        if (statsEnabled) {
                            stats.getEvictionCount().incrementAndGet();
                        }

                        notifyListeners(listener -> listener.onEvict(keyToEvict, removed.getValue(),
                                io.github.dhruv1110.jcachex.EvictionReason.SIZE));
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * Load value using configured loader.
     */
    protected V loadValue(K key) {
        if (config.getLoader() != null) {
            try {
                V value = config.getLoader().apply(key);
                if (value != null) {
                    // Use the createCacheEntry method that passes the key to weigher
                    CacheEntry<V> entry = createCacheEntry(key, value);

                    ReentrantReadWriteLock.WriteLock lock = getWriteLock(key);
                    lock.lock();
                    try {
                        // Only put if not already present (avoid overwriting existing entries)
                        if (!data.containsKey(key)) {
                            data.put(key, entry);
                            currentSize.incrementAndGet();
                            currentWeight.addAndGet(entry.getWeight());
                            evictionStrategy.update(key, entry);
                        }
                    } finally {
                        lock.unlock();
                    }

                    notifyListeners(listener -> listener.onLoad(key, value));

                    // Update statistics
                    if (statsEnabled) {
                        stats.getLoadCount().incrementAndGet();
                    }

                    // Check eviction after loading
                    if (isSizeLimitReached()) {
                        if (shouldAvoidEvictingNewEntries()) {
                            evictEntries(key);
                        } else {
                            evictEntries();
                        }
                    }

                    return value;
                }
            } catch (Exception e) {
                notifyListeners(listener -> listener.onLoadError(key, e));

                // Update statistics for load failure
                if (statsEnabled) {
                    stats.getLoadFailureCount().incrementAndGet();
                }
            }
        }
        return null;
    }

    /**
     * Notify all registered event listeners.
     */
    protected void notifyListeners(Consumer<CacheEventListener<K, V>> action) {
        if (config.getListeners() != null) {
            for (CacheEventListener<K, V> listener : config.getListeners()) {
                action.accept(listener);
            }
        }
    }

    /**
     * Schedule periodic cleanup of expired entries.
     */
    protected void scheduleCleanup() {
        scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries,
                CLEANUP_INTERVAL_SECONDS, CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Schedule refresh operations for entries.
     */
    protected void scheduleRefresh() {
        scheduler.scheduleAtFixedRate(this::refreshEntries,
                REFRESH_INTERVAL_SECONDS, REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Clean up expired entries.
     */
    protected void cleanupExpiredEntries() {
        data.entrySet().removeIf(entry -> {
            if (isEntryExpired(entry.getValue())) {
                currentSize.decrementAndGet();
                currentWeight.addAndGet(-entry.getValue().getWeight());
                evictionStrategy.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * Refresh entries that need refreshing.
     */
    protected void refreshEntries() {
        // Default implementation does nothing
        // Override in subclasses that support refresh
    }

    /**
     * Shutdown the cache and clean up resources.
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    protected long getCurrentWeight() {
        return currentWeight.get();
    }

    /**
     * Creates a cache entry with proper weight calculation.
     * Overrides the parent method to pass the key to the weigher.
     */
    protected CacheEntry<V> createCacheEntry(K key, V value) {
        Instant expirationTime = null;
        if (config.getExpireAfterWrite() != null) {
            expirationTime = Instant.now().plus(config.getExpireAfterWrite());
        } else if (config.getExpireAfterAccess() != null) {
            expirationTime = Instant.now().plus(config.getExpireAfterAccess());
        }

        long weight = config.getWeigher() != null ? config.getWeigher().apply(key, value) : 1L;
        return new CacheEntry<>(value, weight, expirationTime);
    }

    /**
     * Creates a cache entry with proper weight calculation.
     * Uses null as key - this is called from parent class.
     */
    @Override
    protected CacheEntry<V> createCacheEntry(V value) {
        return createCacheEntry(null, value);
    }
}
