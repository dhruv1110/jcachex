import type { CodeTab, Feature, EvictionStrategy, Module, PerformanceStat, ArchitectureComponent } from '@/types';

// Version management
export const VERSION = process.env.REACT_APP_VERSION || '1.0.0';

// Installation tabs configuration
export const INSTALLATION_TABS: CodeTab[] = [
    {
        id: 'maven',
        label: 'Maven',
        language: 'xml',
        code: `<!-- Core JCacheX library -->
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>\${VERSION}</version>
</dependency>

<!-- For Spring Boot integration -->
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-spring</artifactId>
    <version>\${VERSION}</version>
</dependency>

<!-- For Kotlin extensions -->
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-kotlin</artifactId>
    <version>\${VERSION}</version>
</dependency>`
    },
    {
        id: 'gradle',
        label: 'Gradle',
        language: 'gradle',
        code: `// Core JCacheX library
implementation 'io.github.dhruv1110:jcachex-core:\${VERSION}'

// For Spring Boot integration
implementation 'io.github.dhruv1110:jcachex-spring:\${VERSION}'

// For Kotlin extensions
implementation 'io.github.dhruv1110:jcachex-kotlin:\${VERSION}'`
    },
    {
        id: 'sbt',
        label: 'SBT',
        language: 'scala',
        code: `// Core JCacheX library
libraryDependencies += "io.github.dhruv1110" % "jcachex-core" % "\${VERSION}"

// For Spring Boot integration
libraryDependencies += "io.github.dhruv1110" % "jcachex-spring" % "\${VERSION}"

// For Kotlin extensions
libraryDependencies += "io.github.dhruv1110" % "jcachex-kotlin" % "\${VERSION}"`
    }
];

// Spring Boot installation tabs
export const SPRING_INSTALLATION_TABS: CodeTab[] = [
    {
        id: 'maven',
        label: 'Maven',
        language: 'xml',
        code: `<dependencies>
    <!-- JCacheX Spring Boot Starter -->
    <dependency>
        <groupId>io.github.dhruv1110</groupId>
        <artifactId>jcachex-spring-boot-starter</artifactId>
        <version>\${VERSION}</version>
    </dependency>

    <!-- Core JCacheX (included automatically) -->
    <dependency>
        <groupId>io.github.dhruv1110</groupId>
        <artifactId>jcachex-core</artifactId>
        <version>\${VERSION}</version>
    </dependency>

    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot Starter Actuator (Optional) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>`
    },
    {
        id: 'gradle',
        label: 'Gradle',
        language: 'gradle',
        code: `dependencies {
    // JCacheX Spring Boot Starter
    implementation 'io.github.dhruv1110:jcachex-spring-boot-starter:\${VERSION}'

    // Core JCacheX (included automatically)
    implementation 'io.github.dhruv1110:jcachex-core:\${VERSION}'

    // Spring Boot Starter Web
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // Spring Boot Starter Actuator (Optional)
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}`
    }
];

// Common features data
export const FEATURES: Feature[] = [
    {
        icon: '⚡',
        title: 'High Performance',
        description: 'Optimized for speed with nanoTime operations, immediate eviction, and efficient data structures.',
        details: ['Sub-microsecond latency operations', 'O(1) cache operations', 'Minimal GC pressure']
    },
    {
        icon: '🔧',
        title: 'Simple API',
        description: 'Clean, intuitive API with fluent builders and comprehensive documentation.',
        details: ['Fluent builder pattern', 'Type-safe operations', 'Extensive JavaDoc']
    },
    {
        icon: '🔄',
        title: 'Async Support',
        description: 'Built-in asynchronous operations with CompletableFuture and Kotlin coroutines.',
        details: ['CompletableFuture integration', 'Kotlin coroutines', 'Non-blocking operations']
    },
    {
        icon: '🍃',
        title: 'Spring Integration',
        description: 'Seamless integration with Spring Boot, annotations, and auto-configuration.',
        details: ['@Cacheable support', 'Auto-configuration', 'Properties binding']
    },
    {
        icon: '🌐',
        title: 'Distributed Caching',
        description: 'Scale across multiple nodes with consistency guarantees and automatic failover.',
        details: ['Multi-node clustering', 'Consistency models', 'Automatic failover']
    },
    {
        icon: '📊',
        title: 'Monitoring',
        description: 'Built-in metrics, statistics, and observability for production environments.',
        details: ['Hit/miss ratios', 'Performance metrics', 'JMX support']
    }
];

// Eviction strategies
export const EVICTION_STRATEGIES: EvictionStrategy[] = [
    {
        name: 'LRU',
        title: 'Least Recently Used',
        description: 'Evicts the least recently accessed items first',
        useCase: 'General-purpose caching with temporal locality'
    },
    {
        name: 'LFU',
        title: 'Least Frequently Used',
        description: 'Evicts items with the lowest access frequency',
        useCase: 'Workloads with frequency-based access patterns'
    },
    {
        name: 'FIFO',
        title: 'First In, First Out',
        description: 'Evicts the oldest items first',
        useCase: 'Simple eviction with predictable behavior'
    },
    {
        name: 'FILO',
        title: 'First In, Last Out',
        description: 'Evicts the newest items first',
        useCase: 'Stack-like behavior for temporary data'
    },
    {
        name: 'IDLE_TIME',
        title: 'Idle Time Based',
        description: 'Evicts items based on idle time threshold',
        useCase: 'Session management and temporary data'
    },
    {
        name: 'WEIGHT_BASED',
        title: 'Weight Based',
        description: 'Evicts items based on memory weight',
        useCase: 'Memory-constrained environments'
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

// Performance stats
export const PERFORMANCE_STATS: PerformanceStat[] = [
    { label: 'Throughput', value: '1M+ ops/sec', description: 'Operations per second' },
    { label: 'Latency', value: '~0.08µs', description: 'Typical GET operation time' },
    { label: 'Memory', value: '90% efficient', description: 'Memory utilization' },
    { label: 'Eviction', value: 'Immediate', description: 'Zero-delay eviction' }
];

// Architecture components
export const ARCHITECTURE: ArchitectureComponent[] = [
    {
        name: 'Cache Interface',
        description: 'Type-safe generic interface for all cache operations'
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

// Common code examples
export const BASIC_USAGE_JAVA = `import io.github.dhruv1110.jcachex.*;

// Create cache configuration
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .evictionStrategy(EvictionStrategy.LRU)
    .recordStats(true)
    .build();

// Create cache instance
Cache<String, User> cache = new DefaultCache<>(config);

// Basic operations
cache.put("user123", new User("Alice", "alice@example.com"));
User user = cache.get("user123");
System.out.println("User: " + user.getName());`;

export const BASIC_USAGE_KOTLIN = `import io.github.dhruv1110.jcachex.kotlin.*

// Create cache with Kotlin DSL
val cache = cache<String, User> {
    maxSize = 1000
    expireAfterWrite = 30.minutes
    evictionStrategy = EvictionStrategy.LRU
    recordStats = true
}

// Basic operations with operator overloading
cache["user123"] = User("Alice", "alice@example.com")
val user = cache["user123"]
println("User: \${user?.name}")`;

export const SPRING_USAGE = `@Service
public class UserService {

    @JCacheXCacheable(cacheName = "users",
                      expireAfterWrite = 30,
                      expireAfterWriteUnit = TimeUnit.MINUTES)
    public User findUserById(String id) {
        return userRepository.findById(id);
    }

    @JCacheXCacheEvict(cacheName = "users")
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}`;
