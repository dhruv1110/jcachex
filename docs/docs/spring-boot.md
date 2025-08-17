---
id: spring-boot
title: Spring Boot Integration
sidebar_label: Spring Boot Integration
description: Integrate JCacheX with Spring Boot applications
---

# Spring Boot Integration

JCacheX provides seamless integration with Spring Boot, offering annotations, auto-configuration, and Spring-native caching support.

## Quick Start

### 1. Add Dependencies

```xml
<dependencies>
    <!-- Core JCacheX -->
    <dependency>
        <groupId>io.github.dhruv1110</groupId>
        <artifactId>jcachex-spring</artifactId>
        <version>2.0.1</version>
    </dependency>

    <!-- Spring Boot Starter (optional) -->
    <dependency>
        <groupId>io.github.dhruv1110</groupId>
        <artifactId>jcachex-spring-boot-starter</artifactId>
        <version>2.0.1</version>
    </dependency>
</dependencies>
```

### 2. Enable Caching

```java
@SpringBootApplication
@EnableCaching
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. Use JCacheX Annotations

```java
@Service
public class UserService {

    @JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY")
    public User findUserById(String id) {
        return userRepository.findById(id);
    }

    @JCacheXCacheEvict(cacheName = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

## Configuration

### Application Properties

Configure JCacheX caches in your `application.yml`:

```yaml
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

### Java Configuration

Configure caches programmatically:

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        JCacheXCacheManager cacheManager = new JCacheXCacheManager();

        // Configure default settings
        cacheManager.setDefaultProfile(ProfileName.READ_HEAVY);
        cacheManager.setDefaultMaximumSize(1000L);
        cacheManager.setDefaultExpireAfterWrite(Duration.ofMinutes(30));

        return cacheManager;
    }

    @Bean
    public Cache<String, User> userCache() {
        return JCacheXBuilder.forReadHeavyWorkload()
            .name("users")
            .maximumSize(5000L)
            .expireAfterWrite(Duration.ofHours(1))
            .build();
    }

    @Bean
    public Cache<String, Product> productCache() {
        return JCacheXBuilder.forReadHeavyWorkload()
            .name("products")
            .maximumSize(10000L)
            .expireAfterWrite(Duration.ofHours(2))
            .build();
    }
}
```

## JCacheX Annotations

### @JCacheXCacheable

Cache method results with JCacheX-specific optimizations:

```java
@Service
public class ProductService {

    // Basic caching with profile
    @JCacheXCacheable(cacheName = "products", profile = "READ_HEAVY")
    public Product getProduct(String id) {
        return productRepository.findById(id);
    }

    // Custom expiration and size
    @JCacheXCacheable(
        cacheName = "productDetails",
        expireAfterWrite = 30,
        expireAfterWriteUnit = TimeUnit.MINUTES,
        maximumSize = 5000
    )
    public ProductDetail getProductDetail(String id) {
        return buildProductDetail(id);
    }

    // Conditional caching
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

### @JCacheXCacheEvict

Evict cache entries with advanced options:

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

    // Evict before method execution
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

### @JCacheXCachePut

Update cache entries:

```java
@Service
public class DataService {

    @JCacheXCachePut(cacheName = "data", key = "#data.id")
    public Data createData(Data data) {
        return dataRepository.save(data);
    }

    @JCacheXCachePut(
        cacheName = "processedData",
        key = "#data.id",
        condition = "#result != null"
    )
    public ProcessedData processData(Data data) {
        return dataProcessor.process(data);
    }
}
```

## Standard Spring Annotations

JCacheX also supports standard Spring caching annotations:

```java
@Service
public class MixedCacheService {

    // JCacheX-specific annotation
    @JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY")
    public User findUserById(String id) {
        return userRepository.findById(id);
    }

    // Standard Spring annotation (also supported)
    @Cacheable("sessions")
    public UserSession createSession(String userId) {
        return new UserSession(userId, System.currentTimeMillis());
    }

    @CacheEvict(value = "sessions", key = "#sessionId")
    public void invalidateSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }

    @CachePut(value = "userStats", key = "#user.id")
    public UserStats updateUserStats(User user) {
        return statsCalculator.calculate(user);
    }
}
```

## Auto-Configuration

### Spring Boot Starter

The `jcachex-spring-boot-starter` provides automatic configuration:

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

### Custom Auto-Configuration

Extend the auto-configuration:

```java
@Configuration
@ConditionalOnClass(JCacheXCacheManager.class)
@EnableConfigurationProperties(JCacheXProperties.class)
public class CustomJCacheXAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager(JCacheXProperties properties) {
        JCacheXCacheManager cacheManager = new JCacheXCacheManager();

        // Apply custom configuration
        if (properties.getDefaultProfile() != null) {
            cacheManager.setDefaultProfile(properties.getDefaultProfile());
        }

        return cacheManager;
    }

    @Bean
    @ConditionalOnProperty(name = "jcachex.metrics.enabled", havingValue = "true")
    public CacheMetrics cacheMetrics(CacheManager cacheManager) {
        return new CacheMetrics(cacheManager);
    }
}
```

## Testing

### Unit Testing

Test your cached services:

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

### Integration Testing

Test with embedded cache:

```java
@TestConfiguration
public class TestCacheConfig {

    @Bean
    public CacheManager testCacheManager() {
        JCacheXCacheManager cacheManager = new JCacheXCacheManager();

        // Use in-memory cache for testing
        cacheManager.setDefaultProfile(ProfileName.HIGH_PERFORMANCE);
        cacheManager.setDefaultMaximumSize(100L);

        return cacheManager;
    }
}
```

## REST Controller Integration

### Controller with Caching

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

## Performance Monitoring

### Cache Statistics

Monitor cache performance:

```java
@Component
public class CacheMonitor {

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(fixedRate = 60000) // Every minute
    public void logCacheStatistics() {
        if (cacheManager instanceof JCacheXCacheManager) {
            JCacheXCacheManager jcachexManager = (JCacheXCacheManager) cacheManager;

            jcachexManager.getCacheNames().forEach(cacheName -> {
                Cache<?, ?> cache = jcachexManager.getCache(cacheName);
                if (cache instanceof io.github.dhruv1110.jcachex.Cache) {
                    io.github.dhruv1110.jcachex.Cache<?, ?> jcachex =
                        (io.github.dhruv1110.jcachex.Cache<?, ?>) cache;

                    CacheStats stats = jcachex.stats();
                    logger.info("Cache {}: Hit Rate: {:.2f}%, Size: {}",
                        cacheName, stats.hitRate() * 100, jcachex.size());
                }
            });
        }
    }
}
```

### Health Checks

JCacheX provides health check endpoints:

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,caches
  endpoint:
    health:
      show-details: always
```

## Best Practices

### 1. Profile Selection

Choose appropriate profiles for your use cases:

```java
// Read-heavy data (product catalogs, reference data)
@JCacheXCacheable(cacheName = "products", profile = "READ_HEAVY")

// Write-heavy data (logging, analytics)
@JCacheXCacheable(cacheName = "logs", profile = "WRITE_HEAVY")

// API responses
@JCacheXCacheable(cacheName = "api", profile = "API_CACHE")

// User sessions
@JCacheXCacheable(cacheName = "sessions", profile = "SESSION_CACHE")
```

### 2. Cache Naming

Use consistent cache naming conventions:

```java
// Good: Descriptive and consistent
@JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY")
@JCacheXCacheable(cacheName = "userProfiles", profile = "READ_HEAVY")
@JCacheXCacheable(cacheName = "userPermissions", profile = "READ_HEAVY")

// Avoid: Generic names
@JCacheXCacheable(cacheName = "cache1", profile = "READ_HEAVY")
@JCacheXCacheable(cacheName = "data", profile = "READ_HEAVY")
```

### 3. Key Generation

Use meaningful cache keys:

```java
// Good: Descriptive keys
@JCacheXCacheable(cacheName = "products", key = "#productId")
@JCacheXCacheable(cacheName = "userProducts", key = "#userId + '_' + #category")

// Avoid: Complex or unclear keys
@JCacheXCacheable(cacheName = "data", key = "#root.method.name + #p0")
```

## Next Steps

- **[Examples](/docs/examples/spring-boot-examples)** - More Spring Boot examples
- **[Configuration](/docs/spring-boot/configuration)** - Advanced configuration options
- **[Testing](/docs/spring-boot/testing)** - Testing strategies and examples
- **[Performance](/docs/performance)** - Performance optimization tips

---

**Ready to supercharge your Spring Boot application?** Check out our [examples section](/docs/examples/spring-boot-examples) for more patterns and use cases!
