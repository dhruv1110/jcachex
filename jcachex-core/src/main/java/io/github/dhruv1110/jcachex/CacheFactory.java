package io.github.dhruv1110.jcachex;

import io.github.dhruv1110.jcachex.distributed.DistributedCache;
import io.github.dhruv1110.jcachex.observability.MetricsRegistry;
import io.github.dhruv1110.jcachex.resilience.CircuitBreaker;
import io.github.dhruv1110.jcachex.warming.CacheWarmingStrategy;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Function;

/**
 * Factory for creating distributed caches.
 * <p>
 * This factory provides specialized functionality for distributed caching
 * scenarios.
 * For local caching, use {@link JCacheXBuilder} instead.
 * </p>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>{@code
 * // Production-ready distributed cache
 * DistributedCache<String, User> distributedCache = CacheFactory.distributed()
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
 * }</pre>
 *
 * @since 1.0.0
 */
public class CacheFactory {

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
     * Builder for distributed caches with all advanced features.
     */
    public static class DistributedCacheBuilder<K, V> {
        private String name = "default-distributed-cache";
        private String clusterName = "default-cluster";
        private Collection<String> nodes;
        private int replicationFactor = 1;
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

        public DistributedCacheBuilder<K, V> nodes(Collection<String> nodes) {
            this.nodes = nodes;
            return this;
        }

        public DistributedCacheBuilder<K, V> nodes(String... nodes) {
            this.nodes = java.util.Arrays.asList(nodes);
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
}
