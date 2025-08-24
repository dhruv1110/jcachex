---
id: examples
title: Examples
sidebar_label: Examples
description: Comprehensive examples and usage patterns for JCacheX
---

# JCacheX Examples

This section provides comprehensive examples and usage patterns for JCacheX, covering Java, Kotlin, and Spring Boot integration.

## Quick Navigation

- **[Java Examples](/docs/examples/java-examples)** - Core Java usage patterns
- **[Kotlin Examples](/docs/examples/kotlin-examples)** - Kotlin DSL and extensions
- **[Spring Boot Examples](/docs/examples/spring-boot-examples)** - Spring integration
- **[Distributed Examples](/docs/examples/distributed-examples)** - Multi-node caching
- **[Advanced Examples](/docs/examples/advanced-examples)** - Complex use cases

## Basic Usage Patterns

### 1. Simple Cache

```java
import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.JCacheXBuilder;

public class BasicCacheExample {
    public static void main(String[] args) {
        // Create a basic cache
        Cache<String, String> cache = JCacheXBuilder.create()
            .name("basic-cache")
            .maximumSize(100L)
            .build();

        // Basic operations
        cache.put("key1", "value1");
        String value = cache.get("key1");
        System.out.println("Retrieved: " + value);

        // Check statistics
        System.out.println("Cache size: " + cache.size());
        System.out.println("Hit rate: " + cache.stats().hitRate() * 100 + "%");
    }
}
```

### 2. Profile-Based Cache

```java
public class ProfileBasedExample {
    public static void main(String[] args) {
        // Read-heavy workload (e.g., product catalog)
        Cache<String, Product> productCache = JCacheXBuilder.forReadHeavyWorkload()
            .name("products")
            .maximumSize(10000L)
            .build();

        // Write-heavy workload (e.g., logging system)
        Cache<String, LogEntry> logCache = JCacheXBuilder.forWriteHeavyWorkload()
            .name("logs")
            .maximumSize(5000L)
            .build();

        // API response caching
        Cache<String, ApiResponse> apiCache = JCacheXBuilder.forApiResponseCaching()
            .name("api-responses")
            .maximumSize(1000L)
            .build();
    }
}
```

### 3. Advanced Configuration

```java
public class AdvancedConfigExample {
    public static void main(String[] args) {
        Cache<String, User> userCache = JCacheXBuilder.forReadHeavyWorkload()
            .name("users")
            .maximumSize(5000L)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .recordStats(true)
            .listener(new CacheEventListener<String, User>() {
                @Override
                public void onEvict(String key, User value, EvictionReason reason) {
                    System.out.println("Evicted: " + key + " due to " + reason);
                }
            })
            .build();
    }
}
```

## Common Use Cases

### User Session Management

```java
public class SessionCacheExample {
    private final Cache<String, UserSession> sessionCache;

    public SessionCacheExample() {
        this.sessionCache = JCacheXBuilder.forSessionStorage()
            .name("user-sessions")
            .maximumSize(10000L)
            .expireAfterWrite(Duration.ofHours(2))
            .build();
    }

    public UserSession createSession(String userId) {
        UserSession session = new UserSession(userId, System.currentTimeMillis());
        sessionCache.put(session.getSessionId(), session);
        return session;
    }

    public UserSession getSession(String sessionId) {
        return sessionCache.get(sessionId);
    }

    public void invalidateSession(String sessionId) {
        sessionCache.remove(sessionId);
    }
}
```

### Product Catalog Caching

```java
public class ProductCatalogExample {
    private final Cache<String, Product> productCache;
    private final ProductRepository productRepository;

    public ProductCatalogExample(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.productCache = JCacheXBuilder.forReadHeavyWorkload()
            .name("products")
            .maximumSize(50000L)
            .loader(this::loadProductFromDatabase)
            .build();
    }

    public Product getProduct(String productId) {
        return productCache.get(productId);
    }

    public void updateProduct(Product product) {
        productRepository.save(product);
        productCache.put(product.getId(), product);
    }

    public void removeProduct(String productId) {
        productRepository.deleteById(productId);
        productCache.remove(productId);
    }

    private Product loadProductFromDatabase(String productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
```

### API Response Caching

```java
public class ApiCacheExample {
    private final Cache<String, ApiResponse> apiCache;
    private final HttpClient httpClient;

    public ApiCacheExample(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.apiCache = JCacheXBuilder.forApiResponseCaching()
            .name("api-responses")
            .maximumSize(1000L)
            .expireAfterWrite(Duration.ofMinutes(15))
            .loader(this::callExternalApi)
            .build();
    }

    public ApiResponse getApiData(String endpoint, Map<String, String> params) {
        String cacheKey = buildCacheKey(endpoint, params);
        return apiCache.get(cacheKey);
    }

    private String buildCacheKey(String endpoint, Map<String, String> params) {
        return endpoint + "?" + params.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .sorted()
            .collect(Collectors.joining("&"));
    }

    private ApiResponse callExternalApi(String cacheKey) {
        // Parse cache key back to endpoint and params
        // Make HTTP call
        return httpClient.get(cacheKey);
    }
}
```

## Performance Optimization Examples

### Bulk Operations

```java
public class BulkOperationsExample {
    private final Cache<String, User> userCache;

    public BulkOperationsExample() {
        this.userCache = JCacheXBuilder.forReadHeavyWorkload()
            .name("users")
            .maximumSize(10000L)
            .build();
    }

    public Map<String, User> getUsers(List<String> userIds) {
        // Use bulk get for better performance
        return userCache.getAll(userIds);
    }

    public void createUsers(Map<String, User> users) {
        // Use bulk put for better performance
        userCache.putAll(users);
    }

    public void removeUsers(List<String> userIds) {
        // Use bulk remove for better performance
        userCache.removeAll(userIds);
    }
}
```

### Async Operations

```java
public class AsyncOperationsExample {
    private final Cache<String, Data> dataCache;

    public AsyncOperationsExample() {
        this.dataCache = JCacheXBuilder.forHighPerformance()
            .name("async-data")
            .maximumSize(5000L)
            .build();
    }

    public CompletableFuture<Data> getDataAsync(String key) {
        return dataCache.getAsync(key);
    }

    public CompletableFuture<Void> storeDataAsync(String key, Data data) {
        return dataCache.putAsync(key, data);
    }

    public void processDataAsync(String key) {
        dataCache.getAsync(key)
            .thenAccept(data -> {
                if (data != null) {
                    processData(data);
                }
            })
            .exceptionally(throwable -> {
                System.err.println("Error processing data: " + throwable.getMessage());
                return null;
            });
    }

    private void processData(Data data) {
        // Process the data asynchronously
        System.out.println("Processing: " + data);
    }
}
```

## Event Handling Examples

### Cache Event Monitoring

```java
public class EventMonitoringExample {
    private final Cache<String, User> userCache;

    public EventMonitoringExample() {
        this.userCache = JCacheXBuilder.forReadHeavyWorkload()
            .name("users")
            .maximumSize(1000L)
            .listener(new CacheEventListener<String, User>() {
                @Override
                public void onPut(String key, User value) {
                    System.out.println("User added: " + key);
                }

                @Override
                public void onGet(String key, User value) {
                    System.out.println("User retrieved: " + key);
                }

                @Override
                public void onRemove(String key, User value) {
                    System.out.println("User removed: " + key);
                }

                @Override
                public void onEvict(String key, User value, EvictionReason reason) {
                    System.out.println("User evicted: " + key + " due to " + reason);
                }
            })
            .build();
    }
}
```

### Statistics Monitoring

```java
public class StatisticsExample {
    private final Cache<String, Product> productCache;

    public StatisticsExample() {
        this.productCache = JCacheXBuilder.forReadHeavyWorkload()
            .name("products")
            .maximumSize(10000L)
            .recordStats(true)
            .build();
    }

    public void printStatistics() {
        CacheStats stats = productCache.stats();

        System.out.println("=== Cache Statistics ===");
        System.out.println("Hit Rate: " + String.format("%.2f%%", stats.hitRate() * 100));
        System.out.println("Miss Rate: " + String.format("%.2f%%", stats.missRate() * 100));
        System.out.println("Load Success Rate: " + String.format("%.2f%%", stats.loadSuccessRate() * 100));
        System.out.println("Average Load Time: " + stats.averageLoadPenalty() + "ms");
        System.out.println("Total Load Time: " + stats.totalLoadTime() + "ms");
        System.out.println("Total Requests: " + stats.requestCount());
        System.out.println("Hit Count: " + stats.hitCount());
        System.out.println("Miss Count: " + stats.missCount());
        System.out.println("Load Count: " + stats.loadCount());
        System.out.println("Eviction Count: " + stats.evictionCount());
    }

    public void monitorPerformance() {
        // Monitor cache performance over time
        Timer.scheduleAtFixedRate(() -> {
            CacheStats stats = productCache.stats();
            if (stats.hitRate() < 0.8) {
                System.out.println("Warning: Low hit rate detected: " +
                    String.format("%.2f%%", stats.hitRate() * 100));
            }
        }, 0, 60000); // Check every minute
    }
}
```

## Distributed Caching Example

### Multi-Node Cache Sharing

```java
public class DistributedCacheExample {
    private final Cache<String, Product> productCache;
    
    public DistributedCacheExample() {
        // Use distributed caching profile for multi-node environments
        this.productCache = JCacheXBuilder.forDistributedCaching()
            .name("distributed-products")
            .maximumSize(100000L)
            .recordStats(true)
            .build();
    }
    
    public Product getProduct(String id) {
        // This will work across all service instances
        Product cached = productCache.get(id);
        if (cached != null) {
            return cached;
        }
        
        // Cache miss - load from database
        Product product = productRepository.findById(id);
        if (product != null) {
            // Store in distributed cache - visible to all instances
            productCache.put(id, product);
        }
        
        return product;
    }
    
    public void updateProduct(String id, Product product) {
        // Update database
        productRepository.update(id, product);
        
        // Update distributed cache - all instances see the change
        productCache.put(id, product);
    }
}
```

> **💡 Note**: For complete Kubernetes setup and configuration details, see the **[Distributed Caching section in API Reference](api-reference#distributed-caching)**.

## Next Steps

Explore more examples in the specific sections:

- **[Java Examples](/docs/examples/java-examples)** - Detailed Java patterns
- **[Kotlin Examples](/docs/examples/kotlin-examples)** - Kotlin DSL usage
- **[Spring Boot Examples](/docs/examples/spring-boot-examples)** - Spring integration
- **[Distributed Examples](/docs/examples/distributed-examples)** - Multi-node scenarios
- **[Advanced Examples](/docs/examples/advanced-examples)** - Complex use cases

---

**Need more specific examples?** Check out the detailed sections above or explore our [API Reference](/docs/api-reference) for complete method documentation.
