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

// Cache profiles with their characteristics
export const CACHE_PROFILES: { name: string; title: string; description: string; useCase: string; category: string; performance?: string; }[] = [
    // Core Profiles
    {
        name: 'DEFAULT',
        title: 'Default Profile',
        description: 'General-purpose cache with balanced performance',
        useCase: 'Standard caching needs, good all-around performance',
        category: 'Core',
        performance: '40.4ns GET, 92.6ns PUT'
    },
    {
        name: 'READ_HEAVY',
        title: 'Read-Heavy Profile',
        description: 'Optimized for read-intensive workloads (80%+ reads)',
        useCase: 'Read-heavy applications, reference data, configuration',
        category: 'Core',
        performance: '11.5ns GET'
    },
    {
        name: 'WRITE_HEAVY',
        title: 'Write-Heavy Profile',
        description: 'Optimized for write-intensive workloads (50%+ writes)',
        useCase: 'Write-heavy applications, session storage, logging',
        category: 'Core',
        performance: '393.5ns PUT'
    },
    {
        name: 'MEMORY_EFFICIENT',
        title: 'Memory-Efficient Profile',
        description: 'Minimizes memory usage for constrained environments',
        useCase: 'Memory-constrained environments, embedded systems',
        category: 'Core',
        performance: '39.7ns GET, 88.5ns PUT'
    },
    {
        name: 'HIGH_PERFORMANCE',
        title: 'High-Performance Profile',
        description: 'Maximum throughput optimization',
        useCase: 'High-throughput applications, performance-critical systems',
        category: 'Core',
        performance: '24.6ns GET, 63.8ns PUT'
    },
    // Specialized Profiles
    {
        name: 'SESSION_CACHE',
        title: 'Session Cache Profile',
        description: 'Optimized for user session storage with time-based expiration',
        useCase: 'User sessions, temporary data with TTL',
        category: 'Specialized',
        performance: '40.4ns GET, 92.6ns PUT'
    },
    {
        name: 'API_CACHE',
        title: 'API Cache Profile',
        description: 'Optimized for API response caching with short TTL',
        useCase: 'External API responses, network-bound operations',
        category: 'Specialized',
        performance: '728.8ns GET, 1150.0ns PUT'
    },
    {
        name: 'COMPUTE_CACHE',
        title: 'Compute Cache Profile',
        description: 'Optimized for expensive computation results',
        useCase: 'Heavy computations, ML inference, complex calculations',
        category: 'Specialized',
        performance: '728.8ns GET, 1150.0ns PUT'
    },
    // Advanced Profiles
    {
        name: 'ML_OPTIMIZED',
        title: 'ML-Optimized Profile',
        description: 'Machine learning optimized with predictive capabilities',
        useCase: 'ML applications, predictive caching scenarios',
        category: 'Advanced',
        performance: '42961.5ns GET, 349.6ns PUT'
    },
    {
        name: 'ZERO_COPY',
        title: 'Zero-Copy Profile',
        description: 'Ultra-low latency with zero-copy operations',
        useCase: 'High-frequency trading, ultra-low latency requirements',
        category: 'Advanced',
        performance: '7.9ns GET (2.6x faster than Caffeine)'
    },
    {
        name: 'HARDWARE_OPTIMIZED',
        title: 'Hardware-Optimized Profile',
        description: 'Hardware-optimized with CPU-specific features',
        useCase: 'Hardware-specific optimizations, embedded systems',
        category: 'Advanced',
        performance: '24.7ns GET, 78.4ns PUT'
    },
    {
        name: 'DISTRIBUTED',
        title: 'Distributed Profile',
        description: 'Distributed cache optimized for cluster environments',
        useCase: 'Multi-node applications, distributed systems',
        category: 'Advanced',
        performance: 'Network-dependent latency'
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
    },
    {
        name: 'IDLE_TIME',
        title: 'Idle Time Based',
        description: 'Evicts items based on idle time threshold, O(n) scan for expired items',
        useCase: 'Session management and temporary data'
    },
    {
        name: 'WEIGHT_BASED',
        title: 'Weight Based',
        description: 'Evicts items based on memory weight, O(1) operations with read-write locks',
        useCase: 'Memory-constrained environments'
    }
];

// Frequency sketch options
export const FREQUENCY_SKETCH_OPTIONS: { name: string; title: string; description: string; useCase: string; }[] = [
    {
        name: 'NONE',
        title: 'No Frequency Sketch',
        description: 'Pure algorithm without frequency tracking',
        useCase: 'Minimal memory overhead, simple access patterns'
    },
    {
        name: 'BASIC',
        title: 'Basic Frequency Sketch',
        description: 'Standard frequency tracking with moderate accuracy',
        useCase: 'Balanced accuracy and memory usage (default)'
    },
    {
        name: 'OPTIMIZED',
        title: 'Optimized Frequency Sketch',
        description: 'Advanced frequency tracking with higher accuracy',
        useCase: 'Complex access patterns, maximum accuracy'
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

// Common code examples
export const BASIC_USAGE_JAVA = `import io.github.dhruv1110.jcachex.*;

// Profile-based approach - automatically optimized
Cache<String, User> cache = CacheBuilder
    .profile("READ_HEAVY")  // Optimized for read-intensive workloads
    .name("users")
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();

// Basic operations
cache.put("user123", new User("Alice", "alice@example.com"));
User user = cache.get("user123");
System.out.println("User: " + user.getName());

// Check performance
CacheStats stats = cache.stats();
System.out.println("Hit rate: " + (stats.hitRate() * 100) + "%");`;

export const BASIC_USAGE_KOTLIN = `import io.github.dhruv1110.jcachex.kotlin.*

// Profile-based approach with Kotlin DSL
val cache = createCache<String, User> {
    profile("WRITE_HEAVY")  // Optimized for write-intensive workloads
    name("users")
    maximumSize(1000)
    expireAfterWrite(30.minutes)
}

// Basic operations with operator overloading
cache["user123"] = User("Alice", "alice@example.com")
val user = cache["user123"]
println("User: \${user?.name}")

// Check performance
val stats = cache.stats()
println("Hit rate: \${stats.hitRatePercent()}%")`;

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

// Profile-based examples
export const PROFILE_EXAMPLES_JAVA = `import io.github.dhruv1110.jcachex.*;

// Different profiles for different use cases
public class ProfileExamples {

    // For read-heavy workloads
    Cache<String, Product> productCache = CacheBuilder
        .profile("READ_HEAVY")
        .name("products")
        .maximumSize(5000L)
        .build();

    // For session storage
    Cache<String, UserSession> sessionCache = CacheBuilder
        .profile("SESSION_CACHE")
        .name("sessions")
        .maximumSize(2000L)
        .build();

    // For API responses
    Cache<String, ApiResponse> apiCache = CacheBuilder
        .profile("API_CACHE")
        .name("api-responses")
        .maximumSize(500L)
        .build();

    // For distributed caching
    Cache<String, Order> orderCache = CacheBuilder
        .profile("DISTRIBUTED")
        .name("orders")
        .clusterNodes("cache-1:8080", "cache-2:8080")
        .build();
}`;

// Performance comparison examples
export const PERFORMANCE_COMPARISON_JAVA = `import io.github.dhruv1110.jcachex.*;

public class PerformanceComparison {
    public static void main(String[] args) {
        // Ultra-fast reads - Zero-copy profile (7.9ns)
        Cache<String, String> ultraFastCache = CacheBuilder
            .profile("ZERO_COPY")
            .name("ultra-fast")
            .maximumSize(1000L)
            .build();

        // Balanced performance - High-performance profile (24.6ns GET)
        Cache<String, String> balancedCache = CacheBuilder
            .profile("HIGH_PERFORMANCE")
            .name("balanced")
            .maximumSize(1000L)
            .build();

        // Memory efficient - Memory-efficient profile
        Cache<String, String> memoryCache = CacheBuilder
            .profile("MEMORY_EFFICIENT")
            .name("memory-efficient")
            .maximumSize(100L)
            .build();

        // Performance test
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            ultraFastCache.put("key" + i, "value" + i);
        }
        long endTime = System.nanoTime();
        System.out.println("Zero-copy PUT Time: " + (endTime - startTime) / 1000000 + "ms");

        startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            ultraFastCache.get("key" + i);
        }
        endTime = System.nanoTime();
        System.out.println("Zero-copy GET Time: " + (endTime - startTime) / 1000000 + "ms");
    }
}`;

// Export old names for backward compatibility
export const CACHE_TYPES = CACHE_PROFILES;
export const ADVANCED_CACHE_BUILDER_JAVA = PROFILE_EXAMPLES_JAVA;
