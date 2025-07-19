package io.github.dhruv1110.jcachex.spring.distributed;

import io.github.dhruv1110.jcachex.CacheFactory;
import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.distributed.DistributedCache.ConsistencyLevel;
import io.github.dhruv1110.jcachex.distributed.discovery.NodeDiscovery;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Factory for creating distributed caches with Spring configuration
 * integration.
 *
 * <p>
 * This factory allows programmatic creation of distributed caches while
 * respecting
 * the global configuration defaults defined in JCacheXProperties. It provides
 * seamless
 * scaling from local to distributed caching with support for auto-discovery
 * of nodes.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 * <li><strong>Configuration Integration:</strong> Respects global distributed
 * properties</li>
 * <li><strong>Auto-Discovery:</strong> Supports Kubernetes, Consul, and Gossip
 * node discovery</li>
 * <li><strong>Cluster Management:</strong> Automatic cluster formation and node
 * discovery</li>
 * <li><strong>Consistency Control:</strong> Configurable consistency levels per
 * cache</li>
 * <li><strong>Replication Management:</strong> Configurable replication
 * factors</li>
 * <li><strong>Spring Compatible:</strong> Works seamlessly with Spring's
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
 *         private JCacheXDistributedCacheFactory distributedCacheFactory;
 *
 *         public void setupDistributedCaches() {
 *             // Create distributed cache with default configuration
 *             DistributedCache<String, User> userCache = distributedCacheFactory
 *                     .createDistributedCache("users");
 *
 *             // Create cache with custom consistency level
 *             DistributedCache<String, Order> orderCache = distributedCacheFactory
 *                     .createDistributedCache("orders", config -> {
 *                         config.consistencyLevel(ConsistencyLevel.STRONG);
 *                         config.replicationFactor(3);
 *                         config.maximumSize(10000L);
 *                     });
 *
 *             // Create cache with specific types and custom nodes
 *             DistributedCache<Long, Product> productCache = distributedCacheFactory
 *                     .createDistributedCache("products", Long.class, Product.class, config -> {
 *                         config.nodes("prod-cache-1:8080", "prod-cache-2:8080", "prod-cache-3:8080");
 *                         config.replicationFactor(2);
 *                         config.consistencyLevel(ConsistencyLevel.EVENTUAL);
 *                     });
 *         }
 *     }
 * }
 * </pre>
 *
 * @see JCacheXProperties
 * @see JCacheXDistributedCacheManager
 * @see DistributedCache
 * @see NodeDiscoveryFactory
 * @since 1.0.0
 */
@Component
public class JCacheXDistributedCacheFactory {

    private static final Logger logger = Logger.getLogger(JCacheXDistributedCacheFactory.class.getName());

    private final JCacheXProperties properties;
    private final NodeDiscoveryFactory nodeDiscoveryFactory;
    private final ConcurrentMap<String, DistributedCache<Object, Object>> cacheRegistry;

    /**
     * Creates a new distributed cache factory with the specified properties.
     *
     * @param properties           the JCacheX configuration properties
     * @param nodeDiscoveryFactory the node discovery factory for auto-discovery
     */
    @Autowired
    public JCacheXDistributedCacheFactory(JCacheXProperties properties, NodeDiscoveryFactory nodeDiscoveryFactory) {
        this.properties = properties;
        this.nodeDiscoveryFactory = nodeDiscoveryFactory;
        this.cacheRegistry = new ConcurrentHashMap<>();
    }

    /**
     * Creates a distributed cache with the specified name using default
     * configuration.
     *
     * @param cacheName the name of the cache
     * @param <K>       the key type
     * @param <V>       the value type
     * @return the created distributed cache instance
     */
    @SuppressWarnings("unchecked")
    public <K, V> DistributedCache<K, V> createDistributedCache(String cacheName) {
        return (DistributedCache<K, V>) cacheRegistry.computeIfAbsent(cacheName, name -> {
            CacheFactory.DistributedCacheBuilder<Object, Object> builder = createBaseConfiguration(name);
            return builder.create();
        });
    }

    /**
     * Creates a distributed cache with the specified name and custom configuration.
     *
     * @param cacheName    the name of the cache
     * @param configurator function to customize the cache configuration
     * @param <K>          the key type
     * @param <V>          the value type
     * @return the created distributed cache instance
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <K, V> DistributedCache<K, V> createDistributedCache(String cacheName,
            DistributedCacheConfigurator<K, V> configurator) {
        return (DistributedCache<K, V>) cacheRegistry.computeIfAbsent(cacheName, name -> {
            CacheFactory.DistributedCacheBuilder<Object, Object> builder = createBaseConfiguration(name);
            // Use raw type to avoid generics mismatch
            ((DistributedCacheConfigurator) configurator).configure(builder);
            return builder.create();
        });
    }

    /**
     * Creates a distributed cache with the specified name, types, and custom
     * configuration.
     *
     * @param cacheName    the name of the cache
     * @param keyType      the key type class
     * @param valueType    the value type class
     * @param configurator function to customize the cache configuration
     * @param <K>          the key type
     * @param <V>          the value type
     * @return the created distributed cache instance
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <K, V> DistributedCache<K, V> createDistributedCache(String cacheName,
            Class<K> keyType, Class<V> valueType, DistributedCacheConfigurator<K, V> configurator) {
        return (DistributedCache<K, V>) cacheRegistry.computeIfAbsent(cacheName, name -> {
            CacheFactory.DistributedCacheBuilder<Object, Object> builder = createBaseConfiguration(name);
            // Use raw type to avoid generics mismatch
            ((DistributedCacheConfigurator) configurator).configure(builder);
            return builder.create();
        });
    }

    /**
     * Gets an existing distributed cache by name.
     *
     * @param cacheName the name of the cache
     * @param <K>       the key type
     * @param <V>       the value type
     * @return the cache instance, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <K, V> DistributedCache<K, V> getDistributedCache(String cacheName) {
        return (DistributedCache<K, V>) cacheRegistry.get(cacheName);
    }

    /**
     * Checks if a distributed cache with the specified name exists.
     *
     * @param cacheName the cache name
     * @return true if the cache exists
     */
    public boolean hasDistributedCache(String cacheName) {
        return cacheRegistry.containsKey(cacheName);
    }

    /**
     * Removes a distributed cache from the registry.
     *
     * @param cacheName the cache name
     * @return the removed cache, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <K, V> DistributedCache<K, V> removeDistributedCache(String cacheName) {
        return (DistributedCache<K, V>) cacheRegistry.remove(cacheName);
    }

    /**
     * Gets all registered distributed cache names.
     *
     * @return set of cache names
     */
    public java.util.Set<String> getCacheNames() {
        return java.util.Collections.unmodifiableSet(cacheRegistry.keySet());
    }

    /**
     * Creates base configuration for a distributed cache.
     *
     * @param cacheName the cache name
     * @return configured builder
     */
    private CacheFactory.DistributedCacheBuilder<Object, Object> createBaseConfiguration(String cacheName) {
        JCacheXProperties.CacheConfig cacheConfig = getCacheConfig(cacheName);
        JCacheXProperties.DistributedConfig distributedConfig = getDistributedConfig(cacheConfig);

        CacheFactory.DistributedCacheBuilder<Object, Object> builder = CacheFactory.distributed()
                .name(cacheName)
                .clusterName(distributedConfig.getClusterName())
                .replicationFactor(distributedConfig.getReplicationFactor())
                .consistencyLevel(ConsistencyLevel.valueOf(distributedConfig.getConsistencyLevel().toUpperCase()));

        // Configure auto-discovery if enabled
        configureNodeDiscovery(builder, distributedConfig);

        // Apply cache-specific configuration
        applyCacheConfig(builder, cacheConfig);

        return builder;
    }

    /**
     * Configures node discovery for the distributed cache.
     *
     * @param builder           the cache builder
     * @param distributedConfig the distributed configuration
     */
    private void configureNodeDiscovery(CacheFactory.DistributedCacheBuilder<Object, Object> builder,
            JCacheXProperties.DistributedConfig distributedConfig) {

        JCacheXProperties.NodeDiscoveryConfig nodeDiscoveryConfig = distributedConfig.getNodeDiscovery();

        if (nodeDiscoveryConfig != null && !"STATIC".equals(nodeDiscoveryConfig.getType())) {
            try {
                NodeDiscovery nodeDiscovery = nodeDiscoveryFactory.createNodeDiscovery(nodeDiscoveryConfig);
                if (nodeDiscovery != null) {
                    builder.nodeDiscovery(nodeDiscovery);
                    logger.info("Configured auto-discovery for cache: " + builder.getName() +
                            " using discovery type: " + nodeDiscoveryConfig.getType());
                }
            } catch (Exception e) {
                logger.severe("Failed to configure node discovery for cache: " + builder.getName() +
                        ". Error: " + e.getMessage());
                // Fall back to static node configuration
                configureStaticNodes(builder, distributedConfig);
            }
        } else {
            // Use static node configuration
            configureStaticNodes(builder, distributedConfig);
        }
    }

    /**
     * Configures static nodes for the distributed cache.
     *
     * @param builder           the cache builder
     * @param distributedConfig the distributed configuration
     */
    private void configureStaticNodes(CacheFactory.DistributedCacheBuilder<Object, Object> builder,
            JCacheXProperties.DistributedConfig distributedConfig) {

        // Add nodes
        if (distributedConfig.getNodes() != null && !distributedConfig.getNodes().isEmpty()) {
            builder.nodes(distributedConfig.getNodes());
            logger.info("Configured static nodes for cache: " + builder.getName() +
                    " with " + distributedConfig.getNodes().size() + " nodes");
        } else if (distributedConfig.getSeedNodes() != null && !distributedConfig.getSeedNodes().isEmpty()) {
            builder.nodes(distributedConfig.getSeedNodes());
            logger.info("Configured seed nodes for cache: " + builder.getName() +
                    " with " + distributedConfig.getSeedNodes().size() + " nodes");
        } else {
            logger.warning("No nodes configured for distributed cache: " + builder.getName() +
                    ". Cache will run in single-node mode.");
        }
    }

    /**
     * Gets the cache configuration for the specified cache name.
     *
     * @param cacheName the cache name
     * @return the cache configuration
     */
    private JCacheXProperties.CacheConfig getCacheConfig(String cacheName) {
        JCacheXProperties.CacheConfig namedConfig = properties.getCaches() != null
                ? properties.getCaches().get(cacheName)
                : null;

        if (namedConfig != null) {
            return namedConfig;
        }

        return properties.getDefaultConfig();
    }

    /**
     * Gets the distributed configuration for the specified cache config.
     *
     * @param cacheConfig the cache configuration
     * @return the distributed configuration
     */
    private JCacheXProperties.DistributedConfig getDistributedConfig(JCacheXProperties.CacheConfig cacheConfig) {
        if (cacheConfig.getDistributed() != null) {
            return cacheConfig.getDistributed();
        }
        return properties.getDefaultConfig().getDistributed();
    }

    /**
     * Applies cache-specific configuration to the builder.
     *
     * @param builder the cache builder
     * @param config  the cache configuration
     */
    private void applyCacheConfig(CacheFactory.DistributedCacheBuilder<Object, Object> builder,
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

        if (Boolean.TRUE.equals(config.getEnableStatistics())) {
            builder.enableStats(true);
        }

        if (Boolean.TRUE.equals(config.getEnableObservability())) {
            builder.enableObservability(true);
        }

        if (Boolean.TRUE.equals(config.getEnableResilience())) {
            builder.enableResilience(true);
        }

        if (Boolean.TRUE.equals(config.getEnableWarming())) {
            builder.enableWarming(true);
        }
    }

    /**
     * Functional interface for configuring distributed cache builders.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    @FunctionalInterface
    public interface DistributedCacheConfigurator<K, V> {
        /**
         * Configures the distributed cache builder.
         *
         * @param builder the cache configuration builder
         */
        void configure(CacheFactory.DistributedCacheBuilder<K, V> builder);
    }
}
