# Welcome to JCacheX

**JCacheX is a high-performance Java caching library that makes your applications faster, more responsive, and easier to scale.**

## 🎯 What Problem Does JCacheX Solve?

If you've ever experienced:
- **Slow page loads** when users wait for database queries
- **High database costs** from repeated expensive operations
- **Poor user experience** due to sluggish response times
- **Complex caching code** that's hard to maintain

Then JCacheX is for you. It's designed to be **simple to use** while delivering **enterprise-grade performance**.

## 🚀 What Makes JCacheX Special?

### **1. Smart Defaults**
Instead of configuring complex caching strategies, JCacheX provides **pre-built profiles** for common use cases:

```java
// For product catalogs, user data, anything read-heavy
Cache<String, Product> products = JCacheXBuilder.forReadHeavyWorkload()
    .name("products")
    .maximumSize(10000L)
    .build();

// For logging, analytics, anything write-heavy
Cache<String, LogEntry> logs = JCacheXBuilder.forWriteHeavyWorkload()
    .name("logs")
    .maximumSize(5000L)
    .build();
```

### **2. Performance That Matters**
- **Sub-microsecond response times** for cache hits
- **10-100x faster** than database queries
- **Built-in monitoring** so you know it's working
- **Automatic memory management** to prevent crashes

### **3. Developer Experience**
- **Fluent API** that reads like English
- **Kotlin DSL** for idiomatic Kotlin code
- **Spring Boot integration** with annotations
- **Comprehensive examples** for real-world scenarios

## 🎯 Real-World Examples

### **E-commerce Application**
```java
// Before JCacheX: Every product page hits the database
public Product getProduct(String id) {
    return productRepository.findById(id); // 50-200ms
}

// After JCacheX: Instant product display
public Product getProduct(String id) {
    Product cached = productCache.get(id);
    if (cached != null) {
        return cached; // 0.05-0.2ms (1000x faster!)
    }

    Product product = productRepository.findById(id);
    productCache.put(id, product);
    return product;
}
```

### **User Session Management**
```java
// Before: Database lookup on every request
public UserSession getSession(String sessionId) {
    return sessionRepository.findById(sessionId); // 10-50ms
}

// After: Instant session validation
public UserSession getSession(String sessionId) {
    return sessionCache.get(sessionId); // 0.05-0.2ms
}
```

## 🚀 Quick Start (2 Minutes)

### **Step 1: Add to Your Project**

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
implementation 'io.github.dhruv1110:jcachex-core:2.0.1'
```

### **Step 2: Create Your First Cache**

```java
import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.JCacheXBuilder;

public class UserService {
    private final Cache<String, User> userCache;

    public UserService() {
        // Create a cache optimized for reading user data
        this.userCache = JCacheXBuilder.forReadHeavyWorkload()
            .name("users")
            .maximumSize(1000L)
            .build();
    }

    public User getUser(String id) {
        // Try cache first (fast)
        User cached = userCache.get(id);
        if (cached != null) {
            return cached;
        }

        // Load from database if not cached (slow, but only once)
        User user = userRepository.findById(id);
        if (user != null) {
            userCache.put(id, user);
        }

        return user;
    }
}
```

### **Step 3: See the Results**
- **First request**: Database hit (50-200ms)
- **Subsequent requests**: Cache hit (0.05-0.2ms)
- **Performance improvement**: **1000x faster** for cached data

## 🎯 When Should You Use JCacheX?

### **Perfect For:**
- **Web applications** that need fast response times
- **APIs** that want to reduce external service calls
- **Microservices** that need to share data efficiently
- **Data processing** applications with repeated calculations
- **Any application** where speed matters

### **Not For:**
- **Real-time data** that changes every second
- **Very small datasets** (<100 items)
- **Applications** where memory is extremely limited

## 🔧 How It Works (Simple Version)

1. **Your code** asks for data
2. **JCacheX checks** if it's in memory
3. **If found**: Returns instantly (0.05-0.2ms)
4. **If not found**: Loads from your data source, then caches it
5. **Next time**: Instant response from cache

That's it! No complex configuration, no performance tuning, no memory management headaches.

## 🚀 What You'll Learn Next

This documentation is organized to take you from **zero to hero**:

1. **[Getting Started](getting-started)** - Set up JCacheX in your project
2. **[Core Concepts](core-concepts/cache-profiles)** - Understand the different cache profiles
3. **[Examples](examples)** - See real-world patterns and use cases
4. **[Spring Boot Integration](spring-boot)** - Use JCacheX with Spring applications
5. **[API Reference](api-reference)** - Complete method reference
6. **[Performance](performance)** - Optimize for your specific use case

## 🎯 Ready to Get Started?

If you're building an application where **speed matters**, JCacheX can help you:

- **Improve user experience** with faster response times
- **Reduce infrastructure costs** by decreasing database load
- **Scale your application** without performance degradation
- **Focus on business logic** instead of caching complexity

**Start with [Getting Started](getting-started) to add JCacheX to your project in under 5 minutes.**

---

*JCacheX: Simple caching that actually works.*
