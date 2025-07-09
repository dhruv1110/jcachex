import React from 'react';
import { useVersion } from '../hooks';
import { INSTALLATION_TABS, BASIC_USAGE_JAVA, BASIC_USAGE_KOTLIN, SPRING_USAGE } from '../constants';
import { Section, Grid, FeatureCard, InstallationGuide, Badge } from './common';
import CodeTabs from './CodeTabs';
import './Examples.css';

const Examples = () => {
    const { version } = useVersion();

    const basicTabs = [
        {
            id: 'java',
            label: 'Java',
            language: 'java',
            code: `import io.github.dhruv1110.jcachex.*;

// Create cache with comprehensive configuration
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .expireAfterAccess(Duration.ofMinutes(10))
    .evictionStrategy(EvictionStrategy.LRU)
    .recordStats(true)
    .build();

Cache<String, User> cache = new DefaultCache<>(config);

// Basic operations
User alice = new User("Alice", "alice@example.com", 25);
cache.put("user123", alice);
User retrievedUser = cache.get("user123");

// Bulk operations
Map<String, User> users = Map.of(
    "user1", new User("Bob", "bob@example.com", 30),
    "user2", new User("Charlie", "charlie@example.com", 35)
);
cache.putAll(users);

// Statistics
CacheStats stats = cache.stats();
System.out.println("Hit rate: " + stats.hitRate());`
        },
        {
            id: 'kotlin',
            label: 'Kotlin',
            language: 'kotlin',
            code: `import io.github.dhruv1110.jcachex.kotlin.*

// Create cache with Kotlin DSL
val cache = cache<String, User> {
    maxSize = 1000
    expireAfterWrite = 30.minutes
    expireAfterAccess = 10.minutes
    evictionStrategy = EvictionStrategy.LRU
    recordStats = true
}

// Basic operations with operator overloading
val alice = User("Alice", "alice@example.com", 25)
cache["user123"] = alice
val retrievedUser = cache["user123"]

// Bulk operations
val users = mapOf(
    "user1" to User("Bob", "bob@example.com", 30),
    "user2" to User("Charlie", "charlie@example.com", 35)
)
cache += users

// Statistics
val stats = cache.stats()
println("Hit rate: \${stats.hitRate()}")`
        }
    ];

    const asyncTabs = [
        {
            id: 'java',
            label: 'Java Async',
            language: 'java',
            code: `import java.util.concurrent.CompletableFuture;

// Async operations with CompletableFuture
CompletableFuture<User> futureUser = cache.getAsync("user123");

// Handle async result
futureUser.thenAccept(user -> {
    if (user != null) {
        System.out.println("Found user: " + user.getName());
        user.setLastAccess(Instant.now());
        cache.putAsync("user123", user);
    } else {
        System.out.println("User not found, loading from database...");
        loadUserFromDatabase("user123")
            .thenAccept(dbUser -> cache.putAsync("user123", dbUser));
    }
});

// Bulk async operations
List<String> userIds = Arrays.asList("user1", "user2", "user3");
List<CompletableFuture<User>> futures = userIds.stream()
    .map(id -> cache.getAsync(id))
    .collect(Collectors.toList());

CompletableFuture<List<User>> allUsers = CompletableFuture.allOf(
    futures.toArray(new CompletableFuture[0])
).thenApply(v -> futures.stream()
    .map(CompletableFuture::join)
    .filter(Objects::nonNull)
    .collect(Collectors.toList()));

allUsers.thenAccept(users ->
    System.out.println("Loaded " + users.size() + " users from cache"));`
        },
        {
            id: 'kotlin',
            label: 'Kotlin Coroutines',
            language: 'kotlin',
            code: `import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// Coroutine-based async operations
suspend fun getUserWithFallback(id: String): User? = withContext(Dispatchers.IO) {
    cache.getAsync(id).await() ?: run {
        println("User not found in cache, loading from database...")
        loadUserFromDatabase(id)?.also { dbUser ->
            cache.putAsync(id, dbUser).await()
        }
    }
}

// Bulk async operations with Flow
suspend fun loadUsersFlow(userIds: List<String>): Flow<User> = flow {
    userIds.forEach { id ->
        val user = cache.getAsync(id).await()
        if (user != null) {
            emit(user)
        }
    }
}.flowOn(Dispatchers.IO)

// Usage
runBlocking {
    val userIds = listOf("user1", "user2", "user3")
    loadUsersFlow(userIds).collect { user ->
        println("Loaded user: \${user.name}")
    }
}`
        }
    ];

    const springTabs = [
        {
            id: 'service',
            label: 'Service Layer',
            language: 'java',
            code: `@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Basic caching
    @JCacheXCacheable(cacheName = "users")
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    // Custom cache configuration
    @JCacheXCacheable(
        cacheName = "userProfiles",
        expireAfterWrite = 30,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public UserProfile getUserProfile(String userId) {
        return buildUserProfile(userId);
    }

    // Cache eviction
    @JCacheXCacheEvict(cacheName = "users")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Clear all cache entries
    @JCacheXCacheEvict(cacheName = "users", allEntries = true)
    public void clearAllUsers() {
        userRepository.deleteAll();
    }
}`
        },
        {
            id: 'controller',
            label: 'REST Controller',
            language: 'java',
            code: `@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable String id) {
        UserProfile profile = userService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clear-cache")
    public ResponseEntity<Void> clearCache() {
        userService.clearAllUsers();
        return ResponseEntity.ok().build();
    }
}`
        }
    ];

    const distributedTabs = [
        {
            id: 'setup',
            label: 'Distributed Setup',
            language: 'java',
            code: `import io.github.dhruv1110.jcachex.distributed.*;

// Create distributed cache configuration
DistributedCacheConfig<String, User> config = DistributedCacheConfig.<String, User>builder()
    .maximumSize(10000L)
    .expireAfterWrite(Duration.ofHours(1))
    .evictionStrategy(EvictionStrategy.LRU)
    .consistencyModel(ConsistencyModel.EVENTUAL)
    .nodes(Arrays.asList("node1:8080", "node2:8080", "node3:8080"))
    .replicationFactor(2)
    .build();

// Create distributed cache
DistributedCache<String, User> distributedCache = new DefaultDistributedCache<>(config);

// Operations work the same as local cache
distributedCache.put("user123", new User("Alice", "alice@example.com"));
User user = distributedCache.get("user123");

// Monitor cluster health
ClusterHealth health = distributedCache.getClusterHealth();
System.out.println("Active nodes: " + health.getActiveNodes());
System.out.println("Cluster status: " + health.getStatus());`
        },
        {
            id: 'consistency',
            label: 'Consistency Models',
            language: 'java',
            code: `// Strong consistency - all nodes must agree
DistributedCacheConfig<String, User> strongConfig = DistributedCacheConfig.<String, User>builder()
    .consistencyModel(ConsistencyModel.STRONG)
    .writeQuorum(3)
    .readQuorum(2)
    .build();

// Eventual consistency - best performance
DistributedCacheConfig<String, User> eventualConfig = DistributedCacheConfig.<String, User>builder()
    .consistencyModel(ConsistencyModel.EVENTUAL)
    .asyncReplication(true)
    .build();

// Session consistency - read-your-writes
DistributedCacheConfig<String, User> sessionConfig = DistributedCacheConfig.<String, User>builder()
    .consistencyModel(ConsistencyModel.SESSION)
    .stickySession(true)
    .build();

// Choose based on your requirements:
// - STRONG: Banking, financial data
// - EVENTUAL: User profiles, product catalogs
// - SESSION: Shopping carts, user sessions`
        }
    ];

    const monitoringTabs = [
        {
            id: 'metrics',
            label: 'Micrometer Metrics',
            language: 'java',
            code: `@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> cacheMetricsCustomizer() {
        return registry -> {
            // Register JCacheX metrics
            JCacheXMetrics.monitor(registry, cache, "user-cache");

            // Custom metrics
            Gauge.builder("cache.size")
                .description("Current cache size")
                .register(registry, cache, Cache::size);

            Timer.builder("cache.load.time")
                .description("Time to load cache entries")
                .register(registry);
        };
    }
}

// Usage in service
@Service
public class UserService {

    @Autowired
    private MeterRegistry meterRegistry;

    @Timed("user.cache.access")
    public User findUser(String id) {
        return cache.get(id);
    }

    @Counted("user.cache.miss")
    public User loadFromDatabase(String id) {
        return userRepository.findById(id);
    }
}`
        },
        {
            id: 'actuator',
            label: 'Actuator Endpoints',
            language: 'yaml',
            code: `# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches,jcachex
  endpoint:
    health:
      show-details: always
    caches:
      enabled: true
    jcachex:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        cache: true

# Access endpoints:
# GET /actuator/health - Application health
# GET /actuator/metrics - All metrics
# GET /actuator/caches - Cache information
# GET /actuator/jcachex - JCacheX specific metrics`
        }
    ];

    const exampleCategories = [
        {
            id: 'basic',
            icon: '🚀',
            title: 'Basic Operations',
            description: 'Essential cache operations with Java and Kotlin examples',
            tabs: basicTabs
        },
        {
            id: 'async',
            icon: '⚡',
            title: 'Async Operations',
            description: 'Asynchronous caching with CompletableFuture and coroutines',
            tabs: asyncTabs
        },
        {
            id: 'spring',
            icon: '🍃',
            title: 'Spring Boot Integration',
            description: 'Complete Spring Boot integration with annotations',
            tabs: springTabs
        },
        {
            id: 'distributed',
            icon: '🌐',
            title: 'Distributed Caching',
            description: 'Multi-node caching with consistency models',
            tabs: distributedTabs
        },
        {
            id: 'monitoring',
            icon: '📊',
            title: 'Monitoring & Observability',
            description: 'Metrics, health checks, and monitoring integration',
            tabs: monitoringTabs
        }
    ];

    const performanceTips = [
        {
            icon: '⚡',
            title: 'Batch Operations',
            description: 'Use putAll() and getAll() for better performance',
            details: ['Reduced network calls', 'Better throughput', 'Atomic operations']
        },
        {
            icon: '🔄',
            title: 'Async Loading',
            description: 'Leverage async operations for I/O-bound workloads',
            details: ['Non-blocking operations', 'Better resource utilization', 'Improved scalability']
        },
        {
            icon: '📊',
            title: 'Monitor Metrics',
            description: 'Track cache performance and optimize accordingly',
            details: ['Hit rate monitoring', 'Eviction tracking', 'Memory usage analysis']
        }
    ];

    const resources = [
        {
            title: 'Spring Boot Guide',
            description: 'Complete guide for Spring Boot integration',
            icon: '🍃',
            href: '/spring',
            badge: 'spring'
        },
        {
            title: 'API Documentation',
            description: 'Comprehensive API reference and JavaDoc',
            icon: '📖',
            href: 'https://javadoc.io/doc/io.github.dhruv1110/jcachex-core',
            badge: 'java'
        },
        {
            title: 'GitHub Repository',
            description: 'Source code, issues, and contributions',
            icon: '🔗',
            href: 'https://github.com/dhruv1110/JCacheX',
            badge: 'github'
        }
    ];

    return (
        <div className="examples">
            {/* Header */}
            <Section background="gradient" padding="large" centered>
                <div className="header-content">
                    <h1 className="page-title">JCacheX Examples</h1>
                    <p className="page-subtitle">
                        Explore comprehensive examples and learn how to integrate JCacheX into your applications
                    </p>
                </div>
            </Section>

            {/* Installation */}
            <Section padding="large">
                <InstallationGuide
                    tabs={INSTALLATION_TABS}
                    title="Quick Setup"
                    description="Get started with JCacheX in your project:"
                />
            </Section>

            {/* Example Categories */}
            <Section
                background="light"
                padding="large"
                title="Example Categories"
                subtitle="Choose from different categories based on your needs"
                centered
            >
                <div className="examples-grid">
                    {exampleCategories.map((category, index) => (
                        <div key={category.id} className="example-category">
                            <div className="category-header">
                                <div className="category-icon">{category.icon}</div>
                                <h3 className="category-title">{category.title}</h3>
                                <p className="category-description">{category.description}</p>
                            </div>
                            <div className="category-content">
                                <CodeTabs tabs={category.tabs} />
                            </div>
                        </div>
                    ))}
                </div>
            </Section>

            {/* Performance Tips */}
            <Section
                padding="large"
                title="Performance Tips"
                subtitle="Best practices for optimal cache performance"
                centered
            >
                <Grid columns={3} gap="large">
                    {performanceTips.map((tip, index) => (
                        <FeatureCard
                            key={index}
                            icon={tip.icon}
                            title={tip.title}
                            description={tip.description}
                            details={tip.details}
                            variant="compact"
                        />
                    ))}
                </Grid>
            </Section>

            {/* Resources */}
            <Section
                background="light"
                padding="large"
                title="Additional Resources"
                subtitle="Learn more about JCacheX and get support"
                centered
            >
                <Grid columns={3} gap="large">
                    {resources.map((resource, index) => (
                        <div key={index} className="resource-card">
                            <div className="resource-icon">{resource.icon}</div>
                            <h3 className="resource-title">{resource.title}</h3>
                            <p className="resource-description">{resource.description}</p>
                            <div className="resource-action">
                                <Badge
                                    variant={resource.badge}
                                    href={resource.href}
                                    target={resource.href.startsWith('http') ? '_blank' : '_self'}
                                >
                                    {resource.href.startsWith('http') ? 'Open' : 'View'}
                                </Badge>
                            </div>
                        </div>
                    ))}
                </Grid>
            </Section>

            {/* Next Steps */}
            <Section background="gradient" padding="large" centered>
                <div className="next-steps">
                    <h2 className="next-steps-title">Ready to implement?</h2>
                    <p className="next-steps-subtitle">
                        Start building with JCacheX in your application
                    </p>
                    <div className="next-steps-buttons">
                        <Badge variant="primary" size="large" href="/getting-started">
                            Get Started
                        </Badge>
                        <Badge variant="default" size="large" href="/spring">
                            Spring Boot Guide
                        </Badge>
                    </div>
                </div>
            </Section>
        </div>
    );
};

export default Examples;

