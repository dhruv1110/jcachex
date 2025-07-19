package io.github.dhruv1110.jcachex.profiles;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.eviction.EvictionStrategy;
import io.github.dhruv1110.jcachex.FrequencySketchType;
import io.github.dhruv1110.jcachex.impl.*;
import io.github.dhruv1110.jcachex.impl.UltraFastCache;
import io.github.dhruv1110.jcachex.distributed.KubernetesDistributedCache;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for creating cache profiles with minimal boilerplate.
 *
 * <p>
 * This builder eliminates the need for manual registration and
 * reduces profile creation to a few fluent method calls.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>{@code
 * CacheProfileBuilder.create("CUSTOM_PROFILE")
 *         .description("Cache optimized for custom workload")
 *         .implementation(OptimizedCache.class)
 *         .evictionStrategy(EvictionStrategy.ENHANCED_LRU())
 *         .defaultMaximumSize(1000L)
 *         .defaultExpiration(Duration.ofMinutes(30))
 *         .suitableFor(workload -> workload.isReadHeavy())
 *         .register();
 * }</pre>
 */
public final class CacheProfileBuilder {

    private final ProfileName name;
    private String description = "";
    private ProfileCategory category = ProfileCategory.CUSTOM;
    private int priority = ProfileConstants.PRIORITY_NORMAL;
    private ProfileTag[] tags = {};
    private Class<? extends Cache> implementationClass;
    private Function<CacheConfig<?, ?>, Cache<?, ?>> cacheFactory;
    private EvictionStrategy<Object, Object> evictionStrategy;
    private FrequencySketchType frequencySketchType = FrequencySketchType.OPTIMIZED;
    private Long defaultMaximumSize;
    private Duration defaultExpireAfterWrite;
    private Duration defaultExpireAfterAccess;
    private Duration defaultRefreshAfterWrite;
    private Integer defaultInitialCapacity;
    private Integer defaultConcurrencyLevel;
    private Boolean defaultRecordStats = true;
    private Predicate<WorkloadCharacteristics> suitabilityTest;

    private CacheProfileBuilder(ProfileName name) {
        this.name = name;
        this.description = name.getDescription();
        this.category = name.getCategory();
    }

    /**
     * Creates a new profile builder for a predefined profile.
     *
     * @param name the profile name enum
     * @return a new builder instance
     */
    public static CacheProfileBuilder create(ProfileName name) {
        if (name == null) {
            throw new IllegalArgumentException("Profile name cannot be null");
        }
        return new CacheProfileBuilder(name);
    }

    /**
     * Sets the profile description.
     *
     * @param description human-readable description
     * @return this builder
     */
    public CacheProfileBuilder description(String description) {
        this.description = description != null ? description : "";
        return this;
    }

    /**
     * Sets the profile category for grouping.
     *
     * @param category the category enum
     * @return this builder
     */
    public CacheProfileBuilder category(ProfileCategory category) {
        this.category = category != null ? category : ProfileCategory.CUSTOM;
        return this;
    }

    /**
     * Sets the profile priority for automatic selection.
     * Higher priority profiles are selected first.
     * Use ProfileConstants.PRIORITY_* constants for common values.
     *
     * @param priority the priority (default: PRIORITY_NORMAL)
     * @return this builder
     */
    public CacheProfileBuilder priority(int priority) {
        if (priority < ProfileConstants.MIN_PRIORITY || priority > ProfileConstants.MAX_PRIORITY) {
            throw new IllegalArgumentException("Priority must be between " +
                    ProfileConstants.MIN_PRIORITY + " and " + ProfileConstants.MAX_PRIORITY);
        }
        this.priority = priority;
        return this;
    }

    /**
     * Sets tags for the profile.
     *
     * @param tags descriptive ProfileTag enums
     * @return this builder
     */
    public CacheProfileBuilder tags(ProfileTag... tags) {
        this.tags = tags != null ? tags : new ProfileTag[0];
        return this;
    }

    /**
     * Sets the cache implementation class.
     *
     * @param implementationClass the cache implementation
     * @return this builder
     */
    public CacheProfileBuilder implementation(Class<? extends Cache> implementationClass) {
        this.implementationClass = implementationClass;
        return this;
    }

    /**
     * Sets a custom cache factory function.
     *
     * @param factory custom factory for creating cache instances
     * @return this builder
     */
    public CacheProfileBuilder factory(Function<CacheConfig<?, ?>, Cache<?, ?>> factory) {
        this.cacheFactory = factory;
        return this;
    }

    /**
     * Sets the eviction strategy.
     *
     * @param strategy the eviction strategy
     * @return this builder
     */
    public CacheProfileBuilder evictionStrategy(EvictionStrategy<Object, Object> strategy) {
        this.evictionStrategy = strategy;
        return this;
    }

    /**
     * Sets the frequency sketch type.
     *
     * @param type the frequency sketch type
     * @return this builder
     */
    public CacheProfileBuilder frequencySketch(FrequencySketchType type) {
        this.frequencySketchType = type;
        return this;
    }

    /**
     * Sets the default maximum size for caches using this profile.
     *
     * @param size the default maximum size
     * @return this builder
     */
    public CacheProfileBuilder defaultMaximumSize(long size) {
        this.defaultMaximumSize = size;
        return this;
    }

    /**
     * Sets the default expiration time for cache entries.
     *
     * @param duration the expiration duration
     * @return this builder
     */
    public CacheProfileBuilder defaultExpiration(Duration duration) {
        this.defaultExpireAfterWrite = duration;
        return this;
    }

    /**
     * Sets the default expire-after-write duration.
     *
     * @param duration the duration
     * @return this builder
     */
    public CacheProfileBuilder defaultExpireAfterWrite(Duration duration) {
        this.defaultExpireAfterWrite = duration;
        return this;
    }

    /**
     * Sets the default expire-after-access duration.
     *
     * @param duration the duration
     * @return this builder
     */
    public CacheProfileBuilder defaultExpireAfterAccess(Duration duration) {
        this.defaultExpireAfterAccess = duration;
        return this;
    }

    /**
     * Sets the default refresh-after-write duration.
     *
     * @param duration the duration
     * @return this builder
     */
    public CacheProfileBuilder defaultRefreshAfterWrite(Duration duration) {
        this.defaultRefreshAfterWrite = duration;
        return this;
    }

    /**
     * Sets the default initial capacity.
     *
     * @param capacity the initial capacity
     * @return this builder
     */
    public CacheProfileBuilder defaultInitialCapacity(int capacity) {
        this.defaultInitialCapacity = capacity;
        return this;
    }

    /**
     * Sets the default concurrency level.
     *
     * @param level the concurrency level
     * @return this builder
     */
    public CacheProfileBuilder defaultConcurrencyLevel(int level) {
        this.defaultConcurrencyLevel = level;
        return this;
    }

    /**
     * Sets whether statistics recording is enabled by default.
     *
     * @param enabled true to enable statistics
     * @return this builder
     */
    public CacheProfileBuilder defaultRecordStats(boolean enabled) {
        this.defaultRecordStats = enabled;
        return this;
    }

    /**
     * Sets the suitability test for automatic profile selection.
     *
     * @param test predicate that returns true if this profile is suitable for given
     *             workload
     * @return this builder
     */
    public CacheProfileBuilder suitableFor(Predicate<WorkloadCharacteristics> test) {
        this.suitabilityTest = test;
        return this;
    }

    /**
     * Convenience method for read-heavy workloads.
     *
     * @return this builder
     */
    public CacheProfileBuilder forReadHeavy() {
        return suitableFor(WorkloadCharacteristics::isReadHeavy);
    }

    /**
     * Convenience method for write-heavy workloads.
     *
     * @return this builder
     */
    public CacheProfileBuilder forWriteHeavy() {
        return suitableFor(WorkloadCharacteristics::isWriteHeavy);
    }

    /**
     * Convenience method for memory-constrained environments.
     *
     * @return this builder
     */
    public CacheProfileBuilder forMemoryConstrained() {
        return suitableFor(WorkloadCharacteristics::hasMemoryConstraints);
    }

    /**
     * Convenience method for high-concurrency workloads.
     *
     * @return this builder
     */
    public CacheProfileBuilder forHighConcurrency() {
        return suitableFor(WorkloadCharacteristics::requiresHighConcurrency);
    }

    /**
     * Builds and registers the profile with the ProfileRegistry.
     *
     * @return the created profile
     */
    public CacheProfile<Object, Object> register() {
        validate();

        CacheProfile<Object, Object> profile = new BuiltCacheProfile(this);
        ProfileRegistry.ProfileMetadata metadata = ProfileRegistry.ProfileMetadata.builder()
                .description(description)
                .category(category.getDisplayName())
                .priority(priority)
                .tags(ProfileTag.toStringArray(tags))
                .cacheFactory(resolveCacheFactory())
                .build();

        ProfileRegistry.register(profile, metadata);
        return profile;
    }

    /**
     * Builds the profile without registering it.
     * Useful for testing or custom registration logic.
     *
     * @return the created profile
     */
    public CacheProfile<Object, Object> build() {
        validate();
        return new BuiltCacheProfile(this);
    }

    private void validate() {
        if (implementationClass == null && cacheFactory == null) {
            throw new IllegalStateException("Must specify either implementation class or cache factory");
        }
        if (evictionStrategy == null) {
            throw new IllegalStateException("Must specify eviction strategy");
        }
        if (suitabilityTest == null) {
            // Default to always suitable
            suitabilityTest = workload -> true;
        }
    }

    private Function<CacheConfig<?, ?>, Cache<?, ?>> resolveCacheFactory() {
        if (cacheFactory != null) {
            return cacheFactory;
        }

        return config -> {
            try {
                // Use reflection to create the cache instance
                if (implementationClass == DefaultCache.class) {
                    return new DefaultCache<>(config);
                } else if (implementationClass == OptimizedCache.class) {
                    return new OptimizedCache<>(config);
                } else if (implementationClass == JITOptimizedCache.class) {
                    return new JITOptimizedCache<>(config);
                } else if (implementationClass == AllocationOptimizedCache.class) {
                    return new AllocationOptimizedCache<>(config);
                } else if (implementationClass == CacheLocalityOptimizedCache.class) {
                    return new CacheLocalityOptimizedCache<>(config);
                } else if (implementationClass == ZeroCopyOptimizedCache.class) {
                    return new ZeroCopyOptimizedCache<>(config);
                } else if (implementationClass == ReadOnlyOptimizedCache.class) {
                    return new ReadOnlyOptimizedCache<>(config);
                } else if (implementationClass == WriteHeavyOptimizedCache.class) {
                    return new WriteHeavyOptimizedCache<>(config);
                } else if (implementationClass == JVMOptimizedCache.class) {
                    return new JVMOptimizedCache<>(config);
                } else if (implementationClass == UltraFastCache.class) {
                    return new UltraFastCache<>(config);
                } else if (implementationClass == HardwareOptimizedCache.class) {
                    return new HardwareOptimizedCache<>(config);
                } else if (implementationClass == MLOptimizedCache.class) {
                    return new MLOptimizedCache<>(config);
                } else if (implementationClass == ProfiledOptimizedCache.class) {
                    return new ProfiledOptimizedCache<>(config);
                } else if (implementationClass == KubernetesDistributedCache.class) {
                    return new KubernetesDistributedCache.Builder<>().cacheConfig((CacheConfig) config).build();
                } else {
                    // Use constructor reflection as fallback
                    try {
                        return implementationClass.getConstructor(CacheConfig.class).newInstance(config);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to instantiate cache: " + implementationClass.getName(), e);
                    }
                }
            } catch (Exception e) {
                // Fallback to DefaultCache
                return new DefaultCache<>(config);
            }
        };
    }

    /**
     * Implementation of CacheProfile created by the builder.
     */
    private static final class BuiltCacheProfile implements CacheProfile<Object, Object> {
        private final String name;
        private final String description;
        private final Class<? extends Cache> implementationClass;
        private final EvictionStrategy<Object, Object> evictionStrategy;
        private final FrequencySketchType frequencySketchType;
        private final Long defaultMaximumSize;
        private final Duration defaultExpireAfterWrite;
        private final Duration defaultExpireAfterAccess;
        private final Duration defaultRefreshAfterWrite;
        private final Integer defaultInitialCapacity;
        private final Integer defaultConcurrencyLevel;
        private final Boolean defaultRecordStats;
        private final Predicate<WorkloadCharacteristics> suitabilityTest;

        private BuiltCacheProfile(CacheProfileBuilder builder) {
            this.name = builder.name.getValue();
            this.description = builder.description;
            this.implementationClass = builder.implementationClass;
            this.evictionStrategy = builder.evictionStrategy;
            this.frequencySketchType = builder.frequencySketchType;
            this.defaultMaximumSize = builder.defaultMaximumSize;
            this.defaultExpireAfterWrite = builder.defaultExpireAfterWrite;
            this.defaultExpireAfterAccess = builder.defaultExpireAfterAccess;
            this.defaultRefreshAfterWrite = builder.defaultRefreshAfterWrite;
            this.defaultInitialCapacity = builder.defaultInitialCapacity;
            this.defaultConcurrencyLevel = builder.defaultConcurrencyLevel;
            this.defaultRecordStats = builder.defaultRecordStats;
            this.suitabilityTest = builder.suitabilityTest;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Class<?> getCacheImplementation() {
            return implementationClass;
        }

        @Override
        public EvictionStrategy<Object, Object> getEvictionStrategy() {
            return evictionStrategy;
        }

        @Override
        public void applyConfiguration(CacheConfig.Builder<Object, Object> builder) {
            builder.frequencySketchType(frequencySketchType);

            if (defaultMaximumSize != null) {
                builder.maximumSize(defaultMaximumSize);
            }
            if (defaultExpireAfterWrite != null) {
                builder.expireAfterWrite(defaultExpireAfterWrite);
            }
            if (defaultExpireAfterAccess != null) {
                builder.expireAfterAccess(defaultExpireAfterAccess);
            }
            if (defaultRefreshAfterWrite != null) {
                builder.refreshAfterWrite(defaultRefreshAfterWrite);
            }
            if (defaultInitialCapacity != null) {
                builder.initialCapacity(defaultInitialCapacity);
            }
            if (defaultConcurrencyLevel != null) {
                builder.concurrencyLevel(defaultConcurrencyLevel);
            }
            if (defaultRecordStats != null) {
                builder.recordStats(defaultRecordStats);
            }
        }

        @Override
        public boolean isSuitableFor(WorkloadCharacteristics workload) {
            return suitabilityTest.test(workload);
        }

        @Override
        public int getRecommendedInitialCapacity(long expectedSize) {
            return defaultInitialCapacity != null ? defaultInitialCapacity
                    : Math.max(16, (int) Math.min(expectedSize / 4, 1024));
        }

        @Override
        public int getRecommendedConcurrencyLevel() {
            return defaultConcurrencyLevel != null ? defaultConcurrencyLevel
                    : Math.max(1, Runtime.getRuntime().availableProcessors());
        }

        @Override
        public boolean isStatisticsEnabledByDefault() {
            return defaultRecordStats != null ? defaultRecordStats : true;
        }

        @Override
        public void validateConfiguration(CacheConfig<Object, Object> config) {
            if (config.getMaximumSize() != null && config.getMaximumSize() < 1) {
                throw new IllegalArgumentException("Maximum size must be positive");
            }
        }
    }
}
