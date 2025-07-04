# JCacheX Spring Boot Example

This example demonstrates **JCacheX** integration with **Spring Boot**, showcasing production-ready caching patterns for web applications.

## 🎯 What This Example Shows

- **🌱 Spring Integration**: Seamless `@Cacheable` annotation support
- **🔒 Rate Limiting**: API throttling with Spring interceptors
- **📊 Cache Management**: Spring Actuator integration for monitoring
- **⚙️ Auto-Configuration**: Zero-config setup with sensible defaults
- **🧪 Testing**: Spring Boot testing with cache verification
- **🚀 Production**: Health checks, metrics, and observability

## 🚀 Running the Example

```bash
# From the example/springboot directory
./gradlew bootRun

# Application starts on http://localhost:8080
```

### 🌐 API Endpoints

```bash
# User management (demonstrates caching)
GET  /users/{id}           # Cached user lookup
POST /users                # Create user (cache warming)
PUT  /users/{id}           # Update user (cache eviction)
DELETE /users/{id}         # Delete user (cache eviction)

# Cache management
GET  /actuator/caches          # Cache statistics
POST /actuator/caches/{name}/clear # Clear specific cache
GET  /actuator/health          # Health check (includes cache health)
GET  /actuator/metrics         # Metrics (includes cache metrics)
```

## 📋 Key Features Demonstrated

### 1. **Annotation-Based Caching**
- **@Cacheable**: Automatic result caching
- **@CacheEvict**: Cache invalidation on updates
- **@CachePut**: Cache warming strategies
- **@Caching**: Complex caching scenarios

### 2. **Production Features**
- **Health Checks**: Cache health indicators
- **Metrics Integration**: Micrometer metrics export
- **Configuration Properties**: Externalized cache config
- **Profiles**: Different cache configs per environment

### 3. **Testing Strategies**
- **Integration Tests**: Full Spring context testing
- **Cache Verification**: Asserting cache behavior
- **Mock Services**: Testing cache miss scenarios
- **Performance Tests**: Load testing with caching

## 🎓 Learning Path

1. **Start with Application.kt**: See auto-configuration in action
2. **Study UserController.kt**: Understand annotation-based caching
3. **Review Tests**: Learn Spring Boot cache testing

## 🔧 Configuration Examples

### Spring Boot Integration
```kotlin
@SpringBootApplication
@EnableJCacheX
class Application

@RestController
class UserController {
    @GetMapping("/users/{id}")
    @Cacheable("users")
    fun getUser(@PathVariable id: String): User {
        // Simulate database call
        Thread.sleep(1000)
        return User(id, "User $id")
    }
}
```

## 📈 Expected Performance

This example demonstrates:
- **Cache Hit Rate**: 90%+ for user lookups
- **Response Time**: < 5ms for cached responses vs 1000ms+ for simulated database
- **Memory Usage**: Configurable cache sizes with monitoring

## 🔗 Related Documentation

- [Main Documentation](../../README.md): Complete JCacheX guide
- [Java Example](../java/): Core Java patterns
- [Kotlin Example](../kotlin/): Coroutines and DSL examples
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/): Official Spring Boot docs

---

💡 **Pro Tip**: Use Spring profiles to have different cache configurations for development, testing, and production environments.
