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

// Cache types with specialized optimizations
export const CACHE_TYPES: { name: string; title: string; description: string; useCase: string; performance?: string; }[] = [
    {
        name: 'DEFAULT',
        title: 'Default Cache',
        description: 'General-purpose cache with balanced performance',
        useCase: 'Standard caching needs, good all-around performance',
        performance: '40.4ns GET, 92.6ns PUT'
    },
    {
        name: 'OPTIMIZED',
        title: 'Optimized Cache',
        description: 'Enhanced with advanced algorithms and optimizations',
        useCase: 'Complex workloads requiring sophisticated eviction policies',
        performance: '728.8ns GET, 1150.0ns PUT'
    },
    {
        name: 'ZERO_COPY_OPTIMIZED',
        title: 'Zero-Copy Cache',
        description: '⚡ Fastest read performance with zero-copy operations',
        useCase: 'Read-heavy workloads, fastest GET operations',
        performance: '7.9ns GET (2.6x faster than Caffeine)'
    },
    {
        name: 'READ_ONLY_OPTIMIZED',
        title: 'Read-Only Cache',
        description: '⚡ Optimized for read-only scenarios',
        useCase: 'Configuration, reference data, immutable datasets',
        performance: '11.5ns GET (1.6x faster than Caffeine)'
    },
    {
        name: 'WRITE_HEAVY_OPTIMIZED',
        title: 'Write-Heavy Cache',
        description: 'Optimized for frequent write operations',
        useCase: 'Applications with high write throughput',
        performance: '393.5ns PUT'
    },
    {
        name: 'LOCALITY_OPTIMIZED',
        title: 'Cache Locality Optimized',
        description: '⚡ Optimized for CPU cache locality',
        useCase: 'CPU-bound applications, data structure locality',
        performance: '9.7ns GET (1.9x faster than Caffeine)'
    },
    {
        name: 'JIT_OPTIMIZED',
        title: 'JIT Optimized',
        description: 'Balanced performance across all operations',
        useCase: 'JIT-compiled environments, balanced workloads',
        performance: '24.6ns GET, 63.8ns PUT'
    },
    {
        name: 'ALLOCATION_OPTIMIZED',
        title: 'Allocation Optimized',
        description: 'Minimizes memory allocations and GC pressure',
        useCase: 'Memory-constrained environments, GC-sensitive applications',
        performance: '39.7ns GET, 88.5ns PUT'
    },
    {
        name: 'HARDWARE_OPTIMIZED',
        title: 'Hardware Optimized',
        description: 'Tuned for specific hardware characteristics',
        useCase: 'Hardware-specific optimizations, embedded systems',
        performance: '24.7ns GET, 78.4ns PUT'
    },
    {
        name: 'JVM_OPTIMIZED',
        title: 'JVM Optimized',
        description: 'JVM-specific optimizations and memory management',
        useCase: 'JVM-based applications, server workloads',
        performance: '31.0ns GET, 277.7ns PUT'
    },
    {
        name: 'ML_OPTIMIZED',
        title: 'Machine Learning Optimized',
        description: 'Adaptive caching with ML-based predictions',
        useCase: 'ML applications, predictive caching scenarios',
        performance: '42961.5ns GET, 349.6ns PUT'
    },
    {
        name: 'PROFILED_OPTIMIZED',
        title: 'Profiled Optimized',
        description: 'Profile-guided optimizations for specific workloads',
        useCase: 'Development environments, performance profiling',
        performance: 'Variable based on profiling'
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
    { label: 'Throughput', value: '1M+ ops/sec', description: 'Operations per second' },
    { label: 'Fastest GET', value: '7.9ns', description: 'ZeroCopy implementation beats Caffeine' },
    { label: 'Balanced Performance', value: '24.6ns GET', description: 'JIT-optimized balanced performance' },
    { label: 'Eviction', value: 'O(1)', description: 'All eviction strategies optimized' }
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

// Create cache configuration (TinyWindowLFU is default)
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .recordStats(true)
    .build();

// Create cache instance using CacheBuilder
Cache<String, User> cache = CacheBuilder.newBuilder()
    .cacheType(CacheType.DEFAULT)
    .config(config)
    .build();

// Basic operations
cache.put("user123", new User("Alice", "alice@example.com"));
User user = cache.get("user123");
System.out.println("User: " + user.getName());`;

export const BASIC_USAGE_KOTLIN = `import io.github.dhruv1110.jcachex.kotlin.*

// Create cache with Kotlin DSL (TinyWindowLFU is default)
val cache = cache<String, User> {
    maxSize = 1000
    expireAfterWrite = 30.minutes
    frequencySketchType = FrequencySketchType.BASIC
    recordStats = true
}

// Or use specific optimized cache types
val readOnlyCache = createReadOnlyOptimizedCache<String, User> {
    maxSize = 1000
    expireAfterWrite = 30.minutes
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

// Advanced cache builder examples
export const ADVANCED_CACHE_BUILDER_JAVA = `import io.github.dhruv1110.jcachex.*;
import io.github.dhruv1110.jcachex.eviction.FrequencySketchType;

// High-performance read-heavy workload
Cache<String, Product> productCache = CacheBuilder.forReadHeavyWorkload()
    .maximumSize(5000L)
    .expireAfterWrite(Duration.ofHours(2))
    .build();

// Write-heavy workload optimization
Cache<String, UserSession> sessionCache = CacheBuilder.forWriteHeavyWorkload()
    .maximumSize(10000L)
    .expireAfterAccess(Duration.ofMinutes(30))
    .build();

// Memory-constrained environment
Cache<String, Configuration> configCache = CacheBuilder.forMemoryConstrainedEnvironment()
    .maximumSize(100L)
    .expireAfterWrite(Duration.ofHours(12))
    .build();

// Custom configuration with frequency sketch
Cache<String, User> userCache = CacheBuilder.newBuilder()
    .cacheType(CacheType.JIT_OPTIMIZED)
    .maximumSize(1000L)
    .evictionStrategy(EvictionStrategy.ENHANCED_LRU)
    .frequencySketchType(FrequencySketchType.OPTIMIZED)
    .recordStats(true)
    .build();`;

// Performance comparison examples
export const PERFORMANCE_COMPARISON_JAVA = `import io.github.dhruv1110.jcachex.*;

public class PerformanceComparison {
    public static void main(String[] args) {
        // Fastest GET operations - ZeroCopy (7.9ns)
        Cache<String, String> fastestCache = CacheBuilder.newBuilder()
            .cacheType(CacheType.ZERO_COPY_OPTIMIZED)
            .maximumSize(1000L)
            .build();

        // Balanced performance - JIT Optimized (24.6ns GET, 63.8ns PUT)
        Cache<String, String> balancedCache = CacheBuilder.newBuilder()
            .cacheType(CacheType.JIT_OPTIMIZED)
            .maximumSize(1000L)
            .build();

        // Memory efficient - Allocation Optimized (39.7ns GET, 88.5ns PUT)
        Cache<String, String> memoryCache = CacheBuilder.newBuilder()
            .cacheType(CacheType.ALLOCATION_OPTIMIZED)
            .maximumSize(1000L)
            .build();

        // Best locality - Cache Locality Optimized (9.7ns GET)
        Cache<String, String> localityCache = CacheBuilder.newBuilder()
            .cacheType(CacheType.LOCALITY_OPTIMIZED)
            .maximumSize(1000L)
            .build();

        // Performance test
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            fastestCache.put("key" + i, "value" + i);
        }
        long endTime = System.nanoTime();
        System.out.println("ZeroCopy PUT Time: " + (endTime - startTime) / 1000000 + "ms");

        startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            fastestCache.get("key" + i);
        }
        endTime = System.nanoTime();
        System.out.println("ZeroCopy GET Time: " + (endTime - startTime) / 1000000 + "ms");
    }
}`;
