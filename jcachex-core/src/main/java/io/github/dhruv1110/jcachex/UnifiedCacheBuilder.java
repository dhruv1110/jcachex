package io.github.dhruv1110.jcachex;

import io.github.dhruv1110.jcachex.impl.*;
import io.github.dhruv1110.jcachex.profiles.CacheProfile;
import io.github.dhruv1110.jcachex.profiles.ProfileRegistry;
import io.github.dhruv1110.jcachex.profiles.WorkloadCharacteristics;

import java.util.List;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Unified cache builder that simplifies cache creation using profiles and smart
 * defaults.
 *
 * This builder eliminates the complexity of choosing between multiple cache
 * implementations
 * and eviction strategies by providing a profile-based approach that
 * automatically selects
 * the optimal configuration for specific use cases.
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
 * // Read-heavy workload (e.g., user profiles)
 * Cache<String, User> userCache = UnifiedCacheBuilder.forProfile(ProfileRegistry.getProfile("READ_HEAVY"))
 *         .name("users")
 *         .maximumSize(1000L)
 *         .build();
 *
 * // API response caching
 * Cache<String, ApiResponse> apiCache = UnifiedCacheBuilder.forProfile(ProfileRegistry.getProfile("API_CACHE"))
 *         .name("api-responses")
 *         .maximumSize(500L)
 *         .expireAfterWrite(Duration.ofMinutes(15))
 *         .build();
 *
 * // Session storage
 * Cache<String, UserSession> sessionCache = UnifiedCacheBuilder.forProfile(ProfileRegistry.getProfile("SESSION_CACHE"))
 *         .name("sessions")
 *         .maximumSize(2000L)
 *         .build();
 * }</pre>
 *
 * <h3>Smart Defaults (When unsure):</h3>
 *
 * <pre>{@code
 * // Let the builder choose the best profile based on workload characteristics
 * Cache<String, Product> productCache = UnifiedCacheBuilder.withSmartDefaults()
 *         .name("products")
 *         .maximumSize(1000L)
 *         .workloadCharacteristics(WorkloadCharacteristics.builder()
 *                 .readToWriteRatio(8.0) // Read-heavy
 *                 .accessPattern(AccessPattern.TEMPORAL_LOCALITY)
 *                 .build())
 *         .build();
 *
 * // Minimal configuration - uses DEFAULT profile
 * Cache<String, String> simpleCache = UnifiedCacheBuilder.create()
 *         .name("simple")
 *         .maximumSize(100L)
 *         .build();
 * }</pre>
 *
 * <h3>Advanced Configuration:</h3>
 *
 * <pre>{@code
 * // Override profile settings when needed
 * Cache<String, LargeObject> customCache = UnifiedCacheBuilder
 *         .forProfile(ProfileRegistry.getProfile("MEMORY_EFFICIENT"))
 *         .name("large-objects")
 *         .maximumSize(50L)
 *         .expireAfterAccess(Duration.ofMinutes(10))
 *         .loader(key -> loadFromDatabase(key))
 *         .build();
 * }</pre>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 * @see ProfileRegistry
 * @see WorkloadCharacteristics
 * @since 1.0.0
 */
public final class UnifiedCacheBuilder<K, V> {

    private final CacheConfig.Builder<K, V> configBuilder;
    private CacheProfile<K, V> profile;
    private String name;
    private WorkloadCharacteristics workloadCharacteristics;
    private boolean useSmartDefaults;

    private UnifiedCacheBuilder(CacheProfile<K, V> profile) {
        this.configBuilder = CacheConfig.newBuilder();
        this.profile = profile;
        this.useSmartDefaults = false;
    }

    private UnifiedCacheBuilder(boolean useSmartDefaults) {
        this.configBuilder = CacheConfig.newBuilder();
        this.profile = null;
        this.useSmartDefaults = useSmartDefaults;
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
    public static <K, V> UnifiedCacheBuilder<K, V> forProfile(CacheProfile<?, ?> profile) {
        return new UnifiedCacheBuilder<>((CacheProfile<K, V>) profile);
    }

    /**
     * Creates a new builder that will automatically select the optimal profile
     * based on workload characteristics.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a new builder instance
     */
    public static <K, V> UnifiedCacheBuilder<K, V> withSmartDefaults() {
        return new UnifiedCacheBuilder<>(true);
    }

    /**
     * Creates a new builder with the DEFAULT profile.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a new builder instance
     */
    public static <K, V> UnifiedCacheBuilder<K, V> create() {
        return forProfile(ProfileRegistry.getDefaultProfile());
    }

    /**
     * Sets the cache name for identification and debugging.
     *
     * @param name the cache name
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> name(String name) {
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
    public UnifiedCacheBuilder<K, V> workloadCharacteristics(WorkloadCharacteristics workloadCharacteristics) {
        this.workloadCharacteristics = workloadCharacteristics;
        return this;
    }

    /**
     * Sets the maximum number of entries the cache may contain.
     *
     * @param maximumSize the maximum size
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> maximumSize(long maximumSize) {
        configBuilder.maximumSize(maximumSize);
        return this;
    }

    /**
     * Sets the maximum total weight of entries the cache may contain.
     *
     * @param maximumWeight the maximum weight
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> maximumWeight(long maximumWeight) {
        configBuilder.maximumWeight(maximumWeight);
        return this;
    }

    /**
     * Sets the time after which entries should expire after being written.
     *
     * @param duration the expiration duration
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> expireAfterWrite(Duration duration) {
        configBuilder.expireAfterWrite(duration);
        return this;
    }

    /**
     * Sets the time after which entries should expire after being accessed.
     *
     * @param duration the expiration duration
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> expireAfterAccess(Duration duration) {
        configBuilder.expireAfterAccess(duration);
        return this;
    }

    /**
     * Sets the time after which entries should be automatically refreshed.
     *
     * @param duration the refresh duration
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> refreshAfterWrite(Duration duration) {
        configBuilder.refreshAfterWrite(duration);
        return this;
    }

    /**
     * Sets the function to use for loading values when they are not present in the
     * cache.
     *
     * @param loader the value loader function
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> loader(Function<K, V> loader) {
        configBuilder.loader(loader);
        return this;
    }

    /**
     * Sets the function to use for asynchronously loading values.
     *
     * @param asyncLoader the async value loader function
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> asyncLoader(Function<K, CompletableFuture<V>> asyncLoader) {
        configBuilder.asyncLoader(asyncLoader);
        return this;
    }

    /**
     * Sets the weigher function for calculating entry weights.
     *
     * @param weigher the weigher function
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> weigher(java.util.function.BiFunction<K, V, Long> weigher) {
        configBuilder.weigher(weigher);
        return this;
    }

    /**
     * Enables or disables statistics recording.
     *
     * @param recordStats whether to record statistics
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> recordStats(boolean recordStats) {
        configBuilder.recordStats(recordStats);
        return this;
    }

    /**
     * Enables statistics recording.
     *
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> recordStats() {
        return recordStats(true);
    }

    /**
     * Adds an event listener to the cache.
     *
     * @param listener the event listener
     * @return this builder instance
     */
    public UnifiedCacheBuilder<K, V> listener(CacheEventListener<K, V> listener) {
        configBuilder.addListener(listener);
        return this;
    }

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
            profile = (CacheProfile<K, V>) ProfileRegistry.getDefaultProfile();
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

    /**
     * Selects the optimal profile based on workload characteristics.
     */
    @SuppressWarnings("unchecked")
    private CacheProfile<K, V> selectOptimalProfile() {
        if (workloadCharacteristics == null) {
            return (CacheProfile<K, V>) ProfileRegistry.getDefaultProfile();
        }

        // Use ProfileRegistry to find suitable profiles, ordered by priority
        List<CacheProfile<Object, Object>> suitableProfiles = ProfileRegistry
                .findSuitableProfiles(workloadCharacteristics);

        if (!suitableProfiles.isEmpty()) {
            return (CacheProfile<K, V>) suitableProfiles.get(0); // Highest priority
        }

        return (CacheProfile<K, V>) ProfileRegistry.getDefaultProfile();
    }

    /**
     * Applies profile defaults only for values that haven't been explicitly set.
     */
    private void applyProfileDefaults(CacheConfig<K, V> preProfileConfig) {
        // Apply eviction strategy from profile
        configBuilder.evictionStrategy(profile.getEvictionStrategy());

        // Only apply profile defaults for values that haven't been explicitly
        // configured
        // Note: Since CacheConfig.Builder doesn't track what was explicitly set,
        // we use the profile's applyConfiguration but selectively

        // For critical Spring-configurable values, don't override if they were set
        // This is a simple heuristic: if maximumSize was set to something other than
        // default,
        // preserve it
        if (preProfileConfig.getMaximumSize() != null) {
            // User explicitly set maximumSize, preserve it
            configBuilder.maximumSize(preProfileConfig.getMaximumSize());
        }

        if (preProfileConfig.isRecordStats() != false) {
            // User explicitly set recordStats, preserve it
            configBuilder.recordStats(preProfileConfig.isRecordStats());
        }

        // Apply other profile defaults for values that are typically not set explicitly
        // in Spring configuration
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

        // Apply statistics setting if not explicitly set
        // Note: We need to check if this was explicitly set by the user
        // For now, we'll let explicit configuration take precedence
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
        summary.append("UnifiedCacheBuilder Configuration:\n");
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
