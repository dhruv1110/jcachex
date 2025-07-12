package io.github.dhruv1110.jcachex.spring;

import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JCacheXProperties configuration class.
 */
@DisplayName("JCacheXProperties Tests")
class JCacheXPropertiesTest {

    private JCacheXProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JCacheXProperties();
    }

    @Nested
    @DisplayName("Default Configuration Tests")
    class DefaultConfigurationTests {

        @Test
        @DisplayName("Should have correct default values")
        void shouldHaveCorrectDefaultValues() {
            assertTrue(properties.isEnabled(), "JCacheX should be enabled by default");
            assertTrue(properties.isAutoCreateCaches(), "Auto-create caches should be enabled by default");
            assertNotNull(properties.getDefaultConfig(), "Default config should not be null");
            assertNotNull(properties.getCaches(), "Caches map should not be null");
            assertTrue(properties.getCaches().isEmpty(), "Caches map should be empty by default");
        }

        @Test
        @DisplayName("Should allow enabling/disabling JCacheX")
        void shouldAllowEnablingDisabling() {
            properties.setEnabled(false);
            assertFalse(properties.isEnabled(), "JCacheX should be disabled");

            properties.setEnabled(true);
            assertTrue(properties.isEnabled(), "JCacheX should be enabled");
        }

        @Test
        @DisplayName("Should allow configuring auto-create caches")
        void shouldAllowConfiguringAutoCreateCaches() {
            properties.setAutoCreateCaches(false);
            assertFalse(properties.isAutoCreateCaches(), "Auto-create caches should be disabled");

            properties.setAutoCreateCaches(true);
            assertTrue(properties.isAutoCreateCaches(), "Auto-create caches should be enabled");
        }
    }

    @Nested
    @DisplayName("Cache Configuration Tests")
    class CacheConfigurationTests {

        @Test
        @DisplayName("Should configure default cache settings")
        void shouldConfigureDefaultCacheSettings() {
            JCacheXProperties.CacheConfig defaultConfig = properties.getDefaultConfig();

            defaultConfig.setMaximumSize(1000L);
            defaultConfig.setExpireAfterSeconds(300L);
            defaultConfig.setEnableStatistics(true);
            defaultConfig.setEvictionStrategy("LRU");

            assertEquals(1000L, defaultConfig.getMaximumSize(), "Maximum size should be set");
            assertEquals(300L, defaultConfig.getExpireAfterSeconds(), "Expire after seconds should be set");
            assertTrue(defaultConfig.getEnableStatistics(), "Statistics should be enabled");
            assertEquals("LRU", defaultConfig.getEvictionStrategy(), "Eviction strategy should be LRU");
        }

        @Test
        @DisplayName("Should configure named caches")
        void shouldConfigureNamedCaches() {
            Map<String, JCacheXProperties.CacheConfig> caches = new HashMap<>();

            JCacheXProperties.CacheConfig userCache = new JCacheXProperties.CacheConfig();
            userCache.setMaximumSize(500L);
            userCache.setExpireAfterSeconds(600L);
            userCache.setEvictionStrategy("LFU");
            caches.put("users", userCache);

            JCacheXProperties.CacheConfig productCache = new JCacheXProperties.CacheConfig();
            productCache.setMaximumSize(2000L);
            productCache.setExpireAfterAccessSeconds(1800L);
            productCache.setEvictionStrategy("FIFO");
            caches.put("products", productCache);

            properties.setCaches(caches);

            assertEquals(2, properties.getCaches().size(), "Should have 2 named caches");

            JCacheXProperties.CacheConfig retrievedUserCache = properties.getCaches().get("users");
            assertNotNull(retrievedUserCache, "User cache should exist");
            assertEquals(500L, retrievedUserCache.getMaximumSize(), "User cache max size should be correct");
            assertEquals("LFU", retrievedUserCache.getEvictionStrategy(), "User cache eviction strategy should be LFU");

            JCacheXProperties.CacheConfig retrievedProductCache = properties.getCaches().get("products");
            assertNotNull(retrievedProductCache, "Product cache should exist");
            assertEquals(2000L, retrievedProductCache.getMaximumSize(), "Product cache max size should be correct");
            assertEquals(1800L, retrievedProductCache.getExpireAfterAccessSeconds(),
                    "Product cache expire after access should be correct");
        }

        @Test
        @DisplayName("Should handle all cache configuration options")
        void shouldHandleAllCacheConfigurationOptions() {
            JCacheXProperties.CacheConfig config = new JCacheXProperties.CacheConfig();

            // Basic settings
            config.setMaximumSize(1000L);
            config.setMaximumWeight(2000L);
            config.setExpireAfterSeconds(300L);
            config.setExpireAfterAccessSeconds(600L);
            config.setRefreshAfterWriteSeconds(120L);
            config.setEvictionStrategy("LRU");

            // Feature flags
            config.setEnableStatistics(true);
            config.setEnableJmx(true);
            config.setEnableObservability(true);
            config.setEnableResilience(true);
            config.setEnableWarming(true);

            // Reference types
            config.setWeakKeys(true);
            config.setWeakValues(true);
            config.setSoftValues(false);

            // Loader settings
            config.setAsyncLoader(true);
            config.setLoader("com.example.MyLoader");
            config.setWeigher("com.example.MyWeigher");

            // Warming settings
            config.setWarmingStrategy("EAGER");
            config.setWarmingBatchSize(100);
            config.setWarmingDelaySeconds(30);

            // Composite strategies
            config.setCompositeStrategies(Arrays.asList("LRU", "LFU"));
            config.setIdleTimeThresholdSeconds(1800L);

            // Verify all settings
            assertEquals(1000L, config.getMaximumSize());
            assertEquals(2000L, config.getMaximumWeight());
            assertEquals(300L, config.getExpireAfterSeconds());
            assertEquals(600L, config.getExpireAfterAccessSeconds());
            assertEquals(120L, config.getRefreshAfterWriteSeconds());
            assertEquals("LRU", config.getEvictionStrategy());

            assertTrue(config.getEnableStatistics());
            assertTrue(config.getEnableJmx());
            assertTrue(config.getEnableObservability());
            assertTrue(config.getEnableResilience());
            assertTrue(config.getEnableWarming());

            assertTrue(config.getWeakKeys());
            assertTrue(config.getWeakValues());
            assertFalse(config.getSoftValues());

            assertTrue(config.getAsyncLoader());
            assertEquals("com.example.MyLoader", config.getLoader());
            assertEquals("com.example.MyWeigher", config.getWeigher());

            assertEquals("EAGER", config.getWarmingStrategy());
            assertEquals(100, config.getWarmingBatchSize());
            assertEquals(30, config.getWarmingDelaySeconds());

            assertEquals(Arrays.asList("LRU", "LFU"), config.getCompositeStrategies());
            assertEquals(1800L, config.getIdleTimeThresholdSeconds());
        }
    }

    @Nested
    @DisplayName("Distributed Configuration Tests")
    class DistributedConfigurationTests {

        @Test
        @DisplayName("Should configure distributed cache settings")
        void shouldConfigureDistributedCacheSettings() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.DistributedConfig distributedConfig = config.getDistributed();

            distributedConfig.setEnabled(true);
            distributedConfig.setClusterName("test-cluster");
            distributedConfig.setNodes(Arrays.asList("node1:8080", "node2:8080"));
            distributedConfig.setReplicationFactor(3);
            distributedConfig.setConsistencyLevel("STRONG");
            distributedConfig.setNetworkProtocol("TCP");
            distributedConfig.setPort(8080);
            distributedConfig.setTimeoutSeconds(30);
            distributedConfig.setAutoDiscovery(false);
            distributedConfig.setHeartbeatIntervalSeconds(10);
            distributedConfig.setMaxRetries(3);

            assertTrue(distributedConfig.getEnabled());
            assertEquals("test-cluster", distributedConfig.getClusterName());
            assertEquals(Arrays.asList("node1:8080", "node2:8080"), distributedConfig.getNodes());
            assertEquals(3, distributedConfig.getReplicationFactor());
            assertEquals("STRONG", distributedConfig.getConsistencyLevel());
            assertEquals("TCP", distributedConfig.getNetworkProtocol());
            assertEquals(8080, distributedConfig.getPort());
            assertEquals(30, distributedConfig.getTimeoutSeconds());
            assertFalse(distributedConfig.getAutoDiscovery());
            assertEquals(10, distributedConfig.getHeartbeatIntervalSeconds());
            assertEquals(3, distributedConfig.getMaxRetries());
        }

        @Test
        @DisplayName("Should have correct default distributed settings")
        void shouldHaveCorrectDefaultDistributedSettings() {
            JCacheXProperties.DistributedConfig distributedConfig = properties.getDefaultConfig().getDistributed();

            assertFalse(distributedConfig.getEnabled(), "Distributed should be disabled by default");
            assertEquals("jcachex-cluster", distributedConfig.getClusterName(), "Default cluster name should be set");
            assertEquals(2, distributedConfig.getReplicationFactor(), "Default replication factor should be 2");
            assertEquals("EVENTUAL", distributedConfig.getConsistencyLevel(), "Default consistency should be EVENTUAL");
            assertEquals("TCP", distributedConfig.getNetworkProtocol(), "Default protocol should be TCP");
            assertEquals(8080, distributedConfig.getPort(), "Default port should be 8080");
            assertEquals(30, distributedConfig.getTimeoutSeconds(), "Default timeout should be 30 seconds");
            assertTrue(distributedConfig.getAutoDiscovery(), "Auto discovery should be enabled by default");
            assertEquals(10, distributedConfig.getHeartbeatIntervalSeconds(),
                    "Default heartbeat interval should be 10 seconds");
            assertEquals(3, distributedConfig.getMaxRetries(), "Default max retries should be 3");
        }
    }

    @Nested
    @DisplayName("Network Configuration Tests")
    class NetworkConfigurationTests {

        @Test
        @DisplayName("Should configure network settings")
        void shouldConfigureNetworkSettings() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.NetworkConfig networkConfig = config.getNetwork();

            networkConfig.setProtocol("UDP");
            networkConfig.setSerialization("JSON");
            networkConfig.setCompression("GZIP");
            networkConfig.setEncryption(true);
            networkConfig.setPort(9090);
            networkConfig.setConnectionPoolSize(20);
            networkConfig.setConnectionTimeoutSeconds(10);
            networkConfig.setReadTimeoutSeconds(30);
            networkConfig.setEnableCompression(true);
            networkConfig.setEnableEncryption(true);

            assertEquals("UDP", networkConfig.getProtocol());
            assertEquals("JSON", networkConfig.getSerialization());
            assertEquals("GZIP", networkConfig.getCompression());
            assertTrue(networkConfig.getEncryption());
            assertEquals(9090, networkConfig.getPort());
            assertEquals(20, networkConfig.getConnectionPoolSize());
            assertEquals(10, networkConfig.getConnectionTimeoutSeconds());
            assertEquals(30, networkConfig.getReadTimeoutSeconds());
            assertTrue(networkConfig.getEnableCompression());
            assertTrue(networkConfig.getEnableEncryption());
        }

        @Test
        @DisplayName("Should have correct default network settings")
        void shouldHaveCorrectDefaultNetworkSettings() {
            JCacheXProperties.NetworkConfig networkConfig = properties.getDefaultConfig().getNetwork();

            assertEquals("TCP", networkConfig.getProtocol(), "Default protocol should be TCP");
            assertEquals("KRYO", networkConfig.getSerialization(), "Default serialization should be KRYO");
            assertEquals("LZ4", networkConfig.getCompression(), "Default compression should be LZ4");
            assertFalse(networkConfig.getEncryption(), "Encryption should be disabled by default");
            assertEquals(8080, networkConfig.getPort(), "Default port should be 8080");
            assertEquals(10, networkConfig.getConnectionPoolSize(), "Default connection pool size should be 10");
            assertEquals(5, networkConfig.getConnectionTimeoutSeconds(),
                    "Default connection timeout should be 5 seconds");
            assertEquals(10, networkConfig.getReadTimeoutSeconds(), "Default read timeout should be 10 seconds");
            assertTrue(networkConfig.getEnableCompression(), "Compression should be enabled by default");
            assertFalse(networkConfig.getEnableEncryption(), "Encryption should be disabled by default");
        }
    }

    @Nested
    @DisplayName("Event Listeners Configuration Tests")
    class EventListenersConfigurationTests {

        @Test
        @DisplayName("Should configure event listeners")
        void shouldConfigureEventListeners() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.EventListenersConfig eventConfig = config.getEventListeners();

            eventConfig.setEnabled(true);
            eventConfig.setListeners(Arrays.asList("com.example.Listener1", "com.example.Listener2"));
            eventConfig.setAsyncExecution(false);
            eventConfig.setThreadPoolSize(5);

            assertTrue(eventConfig.getEnabled());
            assertEquals(Arrays.asList("com.example.Listener1", "com.example.Listener2"), eventConfig.getListeners());
            assertFalse(eventConfig.getAsyncExecution());
            assertEquals(5, eventConfig.getThreadPoolSize());
        }
    }

    @Nested
    @DisplayName("Resilience Configuration Tests")
    class ResilienceConfigurationTests {

        @Test
        @DisplayName("Should configure circuit breaker")
        void shouldConfigureCircuitBreaker() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.ResilienceConfig.CircuitBreakerConfig circuitBreaker = config.getResilience()
                    .getCircuitBreaker();

            circuitBreaker.setEnabled(true);
            circuitBreaker.setFailureThreshold(10);
            circuitBreaker.setTimeoutSeconds(120);
            circuitBreaker.setCheckIntervalSeconds(30);

            assertTrue(circuitBreaker.getEnabled());
            assertEquals(10, circuitBreaker.getFailureThreshold());
            assertEquals(120, circuitBreaker.getTimeoutSeconds());
            assertEquals(30, circuitBreaker.getCheckIntervalSeconds());
        }

        @Test
        @DisplayName("Should configure retry policy")
        void shouldConfigureRetryPolicy() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.ResilienceConfig.RetryPolicyConfig retryPolicy = config.getResilience().getRetryPolicy();

            retryPolicy.setEnabled(true);
            retryPolicy.setMaxAttempts(5);
            retryPolicy.setInitialDelaySeconds(2);
            retryPolicy.setMaxDelaySeconds(120);
            retryPolicy.setMultiplier(3.0);

            assertTrue(retryPolicy.getEnabled());
            assertEquals(5, retryPolicy.getMaxAttempts());
            assertEquals(2, retryPolicy.getInitialDelaySeconds());
            assertEquals(120, retryPolicy.getMaxDelaySeconds());
            assertEquals(3.0, retryPolicy.getMultiplier(), 0.001);
        }
    }

    @Nested
    @DisplayName("Observability Configuration Tests")
    class ObservabilityConfigurationTests {

        @Test
        @DisplayName("Should configure observability settings")
        void shouldConfigureObservabilitySettings() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.ObservabilityConfig observabilityConfig = config.getObservability();

            observabilityConfig.setEnabled(true);
            observabilityConfig.setEnableMetrics(true);
            observabilityConfig.setEnableHealthIndicators(false);
            observabilityConfig.setMetricsIntervalSeconds(30);
            observabilityConfig.setMetricsTags(Arrays.asList("env:test", "service:cache"));

            assertTrue(observabilityConfig.getEnabled());
            assertTrue(observabilityConfig.getEnableMetrics());
            assertFalse(observabilityConfig.getEnableHealthIndicators());
            assertEquals(30, observabilityConfig.getMetricsIntervalSeconds());
            assertEquals(Arrays.asList("env:test", "service:cache"), observabilityConfig.getMetricsTags());
        }
    }
}
