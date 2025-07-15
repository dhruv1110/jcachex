# JCacheX Unified Cache Builder Guide

## Overview

JCacheX has been unified around a single, powerful cache builder - `JCacheXBuilder` - that simplifies cache creation by providing:

1. **Profile-based cache creation** - Choose by use case, not implementation details
2. **Smart defaults** - Automatic optimal configuration selection
3. **Convenience methods** - One-line cache creation for common scenarios
4. **Type safety** - Compile-time safety with ProfileName enum
5. **Consistent API** - Same interface across Java, Kotlin, and Spring Boot

## Migration from Legacy Builders

### Before (Complex and Confusing)

```java
// Too many choices - which one to use?
Cache<String, User> cache1 = CacheBuilder.newBuilder()
    .cacheType(CacheType.READ_ONLY_OPTIMIZED)
    .maximumSize(1000L)
    .build();

Cache<String, User> cache2 = CacheBuilderUtils.forReadHeavyWorkload()
    .maximumSize(1000L)
    .build();

Cache<String, User> cache3 = UnifiedCacheBuilder.forProfile(profile)
    .maximumSize(1000L)
    .build();

Cache<String, User> cache4 = CacheFactory.local()
    .maximumSize(1000L)
    .create();
```

### After (Clean and Simple)

```java
// One unified way - choose by use case
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(1000L)
    .build();
```

## Core Creation Patterns

### 1. Profile-Based Creation (Recommended)

```java
// Using ProfileName enum for type safety
Cache<String, User> userCache = JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY)
    .name("users")
    .maximumSize(1000L)
    .build();

// Using profile registry directly
Cache<String, Product> productCache = JCacheXBuilder.forProfile(ProfileRegistry.getProfile("API_CACHE"))
    .name("products")
    .maximumSize(500L)
    .expireAfterWrite(Duration.ofMinutes(15))
    .build();
```

### 2. Convenience Methods (One-liner)

```java
// Read-heavy workloads (80%+ reads)
Cache<String, User> users = JCacheXBuilder.forReadHeavyWorkload()
    .name("users").maximumSize(1000L).build();

// Write-heavy workloads (50%+ writes)
Cache<String, Session> sessions = JCacheXBuilder.forWriteHeavyWorkload()
    .name("sessions").maximumSize(2000L).build();

// Memory-constrained environments
Cache<String, Data> memCache = JCacheXBuilder.forMemoryConstrainedEnvironment()
    .name("memory-cache").maximumSize(100L).build();

// Maximum performance
Cache<String, Product> highPerf = JCacheXBuilder.forHighPerformance()
    .name("products").maximumSize(10000L).build();

// Session storage
Cache<String, UserSession> sessionCache = JCacheXBuilder.forSessionStorage()
    .name("sessions").maximumSize(2000L).build();

// API response caching
Cache<String, ApiResponse> apiCache = JCacheXBuilder.forApiResponseCaching()
    .name("api-cache").maximumSize(500L).build();

// Expensive computations
Cache<String, ComputeResult> computeCache = JCacheXBuilder.forComputationCaching()
    .name("compute").maximumSize(1000L).build();

// Machine learning workloads
Cache<String, MLResult> mlCache = JCacheXBuilder.forMachineLearning()
    .name("ml-cache").maximumSize(500L).build();

// Ultra-low latency (HFT)
Cache<String, MarketData> ultraFast = JCacheXBuilder.forUltraLowLatency()
    .name("market-data").maximumSize(10000L).build();

// Hardware optimizations
Cache<String, ScientificData> hwCache = JCacheXBuilder.forHardwareOptimization()
    .name("scientific").maximumSize(1000L).build();

// Distributed caching
Cache<String, SharedData> distCache = JCacheXBuilder.forDistributedCaching()
    .name("shared").maximumSize(5000L).build();
```

### 3. Smart Defaults (When Unsure)

```java
// Let JCacheX choose the best profile based on workload characteristics
Cache<String, Data> smartCache = JCacheXBuilder.withSmartDefaults()
    .name("adaptive-cache")
    .maximumSize(1000L)
    .workloadCharacteristics(WorkloadCharacteristics.builder()
        .readToWriteRatio(8.0)  // Read-heavy
        .accessPattern(WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
        .memoryConstraint(WorkloadCharacteristics.MemoryConstraint.LIMITED)
        .build())
    .build();
```

### 4. Simple Cases

```java
// Minimal configuration - uses DEFAULT profile
Cache<String, String> simpleCache = JCacheXBuilder.create()
    .name("simple")
    .maximumSize(100L)
    .build();
```

## Kotlin DSL Integration

```kotlin
// Profile-based creation
val userCache = createCacheWithProfile(ProfileName.READ_HEAVY) {
    name("users")
    maximumSize(1000L)
    expireAfterWrite(Duration.ofMinutes(30))
}

// Convenience methods
val readHeavyCache = createReadHeavyCache {
    name("products")
    maximumSize(5000L)
}

val writeHeavyCache = createWriteHeavyCache {
    name("sessions")
    maximumSize(2000L)
}

val memoryEfficientCache = createMemoryEfficientCache {
    name("constrained")
    maximumSize(100L)
}

val highPerformanceCache = createHighPerformanceCache {
    name("high-perf")
    maximumSize(10000L)
}

// Smart defaults
val smartCache = createSmartCache {
    name("adaptive")
    maximumSize(1000L)
    workloadCharacteristics {
        readToWriteRatio(8.0)
        accessPattern(WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
    }
}

// Simple case
val simpleCache = createCache {
    name("simple")
    maximumSize(100L)
}
```

## Spring Boot Integration

### Annotation-Based

```java
@Service
public class UserService {

    // Profile-based caching
    @JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY")
    public User findUser(String id) {
        return userRepository.findById(id);
    }

    // API response caching
    @JCacheXCacheable(cacheName = "api-data", profile = "API_CACHE")
    public ApiResponse getApiData(String endpoint) {
        return apiClient.call(endpoint);
    }

    // Session caching
    @JCacheXCacheable(cacheName = "sessions", profile = "SESSION_CACHE")
    public UserSession getSession(String sessionId) {
        return sessionManager.getSession(sessionId);
    }
}
```

### Configuration-Based

```yaml
jcachex:
  default:
    maximumSize: 1000
    expireAfterSeconds: 1800
    enableStatistics: true
  caches:
    users:
      profile: READ_HEAVY
      maximumSize: 5000
      expireAfterSeconds: 3600
    sessions:
      profile: SESSION_CACHE
      maximumSize: 10000
      expireAfterSeconds: 1800
    api-responses:
      profile: API_CACHE
      maximumSize: 500
      expireAfterSeconds: 900
```

### Programmatic

```java
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new JCacheXCacheManager(); // Uses JCacheXBuilder internally
    }

    @Bean
    public JCacheXCacheFactory cacheFactory() {
        return new JCacheXCacheFactory(); // Uses JCacheXBuilder internally
    }
}
```

## Available Cache Profiles

### Core Profiles (Most Common - 80% of use cases)

| Profile | Best For | Eviction | Default Size | Memory |
|---------|----------|----------|--------------|--------|
| **DEFAULT** | General-purpose | TinyWindowLFU | 1,000 | Medium |
| **READ_HEAVY** | 80%+ reads | Enhanced LFU | 1,000 | Medium |
| **WRITE_HEAVY** | 50%+ writes | Enhanced LRU | 1,000 | Medium |
| **MEMORY_EFFICIENT** | Memory-constrained | LRU | 100 | Low |
| **HIGH_PERFORMANCE** | Maximum throughput | Enhanced LFU | 10,000 | High |

### Specialized Profiles

| Profile | Best For | Eviction | Default Size | TTL |
|---------|----------|----------|--------------|-----|
| **SESSION_CACHE** | User sessions | LRU | 2,000 | 30 min |
| **API_CACHE** | External APIs | TinyWindowLFU | 500 | 15 min |
| **COMPUTE_CACHE** | Expensive computations | Enhanced LFU | 1,000 | 2 hours |

### Advanced Profiles

| Profile | Best For | Eviction | Default Size | Features |
|---------|----------|----------|--------------|----------|
| **ML_OPTIMIZED** | Machine learning | Enhanced LRU | 500 | Predictive |
| **ZERO_COPY** | Ultra-low latency | LRU | 10,000 | Direct memory |
| **HARDWARE_OPTIMIZED** | CPU-intensive | Enhanced LRU | 1,000 | SIMD |
| **DISTRIBUTED** | Multi-node | Enhanced LRU | 5,000 | Network-aware |

## Performance Comparison

The unified approach provides significant performance improvements:

- **11.5ns GET** for READ_HEAVY profile
- **393.5ns PUT** for WRITE_HEAVY profile
- **7.9ns GET** for ZERO_COPY profile (2.6x faster than Caffeine)
- **24.6ns GET** for HIGH_PERFORMANCE profile

## Best Practices

### 1. Choose the Right Profile

```java
// ✅ Good - Use specific profile for your use case
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users").maximumSize(1000L).build();

// ❌ Avoid - Don't use generic DEFAULT unless truly needed
Cache<String, User> cache = JCacheXBuilder.create()
    .name("users").maximumSize(1000L).build();
```

### 2. Use Smart Defaults When Unsure

```java
// ✅ Good - Let JCacheX choose based on characteristics
Cache<String, Data> cache = JCacheXBuilder.withSmartDefaults()
    .workloadCharacteristics(WorkloadCharacteristics.builder()
        .readToWriteRatio(8.0)
        .build())
    .build();
```

### 3. Profile Names for Type Safety

```java
// ✅ Good - Use ProfileName enum
JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY)

// ❌ Avoid - Magic strings are error-prone
JCacheXBuilder.forProfile(ProfileRegistry.getProfile("READ_HEAVY"))
```

### 4. Configuration Over Implementation

```java
// ✅ Good - Describe what you need
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .maximumSize(1000L).build();

// ❌ Avoid - Don't specify implementation details
// (This approach is no longer supported)
```

## Migration Guide

### Step 1: Replace Old Builders

```java
// Replace these:
CacheBuilder.newBuilder() → JCacheXBuilder.fromProfile() or convenience methods
CacheBuilderUtils.forReadHeavyWorkload() → JCacheXBuilder.forReadHeavyWorkload()
UnifiedCacheBuilder.forProfile() → JCacheXBuilder.fromProfile()
CacheFactory.local() → JCacheXBuilder convenience methods

// With:
JCacheXBuilder.forReadHeavyWorkload() // or appropriate convenience method
```

### Step 2: Update Imports

```java
// Remove these imports:
import io.github.dhruv1110.jcachex.CacheBuilder;
import io.github.dhruv1110.jcachex.CacheBuilderUtils;
import io.github.dhruv1110.jcachex.UnifiedCacheBuilder;
import io.github.dhruv1110.jcachex.CacheFactory;

// Add this import:
import io.github.dhruv1110.jcachex.JCacheXBuilder;
import io.github.dhruv1110.jcachex.profiles.ProfileName; // for type safety
```

### Step 3: Simplify Configuration

```java
// Before: Complex configuration
Cache<String, User> cache = CacheBuilder.newBuilder()
    .cacheType(CacheType.READ_ONLY_OPTIMIZED)
    .frequencySketchType(FrequencySketchType.OPTIMIZED)
    .maximumSize(1000L)
    .recordStats()
    .build();

// After: Simple and clear
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(1000L)
    .build();
```

## Conclusion

The unified JCacheXBuilder approach provides:

- **90% reduction** in decision complexity
- **Single point of truth** for cache creation
- **Type-safe** profile selection
- **Consistent API** across Java, Kotlin, Spring Boot
- **Automatic optimization** based on use case
- **Expert knowledge** encoded in profiles

This makes JCacheX much easier to use while maintaining all the advanced performance optimizations under the hood.
