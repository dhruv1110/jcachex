package io.github.dhruv1110.jcachex.spring;

import io.github.dhruv1110.jcachex.CacheFactory;
import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Distributed cache manager implementation for JCacheX integration with Spring.
 *
 * <p>
 * This cache manager extends the capabilities of the standard
 * JCacheXCacheManager
 * to provide distributed caching functionality with cluster management,
 * replication,
 * and consistency guarantees.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 * <li><strong>Cluster Management:</strong> Automatic node discovery and cluster
 * formation</li>
 * <li><strong>Replication Control:</strong> Configurable replication factors
 * per cache</li>
 * <li><strong>Consistency Models:</strong> Support for different consistency
 * levels</li>
 * <li><strong>Global Operations:</strong> Cluster-wide cache operations</li>
 * <li><strong>Monitoring:</strong> Distributed cache statistics and health
 * monitoring</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;Service
 *     public class UserService {
 *         @Autowired
 *         private JCacheXDistributedCacheManager distributedCacheManager;
 *
 *         public void cacheUser(User user) {
 *             DistributedCache<String, User> cache = distributedCacheManager.getDistributedCache("users");
 *             cache.putWithConsistency(user.getId(), user, ConsistencyLevel.STRONG);
 *         }
 *
 *         public void invalidateUserGlobally(String userId) {
 *             DistributedCache<String, User> cache = distributedCacheManager.getDistributedCache("users");
 *             cache.invalidateGlobally(userId);
 *         }
 *
 *         public ClusterTopology getClusterStatus() {
 *             return distributedCacheManager.getClusterTopology();
 *         }
 *     }
 * }
 * </pre>
 *
 * @see JCacheXCacheManager
 * @see DistributedCache
 * @see JCacheXProperties
 * @since 1.0.0
 */
public class JCacheXDistributedCacheManager implements CacheManager {

    private final JCacheXProperties properties;
    private final ConcurrentMap<String, DistributedCache<Object, Object>> distributedCaches;
    private final boolean allowNullValues;

    /**
     * Creates a new distributed cache manager with the specified properties.
     *
     * @param properties the JCacheX configuration properties
     */
    public JCacheXDistributedCacheManager(JCacheXProperties properties) {
        this(properties, true);
    }

    /**
     * Creates a new distributed cache manager with the specified properties and
     * null value handling.
     *
     * @param properties      the JCacheX configuration properties
     * @param allowNullValues whether to allow null values in caches
     */
    public JCacheXDistributedCacheManager(JCacheXProperties properties, boolean allowNullValues) {
        this.properties = properties;
        this.allowNullValues = allowNullValues;
        this.distributedCaches = new ConcurrentHashMap<>();
    }

    @Override
    @Nullable
    public org.springframework.cache.Cache getCache(String name) {
        DistributedCache<Object, Object> distributedCache = distributedCaches.computeIfAbsent(name,
                this::createDistributedCache);
        return new JCacheXSpringCache(name, distributedCache, allowNullValues);
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(distributedCaches.keySet());
    }

    /**
     * Gets the underlying distributed cache for advanced operations.
     *
     * @param name the cache name
     * @return the distributed cache, or null if not found
     */
    @Nullable
    public DistributedCache<Object, Object> getDistributedCache(String name) {
        return distributedCaches.get(name);
    }

    /**
     * Creates all distributed caches defined in configuration.
     */
    public void initializeDistributedCaches() {
        if (properties.getCaches() != null) {
            for (String cacheName : properties.getCaches().keySet()) {
                JCacheXProperties.CacheConfig cacheConfig = properties.getCaches().get(cacheName);
                if (cacheConfig.getDistributed() != null &&
                        Boolean.TRUE.equals(cacheConfig.getDistributed().getEnabled())) {
                    distributedCaches.computeIfAbsent(cacheName, this::createDistributedCache);
                }
            }
        }
    }

    /**
     * Gets cluster topology information.
     *
     * @return cluster topology
     */
    public DistributedCache.ClusterTopology getClusterTopology() {
        // Get topology from any distributed cache (they all share the same cluster)
        return distributedCaches.values().stream()
                .findFirst()
                .map(DistributedCache::getClusterTopology)
                .orElse(null);
    }

    /**
     * Gets distributed metrics for all caches.
     *
     * @return map of cache names to distributed metrics
     */
    public java.util.Map<String, DistributedCache.DistributedMetrics> getDistributedMetrics() {
        return distributedCaches.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        java.util.Map.Entry::getKey,
                        entry -> entry.getValue().getDistributedMetrics()));
    }

    /**
     * Performs global invalidation across all nodes for the specified cache and
     * key.
     *
     * @param cacheName the cache name
     * @param key       the key to invalidate
     * @return CompletableFuture that completes when invalidation is done
     */
    public java.util.concurrent.CompletableFuture<Void> invalidateGlobally(String cacheName, Object key) {
        DistributedCache<Object, Object> cache = distributedCaches.get(cacheName);
        if (cache != null) {
            return cache.invalidateGlobally(key);
        }
        return java.util.concurrent.CompletableFuture.completedFuture(null);
    }

    /**
     * Rebalances data across all nodes in the cluster.
     *
     * @return CompletableFuture that completes when rebalancing is done
     */
    public java.util.concurrent.CompletableFuture<Void> rebalanceCluster() {
        return distributedCaches.values().stream()
                .findFirst()
                .map(DistributedCache::rebalance)
                .orElse(java.util.concurrent.CompletableFuture.completedFuture(null));
    }

    /**
     * Adds a new node to the cluster.
     *
     * @param nodeAddress the address of the new node
     * @return CompletableFuture that completes when the node is added
     */
    public java.util.concurrent.CompletableFuture<Void> addNode(String nodeAddress) {
        return distributedCaches.values().stream()
                .findFirst()
                .map(cache -> cache.addNode(nodeAddress))
                .orElse(java.util.concurrent.CompletableFuture.completedFuture(null));
    }

    /**
     * Removes a node from the cluster.
     *
     * @param nodeId the ID of the node to remove
     * @return CompletableFuture that completes when the node is removed
     */
    public java.util.concurrent.CompletableFuture<Void> removeNode(String nodeId) {
        return distributedCaches.values().stream()
                .findFirst()
                .map(cache -> cache.removeNode(nodeId))
                .orElse(java.util.concurrent.CompletableFuture.completedFuture(null));
    }

    /**
     * Creates a distributed cache for the specified name.
     *
     * @param cacheName the cache name
     * @return the created distributed cache
     */
    private DistributedCache<Object, Object> createDistributedCache(String cacheName) {
        JCacheXProperties.CacheConfig cacheConfig = getCacheConfig(cacheName);
        JCacheXProperties.DistributedConfig distributedConfig = cacheConfig.getDistributed() != null
                ? cacheConfig.getDistributed()
                : properties.getDefaultConfig().getDistributed();

        // Build distributed cache using CacheFactory
        CacheFactory.DistributedCacheBuilder<Object, Object> builder = CacheFactory.distributed()
                .name(cacheName)
                .clusterName(distributedConfig.getClusterName())
                .replicationFactor(distributedConfig.getReplicationFactor())
                .consistencyLevel(DistributedCache.ConsistencyLevel.valueOf(
                        distributedConfig.getConsistencyLevel().toUpperCase()));

        // Add nodes
        if (distributedConfig.getNodes() != null) {
            builder.nodes(distributedConfig.getNodes());
        }

        // Configure cache settings
        if (cacheConfig.getMaximumSize() != null) {
            builder.maximumSize(cacheConfig.getMaximumSize());
        }
        if (cacheConfig.getExpireAfterSeconds() != null) {
            builder.expireAfterWrite(java.time.Duration.ofSeconds(cacheConfig.getExpireAfterSeconds()));
        }
        if (cacheConfig.getExpireAfterAccessSeconds() != null) {
            builder.expireAfterAccess(java.time.Duration.ofSeconds(cacheConfig.getExpireAfterAccessSeconds()));
        }

        // Enable features
        if (Boolean.TRUE.equals(cacheConfig.getEnableStatistics())) {
            builder.enableStats(true);
        }
        if (Boolean.TRUE.equals(cacheConfig.getEnableObservability())) {
            builder.enableObservability(true);
        }
        if (Boolean.TRUE.equals(cacheConfig.getEnableResilience())) {
            builder.enableResilience(true);
        }
        if (Boolean.TRUE.equals(cacheConfig.getEnableWarming())) {
            builder.enableWarming(true);
        }

        return builder.create();
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
}
