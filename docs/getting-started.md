# Getting Started with JCacheX

**Get JCacheX working in your project in under 5 minutes.** This guide takes you from zero to a working cache with real examples.

## 🎯 What You'll Build

By the end of this guide, you'll have:
- ✅ JCacheX added to your project
- ✅ A working cache that stores user data
- ✅ A service that uses caching to improve performance
- ✅ Basic monitoring to see the performance improvement

## 🚀 Step 1: Add JCacheX to Your Project

### **Maven Project**

Add this to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>2.0.1</version>
</dependency>
```

### **Gradle Project**

Add this to your `build.gradle`:

```gradle
implementation 'io.github.dhruv1110:jcachex-core:2.0.1'
```

### **Verify Installation**

After adding the dependency, refresh your project. You should see JCacheX in your dependencies without any errors.

## 🚀 Step 2: Create Your First Cache

### **Basic Cache Creation**

Create a new Java class called `UserService.java`:

```java
import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.JCacheXBuilder;

public class UserService {
    private final Cache<String, User> userCache;

    public UserService() {
        // Create a cache for user data
        this.userCache = JCacheXBuilder.newBuilder()
            .name("users")
            .maximumSize(1000L)
            .build();
    }

    public User getUser(String id) {
        // Try cache first (fast)
        User cached = userCache.get(id);
        if (cached != null) {
            System.out.println("Cache HIT for user: " + id);
            return cached;
        }

        // Cache miss - load from database (slow)
        System.out.println("Cache MISS for user: " + id);
        User user = userRepository.findById(id);

        if (user != null) {
            // Store in cache for next time
            userCache.put(id, user);
            System.out.println("Stored user in cache: " + id);
        }

        return user;
    }
}
```

### **What This Code Does**

1. **Creates a cache** that can store up to 1,000 users
2. **Tries the cache first** - if found, returns instantly
3. **Falls back to database** if not in cache
4. **Stores the result** in cache for next time

## 🚀 Step 3: Test Your Cache

### **Create a Simple Test**

Add this method to your `UserService`:

```java
public void testCache() {
    System.out.println("=== Testing Cache Performance ===");

    // First request - will hit database
    long start = System.currentTimeMillis();
    User user1 = getUser("user123");
    long dbTime = System.currentTimeMillis() - start;

    // Second request - will hit cache
    start = System.currentTimeMillis();
    User user2 = getUser("user123");
    long cacheTime = System.currentTimeMillis() - start;

    System.out.println("Database time: " + dbTime + "ms");
    System.out.println("Cache time: " + cacheTime + "ms");
    System.out.println("Speed improvement: " + (dbTime / cacheTime) + "x faster!");

    // Show cache statistics
    System.out.println("Cache stats: " + userCache.stats());
}
```

### **Run the Test**

```java
public static void main(String[] args) {
    UserService userService = new UserService();
    userService.testCache();
}
```

**Expected Output:**
```
=== Testing Cache Performance ===
Cache MISS for user: user123
Stored user in cache: user123
Cache HIT for user: user123
Database time: 45ms
Cache time: 0ms
Speed improvement: 45x faster!
Cache stats: CacheStats{hitCount=1, missCount=1, ...}
```

## 🚀 Step 4: Use Cache Profiles (Recommended)

Instead of the basic builder, use **pre-built profiles** for better performance:

### **Read-Heavy Workload (User Data, Products)**

```java
// For data that's read often but rarely changes
Cache<String, User> userCache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(10000L)
    .build();
```

### **Write-Heavy Workload (Logging, Analytics)**

```java
// For data that's written often
Cache<String, LogEntry> logCache = JCacheXBuilder.forWriteHeavyWorkload()
    .name("logs")
    .maximumSize(5000L)
    .build();
```

### **API Response Caching**

```java
// For HTTP responses, external API results
Cache<String, ApiResponse> apiCache = JCacheXBuilder.forApiResponseCaching()
    .name("api-responses")
    .maximumSize(1000L)
    .build();
```

## 🚀 Step 5: Add Monitoring

### **Enable Statistics**

```java
Cache<String, User> userCache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(10000L)
    .recordStats(true)  // Enable monitoring
    .build();
```

### **Monitor Performance**

```java
public void showCacheStats() {
    CacheStats stats = userCache.stats();

    System.out.println("=== Cache Performance ===");
    System.out.println("Hit Rate: " + String.format("%.1f%%", stats.hitRate() * 100));
    System.out.println("Total Requests: " + (stats.hitCount() + stats.missCount()));
    System.out.println("Cache Hits: " + stats.hitCount());
    System.out.println("Cache Misses: " + stats.missCount());

    // Performance alerts
    if (stats.hitRate() < 0.8) {
        System.out.println("⚠️  Low hit rate - consider increasing cache size");
    }
}
```

## 🚀 Step 6: Common Patterns

### **Pattern 1: Cache-Aside (Most Common)**

```java
public Product getProduct(String id) {
    // Try cache first
    Product cached = productCache.get(id);
    if (cached != null) {
        return cached;
    }

    // Cache miss - load from database
    Product product = productRepository.findById(id);
    if (product != null) {
        productCache.put(id, product);
    }

    return product;
}
```

### **Pattern 2: Write-Through**

```java
public void updateProduct(String id, Product product) {
    // Update database first
    productRepository.update(id, product);

    // Then update cache
    productCache.put(id, product);
}
```

### **Pattern 3: Bulk Operations**

```java
public void warmUpCache() {
    // Load all products
    List<Product> products = productRepository.findAll();

    // Convert to map for bulk put
    Map<String, Product> productMap = products.stream()
        .collect(Collectors.toMap(
            Product::getId,
            product -> product
        ));

    // Bulk put is much faster
    productCache.putAll(productMap);
    System.out.println("Cached " + products.size() + " products");
}
```

## 🚀 Step 7: Kotlin Support (Optional)

If you're using Kotlin, add this dependency:

```xml
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-kotlin</artifactId>
    <version>2.0.1</version>
</dependency>
```

Then use the Kotlin DSL:

```kotlin
import io.github.dhruv1110.jcachex.kotlin.*

class UserService {
    private val userCache = createReadHeavyCache<String, User> {
        name("users")
        maximumSize(1000)
        recordStats(true)
    }

    fun getUser(id: String): User? {
        return userCache.get(id) ?: run {
            val user = userRepository.findById(id)
            user?.let { userCache.put(id, it) }
            user
        }
    }
}
```

## 🚀 Step 8: Spring Boot Integration (Optional)

For Spring Boot applications, add:

```xml
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-spring</artifactId>
    <version>2.0.1</version>
</dependency>
```

Then use annotations:

```java
@Service
public class UserService {

    @JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY")
    public User findUser(String id) {
        return userRepository.findById(id);
    }
}
```

## 🎯 What You've Accomplished

✅ **Added JCacheX** to your project
✅ **Created a working cache** with real examples
✅ **Implemented caching patterns** used in production
✅ **Added monitoring** to track performance
✅ **Learned best practices** for different use cases

## 🚀 Next Steps

Now that you have the basics working:

1. **[Examples](examples)** - See more real-world patterns
2. **[Core Concepts](core-concepts/cache-profiles)** - Understand cache profiles
3. **[API Reference](api-reference)** - Learn all available methods
4. **[Performance](performance)** - Optimize for your use case

## 🔧 Troubleshooting

### **Common Issues**

| Problem | Solution |
|---------|----------|
| **"Cannot resolve symbol"** | Refresh your project after adding dependency |
| **Cache not working** | Check that you're calling `put()` before `get()` |
| **Memory issues** | Reduce `maximumSize` or use weight-based sizing |
| **Poor performance** | Use the right cache profile for your workload |

### **Need Help?**

- Check the **[Examples](examples)** for working code
- Review the **[API Reference](api-reference)** for method details
- Look at **[Performance](performance)** for optimization tips

---

**Congratulations! You now have a working cache that can make your application 10-100x faster. 🎉**
