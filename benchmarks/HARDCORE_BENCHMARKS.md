# JCacheX Hardcore Benchmark Suite

## Overview

The JCacheX Hardcore Benchmark Suite is designed to push cache implementations to their absolute limits, revealing performance characteristics under extreme conditions that mirror real-world production scenarios.

## Benchmark Modes

### 1. **Standard Benchmarks** (Default)
```bash
./run-benchmarks.sh
```
- **Purpose**: Basic performance evaluation
- **Configuration**: 3 warmup, 5 measurement, 2 forks
- **Duration**: ~10-15 minutes
- **Tests**: BasicOperations, ConcurrentOperations, ThroughputBenchmark

### 2. **Quick Validation**
```bash
./run-benchmarks.sh --quick
```
- **Purpose**: Fast validation during development
- **Configuration**: 1 warmup, 2 measurement, 1 fork
- **Duration**: ~3-5 minutes
- **Tests**: Same as standard but reduced iterations

### 3. **Hardcore Stress Testing** ⚡
```bash
./run-benchmarks.sh --hardcore
```
- **Purpose**: Extreme stress testing under brutal conditions
- **Configuration**: 5 warmup, 10 measurement, 3 forks, 10s per test
- **Duration**: ~45-60 minutes
- **Memory Usage**: Up to 4GB+ heap
- **Tests**: HardcoreBenchmark with multiple stress scenarios

### 4. **Endurance Testing** 🔥
```bash
./run-benchmarks.sh --endurance
```
- **Purpose**: Long-running sustained load testing
- **Configuration**: 3 warmup, 5 measurement, 2 forks, 120s per test
- **Duration**: ~2-3 hours
- **Memory Usage**: Sustained memory pressure
- **Tests**: EnduranceBenchmark with GC pressure and memory leak detection

### 5. **All Benchmark Suites** 🚀
```bash
./run-benchmarks.sh --all
```
- **Purpose**: Comprehensive evaluation across all scenarios
- **Duration**: ~3-4 hours
- **Tests**: All benchmark suites combined

## Hardcore Benchmark Scenarios

### 🔥 **Extreme Concurrency Tests**
- **Thread Count**: 32+ concurrent threads
- **Access Pattern**: Zipfian distribution (80/20 rule)
- **Contention**: High lock contention scenarios
- **Purpose**: Test cache behavior under extreme thread contention

### 💾 **Memory Pressure Tests**
- **Object Size**: 10KB large objects
- **Cache Size**: 100K entries
- **Working Set**: 1M objects (10x cache size)
- **Purpose**: Test eviction performance under memory pressure

### 📊 **Zipfian Distribution Tests**
- **Hot Keys**: 99% of requests target 1% of keys
- **Thread Count**: 16 concurrent threads
- **Purpose**: Simulate real-world access patterns

### 🔄 **Mixed Workload Tests**
- **Read**: 75% of operations (12 threads)
- **Write**: 18.75% of operations (3 threads)
- **Remove**: 6.25% of operations (1 thread)
- **Purpose**: Test complex production-like workloads

### ⚡ **Eviction Stress Tests**
- **Access Pattern**: 10x cache size working set
- **Thread Count**: 16 concurrent threads
- **Purpose**: Test eviction algorithm performance under constant pressure

## Endurance Benchmark Scenarios

### 🕐 **Sustained Load Tests**
- **Duration**: 2 minutes per test
- **Thread Count**: 8 concurrent threads
- **Memory Tracking**: Real-time memory usage monitoring
- **Purpose**: Test performance degradation over time

### 🗑️ **GC Pressure Tests**
- **Memory Allocation**: Large temporary objects every 1000 operations
- **Thread Count**: 6 concurrent threads
- **Purpose**: Test cache performance under garbage collection pressure

### 💧 **Memory Leak Detection**
- **Operations**: Continuous put/get/remove cycles
- **Monitoring**: Periodic memory usage logging
- **Purpose**: Detect memory leaks over extended periods

### 📈 **Burst Load Tests**
- **Thread Count**: 16 concurrent threads
- **Pattern**: 90% of requests in 10% of time (burst behavior)
- **Purpose**: Test cache behavior under sudden traffic spikes

## Performance Expectations

### **Current Baseline Results** (Standard Benchmarks)
| Cache Implementation | GET Throughput | PUT Throughput | GET Latency |
|---------------------|----------------|----------------|-------------|
| **ConcurrentHashMap** | 227.5M ops/s | 155.9M ops/s | 4.4ns |
| **Caffeine** | 97.1M ops/s | 8.5M ops/s | 10.3ns |
| **EhCache** | 89.0M ops/s | 8.7M ops/s | 11.2ns |
| **JCacheX-Default** | 19.2M ops/s | 4.0M ops/s | 52.1ns |

### **Hardcore Benchmark Expectations**
- **Extreme Concurrency**: 50-80% performance degradation expected
- **Memory Pressure**: 60-90% performance degradation expected
- **Zipfian Access**: 20-40% performance improvement for hot keys
- **Mixed Workloads**: 30-50% performance degradation expected

## Hardware Requirements

### **Minimum Requirements**
- **CPU**: 4+ cores
- **Memory**: 8GB RAM
- **Storage**: 5GB free space
- **Duration**: 1-4 hours depending on mode

### **Recommended Requirements**
- **CPU**: 8+ cores (Apple M1 Pro/Max, Intel i7/i9, AMD Ryzen 7/9)
- **Memory**: 16GB+ RAM
- **Storage**: 10GB free space
- **Java**: OpenJDK 11+ with G1GC or ZGC

## JVM Tuning for Hardcore Benchmarks

### **Recommended JVM Flags**
```bash
export JAVA_OPTS="-Xmx8g -Xms4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+UseJVMCICompiler"
```

### **For Endurance Testing**
```bash
export JAVA_OPTS="-Xmx12g -Xms8g -XX:+UseZGC -XX:+UnlockExperimentalVMOptions -XX:+LogVMOutput"
```

## Interpreting Results

### **Hardcore Benchmarks**
- **Throughput**: Operations per second under extreme conditions
- **Latency**: Response time under high contention
- **Memory Usage**: Peak memory consumption during stress

### **Endurance Benchmarks**
- **Sustained Performance**: Performance over 2-minute intervals
- **GC Impact**: Performance degradation during garbage collection
- **Memory Stability**: Memory usage patterns over time

## Troubleshooting

### **Common Issues**

1. **OutOfMemoryError**
   - Increase heap size: `-Xmx16g`
   - Use different GC: `-XX:+UseZGC`

2. **Test Timeout**
   - Increase JMH timeout: `-Djmh.timeout=300s`
   - Reduce thread count in hardcore tests

3. **System Overload**
   - Reduce concurrent thread count
   - Run tests during off-peak hours

### **Performance Monitoring**
```bash
# Monitor JVM during benchmarks
jstat -gc -t $(pgrep java) 1s

# Monitor system resources
top -p $(pgrep java)
```

## Integration with CI/CD

### **GitHub Actions Example**
```yaml
- name: Run Hardcore Benchmarks
  run: |
    cd benchmarks
    ./run-benchmarks.sh --hardcore

- name: Upload Results
  uses: actions/upload-artifact@v3
  with:
    name: hardcore-benchmark-results
    path: benchmarks/benchmark-results/
```

## Contributing

When adding new hardcore benchmark scenarios:
1. Ensure tests push cache implementations to their limits
2. Include proper memory pressure simulation
3. Add comprehensive documentation
4. Test across different JVM configurations
5. Validate results against known baselines

## Future Enhancements

- **Network Latency Simulation**: For distributed cache testing
- **Disk I/O Pressure**: For persistent cache scenarios
- **Multi-JVM Testing**: For distributed deployment scenarios
- **Benchmarking Different JVM Versions**: OpenJDK vs Oracle vs GraalVM
