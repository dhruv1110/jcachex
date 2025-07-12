package io.github.dhruv1110.jcachex.impl.base;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.CacheEntry;
import io.github.dhruv1110.jcachex.CacheEventListener;
import io.github.dhruv1110.jcachex.CacheStats;
import io.github.dhruv1110.jcachex.EvictionReason;
import io.github.dhruv1110.jcachex.eviction.EvictionStrategy;
import io.github.dhruv1110.jcachex.eviction.LRUEvictionStrategy;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.Instant;

/**
 * Unified base class for all cache implementations providing comprehensive
 * functionality.
 *
 * <p>
 * This class consolidates functionality from multiple inheritance layers into a
 * single,
 * efficient base class that provides:
 * </p>
 *
 * <ul>
 * <li><strong>Core Operations:</strong> Parameter validation, statistics
 * tracking, configuration management</li>
 * <li><strong>Data Storage:</strong> ConcurrentHashMap-based storage with
 * optimal capacity calculation</li>
 * <li><strong>Concurrency:</strong> Striped locking, thread-safe operations,
 * eviction management</li>
 * <li><strong>Collection Views:</strong> Efficient keys(), values(), entries()
 * implementations</li>
 * <li><strong>Async Operations:</strong> CompletableFuture-based async method
 * implementations</li>
 * <li><strong>Event Handling:</strong> Event listener notification system</li>
 * <li><strong>Maintenance:</strong> Scheduled cleanup and expiration
 * handling</li>
 * </ul>
 *
 * <p>
 * This unified approach eliminates the complexity of multiple inheritance
 * layers while
 * providing all necessary functionality for cache implementations.
 * </p>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @since 1.0.0
 */
public abstract class UnifiedCacheBase<K, V> implements Cache<K, V> {

    // ===== CORE CONFIGURATION AND STATE =====

    protected final CacheConfig<K, V> config;
    protected final CacheStats stats;
    protected final boolean statsEnabled;
    protected final long maximumSize;
    protected final boolean hasExpiration;
    protected final boolean hasMaximumSize;
    protected final boolean hasMaximumWeight;

    // ===== DATA STORAGE =====

    protected final ConcurrentHashMap<K, CacheEntry<V>> data;

    // ===== STATISTICS TRACKING =====

    protected final AtomicLong hitCount;
    protected final AtomicLong missCount;
    protected final AtomicLong currentSize;
    protected final AtomicLong currentWeight;

    // ===== CONCURRENCY CONTROL =====

    protected static final int STRIPE_COUNT = 32;
    protected final ReentrantReadWriteLock[] stripes = new ReentrantReadWriteLock[STRIPE_COUNT];

    // ===== EVICTION AND EVENT HANDLING =====

    protected final EvictionStrategy<K, V> evictionStrategy;
    protected final CacheEventListener<K, V> eventListener;

    // ===== SCHEDULED OPERATIONS =====

    protected final ScheduledExecutorService scheduler;

    // ===== CONSTANTS =====

    private static final long CLEANUP_INTERVAL_SECONDS = 60L;
    private static final long REFRESH_INTERVAL_SECONDS = 1L;

    /**
     * Constructor for all cache implementations.
     *
     * @param config the cache configuration
     * @throws IllegalArgumentException if config is null
     */
    protected UnifiedCacheBase(CacheConfig<K, V> config) {
        // Validate configuration
        if (config == null) {
            throw new IllegalArgumentException("Cache configuration cannot be null");
        }

        this.config = config;
        this.stats = new CacheStats();
        this.statsEnabled = config.isRecordStats();
        this.maximumSize = config.getMaximumSize() != null ? config.getMaximumSize() : Long.MAX_VALUE;

        // Configuration flags
        this.hasExpiration = config.getExpireAfterWrite() != null || config.getExpireAfterAccess() != null;
        this.hasMaximumSize = config.getMaximumSize() != null;
        this.hasMaximumWeight = config.getMaximumWeight() != null;

        // Initialize data storage with optimal capacity
        int capacity = calculateOptimalCapacity(maximumSize);
        this.data = new ConcurrentHashMap<>(capacity, 0.75f, STRIPE_COUNT);

        // Initialize statistics
        this.hitCount = new AtomicLong(0);
        this.missCount = new AtomicLong(0);
        this.currentSize = new AtomicLong(0);
        this.currentWeight = new AtomicLong(0);

        // Initialize striped locks
        for (int i = 0; i < STRIPE_COUNT; i++) {
            stripes[i] = new ReentrantReadWriteLock();
        }

        // Initialize eviction strategy
        this.evictionStrategy = config.getEvictionStrategy() != null ? config.getEvictionStrategy()
                : new LRUEvictionStrategy<>();

        // Initialize event listener (use no-op if not available)
        this.eventListener = CacheEventListener.noOp();

        // Initialize scheduler
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "jcachex-scheduler");
            thread.setDaemon(true);
            return thread;
        });

        // Schedule maintenance tasks
        if (hasExpiration) {
            scheduleCleanup();
        }

        if (config.getRefreshAfterWrite() != null) {
            scheduleRefresh();
        }
    }

    // ===== TEMPLATE METHODS FOR CACHE OPERATIONS =====

    @Override
    public final V get(K key) {
        if (!validateKey(key)) {
            return null;
        }

        V value = doGet(key);
        recordGetStatistics(value != null);
        return value;
    }

    @Override
    public final void put(K key, V value) {
        if (!validateKey(key) || !validateValue(value)) {
            return;
        }

        doPut(key, value);
        recordPutStatistics();
        currentSize.incrementAndGet();

        // Trigger eviction if needed
        if (isSizeLimitReached()) {
            triggerEviction();
        }
    }

    @Override
    public final V remove(K key) {
        if (!validateKey(key)) {
            return null;
        }

        V removedValue = doRemove(key);
        recordRemoveStatistics(removedValue != null);
        if (removedValue != null) {
            currentSize.decrementAndGet();
        }
        return removedValue;
    }

    @Override
    public final void clear() {
        long sizeBefore = size();
        doClear();
        recordClearStatistics(sizeBefore);
        currentSize.set(0);
        currentWeight.set(0);
    }

    @Override
    public final boolean containsKey(K key) {
        return validateKey(key) && doContainsKey(key);
    }

    @Override
    public final long size() {
        return currentSize.get();
    }

    // ===== COLLECTION VIEW METHODS =====

    @Override
    public final Set<K> keys() {
        return data.keySet();
    }

    @Override
    public final Collection<V> values() {
        return data.values().stream()
                .filter(this::isValidEntry)
                .map(CacheEntry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public final Set<Map.Entry<K, V>> entries() {
        return data.entrySet().stream()
                .filter(e -> isValidEntry(e.getValue()))
                .map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), e.getValue().getValue()))
                .collect(Collectors.toSet());
    }

    // ===== ASYNC OPERATIONS =====

    @Override
    public CompletableFuture<V> getAsync(K key) {
        if (!validateKey(key)) {
            return CompletableFuture.completedFuture(null);
        }

        if (config.getAsyncLoader() != null) {
            V cachedValue = doGet(key);
            if (cachedValue != null) {
                return CompletableFuture.completedFuture(cachedValue);
            }

            return config.getAsyncLoader().apply(key).thenApply(value -> {
                if (value != null) {
                    put(key, value);
                }
                return value;
            });
        }

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

    // ===== CONFIGURATION AND STATISTICS =====

    @Override
    public final CacheConfig<K, V> config() {
        return config;
    }

    @Override
    public final CacheStats stats() {
        if (statsEnabled) {
            updateStatsFromCounters();
            return stats.snapshot();
        }
        return stats;
    }

    // ===== ABSTRACT METHODS FOR IMPLEMENTATION-SPECIFIC BEHAVIOR =====

    /**
     * Implementation-specific get operation.
     *
     * @param key the key (already validated)
     * @return the value or null if not found/expired
     */
    protected abstract V doGet(K key);

    /**
     * Implementation-specific put operation.
     *
     * @param key   the key (already validated)
     * @param value the value (already validated)
     */
    protected abstract void doPut(K key, V value);

    /**
     * Implementation-specific remove operation.
     *
     * @param key the key (already validated)
     * @return the removed value or null if not found
     */
    protected abstract V doRemove(K key);

    /**
     * Implementation-specific clear operation.
     */
    protected abstract void doClear();

    /**
     * Implementation-specific containsKey operation.
     *
     * @param key the key (already validated)
     * @return true if key exists and is not expired
     */
    protected abstract boolean doContainsKey(K key);

    // ===== UTILITY METHODS =====

    /**
     * Validates that a key is not null.
     */
    protected boolean validateKey(K key) {
        return key != null;
    }

    /**
     * Validates that a value is acceptable.
     */
    protected boolean validateValue(V value) {
        return true; // Allow null values
    }

    /**
     * Checks if an entry is valid (not expired, etc.).
     */
    protected boolean isValidEntry(CacheEntry<V> entry) {
        if (entry == null) {
            return false;
        }

        if (hasExpiration && entry.isExpired()) {
            return false;
        }

        return true;
    }

    /**
     * Records statistics for get operations.
     */
    protected void recordGetStatistics(boolean hit) {
        if (statsEnabled) {
            if (hit) {
                hitCount.incrementAndGet();
                stats.getHitCount().incrementAndGet();
            } else {
                missCount.incrementAndGet();
                stats.getMissCount().incrementAndGet();
            }
        }
    }

    /**
     * Records statistics for put operations.
     */
    protected void recordPutStatistics() {
        // Override in subclasses for specific put statistics
    }

    /**
     * Records statistics for remove operations.
     */
    protected void recordRemoveStatistics(boolean removed) {
        // Override in subclasses for specific remove statistics
    }

    /**
     * Records statistics for clear operations.
     */
    protected void recordClearStatistics(long sizeBefore) {
        // Override in subclasses for specific clear statistics
    }

    /**
     * Updates statistics from internal counters.
     */
    protected void updateStatsFromCounters() {
        stats.getHitCount().set(hitCount.get());
        stats.getMissCount().set(missCount.get());
    }

    /**
     * Checks if size limit has been reached.
     */
    protected boolean isSizeLimitReached() {
        if (hasMaximumSize && size() > maximumSize) {
            return true;
        }

        if (hasMaximumWeight && config.getMaximumWeight() != null) {
            return currentWeight.get() > config.getMaximumWeight();
        }

        return false;
    }

    /**
     * Triggers eviction when size limit is reached.
     */
    protected void triggerEviction() {
        if (evictionStrategy != null) {
            K evictedKey = evictionStrategy.selectEvictionCandidate(data);
            if (evictedKey != null) {
                CacheEntry<V> evictedEntry = data.remove(evictedKey);
                if (evictedEntry != null) {
                    currentSize.decrementAndGet();
                    eventListener.onEvict(evictedKey, evictedEntry.getValue(),
                            EvictionReason.SIZE);
                }
            }
        }
    }

    /**
     * Calculates optimal capacity for the underlying ConcurrentHashMap.
     */
    protected static int calculateOptimalCapacity(long maximumSize) {
        if (maximumSize <= 0) {
            return 16; // Default minimum
        }

        // Calculate next power of 2, but cap at reasonable maximum
        long capacity = Math.min(maximumSize * 2, Integer.MAX_VALUE / 2);
        return Math.max(16, Integer.highestOneBit((int) capacity) << 1);
    }

    /**
     * Gets the stripe index for a key.
     */
    protected int getStripeIndex(K key) {
        return key.hashCode() & (STRIPE_COUNT - 1);
    }

    /**
     * Schedules cleanup operations for expired entries.
     */
    protected void scheduleCleanup() {
        scheduler.scheduleWithFixedDelay(
                this::cleanupExpiredEntries,
                CLEANUP_INTERVAL_SECONDS,
                CLEANUP_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    /**
     * Schedules refresh operations.
     */
    protected void scheduleRefresh() {
        scheduler.scheduleWithFixedDelay(
                this::refreshStaleEntries,
                REFRESH_INTERVAL_SECONDS,
                REFRESH_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    /**
     * Cleans up expired entries.
     */
    protected void cleanupExpiredEntries() {
        if (!hasExpiration) {
            return;
        }

        data.entrySet().removeIf(entry -> {
            if (!isValidEntry(entry.getValue())) {
                currentSize.decrementAndGet();
                eventListener.onExpire(entry.getKey(), entry.getValue().getValue());
                return true;
            }
            return false;
        });
    }

    /**
     * Refreshes stale entries.
     */
    protected void refreshStaleEntries() {
        // Implementation depends on specific cache requirements
        // Override in subclasses as needed
    }

    /**
     * Shuts down the cache and cleans up resources.
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
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

    // ===== COMMON INNER CLASSES =====

    /**
     * Immutable map entry for collection views.
     */
    protected static class ImmutableMapEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private final V value;

        public ImmutableMapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("Entry is immutable");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            return java.util.Objects.equals(key, entry.getKey()) &&
                    java.util.Objects.equals(value, entry.getValue());
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(key, value);
        }
    }
}
