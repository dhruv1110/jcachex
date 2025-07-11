package io.github.dhruv1110.jcachex;

import io.github.dhruv1110.jcachex.eviction.EvictionStrategy;
import io.github.dhruv1110.jcachex.impl.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Fluent builder for creating JCacheX cache instances with enhanced type
 * support.
 * <p>
 * This builder provides a convenient, type-safe way to create different types
 * of
 * cache implementations with various configurations. It supports all cache
 * types
 * available in JCacheX and provides sensible defaults for different use cases.
 * </p>
 *
 * <h3>Basic Usage Examples:</h3>
 * 
 * <pre>{@code
 * // Simple cache with default settings
 * Cache<String, User> cache = CacheBuilder.<String, User>newBuilder()
 *         .maximumSize(1000L)
 *         .build();
 *
 * // High-performance cache for read-heavy workloads
 * Cache<String, Product> productCache = CacheBuilder.<String, Product>newBuilder()
 *         .maximumSize(5000L)
 *         .expireAfterWrite(Duration.ofMinutes(30))
 *         .cacheType(CacheType.READ_ONLY_OPTIMIZED)
 *         .frequencySketchType(FrequencySketchType.OPTIMIZED)
 *         .build();
 *
 * // Memory-optimized cache for constrained environments
 * Cache<String, Data> dataCache = CacheBuilder.<String, Data>newBuilder()
 *         .maximumSize(2000L)
 *         .cacheType(CacheType.ALLOCATION_OPTIMIZED)
 *         .frequencySketchType(FrequencySketchType.NONE)
 *         .build();
 * }</pre>
 *
 * <h3>Cache Types:</h3>
 * <ul>
 * <li><strong>DEFAULT:</strong> Standard cache implementation</li>
 * <li><strong>OPTIMIZED:</strong> Advanced eviction with TinyWindowLFU</li>
 * <li><strong>JIT_OPTIMIZED:</strong> JIT-friendly implementation</li>
 * <li><strong>ALLOCATION_OPTIMIZED:</strong> Minimal allocation overhead</li>
 * <li><strong>LOCALITY_OPTIMIZED:</strong> CPU cache-friendly layout</li>
 * <li><strong>ZERO_COPY_OPTIMIZED:</strong> Zero-copy operations</li>
 * <li><strong>READ_ONLY_OPTIMIZED:</strong> Read-heavy workloads</li>
 * <li><strong>WRITE_HEAVY_OPTIMIZED:</strong> Write-intensive workloads</li>
 * <li><strong>JVM_OPTIMIZED:</strong> GC-aware optimizations</li>
 * <li><strong>HARDWARE_OPTIMIZED:</strong> Hardware-specific optimizations</li>
 * <li><strong>ML_OPTIMIZED:</strong> Machine learning-based eviction</li>
 * <li><strong>PROFILED_OPTIMIZED:</strong> Development and testing</li>
 * </ul>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 * @since 1.0.0
 */
public final class CacheBuilder<K, V> {

    /**
     * Enumeration of available cache types.
     */
    public enum CacheType {
        DEFAULT,
        OPTIMIZED,
        JIT_OPTIMIZED,
        ALLOCATION_OPTIMIZED,
        LOCALITY_OPTIMIZED,
        ZERO_COPY_OPTIMIZED,
        READ_ONLY_OPTIMIZED,
        WRITE_HEAVY_OPTIMIZED,
        JVM_OPTIMIZED,
        HARDWARE_OPTIMIZED,
        ML_OPTIMIZED,
        PROFILED_OPTIMIZED
    }

    private final CacheConfig.Builder<K, V> configBuilder;
    private CacheType cacheType = CacheType.DEFAULT;

    private CacheBuilder() {
        this.configBuilder = CacheConfig.newBuilder();
    }

    /**
     * Creates a new cache builder.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a new cache builder instance
     */
    public static <K, V> CacheBuilder<K, V> newBuilder() {
        return new CacheBuilder<>();
    }

    /**
     * Sets the cache type to use.
     *
     * @param cacheType the cache type
     * @return this builder instance
     */
    public CacheBuilder<K, V> cacheType(CacheType cacheType) {
        this.cacheType = cacheType;
        return this;
    }

    /**
     * Sets the maximum number of entries the cache may contain.
     *
     * @param maximumSize the maximum size
     * @return this builder instance
     */
    public CacheBuilder<K, V> maximumSize(long maximumSize) {
        configBuilder.maximumSize(maximumSize);
        return this;
    }

    /**
     * Sets the maximum total weight of entries the cache may contain.
     *
     * @param maximumWeight the maximum weight
     * @return this builder instance
     */
    public CacheBuilder<K, V> maximumWeight(long maximumWeight) {
        configBuilder.maximumWeight(maximumWeight);
        return this;
    }

    /**
     * Sets the weigher function used to calculate entry weights.
     *
     * @param weigher the weigher function
     * @return this builder instance
     */
    public CacheBuilder<K, V> weigher(BiFunction<K, V, Long> weigher) {
        configBuilder.weigher(weigher);
        return this;
    }

    /**
     * Sets the duration after which an entry should expire after being written.
     *
     * @param duration the expiration duration
     * @return this builder instance
     */
    public CacheBuilder<K, V> expireAfterWrite(Duration duration) {
        configBuilder.expireAfterWrite(duration);
        return this;
    }

    /**
     * Sets the duration after which an entry should expire after being written.
     *
     * @param duration the duration value
     * @param unit     the time unit
     * @return this builder instance
     */
    public CacheBuilder<K, V> expireAfterWrite(long duration, TimeUnit unit) {
        configBuilder.expireAfterWrite(duration, unit);
        return this;
    }

    /**
     * Sets the duration after which an entry should expire after being accessed.
     *
     * @param duration the expiration duration
     * @return this builder instance
     */
    public CacheBuilder<K, V> expireAfterAccess(Duration duration) {
        configBuilder.expireAfterAccess(duration);
        return this;
    }

    /**
     * Sets the duration after which an entry should expire after being accessed.
     *
     * @param duration the duration value
     * @param unit     the time unit
     * @return this builder instance
     */
    public CacheBuilder<K, V> expireAfterAccess(long duration, TimeUnit unit) {
        configBuilder.expireAfterAccess(duration, unit);
        return this;
    }

    /**
     * Sets the eviction strategy to use.
     *
     * @param evictionStrategy the eviction strategy
     * @return this builder instance
     */
    public CacheBuilder<K, V> evictionStrategy(EvictionStrategy<K, V> evictionStrategy) {
        configBuilder.evictionStrategy(evictionStrategy);
        return this;
    }

    /**
     * Sets the frequency sketch type for tracking access patterns.
     *
     * @param frequencySketchType the frequency sketch type
     * @return this builder instance
     */
    public CacheBuilder<K, V> frequencySketchType(FrequencySketchType frequencySketchType) {
        configBuilder.frequencySketchType(frequencySketchType);
        return this;
    }

    /**
     * Enables weak key references.
     *
     * @return this builder instance
     */
    public CacheBuilder<K, V> weakKeys() {
        configBuilder.weakKeys(true);
        return this;
    }

    /**
     * Enables weak value references.
     *
     * @return this builder instance
     */
    public CacheBuilder<K, V> weakValues() {
        configBuilder.weakValues(true);
        return this;
    }

    /**
     * Enables soft value references.
     *
     * @return this builder instance
     */
    public CacheBuilder<K, V> softValues() {
        configBuilder.softValues(true);
        return this;
    }

    /**
     * Sets the loader function for automatic value loading.
     *
     * @param loader the loader function
     * @return this builder instance
     */
    public CacheBuilder<K, V> loader(Function<K, V> loader) {
        configBuilder.loader(loader);
        return this;
    }

    /**
     * Sets the async loader function for automatic value loading.
     *
     * @param asyncLoader the async loader function
     * @return this builder instance
     */
    public CacheBuilder<K, V> asyncLoader(Function<K, CompletableFuture<V>> asyncLoader) {
        configBuilder.asyncLoader(asyncLoader);
        return this;
    }

    /**
     * Sets the refresh duration for automatic cache refresh.
     *
     * @param duration the refresh duration
     * @return this builder instance
     */
    public CacheBuilder<K, V> refreshAfterWrite(Duration duration) {
        configBuilder.refreshAfterWrite(duration);
        return this;
    }

    /**
     * Enables statistics recording.
     *
     * @return this builder instance
     */
    public CacheBuilder<K, V> recordStats() {
        configBuilder.recordStats(true);
        return this;
    }

    /**
     * Sets whether to record statistics.
     *
     * @param recordStats whether to record statistics
     * @return this builder instance
     */
    public CacheBuilder<K, V> recordStats(boolean recordStats) {
        configBuilder.recordStats(recordStats);
        return this;
    }

    /**
     * Sets the initial capacity hint for the cache's internal data structure.
     *
     * @param initialCapacity the initial capacity
     * @return this builder instance
     */
    public CacheBuilder<K, V> initialCapacity(int initialCapacity) {
        configBuilder.initialCapacity(initialCapacity);
        return this;
    }

    /**
     * Sets the concurrency level hint for the cache's internal data structure.
     *
     * @param concurrencyLevel the concurrency level
     * @return this builder instance
     */
    public CacheBuilder<K, V> concurrencyLevel(int concurrencyLevel) {
        configBuilder.concurrencyLevel(concurrencyLevel);
        return this;
    }

    /**
     * Adds an event listener to the cache.
     *
     * @param listener the event listener
     * @return this builder instance
     */
    public CacheBuilder<K, V> listener(CacheEventListener<K, V> listener) {
        configBuilder.addListener(listener);
        return this;
    }

    /**
     * Builds and returns the configured cache instance.
     *
     * @return the configured cache
     */
    public Cache<K, V> build() {
        CacheConfig<K, V> config = configBuilder.build();
        return createCacheByType(cacheType, config);
    }

    /**
     * Creates a cache instance based on the specified type.
     */
    @SuppressWarnings("unchecked")
    private Cache<K, V> createCacheByType(CacheType cacheType, CacheConfig<K, V> config) {
        switch (cacheType) {
            case OPTIMIZED:
                return new OptimizedCache<>(config);
            case JIT_OPTIMIZED:
                return new JITOptimizedCache<>(config);
            case ALLOCATION_OPTIMIZED:
                return new AllocationOptimizedCache<>(config);
            case LOCALITY_OPTIMIZED:
                return new CacheLocalityOptimizedCache<>(config);
            case ZERO_COPY_OPTIMIZED:
                return new ZeroCopyOptimizedCache<>(config);
            case READ_ONLY_OPTIMIZED:
                return new ReadOnlyOptimizedCache<>(config);
            case WRITE_HEAVY_OPTIMIZED:
                return new WriteHeavyOptimizedCache<>(config);
            case JVM_OPTIMIZED:
                return new JVMOptimizedCache<>(config);
            case HARDWARE_OPTIMIZED:
                return new HardwareOptimizedCache<>(config);
            case ML_OPTIMIZED:
                return new MLOptimizedCache<>(config);
            case PROFILED_OPTIMIZED:
                return new ProfiledOptimizedCache<>(config);
            case DEFAULT:
            default:
                return new DefaultCache<>(config);
        }
    }

    // ===== CONVENIENCE METHODS FOR SPECIFIC CACHE TYPES =====

    /**
     * Creates a builder pre-configured for high-performance read workloads.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> CacheBuilder<K, V> forReadHeavyWorkload() {
        return CacheBuilder.<K, V>newBuilder()
                .cacheType(CacheType.READ_ONLY_OPTIMIZED)
                .frequencySketchType(FrequencySketchType.OPTIMIZED)
                .recordStats();
    }

    /**
     * Creates a builder pre-configured for high-performance write workloads.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> CacheBuilder<K, V> forWriteHeavyWorkload() {
        return CacheBuilder.<K, V>newBuilder()
                .cacheType(CacheType.WRITE_HEAVY_OPTIMIZED)
                .frequencySketchType(FrequencySketchType.BASIC)
                .recordStats();
    }

    /**
     * Creates a builder pre-configured for memory-constrained environments.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> CacheBuilder<K, V> forMemoryConstrainedEnvironment() {
        return CacheBuilder.<K, V>newBuilder()
                .cacheType(CacheType.ALLOCATION_OPTIMIZED)
                .frequencySketchType(FrequencySketchType.NONE)
                .recordStats(false);
    }

    /**
     * Creates a builder pre-configured for high-performance scenarios.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> CacheBuilder<K, V> forHighPerformance() {
        return CacheBuilder.<K, V>newBuilder()
                .cacheType(CacheType.JIT_OPTIMIZED)
                .frequencySketchType(FrequencySketchType.OPTIMIZED)
                .recordStats();
    }

    /**
     * Creates a builder pre-configured for machine learning workloads.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> CacheBuilder<K, V> forMachineLearning() {
        return CacheBuilder.<K, V>newBuilder()
                .cacheType(CacheType.ML_OPTIMIZED)
                .frequencySketchType(FrequencySketchType.OPTIMIZED)
                .recordStats();
    }

    /**
     * Creates a builder pre-configured for development and testing.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> CacheBuilder<K, V> forDevelopment() {
        return CacheBuilder.<K, V>newBuilder()
                .cacheType(CacheType.PROFILED_OPTIMIZED)
                .frequencySketchType(FrequencySketchType.BASIC)
                .recordStats();
    }
}
