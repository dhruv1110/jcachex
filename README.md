# JCacheX

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

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    // Core functionality
    implementation("io.github.dhruv:jcachex-core:1.0.0")

    // Java-specific extensions
    implementation("io.github.dhruv:jcachex-java:1.0.0")

    // Kotlin-specific extensions
    implementation("io.github.dhruv:jcachex-kotlin:1.0.0")

    // Spring Boot integration
    implementation("io.github.dhruv:jcachex-spring:1.0.0")
}
```

### Maven

```xml
<dependencies>
    <!-- Core functionality -->
    <dependency>
        <groupId>io.github.dhruv</groupId>
        <artifactId>jcachex-core</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Java-specific extensions -->
    <dependency>
        <groupId>io.github.dhruv</groupId>
        <artifactId>jcachex-java</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Kotlin-specific extensions -->
    <dependency>
        <groupId>io.github.dhruv</groupId>
        <artifactId>jcachex-kotlin</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Spring Boot integration -->
    <dependency>
        <groupId>io.github.dhruv</groupId>
        <artifactId>jcachex-spring</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## Quick Start

### Java

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

### Kotlin

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

### Spring Boot

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

## Advanced Usage

### Custom Eviction Strategy

```kotlin
class CustomEvictionStrategy<K, V> : EvictionStrategy<K, V> {
    override fun evict(entries: MutableMap<K, CacheEntry<V>>, count: Int) {
        // Custom eviction logic
    }
}
```

### Cache Event Listeners

```kotlin
cache.addListener(object : CacheEventListener<String, String> {
    override fun onPut(key: String, value: String) {
        println("Put: $key = $value")
    }

    override fun onRemove(key: String, value: String) {
        println("Remove: $key = $value")
    }

    override fun onEvict(key: String, value: String, reason: EvictionReason) {
        println("Evict: $key = $value ($reason)")
    }
})
```

### Testing with Fake Cache

```kotlin
@Test
fun testWithFakeCache() {
    val cache = FakeCache<String, String>()

    cache.put("key", "value")
    assertEquals("value", cache.get("key"))

    cache.simulateEviction("key", EvictionReason.SIZE)
    assertNull(cache.get("key"))
}
```

## Documentation

- [API Reference](https://javadoc.io/doc/io.github.dhruv/jcachex-core)
- [Wiki](https://github.com/dhruv/JCacheX/wiki)
- [GitHub Pages](https://dhruv.github.io/JCacheX)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Sample Projects

See the `example/` directory for sample projects:
- [Java Example](example/java/)
- [Kotlin Example](example/kotlin/)
- [Spring Boot Example](example/springboot/)

## Built with ❤️ for the JVM community
