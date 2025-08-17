---
id: getting-started
title: Getting Started
sidebar_label: Getting Started
description: Get up and running with JCacheX in minutes
---

# Getting Started with JCacheX

Welcome to JCacheX! This guide will help you get up and running with high-performance caching in your Java application.

## Prerequisites

- **Java 11+** (JCacheX requires Java 11 or higher)
- **Maven 3.6+** or **Gradle 7.0+**
- **Basic understanding of Java** and dependency management

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Gradle

Add the following dependency to your `build.gradle` or `build.gradle.kts`:

```gradle
implementation 'io.github.dhruv1110:jcachex-core:2.0.1'
```

```kotlin
implementation("io.github.dhruv1110:jcachex-core:2.0.1")
```

### Optional Modules

For additional functionality, you can also include:

```xml
<!-- Spring Boot Integration -->
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-spring</artifactId>
    <version>2.0.1</version>
</dependency>

<!-- Kotlin DSL Support -->
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-kotlin</artifactId>
    <version>2.0.1</version>
</dependency>
```

## Your First Cache

### 1. Basic Cache Creation

Create a simple cache with default settings:

```java
import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.JCacheXBuilder;

public class HelloJCacheX {
    public static void main(String[] args) {
        // Create a basic cache
        Cache<String, String> cache = JCacheXBuilder.create()
            .name("hello-cache")
            .maximumSize(100L)
            .build();

        // Store a value
        cache.put("hello", "world");

        // Retrieve the value
        String value = cache.get("hello");
        System.out.println(value); // Output: world

        // Check cache statistics
        System.out.println("Cache size: " + cache.size());
        System.out.println("Hit rate: " + cache.stats().hitRate() * 100 + "%");
    }
}
```

### 2. Profile-Based Cache

Use pre-configured profiles for common workloads:

```java
// Read-heavy workload (e.g., product catalog)
Cache<String, Product> productCache = JCacheXBuilder.forReadHeavyWorkload()
    .name("products")
    .maximumSize(1000L)
    .build();

// Write-heavy workload (e.g., logging system)
Cache<String, LogEntry> logCache = JCacheXBuilder.forWriteHeavyWorkload()
    .name("logs")
    .maximumSize(500L)
    .build();

// API response caching
Cache<String, ApiResponse> apiCache = JCacheXBuilder.forApiResponseCaching()
    .name("api-responses")
    .maximumSize(200L)
    .build();
```

### 3. Advanced Configuration

Customize your cache with advanced options:

```java
Cache<String, User> userCache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(5000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .expireAfterAccess(Duration.ofMinutes(10))
    .recordStats(true)
    .build();
```

## Quick Examples

### Basic Operations

```java
Cache<String, Integer> cache = JCacheXBuilder.create()
    .name("numbers")
    .maximumSize(100L)
    .build();

// Put values
cache.put("one", 1);
cache.put("two", 2);
cache.put("three", 3);

// Get values
Integer one = cache.get("one");        // Returns 1
Integer missing = cache.get("four");   // Returns null

// Check existence
boolean hasOne = cache.containsKey("one");     // true
boolean hasFour = cache.containsKey("four");   // false

// Remove values
cache.remove("two");

// Clear all
cache.clear();
```

### Bulk Operations

```java
// Put multiple values at once
Map<String, String> data = Map.of(
    "key1", "value1",
    "key2", "value2",
    "key3", "value3"
);
cache.putAll(data);

// Get multiple values
List<String> keys = Arrays.asList("key1", "key2", "key3");
Map<String, String> results = cache.getAll(keys);

// Remove multiple keys
cache.removeAll(Arrays.asList("key1", "key2"));
```

### Async Operations

```java
// Async put
CompletableFuture<Void> putFuture = cache.putAsync("async-key", "async-value");
putFuture.thenRun(() -> System.out.println("Value stored asynchronously"));

// Async get
CompletableFuture<String> getFuture = cache.getAsync("async-key");
getFuture.thenAccept(value -> {
    if (value != null) {
        System.out.println("Retrieved: " + value);
    }
});

// Wait for completion
putFuture.join();
String value = getFuture.join();
```

## Cache Profiles

JCacheX provides several pre-configured profiles optimized for different use cases:

### Available Profiles

| Profile | Description | Use Case |
|---------|-------------|----------|
| `READ_HEAVY` | Optimized for 80%+ read operations | Product catalogs, reference data |
| `WRITE_HEAVY` | Balanced for 50%+ write operations | Logging, analytics, real-time data |
| `API_CACHE` | Optimized for external API responses | HTTP client caching, service calls |
| `SESSION_CACHE` | User session optimization | Web applications, user state |
| `HIGH_PERFORMANCE` | Maximum throughput optimization | High-frequency trading, gaming |
| `MEMORY_EFFICIENT` | Memory-constrained environments | Embedded systems, mobile |
| `ML_OPTIMIZED` | Machine learning workloads | AI/ML applications, predictions |
| `ZERO_COPY` | Ultra-low latency | HFT, real-time systems |

### Profile Selection Guide

```java
// Choose based on your workload characteristics
if (readRatio > 0.8) {
    return JCacheXBuilder.forReadHeavyWorkload();
} else if (writeRatio > 0.5) {
    return JCacheXBuilder.forWriteHeavyWorkload();
} else if (latencyCritical) {
    return JCacheXBuilder.forUltraLowLatency();
} else {
    return JCacheXBuilder.forHighPerformance();
}
```

## Next Steps

Now that you have JCacheX running, explore these topics:

- **[Core Concepts](/docs/core-concepts)** - Learn about cache profiles, eviction strategies, and more
- **[Examples](/docs/examples)** - See real-world usage patterns
- **[Spring Boot Integration](/docs/spring-boot)** - Use JCacheX with Spring Boot
- **[Performance Guide](/docs/performance)** - Optimize your cache performance
- **[API Reference](/docs/api-reference)** - Complete API documentation

## Troubleshooting

### Common Issues

**Cache not working as expected?**
- Check that you're using the correct cache instance
- Verify cache configuration parameters
- Enable statistics to monitor cache behavior

**Performance issues?**
- Review your cache profile selection
- Check eviction strategy configuration
- Monitor cache statistics for insights

**Memory problems?**
- Adjust `maximumSize` parameter
- Review eviction strategy settings
- Consider using `MEMORY_EFFICIENT` profile

### Getting Help

- **Documentation**: This site contains comprehensive guides
- **Examples**: Check the [examples section](/docs/examples) for working code
- **GitHub**: [Report issues](https://github.com/dhruv1110/jcachex/issues) or ask questions
- **Community**: Join discussions on [GitHub Discussions](https://github.com/dhruv1110/jcachex/discussions)

---

**Ready to build something amazing?** Check out our [examples section](/docs/examples) for inspiration!
