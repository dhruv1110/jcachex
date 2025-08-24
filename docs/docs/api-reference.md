# API Reference

This guide shows you how to use JCacheX's key features and APIs. Focus on what you can do, not how it's implemented.

## 🚀 Quick Start

### Basic Cache Creation

```java
// Simple cache with default settings
Cache<String, User> cache = JCacheXBuilder.newBuilder()
    .name("users")
    .maximumSize(1000L)
    .build();

// Profile-based cache (recommended)
Cache<String, Product> products = JCacheXBuilder.forReadHeavyWorkload()
    .name("products")
    .maximumSize(10000L)
    .build();
```

### Kotlin DSL

```kotlin
// Simple cache
val users = createCache<String, User> {
    name("users")
    maximumSize(1000)
}

// Profile-based cache
val products = createReadHeavyCache<String, Product> {
    name("products")
    maximumSize(10000)
}
```

## 📚 Core API Methods

### Basic Operations

```java
// Store and retrieve
cache.put("key", value);
User user = cache.get("key");

// Check if exists
if (cache.containsKey("key")) {
    // Key exists
}

// Remove
cache.remove("key");

// Clear all
cache.clear();
```

### Bulk Operations

```java
// Store multiple items
Map<String, User> users = Map.of(
    "user1", new User("Alice"),
    "user2", new User("Bob")
);
cache.putAll(users);

// Get multiple items
List<String> keys = List.of("user1", "user2");
Map<String, User> retrieved = cache.getAll(keys);
```

### Async Operations

```java
// Async get
CompletableFuture<User> future = cache.getAsync("key");
future.thenAccept(user -> {
    if (user != null) {
        System.out.println("Found user: " + user.getName());
    }
});

// Async put
CompletableFuture<Void> putFuture = cache.putAsync("key", new User("Charlie"));
putFuture.thenRun(() -> System.out.println("User stored successfully"));
```

## 🎯 Cache Profiles

### Available Profiles

```java
// Read-heavy workloads (product catalogs, reference data)
Cache<String, Product> products = JCacheXBuilder.forReadHeavyWorkload()
    .name("products")
    .maximumSize(10000L)
    .build();

// Write-heavy workloads (logging, analytics)
Cache<String, LogEntry> logs = JCacheXBuilder.forWriteHeavyWorkload()
    .name("logs")
    .maximumSize(5000L)
    .build();

// API response caching
Cache<String, ApiResponse> apiCache = JCacheXBuilder.forApiResponseCaching()
    .name("api")
    .maximumSize(1000L)
    .build();

// Session storage
Cache<String, UserSession> sessions = JCacheXBuilder.forSessionStorage()
    .name("sessions")
    .maximumSize(10000L)
    .build();

// High-performance scenarios
Cache<String, Object> highPerf = JCacheXBuilder.forHighPerformance()
    .name("high-perf")
    .maximumSize(100000L)
    .build();
```

### Custom Profile Configuration

```java
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .name("custom-users")
    .maximumSize(5000L)
    .evictionStrategy(new LRUEvictionStrategy<>())
    .recordStats(true)
    .build();

Cache<String, User> cache = new DefaultCache<>(config);
```

## 🔧 Configuration Options

### Size and Memory

```java
CacheConfig<String, Object> config = CacheConfig.<String, Object>builder()
    .maximumSize(10000L)           // Maximum number of entries
    .maximumWeight(100 * 1024 * 1024L)  // Maximum memory in bytes
    .weigher((key, value) -> ((String) value).length())  // Custom weight calculation
    .build();
```

### Eviction Strategies

```java
// LRU (Least Recently Used) - default
.evictionStrategy(new LRUEvictionStrategy<>())

// LFU (Least Frequently Used)
.evictionStrategy(new LFUEvictionStrategy<>())

// FIFO (First In, First Out)
.evictionStrategy(new FIFOEvictionStrategy<>())

// TTL (Time To Live)
.evictionStrategy(new TTLEvictionStrategy<>(Duration.ofMinutes(30)))

// Composite (multiple strategies)
.evictionStrategy(new CompositeEvictionStrategy<>(
    new LRUEvictionStrategy<>(),
    new TTLEvictionStrategy<>(Duration.ofHours(1))
))
```

### Statistics and Monitoring

```java
CacheConfig<String, Object> config = CacheConfig.<String, Object>builder()
    .recordStats(true)  // Enable statistics
    .build();

Cache<String, Object> cache = new DefaultCache<>(config);

// Get statistics
CacheStats stats = cache.stats();
System.out.println("Hit rate: " + (stats.hitRate() * 100) + "%");
System.out.println("Miss rate: " + (stats.missRate() * 100) + "%");
System.out.println("Evictions: " + stats.evictionCount());
```

## 🔄 Event Handling

### Cache Event Listeners

```java
cache.addListener(new CacheEventListener<String, User>() {
    @Override
    public void onEntryAdded(String key, User value) {
        System.out.println("Added: " + key + " -> " + value.getName());
    }
    
    @Override
    public void onEntryRemoved(String key, User value) {
        System.out.println("Removed: " + key + " -> " + value.getName());
    }
    
    @Override
    public void onEntryUpdated(String key, User oldValue, User newValue) {
        System.out.println("Updated: " + key + " from " + oldValue.getName() + " to " + newValue.getName());
    }
});
```

## 🚀 Advanced Features

### Distributed Caching

```java
// For distributed caching across multiple nodes
Cache<String, Object> distributedCache = JCacheXBuilder.forDistributedCaching()
    .name("distributed")
    .maximumSize(50000L)
    .build();
```

### Cache Loading

```java
// Automatic loading from external source
CacheLoader<String, User> loader = new CacheLoader<String, User>() {
    @Override
    public User load(String key) {
        return userRepository.findById(key);
    }
};

CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .cacheLoader(loader)
    .build();
```

## 🌐 Distributed Caching

### **Overview**

JCacheX supports distributed caching across multiple nodes using Kubernetes for service discovery and TCP for communication. This allows you to:

- **Scale horizontally** by adding more cache nodes
- **Share cache data** across multiple application instances
- **Maintain consistency** across the distributed cache
- **Handle node failures** gracefully

### **Basic Distributed Cache**

```java
// Create a distributed cache
Cache<String, User> distributedCache = JCacheXBuilder.forDistributedCaching()
    .name("distributed-users")
    .maximumSize(100000L)
    .build();
```

### **Kubernetes Configuration**

#### **1. Service Discovery Setup**

Create a Kubernetes service for JCacheX:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: jcachex-service
  labels:
    app: jcachex
spec:
  selector:
    app: jcachex
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
      name: jcachex
  type: ClusterIP
```

#### **2. Deployment Configuration**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jcachex-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: jcachex
  template:
    metadata:
      labels:
        app: jcachex
    spec:
      containers:
      - name: jcachex
        image: your-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: JCACHEX_KUBERNETES_NAMESPACE
          value: "default"
        - name: JCACHEX_KUBERNETES_SERVICE_NAME
          value: "jcachex-service"
        - name: JCACHEX_COMMUNICATION_PORT
          value: "8080"
```

#### **3. Required Ports**

| Port | Purpose | Protocol | Direction |
|------|---------|----------|-----------|
| **8080** | JCacheX communication | TCP | Inbound/Outbound |
| **8081** | Health checks | TCP | Inbound |
| **8082** | Metrics endpoint | TCP | Inbound |

### **Communication Protocol**

JCacheX uses a custom TCP-based protocol for node-to-node communication:

- **Port 8080**: Main communication port for cache operations
- **Heartbeat**: Every 30 seconds to detect node failures
- **Data synchronization**: Automatic replication of cache data
- **Load balancing**: Automatic distribution of cache requests

### **Configuration Options**

```java
// Advanced distributed cache configuration
Cache<String, User> distributedCache = JCacheXBuilder.forDistributedCaching()
    .name("distributed-users")
    .maximumSize(100000L)
    .distributedConfig(DistributedConfig.builder()
        .kubernetesNamespace("default")
        .kubernetesServiceName("jcachex-service")
        .communicationPort(8080)
        .heartbeatInterval(Duration.ofSeconds(30))
        .replicationFactor(2)
        .build())
    .build();
```

### **Environment Variables**

Set these environment variables in your Kubernetes deployment:

```bash
# Kubernetes configuration
JCACHEX_KUBERNETES_NAMESPACE=default
JCACHEX_KUBERNETES_SERVICE_NAME=jcachex-service

# Communication settings
JCACHEX_COMMUNICATION_PORT=8080
JCACHEX_HEARTBEAT_INTERVAL=30000
JCACHEX_REPLICATION_FACTOR=2

# Performance tuning
JCACHEX_CONNECTION_POOL_SIZE=10
JCACHEX_REQUEST_TIMEOUT=5000
```

### **Health Checks**

JCacheX provides health check endpoints:

```yaml
# Add to your deployment
livenessProbe:
  httpGet:
    path: /health
    port: 8081
  initialDelaySeconds: 30
  periodSeconds: 10
readinessProbe:
  httpGet:
    path: /ready
    port: 8081
  initialDelaySeconds: 5
  periodSeconds: 5
```

### **Monitoring Distributed Cache**

```java
public class DistributedCacheMonitor {
    
    public void checkClusterHealth(Cache<String, Object> cache) {
        if (cache instanceof DistributedCache) {
            DistributedCache<String, Object> distributed = (DistributedCache<String, Object>) cache;
            
            // Get cluster information
            List<NodeInfo> nodes = distributed.getClusterNodes();
            System.out.println("Cluster nodes: " + nodes.size());
            
            for (NodeInfo node : nodes) {
                System.out.println("Node: " + node.getId() + 
                    " - Status: " + node.getStatus() + 
                    " - Address: " + node.getAddress());
            }
            
            // Check cluster health
            ClusterHealth health = distributed.getClusterHealth();
            System.out.println("Cluster health: " + health.getStatus());
            System.out.println("Active nodes: " + health.getActiveNodes());
            System.out.println("Failed nodes: " + health.getFailedNodes());
        }
    }
}
```

### **Best Practices for Distributed Caching**

#### **1. Network Configuration**

```bash
# Ensure proper network policies
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: jcachex-network-policy
spec:
  podSelector:
    matchLabels:
      app: jcachex
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: jcachex
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: jcachex
    ports:
    - protocol: TCP
      port: 8080
EOF
```

#### **2. Resource Limits**

```yaml
# Add to your container spec
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

#### **3. Scaling Considerations**

```java
// Monitor cluster size and adjust accordingly
public class ClusterScaler {
    
    public void checkScalingNeeds(Cache<String, Object> cache) {
        if (cache instanceof DistributedCache) {
            DistributedCache<String, Object> distributed = (DistributedCache<String, Object>) cache;
            
            ClusterHealth health = distributed.getClusterHealth();
            
            if (health.getActiveNodes() < 3) {
                System.out.println("⚠️  Consider scaling up - only " + 
                    health.getActiveNodes() + " active nodes");
            }
            
            if (health.getFailedNodes() > 0) {
                System.out.println("⚠️  Failed nodes detected: " + 
                    health.getFailedNodes());
            }
        }
    }
}
```

### **Troubleshooting Distributed Cache**

#### **Common Issues**

| Problem | Symptoms | Solution |
|---------|----------|----------|
| **Node discovery fails** | Cache not distributed | Check Kubernetes service and labels |
| **Communication errors** | Cache operations fail | Verify port 8080 is open and accessible |
| **Data inconsistency** | Different values on different nodes | Check replication factor and sync settings |
| **Performance degradation** | Slow cache operations | Monitor network latency and node health |

#### **Debug Commands**

```bash
# Check if pods can communicate
kubectl exec -it <pod-name> -- nc -zv <other-pod-ip> 8080

# Check service endpoints
kubectl get endpoints jcachex-service

# Check pod labels
kubectl get pods --show-labels

# Check network policies
kubectl get networkpolicies
```

## 📊 Performance Monitoring

### Built-in Metrics

```java
CacheStats stats = cache.stats();

// Performance metrics
double hitRate = stats.hitRate();
long totalRequests = stats.hitCount() + stats.missCount();
long evictions = stats.evictionCount();

// Load metrics
long loadCount = stats.loadCount();
double averageLoadTime = stats.averageLoadTime() / 1_000_000.0; // in milliseconds
```

### Custom Metrics

```java
// Export metrics for external monitoring
Map<String, Object> metrics = Map.of(
    "hit_rate", stats.hitRate(),
    "miss_rate", stats.missRate(),
    "eviction_rate", (double) stats.evictionCount() / totalRequests,
    "load_time_avg_ms", averageLoadTime
);
```

## 🎯 Best Practices

### Choose the Right Profile

- **READ_HEAVY**: Product catalogs, reference data, configuration
- **WRITE_HEAVY**: Logging, analytics, real-time data
- **API_CACHE**: HTTP responses, external API results
- **SESSION_CACHE**: User sessions, temporary data
- **HIGH_PERFORMANCE**: Critical path operations
- **MEMORY_EFFICIENT**: Large datasets with memory constraints

### Size Your Cache Appropriately

```java
// For read-heavy workloads
long cacheSize = (long) (datasetSize * 0.8); // Cache 80% of data

// For write-heavy workloads
long cacheSize = (long) (datasetSize * 0.2); // Cache 20% of data

// For API caching
long cacheSize = (long) (requestsPerSecond * 60); // Cache 1 minute of requests
```

### Monitor and Tune

```java
// Regular monitoring
if (stats.hitRate() < 0.8) {
    // Consider increasing cache size or improving key distribution
}

if (stats.evictionCount() > 1000) {
    // Cache is too small, increase size
}
```

This API reference focuses on what you can do with JCacheX and how to use it effectively, rather than internal implementation details.
