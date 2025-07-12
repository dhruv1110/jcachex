# Ideal Cache Definition Patterns for JCacheX

This document demonstrates the recommended patterns for defining caches in Java, Kotlin, and Spring Boot applications using JCacheX's unified design pattern approach.

## Problem Solved

Previously, JCacheX users faced these challenges:
- **Choice Overload**: 15+ cache implementations and 10+ eviction strategies
- **No Guidance**: Users didn't know which combination worked best for their use case
- **Inconsistent APIs**: Different patterns for Java, Kotlin, and Spring Boot
- **Manual Configuration**: Users needed to understand internal implementation details

## The Solution: Profile-Based Approach

JCacheX now provides a unified, profile-based approach that automatically selects the optimal cache implementation and eviction strategy based on your use case.

---

## Java Patterns

### 1. Profile-Based Creation (Recommended)

```java
import io.github.dhruv1110.jcachex.UnifiedCacheBuilder;
import io.github.dhruv1110.jcachex.profiles.CacheProfiles;

// Read-heavy workload (e.g., user profiles, reference data)
Cache<String, User> userCache = UnifiedCacheBuilder.forProfile(CacheProfiles.READ_HEAVY)
    .name("users")
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();

// API response caching
Cache<String, ApiResponse> apiCache = UnifiedCacheBuilder.forProfile(CacheProfiles.API_CACHE)
    .name("api-responses")
    .maximumSize(500L)
    .expireAfterWrite(Duration.ofMinutes(15))
    .build();

// Session storage
Cache<String, UserSession> sessionCache = UnifiedCacheBuilder.forProfile(CacheProfiles.SESSION_CACHE)
    .name("sessions")
    .maximumSize(2000L)
    .build();

// Memory-constrained environment
Cache<String, LargeObject> memoryCache = UnifiedCacheBuilder.forProfile(CacheProfiles.MEMORY_EFFICIENT)
    .name("large-objects")
    .maximumSize(50L)
    .build();

// High-performance computing
Cache<String, ComputeResult> computeCache = UnifiedCacheBuilder.forProfile(CacheProfiles.HIGH_PERFORMANCE)
    .name("compute-results")
    .maximumSize(1000L)
    .build();

// Machine learning workloads
Cache<String, ModelResult> mlCache = UnifiedCacheBuilder.forProfile(CacheProfiles.ML_OPTIMIZED)
    .name("ml-predictions")
    .maximumSize(500L)
    .build();

// Ultra-low latency requirements
Cache<String, MarketData> zeroCopyCache = UnifiedCacheBuilder.forProfile(CacheProfiles.ZERO_COPY)
    .name("market-data")
    .maximumSize(10000L)
    .build();

// CPU-intensive workloads
Cache<String, ScientificResult> hardwareCache = UnifiedCacheBuilder.forProfile(CacheProfiles.HARDWARE_OPTIMIZED)
    .name("scientific-results")
    .maximumSize(1000L)
    .build();

// Distributed environments
Cache<String, SharedData> distributedCache = UnifiedCacheBuilder.forProfile(CacheProfiles.DISTRIBUTED)
    .name("shared-data")
    .maximumSize(5000L)
    .build();
```

### 2. Smart Defaults (When Unsure)

```java
// Let JCacheX choose the best profile based on workload characteristics
Cache<String, Product> productCache = UnifiedCacheBuilder.withSmartDefaults()
    .name("products")
    .maximumSize(1000L)
    .workloadCharacteristics(WorkloadCharacteristics.builder()
        .readToWriteRatio(8.0)  // Read-heavy
        .accessPattern(WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
        .memoryConstraint(WorkloadCharacteristics.MemoryConstraint.LIMITED)
        .build())
    .build();
```

### 3. Simple Cases

```java
// Minimal configuration - uses DEFAULT profile with smart defaults
Cache<String, String> simpleCache = UnifiedCacheBuilder.create()
    .name("simple")
    .maximumSize(100L)
    .build();
```

### 4. Advanced Configuration with Async Support

```java
// Override profile settings when needed
Cache<String, Report> reportCache = UnifiedCacheBuilder.forProfile(CacheProfiles.COMPUTE_CACHE)
    .name("reports")
    .maximumSize(100L)
    .expireAfterWrite(Duration.ofHours(2))
    .loader(reportId -> generateReport(reportId))
    .asyncLoader(reportId -> CompletableFuture.supplyAsync(() -> generateReport(reportId)))
    .recordStats(true)
    .build();
```

### 5. Async Patterns for Java

```java
// Async cache operations
Cache<String, User> userCache = UnifiedCacheBuilder.forProfile(CacheProfiles.READ_HEAVY)
    .name("users")
    .maximumSize(1000L)
    .build();

// Async get operation
CompletableFuture<User> userFuture = userCache.getAsync("user123");
userFuture.thenAccept(user -> {
    if (user != null) {
        System.out.println("User found: " + user.getName());
    }
});

// Async put operation
CompletableFuture<Void> putFuture = userCache.putAsync("user456", newUser);
putFuture.thenRun(() -> System.out.println("User cached successfully"));

// Async remove operation
CompletableFuture<User> removeFuture = userCache.removeAsync("user789");
removeFuture.thenAccept(removedUser -> {
    if (removedUser != null) {
        System.out.println("Removed user: " + removedUser.getName());
    }
});

// Async batch operations
List<CompletableFuture<User>> futures = userIds.stream()
    .map(userCache::getAsync)
    .collect(Collectors.toList());

CompletableFuture<List<User>> allUsers = CompletableFuture.allOf(
    futures.toArray(new CompletableFuture[0]))
    .thenApply(v -> futures.stream()
        .map(CompletableFuture::join)
        .filter(Objects::nonNull)
        .collect(Collectors.toList()));

// Async loader with timeout
Cache<String, ApiResponse> apiCache = UnifiedCacheBuilder.forProfile(CacheProfiles.API_CACHE)
    .name("api-responses")
    .maximumSize(500L)
    .asyncLoader(url -> CompletableFuture.supplyAsync(() -> {
        try {
            return apiClient.call(url);
        } catch (Exception e) {
            throw new RuntimeException("API call failed", e);
        }
    }, executor).orTimeout(5, TimeUnit.SECONDS))
    .build();
```

---

## Kotlin Patterns

### 1. Profile-Based DSL (Recommended)

```kotlin
import io.github.dhruv1110.jcachex.kotlin.*
import io.github.dhruv1110.jcachex.profiles.CacheProfiles

// Read-heavy workload with Kotlin DSL
val userCache = createReadHeavyCache<String, User> {
    name("users")
    maximumSize(1000L)
    expireAfterWrite(Duration.ofMinutes(30))
    recordStats(true)
}

// API response caching
val apiCache = createApiCache<String, ApiResponse> {
    name("api-responses")
    maximumSize(500L)
    expireAfterWrite(Duration.ofMinutes(15))
}

// Memory-efficient for large datasets
val memoryCache = createMemoryEfficientCache<String, LargeObject> {
    name("large-objects")
    maximumSize(50L)
    recordStats(false)  // Minimize overhead
}

// High-performance cache
val performanceCache = createHighPerformanceCache<String, ComputeResult> {
    name("compute-results")
    maximumSize(1000L)
    loader { key -> performExpensiveComputation(key) }
}

// Machine learning optimized cache
val mlCache = createMLOptimizedCache<String, ModelResult> {
    name("ml-predictions")
    maximumSize(500L)
    loader { input -> mlModel.predict(input) }
}

// Zero-copy cache for ultra-low latency
val zeroCopyCache = createZeroCopyCache<String, MarketData> {
    name("market-data")
    maximumSize(10000L)
    recordStats(false) // Minimize overhead
}

// Hardware optimized cache
val hardwareCache = createHardwareOptimizedCache<String, ScientificResult> {
    name("scientific-results")
    maximumSize(1000L)
    loader { input -> scientificComputation(input) }
}

// Distributed cache for cluster environments
val distributedCache = createDistributedCache<String, SharedData> {
    name("shared-data")
    maximumSize(5000L)
    expireAfterWrite(Duration.ofMinutes(30))
}
```

### 2. Smart Cache with Workload Characteristics

```kotlin
// Smart cache with detailed workload description
val smartCache = createSmartCache<String, Product> {
    name("products")
    maximumSize(1000L)

    workloadCharacteristics {
        readToWriteRatio(8.0)
        accessPattern(WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
        memoryConstraint(WorkloadCharacteristics.MemoryConstraint.LIMITED)
        concurrencyLevel(WorkloadCharacteristics.ConcurrencyLevel.HIGH)
        expectedSize(1000L)
        hitRateExpectation(0.85)
    }
}
```

### 3. Generic Profile-Based Creation

```kotlin
// Using specific profiles directly
val sessionCache = createCacheWithProfile(CacheProfiles.SESSION_CACHE) {
    name("sessions")
    maximumSize(2000L)
    expireAfterAccess(Duration.ofMinutes(30))
}

val computeCache = createCacheWithProfile(CacheProfiles.COMPUTE_CACHE) {
    name("computations")
    maximumSize(500L)
    expireAfterWrite(Duration.ofHours(1))
    loader { input -> performHeavyComputation(input) }
}
```

### 4. Coroutine Integration

```kotlin
// Coroutine-friendly cache operations
val cache = createReadHeavyCache<String, User> {
    name("users")
    maximumSize(1000L)
}

// Suspend function for cache operations
suspend fun getUser(id: String): User? {
    return cache.getOrPut(id) {
        userService.loadUser(id)  // Suspending function
    }
}

// Async operations
val futureUser = cache.getDeferred("user123", GlobalScope)
```

---

## Spring Boot Patterns

### 1. Annotation-Based with Profiles (Recommended)

```java
@Service
public class UserService {

    // Read-heavy workload - users are read frequently
    @JCacheXCacheable(value = "users", profile = "READ_HEAVY")
    public User findUserById(String id) {
        return userRepository.findById(id);
    }

    // API response caching with TTL
    @JCacheXCacheable(
        value = "api-data",
        profile = "API_CACHE",
        expireAfterWrite = 15,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public ApiResponse getApiData(String endpoint) {
        return apiClient.call(endpoint);
    }

    // Memory-efficient for large datasets
    @JCacheXCacheable(value = "large-reports", profile = "MEMORY_EFFICIENT")
    public Report generateLargeReport(String reportId) {
        return reportGenerator.generate(reportId);
    }

    // Session storage
    @JCacheXCacheable(value = "sessions", profile = "SESSION_CACHE")
    public UserSession getSession(String sessionId) {
        return sessionStore.load(sessionId);
    }

    // High-performance computing
    @JCacheXCacheable(value = "computations", profile = "HIGH_PERFORMANCE")
    public ComputeResult performComputation(String input) {
        return computeEngine.process(input);
    }

    // Machine learning workloads
    @JCacheXCacheable(value = "ml-predictions", profile = "ML_OPTIMIZED")
    public ModelResult predictWithML(String input) {
        return mlModel.predict(input);
    }

    // Ultra-low latency requirements
    @JCacheXCacheable(value = "market-data", profile = "ZERO_COPY")
    public MarketData getMarketData(String symbol) {
        return marketDataService.getLatestData(symbol);
    }

    // CPU-intensive workloads
    @JCacheXCacheable(value = "scientific-results", profile = "HARDWARE_OPTIMIZED")
    public ScientificResult performScientificComputation(String input) {
        return scientificEngine.compute(input);
    }

    // Distributed environments
    @JCacheXCacheable(value = "shared-data", profile = "DISTRIBUTED")
    public SharedData getSharedData(String key) {
        return distributedDataService.load(key);
    }
}
```

### 2. Configuration Properties

#### application.yml
```yaml
jcachex:
  # Global defaults
  default:
    profile: DEFAULT
    maximumSize: 1000
    expireAfterSeconds: 1800  # 30 minutes
    enableStatistics: true

  # Cache-specific profiles and settings
  caches:
    users:
      profile: READ_HEAVY
      maximumSize: 5000
      expireAfterSeconds: 3600  # 1 hour

    api-data:
      profile: API_CACHE
      maximumSize: 2000
      expireAfterSeconds: 900   # 15 minutes

    sessions:
      profile: SESSION_CACHE
      maximumSize: 10000
      expireAfterAccessSeconds: 1800  # 30 minutes

    large-reports:
      profile: MEMORY_EFFICIENT
      maximumSize: 100
      expireAfterSeconds: 7200  # 2 hours

    computations:
      profile: HIGH_PERFORMANCE
      maximumSize: 1000
      expireAfterSeconds: 3600  # 1 hour

    ml-predictions:
      profile: ML_OPTIMIZED
      maximumSize: 500
      expireAfterSeconds: 1800  # 30 minutes

    market-data:
      profile: ZERO_COPY
      maximumSize: 10000
      expireAfterSeconds: 300   # 5 minutes

    scientific-results:
      profile: HARDWARE_OPTIMIZED
      maximumSize: 1000
      expireAfterSeconds: 7200  # 2 hours

    shared-data:
      profile: DISTRIBUTED
      maximumSize: 5000
      expireAfterSeconds: 1800  # 30 minutes
```

#### application.properties
```properties
# Global defaults
jcachex.default.profile=DEFAULT
jcachex.default.maximumSize=1000
jcachex.default.expireAfterSeconds=1800
jcachex.default.enableStatistics=true

# Cache-specific configurations
jcachex.caches.users.profile=READ_HEAVY
jcachex.caches.users.maximumSize=5000
jcachex.caches.users.expireAfterSeconds=3600

jcachex.caches.api-data.profile=API_CACHE
jcachex.caches.api-data.maximumSize=2000
jcachex.caches.api-data.expireAfterSeconds=900

jcachex.caches.sessions.profile=SESSION_CACHE
jcachex.caches.sessions.maximumSize=10000
jcachex.caches.sessions.expireAfterAccessSeconds=1800
```

### 3. Programmatic Configuration

```java
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        return new JCacheXCacheManager();
    }

    @Bean
    public Cache<String, User> userCache() {
        return UnifiedCacheBuilder.forProfile(CacheProfiles.READ_HEAVY)
            .name("users")
            .maximumSize(5000L)
            .expireAfterWrite(Duration.ofHours(1))
            .recordStats(true)
            .build();
    }

    @Bean
    public Cache<String, ApiResponse> apiCache() {
        return UnifiedCacheBuilder.forProfile(CacheProfiles.API_CACHE)
            .name("api-responses")
            .maximumSize(2000L)
            .expireAfterWrite(Duration.ofMinutes(15))
            .build();
    }
}
```

### 4. Advanced Spring Integration with Async Support

```java
@Service
public class AdvancedCacheService {

    @Autowired
    private JCacheXCacheManager cacheManager;

    public void setupDynamicCaches() {
        // Get underlying JCacheX cache for advanced operations
        Cache<String, Object> advancedCache = cacheManager.getNativeCache("advanced");

        // Use async operations
        CompletableFuture<User> futureUser = advancedCache.getAsync("user123")
            .thenApply(obj -> (User) obj);

        // Batch operations
        Map<String, User> users = advancedCache.getAll(Arrays.asList("user1", "user2", "user3"));

        // Statistics
        CacheStats stats = advancedCache.stats();
        logger.info("Cache hit rate: {}", stats.hitRate());
    }

    @Async
    @JCacheXCacheable(value = "async-computations", profile = "COMPUTE_CACHE")
    public CompletableFuture<ComputationResult> performAsyncComputation(String taskId) {
        return CompletableFuture.supplyAsync(() -> {
            // Expensive computation
            return computeEngine.process(taskId);
        });
    }

    @Async
    @JCacheXCacheable(value = "async-api-calls", profile = "API_CACHE")
    public CompletableFuture<ApiResponse> fetchDataAsync(String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return apiClient.call(endpoint);
            } catch (Exception e) {
                throw new RuntimeException("API call failed", e);
            }
        });
    }

    // Reactive-style async operations
    public CompletableFuture<List<User>> loadUsersAsync(List<String> userIds) {
        Cache<String, User> userCache = cacheManager.getNativeCache("users");

        List<CompletableFuture<User>> futures = userIds.stream()
            .map(userCache::getAsync)
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    // Async cache warming
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public CompletableFuture<Void> warmCaches() {
        return CompletableFuture.runAsync(() -> {
            Cache<String, User> userCache = cacheManager.getNativeCache("users");

            // Pre-load popular users
            List<String> popularUserIds = Arrays.asList("user1", "user2", "user3");
            popularUserIds.forEach(id -> {
                userCache.putAsync(id, userService.loadUser(id));
            });
        });
    }
}
```

---

## Available Profiles

| Profile | Use Case | Cache Implementation | Eviction Strategy | Key Features |
|---------|----------|---------------------|-------------------|--------------|
| **DEFAULT** | General-purpose | OptimizedCache | TinyWindowLFU | Balanced performance |
| **READ_HEAVY** | 80%+ reads | ReadOnlyOptimizedCache | Enhanced LFU | Lock-free reads |
| **WRITE_HEAVY** | 50%+ writes | WriteHeavyOptimizedCache | Enhanced LRU | Async writes |
| **MEMORY_EFFICIENT** | Constrained memory | AllocationOptimizedCache | LRU | Minimal allocation |
| **HIGH_PERFORMANCE** | Maximum throughput | JITOptimizedCache | TinyWindowLFU | JIT optimizations |
| **SESSION_CACHE** | User sessions | DefaultCache | LRU | Time-based expiration |
| **API_CACHE** | API responses | OptimizedCache | TinyWindowLFU | Short TTL |
| **COMPUTE_CACHE** | Expensive computations | OptimizedCache | Enhanced LFU | Long-term caching |
| **ML_OPTIMIZED** | ML workloads | MLOptimizedCache | Enhanced LRU | Predictive prefetching |
| **ZERO_COPY** | Ultra-low latency | ZeroCopyOptimizedCache | LRU | Direct memory buffers |
| **HARDWARE_OPTIMIZED** | CPU-specific | HardwareOptimizedCache | Enhanced LRU | SIMD operations |
| **DISTRIBUTED** | Cluster environments | DefaultDistributedCache | Enhanced LRU | Network-aware caching |

---

## Migration Guide

### From Old Pattern (Complex)
```java
// Old way - complex and error-prone
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .maximumSize(1000L)
    .evictionStrategy(new WindowTinyLFUEvictionStrategy<>(1000L))
    .frequencySketchType(FrequencySketchType.OPTIMIZED)
    .expireAfterWrite(Duration.ofMinutes(30))
    .recordStats(true)
    .build();
Cache<String, User> cache = new OptimizedCache<>(config);
```

### To New Pattern (Simple)
```java
// New way - simple and optimized
Cache<String, User> cache = UnifiedCacheBuilder.forProfile(CacheProfiles.READ_HEAVY)
    .name("users")
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();
```

### Benefits of the New Approach

1. **Simplicity**: Choose by use case, not implementation details
2. **Optimal Performance**: Each profile is expertly tuned
3. **Consistency**: Same patterns work across Java, Kotlin, and Spring Boot
4. **Smart Defaults**: Automatic selection of best implementation
5. **Future-Proof**: Profiles will be updated with new optimizations

---

## Best Practices

### 1. Choose the Right Profile
- **READ_HEAVY**: User profiles, reference data, configuration
- **WRITE_HEAVY**: Metrics, logs, frequently updated data
- **API_CACHE**: External API responses, web service calls
- **SESSION_CACHE**: User sessions, temporary data
- **COMPUTE_CACHE**: Expensive calculations, ML model results
- **MEMORY_EFFICIENT**: Embedded systems, constrained environments
- **HIGH_PERFORMANCE**: High-throughput systems, real-time processing
- **ML_OPTIMIZED**: Machine learning workloads, predictive analytics, adaptive systems
- **ZERO_COPY**: Ultra-low latency requirements, high-frequency trading, real-time processing
- **HARDWARE_OPTIMIZED**: CPU-intensive workloads, scientific computing, parallel processing
- **DISTRIBUTED**: Microservices, cluster environments, multi-node applications

### 2. Configuration Guidelines
- Set appropriate `maximumSize` based on memory constraints
- Use `expireAfterWrite` for data freshness requirements
- Use `expireAfterAccess` for session-like data
- Enable `recordStats` for monitoring and tuning

### 3. Monitoring and Tuning
```java
// Monitor cache performance
CacheStats stats = cache.stats();
System.out.println("Hit rate: " + stats.hitRate());
System.out.println("Miss rate: " + stats.missRate());
System.out.println("Eviction count: " + stats.evictionCount());

// Tune based on metrics
if (stats.hitRate() < 0.8) {
    // Consider increasing cache size or changing profile
}
```

### 4. Testing
```java
@Test
public void testCacheProfile() {
    Cache<String, String> cache = UnifiedCacheBuilder.forProfile(CacheProfiles.READ_HEAVY)
        .maximumSize(100L)
        .build();

    cache.put("key", "value");
    assertEquals("value", cache.get("key"));

    // Verify profile selection
    assertTrue(cache instanceof ReadOnlyOptimizedCache);
}
```

This unified approach eliminates the complexity of manual cache configuration while providing optimal performance for your specific use case.
