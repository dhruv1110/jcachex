# JCacheX Performance Benchmark Results

## System Information
- **Date:** Thu Jul 10 13:15:57 EDT 2025
- **Java Version:** java version "21.0.2" 2024-01-16 LTS
- **System:** Darwin arm64
- **CPU Cores:** 10

## Benchmark Categories

### 1. Basic Operations
- **File:** `basic_operations_results.txt`
- **Description:** Single-threaded performance for fundamental cache operations
- **Metrics:** Average time per operation (microseconds)

### 2. Concurrent Operations
- **File:** `concurrent_operations_results.txt`
- **Description:** Multi-threaded performance under various load patterns
- **Metrics:** Operations per second under concurrent load

### 3. Throughput
- **File:** `throughput_results.txt`
- **Description:** Maximum sustained throughput measurements
- **Metrics:** Operations per second for different thread counts

### 4. Batch Eviction Optimization (NEW)
- **File:** `batch_eviction_optimization_results.txt`
- **Description:** Comprehensive comparison of JCacheX default vs batch eviction configurations
- **Metrics:** Average time per operation for single operations and bulk operations
- **Key Features Tested:**
  - nanoTime optimizations for faster expiration checks
  - Batch eviction with different thresholds (default: 10 ops/100ms, aggressive: 50 ops/1s)
  - Single GET/PUT operations vs bulk operations (10 puts per operation)

### 5. Quick Batch Comparison (NEW)
- **File:** `quick_batch_comparison_results.txt`
- **Description:** Fast validation of batch eviction impact on common operations
- **Metrics:** Average time per operation (microseconds)
- **Focus:** Real-world single operation performance with batch eviction enabled/disabled

## Libraries Tested

| Library | Version | Description |
|---------|---------|-------------|
| JCacheX | latest | High-performance caching library |
| JCacheX Batch | latest | JCacheX with batch eviction optimizations |
| JCacheX Batch Aggressive | latest | JCacheX with aggressive batch eviction settings |
| Caffeine | 3.1.8 | High performance Java caching library |
| Cache2k | 2.6.1 | High performance Java cache |
| EHCache | 3.10.8 | Terracotta's enterprise caching solution |
| ConcurrentHashMap | JDK | Standard Java concurrent map |

## Key Findings

### Performance Highlights
- JCacheX demonstrates competitive performance across all benchmark categories
- Excellent concurrent performance with minimal contention
- Low latency for both read and write operations
- Efficient memory usage and garbage collection impact

### Batch Eviction Optimization Results
- **nanoTime optimizations:** Consistent 5-10% improvement in expiration checking
- **Batch eviction benefits:** Visible in high-throughput, memory-constrained scenarios
- **Single operation overhead:** Batch eviction adds 10-15% overhead for simple operations
- **Bulk operation performance:** Benefits depend on eviction frequency and memory pressure
- **Recommendation:** Use batch eviction for write-heavy workloads with frequent eviction

### Benchmark Methodology
- JMH (Java Microbenchmark Harness) for accurate measurements
- Multiple warmup and measurement iterations
- Separate JVM forks to avoid contamination
- Realistic workload patterns (80% reads, 20% writes)
- Specialized tests for batch eviction scenarios

## Detailed Results
See individual result files for complete benchmark data and statistical analysis.

## Optimization Configuration Guide

### Default Configuration (Recommended for Most Use Cases)
```java
CacheConfig.<K, V>builder()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .recordStats(false)  // Disable for maximum performance
    .build();
```

### Batch Eviction for High-Throughput Scenarios
```java
// Standard batch eviction
CacheConfig.<K, V>builder()
    .maximumSize(500L)  // Smaller cache for frequent eviction
    .batchEviction(true, 20, Duration.ofMillis(50))
    .recordStats(false)
    .build();

// Aggressive batch eviction for very high write loads
CacheConfig.<K, V>builder()
    .maximumSize(200L)
    .batchEviction(true, 100, Duration.ofMillis(10))
    .recordStats(false)
    .build();
```
