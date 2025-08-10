# JCacheX

<div align="center">

**High‑performance Java caching** — profile‑based, from in‑memory to distributed (Kubernetes), with Kotlin DSL and Spring Boot integration.

[Getting Started](#quick-start) • [Docs](https://dhruv1110.github.io/jcachex/) • [Examples](example/) • [Benchmarks](benchmarks/) • [Star ★](https://github.com/dhruv1110/jcachex)

</div>

---

## Why JCacheX?

- **Profile‑based simplicity**: one‑liners for common workloads (READ_HEAVY, WRITE_HEAVY, API, SESSION, ZERO_COPY)
- **Production‑ready**: async loaders, rich stats, listeners, health/metrics
- **Evolves with you**: same API from local in‑memory to distributed on Kubernetes
- **Developer‑friendly**: fluent Java API, idiomatic Kotlin DSL, Spring annotations

> Deep benchmarks, profiles and trade‑offs are on the website to keep this README focused.

---

## Quick Start

---

### 1) Install
Use the latest version from Maven Central (see badge):

```xml
<dependency>
  <groupId>io.github.dhruv1110</groupId>
  <artifactId>jcachex-core</artifactId>
  <version>2.0.1</version>
</dependency>
```

```gradle
implementation "io.github.dhruv1110:jcachex-core:2.0.1"
```

Optional modules: `jcachex-spring` (Spring Boot), `jcachex-kotlin` (Kotlin DSL).

### 2) Create a cache (Java)
```java
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(1000L)
    .build();

cache.put("user:123", new User("Alice"));
User u = cache.get("user:123");
System.out.println(cache.stats());
```

### 3) Kotlin DSL
```kotlin
val users = createReadHeavyCache<String, User> {
    name("users"); maximumSize(1000)
}
users["user:1"] = User("Alice")
println(users.stats().hitRate())
```

### 4) Spring Boot (annotations)
```java
@JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY")
public User findUser(String id) { return repo.findById(id); }
```

More: [Examples](example/) • [Spring Guide](https://dhruv1110.github.io/jcachex/spring)

---

## Distributed on Kubernetes
Same API, pod‑aware discovery and consistent hashing for clusters. See the website’s Kubernetes section and `example/distributed/kubernetes`.

---

## Links
- Website & Docs: https://dhruv1110.github.io/jcachex/
- API Reference (Javadoc): https://javadoc.io/doc/io.github.dhruv1110/jcachex-core
- Examples: `example/` • Benchmarks: `benchmarks/`
- Troubleshooting & Migration: see website navigation

---

## Comprehensive Usage Examples

### Basic Usage
```java
// Simple cache with smart defaults
Cache<String, String> cache = JCacheXBuilder.create()
    .name("simple")
    .maximumSize(100L)
    .build();

cache.put("key1", "value1");
String value = cache.get("key1");
```

### Advanced Configuration
```java
// Custom configuration with async loading
Cache<String, User> userCache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .expireAfterAccess(Duration.ofMinutes(10))
    .loader(userId -> loadUserFromDatabase(userId))
    .recordStats(true)
    .build();
```

### Multi-Language Support

#### Java
```java
// Profile-based approach
Cache<String, Product> productCache = JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY)
    .name("products")
    .maximumSize(5000L)
    .build();

// Convenience method
Cache<String, UserSession> sessionCache = JCacheXBuilder.forSessionStorage()
    .name("sessions")
    .maximumSize(2000L)
    .build();

// Smart defaults
Cache<String, Order> orderCache = JCacheXBuilder.withSmartDefaults()
    .workloadCharacteristics(WorkloadCharacteristics.builder()
        .readToWriteRatio(6.0)
        .build())
    .build();
```

#### Kotlin
```kotlin
// DSL style with convenience methods
val readHeavyCache = createReadHeavyCache<String, Product> {
    name("products")
    maximumSize(5000L)
}

val sessionCache = createSessionCache<String, UserSession> {
    name("sessions")
    maximumSize(2000L)
}

// Profile-based with DSL
val profileCache = createCacheWithProfile<String, Data>(ProfileName.HIGH_PERFORMANCE) {
    name("high-perf")
    maximumSize(10000L)
}
```

#### Spring Boot
```yaml
# application.yml
jcachex:
  caches:
    users:
      profile: READ_HEAVY
      maximumSize: 5000
    sessions:
      profile: SESSION_CACHE
      maximumSize: 2000
    products:
      profile: HIGH_PERFORMANCE
      maximumSize: 10000
```

#### Spring Boot with Annotations
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new JCacheXCacheManager();
    }
}

@Service
public class UserService {

    // JCacheX-specific annotations with profiles
    @JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY")
    public User findUserById(String id) {
        return userRepository.findById(id);
    }

    @JCacheXCacheEvict(cacheName = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // Standard Spring annotations (also supported)
    @Cacheable("sessions")
    public UserSession createSession(String userId) {
        return new UserSession(userId, System.currentTimeMillis());
    }

    @CacheEvict(value = "sessions", key = "#sessionId")
    public void invalidateSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}
```

## 🌱 Spring Boot Annotation-Based Usage

### Basic Setup

Add the JCacheX Spring Boot starter:

```xml
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-spring</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Configuration

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new JCacheXCacheManager();
    }
}
```

### JCacheX-Specific Annotations

#### @JCacheXCacheable - Advanced Caching
```java
@Service
public class UserService {

    // Profile-based caching with automatic optimization
    @JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY")
    public User findUserById(String id) {
        return userRepository.findById(id);
    }

    // Custom expiration and size limits
    @JCacheXCacheable(
        cacheName = "userProfiles",
        expireAfterWrite = 30,
        expireAfterWriteUnit = TimeUnit.MINUTES,
        maximumSize = 5000
    )
    public UserProfile getUserProfile(String userId) {
        return buildUserProfile(userId);
    }

    // Conditional caching with custom keys
    @JCacheXCacheable(
        cacheName = "apiResponses",
        key = "#endpoint + '_' + #version",
        condition = "#cacheable == true",
        unless = "#result.isEmpty()"
    )
    public ApiResponse callApi(String endpoint, String version, boolean cacheable) {
        return httpClient.get(endpoint + "?v=" + version);
    }

    // Session-optimized caching
    @JCacheXCacheable(
        cacheName = "userSessions",
        profile = "SESSION_CACHE",
        expireAfterWrite = 30,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public UserSession createSession(String userId) {
        return new UserSession(userId, System.currentTimeMillis());
    }
}
```

#### @JCacheXCacheEvict - Advanced Eviction
```java
@Service
public class UserService {

    // Evict specific user
    @JCacheXCacheEvict(cacheName = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // Evict multiple related caches
    @JCacheXCacheEvict(cacheName = "users", key = "#userId")
    @JCacheXCacheEvict(cacheName = "userProfiles", key = "#userId")
    @JCacheXCacheEvict(cacheName = "userPermissions", key = "#userId")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    // Conditional eviction
    @JCacheXCacheEvict(
        cacheName = "userStats",
        condition = "#user.isActive()",
        key = "#user.id"
    )
    public void updateUserActivity(User user) {
        userRepository.updateLastActivity(user.getId());
    }

    // Clear entire cache
    @JCacheXCacheEvict(cacheName = "temporaryData", allEntries = true)
    @Scheduled(fixedRate = 3600000) // Every hour
    public void clearTemporaryCache() {
        logger.info("Cleared temporary cache");
    }

    // Evict before method execution (for consistency)
    @JCacheXCacheEvict(
        cacheName = "criticalData",
        beforeInvocation = true,
        key = "#dataId"
    )
    public void updateCriticalData(String dataId, CriticalData data) {
        dataRepository.save(data);
    }
}
```

### Profile-Based Configuration

```java
@Service
public class ProductService {

    // Ultra-high performance for read-heavy workloads
    @JCacheXCacheable(cacheName = "products", profile = "READ_HEAVY")
    public Product getProduct(String id) {
        return productRepository.findById(id);
    }

    // Memory-efficient for large datasets
    @JCacheXCacheable(cacheName = "reports", profile = "MEMORY_EFFICIENT")
    public Report generateReport(String reportId) {
        return reportGenerator.generate(reportId);
    }

    // API response optimization
    @JCacheXCacheable(
        cacheName = "apiData",
        profile = "API_CACHE",
        expireAfterWrite = 15,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public ApiResponse getApiData(String endpoint) {
        return apiClient.call(endpoint);
    }

    // Machine learning workload optimization
    @JCacheXCacheable(cacheName = "mlPredictions", profile = "ML_OPTIMIZED")
    public PredictionResult predict(String modelId, InputData data) {
        return mlService.predict(modelId, data);
    }
}
```

### Application Properties Integration

```yaml
# application.yml
jcachex:
  default:
    maximumSize: 1000
    expireAfterSeconds: 1800
    enableStatistics: true

  profiles:
    users: READ_HEAVY
    sessions: SESSION_CACHE
    products: HIGH_PERFORMANCE
    reports: MEMORY_EFFICIENT

  caches:
    users:
      profile: READ_HEAVY
      maximumSize: 5000
      expireAfterSeconds: 3600

    userProfiles:
      profile: READ_HEAVY
      maximumSize: 2000
      expireAfterSeconds: 1800

    apiResponses:
      profile: API_CACHE
      maximumSize: 1000
      expireAfterSeconds: 900

    mlPredictions:
      profile: ML_OPTIMIZED
      maximumSize: 500
      expireAfterSeconds: 7200
```

### Spring Boot Auto-Configuration

```java
@SpringBootApplication
@EnableCaching
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// JCacheX auto-configuration handles:
// - Automatic CacheManager creation
// - Profile-based cache optimization
// - Metrics integration
// - Health check endpoints
```

### Testing Annotation-Based Caching

```java
@SpringBootTest
@TestProfile("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testCacheableBehavior() {
        // First call - should hit database
        User user1 = userService.findUserById("123");
        verify(userRepository).findById("123");

        // Second call - should use cache
        User user2 = userService.findUserById("123");
        verifyNoMoreInteractions(userRepository);

        assertEquals(user1, user2);
    }

    @Test
    void testCacheEviction() {
        // Cache user
        User user = userService.findUserById("123");

        // Update user (should evict cache)
        user.setName("Updated Name");
        userService.updateUser(user);

        // Next call should hit database again
        User updatedUser = userService.findUserById("123");
        verify(userRepository, times(2)).findById("123");
    }
}
```

### REST Controller Integration

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        User user = userService.findUserById(id); // Cached automatically
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        User updated = userService.updateUser(user); // Evicts cache automatically
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id); // Evicts multiple caches
        return ResponseEntity.noContent().build();
    }
}
```

### Performance Optimized Examples

#### Ultra-Low Latency
```java
// Zero-copy optimized for HFT
Cache<String, MarketData> marketData = JCacheXBuilder.forUltraLowLatency()
    .name("market-data")
    .maximumSize(100000L)
    .build();
```

#### Memory Constrained
```java
// Optimized for limited memory
Cache<String, Config> configCache = JCacheXBuilder.forMemoryConstrainedEnvironment()
    .name("config")
    .maximumSize(50L)
    .build();
```

#### Machine Learning
```java
// ML workload optimization
Cache<String, ModelResult> mlCache = JCacheXBuilder.forMachineLearning()
    .name("ml-predictions")
    .maximumSize(1000L)
    .build();
```

---

## All 12 Convenience Methods

JCacheX provides one-liner cache creation for all common use cases:

```java
// Core profiles
JCacheXBuilder.create()                          // Default profile
JCacheXBuilder.forReadHeavyWorkload()           // 80%+ reads
JCacheXBuilder.forWriteHeavyWorkload()          // 50%+ writes
JCacheXBuilder.forMemoryConstrainedEnvironment() // Limited memory
JCacheXBuilder.forHighPerformance()             // Maximum throughput

// Specialized profiles
JCacheXBuilder.forSessionStorage()              // User sessions
JCacheXBuilder.forApiResponseCaching()          // External APIs
JCacheXBuilder.forComputationCaching()          // Expensive computations

// Advanced profiles
JCacheXBuilder.forMachineLearning()             // ML workloads
JCacheXBuilder.forUltraLowLatency()            // HFT/Gaming
JCacheXBuilder.forHardwareOptimization()        // CPU-intensive
JCacheXBuilder.forDistributedCaching()          // Multi-node
```

---

## Module Structure

```
jcachex/
├── jcachex-core/        # Core caching functionality
├── jcachex-kotlin/      # Kotlin extensions and DSL
├── jcachex-spring/      # Spring Boot integration
└── examples/            # Comprehensive examples
    ├── java/           # Java examples
    ├── kotlin/         # Kotlin examples
    └── springboot/     # Spring Boot examples
```

---

## Getting Started

### Installation

#### Maven
```xml
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>2.0.1</version>
</dependency>
```

#### Gradle
```gradle
implementation 'io.github.dhruv1110:jcachex-core:2.0.1'
```

### Hello World Example

```java
import io.github.dhruv1110.jcachex.JCacheXBuilder;

public class HelloJCacheX {
    public static void main(String[] args) {
        // Create cache with one line
        var cache = JCacheXBuilder.forReadHeavyWorkload()
            .name("hello").maximumSize(1000L).build();

        // Use it
        cache.put("hello", "world");
        System.out.println(cache.get("hello")); // "world"

        // Check performance
        System.out.println("Hit rate: " + cache.stats().hitRate() * 100 + "%");
    }
}
```

---

## Advanced Features

### Distributed Caching
```java
// Distributed cache with automatic failover
Cache<String, User> distributedCache = JCacheXBuilder.forDistributedCaching()
    .name("users")
    .maximumSize(5000L)
    .build();
```

### Async Operations
```java
// Async loading with CompletableFuture
Cache<String, Data> asyncCache = JCacheXBuilder.forHighPerformance()
    .asyncLoader(key -> CompletableFuture.supplyAsync(() -> loadData(key)))
    .build();
```

### Event Listeners
```java
// Monitor cache events
Cache<String, User> monitoredCache = JCacheXBuilder.forReadHeavyWorkload()
    .listener(new CacheEventListener<String, User>() {
        @Override
        public void onEvict(String key, User value, EvictionReason reason) {
            System.out.println("Evicted: " + key + " due to " + reason);
        }
    })
    .build();
```

---

## Documentation

- **[API Reference](https://javadoc.io/doc/io.github.dhruv1110/jcachex-core)**
- **[User Guide](https://dhruv1110.github.io/jcachex/)**
- **[Examples](example/)**
- **[Performance](benchmarks/)**

---

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/dhruv1110/jcachex/issues)
- **Documentation**: [Complete documentation](https://dhruv1110.github.io/jcachex/)
- **Examples**: [Working examples](example/)

---

<div align="center">

If JCacheX helps you, **please star the repo**. Thanks!

</div>
