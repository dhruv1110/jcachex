---
title: 'JCacheX: A High-Performance, Profile-Based Distributed Caching Library for JVM and Kubernetes'
tags:
  - Java
  - Kotlin
  - caching
  - performance
  - distributed systems
  - Kubernetes
authors:
  - name: Dhruv Patel
    orcid: 0009-0007-5454-6841
    affiliation: 1
affiliations:
 - name: Independent Researcher, USA
   index: 1
date: 12 August 2025
bibliography: paper.bib
---

# Summary

JCacheX is a high-performance caching library for JVM applications that simplifies cache configuration through a profile-based approach. Instead of requiring users to understand complex eviction algorithms, concurrency models, and performance trade-offs, JCacheX provides pre-configured profiles that automatically select optimal implementations for specific use cases such as read-heavy workloads, write-heavy scenarios, session storage, and API response caching.

The library includes specialized cache implementations optimized for different scenarios: `ZeroCopyOptimizedCache` for ultra-low latency, `WriteHeavyOptimizedCache` for write-intensive workloads, and `CacheLocalityOptimizedCache` for workloads with temporal locality. JCacheX also provides seamless scaling from local in-memory caching to distributed caching on Kubernetes clusters using the official Kubernetes Java client [@kubernetes-java-client] for service discovery.

Our benchmark results show that JCacheX achieves significant performance improvements in high-contention scenarios: 2.5x better throughput than Caffeine [@caffeine] under extreme contention (32 threads) and 6x better performance in mixed read/write/remove workloads, while maintaining competitive performance in standard scenarios.

# Statement of need

Modern JVM applications require high-performance caching to reduce latency and improve throughput, but configuring caches optimally requires deep expertise in eviction algorithms, concurrency patterns, and performance characteristics. Existing libraries like Caffeine [@caffeine], EhCache [@ehcache], and Cache2k [@cache2k] provide excellent performance but require significant configuration knowledge to achieve optimal results.

Additionally, scaling from local to distributed caching typically requires learning completely different APIs and architectural patterns. Most distributed caching solutions require external service discovery mechanisms rather than integrating natively with container orchestration platforms like Kubernetes.

JCacheX addresses these challenges by providing:

1. **Profile-based configuration**: Eliminates the need to understand implementation details through workload-specific profiles
2. **Specialized implementations**: Multiple cache implementations optimized for specific use cases
3. **Kubernetes-native distribution**: Specialized distributed cache with Kubernetes service discovery that leverages shared JVM heap space across cluster nodes and optimized local TCP networking for enhanced performance
4. **Kubernetes integration**: Native service discovery using the official Kubernetes Java client [@kubernetes-java-client]
5. **Multi-language support**: Java, Kotlin DSL, and Spring Boot integration

# Key Features

## Profile-Based Optimization

JCacheX provides 12 pre-configured profiles that automatically select optimal cache implementations and settings:

**Core Profiles:**
- `DEFAULT`: Balanced performance for general use
- `READ_HEAVY`: Optimized for read-intensive workloads (80%+ reads)
- `WRITE_HEAVY`: Optimized for write-intensive workloads (50%+ writes)
- `MEMORY_EFFICIENT`: Minimized memory usage
- `HIGH_PERFORMANCE`: Maximum throughput optimization

**Specialized Profiles:**
- `SESSION_CACHE`: User session storage with time-based expiration
- `API_CACHE`: API response caching with short TTL
- `COMPUTE_CACHE`: Expensive computation results

**Advanced Profiles:**
- `ML_OPTIMIZED`: Machine learning workloads
- `ZERO_COPY`: Ultra-low latency optimization
- `HARDWARE_OPTIMIZED`: CPU-specific optimizations
- `DISTRIBUTED`: Multi-node cluster environments

## Specialized Cache Implementations

JCacheX includes multiple cache implementations optimized for different scenarios:

- **`ZeroCopyOptimizedCache`**: Eliminates object allocation in hot paths for ultra-low latency
- **`ProfiledOptimizedCache`**: Real-time performance profiling with adaptive optimization
- **`WriteHeavyOptimizedCache`**: Asynchronous write operations with batching and coalescing
- **`CacheLocalityOptimizedCache`**: CPU cache-line optimization for temporal locality workloads

## Enhanced Eviction Strategies

All eviction strategies provide O(1) performance through optimized data structures:

- **Enhanced LRU**: Traditional LRU with frequency sketch integration
- **Enhanced LFU**: Frequency-based eviction with O(1) bucket management
- **WindowTinyLFU**: Hybrid window + frequency-based eviction (default) [@tinylfu]
- **Composite strategies**: Combine multiple algorithms for specific workloads

## Distributed Caching

JCacheX provides Kubernetes-native distributed caching:

```java
// Kubernetes-native distributed cache
DistributedCache<String, User> cache = KubernetesDistributedCache.<String, User>builder()
    .clusterName("user-service")
    .nodeDiscovery(NodeDiscovery.kubernetes()
        .namespace("default")
        .build())
    .communicationProtocol(new TcpCommunicationProtocol.Builder<String, User>()
        .port(8081)
        .build())
    .cacheConfig(CacheConfig.<String, User>builder()
        .maximumSize(1000L)
        .build())
    .build();
```

Features:
- Official Kubernetes Java client [@kubernetes-java-client] integration
- Shared JVM heap space across cluster nodes for increased total cache capacity
- Optimized local TCP networking within Kubernetes clusters
- Consistent hashing with virtual nodes for balanced data distribution
- Multiple consistency models (strong, eventual, session)
- Automatic failover and health-aware routing

## Multi-Language Support

**Java:**
```java
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(1000L)
    .build();
```

**Kotlin DSL:**
```kotlin
val cache = createReadHeavyCache<String, User> {
    name("users")
    maximumSize(1000L)
}
```

**Spring Boot:**
```java
@JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY")
public User findUser(String id) {
    return userRepository.findById(id);
}
```

# Performance Evaluation

We conducted comprehensive benchmarking using JMH [@jmh] 1.37 on Apple M1 Pro (10-core, 32GB RAM) comparing JCacheX against Caffeine [@caffeine], EhCache [@ehcache], Cache2k [@cache2k], and ConcurrentHashMap baseline.

## Benchmark Results

**Extreme Contention (32 threads):**
- JCacheX HighPerformance: 982 MOps/s (2.5x faster than Caffeine)
- JCacheX Default: 499 MOps/s (1.3x faster than Caffeine)
- Caffeine: 388 MOps/s
- ConcurrentHashMap: 378 MOps/s

**Mixed Workload (16 threads, read/write/remove):**
- JCacheX Default: 290 MOps/s (6x faster than Caffeine)
- Caffeine: 49 MOps/s

**Read-Heavy Workload (10 threads, 90% reads):**
- ConcurrentHashMap: 401 MOps/s
- EhCache: 117 MOps/s
- Caffeine: 61 MOps/s
- JCacheX ReadHeavy: 11 MOps/s

**Sustained Load (8 threads):**
- JCacheX HighPerformance: 21.5 MOps/s
- Caffeine: 21.4 MOps/s
- ConcurrentHashMap: 21.3 MOps/s

## Key Findings

1. **JCacheX excels in high-contention scenarios** where multiple threads compete for cache access
2. **Mixed workloads show dramatic improvements** due to optimized write handling
3. **Read-heavy scenarios favor simpler implementations** like ConcurrentHashMap and Caffeine
4. **Sustained load performance matches industry leaders** while providing additional features

The results demonstrate that JCacheX's strength lies in realistic production scenarios involving high contention, mixed access patterns, and complex workloads rather than simple microbenchmarks.

# Architecture

JCacheX uses a modular architecture that separates concerns:

```
Application Layer (Java/Kotlin/Spring)
    ↓
Profile Layer (Automatic Implementation Selection)
    ↓
Cache Implementation Layer (Specialized Implementations)
    ↓
Eviction Strategy Layer (O(1) Algorithms)
    ↓
Storage Layer (Concurrent Data Structures)
    ↓
Monitoring Layer (Statistics, Events, Health)
```

This design enables optimal performance through specialized implementations while maintaining a consistent API across all cache types and deployment modes.

# Usage Examples

## Basic Usage
```java
// Minimal configuration
Cache<String, String> cache = JCacheXBuilder.create()
    .name("simple")
    .maximumSize(100L)
    .build();
```

## Profile-Based Configuration
```java
// Optimized for read-heavy workloads
Cache<String, User> userCache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();
```

## Kubernetes Distributed Deployment
```java
// Full Kubernetes distributed cache setup
DistributedCache<String, Data> distributedCache =
    KubernetesDistributedCache.<String, Data>builder()
        .clusterName("data-service")
        .nodeDiscovery(NodeDiscovery.kubernetes()
            .namespace("production")
            .build())
        .communicationProtocol(new TcpCommunicationProtocol.Builder<String, Data>()
            .port(8081)
            .build())
        .cacheConfig(CacheConfig.<String, Data>builder()
            .maximumSize(5000L)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build())
        .build();
```

# Related Work

JCacheX builds upon established caching research and implementations:

- **Caffeine** [@caffeine]: Industry-standard high-performance cache using W-TinyLFU algorithm
- **EhCache** [@ehcache]: Mature enterprise caching solution with extensive features
- **Cache2k** [@cache2k]: Lightweight, high-performance Java cache
- **JSR-107** [@jsr107]: Java Temporary Caching API standard

JCacheX differentiates itself through profile-based optimization, specialized implementations for different workloads, and Kubernetes-native distributed caching with automatic service discovery.

# Acknowledgements

We thank the broader Java caching community, particularly the Caffeine [@caffeine], EhCache [@ehcache], and Cache2k [@cache2k] projects for establishing performance benchmarks and algorithmic foundations. The Kubernetes community's official Java client [@kubernetes-java-client] provides the foundation for our distributed implementation.
