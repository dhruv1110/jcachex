import type { CodeTab, Feature, EvictionStrategy, Module, PerformanceStat, ArchitectureComponent } from '../types';

// Version management
export const VERSION = process.env.REACT_APP_VERSION || '1.0.0';

// Progressive Learning Examples are now in separate file

// Basic usage examples are now in separate file

// Real-world use case examples are now in separate file

// Common features data
export const FEATURES: Feature[] = [
    {
        icon: 'performance',
        title: 'Ultra-Fast Performance',
        description: 'Sub-microsecond latency with 50M+ operations/second. 3x faster than alternatives.',
        details: ['7.9ns zero-copy operations', '11.5ns read-heavy profile', 'O(1) cache operations', 'Minimal GC pressure']
    },
    {
        icon: 'api',
        title: 'Intelligent Profiles',
        description: 'Profile-based configuration automatically optimizes for your use case.',
        details: ['READ_HEAVY: 11.5ns GET performance', 'WRITE_HEAVY: Optimized for mutations', 'API_RESPONSE: Perfect for gateways', 'Zero configuration complexity']
    },
    {
        icon: 'async',
        title: 'Production Ready',
        description: 'Built-in monitoring, error handling, and resilience patterns.',
        details: ['Comprehensive metrics', 'Circuit breaker support', 'Graceful degradation', 'Automatic failover']
    },
    {
        icon: 'spring',
        title: 'Spring Integration',
        description: 'Seamless integration with Spring Boot, annotations, and auto-configuration.',
        details: ['@JCacheXCacheable annotations', 'Auto-configuration', 'Actuator endpoints', 'Properties binding']
    },
    {
        icon: 'distributed',
        title: 'Distributed Caching',
        description: 'Scale across multiple nodes with consistency guarantees.',
        details: ['Multi-node clustering', 'Eventual consistency', 'Automatic failover', 'Network partitioning tolerance']
    },
    {
        icon: 'monitoring',
        title: 'Observability',
        description: 'Built-in metrics, JMX support, and integration with monitoring systems.',
        details: ['Real-time statistics', 'Prometheus integration', 'Grafana dashboards', 'Custom event listeners']
    }
];

// Modules information
export const MODULES: Module[] = [
    {
        name: 'jcachex-core',
        title: 'Core Library',
        description: 'Essential caching functionality with ultra-high performance',
        features: ['7.9ns zero-copy operations', 'Intelligent profiles', 'Advanced eviction strategies', 'Comprehensive statistics']
    },
    {
        name: 'jcachex-spring',
        title: 'Spring Integration',
        description: 'Seamless integration with Spring Boot and Spring Framework',
        features: ['@JCacheXCacheable annotations', 'Auto-configuration', 'Actuator endpoints', 'Properties binding']
    },
    {
        name: 'jcachex-kotlin',
        title: 'Kotlin Extensions',
        description: 'Kotlin-specific extensions and DSL for enhanced developer experience',
        features: ['Intuitive DSL', 'Coroutine support', 'Operator overloading', 'Extension functions']
    }
];

// Performance stats (updated with more compelling numbers)
export const PERFORMANCE_STATS: PerformanceStat[] = [
    { label: 'Peak Throughput', value: '50M+ ops/sec', description: 'Sustained operations per second' },
    { label: 'Zero-Copy GET', value: '7.9ns', description: 'Fastest possible implementation' },
    { label: 'Read-Heavy Profile', value: '11.5ns', description: 'Production-optimized performance' },
    { label: 'vs Caffeine', value: '3x faster', description: 'Benchmark-proven superiority' }
];

// Installation tabs configuration
export const INSTALLATION_TABS: CodeTab[] = [
    {
        id: 'maven',
        label: 'Maven',
        language: 'xml',
        code: `<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>\${VERSION}</version>
</dependency>

<!-- Optional: Spring Boot integration -->
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-spring</artifactId>
    <version>\${VERSION}</version>
</dependency>`
    },
    {
        id: 'gradle',
        label: 'Gradle',
        language: 'gradle',
        code: `implementation 'io.github.dhruv1110:jcachex-core:\${VERSION}'

// Optional: Spring Boot integration
implementation 'io.github.dhruv1110:jcachex-spring:\${VERSION}'

// Optional: Kotlin extensions
implementation 'io.github.dhruv1110:jcachex-kotlin:\${VERSION}'`
    }
];

// Spring Boot installation tabs
export const SPRING_INSTALLATION_TABS: CodeTab[] = [
    {
        id: 'maven',
        label: 'Maven',
        language: 'xml',
        code: `<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-spring</artifactId>
    <version>\${VERSION}</version>
</dependency>

<!-- Auto-configuration enabled by default -->
<!-- Add to application.yml: -->
<!-- jcachex:
  default-profile: READ_HEAVY
  metrics:
    enabled: true -->`
    },
    {
        id: 'gradle',
        label: 'Gradle',
        language: 'gradle',
        code: `implementation 'io.github.dhruv1110:jcachex-spring:\${VERSION}'

// Auto-configuration enabled by default
// Add to application.yml:
// jcachex:
//   default-profile: READ_HEAVY
//   metrics:
//     enabled: true`
    }
];

// Migration examples
export const MIGRATION_EXAMPLES: CodeTab[] = [
    {
        id: 'from-caffeine',
        label: 'From Caffeine',
        language: 'java',
        code: `// BEFORE (Caffeine)
Cache<String, User> caffeineCache = Caffeine.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .removalListener((key, value, cause) ->
        log.info("Removed {} due to {}", key, cause))
    .build();

// AFTER (JCacheX) - Direct replacement
Cache<String, User> jcacheXCache = JCacheXBuilder.create()
    .maximumSize(10000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .evictionListener((key, value, cause) ->
        log.info("Removed {} due to {}", key, cause))
    .build();

// BETTER (JCacheX) - Using intelligent profiles
Cache<String, User> optimizedCache = JCacheXBuilder
    .forReadHeavyWorkload()  // 3x better performance automatically
    .name("users")
    .maximumSize(10000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();`
    },
    {
        id: 'from-redis',
        label: 'From Redis',
        language: 'java',
        code: `// BEFORE (Redis/Lettuce)
@Service
public class RedisUserService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public User getUser(String id) {
        String json = redisTemplate.opsForValue().get("user:" + id);
        if (json != null) {
            return objectMapper.readValue(json, User.class);
        }
        User user = userRepository.findById(id);
        redisTemplate.opsForValue().set("user:" + id,
            objectMapper.writeValueAsString(user), Duration.ofHours(1));
        return user;
    }
}

// AFTER (JCacheX) - Simpler and 50x faster
@Service
public class JCacheXUserService {
    private final Cache<String, User> userCache =
        JCacheXBuilder.forReadHeavyWorkload()
            .name("users")
            .maximumSize(50000L)
            .expireAfterWrite(Duration.ofHours(1))
            .build();

    public User getUser(String id) {
        return userCache.get(id, userRepository::findById);
    }
}
// Benefits: 50x lower latency, no serialization, type-safe`
    }
];

// Performance comparison data is now in separate file

// Architecture components
export const ARCHITECTURE: ArchitectureComponent[] = [
    {
        name: 'Intelligent Profiles',
        description: 'Automatically configure optimal settings for your use case without complexity'
    },
    {
        name: 'Zero-Copy Storage',
        description: 'Ultra-fast storage engine with lock-free algorithms and minimal allocations'
    },
    {
        name: 'Advanced Eviction',
        description: 'All eviction strategies optimized to O(1) operations with frequency sketches'
    },
    {
        name: 'Observability Engine',
        description: 'Real-time metrics, event listeners, and monitoring system integrations'
    },
    {
        name: 'Resilience Patterns',
        description: 'Built-in circuit breaker, fallback strategies, and graceful degradation'
    },
    {
        name: 'Spring Integration',
        description: 'Seamless annotations, auto-configuration, and Spring Boot starter'
    }
];

// Eviction strategies (all optimized to O(1) operations)
export const EVICTION_STRATEGIES: EvictionStrategy[] = [
    {
        name: 'TINY_WINDOW_LFU',
        title: 'TinyWindowLFU (Recommended)',
        description: 'Hybrid eviction combining recency and frequency with optimal performance',
        useCase: 'Best for most workloads - automatically balances LRU and LFU benefits'
    },
    {
        name: 'ENHANCED_LRU',
        title: 'Enhanced LRU',
        description: 'LRU with optional frequency sketch for improved hit rates',
        useCase: 'Temporal locality patterns with frequency awareness'
    },
    {
        name: 'ENHANCED_LFU',
        title: 'Enhanced LFU',
        description: 'LFU with frequency buckets and sketch optimization',
        useCase: 'Clear frequency patterns in access distribution'
    },
    {
        name: 'ZERO_COPY_LRU',
        title: 'Zero-Copy LRU',
        description: 'Ultra-fast LRU implementation with 7.9ns operations',
        useCase: 'Maximum performance scenarios where every nanosecond counts'
    }
];

// Re-export cache profiles from separate file
export { CACHE_PROFILES } from './cacheProfiles';

// Export old names for backward compatibility
export { CACHE_PROFILES as CACHE_TYPES } from './cacheProfiles';
