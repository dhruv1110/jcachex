package io.github.dhruv1110.jcachex;

import io.github.dhruv1110.jcachex.impl.*;
import io.github.dhruv1110.jcachex.profiles.ProfileName;
import io.github.dhruv1110.jcachex.profiles.ProfileRegistry;
import io.github.dhruv1110.jcachex.profiles.CacheProfile;

import java.time.Duration;
import java.util.function.Function;

/**
 * Utility class that consolidates cache building patterns and provides a
 * simplified API.
 *
 * <p>
 * This class serves as a bridge between different builder approaches and
 * provides
 * common patterns in a clean, efficient way. It helps reduce code duplication
 * and
 * provides a consistent experience across the codebase.
 * </p>
 *
 * <h3>Common Usage Patterns:</h3>
 *
 * <pre>{@code
 * // Simple cache creation
 * Cache<String, User> cache = CacheBuilderUtils.simple()
 *         .maximumSize(1000L)
 *         .build();
 *
 * // Profile-based creation
 * Cache<String, Product> cache = CacheBuilderUtils.fromProfile(ProfileName.READ_HEAVY)
 *         .maximumSize(5000L)
 *         .build();
 *
 * // Workload-optimized creation
 * Cache<String, Data> cache = CacheBuilderUtils.forReadHeavyWorkload()
 *         .maximumSize(2000L)
 *         .expireAfterWrite(Duration.ofMinutes(30))
 *         .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public final class CacheBuilderUtils {

    private CacheBuilderUtils() {
        // Utility class - no instances
    }

    // ===== SIMPLE CREATION METHODS =====

    /**
     * Creates a simple cache builder with default settings.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a configured cache builder
     */
    public static <K, V> SimpleCacheBuilder<K, V> simple() {
        return new SimpleCacheBuilder<>();
    }

    /**
     * Creates a cache builder from a predefined profile.
     *
     * @param <K>         the key type
     * @param <V>         the value type
     * @param profileName the profile to use
     * @return a configured cache builder
     */
    public static <K, V> ProfileBasedCacheBuilder<K, V> fromProfile(ProfileName profileName) {
        CacheProfile<Object, Object> profile = ProfileRegistry.getProfile(profileName.toString());
        return new ProfileBasedCacheBuilder<>(profile);
    }

    // ===== WORKLOAD-OPTIMIZED CREATION METHODS =====

    /**
     * Creates a cache optimized for read-heavy workloads (80%+ reads).
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a configured cache builder
     */
    public static <K, V> WorkloadOptimizedCacheBuilder<K, V> forReadHeavyWorkload() {
        return new WorkloadOptimizedCacheBuilder<>(ReadOnlyOptimizedCache.class);
    }

    /**
     * Creates a cache optimized for write-heavy workloads (40%+ writes).
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a configured cache builder
     */
    public static <K, V> WorkloadOptimizedCacheBuilder<K, V> forWriteHeavyWorkload() {
        return new WorkloadOptimizedCacheBuilder<>(WriteHeavyOptimizedCache.class);
    }

    /**
     * Creates a cache optimized for memory-constrained environments.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a configured cache builder
     */
    public static <K, V> WorkloadOptimizedCacheBuilder<K, V> forMemoryConstrainedEnvironment() {
        return new WorkloadOptimizedCacheBuilder<>(AllocationOptimizedCache.class);
    }

    /**
     * Creates a cache optimized for high-performance scenarios.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a configured cache builder
     */
    public static <K, V> WorkloadOptimizedCacheBuilder<K, V> forHighPerformance() {
        return new WorkloadOptimizedCacheBuilder<>(OptimizedCache.class);
    }

    // ===== BUILDER IMPLEMENTATIONS =====

    /**
     * Simple cache builder for basic use cases.
     */
    public static class SimpleCacheBuilder<K, V> {
        private Long maximumSize = 1000L;
        private Duration expireAfterWrite;
        private Duration expireAfterAccess;
        private Function<K, V> loader;
        private boolean recordStats = true;

        public SimpleCacheBuilder<K, V> maximumSize(Long size) {
            this.maximumSize = size;
            return this;
        }

        public SimpleCacheBuilder<K, V> expireAfterWrite(Duration duration) {
            this.expireAfterWrite = duration;
            return this;
        }

        public SimpleCacheBuilder<K, V> expireAfterAccess(Duration duration) {
            this.expireAfterAccess = duration;
            return this;
        }

        public SimpleCacheBuilder<K, V> loader(Function<K, V> loader) {
            this.loader = loader;
            return this;
        }

        public SimpleCacheBuilder<K, V> recordStats(boolean enabled) {
            this.recordStats = enabled;
            return this;
        }

        public Cache<K, V> build() {
            CacheConfig.Builder<K, V> configBuilder = CacheConfig.<K, V>builder()
                    .maximumSize(maximumSize)
                    .recordStats(recordStats);

            if (expireAfterWrite != null) {
                configBuilder.expireAfterWrite(expireAfterWrite);
            }
            if (expireAfterAccess != null) {
                configBuilder.expireAfterAccess(expireAfterAccess);
            }
            if (loader != null) {
                configBuilder.loader(loader);
            }

            return new DefaultCache<>(configBuilder.build());
        }
    }

    /**
     * Profile-based cache builder that uses predefined profiles.
     */
    public static class ProfileBasedCacheBuilder<K, V> {
        private final CacheProfile<Object, Object> profile;
        private Long maximumSize;
        private Duration expireAfterWrite;
        private Duration expireAfterAccess;
        private Function<K, V> loader;
        private Boolean recordStats;

        private ProfileBasedCacheBuilder(CacheProfile<Object, Object> profile) {
            this.profile = profile;
        }

        public ProfileBasedCacheBuilder<K, V> maximumSize(Long size) {
            this.maximumSize = size;
            return this;
        }

        public ProfileBasedCacheBuilder<K, V> expireAfterWrite(Duration duration) {
            this.expireAfterWrite = duration;
            return this;
        }

        public ProfileBasedCacheBuilder<K, V> expireAfterAccess(Duration duration) {
            this.expireAfterAccess = duration;
            return this;
        }

        public ProfileBasedCacheBuilder<K, V> loader(Function<K, V> loader) {
            this.loader = loader;
            return this;
        }

        public ProfileBasedCacheBuilder<K, V> recordStats(Boolean enabled) {
            this.recordStats = enabled;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Cache<K, V> build() {
            CacheConfig.Builder<K, V> configBuilder = CacheConfig.<K, V>builder();

            // Apply profile defaults first
            if (profile != null) {
                profile.applyConfiguration((CacheConfig.Builder<Object, Object>) configBuilder);
            }

            // Override with explicit values
            if (maximumSize != null) {
                configBuilder.maximumSize(maximumSize);
            }
            if (expireAfterWrite != null) {
                configBuilder.expireAfterWrite(expireAfterWrite);
            }
            if (expireAfterAccess != null) {
                configBuilder.expireAfterAccess(expireAfterAccess);
            }
            if (loader != null) {
                configBuilder.loader(loader);
            }
            if (recordStats != null) {
                configBuilder.recordStats(recordStats);
            }

            CacheConfig<K, V> config = configBuilder.build();

            // Create cache using profile's implementation
            if (profile != null && profile.getCacheImplementation() != null) {
                return createCacheFromClass(profile.getCacheImplementation(), config);
            }

            return new DefaultCache<>(config);
        }
    }

    /**
     * Workload-optimized cache builder for specific use cases.
     */
    public static class WorkloadOptimizedCacheBuilder<K, V> {
        private final Class<? extends Cache> implementationClass;
        private Long maximumSize = 1000L;
        private Duration expireAfterWrite;
        private Duration expireAfterAccess;
        private Function<K, V> loader;
        private boolean recordStats = true;

        private WorkloadOptimizedCacheBuilder(Class<? extends Cache> implementationClass) {
            this.implementationClass = implementationClass;
        }

        public WorkloadOptimizedCacheBuilder<K, V> maximumSize(Long size) {
            this.maximumSize = size;
            return this;
        }

        public WorkloadOptimizedCacheBuilder<K, V> expireAfterWrite(Duration duration) {
            this.expireAfterWrite = duration;
            return this;
        }

        public WorkloadOptimizedCacheBuilder<K, V> expireAfterAccess(Duration duration) {
            this.expireAfterAccess = duration;
            return this;
        }

        public WorkloadOptimizedCacheBuilder<K, V> loader(Function<K, V> loader) {
            this.loader = loader;
            return this;
        }

        public WorkloadOptimizedCacheBuilder<K, V> recordStats(boolean enabled) {
            this.recordStats = enabled;
            return this;
        }

        public Cache<K, V> build() {
            CacheConfig.Builder<K, V> configBuilder = CacheConfig.<K, V>builder()
                    .maximumSize(maximumSize)
                    .recordStats(recordStats);

            if (expireAfterWrite != null) {
                configBuilder.expireAfterWrite(expireAfterWrite);
            }
            if (expireAfterAccess != null) {
                configBuilder.expireAfterAccess(expireAfterAccess);
            }
            if (loader != null) {
                configBuilder.loader(loader);
            }

            CacheConfig<K, V> config = configBuilder.build();
            return createCacheFromClass(implementationClass, config);
        }
    }

    // ===== UTILITY METHODS =====

    /**
     * Creates a cache instance from a class and configuration.
     */
    @SuppressWarnings("unchecked")
    private static <K, V> Cache<K, V> createCacheFromClass(Class<?> cacheClass,
            CacheConfig<K, V> config) {
        try {
            // Try to find constructor that takes CacheConfig
            java.lang.reflect.Constructor<?> constructor = cacheClass.getDeclaredConstructor(CacheConfig.class);
            return (Cache<K, V>) constructor.newInstance(config);
        } catch (Exception e) {
            // Fallback to DefaultCache if instantiation fails
            return new DefaultCache<>(config);
        }
    }

    /**
     * Validates that required parameters are provided.
     */
    private static void validateRequired(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }
}
