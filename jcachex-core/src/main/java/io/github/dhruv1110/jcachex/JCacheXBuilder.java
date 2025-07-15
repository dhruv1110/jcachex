package io.github.dhruv1110.jcachex;

import io.github.dhruv1110.jcachex.impl.*;
import io.github.dhruv1110.jcachex.profiles.CacheProfile;
import io.github.dhruv1110.jcachex.profiles.ProfileName;
import io.github.dhruv1110.jcachex.profiles.ProfileRegistry;
import io.github.dhruv1110.jcachex.profiles.WorkloadCharacteristics;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * The unified cache builder for JCacheX that simplifies cache creation using
 * profiles and smart defaults.
 *
 * <p>
 * This builder eliminates the complexity of choosing between multiple cache
 * implementations
 * and eviction strategies by providing a profile-based approach that
 * automatically selects
 * the optimal configuration for specific use cases.
 * </p>
 *
 * <h2>Key Benefits:</h2>
 * <ul>
 * <li><strong>Simplicity</strong>: Choose by use case, not implementation
 * details</li>
 * <li><strong>Smart Defaults</strong>: Optimal configurations for each
 * profile</li>
 * <li><strong>Automatic Selection</strong>: Best cache implementation chosen
 * automatically</li>
 * <li><strong>Consistent API</strong>: Same interface across all cache
 * types</li>
 * <li><strong>Expert Knowledge</strong>: Profiles encode years of optimization
 * experience</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 *
 * <h3>Profile-Based Creation (Recommended):</h3>
 * 
 * <pre>{@code
 * // Using ProfileName enum for type safety
 * Cache<String, User> userCache = JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY)
 *         .name("users")
 *         .maximumSize(1000L)
 *         .build();
 *
 * // Using convenience methods
 * Cache<String, Product> productCache = JCacheXBuilder.forReadHeavyWorkload()
 *         .name("products")
 *         .maximumSize(5000L)
 *         .expireAfterWrite(Duration.ofMinutes(30))
 *         .build();
 * }</pre>
 *
 * <h3>Smart Defaults (When unsure):</h3>
 * 
 * <pre>{@code
 * // Let JCacheX choose the best profile based on workload characteristics
 * Cache<String, Data> smartCache = JCacheXBuilder.withSmartDefaults()
 *         .name("adaptive-cache")
 *         .maximumSize(1000L)
 *         .workloadCharacteristics(WorkloadCharacteristics.builder()
 *                 .readToWriteRatio(8.0) // Read-heavy
 *                 .accessPattern(WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
 *                 .build())
 *         .build();
 * }</pre>
 *
 * <h3>Simple Cases:</h3>
 * 
 * <pre>{@code
 * // Minimal configuration - uses DEFAULT profile
 * Cache<String, String> simpleCache = JCacheXBuilder.create()
 *         .name("simple")
 *         .maximumSize(100L)
 *         .build();
 * }</pre>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 * @see ProfileName
 * @see ProfileRegistry
 * @see WorkloadCharacteristics
 * @since 1.0.0
 */
public final class JCacheXBuilder<K, V> {

    private final CacheConfig.Builder<K, V> configBuilder;
    private CacheProfile<K, V> profile;
    private String name;
    private WorkloadCharacteristics workloadCharacteristics;
    private boolean useSmartDefaults;

    private JCacheXBuilder(CacheProfile<K, V> profile) {
        this.configBuilder = CacheConfig.newBuilder();
        this.profile = profile;
        this.useSmartDefaults = false;
    }

    private JCacheXBuilder(boolean useSmartDefaults) {
        this.configBuilder = CacheConfig.newBuilder();
        this.profile = null;
        this.useSmartDefaults = useSmartDefaults;
    }

    // ===== PROFILE-BASED CREATION METHODS =====

    /**
     * Creates a new builder with the specified cache profile using ProfileName enum
     * for type safety.
     *
     * @param profileName the profile name enum
     * @param <K>         the key type
     * @param <V>         the value type
     * @return a new builder instance
     */
    @SuppressWarnings("unchecked")
    public static <K, V> JCacheXBuilder<K, V> fromProfile(ProfileName profileName) {
        CacheProfile<?, ?> profile = ProfileRegistry.getProfile(profileName.getValue());
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found: " + profileName.getValue());
        }
        return new JCacheXBuilder<>((CacheProfile<K, V>) profile);
    }

    /**
     * Creates a new builder with the specified cache profile.
     *
     * @param profile the cache profile to use
     * @param <K>     the key type
     * @param <V>     the value type
     * @return a new builder instance
     */
    @SuppressWarnings("unchecked")
    public static <K, V> JCacheXBuilder<K, V> forProfile(CacheProfile<?, ?> profile) {
        return new JCacheXBuilder<>((CacheProfile<K, V>) profile);
    }

    /**
     * Creates a new builder that will automatically select the optimal profile
     * based on workload characteristics.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a new builder instance
     */
    public static <K, V> JCacheXBuilder<K, V> withSmartDefaults() {
        return new JCacheXBuilder<>(true);
    }

    /**
     * Creates a new builder with the DEFAULT profile.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a new builder instance
     */
    public static <K, V> JCacheXBuilder<K, V> create() {
        return fromProfile(ProfileName.DEFAULT);
    }

    // ===== CONVENIENCE METHODS FOR COMMON WORKLOADS =====

    /**
     * Creates a builder optimized for read-heavy workloads (80%+ reads).
     * Uses READ_HEAVY profile with enhanced LFU eviction.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> JCacheXBuilder<K, V> forReadHeavyWorkload() {
        return fromProfile(ProfileName.READ_HEAVY);
    }

    /**
     * Creates a builder optimized for write-heavy workloads (50%+ writes).
     * Uses WRITE_HEAVY profile with enhanced LRU eviction.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> JCacheXBuilder<K, V> forWriteHeavyWorkload() {
        return fromProfile(ProfileName.WRITE_HEAVY);
    }

    /**
     * Creates a builder optimized for memory-constrained environments.
     * Uses MEMORY_EFFICIENT profile with minimal memory footprint.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> JCacheXBuilder<K, V> forMemoryConstrainedEnvironment() {
        return fromProfile(ProfileName.MEMORY_EFFICIENT);
    }

    /**
     * Creates a builder optimized for maximum performance and throughput.
     * Uses HIGH_PERFORMANCE profile with aggressive caching strategies.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> JCacheXBuilder<K, V> forHighPerformance() {
        return fromProfile(ProfileName.HIGH_PERFORMANCE);
    }

    /**
     * Creates a builder optimized for user session storage.
     * Uses SESSION_CACHE profile with time-based expiration.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> JCacheXBuilder<K, V> forSessionStorage() {
        return fromProfile(ProfileName.SESSION_CACHE);
    }

    /**
     * Creates a builder optimized for API response caching.
     * Uses API_CACHE profile with short TTL and network-aware optimizations.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> JCacheXBuilder<K, V> forApiResponseCaching() {
        return fromProfile(ProfileName.API_CACHE);
    }

    /**
     * Creates a builder optimized for expensive computation results.
     * Uses COMPUTE_CACHE profile with long TTL and computation-aware optimizations.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> JCacheXBuilder<K, V> forComputationCaching() {
        return fromProfile(ProfileName.COMPUTE_CACHE);
    }

    /**
     * Creates a builder optimized for machine learning workloads.
     * Uses ML_OPTIMIZED profile with predictive caching capabilities.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> JCacheXBuilder<K, V> forMachineLearning() {
        return fromProfile(ProfileName.ML_OPTIMIZED);
    }

    /**
     * Creates a builder optimized for ultra-low latency requirements.
     * Uses ZERO_COPY profile with minimal memory allocation.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> JCacheXBuilder<K, V> forUltraLowLatency() {
        return fromProfile(ProfileName.ZERO_COPY);
    }

    /**
     * Creates a builder optimized for hardware-specific features.
     * Uses HARDWARE_OPTIMIZED profile with CPU-specific optimizations.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> JCacheXBuilder<K, V> forHardwareOptimization() {
        return fromProfile(ProfileName.HARDWARE_OPTIMIZED);
    }

    /**
     * Creates a builder optimized for distributed caching environments.
     * Uses DISTRIBUTED profile with cluster-aware optimizations.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a pre-configured builder
     */
    public static <K, V> JCacheXBuilder<K, V> forDistributedCaching() {
        return fromProfile(ProfileName.DISTRIBUTED);
    }

    // ===== CONFIGURATION METHODS =====

    /**
     * Sets the cache name for identification and debugging.
     *
     * @param name the cache name
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the workload characteristics for smart profile selection.
     * Only used when builder was created with {@link #withSmartDefaults()}.
     *
     * @param workloadCharacteristics the workload characteristics
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> workloadCharacteristics(WorkloadCharacteristics workloadCharacteristics) {
        this.workloadCharacteristics = workloadCharacteristics;
        return this;
    }

    /**
     * Sets the maximum number of entries the cache may contain.
     *
     * @param maximumSize the maximum size
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> maximumSize(long maximumSize) {
        configBuilder.maximumSize(maximumSize);
        return this;
    }

    /**
     * Sets the maximum total weight of entries the cache may contain.
     *
     * @param maximumWeight the maximum weight
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> maximumWeight(long maximumWeight) {
        configBuilder.maximumWeight(maximumWeight);
        return this;
    }

    /**
     * Sets the duration after which entries are automatically removed after write.
     *
     * @param duration the expiration duration
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> expireAfterWrite(Duration duration) {
        configBuilder.expireAfterWrite(duration);
        return this;
    }

    /**
     * Sets the duration after which entries are automatically removed after access.
     *
     * @param duration the expiration duration
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> expireAfterAccess(Duration duration) {
        configBuilder.expireAfterAccess(duration);
        return this;
    }

    /**
     * Sets the duration after which entries are automatically refreshed.
     *
     * @param duration the refresh duration
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> refreshAfterWrite(Duration duration) {
        configBuilder.refreshAfterWrite(duration);
        return this;
    }

    /**
     * Sets the function to use for loading values when keys are not present.
     *
     * @param loader the value loader function
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> loader(Function<K, V> loader) {
        configBuilder.loader(loader);
        return this;
    }

    /**
     * Sets the function to use for asynchronously loading values.
     *
     * @param asyncLoader the async value loader function
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> asyncLoader(Function<K, CompletableFuture<V>> asyncLoader) {
        configBuilder.asyncLoader(asyncLoader);
        return this;
    }

    /**
     * Sets the weigher function for calculating entry weights.
     *
     * @param weigher the weigher function
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> weigher(java.util.function.BiFunction<K, V, Long> weigher) {
        configBuilder.weigher(weigher);
        return this;
    }

    /**
     * Enables or disables statistics recording.
     *
     * @param recordStats whether to record statistics
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> recordStats(boolean recordStats) {
        configBuilder.recordStats(recordStats);
        return this;
    }

    /**
     * Enables statistics recording.
     *
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> recordStats() {
        return recordStats(true);
    }

    /**
     * Adds an event listener to the cache.
     *
     * @param listener the event listener
     * @return this builder instance
     */
    public JCacheXBuilder<K, V> listener(CacheEventListener<K, V> listener) {
        configBuilder.addListener(listener);
        return this;
    }

    // ===== BUILD METHOD =====

    /**
     * Builds and returns the configured cache instance.
     *
     * @return the configured cache
     */
    public Cache<K, V> build() {
        // Select profile if using smart defaults
        if (useSmartDefaults) {
            profile = selectOptimalProfile();
        }

        // Ensure we have a profile
        if (profile == null) {
            profile = (CacheProfile<K, V>) ProfileRegistry.getProfile(ProfileName.DEFAULT.getValue());
        }

        // Store current configuration values before applying profile
        CacheConfig<K, V> preProfileConfig = configBuilder.build();

        // Apply profile configuration only for values not explicitly set
        if (profile != null) {
            applyProfileDefaults(preProfileConfig);
        }

        // Apply smart defaults based on profile
        applySmartDefaults();

        // Build the configuration
        CacheConfig<K, V> config = configBuilder.build();

        // Validate configuration with profile
        profile.validateConfiguration(config);

        // Create the cache instance
        return createCacheInstance(config);
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Selects the optimal profile based on workload characteristics.
     */
    @SuppressWarnings("unchecked")
    private CacheProfile<K, V> selectOptimalProfile() {
        if (workloadCharacteristics == null) {
            return (CacheProfile<K, V>) ProfileRegistry.getProfile(ProfileName.DEFAULT.getValue());
        }

        // Use ProfileRegistry to find suitable profiles, ordered by priority
        List<CacheProfile<Object, Object>> suitableProfiles = ProfileRegistry
                .findSuitableProfiles(workloadCharacteristics);

        if (!suitableProfiles.isEmpty()) {
            return (CacheProfile<K, V>) suitableProfiles.get(0); // Highest priority
        }

        return (CacheProfile<K, V>) ProfileRegistry.getProfile(ProfileName.DEFAULT.getValue());
    }

    /**
     * Applies profile defaults only for values that haven't been explicitly set.
     */
    private void applyProfileDefaults(CacheConfig<K, V> preProfileConfig) {
        // Apply eviction strategy from profile
        configBuilder.evictionStrategy(profile.getEvictionStrategy());

        // For critical configurable values, don't override if they were set
        if (preProfileConfig.getMaximumSize() != null) {
            // User explicitly set maximumSize, preserve it
            configBuilder.maximumSize(preProfileConfig.getMaximumSize());
        }

        if (preProfileConfig.isRecordStats()) {
            // User explicitly set recordStats, preserve it
            configBuilder.recordStats(preProfileConfig.isRecordStats());
        }

        // Apply other profile defaults for values that are typically not set explicitly
        configBuilder.initialCapacity(profile.getRecommendedInitialCapacity(
                preProfileConfig.getMaximumSize() != null ? preProfileConfig.getMaximumSize() : 1000L));
        configBuilder.concurrencyLevel(profile.getRecommendedConcurrencyLevel());
    }

    /**
     * Applies smart defaults based on the selected profile and cache size.
     */
    private void applySmartDefaults() {
        CacheConfig<K, V> config = configBuilder.build();
        long expectedSize = config.getMaximumSize() != null ? config.getMaximumSize() : 1000L;

        // Apply initial capacity if not set
        if (config.getInitialCapacity() == 16) { // Default value
            configBuilder.initialCapacity(profile.getRecommendedInitialCapacity(expectedSize));
        }

        // Apply concurrency level if not set
        if (config.getConcurrencyLevel() == 16) { // Default value
            configBuilder.concurrencyLevel(profile.getRecommendedConcurrencyLevel());
        }
    }

    /**
     * Creates the appropriate cache instance based on the profile.
     */
    @SuppressWarnings("unchecked")
    private Cache<K, V> createCacheInstance(CacheConfig<K, V> config) {
        try {
            // Use ProfileRegistry to create the cache instance
            return ProfileRegistry.createCache(profile.getName(), config);
        } catch (Exception e) {
            // Fallback to default cache if instantiation fails
            return new DefaultCache<>(config);
        }
    }

    /**
     * Returns information about the builder's current configuration.
     *
     * @return configuration summary
     */
    public String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("JCacheXBuilder Configuration:\n");
        summary.append("  Profile: ").append(profile != null ? profile.getName() : "Not selected").append("\n");
        summary.append("  Smart Defaults: ").append(useSmartDefaults).append("\n");
        summary.append("  Cache Name: ").append(name != null ? name : "Not set").append("\n");

        if (profile != null) {
            summary.append("  Implementation: ").append(profile.getCacheImplementation().getSimpleName()).append("\n");
            summary.append("  Description: ").append(profile.getDescription()).append("\n");
        }

        return summary.toString();
    }
}
