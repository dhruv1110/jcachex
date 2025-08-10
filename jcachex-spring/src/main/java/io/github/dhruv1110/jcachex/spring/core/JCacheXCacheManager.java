package io.github.dhruv1110.jcachex.spring.core;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.JCacheXBuilder;
import io.github.dhruv1110.jcachex.profiles.CacheProfile;
import io.github.dhruv1110.jcachex.profiles.ProfileRegistry;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;
import io.github.dhruv1110.jcachex.spring.utilities.EvictionStrategyFactory;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Spring CacheManager implementation that integrates JCacheX with Spring's
 * caching abstraction.
 *
 * This cache manager provides seamless integration between JCacheX
 * high-performance caching
 * and Spring's declarative caching support (@Cacheable, @CacheEvict, etc.). It
 * supports
 * all JCacheX features while maintaining compatibility with Spring's Cache
 * interface.
 *
 * <h2>Key Features:</h2>
 * <ul>
 * <li><strong>Full JCacheX Integration</strong>: Access to all eviction
 * strategies, async operations, and metrics</li>
 * <li><strong>Spring Compatibility</strong>: Works
 * with @Cacheable, @CacheEvict, @CachePut annotations</li>
 * <li><strong>Dynamic Cache Creation</strong>: Automatically creates caches on
 * demand with default configuration</li>
 * <li><strong>Configuration-Driven</strong>: Respects JCacheXProperties for
 * cache-specific settings</li>
 * <li><strong>Thread-Safe</strong>: Fully thread-safe for concurrent access in
 * multi-threaded environments</li>
 * <li><strong>Graceful Fallback</strong>: Handles missing caches and
 * configuration errors gracefully</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 *
 * <h3>Basic Configuration:</h3>
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;Configuration
 *     &#64;EnableCaching
 *     public class CacheConfig {
 *
 *         @Bean
 *         public CacheManager cacheManager() {
 *             JCacheXCacheManager manager = new JCacheXCacheManager();
 *             manager.setCacheNames(Arrays.asList("users", "products", "sessions"));
 *             return manager;
 *         }
 *     }
 * }
 * </pre>
 *
 * <h3>With Custom Properties:</h3>
 *
 * <pre>{@code @Bean
 * public CacheManager cacheManager(JCacheXProperties properties) {
 *     return new JCacheXCacheManager(properties);
 * }
 * }</pre>
 *
 * <h3>Programmatic Cache Access:</h3>
 *
 * <pre>{@code
 * &#64;Service
 * public class UserService { @Autowired
 *     private JCacheXCacheManager cacheManager;
 *
 *     public User getUser(String id) {
 *         org.springframework.cache.Cache cache = cacheManager.getCache("users");
 *         return cache.get(id, User.class);
 * }
 *
 * // Access underlying JCacheX cache for advanced features
 * public CompletableFuture<User> getUserAsync(String id) {
 * Cache<Object, Object> jcacheXCache = cacheManager.getNativeCache("users");
 * return jcacheXCache.getAsync(id).thenApply(obj -> (User) obj);
 * }
 * }
 * }
 * </pre>
 *
 * <h2>Configuration Integration:</h2>
 * <p>
 * The cache manager automatically applies configuration from JCacheXProperties:
 * </p>
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
 *       evictionStrategy: LRU
 *     sessions:
 *       maximumSize: 10000
 *       expireAfterSeconds: 1800
 *       evictionStrategy: LFU
 * }</pre>
 *
 * @see JCacheXProperties
 * @see JCacheXSpringCache
 * @see Cache
 * @since 1.0.0
 */
public class JCacheXCacheManager implements CacheManager {

    private final JCacheXProperties properties;
    private final ConcurrentMap<String, JCacheXSpringCache> caches = new ConcurrentHashMap<>();
    private final EvictionStrategyFactory evictionStrategyFactory = new EvictionStrategyFactory();
    private boolean allowNullValues = true;
    private boolean dynamic = true;

    /**
     * Creates a new cache manager with default properties.
     */
    public JCacheXCacheManager() {
        this(new JCacheXProperties());
    }

    /**
     * Creates a new cache manager with the specified properties.
     *
     * @param properties the JCacheX properties for configuration
     */
    public JCacheXCacheManager(JCacheXProperties properties) {
        this.properties = properties != null ? properties : new JCacheXProperties();
    }

    /**
     * Retrieves a cache by name, creating it if necessary and dynamic creation is
     * enabled.
     *
     * @param name the cache name
     * @return the Cache instance, or null if not found and dynamic creation is
     *         disabled
     */
    @Override
    @Nullable
    public org.springframework.cache.Cache getCache(String name) {
        return caches.computeIfAbsent(name, cacheName -> {
            if (dynamic) {
                return createCache(cacheName);
            } else {
                return null;
            }
        });
    }

    /**
     * Returns the names of all caches managed by this cache manager.
     *
     * @return collection of cache names
     */
    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(caches.keySet());
    }

    /**
     * Sets whether to allow null values in caches.
     *
     * When disabled, caching null values will result in exceptions.
     * When enabled (default), null values are cached and returned as null.
     *
     * @param allowNullValues whether to allow null values
     */
    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    /**
     * Gets whether null values are allowed in caches.
     *
     * @return true if null values are allowed
     */
    public boolean isAllowNullValues() {
        return allowNullValues;
    }

    /**
     * Sets whether to create caches dynamically when requested.
     *
     * When enabled (default), unknown caches are created automatically with default
     * configuration.
     * When disabled, only pre-configured caches are available.
     *
     * @param dynamic whether to create caches dynamically
     */
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    /**
     * Gets whether caches are created dynamically.
     *
     * @return true if dynamic cache creation is enabled
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * Pre-creates caches with the specified names using default configuration.
     *
     * This is useful when you want to ensure caches exist at startup or when
     * dynamic cache creation is disabled.
     *
     * @param cacheNames the names of caches to create
     */
    public void setCacheNames(Collection<String> cacheNames) {
        if (cacheNames != null) {
            for (String cacheName : cacheNames) {
                caches.computeIfAbsent(cacheName, this::createCache);
            }
        }
    }

    /**
     * Gets the underlying JCacheX cache instance for advanced operations.
     *
     * This provides direct access to JCacheX-specific features like async
     * operations,
     * detailed statistics, and advanced configuration options.
     *
     * @param name the cache name
     * @return the JCacheX cache instance, or null if not found
     */
    @Nullable
    public Cache<Object, Object> getNativeCache(String name) {
        JCacheXSpringCache springCache = caches.get(name);
        if (springCache != null) {
            return springCache.getJCacheXCache();
        }
        return null;
    }

    /**
     * Registers a pre-built JCacheX cache instance with this manager under the
     * given name.
     * If a cache with the same name exists it will be replaced.
     *
     * @param name  cache name
     * @param cache native JCacheX cache
     */
    public void registerCache(String name, Cache<Object, Object> cache) {
        if (name == null || cache == null) {
            return;
        }
        caches.put(name, new JCacheXSpringCache(name, cache, allowNullValues));
    }

    /**
     * Initializes caches based on configuration properties.
     *
     * This method is called automatically by the auto-configuration to create
     * all caches defined in the properties file.
     */
    public void initializeCaches() {
        // Create caches from properties
        if (properties.getCaches() != null) {
            for (String cacheName : properties.getCaches().keySet()) {
                caches.computeIfAbsent(cacheName, this::createCache);
            }
        }
    }

    /**
     * Creates a new cache with the specified name and configuration.
     *
     * This method applies configuration from properties if available, falling back
     * to default configuration for unknown caches. It now uses the
     * UnifiedCacheBuilder
     * with profile support for optimal cache selection.
     *
     * @param cacheName the name of the cache to create
     * @return the created Spring Cache wrapper
     */
    private JCacheXSpringCache createCache(String cacheName) {
        JCacheXProperties.CacheConfig cacheConfig = getCacheConfig(cacheName);

        // Use JCacheXBuilder with profile support
        JCacheXBuilder<Object, Object> builder;

        // Check if a profile is specified in configuration
        String profileName = cacheConfig.getProfile();
        if (profileName != null && !profileName.isEmpty()) {
            CacheProfile<?, ?> profile = getProfileByName(profileName);
            builder = JCacheXBuilder.forProfile(profile);
        } else {
            // Use smart defaults if no profile specified
            builder = JCacheXBuilder.withSmartDefaults();
        }

        // Apply configuration settings
        builder.name(cacheName);

        if (cacheConfig.getMaximumSize() != null) {
            builder.maximumSize(cacheConfig.getMaximumSize());
        }

        if (cacheConfig.getExpireAfterSeconds() != null) {
            builder.expireAfterWrite(Duration.ofSeconds(cacheConfig.getExpireAfterSeconds()));
        }

        if (cacheConfig.getExpireAfterAccessSeconds() != null) {
            builder.expireAfterAccess(Duration.ofSeconds(cacheConfig.getExpireAfterAccessSeconds()));
        }

        if (cacheConfig.getRefreshAfterWriteSeconds() != null) {
            builder.refreshAfterWrite(Duration.ofSeconds(cacheConfig.getRefreshAfterWriteSeconds()));
        }

        // Enable statistics based on configuration
        boolean enableStats = cacheConfig.getEnableStatistics() != null ? cacheConfig.getEnableStatistics() : true;
        builder.recordStats(enableStats);

        Cache<Object, Object> jcacheXCache = builder.build();
        return new JCacheXSpringCache(cacheName, jcacheXCache, allowNullValues);
    }

    /**
     * Gets the cache configuration for the specified cache name.
     *
     * @param cacheName the cache name
     * @return the cache configuration (named config merged with defaults)
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
     * Gets a cache profile by name.
     *
     * @param profileName the profile name
     * @return the cache profile
     */
    @SuppressWarnings("unchecked")
    private CacheProfile<Object, Object> getProfileByName(String profileName) {
        if (profileName == null || profileName.trim().isEmpty()) {
            return ProfileRegistry.getDefaultProfile();
        }

        CacheProfile<Object, Object> profile = ProfileRegistry.getProfile(profileName.toUpperCase());
        return profile != null ? profile : ProfileRegistry.getDefaultProfile();
    }

    /**
     * Builds a JCacheX configuration from Spring properties.
     *
     * This method converts the declarative configuration format used in Spring
     * properties into the programmatic configuration format used by JCacheX.
     *
     * @param cacheName the name of the cache (used for error messages)
     * @param config    the cache configuration from properties
     * @return the JCacheX configuration
     */
    private CacheConfig<Object, Object> buildJCacheXConfig(String cacheName,
            JCacheXProperties.CacheConfig config) {
        CacheConfig.Builder<Object, Object> builder = new CacheConfig.Builder<>();

        // Apply size limits
        if (config.getMaximumSize() != null) {
            builder.maximumSize(config.getMaximumSize());
        }

        // Apply expiration settings
        if (config.getExpireAfterSeconds() != null) {
            builder.expireAfterWrite(Duration.ofSeconds(config.getExpireAfterSeconds()));
        }
        if (config.getExpireAfterAccessSeconds() != null) {
            builder.expireAfterAccess(Duration.ofSeconds(config.getExpireAfterAccessSeconds()));
        }

        // Apply eviction strategy
        if (config.getEvictionStrategy() != null) {
            builder.evictionStrategy(evictionStrategyFactory.createStrategy(config.getEvictionStrategy(), config));
        }

        // Enable statistics
        builder.recordStats(config.getEnableStatistics() != null ? config.getEnableStatistics() : true);

        // Apply refresh settings
        if (config.getRefreshAfterWriteSeconds() != null) {
            builder.refreshAfterWrite(Duration.ofSeconds(config.getRefreshAfterWriteSeconds()));
        }

        // Apply weight settings
        if (config.getMaximumWeight() != null) {
            builder.maximumWeight(config.getMaximumWeight());
        }

        // Apply event listeners
        if (config.getEventListeners() != null && config.getEventListeners().getListeners() != null) {
            config.getEventListeners().getListeners().forEach(listenerClass -> {
                try {
                    Class<?> clazz = Class.forName(listenerClass);
                    Object listener = clazz.getDeclaredConstructor().newInstance();
                    if (listener instanceof io.github.dhruv1110.jcachex.CacheEventListener) {
                        builder.addListener((io.github.dhruv1110.jcachex.CacheEventListener<Object, Object>) listener);
                    }
                } catch (Exception e) {
                    // Log warning and continue
                    System.err.println("Warning: Failed to create listener " + listenerClass + ": " + e.getMessage());
                }
            });
        }

        return builder.build();
    }

    /**
     * Gets the properties used by this cache manager.
     *
     * @return the JCacheX properties
     */
    public JCacheXProperties getProperties() {
        return properties;
    }

    /**
     * Gets statistics for all managed caches.
     *
     * @return map of cache names to their statistics
     */
    public java.util.Map<String, io.github.dhruv1110.jcachex.CacheStats> getAllCacheStats() {
        java.util.Map<String, io.github.dhruv1110.jcachex.CacheStats> stats = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, JCacheXSpringCache> entry : caches.entrySet()) {
            io.github.dhruv1110.jcachex.CacheStats cacheStats = entry.getValue().getStats();
            if (cacheStats != null) {
                stats.put(entry.getKey(), cacheStats);
            }
        }
        return stats;
    }

    /**
     * Clears all caches managed by this cache manager.
     */
    public void clearAllCaches() {
        for (JCacheXSpringCache cache : caches.values()) {
            cache.clear();
        }
    }

    /**
     * Removes a cache from this cache manager.
     *
     * @param cacheName the name of the cache to remove
     * @return the removed cache, or null if not found
     */
    @Nullable
    public JCacheXSpringCache removeCache(String cacheName) {
        return caches.remove(cacheName);
    }

    /**
     * Gets the total size of all caches.
     *
     * @return the total number of entries across all caches
     */
    public long getTotalSize() {
        return caches.values().stream()
                .mapToLong(JCacheXSpringCache::size)
                .sum();
    }

    @Override
    public String toString() {
        return "JCacheXCacheManager{" +
                "cacheCount=" + caches.size() +
                ", allowNullValues=" + allowNullValues +
                ", dynamic=" + dynamic +
                '}';
    }
}
