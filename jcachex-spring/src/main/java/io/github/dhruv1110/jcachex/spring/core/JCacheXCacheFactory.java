package io.github.dhruv1110.jcachex.spring.core;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.JCacheXBuilder;
// CacheBuilder removed - using JCacheXBuilder for all local caches
import io.github.dhruv1110.jcachex.impl.*;
import io.github.dhruv1110.jcachex.profiles.CacheProfile;
import io.github.dhruv1110.jcachex.profiles.ProfileRegistry;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;
import io.github.dhruv1110.jcachex.spring.utilities.EvictionStrategyFactory;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory for creating JCacheX cache instances with Spring configuration
 * integration.
 *
 * This factory allows programmatic cache creation while respecting the global
 * configuration defaults defined in JCacheXProperties. It provides a bridge
 * between
 * Spring's configuration-driven approach and JCacheX's programmatic API.
 *
 * <h2>Key Features:</h2>
 * <ul>
 * <li><strong>Configuration Integration</strong>: Respects global JCacheX
 * properties</li>
 * <li><strong>Programmatic Creation</strong>: Create caches at runtime with
 * custom settings</li>
 * <li><strong>Default Inheritance</strong>: Named caches inherit from default
 * configuration</li>
 * <li><strong>Type Safety</strong>: Generic type support for compile-time
 * safety</li>
 * <li><strong>Spring Compatible</strong>: Works seamlessly with Spring's
 * dependency injection</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;Service
 *     public class CacheService {
 *         @Autowired
 *         private JCacheXCacheFactory cacheFactory;
 *
 *         public void setupCaches() {
 *             // Create cache with default configuration
 *             Cache<String, User> userCache = cacheFactory.createCache("users");
 *
 *             // Create cache with custom configuration
 *             Cache<String, Product> productCache = cacheFactory.createCache("products", config -> {
 *                 config.maximumSize(5000L);
 *                 config.expireAfterWrite(Duration.ofMinutes(30));
 *             });
 *
 *             // Create cache with specific types
 *             Cache<Long, Order> orderCache = cacheFactory.createCache("orders", Long.class, Order.class);
 *         }
 *     }
 * }
 * </pre>
 *
 * <h2>Configuration Integration:</h2>
 *
 * <pre>{@code
 * jcachex:
 *   default:
 *     maximumSize: 1000
 *     expireAfterSeconds: 1800
 *     enableStatistics: true
 *   caches:
 *     users:
 *       maximumSize: 5000
 *       expireAfterSeconds: 3600
 * }</pre>
 *
 * @see JCacheXProperties
 * @see JCacheXCacheManager
 * @since 1.0.0
 */
public class JCacheXCacheFactory {

    private final JCacheXProperties properties;
    private final EvictionStrategyFactory evictionStrategyFactory;
    private final ConcurrentMap<String, Cache<?, ?>> cacheRegistry = new ConcurrentHashMap<>();

    /**
     * Creates a new cache factory with the specified properties.
     *
     * @param properties the JCacheX properties for configuration
     */
    public JCacheXCacheFactory(JCacheXProperties properties) {
        this.properties = properties != null ? properties : new JCacheXProperties();
        this.evictionStrategyFactory = new EvictionStrategyFactory();
    }

    /**
     * Creates a new cache factory with default properties.
     */
    public JCacheXCacheFactory() {
        this(new JCacheXProperties());
    }

    /**
     * Creates a cache with the specified name using default configuration.
     *
     * @param cacheName the name of the cache
     * @param <K>       the key type
     * @param <V>       the value type
     * @return the created cache instance
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> createCache(String cacheName) {
        String key = cacheName == null ? "<null>" : cacheName;
        return (Cache<K, V>) cacheRegistry.computeIfAbsent(key, this::createCacheInternal);
    }

    /**
     * Creates a cache with the specified name and types using default
     * configuration.
     *
     * @param cacheName the name of the cache
     * @param keyType   the key type class
     * @param valueType the value type class
     * @param <K>       the key type
     * @param <V>       the value type
     * @return the created cache instance
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> createCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        String key = cacheName == null ? "<null>" : cacheName;
        return (Cache<K, V>) cacheRegistry.computeIfAbsent(key, this::createCacheWithModernPatterns);
    }

    /**
     * Creates a cache with the specified name and custom configuration.
     *
     * @param cacheName    the name of the cache
     * @param configurator function to customize the cache configuration
     * @param <K>          the key type
     * @param <V>          the value type
     * @return the created cache instance
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> createCache(String cacheName, CacheConfigurator<K, V> configurator) {
        String key = cacheName == null ? "<null>" : cacheName;
        return (Cache<K, V>) cacheRegistry.computeIfAbsent(key, name -> {
            // For custom configurators, we need to create a base cache and apply custom
            // config
            Cache<K, V> baseCache = createCacheWithModernPatterns(name);

            // If we need to apply custom configuration, we'll create a new cache
            // with the configurator applied to the base configuration
            CacheConfig.Builder<K, V> builder = createBaseConfiguration(name);
            configurator.configure(builder);
            CacheConfig<K, V> config = builder.build();

            // Create cache using the DefaultCache with custom config
            return new DefaultCache<>(config);
        });
    }

    /**
     * Creates a cache with the specified name, types, and custom configuration.
     *
     * @param cacheName    the name of the cache
     * @param keyType      the key type class
     * @param valueType    the value type class
     * @param configurator function to customize the cache configuration
     * @param <K>          the key type
     * @param <V>          the value type
     * @return the created cache instance
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> createCache(String cacheName, Class<K> keyType, Class<V> valueType,
            CacheConfigurator<K, V> configurator) {
        String key = cacheName == null ? "<null>" : cacheName;
        return (Cache<K, V>) cacheRegistry.computeIfAbsent(key, name -> {
            // For custom configurators, apply custom config to base configuration
            CacheConfig.Builder<K, V> builder = createBaseConfiguration(name);
            configurator.configure(builder);
            CacheConfig<K, V> config = builder.build();

            // Create cache using the DefaultCache with custom config
            return new DefaultCache<>(config);
        });
    }

    /**
     * Gets an existing cache by name.
     *
     * @param cacheName the name of the cache
     * @param <K>       the key type
     * @param <V>       the value type
     * @return the cache instance, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String cacheName) {
        return (Cache<K, V>) cacheRegistry.get(cacheName);
    }

    /**
     * Checks if a cache with the specified name exists.
     *
     * @param cacheName the name of the cache
     * @return true if the cache exists, false otherwise
     */
    public boolean hasCache(String cacheName) {
        return cacheRegistry.containsKey(cacheName);
    }

    /**
     * Removes a cache from the factory registry.
     *
     * @param cacheName the name of the cache to remove
     * @return the removed cache, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> removeCache(String cacheName) {
        return (Cache<K, V>) cacheRegistry.remove(cacheName);
    }

    /**
     * Gets all registered cache names.
     *
     * @return set of cache names
     */
    public java.util.Set<String> getCacheNames() {
        return java.util.Collections.unmodifiableSet(cacheRegistry.keySet());
    }

    /**
     * Clears all caches from the factory registry.
     */
    public void clearAll() {
        // Clear individual cache contents
        for (Cache<?, ?> cache : cacheRegistry.values()) {
            cache.clear();
        }
        // Then clear registry
        cacheRegistry.clear();
    }

    /**
     * Creates a cache instance using the internal logic with new design patterns.
     */
    private Cache<?, ?> createCacheInternal(String cacheName) {
        return createCacheWithModernPatterns(cacheName);
    }

    /**
     * Creates a cache instance using modern design patterns with profile support.
     *
     * This method uses the new JCacheXBuilder and profile system to create
     * optimal cache instances based on configuration.
     */
    @SuppressWarnings("unchecked")
    private <K, V> Cache<K, V> createCacheWithModernPatterns(String cacheName) {
        JCacheXProperties.CacheConfig cacheConfig = getCacheConfig(cacheName);

        // Check if a profile is specified in configuration
        String profileName = cacheConfig.getProfile();
        if (profileName != null && !profileName.trim().isEmpty()) {
            return createCacheWithProfile(cacheName, profileName, cacheConfig);
        }

        // Legacy cache type configuration is no longer supported - using profile-based
        // approach instead

        // Use smart defaults when no profile or specific type is specified
        return createCacheWithSmartDefaults(cacheName, cacheConfig);
    }

    /**
     * Creates a cache using the JCacheXBuilder with the specified profile.
     */
    @SuppressWarnings("unchecked")
    private <K, V> Cache<K, V> createCacheWithProfile(String cacheName, String profileName,
            JCacheXProperties.CacheConfig cacheConfig) {

        CacheProfile<?, ?> profile = getProfileByName(profileName);
        JCacheXBuilder<Object, Object> builder = JCacheXBuilder.forProfile(profile);

        // Apply cache name
        builder.name(cacheName);

        // Apply configuration settings
        applyCacheConfiguration(builder, cacheConfig);

        return (Cache<K, V>) builder.build();
    }

    // Legacy CacheBuilder support removed - all caches now use JCacheXBuilder with
    // profiles

    /**
     * Creates a cache using smart defaults when no profile or type is specified.
     */
    @SuppressWarnings("unchecked")
    private <K, V> Cache<K, V> createCacheWithSmartDefaults(String cacheName,
            JCacheXProperties.CacheConfig cacheConfig) {

        JCacheXBuilder<Object, Object> builder = JCacheXBuilder.withSmartDefaults();

        // Apply cache name
        builder.name(cacheName);

        // Apply configuration settings
        applyCacheConfiguration(builder, cacheConfig);

        return (Cache<K, V>) builder.build();
    }

    /**
     * Gets the cache configuration for the specified cache name.
     */
    private JCacheXProperties.CacheConfig getCacheConfig(String cacheName) {
        JCacheXProperties.CacheConfig namedConfig = null;
        if (properties.getCaches() != null) {
            namedConfig = properties.getCaches().get(cacheName);
        }

        // Return named config if available, otherwise default config
        return namedConfig != null ? namedConfig : properties.getDefaultConfig();
    }

    /**
     * Gets a cache profile by name with proper fallback handling.
     */
    @SuppressWarnings("unchecked")
    private CacheProfile<Object, Object> getProfileByName(String profileName) {
        if (profileName == null || profileName.trim().isEmpty()) {
            return ProfileRegistry.getDefaultProfile();
        }

        CacheProfile<Object, Object> profile = ProfileRegistry.getProfile(profileName.toUpperCase());
        return profile != null ? profile : ProfileRegistry.getDefaultProfile();
    }

    // Legacy cache type mapping removed - using profile-based approach

    /**
     * Applies cache configuration to JCacheXBuilder.
     */
    private void applyCacheConfiguration(JCacheXBuilder<Object, Object> builder,
            JCacheXProperties.CacheConfig config) {

        if (config.getMaximumSize() != null) {
            builder.maximumSize(config.getMaximumSize());
        }

        if (config.getExpireAfterSeconds() != null) {
            builder.expireAfterWrite(Duration.ofSeconds(config.getExpireAfterSeconds()));
        }

        if (config.getExpireAfterAccessSeconds() != null) {
            builder.expireAfterAccess(Duration.ofSeconds(config.getExpireAfterAccessSeconds()));
        }

        if (config.getRefreshAfterWriteSeconds() != null) {
            builder.refreshAfterWrite(Duration.ofSeconds(config.getRefreshAfterWriteSeconds()));
        }

        // Enable statistics based on configuration
        boolean enableStats = config.getEnableStatistics() == null || config.getEnableStatistics();
        builder.recordStats(enableStats);
    }

    // Legacy CacheBuilder configuration support removed - using JCacheXBuilder with
    // profiles

    /**
     * Creates a base configuration builder for the specified cache name.
     */
    @SuppressWarnings("unchecked")
    private <K, V> CacheConfig.Builder<K, V> createBaseConfiguration(String cacheName) {
        CacheConfig.Builder<K, V> builder = (CacheConfig.Builder<K, V>) CacheConfig.newBuilder();

        // Apply default configuration
        JCacheXProperties.CacheConfig defaultConfig = properties.getDefaultConfig();
        if (defaultConfig != null) {
            applyCacheConfig(builder, defaultConfig);
        }

        // Apply named cache configuration if available
        if (properties.getCaches() != null && properties.getCaches().containsKey(cacheName)) {
            JCacheXProperties.CacheConfig namedConfig = properties.getCaches().get(cacheName);
            if (namedConfig != null) {
                applyCacheConfig(builder, namedConfig);
            }
        }

        return builder;
    }

    /**
     * Applies cache configuration from properties to the builder.
     */
    private <K, V> void applyCacheConfig(CacheConfig.Builder<K, V> builder,
            JCacheXProperties.CacheConfig config) {
        if (config.getMaximumSize() != null) {
            builder.maximumSize(config.getMaximumSize());
        }

        if (config.getMaximumWeight() != null) {
            builder.maximumWeight(config.getMaximumWeight());
        }

        if (config.getExpireAfterSeconds() != null) {
            builder.expireAfterWrite(Duration.ofSeconds(config.getExpireAfterSeconds()));
        }

        if (config.getExpireAfterAccessSeconds() != null) {
            builder.expireAfterAccess(Duration.ofSeconds(config.getExpireAfterAccessSeconds()));
        }

        if (config.getRefreshAfterWriteSeconds() != null) {
            builder.refreshAfterWrite(Duration.ofSeconds(config.getRefreshAfterWriteSeconds()));
        }

        if (config.getEvictionStrategy() != null) {
            try {
                builder.evictionStrategy(evictionStrategyFactory.createStrategy(config.getEvictionStrategy(), config));
            } catch (IllegalArgumentException e) {
                // Log warning and use default strategy
                System.err.println("Warning: Invalid eviction strategy '" + config.getEvictionStrategy() +
                        "', using default instead. " + e.getMessage());
            }
        }

        if (config.getFrequencySketchType() != null) {
            try {
                io.github.dhruv1110.jcachex.FrequencySketchType sketchType = io.github.dhruv1110.jcachex.FrequencySketchType
                        .valueOf(config.getFrequencySketchType().toUpperCase());
                builder.frequencySketchType(sketchType);
            } catch (IllegalArgumentException e) {
                // Log warning and use default
                System.err.println("Warning: Invalid frequency sketch type '" + config.getFrequencySketchType() +
                        "', using BASIC instead. " + e.getMessage());
            }
        }

        if (config.getEnableStatistics() == null || config.getEnableStatistics()) {
            builder.recordStats(true);
        }
    }

    /**
     * Functional interface for configuring cache builders.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    @FunctionalInterface
    public interface CacheConfigurator<K, V> {
        /**
         * Configures the cache builder.
         *
         * @param builder the cache configuration builder
         */
        void configure(CacheConfig.Builder<K, V> builder);
    }

    /**
     * Builder pattern for creating cache factories.
     */
    public static class Builder {
        private JCacheXProperties properties;

        /**
         * Sets the JCacheX properties.
         *
         * @param properties the properties to use
         * @return this builder
         */
        public Builder properties(JCacheXProperties properties) {
            this.properties = properties;
            return this;
        }

        /**
         * Builds the cache factory.
         *
         * @return the created cache factory
         */
        public JCacheXCacheFactory build() {
            return new JCacheXCacheFactory(properties);
        }
    }

    /**
     * Creates a new builder for cache factories.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
