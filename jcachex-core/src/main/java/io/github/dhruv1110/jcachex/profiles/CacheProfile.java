package io.github.dhruv1110.jcachex.profiles;

import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.eviction.EvictionStrategy;

/**
 * Strategy pattern interface for cache profiles.
 *
 * Cache profiles encapsulate the optimal configuration for specific use cases,
 * eliminating the need for users to understand the complex relationships
 * between
 * cache implementations, eviction strategies, and configuration parameters.
 *
 * <h2>Benefits:</h2>
 * <ul>
 * <li><strong>Simplicity</strong>: Users choose by use case, not implementation
 * details</li>
 * <li><strong>Optimal Performance</strong>: Each profile is tuned for specific
 * workloads</li>
 * <li><strong>Consistency</strong>: Same profile works across Java, Kotlin, and
 * Spring Boot</li>
 * <li><strong>Extensibility</strong>: Custom profiles can be created for
 * specific needs</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 *
 * <pre>{@code
 * // Java - Simple API
 * Cache<String, User> cache = CacheBuilder.forProfile(ProfileRegistry.getProfile("READ_HEAVY"))
 *     .name("users")
 *     .maximumSize(1000L)
 *     .build();
 *
 * // Kotlin - DSL Style
 * val cache = createCache<String, User> {
 *     profile(ProfileRegistry.getProfile("WRITE_HEAVY"))
 *     name("products")
 *     maximumSize(5000L)
 * }
 *
 * // Spring Boot - Annotation
 * &#64;JCacheXCacheable(value = "users", profile = "READ_HEAVY")
 * public User findUser(String id) { ... }
 * }</pre>
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of mapped values
 * @see ProfileRegistry
 * @since 1.0.0
 */
public interface CacheProfile<K, V> {

    /**
     * Returns the name of this cache profile.
     *
     * @return the profile name
     */
    String getName();

    /**
     * Returns a description of this cache profile and its intended use case.
     *
     * @return the profile description
     */
    String getDescription();

    /**
     * Returns the recommended cache implementation class for this profile.
     *
     * @return the cache implementation class
     */
    Class<?> getCacheImplementation();

    /**
     * Returns the optimal eviction strategy for this profile.
     *
     * @return the eviction strategy
     */
    EvictionStrategy<K, V> getEvictionStrategy();

    /**
     * Applies profile-specific configuration to the cache config builder.
     *
     * This method implements the Template Method pattern, allowing each profile
     * to customize the configuration while maintaining a consistent structure.
     *
     * @param builder the cache config builder to configure
     */
    void applyConfiguration(CacheConfig.Builder<K, V> builder);

    /**
     * Returns the recommended initial capacity for this profile.
     *
     * @param expectedSize the expected maximum size of the cache
     * @return the recommended initial capacity
     */
    default int getRecommendedInitialCapacity(long expectedSize) {
        return Math.max(16, (int) Math.min(expectedSize / 4, 1024));
    }

    /**
     * Returns the recommended concurrency level for this profile.
     *
     * @return the recommended concurrency level
     */
    default int getRecommendedConcurrencyLevel() {
        return Math.max(1, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Returns whether statistics should be enabled by default for this profile.
     *
     * @return true if statistics should be enabled
     */
    default boolean isStatisticsEnabledByDefault() {
        return true;
    }

    /**
     * Returns whether this profile is suitable for the given workload
     * characteristics.
     *
     * @param workload the workload characteristics
     * @return true if this profile is suitable
     */
    default boolean isSuitableFor(WorkloadCharacteristics workload) {
        return true;
    }

    /**
     * Validates that the provided configuration is compatible with this profile.
     *
     * @param config the cache configuration to validate
     * @throws IllegalArgumentException if the configuration is incompatible
     */
    default void validateConfiguration(CacheConfig<K, V> config) {
        // Default implementation - no validation
    }
}
