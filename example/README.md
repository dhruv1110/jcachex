# JCacheX Examples

This directory contains comprehensive examples demonstrating the advanced features of JCacheX, including the new cache types, enhanced eviction strategies, and frequency sketch options.

## Overview of New Features

### 🚀 Performance Achievements
- **ZeroCopy Cache**: 7.9ns GET (2.6x faster than Caffeine)
- **Locality Optimized**: 9.7ns GET (1.9x faster than Caffeine)
- **JIT Optimized**: 24.6ns GET, 63.8ns PUT (balanced performance)
- **All eviction strategies**: O(1) operations

### 🎯 Default Changes
- **TinyWindowLFU** is now the default eviction strategy (replacing LRU)
- **Frequency sketches** provide improved eviction accuracy
- **CacheBuilder** replaces direct instantiation for better ergonomics

## Examples

### Java Example (`java/`)
Comprehensive Java example showcasing:
- **Basic usage** with new CacheBuilder API
- **Specialized cache types** for different workloads
- **Enhanced eviction strategies** (Enhanced LRU, Enhanced LFU, TinyWindowLFU)
- **Frequency sketch options** (NONE, BASIC, OPTIMIZED)
- **Performance testing** and benchmarking
- **Event listeners** for monitoring

**Key Demonstrations:**
```java
// Read-heavy workload (fastest GET performance)
Cache<String, String> readCache = CacheBuilder.forReadHeavyWorkload()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofHours(2))
    .build();

// Enhanced LFU with optimized frequency sketch
Cache<String, String> enhancedLFU = CacheBuilder.newBuilder()
    .cacheType(CacheType.DEFAULT)
    .evictionStrategy(EvictionStrategy.ENHANCED_LFU)
    .frequencySketchType(FrequencySketchType.OPTIMIZED)
    .build();
```

**Run:**
```bash
cd example/java
./gradlew run
```

### Kotlin Example (`kotlin/`)
Advanced Kotlin example featuring:
- **Idiomatic Kotlin DSL** for cache configuration
- **Coroutines integration** with async operations
- **Specialized cache factory methods** (createReadOnlyOptimizedCache, etc.)
- **Extension properties** for cache type identification
- **Performance comparison** between cache types
- **Operator overloading** for intuitive cache access

**Key Demonstrations:**
```kotlin
// Kotlin DSL with TinyWindowLFU default
val userCache = cache<String, User> {
    maxSize = 1000
    expireAfterWrite = 2.hours
    frequencySketchType = FrequencySketchType.BASIC
    recordStats = true
}

// Specialized cache types
val readOnlyCache = createReadOnlyOptimizedCache<String, Product> {
    maxSize = 5000
    expireAfterWrite = 2.hours
}

// Async operations with coroutines
val user = userCache.getOrPut(userId) {
    loadUserFromDatabase(userId)
}
```

**Run:**
```bash
cd example/kotlin
./gradlew run
```

### SpringBoot Example (`springboot/`)
Production-ready SpringBoot integration demonstrating:
- **Multiple cache bean configurations** for different use cases
- **Cache type specialization** (read-heavy, write-heavy, high-performance)
- **REST endpoints** with performance testing
- **Cache statistics** and monitoring endpoints
- **Real-world usage patterns** with different data types

**Key Features:**
- `/users/{id}` - User cache with TinyWindowLFU
- `/products/{id}` - Read-optimized cache for product data
- `/sessions/{userId}` - Write-optimized cache for session management
- `/analytics/{metric}` - Enhanced LFU with optimized frequency sketch
- `/cache/stats` - Comprehensive cache statistics
- `/cache/performance` - Performance testing endpoints

**Configuration Examples:**
```kotlin
// Read-heavy cache for product data
@Bean
fun productCache(): Cache<String, Product> {
    return CacheBuilder.forReadHeavyWorkload()
        .maximumSize(5000L)
        .expireAfterWrite(Duration.ofHours(2))
        .recordStats(true)
        .build()
}

// Enhanced LFU with optimized frequency sketch
@Bean
fun analyticsCache(): Cache<String, AnalyticsData> {
    return CacheBuilder.newBuilder()
        .cacheType(CacheType.JIT_OPTIMIZED)
        .evictionStrategy(EvictionStrategy.ENHANCED_LFU)
        .frequencySketchType(FrequencySketchType.OPTIMIZED)
        .build()
}
```

**Run:**
```bash
cd example/springboot
./gradlew bootRun
```

**Test Endpoints:**
- `curl http://localhost:8080/users/123`
- `curl http://localhost:8080/products/456`
- `curl http://localhost:8080/cache/stats`
- `curl http://localhost:8080/cache/performance`

## Cache Types

### Performance-Specialized Caches

| Cache Type | Use Case | Performance | Best For |
|------------|----------|-------------|----------|
| `DEFAULT` | General purpose | 40.4ns GET, 92.6ns PUT | Balanced workloads |
| `ZERO_COPY_OPTIMIZED` | Read-heavy | **7.9ns GET** | Ultra-fast reads |
| `READ_ONLY_OPTIMIZED` | Read-only | **11.5ns GET** | Configuration, reference data |
| `WRITE_HEAVY_OPTIMIZED` | Write-heavy | **393.5ns PUT** | High write throughput |
| `LOCALITY_OPTIMIZED` | CPU cache friendly | **9.7ns GET** | Data structure locality |
| `JIT_OPTIMIZED` | Balanced | **24.6ns GET, 63.8ns PUT** | JIT-compiled environments |
| `ALLOCATION_OPTIMIZED` | Memory efficient | 39.7ns GET, 88.5ns PUT | GC-sensitive applications |
| `HARDWARE_OPTIMIZED` | Hardware specific | 24.7ns GET, 78.4ns PUT | Embedded systems |

### Convenience Factory Methods

```java
// CacheBuilder convenience methods
Cache<K, V> readCache = CacheBuilder.forReadHeavyWorkload();
Cache<K, V> writeCache = CacheBuilder.forWriteHeavyWorkload();
Cache<K, V> memoryCache = CacheBuilder.forMemoryConstrainedEnvironment();
Cache<K, V> performanceCache = CacheBuilder.forHighPerformance();
```

## Eviction Strategies

### Enhanced Strategies (All O(1))

| Strategy | Description | Performance | Use Case |
|----------|-------------|-------------|----------|
| `TINY_WINDOW_LFU` | **Default** - Hybrid LRU+LFU | O(1) | Optimal for most workloads |
| `ENHANCED_LRU` | LRU with frequency sketch | O(1) | Temporal locality + frequency |
| `ENHANCED_LFU` | LFU with frequency buckets | O(1) | Clear frequency patterns |
| `LRU` | Least Recently Used | O(1) | Temporal locality |
| `LFU` | Least Frequently Used | O(1) | Frequency-based access |
| `FIFO/FILO` | Queue/Stack eviction | O(1) | Predictable patterns |
| `WEIGHT_BASED` | Memory weight-based | O(1) | Memory-constrained |

### Frequency Sketch Options

| Type | Description | Memory Overhead | Accuracy |
|------|-------------|-----------------|----------|
| `NONE` | Pure algorithm | Minimal | Basic |
| `BASIC` | **Default** - Balanced | Moderate | Good |
| `OPTIMIZED` | Maximum accuracy | Higher | Excellent |

## Performance Benchmarks

The examples include performance testing that demonstrates:

### vs Caffeine Baseline
- **ZeroCopy**: 2.6x faster GET operations
- **Locality**: 1.9x faster GET operations
- **ReadOnly**: 1.6x faster GET operations
- **JIT**: Competitive across all operations

### O(1) Eviction Performance
All eviction strategies have been optimized to O(1) complexity:
- **LRU**: Doubly-linked list implementation
- **LFU**: Frequency bucket-based structure
- **FIFO/FILO**: Queue-based operations
- **TinyWindowLFU**: Hybrid approach with O(1) performance

## Running All Examples

```bash
# Java example
cd example/java && ./gradlew run

# Kotlin example
cd example/kotlin && ./gradlew run

# SpringBoot example
cd example/springboot && ./gradlew bootRun
```

## Key Takeaways

1. **TinyWindowLFU** is now the default eviction strategy for optimal performance
2. **Specialized cache types** provide significant performance improvements for specific workloads
3. **Frequency sketches** improve eviction accuracy with configurable overhead
4. **CacheBuilder** provides a fluent API for cache configuration
5. **O(1) eviction strategies** ensure consistent performance at scale
6. **Kotlin DSL** provides idiomatic cache configuration
7. **SpringBoot integration** supports all new features seamlessly

## Migration Guide

### From Previous Version

```java
// Old approach
CacheConfig<String, String> config = CacheConfig.<String, String>builder()
    .maximumSize(1000L)
    .evictionStrategy(new LRUEvictionStrategy<>())
    .build();
Cache<String, String> cache = new DefaultCache<>(config);

// New approach (TinyWindowLFU is default)
Cache<String, String> cache = CacheBuilder.newBuilder()
    .cacheType(CacheType.DEFAULT)
    .maximumSize(1000L)
    .build();

// Or use specialized cache for better performance
Cache<String, String> readCache = CacheBuilder.forReadHeavyWorkload()
    .maximumSize(1000L)
    .build();
```

The new examples showcase all these features in practical, real-world scenarios with comprehensive performance testing and monitoring.
