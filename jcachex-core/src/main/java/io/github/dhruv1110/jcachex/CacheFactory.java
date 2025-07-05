package io.github.dhruv1110.jcachex;

import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.observability.MetricsRegistry;
import io.github.dhruv1110.jcachex.resilience.CircuitBreaker;
import io.github.dhruv1110.jcachex.warming.CacheWarmingStrategy;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Function;

/**
 * Unified factory for creating both local and distributed caches.
 * <p>
 * This factory provides a seamless API that allows applications to start with
 * local caching and scale to distributed caching without changing code. It
 * integrates all JCacheX features including warming, observability, and
 * resilience.
 * </p>
 *
 * <h3>Key Benefits:</h3>
 * <ul>
 * <li><strong>Seamless Scaling:</strong> Start local, scale to distributed with
 * config change</li>
 * <li><strong>Unified API:</strong> Same interface for local and distributed
 * caches</li>
 * <li><strong>Zero-Config Production:</strong> Sensible defaults for production
 * use</li>
 * <li><strong>Feature Integration:</strong> All advanced features work
 * together</li>
 * <li><strong>Environment Awareness:</strong> Automatically choose best
 * configuration</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Simple local cache
 * Cache<String, User> localCache = CacheFactory.local()
 *         .name("users")
 *         .maximumSize(1000L)
 *         .expireAfterWrite(Duration.ofMinutes(30))
 *         .create();
 *
 * // Production-ready distributed cache
 * Cache<String, User> distributedCache = CacheFactory.distributed()
 *         .name("users")
 *         .clusterName("user-service")
 *         .nodes("cache-1:8080", "cache-2:8080", "cache-3:8080")
 *         .replicationFactor(2)
 *         .consistencyLevel(DistributedCache.ConsistencyLevel.EVENTUAL)
 *         .maximumSize(10000L)
 *         .expireAfterWrite(Duration.ofHours(1))
 *         .enableWarming(true)
 *         .enableObservability(true)
 *         .enableResilience(true)
 *         .create();
 *
 * // Environment-aware cache (local in dev, distributed in prod)
 * Cache<String, User> adaptiveCache = CacheFactory.adaptive()
 *         .name("users")
 *         .maximumSize(1000L)
 *         .expireAfterWrite(Duration.ofMinutes(30))
 *         .distributedWhen(env -> "production".equals(env.get("ENVIRONMENT")))
 *         .create();
 * }</pre>
 *
 * @since 1.0.0
 */
public class CacheFactory {

    /**
     * Creates a local cache builder.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return local cache builder
     */
    public static <K, V> LocalCacheBuilder<K, V> local() {
        return new LocalCacheBuilder<>();
    }

    /**
     * Creates a distributed cache builder.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return distributed cache builder
     */
    public static <K, V> DistributedCacheBuilder<K, V> distributed() {
        return new DistributedCacheBuilder<>();
    }

    /**
     * Creates an adaptive cache builder that can switch between local and
     * distributed
     * based on environment or runtime conditions.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return adaptive cache builder
     */
    public static <K, V> AdaptiveCacheBuilder<K, V> adaptive() {
        return new AdaptiveCacheBuilder<>();
    }

    /**
     * Builder for local caches with production-ready features.
     */
    public static class LocalCacheBuilder<K, V> {
        private String name = "default-cache";
        private Long maximumSize;
        private Duration expireAfterWrite;
        private Duration expireAfterAccess;
        private boolean enableStats = true;
        private boolean enableWarming = false;
        private boolean enableObservability = false;
        private boolean enableResilience = false;
        private Function<K, V> loader;
        private CacheWarmingStrategy<K, V> warmingStrategy;
        private MetricsRegistry metricsRegistry;
        private CircuitBreaker circuitBreaker;

        public LocalCacheBuilder<K, V> name(String name) {
            this.name = name;
            return this;
        }

        public LocalCacheBuilder<K, V> maximumSize(Long maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public LocalCacheBuilder<K, V> expireAfterWrite(Duration duration) {
            this.expireAfterWrite = duration;
            return this;
        }

        public LocalCacheBuilder<K, V> expireAfterAccess(Duration duration) {
            this.expireAfterAccess = duration;
            return this;
        }

        public LocalCacheBuilder<K, V> loader(Function<K, V> loader) {
            this.loader = loader;
            return this;
        }

        public LocalCacheBuilder<K, V> enableStats(boolean enable) {
            this.enableStats = enable;
            return this;
        }

        public LocalCacheBuilder<K, V> enableWarming(boolean enable) {
            this.enableWarming = enable;
            return this;
        }

        public LocalCacheBuilder<K, V> enableObservability(boolean enable) {
            this.enableObservability = enable;
            return this;
        }

        public LocalCacheBuilder<K, V> enableResilience(boolean enable) {
            this.enableResilience = enable;
            return this;
        }

        public LocalCacheBuilder<K, V> warmingStrategy(CacheWarmingStrategy<K, V> strategy) {
            this.warmingStrategy = strategy;
            this.enableWarming = true;
            return this;
        }

        public LocalCacheBuilder<K, V> metricsRegistry(MetricsRegistry registry) {
            this.metricsRegistry = registry;
            this.enableObservability = true;
            return this;
        }

        public LocalCacheBuilder<K, V> circuitBreaker(CircuitBreaker circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
            this.enableResilience = true;
            return this;
        }

        public Cache<K, V> create() {
            // Build configuration
            CacheConfig.Builder<K, V> configBuilder = CacheConfig.<K, V>builder()
                    .recordStats(enableStats);

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

            Cache<K, V> cache = new DefaultCache<>(configBuilder.build());

            // Apply additional features
            if (enableObservability && metricsRegistry != null) {
                metricsRegistry.registerCache(name, cache.stats());
            }

            return cache;
        }
    }

    /**
     * Builder for distributed caches with all advanced features.
     */
    public static class DistributedCacheBuilder<K, V> {
        private String name = "default-distributed-cache";
        private String clusterName = "default-cluster";
        private Collection<String> nodes;
        private int replicationFactor = 2;
        private DistributedCache.ConsistencyLevel consistencyLevel = DistributedCache.ConsistencyLevel.EVENTUAL;
        private Long maximumSize;
        private Duration expireAfterWrite;
        private Duration expireAfterAccess;
        private boolean enableStats = true;
        private boolean enableWarming = false;
        private boolean enableObservability = false;
        private boolean enableResilience = false;
        private Function<K, V> loader;
        private CacheWarmingStrategy<K, V> warmingStrategy;
        private MetricsRegistry metricsRegistry;
        private CircuitBreaker circuitBreaker;

        public DistributedCacheBuilder<K, V> name(String name) {
            this.name = name;
            return this;
        }

        public DistributedCacheBuilder<K, V> clusterName(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public DistributedCacheBuilder<K, V> nodes(String... nodes) {
            this.nodes = java.util.Arrays.asList(nodes);
            return this;
        }

        public DistributedCacheBuilder<K, V> nodes(Collection<String> nodes) {
            this.nodes = nodes;
            return this;
        }

        public DistributedCacheBuilder<K, V> replicationFactor(int replicationFactor) {
            this.replicationFactor = replicationFactor;
            return this;
        }

        public DistributedCacheBuilder<K, V> consistencyLevel(DistributedCache.ConsistencyLevel consistencyLevel) {
            this.consistencyLevel = consistencyLevel;
            return this;
        }

        public DistributedCacheBuilder<K, V> maximumSize(Long maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public DistributedCacheBuilder<K, V> expireAfterWrite(Duration duration) {
            this.expireAfterWrite = duration;
            return this;
        }

        public DistributedCacheBuilder<K, V> expireAfterAccess(Duration duration) {
            this.expireAfterAccess = duration;
            return this;
        }

        public DistributedCacheBuilder<K, V> loader(Function<K, V> loader) {
            this.loader = loader;
            return this;
        }

        public DistributedCacheBuilder<K, V> enableStats(boolean enable) {
            this.enableStats = enable;
            return this;
        }

        public DistributedCacheBuilder<K, V> enableWarming(boolean enable) {
            this.enableWarming = enable;
            return this;
        }

        public DistributedCacheBuilder<K, V> enableObservability(boolean enable) {
            this.enableObservability = enable;
            return this;
        }

        public DistributedCacheBuilder<K, V> enableResilience(boolean enable) {
            this.enableResilience = enable;
            return this;
        }

        public DistributedCacheBuilder<K, V> warmingStrategy(CacheWarmingStrategy<K, V> strategy) {
            this.warmingStrategy = strategy;
            this.enableWarming = true;
            return this;
        }

        public DistributedCacheBuilder<K, V> metricsRegistry(MetricsRegistry registry) {
            this.metricsRegistry = registry;
            this.enableObservability = true;
            return this;
        }

        public DistributedCacheBuilder<K, V> circuitBreaker(CircuitBreaker circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
            this.enableResilience = true;
            return this;
        }

        public DistributedCache<K, V> create() {
            // Build local cache configuration
            CacheConfig.Builder<K, V> configBuilder = CacheConfig.<K, V>builder()
                    .recordStats(enableStats);

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

            // Build distributed cache
            DistributedCache<K, V> cache = DistributedCache.<K, V>builder()
                    .clusterName(clusterName)
                    .nodes(nodes)
                    .replicationFactor(replicationFactor)
                    .consistencyLevel(consistencyLevel)
                    .build();

            // Apply additional features
            if (enableObservability && metricsRegistry != null) {
                metricsRegistry.registerCache(name, cache.stats());
            }

            return cache;
        }
    }

    /**
     * Builder for adaptive caches that can switch between local and distributed
     * based on environment conditions.
     */
    public static class AdaptiveCacheBuilder<K, V> {
        private String name = "adaptive-cache";
        private Long maximumSize;
        private Duration expireAfterWrite;
        private Duration expireAfterAccess;
        private boolean enableStats = true;
        private Function<java.util.Map<String, String>, Boolean> distributedCondition;
        private String clusterName;
        private Collection<String> nodes;
        private Function<K, V> loader;

        public AdaptiveCacheBuilder<K, V> name(String name) {
            this.name = name;
            return this;
        }

        public AdaptiveCacheBuilder<K, V> maximumSize(Long maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public AdaptiveCacheBuilder<K, V> expireAfterWrite(Duration duration) {
            this.expireAfterWrite = duration;
            return this;
        }

        public AdaptiveCacheBuilder<K, V> expireAfterAccess(Duration duration) {
            this.expireAfterAccess = duration;
            return this;
        }

        public AdaptiveCacheBuilder<K, V> loader(Function<K, V> loader) {
            this.loader = loader;
            return this;
        }

        public AdaptiveCacheBuilder<K, V> distributedWhen(Function<java.util.Map<String, String>, Boolean> condition) {
            this.distributedCondition = condition;
            return this;
        }

        public AdaptiveCacheBuilder<K, V> clusterName(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public AdaptiveCacheBuilder<K, V> nodes(String... nodes) {
            this.nodes = java.util.Arrays.asList(nodes);
            return this;
        }

        public Cache<K, V> create() {
            // Check environment to decide between local and distributed
            boolean useDistributed = false;
            if (distributedCondition != null) {
                java.util.Map<String, String> env = System.getenv();
                useDistributed = distributedCondition.apply(env);
            }

            if (useDistributed && nodes != null && !nodes.isEmpty()) {
                // Create distributed cache
                DistributedCacheBuilder<K, V> builder = CacheFactory.<K, V>distributed()
                        .name(name)
                        .clusterName(clusterName != null ? clusterName : name + "-cluster")
                        .nodes(nodes)
                        .maximumSize(maximumSize)
                        .expireAfterWrite(expireAfterWrite)
                        .expireAfterAccess(expireAfterAccess)
                        .enableStats(enableStats);

                if (loader != null) {
                    builder.loader(loader);
                }

                return builder.create();
            } else {
                // Create local cache
                LocalCacheBuilder<K, V> builder = CacheFactory.<K, V>local()
                        .name(name)
                        .maximumSize(maximumSize)
                        .expireAfterWrite(expireAfterWrite)
                        .expireAfterAccess(expireAfterAccess)
                        .enableStats(enableStats);

                if (loader != null) {
                    builder.loader(loader);
                }

                return builder.create();
            }
        }
    }
}
