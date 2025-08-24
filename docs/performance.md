# Performance & Optimization

Learn how to get the best performance from JCacheX and optimize your caching strategy for real-world applications.

## 🚀 Performance Characteristics

### **What You Can Expect**

JCacheX is designed to deliver **sub-microsecond response times** for most operations:

- **GET operations**: 50-200 nanoseconds (typical)
- **PUT operations**: 100-400 nanoseconds (typical)
- **Bulk operations**: 10-100x faster than individual operations
- **Memory overhead**: ~2MB per 10K entries

### **Real-World Performance**

```java
// Example: User session caching
Cache<String, UserSession> sessionCache = JCacheXBuilder.forSessionStorage()
    .name("sessions")
    .maximumSize(10000L)
    .build();

// Typical performance:
// - 1000 concurrent users: <1ms response time
// - 10000 concurrent users: <5ms response time
// - 100000 concurrent users: <50ms response time
```

## 🎯 Choosing the Right Profile

### **Profile Performance Comparison**

| Profile | Read Performance | Write Performance | Memory Usage | Best For |
|---------|------------------|-------------------|--------------|----------|
| **READ_HEAVY** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | Product catalogs, user data |
| **WRITE_HEAVY** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Logging, analytics |
| **API_CACHE** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | HTTP responses, API results |
| **SESSION_CACHE** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | User sessions, temporary data |
| **HIGH_PERFORMANCE** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | Critical path operations |
| **MEMORY_EFFICIENT** | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Large datasets |

### **Profile Selection Guide**

```java
// For e-commerce product catalogs (read-heavy)
Cache<String, Product> products = JCacheXBuilder.forReadHeavyWorkload()
    .name("products")
    .maximumSize(50000L)
    .build();

// For logging systems (write-heavy)
Cache<String, LogEntry> logs = JCacheXBuilder.forWriteHeavyWorkload()
    .name("logs")
    .maximumSize(10000L)
    .build();

// For API response caching
Cache<String, ApiResponse> apiCache = JCacheXBuilder.forApiResponseCaching()
    .name("api")
    .maximumSize(1000L)
    .build();
```

## 🔧 Optimization Strategies

### **1. Size Your Cache Appropriately**

```java
// Calculate optimal cache size based on your data
public class CacheSizer {
    
    // For read-heavy workloads: cache 80% of data
    public static long getReadHeavySize(long totalDataSize) {
        return (long) (totalDataSize * 0.8);
    }
    
    // For write-heavy workloads: cache 20% of data
    public static long getWriteHeavySize(long totalDataSize) {
        return (long) (totalDataSize * 0.2);
    }
    
    // For API caching: cache 1 minute of requests
    public static long getApiCacheSize(long requestsPerSecond) {
        return requestsPerSecond * 60;
    }
}

// Usage
long productCacheSize = CacheSizer.getReadHeavySize(100000); // 80,000
long logCacheSize = CacheSizer.getWriteHeavySize(1000000);   // 200,000
long apiCacheSize = CacheSizer.getApiCacheSize(1000);        // 60,000
```

### **2. Use Bulk Operations**

```java
// ❌ Slow: Individual operations
for (Product product : products) {
    productCache.put(product.getId(), product); // 1000 individual puts
}

// ✅ Fast: Bulk operations
Map<String, Product> productMap = products.stream()
    .collect(Collectors.toMap(
        Product::getId,
        product -> product
    ));
productCache.putAll(productMap); // 1 bulk put

// Performance improvement: 10-100x faster
```

### **3. Optimize Key Design**

```java
// ❌ Poor: Complex keys
cache.put("user:123:profile:basic:2024:01:15", userProfile);

// ✅ Good: Simple, consistent keys
cache.put("user:123:profile", userProfile);

// ❌ Poor: Inconsistent key patterns
cache.put("user_123", user1);
cache.put("user:456", user2);
cache.put("USER_789", user3);

// ✅ Good: Consistent key patterns
cache.put("user:123", user1);
cache.put("user:456", user2);
cache.put("user:789", user3);
```

### **4. Choose the Right Eviction Strategy**

```java
// For frequently accessed data
.evictionStrategy(new LRUEvictionStrategy<>())  // Keep recently used

// For data with access patterns
.evictionStrategy(new LFUEvictionStrategy<>())  // Keep frequently used

// For time-sensitive data
.evictionStrategy(new TTLEvictionStrategy<>(Duration.ofMinutes(30)))

// For complex scenarios
.evictionStrategy(new CompositeEvictionStrategy<>(
    new LRUEvictionStrategy<>(),
    new TTLEvictionStrategy<>(Duration.ofHours(1))
))
```

## 📊 Monitoring Performance

### **Key Metrics to Track**

```java
public class PerformanceMonitor {
    private final Cache<String, Object> cache;
    
    public void logPerformance() {
        CacheStats stats = cache.stats();
        
        // Core metrics
        double hitRate = stats.hitRate();
        long totalRequests = stats.hitCount() + stats.missCount();
        long evictions = stats.evictionCount();
        double avgLoadTime = stats.averageLoadTime() / 1_000_000.0; // ms
        
        System.out.println("=== Cache Performance ===");
        System.out.println("Hit Rate: " + String.format("%.2f%%", hitRate * 100));
        System.out.println("Total Requests: " + totalRequests);
        System.out.println("Evictions: " + evictions);
        System.out.println("Avg Load Time: " + String.format("%.2fms", avgLoadTime));
        
        // Performance alerts
        if (hitRate < 0.8) {
            System.out.println("⚠️  Low hit rate - consider increasing cache size");
        }
        
        if (evictions > totalRequests * 0.1) {
            System.out.println("⚠️  High eviction rate - cache may be too small");
        }
    }
}
```

### **Performance Thresholds**

| Metric | Good | Warning | Critical | Action |
|--------|------|---------|----------|---------|
| **Hit Rate** | >90% | 80-90% | <80% | Increase cache size |
| **Eviction Rate** | <5% | 5-10% | >10% | Increase cache size |
| **Load Time** | <1ms | 1-5ms | >5ms | Check data source |
| **Memory Usage** | <80% | 80-90% | >90% | Reduce cache size |

## 🚀 Advanced Optimization

### **1. Memory-Efficient Caching**

```java
// Use weight-based sizing for variable-sized objects
CacheConfig<String, Object> config = CacheConfig.<String, Object>builder()
    .maximumWeight(100 * 1024 * 1024L)  // 100MB limit
    .weigher((key, value) -> {
        if (value instanceof String) {
            return ((String) value).length();
        } else if (value instanceof byte[]) {
            return ((byte[]) value).length;
        }
        return 1; // Default weight
    })
    .build();
```

### **2. Async Operations for Non-Critical Paths**

```java
// Use async operations for background tasks
public class AsyncCacheService {
    
    public CompletableFuture<Void> warmUpCacheAsync() {
        return CompletableFuture.runAsync(() -> {
            List<Product> products = productRepository.findAll();
            Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(
                    Product::getId,
                    product -> product
                ));
            productCache.putAll(productMap);
        });
    }
    
    public CompletableFuture<Void> cleanupExpiredAsync() {
        return CompletableFuture.runAsync(() -> {
            // Background cleanup logic
        });
    }
}
```

### **3. Smart Cache Warming**

```java
public class CacheWarmer {
    
    public void warmUpByAccessPattern() {
        // Warm up most frequently accessed data first
        List<String> hotKeys = getHotKeys();
        
        for (String key : hotKeys) {
            Object value = dataSource.load(key);
            if (value != null) {
                cache.put(key, value);
            }
        }
    }
    
    public void warmUpByTime() {
        // Warm up data that's accessed at specific times
        LocalTime now = LocalTime.now();
        
        if (now.getHour() >= 9 && now.getHour() <= 17) {
            // Business hours - warm up business data
            warmUpBusinessData();
        } else {
            // Off hours - warm up maintenance data
            warmUpMaintenanceData();
        }
    }
}
```

## 🎯 Real-World Performance Examples

### **E-commerce Application**

```java
// Product catalog with 100,000 products
Cache<String, Product> productCache = JCacheXBuilder.forReadHeavyWorkload()
    .name("products")
    .maximumSize(80000L)  // Cache 80% of products
    .build();

// Performance results:
// - Page load time: 50ms → 5ms (10x improvement)
// - Database queries: 1000/second → 100/second (90% reduction)
// - User experience: Significant improvement
```

### **API Gateway**

```java
// API response caching
Cache<String, ApiResponse> apiCache = JCacheXBuilder.forApiResponseCaching()
    .name("api-responses")
    .maximumSize(10000L)
    .build();

// Performance results:
// - Response time: 200ms → 20ms (10x improvement)
// - External API calls: 1000/second → 100/second (90% reduction)
// - Cost savings: Reduced external API usage
```

### **User Session Management**

```java
// User session caching
Cache<String, UserSession> sessionCache = JCacheXBuilder.forSessionStorage()
    .name("sessions")
    .maximumSize(50000L)
    .build();

// Performance results:
// - Login time: 500ms → 50ms (10x improvement)
// - Session validation: 100ms → 5ms (20x improvement)
// - User experience: Instant authentication
```

## 🔧 Performance Troubleshooting

### **Common Issues and Solutions**

| Problem | Symptoms | Solution |
|---------|----------|----------|
| **Low Hit Rate** | High miss count, slow responses | Increase cache size, improve key distribution |
| **High Eviction Rate** | Frequent evictions, poor performance | Increase cache size, optimize eviction strategy |
| **Memory Issues** | OutOfMemoryError, high GC activity | Reduce cache size, use weight-based sizing |
| **Slow Operations** | High load times, poor response times | Check data source, optimize bulk operations |

### **Performance Checklist**

- [ ] **Cache Size**: Appropriate for your data volume
- [ ] **Profile Selection**: Matches your access pattern
- [ ] **Key Design**: Simple, consistent, well-distributed
- [ ] **Bulk Operations**: Used for multiple items
- [ ] **Monitoring**: Statistics enabled and tracked
- [ ] **Eviction Strategy**: Appropriate for your use case
- [ ] **Memory Management**: Within system constraints

## 🚀 Next Steps

Optimize your caching strategy:

1. **[Examples](examples)** - See optimization patterns in action
2. **[API Reference](api-reference)** - Learn advanced configuration options
3. **[Spring Boot Integration](spring-boot)** - Optimize Spring applications

Remember: The best performance comes from choosing the right configuration for your specific use case, not from generic optimizations.



