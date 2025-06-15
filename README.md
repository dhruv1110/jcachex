# JCacheX

[![CI](https://github.com/dpflux/JCacheX/workflows/CI/badge.svg)](https://github.com/dpflux/JCacheX/actions)
[![codecov](https://codecov.io/gh/dpflux/JCacheX/branch/main/graph/badge.svg)](https://codecov.io/gh/dpflux/JCacheX)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dpflux_JCacheX&metric=alert_status)](https://sonarcloud.io/dashboard?id=dpflux_JCacheX)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.dpflux/jcachex-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.dpflux/jcachex-core)
[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://dpflux.github.io/JCacheX/)
[![javadoc](https://javadoc.io/badge2/io.github.dpflux/jcachex-core/javadoc.svg)](https://javadoc.io/doc/io.github.dpflux/jcachex-core)

A high-performance, lightweight caching library for Kotlin and Java applications.

## Features

- Pure Kotlin/Java implementation with no external caching dependencies
- Support for both synchronous and asynchronous operations
  - Java: `CompletableFuture` and `Executor`
  - Kotlin: Coroutines
- Multiple eviction strategies (LRU, LFU, FIFO)
- Configurable cache size and weight-based eviction
- Time-based expiration (write, access, custom)
- Spring Boot integration with annotation-based caching
- Android compatibility
- Configurable logging
- Comprehensive test coverage
- Fake cache support for testing

## 📚 Documentation

- **📖 API Documentation**: [GitHub Pages](https://dpflux.github.io/JCacheX/)
- **📋 Javadoc**: [javadoc.io](https://javadoc.io/doc/io.github.dpflux/)
- **📊 Documentation Coverage**: Run `./gradlew allDocumentationCoverage`

## 📦 Installation

### Gradle
```kotlin
dependencies {
    // Core functionality
    implementation("io.github.dpflux:jcachex-core:x.y.z")

    // Java-specific extensions
    implementation("io.github.dpflux:jcachex-java:x.y.z")

    // Kotlin-specific extensions
    implementation("io.github.dpflux:jcachex-kotlin:x.y.z")

    // Spring Boot integration
    implementation("io.github.dpflux:jcachex-spring:x.y.z")
}
```

### Maven
```xml
<dependencies>
    <!-- Core functionality -->
    <dependency>
        <groupId>io.github.dpflux</groupId>
        <artifactId>jcachex-core</artifactId>
        <version>x.y.z</version>
    </dependency>

    <!-- Java-specific extensions -->
    <dependency>
        <groupId>io.github.dpflux</groupId>
        <artifactId>jcachex-java</artifactId>
        <version>x.y.z</version>
    </dependency>

    <!-- Kotlin-specific extensions -->
    <dependency>
        <groupId>io.github.dpflux</groupId>
        <artifactId>jcachex-kotlin</artifactId>
        <version>x.y.z</version>
    </dependency>

    <!-- Spring Boot integration -->
    <dependency>
        <groupId>io.github.dpflux</groupId>
        <artifactId>jcachex-spring</artifactId>
        <version>x.y.z</version>
    </dependency>
</dependencies>
```

## 🚀 Quick Start

### Basic Usage (Java)
```java
// Create a cache
Cache<String, String> cache = CacheBuilder.<String, String>newBuilder()
    .maximumSize(100)
    .expireAfterWrite(Duration.ofMinutes(5))
    .evictionStrategy(new LRUEvictionStrategy<>())
    .build();

// Synchronous operations
cache.put("key", "value");
String value = cache.get("key");

// Asynchronous operations
CompletableFuture<String> future = cache.getAsync("key", executor);
```

### Kotlin Extensions
```kotlin
// Create a cache
val cache = CacheBuilder<String, String>()
    .maximumSize(100)
    .expireAfterWrite(Duration.ofMinutes(5))
    .evictionStrategy(LRUEvictionStrategy())
    .build()

// Synchronous operations
cache.put("key", "value")
val value = cache.get("key")

// Asynchronous operations with coroutines
val asyncValue = cache.getSuspend("key")
```

### Spring Boot Integration
```kotlin
@SpringBootApplication
@EnableJCacheX
class Application

@RestController
class UserController {
    @GetMapping("/users/{id}")
    @Cacheable("users")
    fun getUser(@PathVariable id: String): User {
        // Simulate database call
        Thread.sleep(1000)
        return User(id, "User $id")
    }
}
```

## 🔧 Development

### Documentation Tasks
```bash
# Check documentation coverage
./gradlew allDocumentationCoverage

# Generate all documentation
./gradlew generateAllDocs

# Generate specific documentation
./gradlew javadoc
./gradlew dokkaHtml
```

### Testing
```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Check code quality
./gradlew detekt ktlintCheck
```

## 📈 Quality Metrics

- **Test Coverage**: 60%+ (tracked by Codecov)
- **Documentation Coverage**: 100% (tracked automatically)
- **Code Quality**: SonarQube quality gate
- **Code Style**: Detekt + KtLint

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## 📊 CI/CD

For detailed CI/CD setup instructions, see [CICD_SETUP.md](CICD_SETUP.md).

The project uses:
- **GitHub Actions** for CI/CD
- **SonarCloud** for code quality
- **Codecov** for test coverage
- **Maven Central** for artifact publishing
- **GitHub Pages** for documentation hosting

## Sample Projects

See the `example/` directory for sample projects:
- [Java Example](example/java/)
- [Kotlin Example](example/kotlin/)
- [Spring Boot Example](example/springboot/)

## Built with ❤️ for the JVM community
