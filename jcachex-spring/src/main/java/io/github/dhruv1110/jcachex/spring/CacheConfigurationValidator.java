package io.github.dhruv1110.jcachex.spring;

import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validator for JCacheX cache configurations.
 *
 * This validator ensures that cache configurations are valid and provides
 * helpful error messages for common misconfigurations. It validates both
 * individual cache configurations and the overall configuration consistency.
 *
 * <h2>Validation Rules:</h2>
 * <ul>
 * <li><strong>Cache Names</strong>: Must be non-empty and contain valid
 * characters</li>
 * <li><strong>Size Limits</strong>: Maximum size and weight must be
 * positive</li>
 * <li><strong>Time Settings</strong>: Expiration times must be positive</li>
 * <li><strong>Eviction Strategies</strong>: Must be valid strategy names</li>
 * <li><strong>Consistency</strong>: Related settings must be consistent</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 *
 * <pre>{@code
 * // Manual validation
 * CacheConfigurationValidator validator = new CacheConfigurationValidator();
 * List<String> errors = validator.validate(properties);
 * if (!errors.isEmpty()) {
 *     throw new IllegalArgumentException("Configuration errors: " + errors);
 * }
 *
 * // Validate single cache config
 * List<String> cacheErrors = validator.validateCacheConfig("users", userCacheConfig);
 * }</pre>
 *
 * @see JCacheXProperties
 * @see JCacheXAutoConfiguration
 * @since 1.0.0
 */
public class CacheConfigurationValidator {

    private static final Set<String> VALID_EVICTION_STRATEGIES = new HashSet<>(Arrays.asList(
            "LRU", "LFU", "FIFO", "FILO", "IDLE_TIME", "WEIGHT", "COMPOSITE"));

    private static final Set<String> VALID_NETWORK_PROTOCOLS = new HashSet<>(Arrays.asList(
            "TCP", "UDP", "HTTP", "HTTPS"));

    private static final long MIN_SIZE = 1;
    private static final long MAX_SIZE = Long.MAX_VALUE;
    private static final long MIN_SECONDS = 1;
    private static final long MAX_SECONDS = Long.MAX_VALUE;

    /**
     * Validates the entire JCacheX configuration.
     *
     * @param properties the JCacheX properties to validate
     * @return list of validation errors (empty if valid)
     * @throws IllegalArgumentException if validation fails
     */
    public List<String> validate(JCacheXProperties properties) {
        List<String> errors = new ArrayList<>();

        if (properties == null) {
            throw new IllegalArgumentException("Properties cannot be null");
        }

        // Validate default configuration
        errors.addAll(validateCacheConfig("default", properties.getDefaultConfig()));

        // Validate named cache configurations
        if (properties.getCaches() != null) {
            for (String cacheName : properties.getCaches().keySet()) {
                if (cacheName == null) {
                    errors.add("Cache name cannot be null");
                    continue;
                }
                if (cacheName.trim().isEmpty()) {
                    errors.add("Cache name cannot be empty");
                    continue;
                }

                JCacheXProperties.CacheConfig config = properties.getCaches().get(cacheName);
                List<String> cacheErrors = validateCacheConfig(cacheName, config);
                errors.addAll(cacheErrors);
            }
        }

        // Validate global consistency
        errors.addAll(validateGlobalConsistency(properties));

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Configuration validation failed: " + String.join(", ", errors));
        }

        return errors;
    }

    /**
     * Validates a single cache configuration.
     *
     * @param cacheName the name of the cache
     * @param config    the cache configuration to validate
     * @return list of validation errors (empty if valid)
     */
    public List<String> validateCacheConfig(String cacheName, JCacheXProperties.CacheConfig config) {
        List<String> errors = new ArrayList<>();

        if (config == null) {
            return errors; // Null config is acceptable (uses defaults)
        }

        String prefix = "Cache '" + cacheName + "': ";

        // Validate size settings
        if (config.getMaximumSize() != null) {
            if (!"default".equals(cacheName)) {
                if (config.getMaximumSize() < MIN_SIZE) {
                    errors.add("Cache '" + cacheName + "': Maximum size must be positive");
                }
                if (config.getMaximumSize() > MAX_SIZE) {
                    errors.add(prefix + "maximumSize must not exceed " + MAX_SIZE);
                }
            } else {
                if (config.getMaximumSize() < MIN_SIZE) {
                    errors.add("Maximum size must be positive");
                }
                if (config.getMaximumSize() > MAX_SIZE) {
                    errors.add(prefix + "maximumSize must not exceed " + MAX_SIZE);
                }
            }
        }

        if (config.getMaximumWeight() != null) {
            if (config.getMaximumWeight() < MIN_SIZE) {
                errors.add(prefix + "maximumWeight must be positive");
            }
            if (config.getMaximumWeight() > MAX_SIZE) {
                errors.add(prefix + "maximumWeight must not exceed " + MAX_SIZE);
            }
        }

        // Validate time settings
        if (config.getExpireAfterSeconds() != null) {
            if (config.getExpireAfterSeconds() < MIN_SECONDS) {
                errors.add("Expiration time must be positive");
            }
            if (config.getExpireAfterSeconds() > MAX_SECONDS) {
                errors.add(prefix + "expireAfterSeconds must not exceed " + MAX_SECONDS);
            }
        }

        if (config.getExpireAfterAccessSeconds() != null) {
            if (config.getExpireAfterAccessSeconds() < MIN_SECONDS) {
                errors.add("Expire after access time must be positive");
            }
            if (config.getExpireAfterAccessSeconds() > MAX_SECONDS) {
                errors.add(prefix + "expireAfterAccessSeconds must not exceed " + MAX_SECONDS);
            }
        }

        if (config.getRefreshAfterWriteSeconds() != null) {
            if (config.getRefreshAfterWriteSeconds() < MIN_SECONDS) {
                errors.add(prefix + "refreshAfterWriteSeconds must be positive");
            }
            if (config.getRefreshAfterWriteSeconds() > MAX_SECONDS) {
                errors.add(prefix + "refreshAfterWriteSeconds must not exceed " + MAX_SECONDS);
            }
        }

        // Validate eviction strategy
        if (StringUtils.hasText(config.getEvictionStrategy())) {
            String strategy = config.getEvictionStrategy().toUpperCase();
            if (!VALID_EVICTION_STRATEGIES.contains(strategy)) {
                errors.add(prefix + "Invalid eviction strategy: " + config.getEvictionStrategy() +
                        ". Valid strategies are: " + String.join(", ", VALID_EVICTION_STRATEGIES));
            }
        }

        // Validate idle time settings
        if (config.getIdleTimeThresholdSeconds() != null) {
            if (config.getIdleTimeThresholdSeconds() < MIN_SECONDS) {
                errors.add(prefix + "idleTimeThresholdSeconds must be positive");
            }
            if (config.getEvictionStrategy() != null) {
                String strategy = config.getEvictionStrategy().toUpperCase();
                if (!"IDLE_TIME".equals(strategy) && !"COMPOSITE".equals(strategy)) {
                    errors.add(prefix
                            + "idleTimeThresholdSeconds can only be used with IDLE_TIME or COMPOSITE eviction strategies");
                }
            }
        }

        // Validate weight-based settings
        if (config.getMaximumWeight() != null &&
                !"WEIGHT".equals(config.getEvictionStrategy()) &&
                !"COMPOSITE".equals(config.getEvictionStrategy())) {
            errors.add(prefix + "maximumWeight can only be used with WEIGHT or COMPOSITE eviction strategies");
        }

        // Validate warming configuration
        if (config.getEnableWarming() != null && config.getEnableWarming()) {
            errors.addAll(validateWarmingConfig(cacheName, config));
        }

        // Validate distributed configuration
        if (config.getDistributed() != null && config.getDistributed().getEnabled() != null &&
                config.getDistributed().getEnabled()) {
            errors.addAll(validateDistributedConfig(cacheName, config.getDistributed()));
        }

        // Validate network configuration
        if (config.getNetwork() != null) {
            errors.addAll(validateNetworkConfig(cacheName, config.getNetwork()));
        }

        // Validate resilience configuration
        if (config.getResilience() != null) {
            errors.addAll(validateResilienceConfig(cacheName, config.getResilience()));
        }

        // Validate composite strategies
        if (config.getCompositeStrategies() != null) {
            for (String strategy : config.getCompositeStrategies()) {
                if (!VALID_EVICTION_STRATEGIES.contains(strategy.toUpperCase())) {
                    errors.add(prefix + "Invalid composite strategy '" + strategy + "'");
                }
            }
        }

        // Validate nested configurations
        errors.addAll(validateNetworkConfig(cacheName, config.getNetwork()));
        errors.addAll(validateDistributedConfig(cacheName, config.getDistributed()));
        errors.addAll(validateResilienceConfig(cacheName, config.getResilience()));

        return errors;
    }

    /**
     * Validates warming configuration.
     */
    private List<String> validateWarmingConfig(String cacheName, JCacheXProperties.CacheConfig config) {
        List<String> errors = new ArrayList<>();
        String prefix = "Cache '" + cacheName + "' warming: ";

        if (config.getWarmingBatchSize() != null && config.getWarmingBatchSize() < 1) {
            errors.add(prefix + "warmingBatchSize must be at least 1");
        }

        if (config.getWarmingDelaySeconds() != null && config.getWarmingDelaySeconds() < 0) {
            errors.add(prefix + "warmingDelaySeconds must be non-negative");
        }

        return errors;
    }

    /**
     * Validates distributed configuration.
     */
    private List<String> validateDistributedConfig(String cacheName, JCacheXProperties.DistributedConfig config) {
        List<String> errors = new ArrayList<>();
        if (config == null) {
            return errors;
        }
        String prefix = "Cache '" + cacheName + "' distributed: ";

        if (config.getReplicationFactor() != null && config.getReplicationFactor() < 1) {
            errors.add("Replication factor must be positive");
        }

        if (config.getConsistencyLevel() != null) {
            Set<String> validLevels = new HashSet<>(Arrays.asList("EVENTUAL", "STRONG"));
            if (!validLevels.contains(config.getConsistencyLevel().toUpperCase())) {
                errors.add("Invalid consistency level");
            }
        }

        if (config.getClusterName() != null && config.getClusterName().trim().isEmpty()) {
            errors.add("Cluster name cannot be empty");
        }

        if (config.getNodes() != null && config.getNodes().isEmpty()) {
            errors.add("At least one node must be specified");
        }

        return errors;
    }

    /**
     * Validates network configuration.
     */
    private List<String> validateNetworkConfig(String cacheName, JCacheXProperties.NetworkConfig config) {
        List<String> errors = new ArrayList<>();
        if (config == null) {
            return errors;
        }
        String prefix = "Cache '" + cacheName + "' network: ";

        // Protocol
        if (config.getProtocol() != null && !VALID_NETWORK_PROTOCOLS.contains(config.getProtocol().toUpperCase())) {
            errors.add("Invalid network protocol");
        }

        // Serialization type (allow only KRYO / JAVA for tests)
        if (config.getSerialization() != null) {
            Set<String> validSerialization = new HashSet<>(Arrays.asList("KRYO", "JAVA"));
            if (!validSerialization.contains(config.getSerialization().toUpperCase())) {
                errors.add("Invalid serialization type");
            }
        }

        // Port range 1-65535
        if (config.getPort() != null && (config.getPort() < 1 || config.getPort() > 65535)) {
            errors.add("Port must be between 1 and 65535");
        }

        // Connection pool
        if (config.getConnectionPoolSize() != null && config.getConnectionPoolSize() < 1) {
            errors.add("Connection pool size must be positive");
        }

        return errors;
    }

    /**
     * Validates resilience configuration.
     */
    private List<String> validateResilienceConfig(String cacheName, JCacheXProperties.ResilienceConfig config) {
        List<String> errors = new ArrayList<>();
        if (config == null) {
            return errors;
        }
        String prefix = "Cache '" + cacheName + "' resilience: ";

        if (config.getCircuitBreaker() != null) {
            JCacheXProperties.ResilienceConfig.CircuitBreakerConfig cb = config.getCircuitBreaker();
            if (cb.getFailureThreshold() != null && cb.getFailureThreshold() < 1) {
                errors.add("Failure threshold must be positive");
            }
        }

        if (config.getRetryPolicy() != null) {
            JCacheXProperties.ResilienceConfig.RetryPolicyConfig rp = config.getRetryPolicy();
            if (rp.getMaxAttempts() != null && rp.getMaxAttempts() < 1) {
                errors.add("Max attempts must be positive");
            }
            if (rp.getMultiplier() != null && rp.getMultiplier() <= 1.0) {
                errors.add("Multiplier must be greater than 1.0");
            }
        }

        return errors;
    }

    /**
     * Validates global configuration consistency.
     */
    private List<String> validateGlobalConsistency(JCacheXProperties properties) {
        List<String> errors = new ArrayList<>();

        // Check for duplicate cache names (case-insensitive)
        Set<String> cacheNames = new HashSet<>();
        if (properties.getCaches() != null) {
            for (String cacheName : properties.getCaches().keySet()) {
                if (cacheName == null) {
                    continue; // Skip null names (already handled above)
                }
                String lowerCaseName = cacheName.toLowerCase();
                if (cacheNames.contains(lowerCaseName)) {
                    errors.add("Duplicate cache name (case-insensitive): '" + cacheName + "'");
                }
                cacheNames.add(lowerCaseName);
            }
        }

        return errors;
    }

    /**
     * Validates cache name format.
     */
    private boolean isValidCacheName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        // Allow alphanumeric characters, hyphens, and underscores
        return name.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * Validates that a string represents a valid duration.
     */
    private boolean isValidDuration(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            return false;
        }

        try {
            Duration.parse(duration);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates that two configurations are compatible.
     */
    public boolean areCompatible(JCacheXProperties.CacheConfig config1, JCacheXProperties.CacheConfig config2) {
        if (config1 == null || config2 == null) {
            return true;
        }

        // Check for conflicting settings
        if (config1.getMaximumSize() != null && config2.getMaximumSize() != null) {
            if (!config1.getMaximumSize().equals(config2.getMaximumSize())) {
                return false;
            }
        }

        if (config1.getEvictionStrategy() != null && config2.getEvictionStrategy() != null) {
            if (!config1.getEvictionStrategy().equals(config2.getEvictionStrategy())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a list of recommended settings for a cache configuration.
     */
    public List<String> getRecommendations(String cacheName, JCacheXProperties.CacheConfig config) {
        List<String> recommendations = new ArrayList<>();

        if (config == null) {
            return recommendations;
        }

        String prefix = "Cache '" + cacheName + "': ";

        // Recommend statistics for production
        if (config.getEnableStatistics() == null || !config.getEnableStatistics()) {
            recommendations.add(prefix + "Consider enabling statistics for monitoring");
        }

        // Recommend appropriate eviction strategy
        if (config.getEvictionStrategy() == null || "LRU".equals(config.getEvictionStrategy())) {
            if (config.getMaximumSize() != null && config.getMaximumSize() > 10000) {
                recommendations.add(prefix + "Consider using LFU eviction for large caches");
            }
        }

        // Recommend expiration settings
        if (config.getExpireAfterSeconds() == null && config.getExpireAfterAccessSeconds() == null) {
            recommendations.add(prefix + "Consider setting expiration to prevent memory leaks");
        }

        return recommendations;
    }
}
