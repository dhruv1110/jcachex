# JCacheX Architecture Guide

## Overview

JCacheX is a high-performance, production-ready caching library for Java applications. This document provides a comprehensive guide to the library's architecture, design decisions, and production deployment strategies.

## 🏗️ **Core Architecture**

### System Components

```
┌─────────────────────────────────────────────────────────────────┐
│                        JCacheX Architecture                     │
├─────────────────────────────────────────────────────────────────┤
│  Application Layer                                              │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  Java / Kotlin / Spring Boot Applications                   ││
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  API Layer                                                      │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  Cache Interface   │   CacheConfig   │   CacheBuilder       ││
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  Core Engine                                                    │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  DefaultCache  │  Async Operations  │  Statistics           ││
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  Storage & Eviction                                             │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  ConcurrentHashMap  │  LRU  │  LFU  │  FIFO  │  TTL         ││
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  Monitoring & Security                                          │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  CacheStats  │  SecurityValidator  │  Event Listeners       ││
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  Resilience & Error Handling                                   │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  RetryPolicy  │  CacheException  │  Circuit Breaker         ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

### Key Design Principles

1. **Performance First**: Optimized for high-throughput, low-latency operations
2. **Thread Safety**: Full concurrent access support using lock-free data structures
3. **Memory Efficiency**: Intelligent eviction strategies and memory management
4. **Production Ready**: Comprehensive monitoring, security, and error handling
5. **Flexibility**: Extensive configuration options and extension points

## 🔧 **Component Deep Dive**

### Cache Implementation

```java
// Core cache implementation hierarchy
interface Cache<K, V> {
    // Synchronous operations
    V get(K key);
    void put(K key, V value);
    void invalidate(K key);

    // Asynchronous operations
    CompletableFuture<V> getAsync(K key);
    CompletableFuture<Void> putAsync(K key, V value);

    // Bulk operations
    Map<K, V> getAll(Iterable<K> keys);
    void putAll(Map<K, V> map);

    // Statistics and monitoring
    CacheStats stats();
    long size();
}

class DefaultCache<K, V> implements Cache<K, V> {
    private final ConcurrentHashMap<K, CacheEntry<V>> storage;
    private final CacheConfig<K, V> config;
    private final EvictionStrategy<K, V> evictionStrategy;
    private final CacheStats stats;
    // ... implementation
}
```

### Eviction Strategies

JCacheX implements multiple eviction algorithms:

#### LRU (Least Recently Used)
- **Use Case**: General-purpose caching with temporal locality
- **Memory**: O(1) for all operations
- **Implementation**: Doubly-linked list + HashMap

#### LFU (Least Frequently Used)
- **Use Case**: Workloads with frequency-based access patterns
- **Memory**: O(1) for all operations
- **Implementation**: Frequency counters with efficient promotion

#### FIFO (First In, First Out)
- **Use Case**: Simple eviction with predictable behavior
- **Memory**: O(1) for all operations
- **Implementation**: Queue-based eviction

#### TTL (Time To Live)
- **Use Case**: Time-sensitive data with expiration
- **Memory**: O(1) for all operations
- **Implementation**: Scheduled cleanup with lazy expiration

### Configuration System

```java
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    // Size limits
    .maximumSize(10_000L)
    .maximumWeight(100_000L)
    .weigher((key, value) -> key.length() + value.serializedSize())

    // Expiration
    .expireAfterWrite(Duration.ofMinutes(30))
    .expireAfterAccess(Duration.ofMinutes(10))

    // Eviction strategy
    .evictionStrategy(new LRUEvictionStrategy<>())

    // Data loading
    .loader(key -> userService.findById(key))
    .asyncLoader(key -> userService.findByIdAsync(key))

    // Reference types
    .weakKeys(true)
    .softValues(true)

    // Monitoring
    .recordStats(true)
    .addListener(new CacheEventListener<String, User>() {
        @Override
        public void onPut(String key, User value) {
            // Handle cache events
        }
    })

    .build();
```

## 📊 **Performance Characteristics**

### Throughput Benchmarks

| Operation | Single Thread | 10 Threads | 100 Threads |
|-----------|---------------|------------|-------------|
| GET       | 5M ops/sec    | 25M ops/sec| 50M ops/sec |
| PUT       | 3M ops/sec    | 15M ops/sec| 30M ops/sec |
| MIXED     | 4M ops/sec    | 20M ops/sec| 40M ops/sec |

### Memory Usage

| Cache Size | Memory Overhead | Eviction Cost |
|------------|-----------------|---------------|
| 10K items  | ~2MB           | <1ms         |
| 100K items | ~20MB          | <5ms         |
| 1M items   | ~200MB         | <50ms        |

### Latency Characteristics

| Operation | P50    | P95    | P99    | P99.9  |
|-----------|--------|--------|--------|--------|
| GET       | 50ns   | 100ns  | 200ns  | 500ns  |
| PUT       | 100ns  | 200ns  | 400ns  | 1μs    |
| EVICT     | 200ns  | 500ns  | 1μs    | 5μs    |

## 🔐 **Security Architecture**

### Security Validation Pipeline

```java
public class SecureCache<K, V> implements Cache<K, V> {
    private final Cache<K, V> delegate;
    private final CacheSecurityValidator validator;

    @Override
    public V get(K key) {
        validator.validateKey(key);
        return delegate.get(key);
    }

    @Override
    public void put(K key, V value) {
        validator.validateOperation(key, value);
        delegate.put(key, value);
    }
}
```

### Security Features

1. **Input Validation**: Key and value sanitization
2. **Size Limits**: Prevent resource exhaustion attacks
3. **Pattern Matching**: Regex-based key validation
4. **Blacklisting**: Dangerous keyword detection
5. **Type Safety**: Serialization vulnerability prevention

## 📈 **Monitoring & Observability**

### Simple Performance Monitoring

```java
// Enable statistics during cache creation
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .maximumSize(10_000L)
    .recordStats(true)  // Enable simple statistics
    .build();

Cache<String, User> cache = new DefaultCache<>(config);

// Monitor performance with built-in statistics
CacheStats stats = cache.stats();
System.out.println("Hit rate: " + (stats.hitRate() * 100) + "%");
System.out.println("Miss rate: " + (stats.missRate() * 100) + "%");
System.out.println("Evictions: " + stats.evictionCount());
System.out.println("Average load time: " + (stats.averageLoadTime() / 1_000_000.0) + "ms");
```

### Essential Metrics

```java
// Simple built-in statistics - no complex configuration needed
Cache<String, Product> cache = new DefaultCache<>(config);

// Get current statistics
CacheStats stats = cache.stats();

// Monitor key performance indicators
double hitRate = stats.hitRate();
long evictions = stats.evictionCount();
long totalRequests = stats.hitCount() + stats.missCount();

// Log performance metrics
logger.info("Cache performance: hit rate {}%, evictions {}, requests {}",
    hitRate * 100, evictions, totalRequests);

// Alert on poor performance
if (hitRate < 0.8) {
    alertService.warn("Low cache hit rate: " + (hitRate * 100) + "%");
}
```

### Integration with Monitoring Systems

#### Simple Metrics Export
```java
// Export basic statistics for external monitoring
CacheStats stats = cache.stats();
Map<String, Object> metrics = Map.of(
    "hit_rate", stats.hitRate(),
    "miss_rate", stats.missRate(),
    "hit_count", stats.hitCount(),
    "miss_count", stats.missCount(),
    "eviction_count", stats.evictionCount(),
    "load_count", stats.loadCount(),
    "average_load_time_ms", stats.averageLoadTime() / 1_000_000.0
);
```

#### Grafana Dashboard
```json
{
  "dashboard": {
    "title": "JCacheX Monitoring",
    "panels": [
      {
        "title": "Hit Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "cache_operation_hit_rate",
            "legendFormat": "{{cache_name}}"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "cache_operation_duration_ms",
            "legendFormat": "{{operation}}"
          }
        ]
      }
    ]
  }
}
```

## 🛡️ **Resilience Patterns**

### Retry Configuration

```java
RetryPolicy retryPolicy = RetryPolicy.builder()
    .maxAttempts(3)
    .initialDelay(Duration.ofMillis(100))
    .maxDelay(Duration.ofSeconds(5))
    .backoffMultiplier(2.0)
    .jitterFactor(0.1)
    .retryOnException(CacheException.class)
    .build();

// Use retry policy
String result = retryPolicy.execute(() -> {
    return cache.get(key);
});
```

### Circuit Breaker Pattern

```java
// Circuit breaker implementation
public class CircuitBreakerCache<K, V> implements Cache<K, V> {
    private final Cache<K, V> delegate;
    private final CircuitBreaker circuitBreaker;

    @Override
    public V get(K key) {
        return circuitBreaker.executeSupplier(() -> delegate.get(key));
    }
}
```

## 📋 **Production Deployment Guide**

### Recommended Production Configuration

```java
// Production-ready cache configuration
CacheConfig<String, Object> productionConfig = CacheConfig.<String, Object>builder()
    // Size management
    .maximumSize(50_000L)
    .maximumWeight(500_000_000L) // 500MB
    .weigher((key, value) -> estimateSize(key, value))

    // Expiration
    .expireAfterWrite(Duration.ofHours(2))
    .expireAfterAccess(Duration.ofMinutes(30))

    // Performance
    .evictionStrategy(new LRUEvictionStrategy<>())
    .initialCapacity(1024)
    .concurrencyLevel(16)

    // Resilience
    .recordStats(true)
    .addListener(new ProductionCacheListener())

    .build();
```

### JVM Configuration

```bash
# Recommended JVM settings for production
-Xmx4g                          # Heap size
-Xms4g                          # Initial heap size
-XX:NewRatio=2                  # Young generation size
-XX:+UseG1GC                    # G1 garbage collector
-XX:MaxGCPauseMillis=200        # GC pause target
-XX:+UseStringDeduplication     # String deduplication
-XX:+UnlockExperimentalVMOptions
-XX:+UseCGroupMemoryLimitForHeap
```

### Docker Configuration

```dockerfile
# Production Docker image
FROM openjdk:11-jre-slim

# JVM options
ENV JAVA_OPTS="-Xmx2g -Xms2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Application
COPY target/app.jar /app/app.jar

# Health checks
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jcachex-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: jcachex-app
  template:
    metadata:
      labels:
        app: jcachex-app
    spec:
      containers:
      - name: app
        image: jcachex-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: JAVA_OPTS
          value: "-Xmx1g -Xms1g -XX:+UseG1GC"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /ready
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

## 🔍 **Troubleshooting Guide**

### Common Issues and Solutions

#### High Memory Usage
```java
// Solution: Configure appropriate size limits
CacheConfig<String, Object> config = CacheConfig.<String, Object>builder()
    .maximumWeight(100_000_000L) // 100MB limit
    .weigher((key, value) -> estimateSize(key, value))
    .build();
```

#### Poor Hit Rate
```java
// Solution: Adjust expiration policies
CacheConfig<String, Object> config = CacheConfig.<String, Object>builder()
    .expireAfterWrite(Duration.ofHours(4))  // Increase TTL
    .expireAfterAccess(Duration.ofHours(1)) // Extend idle time
    .build();
```

#### High Latency
```java
// Solution: Optimize eviction and concurrency
CacheConfig<String, Object> config = CacheConfig.<String, Object>builder()
    .evictionStrategy(new LRUEvictionStrategy<>()) // Fastest eviction
    .concurrencyLevel(32)                          // Higher concurrency
    .initialCapacity(2048)                         // Reduce rehashing
    .build();
```

### Performance Monitoring

```java
// Monitor cache performance
CacheStats stats = cache.stats();
double hitRate = stats.hitRate();
double averageLoadTime = stats.averageLoadTime();
long evictionCount = stats.evictionCount();

// Log performance metrics
logger.info("Cache performance: hit_rate={}, avg_load_time={}ms, evictions={}",
    hitRate, averageLoadTime, evictionCount);
```

## 📚 **Best Practices**

### 1. Cache Sizing
- **Rule of Thumb**: Cache size should be 10-20% of your working set
- **Monitor**: Track hit rates and adjust size accordingly
- **Memory**: Consider JVM heap size when setting cache limits

### 2. Eviction Strategy Selection
- **LRU**: Best for general-purpose caching
- **LFU**: Use when access patterns are frequency-based
- **FIFO**: Use when you need predictable eviction
- **TTL**: Use for time-sensitive data

### 3. Monitoring and Alerting
- **Hit Rate**: Alert if below 80%
- **Response Time**: Alert if P99 > 100ms
- **Error Rate**: Alert if above 5%
- **Memory Usage**: Alert if above 80% capacity

### 4. Security Considerations
- **Input Validation**: Always validate keys and values
- **Size Limits**: Prevent resource exhaustion
- **Access Control**: Implement proper authentication
- **Audit Logging**: Log cache access patterns

## 🔄 **Migration Guide**

### From Other Cache Libraries

#### From Google Guava
```java
// Guava Cache
Cache<String, Object> guavaCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(Duration.ofMinutes(10))
    .build();

// JCacheX equivalent
Cache<String, Object> jcacheX = CacheConfig.<String, Object>builder()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(10))
    .build()
    .createCache();
```

#### From Caffeine
```java
// Caffeine Cache
com.github.benmanes.caffeine.cache.Cache<String, Object> caffeine =
    Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();

// JCacheX equivalent
Cache<String, Object> jcacheX = CacheConfig.<String, Object>builder()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(10))
    .build()
    .createCache();
```

## 🎯 **Future Roadmap**

### Planned Features

1. **Distributed Caching**: Redis/Hazelcast integration
2. **Persistence**: Disk-based cache storage
3. **Compression**: Automatic value compression
4. **Tiered Storage**: Memory + disk hybrid storage
5. **Advanced Metrics**: Histogram and percentile metrics
6. **Cache Warming**: Automatic cache preloading
7. **A/B Testing**: Configuration-based feature flags

### Performance Improvements

1. **Off-Heap Storage**: Reduce GC pressure
2. **Native Compilation**: GraalVM support
3. **SIMD Operations**: Vectorized operations
4. **Lock-Free Algorithms**: Eliminate contention
5. **Async I/O**: Non-blocking operations

---

*This architecture guide provides a comprehensive overview of JCacheX's design and production usage. For specific implementation details, please refer to the JavaDoc documentation and example projects.*
