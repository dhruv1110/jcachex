package io.github.dhruv1110.jcachex.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Configuration properties for JCacheX in Spring Boot applications.
 *
 * This class provides type-safe configuration binding for JCacheX cache
 * settings
 * through Spring Boot's configuration properties mechanism. It allows you to
 * configure
 * cache behavior through application.yml, application.properties, or
 * environment variables.
 *
 * <h2>Application Properties Examples:</h2>
 *
 * <h3>YAML Configuration:</h3>
 *
 * <pre>{@code
 * jcachex:
 *   default:
 *     maximumSize: 1000
 *     expireAfterSeconds: 1800  # 30 minutes
 *     enableStatistics: true
 *     enableJmx: false
 *   caches:
 *     users:
 *       maximumSize: 5000
 *       expireAfterSeconds: 3600  # 1 hour
 *       enableStatistics: true
 *       enableJmx: true
 *     sessions:
 *       maximumSize: 10000
 *       expireAfterSeconds: 1800  # 30 minutes
 *       enableStatistics: false
 *     apiResponses:
 *       maximumSize: 2000
 *       expireAfterSeconds: 300   # 5 minutes
 *       enableStatistics: true
 * }</pre>
 *
 * <h3>Properties File Configuration:</h3>
 *
 * <pre>{@code
 * # Default cache configuration
 * jcachex.default.maximumSize=1000
 * jcachex.default.expireAfterSeconds=1800
 * jcachex.default.enableStatistics=true
 * jcachex.default.enableJmx=false
 *
 * # Named cache configurations
 * jcachex.caches.users.maximumSize=5000
 * jcachex.caches.users.expireAfterSeconds=3600
 * jcachex.caches.users.enableStatistics=true
 *
 * jcachex.caches.sessions.maximumSize=10000
 * jcachex.caches.sessions.expireAfterSeconds=1800
 * jcachex.caches.sessions.enableStatistics=false
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "jcachex")
public class JCacheXProperties {

    /**
     * Default cache configuration that applies to all caches unless overridden.
     */
    private CacheConfig defaultConfig = new CacheConfig();

    /**
     * Named cache configurations that override the default configuration.
     */
    private Map<String, CacheConfig> caches = new HashMap<>();

    /**
     * Whether JCacheX auto-configuration is enabled.
     */
    private boolean enabled = true;

    /**
     * Whether to automatically create caches from configuration.
     */
    private boolean autoCreateCaches = true;

    // Getters and setters
    public CacheConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(CacheConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public Map<String, CacheConfig> getCaches() {
        return caches;
    }

    public void setCaches(Map<String, CacheConfig> caches) {
        this.caches = caches;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAutoCreateCaches() {
        return autoCreateCaches;
    }

    public void setAutoCreateCaches(boolean autoCreateCaches) {
        this.autoCreateCaches = autoCreateCaches;
    }

    /**
     * Configuration for individual cache instances.
     */
    public static class CacheConfig {
        // Basic configuration
        private Long maximumSize;
        private Long maximumWeight;
        private Long expireAfterSeconds;
        private Long expireAfterAccessSeconds;
        private Long refreshAfterWriteSeconds;
        private String evictionStrategy = "LRU";
        private Boolean enableStatistics = false;
        private Boolean enableJmx = false;
        private Boolean enableObservability = false;
        private Boolean enableResilience = false;
        private Boolean enableWarming = false;

        // Reference types
        private Boolean weakKeys = false;
        private Boolean weakValues = false;
        private Boolean softValues = false;

        // Async configuration
        private Boolean asyncLoader = false;
        private String loader;
        private String weigher;

        // Warming configuration
        private String warmingStrategy;
        private Integer warmingBatchSize;
        private Integer warmingDelaySeconds;

        // Composite eviction strategies
        private List<String> compositeStrategies;
        private Long idleTimeThresholdSeconds;

        // Distributed configuration
        private DistributedConfig distributed = new DistributedConfig();

        // Network configuration
        private NetworkConfig network = new NetworkConfig();

        // Event listeners configuration
        private EventListenersConfig eventListeners = new EventListenersConfig();

        // Resilience configuration
        private ResilienceConfig resilience = new ResilienceConfig();

        // Observability configuration
        private ObservabilityConfig observability = new ObservabilityConfig();

        // Getters and setters
        public Long getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(Long maximumSize) {
            this.maximumSize = maximumSize;
        }

        public Long getMaximumWeight() {
            return maximumWeight;
        }

        public void setMaximumWeight(Long maximumWeight) {
            this.maximumWeight = maximumWeight;
        }

        public Long getExpireAfterSeconds() {
            return expireAfterSeconds;
        }

        public void setExpireAfterSeconds(Long expireAfterSeconds) {
            this.expireAfterSeconds = expireAfterSeconds;
        }

        public Long getExpireAfterAccessSeconds() {
            return expireAfterAccessSeconds;
        }

        public void setExpireAfterAccessSeconds(Long expireAfterAccessSeconds) {
            this.expireAfterAccessSeconds = expireAfterAccessSeconds;
        }

        public Long getRefreshAfterWriteSeconds() {
            return refreshAfterWriteSeconds;
        }

        public void setRefreshAfterWriteSeconds(Long refreshAfterWriteSeconds) {
            this.refreshAfterWriteSeconds = refreshAfterWriteSeconds;
        }

        public String getEvictionStrategy() {
            return evictionStrategy;
        }

        public void setEvictionStrategy(String evictionStrategy) {
            this.evictionStrategy = evictionStrategy;
        }

        public Boolean getEnableStatistics() {
            return enableStatistics;
        }

        public void setEnableStatistics(Boolean enableStatistics) {
            this.enableStatistics = enableStatistics;
        }

        public Boolean getEnableJmx() {
            return enableJmx;
        }

        public void setEnableJmx(Boolean enableJmx) {
            this.enableJmx = enableJmx;
        }

        public Boolean getEnableObservability() {
            return enableObservability;
        }

        public void setEnableObservability(Boolean enableObservability) {
            this.enableObservability = enableObservability;
        }

        public Boolean getEnableResilience() {
            return enableResilience;
        }

        public void setEnableResilience(Boolean enableResilience) {
            this.enableResilience = enableResilience;
        }

        public Boolean getEnableWarming() {
            return enableWarming;
        }

        public void setEnableWarming(Boolean enableWarming) {
            this.enableWarming = enableWarming;
        }

        public Boolean getWeakKeys() {
            return weakKeys;
        }

        public void setWeakKeys(Boolean weakKeys) {
            this.weakKeys = weakKeys;
        }

        public Boolean getWeakValues() {
            return weakValues;
        }

        public void setWeakValues(Boolean weakValues) {
            this.weakValues = weakValues;
        }

        public Boolean getSoftValues() {
            return softValues;
        }

        public void setSoftValues(Boolean softValues) {
            this.softValues = softValues;
        }

        public Boolean getAsyncLoader() {
            return asyncLoader;
        }

        public void setAsyncLoader(Boolean asyncLoader) {
            this.asyncLoader = asyncLoader;
        }

        public String getLoader() {
            return loader;
        }

        public void setLoader(String loader) {
            this.loader = loader;
        }

        public String getWeigher() {
            return weigher;
        }

        public void setWeigher(String weigher) {
            this.weigher = weigher;
        }

        public String getWarmingStrategy() {
            return warmingStrategy;
        }

        public void setWarmingStrategy(String warmingStrategy) {
            this.warmingStrategy = warmingStrategy;
        }

        public Integer getWarmingBatchSize() {
            return warmingBatchSize;
        }

        public void setWarmingBatchSize(Integer warmingBatchSize) {
            this.warmingBatchSize = warmingBatchSize;
        }

        public Integer getWarmingDelaySeconds() {
            return warmingDelaySeconds;
        }

        public void setWarmingDelaySeconds(Integer warmingDelaySeconds) {
            this.warmingDelaySeconds = warmingDelaySeconds;
        }

        public List<String> getCompositeStrategies() {
            return compositeStrategies;
        }

        public void setCompositeStrategies(List<String> compositeStrategies) {
            this.compositeStrategies = compositeStrategies;
        }

        public Long getIdleTimeThresholdSeconds() {
            return idleTimeThresholdSeconds;
        }

        public void setIdleTimeThresholdSeconds(Long idleTimeThresholdSeconds) {
            this.idleTimeThresholdSeconds = idleTimeThresholdSeconds;
        }

        public DistributedConfig getDistributed() {
            return distributed;
        }

        public void setDistributed(DistributedConfig distributed) {
            this.distributed = distributed;
        }

        public NetworkConfig getNetwork() {
            return network;
        }

        public void setNetwork(NetworkConfig network) {
            this.network = network;
        }

        public EventListenersConfig getEventListeners() {
            return eventListeners;
        }

        public void setEventListeners(EventListenersConfig eventListeners) {
            this.eventListeners = eventListeners;
        }

        public ResilienceConfig getResilience() {
            return resilience;
        }

        public void setResilience(ResilienceConfig resilience) {
            this.resilience = resilience;
        }

        public ObservabilityConfig getObservability() {
            return observability;
        }

        public void setObservability(ObservabilityConfig observability) {
            this.observability = observability;
        }
    }

    /**
     * Configuration for distributed cache functionality.
     */
    public static class DistributedConfig {
        private Boolean enabled = false;
        private String clusterName = "jcachex-cluster";
        private List<String> nodes;
        private List<String> seedNodes;
        private Integer replicationFactor = 2;
        private String consistencyLevel = "EVENTUAL";
        private String networkProtocol = "TCP";
        private Integer port = 8080;
        private Integer timeoutSeconds = 30;
        private Boolean autoDiscovery = true;
        private Integer heartbeatIntervalSeconds = 10;
        private Integer maxRetries = 3;

        // Getters and setters
        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public List<String> getNodes() {
            return nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public List<String> getSeedNodes() {
            return seedNodes;
        }

        public void setSeedNodes(List<String> seedNodes) {
            this.seedNodes = seedNodes;
        }

        public Integer getReplicationFactor() {
            return replicationFactor;
        }

        public void setReplicationFactor(Integer replicationFactor) {
            this.replicationFactor = replicationFactor;
        }

        public String getConsistencyLevel() {
            return consistencyLevel;
        }

        public void setConsistencyLevel(String consistencyLevel) {
            this.consistencyLevel = consistencyLevel;
        }

        public String getNetworkProtocol() {
            return networkProtocol;
        }

        public void setNetworkProtocol(String networkProtocol) {
            this.networkProtocol = networkProtocol;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Integer getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(Integer timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public Boolean getAutoDiscovery() {
            return autoDiscovery;
        }

        public void setAutoDiscovery(Boolean autoDiscovery) {
            this.autoDiscovery = autoDiscovery;
        }

        public Integer getHeartbeatIntervalSeconds() {
            return heartbeatIntervalSeconds;
        }

        public void setHeartbeatIntervalSeconds(Integer heartbeatIntervalSeconds) {
            this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
        }

        public Integer getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
        }
    }

    /**
     * Configuration for network-related settings.
     */
    public static class NetworkConfig {
        private String protocol = "TCP";
        private String serialization = "KRYO";
        private String compression = "LZ4";
        private Boolean encryption = false;
        private Integer port = 8080;
        private Integer connectionPoolSize = 10;
        private Integer connectionTimeoutSeconds = 5;
        private Integer readTimeoutSeconds = 10;
        private Boolean enableCompression = true;
        private Boolean enableEncryption = false;

        // Getters and setters
        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getSerialization() {
            return serialization;
        }

        public void setSerialization(String serialization) {
            this.serialization = serialization;
        }

        public String getCompression() {
            return compression;
        }

        public void setCompression(String compression) {
            this.compression = compression;
        }

        public Boolean getEncryption() {
            return encryption;
        }

        public void setEncryption(Boolean encryption) {
            this.encryption = encryption;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Integer getConnectionPoolSize() {
            return connectionPoolSize;
        }

        public void setConnectionPoolSize(Integer connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
        }

        public Integer getConnectionTimeoutSeconds() {
            return connectionTimeoutSeconds;
        }

        public void setConnectionTimeoutSeconds(Integer connectionTimeoutSeconds) {
            this.connectionTimeoutSeconds = connectionTimeoutSeconds;
        }

        public Integer getReadTimeoutSeconds() {
            return readTimeoutSeconds;
        }

        public void setReadTimeoutSeconds(Integer readTimeoutSeconds) {
            this.readTimeoutSeconds = readTimeoutSeconds;
        }

        public Boolean getEnableCompression() {
            return enableCompression;
        }

        public void setEnableCompression(Boolean enableCompression) {
            this.enableCompression = enableCompression;
        }

        public Boolean getEnableEncryption() {
            return enableEncryption;
        }

        public void setEnableEncryption(Boolean enableEncryption) {
            this.enableEncryption = enableEncryption;
        }
    }

    /**
     * Configuration for cache event listeners.
     */
    public static class EventListenersConfig {
        private Boolean enabled = false;
        private List<String> listeners;
        private Boolean asyncExecution = true;
        private Integer threadPoolSize = 2;

        // Getters and setters
        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getListeners() {
            return listeners;
        }

        public void setListeners(List<String> listeners) {
            this.listeners = listeners;
        }

        public Boolean getAsyncExecution() {
            return asyncExecution;
        }

        public void setAsyncExecution(Boolean asyncExecution) {
            this.asyncExecution = asyncExecution;
        }

        public Integer getThreadPoolSize() {
            return threadPoolSize;
        }

        public void setThreadPoolSize(Integer threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
        }
    }

    /**
     * Configuration for resilience features (circuit breaker, retry policy).
     */
    public static class ResilienceConfig {
        private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();
        private RetryPolicyConfig retryPolicy = new RetryPolicyConfig();

        // Getters and setters
        public CircuitBreakerConfig getCircuitBreaker() {
            return circuitBreaker;
        }

        public void setCircuitBreaker(CircuitBreakerConfig circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
        }

        public RetryPolicyConfig getRetryPolicy() {
            return retryPolicy;
        }

        public void setRetryPolicy(RetryPolicyConfig retryPolicy) {
            this.retryPolicy = retryPolicy;
        }

        public static class CircuitBreakerConfig {
            private Boolean enabled = false;
            private Integer failureThreshold = 5;
            private Integer timeoutSeconds = 60;
            private Integer checkIntervalSeconds = 10;

            // Getters and setters
            public Boolean getEnabled() {
                return enabled;
            }

            public void setEnabled(Boolean enabled) {
                this.enabled = enabled;
            }

            public Integer getFailureThreshold() {
                return failureThreshold;
            }

            public void setFailureThreshold(Integer failureThreshold) {
                this.failureThreshold = failureThreshold;
            }

            public Integer getTimeoutSeconds() {
                return timeoutSeconds;
            }

            public void setTimeoutSeconds(Integer timeoutSeconds) {
                this.timeoutSeconds = timeoutSeconds;
            }

            public Integer getCheckIntervalSeconds() {
                return checkIntervalSeconds;
            }

            public void setCheckIntervalSeconds(Integer checkIntervalSeconds) {
                this.checkIntervalSeconds = checkIntervalSeconds;
            }
        }

        public static class RetryPolicyConfig {
            private Boolean enabled = false;
            private Integer maxAttempts = 3;
            private Integer initialDelaySeconds = 1;
            private Integer maxDelaySeconds = 60;
            private Double multiplier = 2.0;

            // Getters and setters
            public Boolean getEnabled() {
                return enabled;
            }

            public void setEnabled(Boolean enabled) {
                this.enabled = enabled;
            }

            public Integer getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(Integer maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public Integer getInitialDelaySeconds() {
                return initialDelaySeconds;
            }

            public void setInitialDelaySeconds(Integer initialDelaySeconds) {
                this.initialDelaySeconds = initialDelaySeconds;
            }

            public Integer getMaxDelaySeconds() {
                return maxDelaySeconds;
            }

            public void setMaxDelaySeconds(Integer maxDelaySeconds) {
                this.maxDelaySeconds = maxDelaySeconds;
            }

            public Double getMultiplier() {
                return multiplier;
            }

            public void setMultiplier(Double multiplier) {
                this.multiplier = multiplier;
            }
        }
    }

    /**
     * Configuration for observability features (metrics, monitoring).
     */
    public static class ObservabilityConfig {
        private Boolean enabled = false;
        private Boolean enableMetrics = true;
        private Boolean enableHealthIndicators = true;
        private Integer metricsIntervalSeconds = 60;
        private List<String> metricsTags;

        // Getters and setters
        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Boolean getEnableMetrics() {
            return enableMetrics;
        }

        public void setEnableMetrics(Boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
        }

        public Boolean getEnableHealthIndicators() {
            return enableHealthIndicators;
        }

        public void setEnableHealthIndicators(Boolean enableHealthIndicators) {
            this.enableHealthIndicators = enableHealthIndicators;
        }

        public Integer getMetricsIntervalSeconds() {
            return metricsIntervalSeconds;
        }

        public void setMetricsIntervalSeconds(Integer metricsIntervalSeconds) {
            this.metricsIntervalSeconds = metricsIntervalSeconds;
        }

        public List<String> getMetricsTags() {
            return metricsTags;
        }

        public void setMetricsTags(List<String> metricsTags) {
            this.metricsTags = metricsTags;
        }
    }
}
