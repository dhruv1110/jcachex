# JCacheX Distributed Caching Examples

This directory demonstrates JCacheX's **game-changing distributed caching** capabilities that set it apart from competitors like Caffeine, Ehcache, and Redis.

## 🚀 Key Differentiators

### 1. **Seamless Local-to-Distributed Migration**
Unlike other libraries that force you to choose between local OR distributed caching, JCacheX provides a **unified API** that scales seamlessly:

```java
// Start with local cache (same API)
Cache<String, User> cache = CacheFactory.local()
    .name("users")
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .create();

// Scale to distributed with ZERO code changes
Cache<String, User> cache = CacheFactory.distributed()
    .name("users")
    .clusterName("user-service")
    .nodes("cache-1:8080", "cache-2:8080", "cache-3:8080")
    .replicationFactor(2)
    .consistencyLevel(ConsistencyLevel.EVENTUAL)
    .maximumSize(10000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .create();
```

### 2. **Environment-Aware Adaptive Caching**
Automatically choose the best caching strategy based on environment:

```java
Cache<String, User> adaptiveCache = CacheFactory.adaptive()
    .name("users")
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .distributedWhen(env -> "production".equals(env.get("ENVIRONMENT")))
    .nodes("cache-1:8080", "cache-2:8080")
    .create();
```

### 3. **Multiple Consistency Models**
Choose the right consistency model for your use case:

```java
// Strong consistency for financial data
cache.putWithConsistency("account-123", account, ConsistencyLevel.STRONG);

// Eventual consistency for user sessions
cache.putWithConsistency("session-456", session, ConsistencyLevel.EVENTUAL);

// Session consistency for shopping carts
cache.putWithConsistency("cart-789", cart, ConsistencyLevel.SESSION);
```

### 4. **Integrated Production Features**
All advanced features work together seamlessly:

```java
Cache<String, User> productionCache = CacheFactory.distributed()
    .name("users")
    .clusterName("user-service")
    .nodes("cache-1:8080", "cache-2:8080", "cache-3:8080")
    .replicationFactor(2)
    .consistencyLevel(ConsistencyLevel.EVENTUAL)
    .maximumSize(10000L)
    .expireAfterWrite(Duration.ofHours(1))
    .enableWarming(true)        // Intelligent cache warming
    .enableObservability(true)  // Comprehensive metrics
    .enableResilience(true)     // Circuit breaker integration
    .warmingStrategy(WarmingStrategy.predictive())
    .metricsRegistry(MetricsRegistry.prometheus())
    .circuitBreaker(CircuitBreaker.defaultConfig())
    .create();
```

## 🏆 Competitive Comparison

| Feature | JCacheX | Caffeine | Ehcache | Redis |
|---------|---------|----------|---------|-------|
| **Unified API** | ✅ Local + Distributed | ❌ Local only | ❌ Separate APIs | ❌ Distributed only |
| **Seamless Scaling** | ✅ Zero code changes | ❌ Requires rewrite | ❌ Requires rewrite | ❌ Always distributed |
| **Environment Awareness** | ✅ Adaptive switching | ❌ Manual choice | ❌ Manual choice | ❌ Manual choice |
| **Consistency Models** | ✅ 4 models | ❌ N/A | ❌ Limited | ❌ Limited |
| **Built-in Observability** | ✅ Multiple platforms | ❌ Basic stats | ❌ Basic stats | ❌ Basic stats |
| **Circuit Breaker** | ✅ Integrated | ❌ None | ❌ None | ❌ None |
| **Cache Warming** | ✅ Intelligent | ❌ None | ❌ None | ❌ Manual |
| **Network Protocols** | ✅ TCP/UDP/HTTP/WebSocket | ❌ N/A | ❌ Limited | ❌ Redis protocol |
| **Compression** | ✅ Multiple algorithms | ❌ N/A | ❌ Limited | ❌ Limited |
| **Encryption** | ✅ Built-in TLS | ❌ N/A | ❌ Limited | ❌ Optional |

## 📊 Performance Characteristics

### Local Performance
- **Caffeine-level performance** for local operations
- **Sub-microsecond latency** for cache hits
- **Zero-copy operations** where possible

### Distributed Performance
- **Microsecond-level latency** for distributed operations with eventual consistency
- **Millisecond-level latency** for strong consistency
- **Automatic load balancing** across nodes
- **Intelligent routing** based on key hashing

### Fault Tolerance
- **Automatic failover** when nodes go down
- **Self-healing** cluster topology
- **Partition tolerance** during network splits
- **Circuit breaker** protection for external systems

## 🎯 Use Cases

### 1. **Microservices Architecture**
Perfect for services that need to scale from single instance to cluster:

```java
// Service cache that grows with your application
Cache<String, ServiceResponse> serviceCache = CacheFactory.adaptive()
    .name("service-responses")
    .maximumSize(5000L)
    .expireAfterWrite(Duration.ofMinutes(10))
    .distributedWhen(env -> env.get("REPLICAS") != null &&
                           Integer.parseInt(env.get("REPLICAS")) > 1)
    .nodes(getServiceNodes())
    .create();
```

### 2. **Session Management**
Distributed session store with session consistency:

```java
Cache<String, UserSession> sessionCache = CacheFactory.distributed()
    .name("user-sessions")
    .clusterName("session-cluster")
    .nodes("session-1:8080", "session-2:8080", "session-3:8080")
    .replicationFactor(2)
    .consistencyLevel(ConsistencyLevel.SESSION)
    .expireAfterAccess(Duration.ofHours(2))
    .create();
```

### 3. **High-Performance Applications**
Financial services requiring strong consistency:

```java
Cache<String, Account> accountCache = CacheFactory.distributed()
    .name("accounts")
    .clusterName("trading-cluster")
    .nodes("cache-1:8080", "cache-2:8080", "cache-3:8080")
    .replicationFactor(3)
    .consistencyLevel(ConsistencyLevel.STRONG)
    .maximumSize(100000L)
    .expireAfterWrite(Duration.ofMinutes(5))
    .enableObservability(true)
    .metricsRegistry(MetricsRegistry.datadog())
    .create();
```

### 4. **Content Delivery**
CDN-like caching with intelligent warming:

```java
Cache<String, Content> contentCache = CacheFactory.distributed()
    .name("content")
    .clusterName("cdn-cluster")
    .nodes("edge-1:8080", "edge-2:8080", "edge-3:8080")
    .replicationFactor(2)
    .consistencyLevel(ConsistencyLevel.EVENTUAL)
    .maximumSize(1000000L)
    .expireAfterWrite(Duration.ofHours(24))
    .enableWarming(true)
    .warmingStrategy(WarmingStrategy.predictive()
        .priority(WarmingPriority.HIGH)
        .batchSize(1000)
        .maxConcurrency(10))
    .create();
```

## 🌟 Advanced Features

### Network Protocol Selection
Choose the best protocol for your use case:

```java
// High-performance TCP for internal clusters
NetworkProtocol tcpProtocol = NetworkProtocol.tcp()
    .serialization(SerializationType.KRYO)
    .compression(CompressionType.LZ4)
    .encryption(true)
    .build();

// HTTP for cross-platform compatibility
NetworkProtocol httpProtocol = NetworkProtocol.http()
    .serialization(SerializationType.JSON)
    .compression(CompressionType.GZIP)
    .build();
```

### Custom Consistency Models
Define your own consistency requirements:

```java
// Custom consistency for specific business logic
cache.putWithConsistency("critical-data", data, ConsistencyLevel.STRONG);
cache.putWithConsistency("user-preferences", prefs, ConsistencyLevel.SESSION);
cache.putWithConsistency("analytics-data", analytics, ConsistencyLevel.EVENTUAL);
```

### Cluster Management
Dynamic cluster topology:

```java
DistributedCache<String, Object> cluster = // ... create cache

// Add nodes dynamically
cluster.addNode("new-node:8080").thenRun(() ->
    System.out.println("Node added successfully"));

// Monitor cluster health
ClusterTopology topology = cluster.getClusterTopology();
System.out.println("Healthy nodes: " + topology.getHealthyNodeCount());

// Rebalance data across nodes
cluster.rebalance().thenRun(() ->
    System.out.println("Rebalancing completed"));
```

## 🚦 Getting Started

1. **Add Dependencies**:
   ```xml
   <dependency>
       <groupId>io.github.dhruv1110</groupId>
       <artifactId>jcachex-core</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

2. **Start Simple**:
   ```java
   Cache<String, String> cache = CacheFactory.local()
       .name("my-cache")
       .maximumSize(1000L)
       .create();
   ```

3. **Scale Up**:
   ```java
   Cache<String, String> cache = CacheFactory.distributed()
       .name("my-cache")
       .clusterName("my-cluster")
       .nodes("node-1:8080", "node-2:8080")
       .create();
   ```

4. **Go Production**:
   ```java
   Cache<String, String> cache = CacheFactory.distributed()
       .name("my-cache")
       .clusterName("my-cluster")
       .nodes("node-1:8080", "node-2:8080", "node-3:8080")
       .replicationFactor(2)
       .consistencyLevel(ConsistencyLevel.EVENTUAL)
       .enableWarming(true)
       .enableObservability(true)
       .enableResilience(true)
       .create();
   ```

## 🎯 Why Choose JCacheX?

### **For Caffeine Users**
- Keep all the performance benefits of Caffeine
- Add distributed capabilities without changing code
- Get production-ready features out of the box

### **For Ehcache Users**
- Modern, fluent API design
- Better performance and lower memory footprint
- Integrated advanced features (warming, observability, resilience)

### **For Redis Users**
- Eliminate network round-trips with local caching
- Maintain distributed benefits with better consistency models
- Reduce infrastructure complexity

### **For New Projects**
- Start simple, scale seamlessly
- Production-ready from day one
- Future-proof architecture

## 🏗️ Architecture Benefits

1. **Hybrid Architecture**: Best of both local and distributed caching
2. **Consistency Choice**: Pick the right consistency model per use case
3. **Fault Tolerance**: Built-in resilience and self-healing
4. **Observability**: Comprehensive metrics and monitoring
5. **Performance**: Caffeine-level local performance + distributed availability
6. **Simplicity**: One API for all caching needs

**JCacheX distributed caching isn't just another caching library – it's a complete caching platform that grows with your application from prototype to planet-scale.**
