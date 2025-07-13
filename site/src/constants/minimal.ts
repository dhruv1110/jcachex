import type { CodeTab, Feature, EvictionStrategy, Module, PerformanceStat, ArchitectureComponent } from '@/types';

// Version management
export const VERSION = process.env.REACT_APP_VERSION || '1.0.0';

// Common features data
export const FEATURES: Feature[] = [
    {
        icon: 'performance',
        title: 'High Performance',
        description: 'Sub-microsecond latency with 50M+ operations/second throughput.',
        details: ['Sub-microsecond latency operations', 'O(1) cache operations', 'Minimal GC pressure']
    },
    {
        icon: 'api',
        title: 'Simple API',
        description: 'Profile-based configuration makes complex caching decisions simple.',
        details: ['Profile-based configuration', 'Type-safe operations', 'Comprehensive documentation']
    },
    {
        icon: 'async',
        title: 'Async Support',
        description: 'Built-in asynchronous operations with CompletableFuture and Kotlin coroutines.',
        details: ['CompletableFuture integration', 'Kotlin coroutines', 'Non-blocking operations']
    },
    {
        icon: 'spring',
        title: 'Spring Integration',
        description: 'Seamless integration with Spring Boot, annotations, and auto-configuration.',
        details: ['@Cacheable support', 'Auto-configuration', 'Properties binding']
    },
    {
        icon: 'distributed',
        title: 'Distributed Caching',
        description: 'Scale across multiple nodes with consistency guarantees and automatic failover.',
        details: ['Multi-node clustering', 'Consistency models', 'Automatic failover']
    },
    {
        icon: 'monitoring',
        title: 'Monitoring',
        description: 'Built-in metrics, statistics, and observability for production environments.',
        details: ['Hit/miss ratios', 'Performance metrics', 'JMX support']
    }
];

// Modules information
export const MODULES: Module[] = [
    {
        name: 'jcachex-core',
        title: 'Core Library',
        description: 'Essential caching functionality with high performance and advanced features',
        features: ['Cache operations', 'Eviction strategies', 'Statistics', 'Event handling']
    },
    {
        name: 'jcachex-spring',
        title: 'Spring Integration',
        description: 'Seamless integration with Spring Boot and Spring Framework',
        features: ['Annotations', 'Auto-configuration', 'Properties binding', 'Actuator support']
    },
    {
        name: 'jcachex-kotlin',
        title: 'Kotlin Extensions',
        description: 'Kotlin-specific extensions and DSL for enhanced developer experience',
        features: ['DSL support', 'Coroutines', 'Extension functions', 'Operator overloading']
    }
];

// Performance stats (based on comprehensive benchmarks)
export const PERFORMANCE_STATS: PerformanceStat[] = [
    { label: 'Throughput', value: '50M+ ops/sec', description: 'Operations per second' },
    { label: 'Fastest GET', value: '7.9ns', description: 'Zero-copy implementation' },
    { label: 'Balanced Performance', value: '24.6ns GET', description: 'High-performance profile' },
    { label: 'Eviction', value: 'O(1)', description: 'All eviction strategies optimized' }
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
</dependency>`
    },
    {
        id: 'gradle',
        label: 'Gradle',
        language: 'gradle',
        code: `implementation 'io.github.dhruv1110:jcachex-core:\${VERSION}'`
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
</dependency>`
    },
    {
        id: 'gradle',
        label: 'Gradle',
        language: 'gradle',
        code: `implementation 'io.github.dhruv1110:jcachex-spring:\${VERSION}'`
    }
];

// Basic usage examples
export const BASIC_USAGE_JAVA = `import io.github.dhruv1110.jcachex.*;

// Profile-based approach - automatically optimized
Cache<String, User> cache = JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY)
    .name("users")
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();

// Basic operations
cache.put("user123", new User("Alice", "alice@example.com"));
User user = cache.get("user123");
System.out.println("User: " + user.getName());`;

export const BASIC_USAGE_KOTLIN = `import io.github.dhruv1110.jcachex.kotlin.*

// Profile-based approach with Kotlin DSL
val cache = createCacheWithProfile<String, User>(ProfileName.WRITE_HEAVY) {
    name("users")
    maximumSize(1000)
    expireAfterWrite(30.minutes)
}

// Basic operations with operator overloading
cache["user123"] = User("Alice", "alice@example.com")
val user = cache["user123"]
println("User: \${user?.name}")`;

export const SPRING_USAGE = `@Service
public class UserService {

    @JCacheXCacheable(value = "users", profile = "READ_HEAVY")
    public User findUserById(String id) {
        return userRepository.findById(id);
    }

    @JCacheXCacheEvict(value = "users")
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}`;

// Architecture components
export const ARCHITECTURE: ArchitectureComponent[] = [
    {
        name: 'Cache Interface',
        description: 'Type-safe generic interface for all cache operations'
    },
    {
        name: 'Profile System',
        description: 'Intelligent profiles that automatically configure optimal cache settings'
    },
    {
        name: 'Eviction Strategies',
        description: 'Pluggable eviction algorithms (LRU, LFU, FIFO, etc.)'
    },
    {
        name: 'Storage Engine',
        description: 'High-performance concurrent storage with lock-free algorithms'
    },
    {
        name: 'Statistics & Monitoring',
        description: 'Real-time metrics, JMX support, and observability'
    },
    {
        name: 'Spring Integration',
        description: 'Annotations, auto-configuration, and Spring Boot starter'
    },
    {
        name: 'Distributed Support',
        description: 'Multi-node clustering with consistency guarantees'
    }
];

// Eviction strategies (all optimized to O(1) operations)
export const EVICTION_STRATEGIES: EvictionStrategy[] = [
    {
        name: 'TINY_WINDOW_LFU',
        title: 'TinyWindowLFU (Default)',
        description: 'High-performance hybrid eviction combining recency and frequency with O(1) operations',
        useCase: 'Optimal for most workloads - combines benefits of LRU and LFU'
    },
    {
        name: 'ENHANCED_LRU',
        title: 'Enhanced LRU',
        description: 'LRU with optional frequency sketch for improved accuracy, O(1) operations',
        useCase: 'Temporal locality with optional frequency awareness'
    },
    {
        name: 'ENHANCED_LFU',
        title: 'Enhanced LFU',
        description: 'LFU with frequency buckets and optional sketch, O(1) operations',
        useCase: 'Workloads with clear frequency patterns'
    },
    {
        name: 'LRU',
        title: 'Least Recently Used',
        description: 'Evicts the least recently accessed items first, O(1) operations',
        useCase: 'General-purpose caching with temporal locality'
    },
    {
        name: 'LFU',
        title: 'Least Frequently Used',
        description: 'Evicts items with the lowest access frequency, O(1) operations',
        useCase: 'Workloads with frequency-based access patterns'
    },
    {
        name: 'FIFO',
        title: 'First In, First Out',
        description: 'Evicts the oldest items first, O(1) operations',
        useCase: 'Simple eviction with predictable behavior'
    },
    {
        name: 'FILO',
        title: 'First In, Last Out',
        description: 'Evicts the newest items first, O(1) operations',
        useCase: 'Stack-like behavior for temporary data'
    }
];

// Re-export cache profiles from separate file
export { CACHE_PROFILES } from './cacheProfiles';

// Export old names for backward compatibility
export { CACHE_PROFILES as CACHE_TYPES } from './cacheProfiles';
