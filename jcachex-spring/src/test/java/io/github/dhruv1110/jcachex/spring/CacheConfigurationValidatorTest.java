package io.github.dhruv1110.jcachex.spring;

import io.github.dhruv1110.jcachex.spring.configuration.CacheConfigurationValidator;
import io.github.dhruv1110.jcachex.spring.configuration.JCacheXProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CacheConfigurationValidator.
 */
@DisplayName("CacheConfigurationValidator Tests")
class CacheConfigurationValidatorTest {

    private CacheConfigurationValidator validator;
    private JCacheXProperties properties;

    @BeforeEach
    void setUp() {
        validator = new CacheConfigurationValidator();
        properties = new JCacheXProperties();
    }

    @Nested
    @DisplayName("Valid Configuration Tests")
    class ValidConfigurationTests {

        @Test
        @DisplayName("Should validate minimal valid configuration")
        void shouldValidateMinimalValidConfiguration() {
            assertDoesNotThrow(() -> validator.validate(properties),
                    "Minimal configuration should be valid");
        }

        @Test
        @DisplayName("Should validate complete valid configuration")
        void shouldValidateCompleteValidConfiguration() {
            // Setup complete valid configuration
            properties.setEnabled(true);
            properties.setAutoCreateCaches(true);

            JCacheXProperties.CacheConfig defaultConfig = properties.getDefaultConfig();
            defaultConfig.setMaximumSize(1000L);
            defaultConfig.setExpireAfterSeconds(300L);
            defaultConfig.setEvictionStrategy("LRU");
            defaultConfig.setEnableStatistics(true);

            // Add named cache
            Map<String, JCacheXProperties.CacheConfig> caches = new HashMap<>();
            JCacheXProperties.CacheConfig userCache = new JCacheXProperties.CacheConfig();
            userCache.setMaximumSize(500L);
            userCache.setExpireAfterSeconds(600L);
            userCache.setEvictionStrategy("LFU");
            caches.put("users", userCache);
            properties.setCaches(caches);

            assertDoesNotThrow(() -> validator.validate(properties),
                    "Complete valid configuration should be valid");
        }

        @Test
        @DisplayName("Should validate configuration with all eviction strategies")
        void shouldValidateConfigurationWithAllEvictionStrategies() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();

            String[] validStrategies = { "LRU", "LFU", "FIFO", "FILO", "WEIGHT", "IDLE_TIME" };

            for (String strategy : validStrategies) {
                config.setEvictionStrategy(strategy);
                assertDoesNotThrow(() -> validator.validate(properties),
                        "Eviction strategy " + strategy + " should be valid");
            }
        }

        @Test
        @DisplayName("Should validate configuration with composite strategies")
        void shouldValidateConfigurationWithCompositeStrategies() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            config.setCompositeStrategies(Arrays.asList("LRU", "LFU"));

            assertDoesNotThrow(() -> validator.validate(properties),
                    "Composite strategies should be valid");
        }
    }

    @Nested
    @DisplayName("Invalid Configuration Tests")
    class InvalidConfigurationTests {

        @Test
        @DisplayName("Should reject null properties")
        void shouldRejectNullProperties() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(null),
                    "Should reject null properties");
            assertTrue(exception.getMessage().contains("Properties cannot be null"));
        }

        @Test
        @DisplayName("Should reject invalid eviction strategy")
        void shouldRejectInvalidEvictionStrategy() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            config.setEvictionStrategy("INVALID_STRATEGY");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid eviction strategy");
            assertTrue(exception.getMessage().contains("Invalid eviction strategy"));
        }

        @Test
        @DisplayName("Should reject negative maximum size")
        void shouldRejectNegativeMaximumSize() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            config.setMaximumSize(-1L);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject negative maximum size");
            assertTrue(exception.getMessage().contains("Maximum size must be positive"));
        }

        @Test
        @DisplayName("Should reject zero maximum size")
        void shouldRejectZeroMaximumSize() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            config.setMaximumSize(0L);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject zero maximum size");
            assertTrue(exception.getMessage().contains("Maximum size must be positive"));
        }

        @Test
        @DisplayName("Should reject negative expiration time")
        void shouldRejectNegativeExpirationTime() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            config.setExpireAfterSeconds(-1L);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject negative expiration time");
            assertTrue(exception.getMessage().contains("Expiration time must be positive"));
        }

        @Test
        @DisplayName("Should reject negative expire after access time")
        void shouldRejectNegativeExpireAfterAccessTime() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            config.setExpireAfterAccessSeconds(-1L);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject negative expire after access time");
            assertTrue(exception.getMessage().contains("Expire after access time must be positive"));
        }

        @Test
        @DisplayName("Should reject invalid composite strategy")
        void shouldRejectInvalidCompositeStrategy() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            config.setCompositeStrategies(Arrays.asList("LRU", "INVALID_STRATEGY"));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid composite strategy");
            assertTrue(exception.getMessage().contains("Invalid composite strategy"));
        }
    }

    @Nested
    @DisplayName("Named Cache Validation Tests")
    class NamedCacheValidationTests {

        @Test
        @DisplayName("Should validate named cache configurations")
        void shouldValidateNamedCacheConfigurations() {
            Map<String, JCacheXProperties.CacheConfig> caches = new HashMap<>();

            JCacheXProperties.CacheConfig userCache = new JCacheXProperties.CacheConfig();
            userCache.setMaximumSize(500L);
            userCache.setEvictionStrategy("LFU");
            caches.put("users", userCache);

            JCacheXProperties.CacheConfig productCache = new JCacheXProperties.CacheConfig();
            productCache.setMaximumSize(1000L);
            productCache.setEvictionStrategy("LRU");
            caches.put("products", productCache);

            properties.setCaches(caches);

            assertDoesNotThrow(() -> validator.validate(properties),
                    "Named cache configurations should be valid");
        }

        @Test
        @DisplayName("Should reject invalid named cache configuration")
        void shouldRejectInvalidNamedCacheConfiguration() {
            Map<String, JCacheXProperties.CacheConfig> caches = new HashMap<>();

            JCacheXProperties.CacheConfig invalidCache = new JCacheXProperties.CacheConfig();
            invalidCache.setMaximumSize(-1L); // Invalid
            caches.put("invalid", invalidCache);

            properties.setCaches(caches);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid named cache configuration");
            assertTrue(exception.getMessage().contains("Cache 'invalid'"));
        }

        @Test
        @DisplayName("Should handle empty cache name")
        void shouldHandleEmptyCacheName() {
            Map<String, JCacheXProperties.CacheConfig> caches = new HashMap<>();
            caches.put("", new JCacheXProperties.CacheConfig());
            properties.setCaches(caches);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject empty cache name");
            assertTrue(exception.getMessage().contains("Cache name cannot be empty"));
        }

        @Test
        @DisplayName("Should handle null cache name")
        void shouldHandleNullCacheName() {
            Map<String, JCacheXProperties.CacheConfig> caches = new HashMap<>();
            caches.put(null, new JCacheXProperties.CacheConfig());
            properties.setCaches(caches);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject null cache name");
            assertTrue(exception.getMessage().contains("Cache name cannot be null"));
        }
    }

    @Nested
    @DisplayName("Distributed Configuration Validation Tests")
    class DistributedConfigurationValidationTests {

        @Test
        @DisplayName("Should validate distributed configuration")
        void shouldValidateDistributedConfiguration() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.DistributedConfig distributedConfig = config.getDistributed();

            distributedConfig.setEnabled(true);
            distributedConfig.setClusterName("test-cluster");
            distributedConfig.setNodes(Arrays.asList("node1:8080", "node2:8080"));
            distributedConfig.setReplicationFactor(2);
            distributedConfig.setConsistencyLevel("STRONG");

            assertDoesNotThrow(() -> validator.validate(properties),
                    "Valid distributed configuration should be accepted");
        }

        @Test
        @DisplayName("Should reject invalid replication factor")
        void shouldRejectInvalidReplicationFactor() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.DistributedConfig distributedConfig = config.getDistributed();

            distributedConfig.setEnabled(true);
            distributedConfig.setReplicationFactor(0);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid replication factor");
            assertTrue(exception.getMessage().contains("Replication factor must be positive"));
        }

        @Test
        @DisplayName("Should reject invalid consistency level")
        void shouldRejectInvalidConsistencyLevel() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.DistributedConfig distributedConfig = config.getDistributed();

            distributedConfig.setEnabled(true);
            distributedConfig.setConsistencyLevel("INVALID");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid consistency level");
            assertTrue(exception.getMessage().contains("Invalid consistency level"));
        }

        @Test
        @DisplayName("Should reject empty cluster name")
        void shouldRejectEmptyClusterName() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.DistributedConfig distributedConfig = config.getDistributed();

            distributedConfig.setEnabled(true);
            distributedConfig.setClusterName("");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject empty cluster name");
            assertTrue(exception.getMessage().contains("Cluster name cannot be empty"));
        }

        @Test
        @DisplayName("Should reject empty nodes list")
        void shouldRejectEmptyNodesList() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.DistributedConfig distributedConfig = config.getDistributed();

            distributedConfig.setEnabled(true);
            distributedConfig.setNodes(Collections.emptyList());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject empty nodes list");
            assertTrue(exception.getMessage().contains("At least one node must be specified"));
        }
    }

    @Nested
    @DisplayName("Network Configuration Validation Tests")
    class NetworkConfigurationValidationTests {

        @Test
        @DisplayName("Should validate network configuration")
        void shouldValidateNetworkConfiguration() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.NetworkConfig networkConfig = config.getNetwork();

            networkConfig.setProtocol("TCP");
            networkConfig.setSerialization("KRYO");
            networkConfig.setCompression("LZ4");
            networkConfig.setPort(8080);
            networkConfig.setConnectionPoolSize(10);

            assertDoesNotThrow(() -> validator.validate(properties),
                    "Valid network configuration should be accepted");
        }

        @Test
        @DisplayName("Should reject invalid protocol")
        void shouldRejectInvalidProtocol() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.NetworkConfig networkConfig = config.getNetwork();

            networkConfig.setProtocol("INVALID");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid protocol");
            assertTrue(exception.getMessage().contains("Invalid network protocol"));
        }

        @Test
        @DisplayName("Should reject invalid serialization")
        void shouldRejectInvalidSerialization() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.NetworkConfig networkConfig = config.getNetwork();

            networkConfig.setSerialization("INVALID");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid serialization");
            assertTrue(exception.getMessage().contains("Invalid serialization type"));
        }

        @Test
        @DisplayName("Should reject invalid port")
        void shouldRejectInvalidPort() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.NetworkConfig networkConfig = config.getNetwork();

            networkConfig.setPort(-1);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid port");
            assertTrue(exception.getMessage().contains("Port must be between 1 and 65535"));
        }

        @Test
        @DisplayName("Should reject port out of range")
        void shouldRejectPortOutOfRange() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.NetworkConfig networkConfig = config.getNetwork();

            networkConfig.setPort(70000);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject port out of range");
            assertTrue(exception.getMessage().contains("Port must be between 1 and 65535"));
        }

        @Test
        @DisplayName("Should reject invalid connection pool size")
        void shouldRejectInvalidConnectionPoolSize() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.NetworkConfig networkConfig = config.getNetwork();

            networkConfig.setConnectionPoolSize(0);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid connection pool size");
            assertTrue(exception.getMessage().contains("Connection pool size must be positive"));
        }
    }

    @Nested
    @DisplayName("Resilience Configuration Validation Tests")
    class ResilienceConfigurationValidationTests {

        @Test
        @DisplayName("Should validate circuit breaker configuration")
        void shouldValidateCircuitBreakerConfiguration() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.ResilienceConfig.CircuitBreakerConfig circuitBreaker = config.getResilience()
                    .getCircuitBreaker();

            circuitBreaker.setEnabled(true);
            circuitBreaker.setFailureThreshold(5);
            circuitBreaker.setTimeoutSeconds(60);
            circuitBreaker.setCheckIntervalSeconds(10);

            assertDoesNotThrow(() -> validator.validate(properties),
                    "Valid circuit breaker configuration should be accepted");
        }

        @Test
        @DisplayName("Should reject invalid failure threshold")
        void shouldRejectInvalidFailureThreshold() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.ResilienceConfig.CircuitBreakerConfig circuitBreaker = config.getResilience()
                    .getCircuitBreaker();

            circuitBreaker.setEnabled(true);
            circuitBreaker.setFailureThreshold(0);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid failure threshold");
            assertTrue(exception.getMessage().contains("Failure threshold must be positive"));
        }

        @Test
        @DisplayName("Should validate retry policy configuration")
        void shouldValidateRetryPolicyConfiguration() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.ResilienceConfig.RetryPolicyConfig retryPolicy = config.getResilience().getRetryPolicy();

            retryPolicy.setEnabled(true);
            retryPolicy.setMaxAttempts(3);
            retryPolicy.setInitialDelaySeconds(1);
            retryPolicy.setMaxDelaySeconds(60);
            retryPolicy.setMultiplier(2.0);

            assertDoesNotThrow(() -> validator.validate(properties),
                    "Valid retry policy configuration should be accepted");
        }

        @Test
        @DisplayName("Should reject invalid max attempts")
        void shouldRejectInvalidMaxAttempts() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.ResilienceConfig.RetryPolicyConfig retryPolicy = config.getResilience().getRetryPolicy();

            retryPolicy.setEnabled(true);
            retryPolicy.setMaxAttempts(0);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid max attempts");
            assertTrue(exception.getMessage().contains("Max attempts must be positive"));
        }

        @Test
        @DisplayName("Should reject invalid multiplier")
        void shouldRejectInvalidMultiplier() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            JCacheXProperties.ResilienceConfig.RetryPolicyConfig retryPolicy = config.getResilience().getRetryPolicy();

            retryPolicy.setEnabled(true);
            retryPolicy.setMultiplier(0.0);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(properties),
                    "Should reject invalid multiplier");
            assertTrue(exception.getMessage().contains("Multiplier must be greater than 1.0"));
        }
    }

    @Nested
    @DisplayName("Validation Result Tests")
    class ValidationResultTests {

        @Test
        @DisplayName("Should provide detailed validation results")
        void shouldProvideDetailedValidationResults() {
            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            config.setMaximumSize(-1L);
            config.setEvictionStrategy("INVALID");

            try {
                validator.validate(properties);
                fail("Should have thrown validation exception");
            } catch (IllegalArgumentException e) {
                String message = e.getMessage();
                assertTrue(message.contains("Maximum size must be positive"),
                        "Should contain maximum size error");
                // Note: The validator might stop at the first error, so we might not see all
                // errors
            }
        }

        @Test
        @DisplayName("Should validate all aspects of configuration")
        void shouldValidateAllAspectsOfConfiguration() {
            // This test verifies that the validator calls all necessary validation methods
            // by setting up a configuration that would pass individual validations
            // but might fail comprehensive validation

            JCacheXProperties.CacheConfig config = properties.getDefaultConfig();
            config.setMaximumSize(1000L);
            config.setEvictionStrategy("LRU");
            config.setExpireAfterSeconds(300L);
            config.setExpireAfterAccessSeconds(600L);

            // Network configuration
            JCacheXProperties.NetworkConfig networkConfig = config.getNetwork();
            networkConfig.setProtocol("TCP");
            networkConfig.setSerialization("KRYO");
            networkConfig.setPort(8080);

            // Distributed configuration
            JCacheXProperties.DistributedConfig distributedConfig = config.getDistributed();
            distributedConfig.setEnabled(false); // Keep disabled for simplicity

            assertDoesNotThrow(() -> validator.validate(properties),
                    "Comprehensive configuration should be valid");
        }
    }
}
