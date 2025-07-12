package io.github.dhruv1110.jcachex.profiles;

import io.github.dhruv1110.jcachex.eviction.EvictionStrategy;
import io.github.dhruv1110.jcachex.impl.*;

/**
 * Simplified and standardized cache profiles using the new ProfileRegistry
 * system.
 *
 * <p>
 * This class demonstrates the power of the new design:
 * - <strong>Type Safety</strong>: Uses enums instead of strings
 * - <strong>Reusability</strong>: Centralized constants and tags
 * - <strong>Readability</strong>: Clear, self-documenting code
 * - <strong>Testability</strong>: Easy to unit test individual profiles
 * - <strong>Maintainability</strong>: Adding new profiles is simple
 * </p>
 *
 * <h2>Before vs After:</h2>
 *
 * <h3>Before (Old CacheProfiles.java):</h3>
 *
 * <pre>
 * {
 *     &#64;code
 *     // 60+ lines of boilerplate per profile
 *     public static final CacheProfile<Object, Object> READ_HEAVY = new AbstractCacheProfile<Object, Object>() {
 *         &#64;Override
 *         public String getName() {
 *             return "READ_HEAVY";
 *         }
 *
 *         &#64;Override
 *         public String getDescription() {
 *             return "Optimized for read-intensive workloads";
 *         }
 *
 *         @Override
 *         public Class<?> getCacheImplementation() {
 *             return ReadOnlyOptimizedCache.class;
 *         }
 *         // ... 50+ more lines
 *     };
 * }
 * </pre>
 *
 * <h3>After (New System):</h3>
 *
 * <pre>{@code
 * // 10 lines, type-safe, reusable
 * CacheProfileBuilder.create(ProfileName.READ_HEAVY)
 *     .implementation(ReadOnlyOptimizedCache.class)
 *     .evictionStrategy(EvictionStrategy.ENHANCED_LFU())
 *     .defaultMaximumSize(ProfileConstants.SIZE_MEDIUM)
 *     .defaultInitialCapacity(ProfileConstants.CAPACITY_MEDIUM)
 *     .priority(ProfileConstants.PRIORITY_HIGH)
 *     .tags(ProfileTag.READ_HEAVY, ProfileTag.PERFORMANCE, ProfileTag.CACHE_FRIENDLY)
 *     .forReadHeavy()
 *     .register();
 * }</pre>
 *
 * @since 1.0.0
 */
public final class CacheProfilesV3 {

    private CacheProfilesV3() {
        // Utility class - no instances
    }

    /**
     * Initializes all default cache profiles using the new standardized approach.
     *
     * <p>
     * This method is called automatically when the ProfileRegistry is accessed.
     * It demonstrates how easy it is to create profiles with the new system.
     * </p>
     */
    static {
        System.out.println(ProfileConstants.LOG_PROFILE_INIT_START);

        // Core Profiles - covering 80% of use cases
        createCoreProfiles();

        // Specialized Profiles - for specific scenarios
        createSpecializedProfiles();

        // Advanced Profiles - for cutting-edge requirements
        createAdvancedProfiles();

        // Avoid circular dependency - don't call ProfileRegistry methods during
        // initialization
        System.out.println("Cache profile system initialized successfully");
    }

    /**
     * Creates core profiles that cover most common use cases.
     *
     * <p>
     * These profiles use constants and enums for type safety and reusability.
     * Notice how much cleaner and more maintainable this is compared to the old
     * system.
     * </p>
     */
    private static void createCoreProfiles() {

        // DEFAULT - General-purpose balanced cache
        CacheProfileBuilder.create(ProfileName.DEFAULT)
                .implementation(OptimizedCache.class)
                .evictionStrategy(EvictionStrategy.TINY_WINDOW_LFU())
                .defaultMaximumSize(ProfileConstants.SIZE_MEDIUM)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_SMALL)
                .priority(ProfileConstants.PRIORITY_DEFAULT)
                .tags(ProfileTag.GENERAL, ProfileTag.BALANCED, ProfileTag.DEFAULT, ProfileTag.PRODUCTION)
                .suitableFor(workload -> true) // Always suitable as fallback
                .register();

        // READ_HEAVY - Optimized for read-intensive workloads
        CacheProfileBuilder.create(ProfileName.READ_HEAVY)
                .implementation(ReadOnlyOptimizedCache.class)
                .evictionStrategy(EvictionStrategy.ENHANCED_LFU())
                .defaultMaximumSize(ProfileConstants.SIZE_MEDIUM)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_MEDIUM)
                .priority(ProfileConstants.PRIORITY_HIGH)
                .tags(ProfileTag.READ_HEAVY, ProfileTag.PERFORMANCE, ProfileTag.CACHE_FRIENDLY)
                .forReadHeavy()
                .register();

        // WRITE_HEAVY - Optimized for write-intensive workloads
        CacheProfileBuilder.create(ProfileName.WRITE_HEAVY)
                .implementation(WriteHeavyOptimizedCache.class)
                .evictionStrategy(EvictionStrategy.ENHANCED_LRU())
                .defaultMaximumSize(ProfileConstants.SIZE_MEDIUM)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_MEDIUM)
                .priority(ProfileConstants.PRIORITY_HIGH)
                .tags(ProfileTag.WRITE_HEAVY, ProfileTag.ASYNC, ProfileTag.THROUGHPUT)
                .forWriteHeavy()
                .register();

        // MEMORY_EFFICIENT - For constrained memory environments
        CacheProfileBuilder.create(ProfileName.MEMORY_EFFICIENT)
                .implementation(AllocationOptimizedCache.class)
                .evictionStrategy(EvictionStrategy.LRU())
                .defaultMaximumSize(ProfileConstants.SIZE_SMALL)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_SMALL)
                .priority(ProfileConstants.PRIORITY_NORMAL)
                .tags(ProfileTag.MEMORY_EFFICIENT, ProfileTag.MEMORY_CONSTRAINED)
                .forMemoryConstrained()
                .register();

        // HIGH_PERFORMANCE - Maximum throughput optimization
        CacheProfileBuilder.create(ProfileName.HIGH_PERFORMANCE)
                .implementation(JITOptimizedCache.class)
                .evictionStrategy(EvictionStrategy.ENHANCED_LFU())
                .defaultMaximumSize(ProfileConstants.SIZE_LARGE)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_LARGE)
                .priority(ProfileConstants.PRIORITY_HIGH)
                .tags(ProfileTag.PERFORMANCE, ProfileTag.THROUGHPUT, ProfileTag.HIGH_CONCURRENCY)
                .forHighConcurrency()
                .register();
    }

    /**
     * Creates specialized profiles for specific use cases.
     *
     * <p>
     * These profiles show how different configurations can be easily created
     * using the new constants and enum system.
     * </p>
     */
    private static void createSpecializedProfiles() {

        // SESSION_CACHE - User session storage
        CacheProfileBuilder.create(ProfileName.SESSION_CACHE)
                .implementation(DefaultCache.class)
                .evictionStrategy(EvictionStrategy.LRU())
                .defaultMaximumSize(ProfileConstants.SIZE_MEDIUM * 2) // 2000 entries
                .defaultExpireAfterAccess(ProfileConstants.EXPIRATION_LONG)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_SMALL)
                .priority(ProfileConstants.PRIORITY_NORMAL)
                .tags(ProfileTag.SESSION, ProfileTag.TEMPORAL, ProfileTag.EXPIRATION)
                .suitableFor(workload -> workload
                        .getAccessPattern() == WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
                .register();

        // API_CACHE - External API response caching
        CacheProfileBuilder.create(ProfileName.API_CACHE)
                .implementation(OptimizedCache.class)
                .evictionStrategy(EvictionStrategy.TINY_WINDOW_LFU())
                .defaultMaximumSize(ProfileConstants.SIZE_SMALL * 5) // 500 entries
                .defaultExpireAfterWrite(ProfileConstants.EXPIRATION_MEDIUM)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_MEDIUM)
                .priority(ProfileConstants.PRIORITY_NORMAL)
                .tags(ProfileTag.API, ProfileTag.EXTERNAL, ProfileTag.TTL, ProfileTag.NETWORK_AWARE)
                .suitableFor(workload -> workload.isReadHeavy() &&
                        workload.getAccessPattern() == WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
                .register();

        // COMPUTE_CACHE - Expensive computation results
        CacheProfileBuilder.create(ProfileName.COMPUTE_CACHE)
                .implementation(OptimizedCache.class)
                .evictionStrategy(EvictionStrategy.ENHANCED_LFU())
                .defaultMaximumSize(ProfileConstants.SIZE_MEDIUM)
                .defaultExpireAfterWrite(ProfileConstants.EXPIRATION_XLONG)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_MEDIUM)
                .priority(ProfileConstants.PRIORITY_NORMAL)
                .tags(ProfileTag.COMPUTATION, ProfileTag.EXPIRATION, ProfileTag.PERFORMANCE)
                .suitableFor(workload -> workload.isReadHeavy())
                .register();
    }

    /**
     * Creates advanced profiles for cutting-edge requirements.
     *
     * <p>
     * These profiles demonstrate advanced features and show how the new system
     * makes it easy to configure complex cache implementations.
     * </p>
     */
    private static void createAdvancedProfiles() {

        // ML_OPTIMIZED - Machine learning workloads with predictive capabilities
        CacheProfileBuilder.create(ProfileName.ML_OPTIMIZED)
                .implementation(MLOptimizedCache.class)
                .evictionStrategy(EvictionStrategy.ENHANCED_LRU())
                .defaultMaximumSize(ProfileConstants.SIZE_SMALL * 5) // 500 entries
                .defaultInitialCapacity(ProfileConstants.CAPACITY_LARGE)
                .priority(ProfileConstants.PRIORITY_HIGH)
                .tags(ProfileTag.MACHINE_LEARNING, ProfileTag.PREDICTIVE, ProfileTag.ADAPTIVE, ProfileTag.INTELLIGENT)
                .suitableFor(workload -> workload.getAccessPattern() == WorkloadCharacteristics.AccessPattern.MIXED &&
                        workload.getConcurrencyLevel() == WorkloadCharacteristics.ConcurrencyLevel.HIGH)
                .register();

        // ZERO_COPY - Ultra-low latency with direct memory buffers
        CacheProfileBuilder.create(ProfileName.ZERO_COPY)
                .implementation(ZeroCopyOptimizedCache.class)
                .evictionStrategy(EvictionStrategy.LRU())
                .defaultMaximumSize(ProfileConstants.SIZE_LARGE)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_MEDIUM)
                .defaultRecordStats(false) // Minimize overhead
                .priority(ProfileConstants.PRIORITY_HIGH)
                .tags(ProfileTag.ZERO_COPY, ProfileTag.ULTRA_LOW_LATENCY, ProfileTag.DIRECT_MEMORY, ProfileTag.HFT)
                .suitableFor(workload -> workload
                        .getMemoryConstraint() == WorkloadCharacteristics.MemoryConstraint.VERY_LIMITED &&
                        workload.requiresHighConcurrency())
                .register();

        // HARDWARE_OPTIMIZED - CPU-specific optimizations with SIMD
        CacheProfileBuilder.create(ProfileName.HARDWARE_OPTIMIZED)
                .implementation(HardwareOptimizedCache.class)
                .evictionStrategy(EvictionStrategy.ENHANCED_LRU())
                .defaultMaximumSize(ProfileConstants.SIZE_MEDIUM)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_XLARGE)
                .priority(ProfileConstants.PRIORITY_HIGH)
                .tags(ProfileTag.HARDWARE, ProfileTag.CPU_OPTIMIZED, ProfileTag.SIMD, ProfileTag.PARALLEL)
                .suitableFor(workload -> workload.requiresHighConcurrency() &&
                        workload.getConcurrencyLevel() == WorkloadCharacteristics.ConcurrencyLevel.VERY_HIGH)
                .register();

        // DISTRIBUTED - Cluster environments with network-aware caching
        CacheProfileBuilder.create(ProfileName.DISTRIBUTED)
                .implementation(DefaultDistributedCache.class)
                .evictionStrategy(EvictionStrategy.ENHANCED_LRU())
                .defaultMaximumSize(ProfileConstants.SIZE_XLARGE / 10) // 5000 entries
                .defaultExpireAfterWrite(ProfileConstants.EXPIRATION_LONG)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_LARGE)
                .priority(ProfileConstants.PRIORITY_HIGH)
                .tags(ProfileTag.DISTRIBUTED, ProfileTag.CLUSTER, ProfileTag.NETWORK_AWARE, ProfileTag.CONSISTENCY)
                .suitableFor(workload -> workload.isRequiresConsistency() &&
                        workload.getConcurrencyLevel() == WorkloadCharacteristics.ConcurrencyLevel.HIGH)
                .register();
    }

    /**
     * Example of how easy it is to create a custom profile.
     *
     * <p>
     * This demonstrates the power and simplicity of the new system:
     * </p>
     *
     * <ul>
     * <li><strong>Type Safe</strong>: Uses enums instead of strings</li>
     * <li><strong>Reusable</strong>: Uses constants for common values</li>
     * <li><strong>Readable</strong>: Self-documenting code</li>
     * <li><strong>Testable</strong>: Easy to test individual components</li>
     * </ul>
     *
     * @return example custom profile for Redis-like workloads
     */
    public static CacheProfile<Object, Object> createRedisLikeProfile() {
        return CacheProfileBuilder.create(ProfileName.DISTRIBUTED)
                .description("Redis-like distributed cache with persistence")
                .implementation(DefaultDistributedCache.class)
                .evictionStrategy(EvictionStrategy.ENHANCED_LRU())
                .defaultMaximumSize(ProfileConstants.SIZE_XLARGE)
                .defaultExpireAfterWrite(ProfileConstants.EXPIRATION_LONG)
                .defaultInitialCapacity(ProfileConstants.CAPACITY_LARGE)
                .category(ProfileCategory.ADVANCED)
                .priority(ProfileConstants.PRIORITY_HIGH)
                .tags(ProfileTag.DISTRIBUTED, ProfileTag.NETWORK_AWARE, ProfileTag.ENTERPRISE)
                .suitableFor(workload -> workload.isRequiresConsistency())
                .build(); // Note: build() instead of register() for custom profiles
    }
}
