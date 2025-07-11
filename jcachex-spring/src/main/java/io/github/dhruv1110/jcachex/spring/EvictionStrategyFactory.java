package io.github.dhruv1110.jcachex.spring;

import io.github.dhruv1110.jcachex.FrequencySketchType;
import io.github.dhruv1110.jcachex.eviction.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Factory for creating eviction strategies from string configuration values.
 *
 * This factory provides a bridge between Spring's string-based configuration
 * and JCacheX's programmatic eviction strategy configuration. It supports
 * all built-in eviction strategies and can be extended for custom strategies.
 *
 * <h2>Supported Strategies:</h2>
 * <ul>
 * <li><strong>LRU</strong>: Least Recently Used (default)</li>
 * <li><strong>LFU</strong>: Least Frequently Used</li>
 * <li><strong>FIFO</strong>: First In, First Out</li>
 * <li><strong>FILO</strong>: First In, Last Out</li>
 * <li><strong>IDLE_TIME</strong>: Based on idle time</li>
 * <li><strong>WEIGHT</strong>: Based on entry weight</li>
 * <li><strong>COMPOSITE</strong>: Combines multiple strategies</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 *
 * <h3>In Configuration:</h3>
 *
 * <pre>{@code
 * jcachex:
 *   caches:
 *     users:
 *       evictionStrategy: LRU
 *     sessions:
 *       evictionStrategy: LFU
 *     files:
 *       evictionStrategy: WEIGHT
 *       maximumWeight: 10485760  # 10MB
 * }</pre>
 *
 * <h3>Programmatic Usage:</h3>
 *
 * <pre>{@code
 * EvictionStrategyFactory factory = new EvictionStrategyFactory();
 * EvictionStrategy<String, User> lruStrategy = factory.createStrategy("LRU");
 * EvictionStrategy<String, byte[]> weightStrategy = factory.createStrategy("WEIGHT");
 * }</pre>
 *
 * @since 1.0.0
 */
public class EvictionStrategyFactory {

    private final Map<String, StrategyProvider> strategyProviders = new HashMap<>();

    public EvictionStrategyFactory() {
        registerBuiltInStrategies();
    }

    /**
     * Creates an eviction strategy from a string configuration.
     *
     * @param strategyName the name of the eviction strategy (case-insensitive)
     * @param config       optional cache configuration for strategy customization
     * @return the created eviction strategy
     * @throws IllegalArgumentException if the strategy name is not recognized
     */
    public <K, V> EvictionStrategy<K, V> createStrategy(
            String strategyName,
            JCacheXProperties.CacheConfig config) {
        if (strategyName == null) {
            throw new IllegalArgumentException("Unknown eviction strategy: null");
        }
        String normalizedName = strategyName.toUpperCase();
        StrategyProvider provider = strategyProviders.get(normalizedName);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown eviction strategy: " + strategyName);
        }
        return provider.create(config);
    }

    /**
     * Creates an eviction strategy from a string configuration using default
     * config.
     *
     * @param strategyName the name of the eviction strategy (case-insensitive)
     * @return the created eviction strategy
     * @throws IllegalArgumentException if the strategy name is not recognized
     */
    public <K, V> EvictionStrategy<K, V> createStrategy(String strategyName) {
        return createStrategy(strategyName, new JCacheXProperties.CacheConfig());
    }

    /**
     * Registers a custom eviction strategy.
     *
     * @param name     the strategy name (case-insensitive)
     * @param provider the strategy provider function
     */
    public void registerStrategy(String name, StrategyProvider provider) {
        strategyProviders.put(name.toUpperCase(), provider);
    }

    /**
     * Gets all registered strategy names.
     *
     * @return set of strategy names
     */
    public Set<String> getAvailableStrategies() {
        return strategyProviders.keySet();
    }

    /**
     * Registers all built-in eviction strategies.
     */
    private void registerBuiltInStrategies() {
        strategyProviders.put("LRU", new StrategyProvider() {
            @Override
            public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                return new LRUEvictionStrategy<>();
            }
        });
        strategyProviders.put("ENHANCED_LRU", new StrategyProvider() {
            @Override
            public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                FrequencySketchType sketchType = parseFrequencySketchType(config.getFrequencySketchType());
                long capacity = config.getMaximumSize() != null ? config.getMaximumSize() : 1000L;
                return new EnhancedLRUEvictionStrategy<>(sketchType, capacity);
            }
        });
        strategyProviders.put("LFU", new StrategyProvider() {
            @Override
            public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                return new LFUEvictionStrategy<>();
            }
        });
        strategyProviders.put("ENHANCED_LFU", new StrategyProvider() {
            @Override
            public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                FrequencySketchType sketchType = parseFrequencySketchType(config.getFrequencySketchType());
                long capacity = config.getMaximumSize() != null ? config.getMaximumSize() : 1000L;
                return new EnhancedLFUEvictionStrategy<>(sketchType, capacity);
            }
        });
        strategyProviders.put("TINY_WINDOW_LFU", new StrategyProvider() {
            @Override
            public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                long capacity = config.getMaximumSize() != null ? config.getMaximumSize() : 1000L;
                return new WindowTinyLFUEvictionStrategy<>(capacity);
            }
        });
        strategyProviders.put("FIFO", new StrategyProvider() {
            @Override
            public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                return new FIFOEvictionStrategy<>();
            }
        });
        strategyProviders.put("FILO", new StrategyProvider() {
            @Override
            public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                return new FILOEvictionStrategy<>();
            }
        });
        strategyProviders.put("IDLE_TIME", new StrategyProvider() {
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                Long threshold = config.getIdleTimeThresholdSeconds();
                java.time.Duration duration = java.time.Duration.ofSeconds(threshold != null ? threshold : 3600);
                return (EvictionStrategy<K, V>) new IdleTimeEvictionStrategy(duration);
            }
        });
        strategyProviders.put("WEIGHT", new StrategyProvider() {
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                Long maxWeight = config.getMaximumWeight();
                return (EvictionStrategy<K, V>) new WeightBasedEvictionStrategy(
                        maxWeight != null ? maxWeight : 1000000L);
            }
        });
        strategyProviders.put("COMPOSITE", new StrategyProvider() {
            @Override
            public <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config) {
                return createCompositeStrategy(config);
            }
        });
    }

    /**
     * Creates a composite eviction strategy from configuration.
     *
     * @param config the cache configuration
     * @return the composite eviction strategy
     */
    @SuppressWarnings("unchecked")
    private <K, V> CompositeEvictionStrategy<K, V> createCompositeStrategy(
            JCacheXProperties.CacheConfig config) {
        java.util.List<EvictionStrategy<K, V>> strategies = new java.util.ArrayList<>();

        // Add strategies based on configuration
        if (config.getCompositeStrategies() != null) {
            for (String strategyName : config.getCompositeStrategies()) {
                EvictionStrategy<K, V> strategy = createStrategy(strategyName, config);
                strategies.add(strategy);
            }
        }

        // If no composite strategies specified, use LRU as default
        if (strategies.isEmpty()) {
            strategies.add(new LRUEvictionStrategy<>());
        }

        return new CompositeEvictionStrategy<>(strategies);
    }

    /**
     * Parses frequency sketch type from string configuration.
     *
     * @param sketchTypeStr the frequency sketch type string
     * @return the parsed FrequencySketchType
     */
    private FrequencySketchType parseFrequencySketchType(String sketchTypeStr) {
        if (sketchTypeStr == null) {
            return FrequencySketchType.BASIC;
        }

        try {
            return FrequencySketchType.valueOf(sketchTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Default to BASIC if unknown type
            return FrequencySketchType.BASIC;
        }
    }

    /**
     * Functional interface for creating eviction strategies.
     */
    @FunctionalInterface
    public interface StrategyProvider {
        <K, V> EvictionStrategy<K, V> create(JCacheXProperties.CacheConfig config);
    }
}
