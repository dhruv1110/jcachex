# JCacheX Java Example

This example demonstrates **JCacheX** in a Java application, showcasing core caching functionality and event handling.

## 🎯 What This Example Shows

- **📦 Basic Cache Operations**: Put, get, remove, and clear operations
- **🔧 Cache Configuration**: Using CacheConfig builder for setup
- **📊 Cache Statistics**: Hit rates, miss counts, and performance monitoring
- **🎧 Event Listening**: Real-time cache operation notifications
- **⚡ Async Operations**: CompletableFuture-based asynchronous caching
- **🧪 Eviction Strategies**: LRU eviction with automatic cache management

## 🚀 Running the Example

```bash
# From the project root directory
./gradlew :example:java:run

# Or from the example/java directory (if gradlew were available there)
# java -cp "build/libs/*" io.github.dhruv1110.jcachex.example.java.Main
```

## 📋 Key Features Demonstrated

### 1. **Cache Configuration**
- **Size Limit**: 100 entries maximum
- **Time-based Expiration**: 5-minute write expiration
- **LRU Eviction**: Least recently used eviction strategy
- **Statistics Enabled**: Track hit rates and performance metrics

### 2. **Event Handling**
- **Put Events**: Notifications when entries are added
- **Remove Events**: Notifications when entries are removed
- **Eviction Events**: Notifications when entries are evicted
- **Expiration Events**: Notifications when entries expire
- **Clear Events**: Notifications when cache is cleared

### 3. **Synchronous and Asynchronous Operations**
- **Sync Operations**: Direct put/get/remove calls
- **Async Operations**: CompletableFuture-based operations
- **Performance Monitoring**: Real-time statistics and hit rates

## 📖 Code Structure

```
src/main/java/
└── io/github/dhruv1110/jcachex/example/java/
    └── Main.java                    # Complete demo application
```

## 🎓 Learning Path

1. **Start with Main.java**: See all features in a single comprehensive example
2. **Study Configuration**: Learn how to set up cache with builder pattern
3. **Explore Event Listeners**: Understand cache operation notifications
4. **Review Statistics**: See how to monitor cache performance

## 🔧 Configuration Examples

### Basic Cache Setup
```java
CacheConfig<String, String> config = CacheConfig.<String, String>newBuilder()
    .maximumSize(100L)
    .expireAfterWrite(Duration.ofMinutes(5))
    .evictionStrategy(new LRUEvictionStrategy<>())
    .recordStats(true)
    .build();

Cache<String, String> cache = new DefaultCache<>(config);
```

### Event Listener Setup
```java
config.addListener(new CacheEventListener<String, String>() {
    @Override
    public void onPut(String key, String value) {
        System.out.println("Put: " + key + " = " + value);
    }

    @Override
    public void onEvict(String key, String value, EvictionReason reason) {
        System.out.println("Evicted: " + key + " due to " + reason);
    }

    // ... implement other event methods
});
```

## 📈 Expected Output

When you run this example, you should see:
```
Put: key1 = value1
Value for key1: value1
Async value for key1: value1
Put: key2 = value2
Put: key3 = value3
Cache stats: CacheStats{hitCount=2, missCount=0, evictionCount=0, ...}
Hit rate: 1.0
Cache size: 3
Remove: key2 = value2
Cache cleared
Final cache size: 0
```

## 📈 Expected Performance

This example demonstrates:
- **Cache Hit Rate**: 100% for the operations shown
- **Response Time**: < 1ms for cached operations
- **Memory Usage**: ~32 bytes overhead per cache entry
- **Event Processing**: Real-time notifications for all cache operations

## 🔗 Related Documentation

- [Main Documentation](../../README.md): Complete JCacheX guide
- [Kotlin Example](../kotlin/): Coroutines and DSL examples
- [Spring Boot Example](../springboot/): Spring integration
- [API Documentation](https://javadoc.io/doc/io.github.dhruv1110/jcachex-core): Complete API reference

---

💡 **Pro Tip**: This example provides a solid foundation for understanding JCacheX core concepts. The single-file approach makes it easy to see all features in context and experiment with different configurations.
