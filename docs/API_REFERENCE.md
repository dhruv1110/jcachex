# JCacheX API Reference Guide

## Overview

This document provides comprehensive API documentation for JCacheX, including all public interfaces, classes, and methods. For architectural details and best practices, see the [Architecture Guide](ARCHITECTURE.md).

## 📋 **Table of Contents**

1. [Core Cache API](#core-cache-api)
2. [Configuration API](#configuration-api)
3. [Statistics API](#statistics-api)
4. [Eviction Strategies](#eviction-strategies)
5. [Event Listeners](#event-listeners)
6. [Monitoring API](#monitoring-api)
7. [Resilience API](#resilience-api)
8. [Exception Handling](#exception-handling)
9. [Utility Classes](#utility-classes)

---

## 🎯 **Core Cache API**

### Cache Interface

The primary interface for all cache operations.

```java
public interface Cache<K, V> {
    // Synchronous operations
    V get(K key);
    void put(K key, V value);
    void invalidate(K key);
    void invalidateAll();

    // Asynchronous operations
    CompletableFuture<V> getAsync(K key);
    CompletableFuture<Void> putAsync(K key, V value);
    CompletableFuture<Void> invalidateAsync(K key);

    // Bulk operations
    Map<K, V> getAll(Iterable<K> keys);
    void putAll(Map<K, V> map);
    void invalidateAll(Iterable<K> keys);

    // Conditional operations
    V getIfPresent(K key);
    void putIfAbsent(K key, V value);
    boolean replace(K key, V oldValue, V newValue);
    V replace(K key, V value);
    boolean remove(K key, V value);

    // Statistics and monitoring
    CacheStats stats();
    long size();
    void cleanUp();

    // Lifecycle
    void close();
}
```

### Method Details

#### `V get(K key)`
Retrieves a value from the cache.

- **Parameters**: `key` - the cache key
- **Returns**: The cached value, or `null` if not found
- **Throws**: `CacheOperationException` if the key is invalid

```java
// Example usage
Cache<String, User> userCache = createCache();
User user = userCache.get("user123");
if (user != null) {
    // Use cached user
}
```

#### `void put(K key, V value)`
Stores a key-value pair in the cache.

- **Parameters**:
  - `key` - the cache key
  - `value` - the value to store
- **Throws**: `CacheOperationException` if validation fails

```java
// Example usage
User user = new User("john", "John Doe");
userCache.put("user123", user);
```

#### `CompletableFuture<V> getAsync(K key)`
Asynchronously retrieves a value from the cache.

- **Parameters**: `key` - the cache key
- **Returns**: A `CompletableFuture` containing the cached value

```java
// Example usage
CompletableFuture<User> userFuture = userCache.getAsync("user123");
userFuture.thenAccept(user -> {
    if (user != null) {
        // Handle cached user
    }
});
```

#### `Map<K, V> getAll(Iterable<K> keys)`
Retrieves multiple values from the cache.

- **Parameters**: `keys` - collection of cache keys
- **Returns**: Map of found key-value pairs

```java
// Example usage
List<String> keys = Arrays.asList("user1", "user2", "user3");
Map<String, User> users = userCache.getAll(keys);
```

---

## ⚙️ **Configuration API**

### CacheConfig Class

The configuration builder for creating cache instances.

```java
public class CacheConfig<K, V> {
    public static <K, V> Builder<K, V> builder() { /* ... */ }

    // Getters for all configuration options
    public Long getMaximumSize() { /* ... */ }
    public Long getMaximumWeight() { /* ... */ }
    public Duration getExpireAfterWrite() { /* ... */ }
    public Duration getExpireAfterAccess() { /* ... */ }
    // ... more getters
}
```

### Configuration Builder

```java
public static class Builder<K, V> {
    // Size configuration
    public Builder<K, V> maximumSize(Long maximumSize);
    public Builder<K, V> maximumWeight(Long maximumWeight);
    public Builder<K, V> weigher(BiFunction<K, V, Long> weigher);

    // Expiration configuration
    public Builder<K, V> expireAfterWrite(Duration duration);
    public Builder<K, V> expireAfterAccess(Duration duration);
    public Builder<K, V> refreshAfterWrite(Duration duration);

    // Eviction configuration
    public Builder<K, V> evictionStrategy(EvictionStrategy<K, V> strategy);

    // Reference configuration
    public Builder<K, V> weakKeys(boolean weakKeys);
    public Builder<K, V> weakValues(boolean weakValues);
    public Builder<K, V> softValues(boolean softValues);

    // Loading configuration
    public Builder<K, V> loader(Function<K, V> loader);
    public Builder<K, V> asyncLoader(Function<K, CompletableFuture<V>> asyncLoader);

    // Monitoring configuration
    public Builder<K, V> recordStats(boolean recordStats);
    public Builder<K, V> addListener(CacheEventListener<K, V> listener);

    // Performance configuration
    public Builder<K, V> initialCapacity(int initialCapacity);
    public Builder<K, V> concurrencyLevel(int concurrencyLevel);

    // Build the configuration
    public CacheConfig<K, V> build();
}
```

### Configuration Examples

#### Basic Configuration
```java
CacheConfig<String, Object> config = CacheConfig.<String, Object>builder()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(10))
    .build();
```

#### Advanced Configuration
```java
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .maximumWeight(1_000_000L)
    .weigher((key, value) -> key.length() + value.getSerializedSize())
    .expireAfterWrite(Duration.ofHours(1))
    .expireAfterAccess(Duration.ofMinutes(30))
    .evictionStrategy(new LRUEvictionStrategy<>())
    .loader(key -> userService.findById(key))
    // Statistics are opt-in; default is false in core
    .recordStats(true)
    .addListener(new UserCacheListener())
    .build();
```

---

## 📊 **Statistics API**

### CacheStats Interface

Provides comprehensive statistics about cache performance.

```java
public interface CacheStats {
    // Hit/miss statistics
    long hitCount();
    long missCount();
    long requestCount();
    double hitRate();
    double missRate();

    // Load statistics
    long loadCount();
    long loadSuccessCount();
    long loadFailureCount();
    double averageLoadTime();
    double loadFailureRate();

    // Eviction statistics
    long evictionCount();
    long evictionWeight();

    // Size statistics
    long approximateSize();
    long estimatedSize();

    // Utility methods
    CacheStats minus(CacheStats other);
    CacheStats plus(CacheStats other);
    String toString();
}
```

### Statistics Usage

```java
// Get cache statistics
CacheStats stats = cache.stats();

// Monitor hit rate
double hitRate = stats.hitRate();
if (hitRate < 0.8) {
    logger.warn("Cache hit rate is low: {}", hitRate);
}

// Monitor load performance
double avgLoadTime = stats.averageLoadTime();
if (avgLoadTime > 100) {
    logger.warn("Average load time is high: {}ms", avgLoadTime);
}

// Monitor evictions
long evictionCount = stats.evictionCount();
if (evictionCount > 1000) {
    logger.info("High eviction count: {}", evictionCount);
}
```

---

## 🔄 **Eviction Strategies**

### EvictionStrategy Interface

```java
public interface EvictionStrategy<K, V> {
    void onAccess(K key, V value);
    void onPut(K key, V value);
    void onRemove(K key, V value);
    Collection<K> evict(int count);
    void clear();
}
```

### Built-in Eviction Strategies

#### LRU (Least Recently Used)
```java
EvictionStrategy<K, V> lru = new LRUEvictionStrategy<>();
```

#### LFU (Least Frequently Used)
```java
EvictionStrategy<K, V> lfu = new LFUEvictionStrategy<>();
```

#### FIFO (First In, First Out)
```java
EvictionStrategy<K, V> fifo = new FIFOEvictionStrategy<>();
```

### Custom Eviction Strategy

```java
public class CustomEvictionStrategy<K, V> implements EvictionStrategy<K, V> {
    @Override
    public void onAccess(K key, V value) {
        // Update access tracking
    }

    @Override
    public void onPut(K key, V value) {
        // Update put tracking
    }

    @Override
    public void onRemove(K key, V value) {
        // Clean up tracking
    }

    @Override
    public Collection<K> evict(int count) {
        // Return keys to evict
        return keysToEvict;
    }

    @Override
    public void clear() {
        // Clear all tracking
    }
}
```

---

## 🎧 **Event Listeners**

### CacheEventListener Interface

```java
public interface CacheEventListener<K, V> {
    default void onPut(K key, V value) {}
    default void onRemove(K key, V value) {}
    default void onEvict(K key, V value) {}
    default void onExpire(K key, V value) {}
    default void onLoad(K key, V value) {}
    default void onLoadFailure(K key, Throwable cause) {}
}
```

### Event Listener Implementation

```java
public class UserCacheListener implements CacheEventListener<String, User> {
    private static final Logger logger = LoggerFactory.getLogger(UserCacheListener.class);

    @Override
    public void onPut(String key, User value) {
        logger.debug("User added to cache: {}", key);
        metrics.increment("cache.put.count");
    }

    @Override
    public void onEvict(String key, User value) {
        logger.debug("User evicted from cache: {}", key);
        metrics.increment("cache.evict.count");
    }

    @Override
    public void onLoadFailure(String key, Throwable cause) {
        logger.error("Failed to load user: {}", key, cause);
        metrics.increment("cache.load.failure.count");
    }
}
```

---

## 📈 **Monitoring API**

### CacheStats Class

```java
public class CacheStats {
    // Access counters
    public long hitCount();
    public long missCount();

    // Calculated rates
    public double hitRate();
    public double missRate();

    // Operation counters
    public long evictionCount();
    public long loadCount();
    public long loadFailureCount();

    // Timing metrics
    public long totalLoadTime();
    public double averageLoadTime();

    // Utility methods
    public CacheStats snapshot();
    public void reset();
    public static CacheStats empty();
}
```

### Simple Monitoring Usage

```java
// Enable statistics in cache configuration
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .maximumSize(1000L)
    .recordStats(true)  // Enable simple statistics
    .build();

Cache<String, User> cache = new DefaultCache<>(config);

// Monitor performance
CacheStats stats = cache.stats();

// Basic performance monitoring
logger.info("Cache performance:");
logger.info("  Hit rate: {}%", stats.hitRate() * 100);
logger.info("  Total requests: {}", stats.hitCount() + stats.missCount());
logger.info("  Evictions: {}", stats.evictionCount());
logger.info("  Average load time: {}ms", stats.averageLoadTime() / 1_000_000.0);

// Alert on poor performance
if (stats.hitRate() < 0.8) {
    alertService.warn("Low cache hit rate: " + (stats.hitRate() * 100) + "%");
}

// Reset statistics for fresh measurement
stats.reset();

// Create snapshot for reporting
CacheStats snapshot = stats.snapshot();
```

---

## 🛡️ **Resilience API**

### RetryPolicy Class

```java
public class RetryPolicy {
    public static Builder builder() { /* ... */ }
    public static RetryPolicy defaultPolicy() { /* ... */ }

    public <T> T execute(Supplier<T> supplier) throws Exception;
    public void execute(Runnable runnable) throws Exception;
}
```

### RetryPolicy Builder

```java
public static class Builder {
    public Builder maxAttempts(int maxAttempts);
    public Builder initialDelay(Duration initialDelay);
    public Builder maxDelay(Duration maxDelay);
    public Builder backoffMultiplier(double backoffMultiplier);
    public Builder jitterFactor(double jitterFactor);
    public Builder retryOn(Predicate<Throwable> retryPredicate);
    public Builder retryOnException(Class<? extends Throwable>... types);
    public Builder delayFunction(Function<Integer, Duration> delayFunction);
    public RetryPolicy build();
}
```

### Resilience Usage

```java
// Create retry policy
RetryPolicy retryPolicy = RetryPolicy.builder()
    .maxAttempts(3)
    .initialDelay(Duration.ofMillis(100))
    .backoffMultiplier(2.0)
    .jitterFactor(0.1)
    .retryOnException(CacheOperationException.class)
    .build();

// Execute with retry
User user = retryPolicy.execute(() -> {
    return cache.get(userId);
});

// Async execution with retry
CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> {
    try {
        return retryPolicy.execute(() -> cache.get(userId));
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
});
```

---

## ⚠️ **Exception Handling**

### Exception Hierarchy

```java
CacheException
├── CacheConfigurationException
├── CacheOperationException
├── CacheLoadingException
├── CacheCapacityException
└── CacheTimeoutException
```

### CacheException Class

```java
public class CacheException extends RuntimeException {
    public enum ErrorType {
        CONFIGURATION, OPERATION, CAPACITY, TIMEOUT, LOADING, UNKNOWN
    }

    public ErrorType getErrorType();
    public String getErrorCode();
    public boolean isRetryable();
}
```

### Exception Usage

```java
try {
    cache.put(key, value);
} catch (CacheConfigurationException e) {
    // Handle configuration errors
    logger.error("Cache configuration error: {}", e.getMessage());
} catch (CacheOperationException e) {
    // Handle operation errors
    if (e.isRetryable()) {
        // Retry the operation
        retryOperation();
    } else {
        // Handle permanent failure
        logger.error("Permanent cache error: {}", e.getMessage());
    }
} catch (CacheException e) {
    // Handle all other cache exceptions
    logger.error("Cache error [{}]: {}", e.getErrorCode(), e.getMessage());
}
```

---

## 🛠️ **Utility Classes**

### DefaultCache Factory

```java
public class DefaultCache {
    public static <K, V> Cache<K, V> create(CacheConfig<K, V> config);
    public static <K, V> Cache<K, V> create();
}
```

### Cache Utilities

```java
public final class CacheUtils {
    public static <K, V> Cache<K, V> synchronizedCache(Cache<K, V> cache);
    public static <K, V> Cache<K, V> unmodifiableCache(Cache<K, V> cache);
    public static <K, V> Map<K, V> asMap(Cache<K, V> cache);

    public static long estimateSize(Object key, Object value);
    public static <K, V> BiFunction<K, V, Long> defaultWeigher();
}
```

---

## 📝 **Usage Examples**

### Basic Cache Usage

```java
// Create a simple cache
Cache<String, String> cache = CacheConfig.<String, String>builder()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(10))
    .build()
    .createCache();

// Use the cache
cache.put("key1", "value1");
String value = cache.get("key1");
```

### Production Configuration

```java
// Production-ready cache configuration
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    // Size limits
    .maximumSize(50_000L)
    .maximumWeight(100_000_000L) // 100MB
    .weigher((key, value) -> key.length() + value.getSerializedSize())

    // Expiration
    .expireAfterWrite(Duration.ofHours(2))
    .expireAfterAccess(Duration.ofMinutes(30))

    // Performance
    .evictionStrategy(new LRUEvictionStrategy<>())
    .initialCapacity(1024)
    .concurrencyLevel(16)

    // Loading
    .loader(key -> userService.findById(key))
    .asyncLoader(key -> userService.findByIdAsync(key))
    .refreshAfterWrite(Duration.ofMinutes(30))

    // Monitoring
    .recordStats(true)
    .addListener(new ProductionCacheListener())

    .build();

Cache<String, User> userCache = config.createCache();
```

### Async Operations

```java
// Async cache operations
CompletableFuture<User> userFuture = cache.getAsync("user123");

CompletableFuture<Void> putFuture = cache.putAsync("user123", user);

CompletableFuture<Void> invalidateFuture = cache.invalidateAsync("user123");

// Combine async operations
CompletableFuture<String> result = userFuture
    .thenCompose(user -> processUser(user))
    .thenApply(processedUser -> processedUser.getId());
```

---

## 🔗 **Integration Examples**

### Spring Boot Integration

```java
@Configuration
public class CacheConfiguration {

    @Bean
    public Cache<String, User> userCache() {
        return CacheConfig.<String, User>builder()
            .maximumSize(10_000L)
            .expireAfterWrite(Duration.ofMinutes(30))
            .loader(this::loadUser)
            .recordStats(true)
            .build()
            .createCache();
    }

    @Bean
    public void monitorCache(Cache<String, User> userCache) {
        CacheStats stats = userCache.stats();

        // Simple monitoring without complex health checks
        if (stats.hitRate() < 0.8) {
            logger.warn("Cache hit rate below threshold: {}%", stats.hitRate() * 100);
        }

                // Log key metrics
        logger.info("Cache metrics - Hit rate: {}%, Evictions: {}",
            stats.hitRate() * 100, stats.evictionCount());
    }

    private User loadUser(String userId) {
        return userRepository.findById(userId);
    }
}
```

### Metrics Integration

```java
@Component
public class CacheMetricsExporter {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private CacheMetrics cacheMetrics;

    @EventListener
    public void exportMetrics(CacheMetricsEvent event) {
        Map<String, Object> metrics = cacheMetrics.exportMetrics();

        metrics.forEach((name, value) -> {
            if (value instanceof Number) {
                Gauge.builder(name)
                    .register(meterRegistry, () -> ((Number) value).doubleValue());
            }
        });
    }
}
```

---

*This API reference provides comprehensive documentation for all JCacheX public APIs. For additional examples and best practices, see the [Architecture Guide](ARCHITECTURE.md) and example projects.*
