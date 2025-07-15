// Version management
export const VERSION = process.env.REACT_APP_VERSION || '1.0.0';

// Installation tabs configuration
export const INSTALLATION_TABS = [
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
        code: `libraryDependencies ++= Seq(
  "io.github.dhruv1110" % "jcachex-core" % "\${VERSION}",
  "io.github.dhruv1110" % "jcachex-spring" % "\${VERSION}",
  "io.github.dhruv1110" % "jcachex-kotlin" % "\${VERSION}"
)`
    }
];

// Spring Boot installation tabs
export const SPRING_INSTALLATION_TABS = [
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
export const FEATURES = [
    {
        icon: 'performance',
        title: 'Optimized Performance',
        description: 'High-performance caching with nanoTime operations, immediate eviction, and concurrent data structures.',
        details: ['Sub-microsecond latency', 'Immediate eviction triggers', 'Minimal allocation overhead']
    },
    {
        icon: 'api',
        title: 'Developer-Friendly API',
        description: 'Clean, type-safe interface with comprehensive configuration options and fluent builders.',
        details: ['Fluent builder pattern', 'Generic type safety', 'Comprehensive documentation']
    },
    {
        icon: 'async',
        title: 'Asynchronous Operations',
        description: 'Non-blocking operations with CompletableFuture support and reactive programming compatibility.',
        details: ['CompletableFuture integration', 'Non-blocking I/O', 'Reactive compatibility']
    },
    {
        icon: 'spring',
        title: 'Spring Framework Integration',
        description: 'Native Spring Boot support with auto-configuration and declarative caching annotations.',
        details: ['Auto-configuration', 'Annotation-driven caching', 'Properties configuration']
    },
    {
        icon: 'distributed',
        title: 'Distributed Architecture',
        description: 'Multi-node caching support with configurable consistency models and network protocols.',
        details: ['Node clustering', 'Configurable consistency', 'Network fault tolerance']
    },
    {
        icon: 'monitoring',
        title: 'Observability & Metrics',
        description: 'Built-in performance monitoring with JMX support and integration-ready metrics collection.',
        details: ['Performance statistics', 'JMX metric exposure', 'Health check endpoints']
    }
];

// Eviction strategies
export const EVICTION_STRATEGIES = [
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
export const MODULES = [
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

// Performance stats - realistic and conservative
export const PERFORMANCE_STATS = [
    { label: 'Latency', value: '~0.08µs', description: 'Typical GET operation time' },
    { label: 'Throughput', value: '1M+ ops/sec', description: 'Concurrent operations capacity' },
    { label: 'Memory', value: 'Efficient', description: 'Optimized memory utilization' },
    { label: 'Eviction', value: 'Immediate', description: 'Zero-delay eviction' }
];

// Architecture components
export const ARCHITECTURE = [
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

// Common code examples with modern JCacheXBuilder patterns
export const BASIC_USAGE_JAVA = `import io.github.dhruv1110.jcachex.*;
import io.github.dhruv1110.jcachex.profiles.ProfileName;
import java.time.Duration;

// Profile-based creation (recommended)
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();

// Or use ProfileName enum for type safety
Cache<String, User> cache = JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY)
    .name("users")
    .maximumSize(1000L)
    .build();

// Basic operations
cache.put("user123", new User("Alice", "alice@example.com"));
User user = cache.get("user123");
System.out.println("User: " + user.getName());`;

export const BASIC_USAGE_KOTLIN = `import io.github.dhruv1110.jcachex.kotlin.*
import io.github.dhruv1110.jcachex.profiles.ProfileName
import java.time.Duration

// Kotlin DSL with convenience methods
val cache = createReadHeavyCache<String, User> {
    name("users")
    maximumSize(1000L)
    expireAfterWrite(Duration.ofMinutes(30))
}

// Or use profile-based creation
val cache = createCacheWithProfile<String, User>(ProfileName.READ_HEAVY) {
    name("users")
    maximumSize(1000L)
}

// Basic operations with operator overloading
cache["user123"] = User("Alice", "alice@example.com")
val user = cache["user123"]
println("User: \${user?.name}")`;

export const SPRING_USAGE = `@Service
public class UserService {

    @JCacheXCacheable(cacheName = "users",
                      profile = "READ_HEAVY",
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
