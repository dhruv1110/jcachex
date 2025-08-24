# API Reference

**Complete reference for JCacheX methods and features, organized by what you want to accomplish.**

## 🚀 Quick Navigation

- **[Cache Creation](#cache-creation)** - Build caches for different use cases
- **[Basic Operations](#basic-operations)** - Store, retrieve, and manage data
- **[Bulk Operations](#bulk-operations)** - Work with multiple items efficiently
- **[Async Operations](#async-operations)** - Non-blocking cache operations
- **[Monitoring & Statistics](#monitoring--statistics)** - Track performance and health
- **[Advanced Features](#advanced-features)** - Event handling, eviction, and more

## 🏗️ Cache Creation

### **Profile-Based Builders (Recommended)**

```java
// For product catalogs, user data, anything read-heavy
Cache<String, Product> products = JCacheXBuilder.forReadHeavyWorkload()
    .name("products")
    .maximumSize(10000L)
    .build();

// For logging, analytics, anything write-heavy
Cache<String, LogEntry> logs = JCacheXBuilder.forWriteHeavyWorkload()
    .name("logs")
    .maximumSize(5000L)
    .build();

// For HTTP responses, external API results
Cache<String, ApiResponse> apiCache = JCacheXBuilder.forApiResponseCaching()
    .name("api")
    .maximumSize(1000L)
    .build();

// For user sessions, temporary data
Cache<String, UserSession> sessions = JCacheXBuilder.forSessionStorage()
    .name("sessions")
    .maximumSize(10000L)
    .build();

// For critical path operations
Cache<String, Object> critical = JCacheXBuilder.forHighPerformance()
    .name("critical")
    .maximumSize(100000L)
    .build();

// For large datasets with memory constraints
Cache<String, Object> large = JCacheXBuilder.forMemoryEfficient()
    .name("large-dataset")
    .maximumSize(1000000L)
    .build();
```

### **Custom Configuration**

```java
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .name("custom-users")
    .maximumSize(5000L)
    .evictionStrategy(new LRUEvictionStrategy<>())
    .recordStats(true)
    .build();

Cache<String, User> cache = new DefaultCache<>(config);
```

### **Basic Builder**

```java
Cache<String, User> cache = JCacheXBuilder.newBuilder()
    .name("users")
    .maximumSize(1000L)
    .build();
```

## 📚 Basic Operations

### **Store and Retrieve**

```java
// Store a single item
cache.put("key", value);

// Retrieve a single item
User user = cache.get("key");

// Check if key exists
if (cache.containsKey("key")) {
    // Key exists
}

// Remove a single item
cache.remove("key");

// Clear all items
cache.clear();
```

### **Conditional Operations**

```java
// Get with default value if not found
User user = cache.getOrDefault("key", defaultUser);

// Put only if key doesn't exist
boolean wasNew = cache.putIfAbsent("key", value);

// Replace only if key exists
boolean wasReplaced = cache.replace("key", oldValue, newValue);

// Remove only if value matches
boolean wasRemoved = cache.remove("key", expectedValue);
```

### **Size and Capacity**

```java
// Get current number of items
long size = cache.size();

// Check if cache is empty
boolean isEmpty = cache.isEmpty();

// Get maximum capacity
long maxSize = cache.getMaximumSize();

// Check if cache is full
boolean isFull = cache.size() >= cache.getMaximumSize();
```

## 🚀 Bulk Operations

### **Store Multiple Items**

```java
// Store multiple items at once (much faster than individual puts)
Map<String, User> users = Map.of(
    "user1", new User("Alice"),
    "user2", new User("Bob"),
    "user3", new User("Charlie")
);
cache.putAll(users);
```

### **Retrieve Multiple Items**

```java
// Get multiple items at once
List<String> keys = List.of("user1", "user2", "user3");
Map<String, User> retrieved = cache.getAll(keys);

// Get all items (use with caution on large caches)
Map<String, User> allUsers = cache.getAll();
```

### **Bulk Removal**

```java
// Remove multiple items
List<String> keysToRemove = List.of("user1", "user2");
cache.removeAll(keysToRemove);
```

## ⚡ Async Operations

### **Non-Blocking Gets**

```java
// Async get operation
CompletableFuture<User> future = cache.getAsync("key");

// Handle the result
future.thenAccept(user -> {
    if (user != null) {
        System.out.println("Found user: " + user.getName());
    }
});

// Or wait for completion
User user = future.get(); // Blocking
```

### **Non-Blocking Puts**

```java
// Async put operation
CompletableFuture<Void> putFuture = cache.putAsync("key", new User("Alice"));

// Handle completion
putFuture.thenRun(() -> {
    System.out.println("User stored successfully");
});

// Or wait for completion
putFuture.get(); // Blocking
```

### **Async Bulk Operations**

```java
// Async bulk put
Map<String, User> users = Map.of(
    "user1", new User("Alice"),
    "user2", new User("Bob")
);
CompletableFuture<Void> bulkFuture = cache.putAllAsync(users);

// Async bulk get
List<String> keys = List.of("user1", "user2");
CompletableFuture<Map<String, User>> bulkGetFuture = cache.getAllAsync(keys);
```

## 📊 Monitoring & Statistics

### **Enable Statistics**

```java
// Enable statistics when building cache
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(10000L)
    .recordStats(true)  // Enable monitoring
    .build();
```

### **Performance Metrics**

```java
CacheStats stats = cache.stats();

// Hit rate (percentage of requests served from cache)
double hitRate = stats.hitRate();
System.out.println("Hit Rate: " + (hitRate * 100) + "%");

// Request counts
long hitCount = stats.hitCount();      // Cache hits
long missCount = stats.missCount();    // Cache misses
long totalRequests = hitCount + missCount;

// Eviction information
long evictionCount = stats.evictionCount();
long evictionWeight = stats.evictionWeight();

// Load information (if using cache loader)
long loadCount = stats.loadCount();
long loadSuccessCount = stats.loadSuccessCount();
long loadExceptionCount = stats.loadExceptionCount();
double averageLoadTime = stats.averageLoadTime() / 1_000_000.0; // in milliseconds
```

### **Performance Monitoring**

```java
public class CacheMonitor {

    public void checkHealth(Cache<String, Object> cache) {
        CacheStats stats = cache.stats();

        // Performance alerts
        if (stats.hitRate() < 0.8) {
            System.out.println("⚠️  Low hit rate: " + (stats.hitRate() * 100) + "%");
        }

        if (stats.evictionCount() > 1000) {
            System.out.println("⚠️  High eviction count: " + stats.evictionCount());
        }

        if (stats.averageLoadTime() > 5_000_000) { // 5ms
            System.out.println("⚠️  Slow load time: " + (stats.averageLoadTime() / 1_000_000.0) + "ms");
        }
    }

    public void logPerformance(Cache<String, Object> cache) {
        CacheStats stats = cache.stats();

        System.out.println("=== Cache Performance ===");
        System.out.println("Hit Rate: " + String.format("%.1f%%", stats.hitRate() * 100));
        System.out.println("Total Requests: " + (stats.hitCount() + stats.missCount()));
        System.out.println("Evictions: " + stats.evictionCount());
        System.out.println("Avg Load Time: " + String.format("%.2fms", stats.averageLoadTime() / 1_000_000.0));
    }
}
```

## 🔄 Event Handling

### **Cache Event Listeners**

```java
cache.addListener(new CacheEventListener<String, User>() {
    @Override
    public void onEntryAdded(String key, User value) {
        System.out.println("User added to cache: " + value.getName());
    }

    @Override
    public void onEntryRemoved(String key, User value) {
        System.out.println("User removed from cache: " + value.getName());
    }

    @Override
    public void onEntryUpdated(String key, User oldValue, User newValue) {
        System.out.println("User updated: " + oldValue.getName() + " → " + newValue.getName());
    }

    @Override
    public void onEntryExpired(String key, User value) {
        System.out.println("User expired: " + value.getName());
    }
});
```

### **Remove Event Listeners**

```java
// Store reference to listener
CacheEventListener<String, User> listener = new MyCacheEventListener();

// Add listener
cache.addListener(listener);

// Remove specific listener
cache.removeListener(listener);

// Remove all listeners
cache.clearListeners();
```

## ⚙️ Advanced Features

### **Eviction Strategies**

```java
// Least Recently Used (default)
.evictionStrategy(new LRUEvictionStrategy<>())

// Least Frequently Used
.evictionStrategy(new LFUEvictionStrategy<>())

// First In, First Out
.evictionStrategy(new FIFOEvictionStrategy<>())

// Time To Live
.evictionStrategy(new TTLEvictionStrategy<>(Duration.ofMinutes(30)))

// Composite (multiple strategies)
.evictionStrategy(new CompositeEvictionStrategy<>(
    new LRUEvictionStrategy<>(),
    new TTLEvictionStrategy<>(Duration.ofHours(1))
))
```

### **Weight-Based Sizing**

```java
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

### **Cache Loading**

```java
// Automatic loading from external source
CacheLoader<String, User> loader = new CacheLoader<String, User>() {
    @Override
    public User load(String key) {
        return userRepository.findById(key);
    }

    @Override
    public Map<String, User> loadAll(Collection<String> keys) {
        return userRepository.findAllById(keys).stream()
            .collect(Collectors.toMap(User::getId, user -> user));
    }
};

CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .cacheLoader(loader)
    .build();
```

## 🎨 Kotlin DSL

### **Kotlin Cache Creation**

```kotlin
import io.github.dhruv1110.jcachex.kotlin.*

// Create cache with Kotlin DSL
val userCache = createReadHeavyCache<String, User> {
    name("users")
    maximumSize(10000)
    recordStats(true)
}

// Create different profile types
val productCache = createReadHeavyCache<String, Product> { /* config */ }
val logCache = createWriteHeavyCache<String, LogEntry> { /* config */ }
val apiCache = createApiResponseCache<String, ApiResponse> { /* config */ }
val sessionCache = createSessionCache<String, UserSession> { /* config */ }
```

### **Kotlin Extension Functions**

```kotlin
// Extension function for easier usage
fun Cache<String, User>.getOrLoad(key: String, loader: () -> User): User {
    return get(key) ?: loader().also { put(key, it) }
}

// Usage
val user = userCache.getOrLoad("user123") {
    userRepository.findById("user123")
}

// Operator overloading for map-like access
userCache["user123"] = User("Alice")
val user = userCache["user123"]
```

## 🔧 Configuration Options

### **Common Configuration**

```java
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .name("users")                           // Cache name for identification
    .maximumSize(10000L)                     // Maximum number of entries
    .maximumWeight(100 * 1024 * 1024L)      // Maximum memory in bytes
    .recordStats(true)                       // Enable statistics
    .evictionStrategy(new LRUEvictionStrategy<>())  // Eviction strategy
    .build();
```

### **Advanced Configuration**

```java
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .name("users")
    .maximumSize(10000L)
    .recordStats(true)
    .evictionStrategy(new LRUEvictionStrategy<>())
    .weigher((key, value) -> ((String) value).length())  // Custom weight calculation
    .build();
```

## 🚀 Best Practices

### **Choose the Right Profile**

| Use Case | Profile | Why |
|----------|---------|-----|
| **Product catalogs** | `READ_HEAVY` | Optimized for frequent reads |
| **Logging systems** | `WRITE_HEAVY` | Optimized for frequent writes |
| **API responses** | `API_CACHE` | Balanced read/write performance |
| **User sessions** | `SESSION_CACHE` | Optimized for temporary data |
| **Critical operations** | `HIGH_PERFORMANCE` | Maximum speed for critical paths |
| **Large datasets** | `MEMORY_EFFICIENT` | Minimize memory usage |

### **Size Your Cache Appropriately**

```java
// For read-heavy workloads: cache 80% of data
long cacheSize = (long) (totalDataSize * 0.8);

// For write-heavy workloads: cache 20% of data
long cacheSize = (long) (totalDataSize * 0.2);

// For API caching: cache 1 minute of requests
long cacheSize = (long) (requestsPerSecond * 60);
```

### **Monitor and Tune**

```java
// Regular monitoring
if (stats.hitRate() < 0.8) {
    // Consider increasing cache size or improving key distribution
}

if (stats.evictionCount() > 1000) {
    // Cache is too small, increase size
}

if (stats.averageLoadTime() > 5_000_000) { // 5ms
    // Data source is slow, investigate
}
```

---

**This API reference covers everything you need to build fast, scalable applications with JCacheX. Start with the basics and explore advanced features as you need them! 🚀**
