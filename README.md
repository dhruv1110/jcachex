# JCacheX

<div align="center">

[![CI](https://github.com/dhruv1110/JCacheX/workflows/CI/badge.svg)](https://github.com/dhruv1110/JCacheX/actions)
[![codecov](https://codecov.io/gh/dhruv1110/JCacheX/branch/main/graph/badge.svg)](https://codecov.io/gh/dhruv1110/JCacheX)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dhruv1110_jcachex&metric=alert_status)](https://sonarcloud.io/project/overview?id=dhruv1110_jcachex)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.dhruv1110/jcachex-core)](https://maven-badges.herokuapp.com/maven-central/io.github.dhruv1110/jcachex-core)
[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://dhruv1110.github.io/jcachex/)
[![javadoc](https://javadoc.io/badge2/io.github.dhruv1110/jcachex-core/javadoc.svg)](https://javadoc.io/doc/io.github.dhruv1110/jcachex-core)

**High-performance caching library for Java applications**

*Profile-based • Local to distributed • Zero code changes*

[**Get Started**](#quick-start) • [**Documentation**](https://dhruv1110.github.io/jcachex/) • [**Examples**](example/) • [**Star this repo**](https://github.com/dhruv1110/jcachex)

</div>

---

## Why JCacheX?

**Stop choosing between simple caching and enterprise features.** JCacheX provides a unified solution that grows with your application using intelligent cache profiles.

```java
// Start with a profile that matches your use case
Cache<String, User> cache = JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY)
    .name("users")
    .maximumSize(1000L)
    .build();

// Scale to distributed with the same API
Cache<String, User> cache = JCacheXBuilder.forDistributedCaching()
    .name("users")
    .maximumSize(1000L)
    .build();
```

### The Problem
- **HashMap**: Fast but limited (no TTL, no eviction, not thread-safe)
- **Caffeine**: Great locally but doesn't scale to distributed
- **Redis**: Powerful but requires infrastructure and network calls
- **Existing solutions**: Force you to choose between simplicity OR enterprise features

### The Solution
**JCacheX bridges the gap** with intelligent cache profiles that automatically configure optimal settings for your specific use case.

---

## Performance Benchmarks

<table>
<tr>
<td align="center"><strong>501M+ ops/sec</strong><br/>JCacheX-ZeroCopy</td>
<td align="center"><strong>98.4% efficiency</strong><br/>CPU scaling</td>
<td align="center"><strong>7.1μs latency</strong><br/>GET operations</td>
<td align="center"><strong>Enterprise-ready</strong><br/>Stress tested</td>
</tr>
</table>

### 🚀 **JCacheX Profile Performance**

**Ultra-High Performance Profiles:**
```
Profile                 | Peak Throughput | Scaling Efficiency | Use Case
JCacheX-ZeroCopy       | 501.1M ops/sec  | 98.4% (4 threads)  | Ultra-high throughput
JCacheX-WriteHeavy     | 224.6M ops/sec  | 97.2% (4 threads)  | Write-intensive workloads
JCacheX-HighPerformance| 198.4M ops/sec  | 82.9% (4 threads)  | Balanced performance
```

**Specialized Performance Profiles:**
```
Profile                    | Throughput     | Efficiency | Optimization Focus
JCacheX-ReadHeavy         | 22.6M ops/sec  | 93.7%     | Read-intensive (80%+ reads)
JCacheX-HardwareOptimized | 143.9M ops/sec | 80.6%     | CPU-specific features
JCacheX-MemoryEfficient   | 31.0M ops/sec  | 23.7%     | Memory-constrained environments
```

**Latency Performance:**
```
Profile                 | GET Latency | PUT Latency | Performance Rating
JCacheX-WriteHeavy     | 7.1μs       | 4.3μs       | ⭐⭐⭐⭐⭐
JCacheX-HighPerformance| 7.2μs       | 4.2μs       | ⭐⭐⭐⭐⭐
JCacheX-ZeroCopy       | 8.9μs       | 5.0μs       | ⭐⭐⭐⭐
```

> **Benchmark Environment**: Apple M1 Pro, 10 cores, 32GB RAM, OpenJDK 21
> **Methodology**: JMH framework, 5 warmup/10 measurement iterations, 3 forks
> **Full Results**: See [benchmarks/](benchmarks/) directory for complete analysis

---

## Quick Start

### 1. Add Dependency
```xml
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>0.1.18</version>
</dependency>
```

### 2. Choose Your Cache Pattern

## 🎯 Easy-to-Use Examples

### **1. Profile-Based Creation (Type Safe)**
```java
// Using ProfileName enum for compile-time safety
Cache<String, User> userCache = JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY)
    .name("users")
    .maximumSize(1000L)
    .build();
```

### **2. Convenience Methods (One-liner Creation)**
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

// All 12 profiles supported with convenience methods
```

### **3. Smart Defaults (Automatic Selection)**
```java
// Let JCacheX choose optimal profile based on workload characteristics
Cache<String, Data> smartCache = JCacheXBuilder.withSmartDefaults()
    .workloadCharacteristics(WorkloadCharacteristics.builder()
        .readToWriteRatio(8.0) // Read-heavy
        .accessPattern(WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
        .build())
    .build();
```

### **4. Kotlin DSL Integration**
```kotlin
// Convenience methods with DSL
val readHeavyCache = createReadHeavyCache {
    name("products")
    maximumSize(5000L)
}

val sessionCache = createSessionCache {
    name("sessions")
    maximumSize(2000L)
}

// All 12 profiles supported
```

### **5. Spring Boot Integration**
```yaml
# Configuration-based
jcachex:
  caches:
    users:
      profile: READ_HEAVY
      maximumSize: 5000
```

### 3. See It Work
```java
// Check performance
CacheStats stats = cache.stats();
System.out.println("Hit rate: " + (stats.hitRate() * 100) + "%");
// Output: Hit rate: 94.7%
```

---

## Cache Profiles

JCacheX uses intelligent profiles to automatically configure optimal cache settings for your specific use case. No more guessing about eviction strategies, initial capacities, or concurrency levels.

### Core Profiles (Most Common)

| Profile | Best For | Eviction Strategy | Default Size | Memory Usage |
|---------|----------|-------------------|--------------|--------------|
| **DEFAULT** | General-purpose caching | TinyWindowLFU | 1,000 | Medium |
| **READ_HEAVY** | 80%+ read operations | Enhanced LFU | 1,000 | Medium |
| **WRITE_HEAVY** | 50%+ write operations | Enhanced LRU | 1,000 | Medium |
| **MEMORY_EFFICIENT** | Memory-constrained environments | LRU | 100 | Low |
| **HIGH_PERFORMANCE** | Maximum throughput | Enhanced LFU | 10,000 | High |

### Specialized Profiles

| Profile | Best For | Eviction Strategy | Default Size | TTL |
|---------|----------|-------------------|--------------|-----|
| **SESSION_CACHE** | User session storage | LRU | 2,000 | 30 min |
| **API_CACHE** | External API responses | TinyWindowLFU | 500 | 15 min |
| **COMPUTE_CACHE** | Expensive computations | Enhanced LFU | 1,000 | 2 hours |

### Advanced Profiles

| Profile | Best For | Eviction Strategy | Default Size | Special Features |
|---------|----------|-------------------|--------------|------------------|
| **ML_OPTIMIZED** | Machine learning workloads | Enhanced LRU | 500 | Predictive caching |
| **ZERO_COPY** | Ultra-low latency (HFT) | LRU | 10,000 | Direct memory buffers |
| **HARDWARE_OPTIMIZED** | CPU-intensive workloads | Enhanced LRU | 1,000 | SIMD optimizations |
| **DISTRIBUTED** | Multi-node clustering | Enhanced LRU | 5,000 | Network-aware |

### Profile Tradeoffs

| Aspect | Core Profiles | Specialized Profiles | Advanced Profiles |
|--------|---------------|---------------------|-------------------|
| **Setup Complexity** | Simple | Simple | Moderate |
| **Performance** | High | High | Very High |
| **Memory Usage** | Configurable | Optimized | Highly Optimized |
| **Features** | Standard | Use-case specific | Cutting-edge |
| **Stability** | Production-ready | Production-ready | Beta/Experimental |
| **Learning Curve** | Low | Low | Medium |

---

## Key Features

<table>
<tr>
<td>

**High Performance**
- Sub-microsecond latency
- 50M+ operations/second
- Lock-free algorithms
- Minimal GC pressure

</td>
<td>

**Async Operations**
- CompletableFuture support
- Kotlin coroutines
- Non-blocking operations
- Reactive compatibility

</td>
</tr>
<tr>
<td>

**Smart Eviction**
- LRU, LFU, FIFO, TTL
- Weight-based eviction
- Composite strategies
- Custom algorithms

</td>
<td>

**Profile-Based**
- 12 built-in profiles
- Automatic optimization
- Use-case specific tuning
- Custom profile creation

</td>
</tr>
<tr>
<td>

**Production Ready**
- Comprehensive metrics
- Circuit breaker support
- Cache warming strategies
- Event-driven architecture

</td>
<td>

**Developer Friendly**
- Fluent API design
- Kotlin DSL support
- Spring Boot integration
- Comprehensive documentation

</td>
</tr>
</table>

---

## Comprehensive Usage Examples

### Basic Usage
```java
// Simple cache with smart defaults
Cache<String, String> cache = JCacheXBuilder.create()
    .name("simple")
    .maximumSize(100L)
    .build();

cache.put("key1", "value1");
String value = cache.get("key1");
```

### Advanced Configuration
```java
// Custom configuration with async loading
Cache<String, User> userCache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users")
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .expireAfterAccess(Duration.ofMinutes(10))
    .loader(userId -> loadUserFromDatabase(userId))
    .recordStats(true)
    .build();
```

### Multi-Language Support

#### Java
```java
// Profile-based approach
Cache<String, Product> productCache = JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY)
    .name("products")
    .maximumSize(5000L)
    .build();

// Convenience method
Cache<String, UserSession> sessionCache = JCacheXBuilder.forSessionStorage()
    .name("sessions")
    .maximumSize(2000L)
    .build();

// Smart defaults
Cache<String, Order> orderCache = JCacheXBuilder.withSmartDefaults()
    .workloadCharacteristics(WorkloadCharacteristics.builder()
        .readToWriteRatio(6.0)
        .build())
    .build();
```

#### Kotlin
```kotlin
// DSL style with convenience methods
val readHeavyCache = createReadHeavyCache<String, Product> {
    name("products")
    maximumSize(5000L)
}

val sessionCache = createSessionCache<String, UserSession> {
    name("sessions")
    maximumSize(2000L)
}

// Profile-based with DSL
val profileCache = createCacheWithProfile<String, Data>(ProfileName.HIGH_PERFORMANCE) {
    name("high-perf")
    maximumSize(10000L)
}
```

#### Spring Boot
```yaml
# application.yml
jcachex:
  caches:
    users:
      profile: READ_HEAVY
      maximumSize: 5000
    sessions:
      profile: SESSION_CACHE
      maximumSize: 2000
    products:
      profile: HIGH_PERFORMANCE
      maximumSize: 10000
```

### Performance Optimized Examples

#### Ultra-Low Latency
```java
// Zero-copy optimized for HFT
Cache<String, MarketData> marketData = JCacheXBuilder.forUltraLowLatency()
    .name("market-data")
    .maximumSize(100000L)
    .build();
```

#### Memory Constrained
```java
// Optimized for limited memory
Cache<String, Config> configCache = JCacheXBuilder.forMemoryConstrainedEnvironment()
    .name("config")
    .maximumSize(50L)
    .build();
```

#### Machine Learning
```java
// ML workload optimization
Cache<String, ModelResult> mlCache = JCacheXBuilder.forMachineLearning()
    .name("ml-predictions")
    .maximumSize(1000L)
    .build();
```

---

## All 12 Convenience Methods

JCacheX provides one-liner cache creation for all common use cases:

```java
// Core profiles
JCacheXBuilder.create()                          // Default profile
JCacheXBuilder.forReadHeavyWorkload()           // 80%+ reads
JCacheXBuilder.forWriteHeavyWorkload()          // 50%+ writes
JCacheXBuilder.forMemoryConstrainedEnvironment() // Limited memory
JCacheXBuilder.forHighPerformance()             // Maximum throughput

// Specialized profiles
JCacheXBuilder.forSessionStorage()              // User sessions
JCacheXBuilder.forApiResponseCaching()          // External APIs
JCacheXBuilder.forComputationCaching()          // Expensive computations

// Advanced profiles
JCacheXBuilder.forMachineLearning()             // ML workloads
JCacheXBuilder.forUltraLowLatency()            // HFT/Gaming
JCacheXBuilder.forHardwareOptimization()        // CPU-intensive
JCacheXBuilder.forDistributedCaching()          // Multi-node
```

---

## Module Structure

```
jcachex/
├── jcachex-core/        # Core caching functionality
├── jcachex-kotlin/      # Kotlin extensions and DSL
├── jcachex-spring/      # Spring Boot integration
└── examples/            # Comprehensive examples
    ├── java/           # Java examples
    ├── kotlin/         # Kotlin examples
    └── springboot/     # Spring Boot examples
```

---

## Getting Started

### Installation

#### Maven
```xml
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>0.1.18</version>
</dependency>
```

#### Gradle
```gradle
implementation 'io.github.dhruv1110:jcachex-core:0.1.18'
```

### Hello World Example

```java
import io.github.dhruv1110.jcachex.JCacheXBuilder;

public class HelloJCacheX {
    public static void main(String[] args) {
        // Create cache with one line
        var cache = JCacheXBuilder.forReadHeavyWorkload()
            .name("hello").maximumSize(1000L).build();

        // Use it
        cache.put("hello", "world");
        System.out.println(cache.get("hello")); // "world"

        // Check performance
        System.out.println("Hit rate: " + cache.stats().hitRate() * 100 + "%");
    }
}
```

---

## Advanced Features

### Distributed Caching
```java
// Distributed cache with automatic failover
Cache<String, User> distributedCache = JCacheXBuilder.forDistributedCaching()
    .name("users")
    .maximumSize(5000L)
    .build();
```

### Async Operations
```java
// Async loading with CompletableFuture
Cache<String, Data> asyncCache = JCacheXBuilder.forHighPerformance()
    .asyncLoader(key -> CompletableFuture.supplyAsync(() -> loadData(key)))
    .build();
```

### Event Listeners
```java
// Monitor cache events
Cache<String, User> monitoredCache = JCacheXBuilder.forReadHeavyWorkload()
    .listener(new CacheEventListener<String, User>() {
        @Override
        public void onEvict(String key, User value, EvictionReason reason) {
            System.out.println("Evicted: " + key + " due to " + reason);
        }
    })
    .build();
```

---

## Documentation

- **[API Reference](https://javadoc.io/doc/io.github.dhruv1110/jcachex-core)** - Complete API documentation
- **[User Guide](https://dhruv1110.github.io/jcachex/)** - Comprehensive user guide
- **[Examples](example/)** - Real-world examples
- **[Performance](benchmarks/)** - Detailed benchmarks

---

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/dhruv1110/jcachex/issues)
- **Documentation**: [Complete documentation](https://dhruv1110.github.io/jcachex/)
- **Examples**: [Working examples](example/)

---

<div align="center">

**[⭐ Star this repo](https://github.com/dhruv1110/jcachex)** if you find JCacheX useful!

Made with ❤️ by [Dhruv](https://github.com/dhruv1110)

</div>
