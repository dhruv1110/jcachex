# JCacheX Java Example

This example demonstrates **JCacheX** in a real-world Java application with multiple caching patterns commonly found in enterprise applications.

## 🎯 What This Example Shows

- **📦 E-commerce Product Catalog**: Multi-level caching for products and categories
- **🔒 API Rate Limiting**: Request throttling using time-based cache expiration
- **📊 Performance Monitoring**: Cache statistics and event listening
- **🧪 Testing Strategies**: Using fake caches for unit testing
- **⚡ Async Operations**: CompletableFuture integration

## 🚀 Running the Example

```bash
# From the example/java directory
./gradlew run

# Or with your IDE: Run Main.java
```

## 📋 Key Features Demonstrated

### 1. **Product Catalog Service**
- **Product Cache**: 50k entries, 2-hour expiration
- **Category Cache**: 1k entries, 30-minute expiration
- **Smart Loading**: `getOrCompute` pattern

### 2. **Rate Limiter**
- **Time Windows**: 1-minute sliding windows
- **Client Tracking**: Per-client request counting
- **Automatic Cleanup**: Expired counters removed automatically

### 3. **Performance Monitoring**
- **Cache Statistics**: Hit rates, miss counts, eviction stats
- **Event Listeners**: Real-time cache operation logging
- **Metrics Export**: Ready for monitoring systems

### 4. **Testing Patterns**
- **Fake Cache**: Deterministic testing with `FakeCache`
- **Mock Integration**: Testing cache interactions
- **Performance Tests**: JMH benchmarking setup

## 📖 Code Structure

```
src/main/java/
├── Main.java                          # Main demo application
├── ecommerce/
│   ├── ProductCatalogService.java     # Multi-level caching example
│   ├── Product.java                   # Domain model
│   └── ProductRepository.java         # Simulated data access
├── ratelimit/
│   ├── RateLimiter.java               # Request throttling
│   └── ClientRequestTracker.java      # Request counting
├── monitoring/
│   ├── CacheMonitor.java              # Statistics and metrics
│   └── LoggingCacheEventListener.java # Event logging
└── testing/
    ├── ProductServiceTest.java        # Unit testing example
    └── PerformanceBenchmark.java      # JMH benchmarking
```

## 🎓 Learning Path

1. **Start with Main.java**: See all examples in action
2. **Study ProductCatalogService**: Learn multi-level caching
3. **Explore RateLimiter**: Understand time-based patterns
4. **Check CacheMonitor**: See performance monitoring
5. **Review Tests**: Learn testing strategies

## 🔧 Configuration Examples

### High-Performance Cache
```java
Cache<String, Product> cache = CacheBuilder.<String, Product>newBuilder()
    .maximumSize(50_000)
    .expireAfterWrite(Duration.ofHours(2))
    .evictionStrategy(new LRUEvictionStrategy<>())
    .enableStatistics()
    .concurrencyLevel(16)
    .build();
```

### Memory-Conscious Cache
```java
Cache<String, List<Product>> cache = CacheBuilder.<String, List<Product>>newBuilder()
    .maximumWeight(10_000_000) // 10MB
    .weigher((key, value) -> value.size() * 1000) // Estimated bytes per product
    .expireAfterAccess(Duration.ofMinutes(30))
    .build();
```

## 📈 Expected Performance

Running this example should show:
- **Cache Hit Rate**: 85-95% for product lookups
- **Response Time**: < 1ms for cached products vs 100ms+ for database
- **Memory Usage**: ~32 bytes overhead per cache entry
- **Throughput**: 10M+ operations/second for in-memory operations

## 🔗 Related Documentation

- [Main Documentation](../../README.md): Complete JCacheX guide
- [Kotlin Example](../kotlin/): Coroutines and DSL examples
- [Spring Boot Example](../springboot/): Annotation-based caching
- [API Documentation](https://javadoc.io/doc/io.github.dhruv1110/jcachex-core): Complete API reference

---

💡 **Pro Tip**: Use this example as a starting point for your own caching implementation. The patterns shown here scale well in production environments.
