# JCacheX Spring Boot Example

This example demonstrates **JCacheX integration with Spring Boot**, showcasing how to use JCacheX alongside Spring's standard caching annotations.

## 🎯 What This Example Shows

- **🌱 Spring Boot Integration**: Using JCacheX with Spring Boot applications
- **📦 Dependency Injection**: Injecting JCacheX cache instances as Spring beans
- **🔧 Configuration Management**: Setting up cache instances through Spring configuration
- **🎧 REST API Caching**: Caching REST endpoint responses
- **📊 Cache Statistics**: Monitoring cache performance through REST endpoints
- **⚡ Hybrid Approach**: Using both Spring's `@Cacheable` and direct JCacheX operations

## 🚀 Running the Example

```bash
# From the project root directory
./gradlew :example:springboot:bootRun

# Or compiling first
./gradlew :example:springboot:build
java -jar example/springboot/build/libs/springboot-1.0-SNAPSHOT.jar
```

## 📋 Key Features Demonstrated

### 1. **Spring Configuration**
- **Cache Bean**: JCacheX cache configured as a Spring bean
- **Auto-Configuration**: Spring Boot auto-configuration integration
- **Dependency Injection**: Cache injected into REST controllers

### 2. **REST API Endpoints**
- **User Retrieval**: `/users/{id}` - Cached user lookup with 1-second delay simulation
- **User Profile**: `/users/{id}/profile` - Profile data with 0.5-second delay simulation
- **Cache Statistics**: `/stats` - Real-time cache performance metrics

### 3. **Caching Strategy**
- **Spring Integration**: Uses Spring's `@Cacheable` annotation
- **JCacheX Direct Access**: Direct cache operations for custom logic
- **Performance Monitoring**: Hit rate, miss rate, and operation counts

## 📖 Code Structure

```
src/main/kotlin/
└── io/github/dhruv1110/jcachex/example/springboot/
    └── Application.kt               # Complete Spring Boot application
```

## 🎓 Learning Path

1. **Start with Application.kt**: See complete Spring Boot integration
2. **Study Configuration**: Learn how to configure JCacheX as Spring beans
3. **Explore REST Endpoints**: Understand caching in web applications
4. **Review Statistics**: See how to monitor cache performance

## 🔧 Configuration Examples

### Cache Bean Configuration
```kotlin
@Configuration
class CacheConfiguration {

    @Bean
    fun userCache(): Cache<String, User> {
        val config = CacheConfig.builder<String, User>()
            .maximumSize(1000L)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats(true)
            .build()
        return DefaultCache(config)
    }
}
```

### REST Controller with Caching
```kotlin
@RestController
class UserController(private val userCache: Cache<String, User>) {

    @GetMapping("/users/{id}")
    @Cacheable("users")
    fun getUser(@PathVariable id: String): User {
        // Check JCacheX cache first
        val cachedUser = userCache.get(id)
        if (cachedUser != null) return cachedUser

        // Simulate database call
        Thread.sleep(1000)
        val user = User(id, "User $id")

        // Store in JCacheX cache
        userCache.put(id, user)
        return user
    }
}
```

### Statistics Monitoring
```kotlin
@GetMapping("/stats")
fun getCacheStats(): Map<String, Any> {
    val stats = userCache.stats()
    return mapOf(
        "size" to userCache.size(),
        "hitRate" to stats.hitRate(),
        "hitCount" to stats.hitCount(),
        "missCount" to stats.missCount()
    )
}
```

## 🌐 Testing the API

Once the application is running (default port 8080), test these endpoints:

### Create and Cache Users
```bash
# First call (slow - simulates database)
curl http://localhost:8080/users/123
# Response: {"id":"123","name":"User 123"}

# Second call (fast - cached)
curl http://localhost:8080/users/123
# Response: {"id":"123","name":"User 123"} (instant)
```

### User Profiles
```bash
# Get user profile (0.5s delay)
curl http://localhost:8080/users/123/profile
# Response: {"userId":"123","displayName":"Profile for User 123","email":"user123@example.com"}
```

### Cache Statistics
```bash
# Check cache performance
curl http://localhost:8080/stats
# Response: {"size":1,"hitRate":0.5,"hitCount":1,"missCount":1}
```

## 📈 Expected Performance

This example demonstrates:
- **First Request**: ~1000ms (simulated database call)
- **Cached Requests**: < 5ms (from cache)
- **Cache Hit Rate**: Improves with repeated requests
- **Memory Usage**: ~64 bytes per cached user

## 🔧 Spring Boot Features

### Application Properties
```yaml
# Optional: Configure logging to see cache operations
logging:
  level:
    io.github.dhruv1110.jcachex: DEBUG

# Server configuration
server:
  port: 8080
```

### Health Check Integration
```bash
# Check application health (if actuator is enabled)
curl http://localhost:8080/actuator/health
```

## 🔗 Related Documentation

- [Main Documentation](../../README.md): Complete JCacheX guide
- [Java Example](../java/): Core Java patterns
- [Kotlin Example](../kotlin/): Kotlin extensions and coroutines
- [Spring Boot Documentation](https://spring.io/projects/spring-boot): Spring Boot reference

---

💡 **Pro Tip**: This example shows how JCacheX can complement Spring's caching annotations. You can use Spring's declarative caching for simple scenarios and JCacheX directly for complex caching logic that requires fine-grained control.
