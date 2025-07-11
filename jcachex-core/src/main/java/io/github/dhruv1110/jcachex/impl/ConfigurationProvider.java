package io.github.dhruv1110.jcachex.impl;

import io.github.dhruv1110.jcachex.CacheConfig;

/**
 * Utility class for common configuration handling patterns.
 *
 * This class provides static methods for creating common configuration
 * objects and handling default values, eliminating duplication across
 * cache implementations.
 */
public final class ConfigurationProvider {

    private ConfigurationProvider() {
        // Utility class - no instances
    }

    /**
     * Creates a basic configuration object with minimal settings.
     * Used by many cache implementations for their config() method.
     *
     * @param maximumSize the maximum size
     * @param recordStats whether to record statistics
     * @return a basic cache configuration
     */
    public static <K, V> CacheConfig<K, V> createBasicConfig(long maximumSize, boolean recordStats) {
        return CacheConfig.<K, V>builder()
                .maximumSize(maximumSize)
                .recordStats(recordStats)
                .build();
    }

    /**
     * Creates a configuration object with size and weight limits.
     *
     * @param maximumSize   the maximum size
     * @param maximumWeight the maximum weight
     * @param recordStats   whether to record statistics
     * @return a cache configuration with size and weight limits
     */
    public static <K, V> CacheConfig<K, V> createSizeWeightConfig(
            long maximumSize, long maximumWeight, boolean recordStats) {
        return CacheConfig.<K, V>builder()
                .maximumSize(maximumSize)
                .maximumWeight(maximumWeight)
                .recordStats(recordStats)
                .build();
    }

    /**
     * Calculates the optimal initial capacity for a ConcurrentHashMap
     * based on the maximum size and load factor.
     *
     * @param maximumSize the maximum expected size
     * @return the optimal initial capacity
     */
    public static int calculateOptimalCapacity(long maximumSize) {
        if (maximumSize <= 0) {
            return 16;
        }

        // Use next power of 2, accounting for load factor
        int capacity = (int) Math.min(maximumSize * 4 / 3, Integer.MAX_VALUE);
        return Math.max(16, Integer.highestOneBit(capacity) << 1);
    }

    /**
     * Calculates the optimal concurrency level for concurrent data structures.
     *
     * @return the optimal concurrency level
     */
    public static int calculateOptimalConcurrency() {
        int processors = Runtime.getRuntime().availableProcessors();
        return Math.max(1, Math.min(32, Integer.highestOneBit(processors) << 1));
    }

    /**
     * Validates cache configuration parameters and throws appropriate exceptions.
     *
     * @param config the configuration to validate
     * @throws IllegalArgumentException if configuration is invalid
     */
    public static void validateConfiguration(CacheConfig<?, ?> config) {
        if (config == null) {
            throw new IllegalArgumentException("Cache configuration cannot be null");
        }

        if (config.getMaximumSize() != null && config.getMaximumSize() <= 0) {
            throw new IllegalArgumentException("Maximum size must be positive");
        }

        if (config.getMaximumWeight() != null && config.getMaximumWeight() <= 0) {
            throw new IllegalArgumentException("Maximum weight must be positive");
        }

        Integer initialCapacity = config.getInitialCapacity();
        if (initialCapacity != null && initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be positive");
        }
    }

    /**
     * Gets a safe maximum size value, handling null cases.
     *
     * @param config the cache configuration
     * @return the maximum size or Long.MAX_VALUE if not specified
     */
    public static long getSafeMaximumSize(CacheConfig<?, ?> config) {
        return config.getMaximumSize() != null ? config.getMaximumSize() : Long.MAX_VALUE;
    }

    /**
     * Gets a safe initial capacity value, handling null cases.
     *
     * @param config the cache configuration
     * @return the initial capacity or a calculated default
     */
    public static int getSafeInitialCapacity(CacheConfig<?, ?> config) {
        Integer initialCapacity = config.getInitialCapacity();
        if (initialCapacity != null) {
            return initialCapacity;
        }

        // Calculate based on maximum size if available
        if (config.getMaximumSize() != null) {
            return Math.min(config.getMaximumSize().intValue() / 4, 1024);
        }

        return 16; // Default fallback
    }

    /**
     * Checks if the configuration has expiration settings.
     *
     * @param config the cache configuration
     * @return true if any expiration is configured
     */
    public static boolean hasExpiration(CacheConfig<?, ?> config) {
        return config.getExpireAfterWrite() != null || config.getExpireAfterAccess() != null;
    }

    /**
     * Checks if the configuration has refresh settings.
     *
     * @param config the cache configuration
     * @return true if refresh is configured
     */
    public static boolean hasRefresh(CacheConfig<?, ?> config) {
        return config.getRefreshAfterWrite() != null;
    }

    /**
     * Checks if the configuration has loading capability.
     *
     * @param config the cache configuration
     * @return true if loader or async loader is configured
     */
    public static boolean hasLoader(CacheConfig<?, ?> config) {
        return config.getLoader() != null || config.getAsyncLoader() != null;
    }
}
