# JCacheX Kotlin Example

This example demonstrates **JCacheX** with **Kotlin extensions**, showcasing idiomatic Kotlin patterns and coroutine integration.

## 🎯 What This Example Shows

- **🎨 Kotlin DSL**: Type-safe cache configuration with builder DSL
- **🔧 Operator Overloading**: Array-like syntax for cache operations (`cache["key"] = value`)
- **⚡ Coroutines**: Suspending functions and async operations
- **📦 Collection Extensions**: Familiar collection operations (filter, map, forEach)
- **🧪 Advanced Operations**: Bulk operations, batch processing, and utilities
- **📊 Enhanced Statistics**: Formatted stats and measurement utilities
- **🔒 Safe Operations**: Result-based error handling and null safety

## 🚀 Running the Example

```bash
# From the project root directory
./gradlew :example:kotlin:run

# Or from the example/kotlin directory (if gradlew were available there)
# kotlin -cp "build/libs/*" io.github.dhruv1110.jcachex.example.kotlin.MainKt
```

## 📋 Key Features Demonstrated

### 1. **Kotlin DSL Configuration**
```kotlin
val cache = createCache<String, String> {
    maximumSize(100)
    expireAfterWrite(Duration.ofMinutes(5))
    evictionStrategy(LRUEvictionStrategy())
    recordStats(true)
}
```

### 2. **Operator Overloading**
```kotlin
cache["key1"] = "value1"              // Put operation
val value = cache["key1"]             // Get operation
cache += "key2" to "value2"           // Add operation
val exists = "key1" in cache          // Contains operation
cache -= "key1"                       // Remove operation
```

### 3. **Coroutine Integration**
```kotlin
// Suspending getOrPut
val value = cache.getOrPut("key") {
    delay(100) // Simulate async work
    "computed_value"
}

// Deferred operations
val deferred = cache.getDeferred("key", coroutineScope)
val result = deferred.await()
```

### 4. **Collection-Like Operations**
```kotlin
// Filtering
val users = cache.filterKeys { it.startsWith("user") }
val longValues = cache.filterValues { it.length > 3 }

// Mapping
val upperCase = cache.mapValues { it.uppercase() }

// Utilities
val userCount = cache.count { key, _ -> key.startsWith("user") }
val hasUsers = cache.any { key, _ -> key.startsWith("user") }
```

### 5. **Bulk Operations**
```kotlin
// Batch operations
cache.batch {
    put("batch1", "value1")
    put("batch2", "value2")
    put("batch3", "value3")
}

// Multiple gets
val values = cache.getAll(listOf("key1", "key2", "key3"))
val presentOnly = cache.getAllPresent(listOf("key1", "key2", "missing"))
```

### 6. **Advanced Features**
```kotlin
// Measure execution time
val (result, timeNanos) = cache.measureTime {
    // Expensive operations
}

// Sequence operations (lazy evaluation)
val filtered = cache.asSequence()
    .filter { it.key.length > 4 }
    .map { "${it.key}=${it.value}" }
    .take(3)
    .toList()

// Cache summary
println(cache.summary())
```

## 📖 Code Structure

```
src/main/kotlin/
└── io/github/dhruv1110/jcachex/example/kotlin/
    └── Main.kt                      # Complete demo application
```

## 🎓 Learning Path

1. **Start with Main.kt**: See all Kotlin extensions in action
2. **Study DSL Configuration**: Learn type-safe cache setup
3. **Explore Operators**: Understand syntactic sugar for cache operations
4. **Review Coroutines**: See async patterns and suspending functions
5. **Check Collections**: Learn familiar collection-like operations
6. **Examine Utilities**: Discover measurement and batch operations

## 🔧 Configuration Examples

### DSL Configuration
```kotlin
val cache = createCache<String, User> {
    maximumSize(1000)
    expireAfterWrite(Duration.ofMinutes(30))
    evictionStrategy(LRUEvictionStrategy())
    recordStats(true)
}
```

### Coroutine-Based Loading
```kotlin
suspend fun loadUser(id: String): User {
    return cache.getOrPut(id) {
        // Suspending function - can use delay, API calls, etc.
        userService.loadUser(id)
    }
}
```

### Bulk Processing
```kotlin
fun processUsers() {
    cache.batch {
        users.forEach { user ->
            put(user.id, user)
        }
    }
}
```

## 📈 Expected Output

When you run this example, you should see:
```
=== JCacheX Kotlin Extensions Demo ===

1. Creating cache with DSL:

2. Using operator overloading:
cache['key1'] = value1
'key2' in cache = true

3. Coroutine support:
Deferred value: value1
Async put completed
Computed value: computed_value_1234567890

5. Collection-like operations:
Cache size: 8
Is empty: false
Is not empty: true
Users: {user1=John, user2=Jane, user3=Bob}

...

11. Batch operations:
Batch operations completed in 0.05 ms

13. Cache summary:
Cache Summary:
- Size: 12
- Empty: false
- Keys: [key1, key2, user1, user2, ...]
- Stats: Hit Rate: 61.54%, Hits: 8, Misses: 5

After cleanup, cache size: 9
```

## 📈 Expected Performance

This example demonstrates:
- **Cache Hit Rate**: ~60% during the demo operations
- **Response Time**: < 1ms for cached operations
- **Coroutine Overhead**: Minimal - ~0.01ms per operation
- **Memory Usage**: ~32 bytes overhead per cache entry
- **Batch Operations**: 5-10x faster than individual operations

## 🔗 Related Documentation

- [Main Documentation](../../README.md): Complete JCacheX guide
- [Java Example](../java/): Core Java patterns
- [Spring Boot Example](../springboot/): Spring integration
- [Kotlin Extensions API](https://javadoc.io/doc/io.github.dhruv1110/jcachex-kotlin): Complete Kotlin API reference

---

💡 **Pro Tip**: This example showcases the power of Kotlin extensions. The DSL configuration, operator overloading, and coroutine integration make caching operations feel natural and idiomatic in Kotlin applications.
