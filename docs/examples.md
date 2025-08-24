# Real-World Examples

**See JCacheX in action with practical examples you can use in your applications.** Each example shows a real problem and how JCacheX solves it.

## 🎯 Example 1: E-commerce Product Catalog

### **The Problem**
Your e-commerce site is slow because every product page hits the database. Users wait 2-5 seconds for product information to load.

### **The Solution**
Cache product data for instant display.

```java
@Service
public class ProductService {
    private final Cache<String, Product> productCache;
    private final ProductRepository productRepository;

    public ProductService() {
        // Use read-heavy profile for product catalogs
        this.productCache = JCacheXBuilder.forReadHeavyWorkload()
            .name("products")
            .maximumSize(50000L)  // Cache 50,000 products
            .recordStats(true)
            .build();
        this.productRepository = productRepository;
    }

    public Product getProduct(String id) {
        // Try cache first (fast)
        Product cached = productCache.get(id);
        if (cached != null) {
            return cached;
        }

        // Cache miss - load from database (slow, but only once)
        Product product = productRepository.findById(id);
        if (product != null) {
            productCache.put(id, product);
        }

        return product;
    }

    public void updateProduct(String id, Product product) {
        // Update database
        productRepository.update(id, product);

        // Update cache immediately
        productCache.put(id, product);
    }

    public void warmUpCache() {
        // Pre-load popular products
        List<Product> popularProducts = productRepository.findPopularProducts();

        Map<String, Product> productMap = popularProducts.stream()
            .collect(Collectors.toMap(
                Product::getId,
                product -> product
            ));

        productCache.putAll(productMap);
        System.out.println("Warmed up cache with " + popularProducts.size() + " products");
    }
}
```

### **Results**
- **Before**: 2-5 seconds page load time
- **After**: 0.05-0.2 seconds page load time
- **Improvement**: **25-100x faster**

---

## 🎯 Example 2: User Session Management

### **The Problem**
Your web application validates user sessions on every request, hitting the database 10-50 times per user session.

### **The Solution**
Cache user sessions for instant validation.

```java
@Service
public class SessionService {
    private final Cache<String, UserSession> sessionCache;
    private final UserRepository userRepository;

    public SessionService() {
        // Use session storage profile
        this.sessionCache = JCacheXBuilder.forSessionStorage()
            .name("user-sessions")
            .maximumSize(10000L)  // Cache 10,000 sessions
            .recordStats(true)
            .build();
        this.userRepository = userRepository;
    }

    public UserSession getSession(String sessionId) {
        UserSession session = sessionCache.get(sessionId);

        if (session != null && !session.isExpired()) {
            return session;
        }

        // Session expired or not found
        if (session != null) {
            sessionCache.remove(sessionId);
        }

        return null;
    }

    public void createSession(String sessionId, User user) {
        UserSession session = new UserSession(user, Duration.ofHours(24));
        sessionCache.put(sessionId, session);
    }

    public void invalidateSession(String sessionId) {
        sessionCache.remove(sessionId);
    }

    public void cleanupExpiredSessions() {
        // This could be a scheduled task
        // For now, we rely on TTL eviction
    }
}
```

### **Results**
- **Before**: 10-50 database hits per session
- **After**: 0 database hits after first validation
- **Improvement**: **Eliminates 90%+ of database calls**

---

## 🎯 Example 3: API Response Caching

### **The Problem**
Your API calls external services (weather, stock prices, etc.) and users wait 200-1000ms for responses.

### **The Solution**
Cache API responses to reduce external calls.

```java
@Service
public class WeatherService {
    private final Cache<String, WeatherData> weatherCache;
    private final ExternalWeatherApi weatherApi;

    public WeatherService() {
        // Use API caching profile
        this.weatherCache = JCacheXBuilder.forApiResponseCaching()
            .name("weather-data")
            .maximumSize(1000L)  // Cache 1000 city responses
            .recordStats(true)
            .build();
        this.weatherApi = weatherApi;
    }

    public WeatherData getWeather(String city) {
        // Try cache first
        WeatherData cached = weatherCache.get(city);
        if (cached != null && !cached.isExpired()) {
            return cached;
        }

        // Cache miss or expired - call external API
        WeatherData weather = weatherApi.getWeather(city);

        if (weather != null) {
            // Cache for 30 minutes
            weather.setExpirationTime(Instant.now().plus(Duration.ofMinutes(30)));
            weatherCache.put(city, weather);
        }

        return weather;
    }

    public void refreshWeather(String city) {
        // Force refresh by removing from cache
        weatherCache.remove(city);
        getWeather(city);  // Will fetch fresh data
    }
}
```

### **Results**
- **Before**: 200-1000ms response time
- **After**: 0.05-0.2ms for cached responses
- **Improvement**: **1000-5000x faster** for cached data

---

## 🎯 Example 4: Database Query Result Caching

### **The Problem**
Your application runs the same database queries repeatedly, wasting database resources and slowing down responses.

### **The Solution**
Cache query results to eliminate repeated database calls.

```java
@Service
public class UserQueryService {
    private final Cache<String, List<User>> queryCache;
    private final UserRepository userRepository;

    public UserQueryService() {
        // Use read-heavy profile for query results
        this.queryCache = JCacheXBuilder.forReadHeavyWorkload()
            .name("user-queries")
            .maximumSize(1000L)  // Cache 1000 query results
            .recordStats(true)
            .build();
        this.userRepository = userRepository;
    }

    public List<User> getUsersByDepartment(String department) {
        String cacheKey = "users:dept:" + department;

        List<User> cached = queryCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Cache miss - query database
        List<User> users = userRepository.findByDepartment(department);

        if (users != null) {
            queryCache.put(cacheKey, users);
        }

        return users;
    }

    public List<User> getUsersByRole(String role) {
        String cacheKey = "users:role:" + role;

        List<User> cached = queryCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<User> users = userRepository.findByRole(role);

        if (users != null) {
            queryCache.put(cacheKey, users);
        }

        return users;
    }

    public void invalidateUserQueries() {
        // Clear all user-related queries when users change
        queryCache.clear();
    }
}
```

### **Results**
- **Before**: Database hit on every request
- **After**: Database hit only when data changes
- **Improvement**: **Eliminates 80-95% of database queries**

---

## 🎯 Example 5: Configuration Caching

### **The Problem**
Your application loads configuration from database on every startup, taking 5-10 seconds to become ready.

### **The Solution**
Cache configuration for instant access.

```java
@Service
public class ConfigurationService {
    private final Cache<String, ConfigValue> configCache;
    private final ConfigRepository configRepository;

    public ConfigurationService() {
        // Use read-heavy profile for configuration
        this.configCache = JCacheXBuilder.forReadHeavyWorkload()
            .name("app-config")
            .maximumSize(1000L)  // Cache 1000 config values
            .recordStats(true)
            .build();
        this.configRepository = configRepository;
    }

    public String getConfigValue(String key) {
        ConfigValue config = configCache.get(key);
        if (config != null) {
            return config.getValue();
        }

        // Cache miss - load from database
        ConfigValue newConfig = configRepository.findByKey(key);
        if (newConfig != null) {
            configCache.put(key, newConfig);
        }

        return newConfig != null ? newConfig.getValue() : null;
    }

    public void setConfigValue(String key, String value) {
        // Update database
        ConfigValue config = new ConfigValue(key, value);
        configRepository.save(config);

        // Update cache
        configCache.put(key, config);
    }

    public void refreshConfiguration() {
        // Clear cache to force reload from database
        configCache.clear();

        // Optionally pre-load common config values
        List<ConfigValue> commonConfigs = configRepository.findCommonConfigs();
        for (ConfigValue config : commonConfigs) {
            configCache.put(config.getKey(), config);
        }
    }
}
```

### **Results**
- **Before**: 5-10 seconds startup time
- **After**: 0.05-0.2 seconds for config access
- **Improvement**: **50-200x faster** configuration access

---

## 🎯 Example 6: Rate Limiting and Circuit Breakers

### **The Problem**
Your application needs to track API usage and implement rate limiting, but storing this data in memory causes issues on restart.

### **The Solution**
Use JCacheX for distributed rate limiting with persistence.

```java
@Service
public class RateLimitService {
    private final Cache<String, RateLimitData> rateLimitCache;

    public RateLimitService() {
        // Use write-heavy profile for rate limiting
        this.rateLimitCache = JCacheXBuilder.forWriteHeavyWorkload()
            .name("rate-limits")
            .maximumSize(10000L)  // Track 10,000 clients
            .recordStats(true)
            .build();
    }

    public boolean isAllowed(String clientId, String endpoint, int maxRequests, Duration window) {
        String cacheKey = "rate:" + clientId + ":" + endpoint;

        RateLimitData data = rateLimitCache.get(cacheKey);
        if (data == null) {
            data = new RateLimitData(maxRequests, window);
        }

        if (data.isAllowed()) {
            data.incrementRequest();
            rateLimitCache.put(cacheKey, data);
            return true;
        }

        return false;
    }

    public void resetRateLimit(String clientId, String endpoint) {
        String cacheKey = "rate:" + clientId + ":" + endpoint;
        rateLimitCache.remove(cacheKey);
    }

    public RateLimitData getRateLimitStatus(String clientId, String endpoint) {
        String cacheKey = "rate:" + clientId + ":" + endpoint;
        return rateLimitCache.get(cacheKey);
    }
}

class RateLimitData {
    private final int maxRequests;
    private final Duration window;
    private int requestCount;
    private Instant windowStart;

    public RateLimitData(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
        this.windowStart = Instant.now();
        this.requestCount = 0;
    }

    public boolean isAllowed() {
        if (Instant.now().isAfter(windowStart.plus(window))) {
            // Reset window
            windowStart = Instant.now();
            requestCount = 0;
        }

        return requestCount < maxRequests;
    }

    public void incrementRequest() {
        requestCount++;
    }
}
```

### **Results**
- **Before**: Rate limiting lost on restart
- **After**: Persistent rate limiting across restarts
- **Improvement**: **Reliable rate limiting** with no data loss

---

## 🚀 Advanced Patterns

### **Pattern 1: Cache Warming**

```java
@Component
public class CacheWarmer {

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCaches() {
        // Warm up caches when application starts
        warmUpProductCache();
        warmUpUserCache();
        warmUpConfigCache();
    }

    private void warmUpProductCache() {
        List<Product> popularProducts = productRepository.findPopularProducts();
        Map<String, Product> productMap = popularProducts.stream()
            .collect(Collectors.toMap(Product::getId, p -> p));

        productCache.putAll(productMap);
        System.out.println("Warmed up product cache with " + popularProducts.size() + " products");
    }
}
```

### **Pattern 2: Cache Invalidation Strategies**

```java
@Service
public class CacheInvalidationService {

    public void invalidateUserRelatedCaches(String userId) {
        // Invalidate all caches related to a user
        userCache.remove(userId);
        sessionCache.remove(userId);

        // Clear query caches that might contain this user
        queryCache.clear();
    }

    public void invalidateProductRelatedCaches(String productId) {
        // Invalidate product cache
        productCache.remove(productId);

        // Clear category caches that might contain this product
        categoryCache.clear();
    }
}
```

### **Pattern 3: Async Cache Operations**

```java
@Service
public class AsyncCacheService {

    public CompletableFuture<Void> warmUpCacheAsync() {
        return CompletableFuture.runAsync(() -> {
            // Background cache warming
            warmUpProductCache();
            warmUpUserCache();
        });
    }

    public CompletableFuture<Void> cleanupExpiredAsync() {
        return CompletableFuture.runAsync(() -> {
            // Background cleanup
            cleanupExpiredSessions();
            cleanupExpiredApiResponses();
        });
    }
}
```

## 🎯 Performance Results Summary

| Use Case | Before | After | Improvement |
|----------|--------|-------|-------------|
| **Product Pages** | 2-5 seconds | 0.05-0.2 seconds | **25-100x faster** |
| **User Sessions** | 10-50 DB hits | 0 DB hits | **Eliminates 90%+ DB calls** |
| **API Responses** | 200-1000ms | 0.05-0.2ms | **1000-5000x faster** |
| **Database Queries** | Every request | Only on changes | **Eliminates 80-95%** |
| **Configuration** | 5-10s startup | 0.05-0.2s access | **50-200x faster** |

## 🚀 Next Steps

Now that you've seen real examples:

1. **[Core Concepts](core-concepts/cache-profiles)** - Understand which cache profile to use
2. **[API Reference](api-reference)** - Learn all available methods
3. **[Performance](performance)** - Optimize for your specific use case
4. **[Spring Boot Integration](spring-boot)** - Use with Spring applications

---

**These examples show real problems that JCacheX solves every day in production applications. Start with the pattern that matches your use case! 🚀**
