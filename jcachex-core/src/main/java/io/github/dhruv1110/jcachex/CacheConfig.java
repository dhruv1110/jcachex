package io.github.dhruv1110.jcachex;

import io.github.dhruv1110.jcachex.eviction.EvictionStrategy;
import io.github.dhruv1110.jcachex.eviction.WindowTinyLFUEvictionStrategy;
import io.github.dhruv1110.jcachex.exceptions.CacheConfigurationException;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;

/**
 * Configuration options for a JCacheX cache instance.
 * <p>
 * This class provides a comprehensive set of configuration options to customize
 * cache behavior including size limits, expiration policies, eviction
 * strategies,
 * and performance tuning. JCacheX is optimized for high performance with
 * nanoTime-based operations and immediate eviction for minimal latency.
 * </p>
 *
 * <h3>Basic Usage Examples:</h3>
 *
 * <pre>{@code
 * // Simple cache with size limit
 * CacheConfig<String, String> config = CacheConfig.<String, String>builder()
 *         .maximumSize(1000L)
 *         .build();
 *
 * // Cache with time-based expiration
 * CacheConfig<String, User> userConfig = CacheConfig.<String, User>builder()
 *         .maximumSize(500L)
 *         .expireAfterWrite(Duration.ofMinutes(15))
 *         .expireAfterAccess(Duration.ofMinutes(5))
 *         .build();
 *
 * // Cache with custom loader and statistics
 * CacheConfig<String, String> loadingConfig = CacheConfig.<String, String>builder()
 *         .maximumSize(200L)
 *         .loader(key -> loadFromDatabase(key))
 *         .recordStats(true)
 *         .build();
 * }</pre>
 *
 * <h3>Advanced Configuration Examples:</h3>
 *
 * <pre>{@code
 * // Cache with custom eviction strategy and listeners
 * CacheConfig<String, LargeObject> advancedConfig = CacheConfig.<String, LargeObject>builder()
 *         .maximumWeight(10_000L)
 *         .weigher((key, value) -> value.getSize())
 *         .evictionStrategy(new LRUEvictionStrategy<>())
 *         .addListener(new CacheEventListener<String, LargeObject>() { @Override
 *             public void onEvict(String key, LargeObject value, EvictionReason reason) {
 *                 System.out.println("Evicted: " + key + " due to " + reason);
 *             }
 *             // ... other methods
 *         })
 *         .build();
 *
 * // Async loading cache with refresh
 * CacheConfig<String, String> asyncConfig = CacheConfig.<String, String>builder()
 *         .maximumSize(1000L)
 *         .asyncLoader(key -> CompletableFuture.supplyAsync(() -> loadAsync(key)))
 *         .refreshAfterWrite(Duration.ofMinutes(10))
 *         .build();
 * }</pre>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @see io.github.dhruv1110.jcachex.impl.DefaultCache
 * @see CacheEventListener
 * @see EvictionStrategy
 * @since 1.0.0
 */
public class CacheConfig<K, V> {
    private final Long maximumSize;
    private final Long maximumWeight;
    private final BiFunction<K, V, Long> weigher;
    private final Duration expireAfterWrite;
    private final Duration expireAfterAccess;
    private final EvictionStrategy<K, V> evictionStrategy;
    private final FrequencySketchType frequencySketchType;
    private final boolean weakKeys;
    private final boolean weakValues;
    private final boolean softValues;
    private final Function<K, V> loader;
    private final Function<K, CompletableFuture<V>> asyncLoader;
    private final Duration refreshAfterWrite;
    private final boolean recordStats;
    private final int initialCapacity;
    private final int concurrencyLevel;
    private final String directory;
    private final Set<CacheEventListener<K, V>> listeners;

    private CacheConfig(Builder<K, V> builder) {
        this.maximumSize = builder.maximumSize;
        this.maximumWeight = builder.maximumWeight;
        this.weigher = builder.weigher;
        this.expireAfterWrite = builder.expireAfterWrite;
        this.expireAfterAccess = builder.expireAfterAccess;
        this.evictionStrategy = builder.evictionStrategy;
        this.frequencySketchType = builder.frequencySketchType;
        this.weakKeys = builder.weakKeys;
        this.weakValues = builder.weakValues;
        this.softValues = builder.softValues;
        this.loader = builder.loader;
        this.asyncLoader = builder.asyncLoader;
        this.refreshAfterWrite = builder.refreshAfterWrite;
        this.recordStats = builder.recordStats;
        this.initialCapacity = builder.initialCapacity;
        this.concurrencyLevel = builder.concurrencyLevel;
        this.directory = builder.directory;
        this.listeners = new HashSet<>(builder.listeners);
    }

    /**
     * Returns the maximum number of entries the cache may contain.
     *
     * @return the maximum size, or null if no limit is set
     */
    public Long getMaximumSize() {
        return maximumSize;
    }

    /**
     * Returns the maximum total weight of entries the cache may contain.
     *
     * @return the maximum weight, or null if no limit is set
     */
    public Long getMaximumWeight() {
        return maximumWeight;
    }

    /**
     * Returns the weigher function used to calculate entry weights.
     *
     * @return the weigher function, or null if not set
     */
    public BiFunction<K, V, Long> getWeigher() {
        return weigher;
    }

    /**
     * Returns the duration after which an entry should be automatically removed
     * from the cache once the duration has elapsed after the entry's creation
     * or last update.
     *
     * @return the expiration duration, or null if not set
     */
    public Duration getExpireAfterWrite() {
        return expireAfterWrite;
    }

    /**
     * Returns the duration after which an entry should be automatically removed
     * from the cache once the duration has elapsed after the entry's creation,
     * last update, or last access.
     *
     * @return the expiration duration, or null if not set
     */
    public Duration getExpireAfterAccess() {
        return expireAfterAccess;
    }

    /**
     * Returns the eviction strategy used to determine which entries to remove
     * when the cache size or weight limits are exceeded.
     *
     * @return the eviction strategy, or null if not set (will use TinyWindowLFU by
     *         default)
     */
    public EvictionStrategy<K, V> getEvictionStrategy() {
        return evictionStrategy;
    }

    /**
     * Returns the frequency sketch type used for tracking access patterns.
     *
     * @return the frequency sketch type, defaults to BASIC
     */
    public FrequencySketchType getFrequencySketchType() {
        return frequencySketchType;
    }

    /**
     * Returns whether cache keys should be stored using weak references.
     *
     * @return true if weak keys are enabled, false otherwise
     */
    public boolean isWeakKeys() {
        return weakKeys;
    }

    /**
     * Returns whether cache values should be stored using weak references.
     *
     * @return true if weak values are enabled, false otherwise
     */
    public boolean isWeakValues() {
        return weakValues;
    }

    /**
     * Returns whether cache values should be stored using soft references.
     *
     * @return true if soft values are enabled, false otherwise
     */
    public boolean isSoftValues() {
        return softValues;
    }

    /**
     * Returns the function used to load values synchronously when not present in
     * cache.
     *
     * @return the loader function, or null if not set
     */
    public Function<K, V> getLoader() {
        return loader;
    }

    /**
     * Returns the function used to load values asynchronously when not present in
     * cache.
     *
     * @return the async loader function, or null if not set
     */
    public Function<K, CompletableFuture<V>> getAsyncLoader() {
        return asyncLoader;
    }

    /**
     * Returns the duration after which an entry should be automatically refreshed
     * once the duration has elapsed after the entry's creation or last update.
     *
     * @return the refresh duration, or null if not set
     */
    public Duration getRefreshAfterWrite() {
        return refreshAfterWrite;
    }

    /**
     * Returns whether statistics should be recorded for this cache.
     *
     * @return true if statistics recording is enabled, false otherwise
     */
    public boolean isRecordStats() {
        return recordStats;
    }

    /**
     * Returns the initial capacity of the cache's internal data structure.
     *
     * @return the initial capacity
     */
    public int getInitialCapacity() {
        return initialCapacity;
    }

    /**
     * Returns the concurrency level for the cache's internal data structure.
     *
     * @return the concurrency level
     */
    public int getConcurrencyLevel() {
        return concurrencyLevel;
    }

    /**
     * Returns the directory path for persistent cache storage.
     *
     * @return the directory path, or null if not set
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Returns a copy of the set of event listeners registered for this cache.
     *
     * @return the event listeners
     */
    public Set<CacheEventListener<K, V>> getListeners() {
        return new HashSet<>(listeners);
    }

    /**
     * Creates a new builder instance for constructing cache configurations.
     *
     * @param <K> the type of keys maintained by the cache
     * @param <V> the type of mapped values
     * @return a new builder instance
     */
    public static <K, V> Builder<K, V> newBuilder() {
        return new Builder<>();
    }

    /**
     * Creates a new builder instance for constructing cache configurations.
     *
     * @param <K> the type of keys maintained by the cache
     * @param <V> the type of mapped values
     * @return a new builder instance
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    /**
     * Builder class for constructing {@link CacheConfig} instances.
     * <p>
     * This builder follows the fluent interface pattern, allowing method chaining
     * for convenient configuration setup.
     * </p>
     *
     * <h3>Usage Example:</h3>
     *
     * <pre>{@code
     * CacheConfig<String, User> config = CacheConfig.<String, User>builder()
     *         .maximumSize(1000L)
     *         .expireAfterWrite(Duration.ofMinutes(30))
     *         .recordStats(true)
     *         .addListener(new MyEventListener())
     *         .build();
     * }</pre>
     *
     * @param <K> the type of keys maintained by the cache
     * @param <V> the type of mapped values
     */
    public static class Builder<K, V> {
        private Long maximumSize;
        private Long maximumWeight;
        private BiFunction<K, V, Long> weigher;
        private Duration expireAfterWrite;
        private Duration expireAfterAccess;
        private EvictionStrategy<K, V> evictionStrategy;
        private FrequencySketchType frequencySketchType = FrequencySketchType.BASIC;
        private boolean weakKeys;
        private boolean weakValues;
        private boolean softValues;
        private Function<K, V> loader;
        private Function<K, CompletableFuture<V>> asyncLoader;
        private Duration refreshAfterWrite;
        private boolean recordStats = true;
        private int initialCapacity = 16;
        private int concurrencyLevel = 16;
        private String directory;
        private Set<CacheEventListener<K, V>> listeners = new HashSet<>();

        /**
         * Sets the maximum number of entries the cache may contain.
         * <p>
         * When this limit is exceeded, the cache will evict entries according to
         * the configured eviction strategy.
         * </p>
         *
         * @param maximumSize the maximum size, must be positive
         * @return this builder instance
         * @throws IllegalArgumentException if maximumSize is not positive
         */
        public Builder<K, V> maximumSize(Long maximumSize) {
            if (maximumSize != null && maximumSize <= 0) {
                throw CacheConfigurationException.invalidMaximumSize(maximumSize);
            }
            this.maximumSize = maximumSize;
            return this;
        }

        /**
         * Sets the maximum total weight of entries the cache may contain.
         * <p>
         * This requires a weigher function to be set. When this limit is exceeded,
         * the cache will evict entries according to the configured eviction strategy.
         * </p>
         *
         * @param maximumWeight the maximum weight, must be positive
         * @return this builder instance
         * @throws IllegalArgumentException if maximumWeight is not positive
         */
        public Builder<K, V> maximumWeight(Long maximumWeight) {
            if (maximumWeight != null && maximumWeight <= 0) {
                throw CacheConfigurationException.invalidMaximumWeight(maximumWeight);
            }
            this.maximumWeight = maximumWeight;
            return this;
        }

        /**
         * Sets the weigher function used to calculate entry weights.
         * <p>
         * This is required when using {@link #maximumWeight(Long)}.
         * </p>
         *
         * <h3>Example:</h3>
         *
         * <pre>{@code
         * .weigher((key, value) -> value.toString().length())
         * }</pre>
         *
         * @param weigher the weigher function
         * @return this builder instance
         */
        public Builder<K, V> weigher(BiFunction<K, V, Long> weigher) {
            this.weigher = weigher;
            return this;
        }

        /**
         * Sets the duration after which an entry should be automatically removed
         * from the cache once the duration has elapsed after the entry's creation
         * or last update.
         *
         * @param duration the expiration duration, must not be negative
         * @return this builder instance
         * @throws IllegalArgumentException if duration is negative
         */
        public Builder<K, V> expireAfterWrite(Duration duration) {
            this.expireAfterWrite = duration;
            return this;
        }

        /**
         * Sets the duration after which an entry should be automatically removed
         * from the cache once the duration has elapsed after the entry's creation
         * or last update.
         *
         * @param duration the expiration duration, must not be negative
         * @param unit     the time unit of the duration argument
         * @return this builder instance
         * @throws IllegalArgumentException if duration is negative
         */
        public Builder<K, V> expireAfterWrite(long duration, TimeUnit unit) {
            this.expireAfterWrite = Duration.ofNanos(unit.toNanos(duration));
            return this;
        }

        /**
         * Sets the duration after which an entry should be automatically removed
         * from the cache once the duration has elapsed after the entry's creation,
         * last update, or last access.
         *
         * @param duration the expiration duration, must not be negative
         * @return this builder instance
         * @throws IllegalArgumentException if duration is negative
         */
        public Builder<K, V> expireAfterAccess(Duration duration) {
            this.expireAfterAccess = duration;
            return this;
        }

        /**
         * Sets the duration after which an entry should be automatically removed
         * from the cache once the duration has elapsed after the entry's creation,
         * last update, or last access.
         *
         * @param duration the expiration duration, must not be negative
         * @param unit     the time unit of the duration argument
         * @return this builder instance
         * @throws IllegalArgumentException if duration is negative
         */
        public Builder<K, V> expireAfterAccess(long duration, TimeUnit unit) {
            this.expireAfterAccess = Duration.ofNanos(unit.toNanos(duration));
            return this;
        }

        /**
         * Sets the eviction strategy used to determine which entries to remove
         * when the cache size or weight limits are exceeded.
         * <p>
         * If not set, the cache will use TinyWindowLFU eviction by default for optimal
         * performance.
         * </p>
         *
         * @param evictionStrategy the eviction strategy to use
         * @return this builder instance
         */
        public Builder<K, V> evictionStrategy(EvictionStrategy<K, V> evictionStrategy) {
            this.evictionStrategy = evictionStrategy;
            return this;
        }

        /**
         * Sets the frequency sketch type to use for tracking access patterns.
         * <p>
         * Frequency sketches are used by eviction strategies to track access frequency
         * with minimal memory overhead. Different types provide different trade-offs
         * between memory usage, accuracy, and performance.
         * </p>
         *
         * @param frequencySketchType the frequency sketch type to use
         * @return this builder instance
         */
        public Builder<K, V> frequencySketchType(FrequencySketchType frequencySketchType) {
            this.frequencySketchType = frequencySketchType;
            return this;
        }

        /**
         * Configures the cache to store keys using weak references.
         * <p>
         * This allows keys to be garbage collected when they are not referenced
         * elsewhere in the application.
         * </p>
         *
         * @param weakKeys true to enable weak key references
         * @return this builder instance
         */
        public Builder<K, V> weakKeys(boolean weakKeys) {
            this.weakKeys = weakKeys;
            return this;
        }

        /**
         * Configures the cache to store values using weak references.
         * <p>
         * This allows values to be garbage collected when they are not referenced
         * elsewhere in the application.
         * </p>
         *
         * @param weakValues true to enable weak value references
         * @return this builder instance
         */
        public Builder<K, V> weakValues(boolean weakValues) {
            this.weakValues = weakValues;
            return this;
        }

        /**
         * Configures the cache to store values using soft references.
         * <p>
         * This allows values to be garbage collected when memory is low.
         * </p>
         *
         * @param softValues true to enable soft value references
         * @return this builder instance
         */
        public Builder<K, V> softValues(boolean softValues) {
            this.softValues = softValues;
            return this;
        }

        /**
         * Sets the function used to load values synchronously when not present in
         * cache.
         * <p>
         * This enables the cache to automatically load missing values on demand.
         * </p>
         *
         * <h3>Example:</h3>
         *
         * <pre>{@code
         * .loader(key -> userService.findById(key))
         * }</pre>
         *
         * @param loader the loader function
         * @return this builder instance
         */
        public Builder<K, V> loader(Function<K, V> loader) {
            this.loader = loader;
            return this;
        }

        /**
         * Sets the function used to load values asynchronously when not present in
         * cache.
         * <p>
         * This enables the cache to automatically load missing values on demand
         * without blocking the calling thread.
         * </p>
         *
         * <h3>Example:</h3>
         *
         * <pre>{@code
         * .asyncLoader(key -> CompletableFuture.supplyAsync(() -> userService.findById(key)))
         * }</pre>
         *
         * @param asyncLoader the async loader function
         * @return this builder instance
         */
        public Builder<K, V> asyncLoader(Function<K, CompletableFuture<V>> asyncLoader) {
            this.asyncLoader = asyncLoader;
            return this;
        }

        /**
         * Sets the duration after which an entry should be automatically refreshed
         * once the duration has elapsed after the entry's creation or last update.
         * <p>
         * This requires a loader function to be set. Refresh operations happen
         * asynchronously and do not block cache access.
         * </p>
         *
         * @param duration the refresh duration, must not be negative
         * @return this builder instance
         * @throws IllegalArgumentException if duration is negative
         */
        public Builder<K, V> refreshAfterWrite(Duration duration) {
            this.refreshAfterWrite = duration;
            return this;
        }

        /**
         * Sets whether statistics should be recorded for this cache.
         * <p>
         * Statistics include hit/miss rates, eviction counts, and load times.
         * Enabling statistics has a small performance overhead.
         * </p>
         *
         * @param recordStats true to enable statistics recording
         * @return this builder instance
         */
        public Builder<K, V> recordStats(boolean recordStats) {
            this.recordStats = recordStats;
            return this;
        }

        /**
         * Sets the initial capacity of the cache's internal data structure.
         * <p>
         * This is a performance hint and does not limit the cache size.
         * </p>
         *
         * @param initialCapacity the initial capacity, must be positive
         * @return this builder instance
         * @throws IllegalArgumentException if initialCapacity is not positive
         */
        public Builder<K, V> initialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
            return this;
        }

        /**
         * Sets the concurrency level for the cache's internal data structure.
         * <p>
         * This is a performance hint that indicates the expected number of
         * concurrent threads that will modify the cache.
         * </p>
         *
         * @param concurrencyLevel the concurrency level, must be positive
         * @return this builder instance
         * @throws IllegalArgumentException if concurrencyLevel is not positive
         */
        public Builder<K, V> concurrencyLevel(int concurrencyLevel) {
            this.concurrencyLevel = concurrencyLevel;
            return this;
        }

        /**
         * Sets the directory path for persistent cache storage.
         * <p>
         * This is currently reserved for future use and does not affect
         * cache behavior in the current implementation.
         * </p>
         *
         * @param directory the directory path
         * @return this builder instance
         */
        public Builder<K, V> directory(String directory) {
            this.directory = directory;
            return this;
        }

        /**
         * Adds an event listener to receive notifications about cache operations.
         * <p>
         * Multiple listeners can be added and all will receive events.
         * </p>
         *
         * <h3>Example:</h3>
         *
         * <pre>{@code
         * .addListener(new CacheEventListener<String, User>() {
         *     &#64;Override
         *     public void onPut(String key, User value) {
         *         logger.info("Added user: " + key);
         *     }
         *     // ... other methods
         * })
         * }</pre>
         *
         * @param listener the event listener to add
         * @return this builder instance
         */
        public Builder<K, V> addListener(CacheEventListener<K, V> listener) {
            this.listeners.add(listener);
            return this;
        }

        /**
         * Builds and returns a new {@link CacheConfig} instance with the
         * configured settings.
         *
         * @return a new cache configuration
         * @throws IllegalArgumentException if any configuration is invalid
         */
        public CacheConfig<K, V> build() {
            validateConfiguration();
            checkConflictingSettings();
            setDefaults();
            return new CacheConfig<>(this);
        }

        /**
         * Validates the basic configuration parameters.
         */
        private void validateConfiguration() {
            if (maximumSize != null && maximumSize < 1) {
                throw CacheConfigurationException.invalidMaximumSize(maximumSize);
            }
            if (maximumWeight != null && maximumWeight < 1) {
                throw CacheConfigurationException.invalidMaximumWeight(maximumWeight);
            }
            if (expireAfterWrite != null && expireAfterWrite.isNegative()) {
                throw new CacheConfigurationException("Expire after write duration must be non-negative",
                        "INVALID_EXPIRATION");
            }
        }

        /**
         * Checks for conflicting configuration settings.
         */
        private void checkConflictingSettings() {
            if (maximumWeight != null && weigher == null) {
                throw CacheConfigurationException.missingWeigher();
            }

            if (maximumSize != null && maximumWeight != null) {
                throw CacheConfigurationException.conflictingSettings("maximumSize", "maximumWeight");
            }

            if (weakValues && softValues) {
                throw CacheConfigurationException.conflictingSettings("weakValues", "softValues");
            }

            if (loader != null && asyncLoader != null) {
                throw CacheConfigurationException.conflictingSettings("loader", "asyncLoader");
            }
        }

        /**
         * Sets default values for configuration options that weren't explicitly set.
         */
        private void setDefaults() {
            if (evictionStrategy == null) {
                long cacheSize = maximumSize != null ? maximumSize : 1000L;
                evictionStrategy = new WindowTinyLFUEvictionStrategy<>(cacheSize);
            }
        }
    }
}
