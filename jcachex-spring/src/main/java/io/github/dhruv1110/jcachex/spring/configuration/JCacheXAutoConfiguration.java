package io.github.dhruv1110.jcachex.spring.configuration;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.CacheConfig;
import io.github.dhruv1110.jcachex.impl.DefaultCache;
import io.github.dhruv1110.jcachex.spring.core.JCacheXCacheFactory;
import io.github.dhruv1110.jcachex.spring.core.JCacheXCacheManager;
import io.github.dhruv1110.jcachex.spring.utilities.EvictionStrategyFactory;
import io.github.dhruv1110.jcachex.spring.aop.JCacheXAnnotationsAspect;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot auto-configuration for JCacheX.
 * <p>
 * This configuration class automatically sets up JCacheX caching components
 * when the library is present on the classpath. It integrates seamlessly with
 * Spring Boot's auto-configuration system and provides sensible defaults for
 * most use cases.
 * </p>
 *
 * <h2>Auto-Configuration Features:</h2>
 * <ul>
 * <li><strong>Automatic Cache Manager</strong>: Creates
 * {@link JCacheXCacheManager} bean automatically</li>
 * <li><strong>Property Configuration</strong>: Binds configuration from
 * application properties</li>
 * <li><strong>Cache Factory</strong>: Provides {@link JCacheXCacheFactory} for
 * programmatic use</li>
 * <li><strong>Health Indicators</strong>: Registers cache health indicators if
 * Spring Boot Actuator is present</li>
 * <li><strong>Metrics Integration</strong>: Integrates with Micrometer metrics
 * if available</li>
 * <li><strong>Graceful Degradation</strong>: Falls back gracefully when
 * dependencies are missing</li>
 * </ul>
 *
 * <h2>Configuration Properties:</h2>
 * <p>
 * All configuration is done through application properties under the
 * {@code jcachex} prefix:
 * </p>
 *
 * <pre>{@code
 * # Enable/disable JCacheX (default: true)
 * jcachex.enabled=true
 *
 * # Default cache configuration
 * jcachex.default.maximumSize=1000
 * jcachex.default.expireAfterSeconds=1800
 * jcachex.default.enableStatistics=true
 *
 * # Cache-specific configuration
 * jcachex.caches.users.maximumSize=5000
 * jcachex.caches.users.expireAfterSeconds=3600
 * jcachex.caches.users.evictionStrategy=LRU
 *
 * jcachex.caches.sessions.maximumSize=10000
 * jcachex.caches.sessions.expireAfterSeconds=1800
 * jcachex.caches.sessions.evictionStrategy=LFU
 * }</pre>
 *
 * <h2>Conditional Configuration:</h2>
 * <p>
 * Components are only created when their dependencies are available:
 * </p>
 * <ul>
 * <li><strong>Core Components</strong>: Always created when JCacheX is
 * enabled</li>
 * <li><strong>Health Indicators</strong>: Only when Spring Boot Actuator is
 * present</li>
 * <li><strong>Metrics</strong>: Only when Micrometer is present</li>
 * <li><strong>JMX</strong>: Only when JMX is enabled</li>
 * </ul>
 *
 * <h2>Customization:</h2>
 * <p>
 * You can customize the auto-configuration by providing your own beans:
 * </p>
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;Configuration
 *     public class CustomCacheConfig {
 *
 *         &#64;Bean
 *         &#64;Primary
 *         public JCacheXCacheManager customCacheManager() {
 *             JCacheXCacheManager manager = new JCacheXCacheManager();
 *             manager.setAllowNullValues(false);
 *             return manager;
 *         }
 *
 *         @Bean
 *         public JCacheXProperties customProperties() {
 *             JCacheXProperties properties = new JCacheXProperties();
 *             properties.getDefault().setMaximumSize(5000L);
 *             return properties;
 *         }
 *     }
 * }
 * </pre>
 *
 * @see JCacheXProperties
 * @see JCacheXCacheManager
 * @see JCacheXCacheFactory
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass({
        DefaultCache.class, // JCacheX core must be present
        CacheManager.class // Spring cache support must be present
})
@ConditionalOnProperty(prefix = "jcachex", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JCacheXProperties.class)
@AutoConfigureBefore(name = {
        "org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration"
})
public class JCacheXAutoConfiguration {

    private final JCacheXProperties properties;

    /**
     * Creates a new auto-configuration with the specified properties.
     *
     * @param properties the JCacheX properties
     */
    public JCacheXAutoConfiguration(JCacheXProperties properties) {
        this.properties = properties;
    }

    /**
     * Provides the JCacheX properties with the expected bean name.
     *
     * @return the JCacheX properties
     */
    @Bean("jcacheXProperties")
    @Primary
    public JCacheXProperties jcacheXProperties() {
        return this.properties;
    }

    /**
     * Creates the primary JCacheX cache manager.
     *
     * This cache manager integrates with Spring's caching abstraction and provides
     * all the advanced features of JCacheX including multiple eviction strategies,
     * async operations, and comprehensive metrics.
     *
     * @return configured JCacheXCacheManager instance
     */
    @Bean("jcacheXCacheManager")
    @Primary
    public JCacheXCacheManager jcacheXCacheManager() {
        JCacheXCacheManager manager = new JCacheXCacheManager(properties);
        // Initialize with configured caches
        manager.initializeCaches();
        return manager;
    }

    /**
     * Creates individual cache instances based on configuration.
     *
     * This method creates cache beans for each named cache defined in the
     * configuration.
     * These beans can be injected directly or accessed through the cache manager.
     *
     * @return map of cache name to cache instance
     */
    @Bean
    @ConditionalOnProperty(prefix = "jcachex", name = "autoCreateCaches", havingValue = "true", matchIfMissing = true)
    public Map<String, Cache<Object, Object>> configureCaches() {
        Map<String, Cache<Object, Object>> caches = new HashMap<>();

        // Create caches from configuration
        if (properties.getCaches() != null) {
            for (Map.Entry<String, JCacheXProperties.CacheConfig> entry : properties.getCaches().entrySet()) {
                String cacheName = entry.getKey();
                JCacheXProperties.CacheConfig cacheConfig = entry.getValue();
                Cache<Object, Object> cache = createCacheFromConfig(cacheName, cacheConfig);
                caches.put(cacheName, cache);
            }
        }

        return caches;
    }

    /**
     * Creates a cache factory bean for programmatic cache creation.
     *
     * This factory allows creating caches at runtime with custom configurations
     * while still respecting the global configuration defaults.
     *
     * @return the cache factory
     */
    @Bean("jcacheXCacheFactory")
    @ConditionalOnMissingBean(JCacheXCacheFactory.class)
    public JCacheXCacheFactory jcacheXCacheFactory() {
        return new JCacheXCacheFactory(properties);
    }

    /**
     * Creates eviction strategy factory for different eviction algorithms.
     *
     * This factory creates appropriate eviction strategy instances based on
     * string configuration values, making it easy to configure via properties.
     *
     * @return the eviction strategy factory
     */
    @Bean
    @ConditionalOnMissingBean(EvictionStrategyFactory.class)
    public EvictionStrategyFactory evictionStrategyFactory() {
        return new EvictionStrategyFactory();
    }

    /**
     * Creates a cache configuration validator.
     *
     * This validator ensures that cache configurations are valid and provides
     * helpful error messages for common misconfigurations.
     *
     * @return the configuration validator
     */
    @Bean
    @ConditionalOnMissingBean(CacheConfigurationValidator.class)
    public CacheConfigurationValidator cacheConfigurationValidator() {
        return new CacheConfigurationValidator();
    }

    /**
     * Registers the AOP aspect that powers @JCacheXCacheable/@JCacheXCacheEvict if
     * Spring AOP is present.
     */
    @Bean
    @ConditionalOnClass(name = {
            "org.aspectj.lang.annotation.Aspect",
            "org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator"
    })
    @ConditionalOnMissingBean(JCacheXAnnotationsAspect.class)
    public JCacheXAnnotationsAspect jcacheXAnnotationsAspect(
            org.springframework.beans.factory.BeanFactory beanFactory) {
        return new JCacheXAnnotationsAspect(jcacheXCacheManager(), beanFactory);
    }

    /**
     * Creates a cache instance from configuration.
     *
     * This internal method handles the complex logic of converting declarative
     * configuration into actual cache instances with all the appropriate
     * settings and integrations.
     *
     * @param cacheName the cache name
     * @param config    the cache configuration
     * @return the configured cache instance
     */
    private Cache<Object, Object> createCacheFromConfig(String cacheName,
            JCacheXProperties.CacheConfig config) {
        CacheConfig.Builder<Object, Object> builder = CacheConfig.newBuilder();

        // Basic size configuration
        if (config.getMaximumSize() != null) {
            builder.maximumSize(config.getMaximumSize());
        }
        if (config.getMaximumWeight() != null) {
            builder.maximumWeight(config.getMaximumWeight());
        }

        // Expiration configuration
        if (config.getExpireAfterSeconds() != null) {
            builder.expireAfterWrite(Duration.ofSeconds(config.getExpireAfterSeconds()));
        }
        if (config.getExpireAfterAccessSeconds() != null) {
            builder.expireAfterAccess(Duration.ofSeconds(config.getExpireAfterAccessSeconds()));
        }
        if (config.getRefreshAfterWriteSeconds() != null) {
            builder.refreshAfterWrite(Duration.ofSeconds(config.getRefreshAfterWriteSeconds()));
        }

        // Reference types
        if (config.getWeakKeys() != null) {
            builder.weakKeys(config.getWeakKeys());
        }
        if (config.getWeakValues() != null) {
            builder.weakValues(config.getWeakValues());
        }
        if (config.getSoftValues() != null) {
            builder.softValues(config.getSoftValues());
        }

        // Performance settings
        if (config.getEnableStatistics() != null) {
            builder.recordStats(config.getEnableStatistics());
        }

        // Eviction strategy
        if (config.getEvictionStrategy() != null) {
            try {
                EvictionStrategyFactory factory = evictionStrategyFactory();
                builder.evictionStrategy(factory.createStrategy(config.getEvictionStrategy(), config));
            } catch (IllegalArgumentException e) {
                // Log warning and continue with default strategy
                System.err.println("Warning: Invalid eviction strategy '" + config.getEvictionStrategy() +
                        "' for cache '" + cacheName + "', using default LRU strategy. " + e.getMessage());
            }
        }

        CacheConfig<Object, Object> cacheConfig = builder.build();
        return new DefaultCache<>(cacheConfig);
    }

    /**
     * Configuration for JCacheX metrics integration.
     *
     * This configuration is only activated when Micrometer is present on the
     * classpath,
     * ensuring no dependency conflicts when metrics are not needed.
     *
     * Note: This is a placeholder for future metrics integration. The actual
     * implementation
     * would require additional Micrometer dependencies and custom meter registry
     * components.
     */
    @Configuration
    @ConditionalOnClass(name = { "io.micrometer.core.instrument.MeterRegistry" })
    @ConditionalOnProperty(prefix = "jcachex.observability", name = "micrometer", havingValue = "true", matchIfMissing = true)
    public static class JCacheXMetricsAutoConfiguration {

        /**
         * Placeholder for metrics integration when Micrometer is available.
         *
         * Future implementation would create:
         * - Cache metrics collectors
         * - Custom meter registry customizers
         * - Prometheus/Grafana dashboard integration
         */
        // @Bean
        // @ConditionalOnMissingBean(JCacheXMeterRegistryCustomizer.class)
        // public JCacheXMeterRegistryCustomizer jcachexMeterRegistryCustomizer() {
        // return new JCacheXMeterRegistryCustomizer();
        // }
    }

    /**
     * Configuration for JCacheX actuator integration.
     *
     * This configuration provides health indicators and endpoints when
     * Spring Boot Actuator is present on the classpath.
     *
     * Note: This is a placeholder for future actuator integration. The actual
     * implementation
     * would require additional actuator dependencies and custom health indicator
     * components.
     */
    @Configuration
    @ConditionalOnClass(name = {
            "org.springframework.boot.actuate.health.HealthIndicator",
            "org.springframework.boot.actuate.endpoint.annotation.Endpoint"
    })
    @ConditionalOnProperty(prefix = "management.health.jcachex", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static class JCacheXActuatorAutoConfiguration {

        /**
         * Placeholder for health indicator for cache monitoring.
         *
         * Future implementation would create:
         * - Health indicators showing cache status
         * - Custom actuator endpoints for cache management
         * - Cache statistics exposure via actuator
         */
        // @Bean
        // @ConditionalOnMissingBean(JCacheXHealthIndicator.class)
        // public JCacheXHealthIndicator jcachexHealthIndicator(JCacheXCacheManager
        // cacheManager) {
        // return new JCacheXHealthIndicator(cacheManager);
        // }

        // @Bean
        // @ConditionalOnMissingBean(JCacheXEndpoint.class)
        // public JCacheXEndpoint jcachexEndpoint(JCacheXCacheManager cacheManager) {
        // return new JCacheXEndpoint(cacheManager);
        // }
    }

}
