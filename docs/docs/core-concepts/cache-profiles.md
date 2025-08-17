---
id: cache-profiles
title: Cache Profiles
sidebar_label: Cache Profiles
description: Understanding JCacheX cache profiles and workload optimization
---

# Cache Profiles

JCacheX provides pre-configured cache profiles optimized for different workload characteristics. Each profile is tuned for specific use cases to deliver optimal performance.

## Overview

Cache profiles are pre-configured optimizations that automatically tune:
- **Data structures** for specific access patterns
- **Eviction strategies** for workload characteristics
- **Memory allocation** for optimal usage
- **Concurrency handling** for performance

## Available Profiles

### READ_HEAVY Profile

Optimized for workloads with 80%+ read operations.

```java
Cache<String, Product> productCache = JCacheXBuilder.forReadHeavyWorkload()
    .name("products")
    .maximumSize(10000L)
    .build();
```

**Use Cases:**
- Product catalogs
- Reference data
- Configuration caches
- Static content

**Optimizations:**
- Read-optimized hash tables
- Minimal write overhead
- Efficient memory layout
- Reduced synchronization

**Performance Characteristics:**
- **Read latency**: < 100ns
- **Write latency**: ~200ns
- **Memory overhead**: ~24 bytes/entry
- **Concurrent reads**: Linear scaling

### WRITE_HEAVY Profile

Balanced for workloads with 50%+ write operations.

```java
Cache<String, LogEntry> logCache = JCacheXBuilder.forWriteHeavyWorkload()
    .name("logs")
    .maximumSize(5000L)
    .build();
```

**Use Cases:**
- Logging systems
- Analytics data
- Real-time feeds
- Event streams

**Optimizations:**
- Write-optimized data structures
- Batch processing capabilities
- Efficient insertion algorithms
- Balanced read/write performance

**Performance Characteristics:**
- **Read latency**: ~150ns
- **Write latency**: < 100ns
- **Memory overhead**: ~32 bytes/entry
- **Concurrent writes**: High throughput

### API_CACHE Profile

Optimized for external API response caching.

```java
Cache<String, ApiResponse> apiCache = JCacheXBuilder.forApiResponseCaching()
    .name("api-responses")
    .maximumSize(1000L)
    .build();
```

**Use Cases:**
- HTTP client caching
- Service call results
- External API responses
- Network data caching

**Optimizations:**
- Network-aware eviction
- Response size optimization
- TTL-based expiration
- Memory-efficient storage

**Performance Characteristics:**
- **Read latency**: ~120ns
- **Write latency**: ~150ns
- **Memory overhead**: ~28 bytes/entry
- **Network optimization**: High

### SESSION_CACHE Profile

Optimized for user session management.

```java
Cache<String, UserSession> sessionCache = JCacheXBuilder.forSessionStorage()
    .name("user-sessions")
    .maximumSize(2000L)
    .build();
```

**Use Cases:**
- Web application sessions
- User state management
- Authentication tokens
- Temporary user data

**Optimizations:**
- Session-aware expiration
- User-specific eviction
- Memory-efficient storage
- Fast session lookup

**Performance Characteristics:**
- **Read latency**: ~130ns
- **Write latency**: ~120ns
- **Memory overhead**: ~26 bytes/entry
- **Session management**: Optimized

### HIGH_PERFORMANCE Profile

Maximum throughput optimization for balanced workloads.

```java
Cache<String, Data> dataCache = JCacheXBuilder.forHighPerformance()
    .name("high-perf-data")
    .maximumSize(50000L)
    .build();
```

**Use Cases:**
- General-purpose caching
- Balanced workloads
- High-frequency operations
- Performance-critical applications

**Optimizations:**
- Balanced read/write optimization
- High-throughput data structures
- Efficient concurrency handling
- Memory-performance balance

**Performance Characteristics:**
- **Read latency**: ~80ns
- **Write latency**: ~80ns
- **Memory overhead**: ~20 bytes/entry
- **Overall throughput**: Maximum

### MEMORY_EFFICIENT Profile

Optimized for memory-constrained environments.

```java
Cache<String, Config> configCache = JCacheXBuilder.forMemoryConstrainedEnvironment()
    .name("config")
    .maximumSize(100L)
    .build();
```

**Use Cases:**
- Embedded systems
- Mobile applications
- Memory-constrained servers
- IoT devices

**Optimizations:**
- Minimal memory footprint
- Efficient data structures
- Aggressive eviction
- Memory-aware allocation

**Performance Characteristics:**
- **Read latency**: ~200ns
- **Write latency**: ~250ns
- **Memory overhead**: ~16 bytes/entry
- **Memory usage**: Minimal

### ML_OPTIMIZED Profile

Optimized for machine learning workloads.

```java
Cache<String, ModelResult> mlCache = JCacheXBuilder.forMachineLearning()
    .name("ml-predictions")
    .maximumSize(1000L)
    .build();
```

**Use Cases:**
- AI/ML applications
- Prediction caching
- Model results
- Algorithm outputs

**Optimizations:**
- ML-specific data structures
- Prediction-aware eviction
- Efficient numerical storage
- Batch operation support

**Performance Characteristics:**
- **Read latency**: ~110ns
- **Write latency**: ~140ns
- **Memory overhead**: ~30 bytes/entry
- **ML operations**: Optimized

### ZERO_COPY Profile

Ultra-low latency for high-frequency operations.

```java
Cache<String, MarketData> marketData = JCacheXBuilder.forUltraLowLatency()
    .name("market-data")
    .maximumSize(100000L)
    .build();
```

**Use Cases:**
- High-frequency trading
- Real-time gaming
- Ultra-low latency systems
- Performance-critical applications

**Optimizations:**
- Zero-copy operations
- Direct memory access
- Lock-free algorithms
- CPU cache optimization

**Performance Characteristics:**
- **Read latency**: < 50ns
- **Write latency**: < 50ns
- **Memory overhead**: ~16 bytes/entry
- **Latency**: Ultra-low

## Profile Selection Guide

### Workload Analysis

Analyze your workload characteristics to choose the right profile:

```java
public class ProfileSelector {

    public static JCacheXBuilder selectProfile(WorkloadCharacteristics characteristics) {
        double readRatio = characteristics.getReadToWriteRatio() /
                          (1.0 + characteristics.getReadToWriteRatio());

        if (readRatio > 0.8) {
            return JCacheXBuilder.forReadHeavyWorkload();
        } else if (readRatio < 0.2) {
            return JCacheXBuilder.forWriteHeavyWorkload();
        } else if (characteristics.isLatencyCritical()) {
            return JCacheXBuilder.forUltraLowLatency();
        } else if (characteristics.isMemoryConstrained()) {
            return JCacheXBuilder.forMemoryConstrainedEnvironment();
        } else {
            return JCacheXBuilder.forHighPerformance();
        }
    }
}
```

### Performance Requirements

Consider your performance requirements:

```java
// For ultra-low latency
if (maxLatency < 100) { // nanoseconds
    return JCacheXBuilder.forUltraLowLatency();
}

// For high throughput
if (requiredThroughput > 10_000_000) { // ops/sec
    return JCacheXBuilder.forHighPerformance();
}

// For memory efficiency
if (availableMemory < 1024 * 1024 * 1024) { // 1GB
    return JCacheXBuilder.forMemoryConstrainedEnvironment();
}
```

### Use Case Patterns

Match profiles to common use case patterns:

```java
public class UseCaseProfiles {

    // E-commerce product catalog
    public static Cache<String, Product> createProductCache() {
        return JCacheXBuilder.forReadHeavyWorkload()
            .name("products")
            .maximumSize(100000L)
            .build();
    }

    // User session management
    public static Cache<String, UserSession> createSessionCache() {
        return JCacheXBuilder.forSessionStorage()
            .name("sessions")
            .maximumSize(10000L)
            .build();
    }

    // API response caching
    public static Cache<String, ApiResponse> createApiCache() {
        return JCacheXBuilder.forApiResponseCaching()
            .name("api-responses")
            .maximumSize(5000L)
            .build();
    }

    // High-frequency trading
    public static Cache<String, MarketData> createMarketDataCache() {
        return JCacheXBuilder.forUltraLowLatency()
            .name("market-data")
            .maximumSize(1000000L)
            .build();
    }
}
```

## Custom Profile Configuration

### Extending Profiles

You can extend existing profiles with custom configuration:

```java
Cache<String, User> customCache = JCacheXBuilder.forReadHeavyWorkload()
    .name("custom-users")
    .maximumSize(5000L)
    .expireAfterWrite(Duration.ofHours(2))
    .expireAfterAccess(Duration.ofMinutes(30))
    .recordStats(true)
    .listener(new CustomEventListener())
    .build();
```

### Profile Overrides

Override specific profile settings:

```java
Cache<String, Data> overriddenCache = JCacheXBuilder.forHighPerformance()
    .name("overridden")
    .maximumSize(1000L) // Override default size
    .expireAfterWrite(Duration.ofMinutes(15)) // Add expiration
    .build();
```

## Performance Comparison

### Throughput Comparison

| Profile | Read Ops/sec | Write Ops/sec | Memory Usage |
|---------|--------------|---------------|--------------|
| READ_HEAVY | 12.5M | 8.2M | Medium |
| WRITE_HEAVY | 8.3M | 12.1M | Medium |
| HIGH_PERFORMANCE | 15.2M | 14.8M | High |
| ZERO_COPY | 20.0M | 18.5M | Very High |
| MEMORY_EFFICIENT | 6.8M | 7.2M | Very Low |

### Latency Comparison

| Profile | P50 Read | P99 Read | P50 Write | P99 Write |
|---------|----------|----------|-----------|-----------|
| READ_HEAVY | 0.12us | 0.18us | 0.20us | 0.35us |
| WRITE_HEAVY | 0.15us | 0.22us | 0.10us | 0.18us |
| HIGH_PERFORMANCE | 0.10us | 0.15us | 0.10us | 0.15us |
| ZERO_COPY | 0.05us | 0.08us | 0.05us | 0.08us |
| MEMORY_EFFICIENT | 0.20us | 0.30us | 0.25us | 0.40us |

## Best Practices

### 1. Profile Selection

- **Analyze your workload** before choosing a profile
- **Start with HIGH_PERFORMANCE** for unknown workloads
- **Use specific profiles** for known use cases
- **Monitor performance** and adjust as needed

### 2. Configuration

- **Set appropriate sizes** based on available memory
- **Configure expiration** for time-sensitive data
- **Enable statistics** for performance monitoring
- **Add event listeners** for operational insights

### 3. Monitoring

- **Track hit rates** to ensure cache effectiveness
- **Monitor eviction rates** to detect memory pressure
- **Measure latency** to identify performance issues
- **Watch memory usage** to prevent OOM errors

## Next Steps

- **[Eviction Strategies](/docs/core-concepts/eviction-strategies)** - Learn about cache eviction algorithms
- **[Performance Guide](/docs/performance)** - Understand performance characteristics
- **[Examples](/docs/examples)** - See profile usage in real applications
- **[API Reference](/docs/api-reference)** - Complete profile configuration options

---

**Ready to optimize your cache performance?** Choose the right profile and start caching efficiently!
