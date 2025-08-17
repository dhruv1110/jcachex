---
id: introduction
title: Introduction
sidebar_label: Introduction
description: Welcome to JCacheX - High-performance Java caching library
slug: /
---

# Welcome to JCacheX

<div align="center">

**High‑performance Java caching** — profile‑based, from in‑memory to distributed (Kubernetes), with Kotlin DSL and Spring Boot integration.

[Getting Started](/docs/getting-started) • [Examples](/docs/examples) • [Performance](/docs/performance) • [GitHub](https://github.com/dhruv1110/jcachex)

</div>

---

## Why JCacheX?

JCacheX is designed to solve real-world caching challenges with a focus on **performance**, **simplicity**, and **production readiness**.

### 🚀 **Profile‑based Simplicity**
One‑liners for common workloads with intelligent defaults:
- **READ_HEAVY** - Optimized for 80%+ read operations
- **WRITE_HEAVY** - Balanced for 50%+ write operations
- **API_CACHE** - Perfect for external API responses
- **SESSION_CACHE** - User session optimization
- **ZERO_COPY** - Ultra-low latency for HFT/gaming

### 🏭 **Production‑ready Features**
Built for real-world applications:
- Async loaders with CompletableFuture support
- Rich statistics and performance monitoring
- Event listeners for cache lifecycle management
- Health checks and metrics integration
- Circuit breaker and retry policies

### 🔄 **Evolves with Your Application**
Same API from local in‑memory to distributed on Kubernetes:
- **Local**: Single JVM with optimized data structures
- **Distributed**: Multi-node with consistent hashing
- **Kubernetes**: Pod-aware discovery and auto-scaling

### 👨‍💻 **Developer‑friendly**
Multiple language and framework support:
- **Java**: Fluent builder API with method chaining
- **Kotlin**: Idiomatic DSL with extension functions
- **Spring Boot**: Annotations and auto-configuration

## Quick Start

### 1) Install

**Maven:**
```xml
<dependency>
  <groupId>io.github.dhruv1110</groupId>
  <artifactId>jcachex-core</artifactId>
  <version>2.0.1</version>
</dependency>
```

**Gradle:**
```gradle
implementation "io.github.dhruv1110:jcachex-core:2.0.1"
```

### 2) Create a Cache (Java)

```java
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(1000L)
    .build();

cache.put("user:123", new User("Alice"));
User u = cache.get("user:123");
System.out.println(cache.stats());
```

### 3) Kotlin DSL

```kotlin
val users = createReadHeavyCache<String, User> {
    name("users"); maximumSize(1000)
}
users["user:1"] = User("Alice")
println(users.stats().hitRate())
```

### 4) Spring Boot (Annotations)

```java
@JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY")
public User findUser(String id) {
    return repo.findById(id);
}
```

## Key Features

### 🎯 **Smart Cache Profiles**
Pre-configured optimizations for common workloads:

```java
// Read-heavy workloads (e.g., product catalogs)
Cache<String, Product> products = JCacheXBuilder.forReadHeavyWorkload()
    .name("products")
    .maximumSize(10000L)
    .build();

// Write-heavy workloads (e.g., logging systems)
Cache<String, LogEntry> logs = JCacheXBuilder.forWriteHeavyWorkload()
    .name("logs")
    .maximumSize(5000L)
    .build();

// API response caching
Cache<String, ApiResponse> apiCache = JCacheXBuilder.forApiResponseCaching()
    .name("api")
    .maximumSize(1000L)
    .build();
```

### 🔄 **Advanced Eviction Strategies**
Multiple algorithms for different access patterns:

- **LRU (Least Recently Used)**: General-purpose caching
- **LFU (Least Frequently Used)**: Frequency-based access
- **FIFO (First In, First Out)**: Time-based ordering
- **TTL (Time To Live)**: Expiration-based eviction
- **Composite**: Hybrid strategies for complex workloads

### 📊 **Comprehensive Monitoring**
Built-in statistics and health checks:

```java
CacheStats stats = cache.stats();
System.out.println("Hit Rate: " + stats.hitRate() * 100 + "%");
System.out.println("Miss Rate: " + stats.missRate() * 100 + "%");
System.out.println("Load Success Rate: " + stats.loadSuccessRate() * 100 + "%");
System.out.println("Average Load Time: " + stats.averageLoadPenalty() + "ms");
```

### 🌐 **Distributed Caching**
Seamless scaling from single node to cluster:

```java
// Distributed cache with automatic failover
Cache<String, User> distributedCache = JCacheXBuilder.forDistributedCaching()
    .name("users")
    .maximumSize(5000L)
    .kubernetesDiscovery()
    .build();
```

## Performance Highlights

JCacheX is designed for **high-throughput** and **low-latency** operations:

- **Lock-free operations** using concurrent data structures
- **Memory-efficient** storage with intelligent eviction
- **Zero-copy optimizations** for ultra-low latency
- **Profile-based tuning** for specific workload characteristics
- **Async operations** with CompletableFuture support

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        JCacheX Architecture                     │
├─────────────────────────────────────────────────────────────────┤
│  Application Layer                                              │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  Java / Kotlin / Spring Boot Applications                   │
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  API Layer                                                      │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  Cache Interface   │   CacheConfig   │   CacheBuilder       │
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  Core Engine                                                    │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  DefaultCache  │  Async Operations  │  Statistics           │
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  Storage & Eviction                                             │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  ConcurrentHashMap  │  LRU  │  LFU  │  FIFO  │  TTL         │
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  Monitoring & Events                                            │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  CacheStats  │  Event Listeners  │  Performance Monitoring  │
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

## Getting Started

Ready to dive in? Start with our comprehensive guides:

- **[Installation Guide](/docs/getting-started/installation)** - Set up JCacheX in your project
- **[Quick Start](/docs/getting-started/quick-start)** - Create your first cache in minutes
- **[Examples](/docs/examples)** - Real-world usage patterns and code samples
- **[Spring Boot Integration](/docs/spring-boot)** - Annotations and auto-configuration
- **[Performance Guide](/docs/performance)** - Benchmarks and optimization tips

## Community & Support

- **GitHub**: [Source code and issues](https://github.com/dhruv1110/jcachex)
- **Documentation**: [Complete API reference](/docs/api-reference)
- **Examples**: [Working code samples](/docs/examples)
- **Benchmarks**: [Performance comparisons](/docs/performance)

---

<div align="center">

**Ready to supercharge your Java applications with high-performance caching?**

[Get Started Now →](/docs/getting-started)

</div>
