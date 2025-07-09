# JCacheX

[![CI](https://github.com/dhruv1110/JCacheX/workflows/CI/badge.svg)](https://github.com/dhruv1110/JCacheX/actions)
[![codecov](https://codecov.io/gh/dhruv1110/JCacheX/branch/main/graph/badge.svg)](https://codecov.io/gh/dhruv1110/JCacheX)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dhruv1110_jcachex&metric=alert_status)](https://sonarcloud.io/project/overview?id=dhruv1110_jcachex)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.dhruv1110/jcachex-core)](https://maven-badges.herokuapp.com/maven-central/io.github.dhruv1110/jcachex-core)
[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://dhruv1110.github.io/jcachex/)
[![javadoc](https://javadoc.io/badge2/io.github.dhruv1110/jcachex-core/javadoc.svg)](https://javadoc.io/doc/io.github.dhruv1110/jcachex-core)

**JCacheX** is a high-performance, lightweight caching library for modern Java and Kotlin applications. Built for simplicity and performance, it provides a clean API for both synchronous and asynchronous caching operations.

## 🚀 Key Features

- **High Performance**: Optimized for speed with minimal overhead
- **Simple API**: Intuitive, fluent interface that's easy to learn
- **Async Support**: Built-in CompletableFuture and Kotlin Coroutines support
- **Flexible Configuration**: Multiple eviction strategies and expiration policies
- **Spring Integration**: Seamless Spring Boot integration with annotations
- **Distributed Caching**: Multi-node caching with consistency guarantees
- **Comprehensive Monitoring**: Built-in metrics and observability

## ⚡ Quick Start

### Installation

**Maven:**
```xml
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'io.github.dhruv1110:jcachex-core:1.0.0'
```

### Basic Usage

**Java:**
```java
import io.github.dhruv1110.jcachex.*;

// Create cache
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();

Cache<String, User> cache = new DefaultCache<>(config);

// Use cache
cache.put("user123", new User("Alice"));
User user = cache.get("user123");
```

**Kotlin:**
```kotlin
import io.github.dhruv1110.jcachex.kotlin.*

// Create cache with DSL
val cache = createCache<String, User> {
    maximumSize(1000)
    expireAfterWrite(Duration.ofMinutes(30))
}

// Use cache
cache["user123"] = User("Alice")
val user = cache["user123"]
```

## 📦 Modules

- **`jcachex-core`** - Core caching functionality
- **`jcachex-kotlin`** - Kotlin extensions and DSL
- **`jcachex-spring`** - Spring Boot integration

## 🔧 Eviction Strategies

- **LRU** (Least Recently Used) - Default strategy
- **LFU** (Least Frequently Used) - Frequency-based eviction
- **FIFO** (First In, First Out) - Simple queue-based eviction
- **TTL** (Time To Live) - Time-based expiration
- **Weight-based** - Custom weight calculation

## 🌐 Spring Boot Integration

```java
@Service
public class UserService {
    @JCacheXCacheable(cacheName = "users")
    public User findUser(String id) {
        return userRepository.findById(id);
    }
}
```

## 📊 Monitoring & Metrics

```java
CacheStats stats = cache.stats();
System.out.println("Hit rate: " + stats.hitRate());
System.out.println("Total requests: " + stats.requestCount());
```

## 📖 Documentation

- [**Website**](https://dhruv1110.github.io/jcachex/) - Complete documentation with examples
- [**API Reference**](https://javadoc.io/doc/io.github.dhruv1110/jcachex-core) - Javadoc API documentation
- [**Examples**](example/) - Code samples and usage examples

## 🤝 Contributing

Contributions are welcome! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## 📄 License

JCacheX is licensed under the [MIT License](LICENSE).

---

**[Visit our website](https://dhruv1110.github.io/jcachex/) for detailed documentation, examples, and guides.**
