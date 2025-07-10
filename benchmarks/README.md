# JCacheX Performance Benchmarks

This module contains comprehensive performance benchmarks comparing JCacheX against other popular Java caching libraries.

## Overview

The benchmarks are built using [JMH (Java Microbenchmark Harness)](https://openjdk.java.net/projects/code-tools/jmh/), the gold standard for Java performance testing. They compare JCacheX performance against:

- **Caffeine** - High performance Java caching library
- **Cache2k** - High performance Java cache
- **EHCache** - Terracotta's enterprise caching solution
- **JCache** - Standard Java caching API implementations
- **ConcurrentHashMap** - JDK's standard concurrent map (baseline)

## Benchmark Categories

### 1. Basic Operations (`BasicOperationsBenchmark`)
- **Purpose**: Measures single-threaded performance for fundamental cache operations
- **Operations**: GET, PUT, REMOVE, and mixed workloads
- **Metrics**: Average time per operation (microseconds)
- **Use case**: Understanding raw performance characteristics

### 2. Concurrent Operations (`ConcurrentBenchmark`)
- **Purpose**: Multi-threaded performance under various load patterns
- **Workloads**:
  - Read-heavy (90% reads, 10% writes)
  - Write-heavy (30% reads, 70% writes)
  - High contention scenarios
- **Metrics**: Operations per second under concurrent load
- **Use case**: Real-world application scenarios

### 3. Throughput (`ThroughputBenchmark`)
- **Purpose**: Maximum sustained throughput measurements
- **Thread counts**: 1, 4, and maximum available threads
- **Workloads**: GET, PUT, and mixed operations
- **Metrics**: Operations per second for different thread counts
- **Use case**: Scalability analysis

## Prerequisites

- **Java 11+** (required for JMH)
- **Memory**: At least 4GB RAM recommended
- **Time**: 30-60 minutes for full benchmark suite

## Quick Start

### Running All Benchmarks
```bash
cd benchmarks
./run-benchmarks.sh
```

### Running Individual Benchmarks
```bash
# Basic operations only
../gradlew jmh -PjmhInclude=".*BasicOperationsBenchmark.*"

# Concurrent operations only
../gradlew jmh -PjmhInclude=".*ConcurrentBenchmark.*"

# Throughput only
../gradlew jmh -PjmhInclude=".*ThroughputBenchmark.*"
```

### Running Specific Tests
```bash
# Only JCacheX vs Caffeine GET operations
../gradlew jmh -PjmhInclude=".*jcacheXGet.*|.*caffeineGet.*"

# Only PUT operations across all libraries
../gradlew jmh -PjmhInclude=".*Put.*"
```

## Understanding Results

### JMH Output Format
```
Benchmark                               Mode  Cnt    Score   Error  Units
BasicOperationsBenchmark.jcacheXGet    avgt   10    0.234 ± 0.003  us/op
BasicOperationsBenchmark.caffeineGet   avgt   10    0.241 ± 0.004  us/op
```

- **Mode**: `avgt` (average time) or `thrpt` (throughput)
- **Cnt**: Number of measurement iterations
- **Score**: Average result
- **Error**: Confidence interval (±)
- **Units**: `us/op` (microseconds per operation) or `ops/s` (operations per second)

### Performance Interpretation

#### Latency (us/op - lower is better)
- **< 1 µs**: Excellent performance
- **1-10 µs**: Good performance
- **10-100 µs**: Moderate performance
- **> 100 µs**: Poor performance

#### Throughput (ops/s - higher is better)
- **> 1M ops/s**: Excellent throughput
- **100K-1M ops/s**: Good throughput
- **10K-100K ops/s**: Moderate throughput
- **< 10K ops/s**: Poor throughput

## Advanced Usage

### Custom JMH Parameters
```bash
../gradlew jmh \
    -PjmhInclude=".*BasicOperationsBenchmark.*" \
    -PjmhWarmupIterations=5 \
    -PjmhIterations=10 \
    -PjmhForks=3 \
    -PjmhThreads=2
```

### Profiling with JFR
```bash
../gradlew jmh -PjmhInclude=".*BasicOperationsBenchmark.*" \
    -PjmhJvmArgs="-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=benchmark.jfr"
```

### Memory Analysis
```bash
../gradlew jmh -PjmhInclude=".*BasicOperationsBenchmark.*" \
    -PjmhJvmArgs="-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log"
```

## Benchmark Configuration

The benchmarks use the following JMH configuration:

- **Warmup**: 3 iterations, 1-3 seconds each
- **Measurement**: 5 iterations, 1-5 seconds each
- **Forks**: 2 separate JVM instances
- **JVM Args**:
  - `-Xms2g -Xmx4g` (heap size)
  - `-XX:+UseG1GC` (garbage collector)
  - `-XX:+UnlockExperimentalVMOptions -XX:+UseStringDeduplication`

## Results Analysis

### Generated Files
After running benchmarks, you'll find:

- `benchmark_summary.md` - Overview and methodology
- `*_results.txt` - Detailed JMH output
- `benchmark_results.csv` - Structured data for analysis
- `extract_results.py` - Script to parse results

### Example Analysis
```bash
# View top performers for GET operations
grep "Get.*avgt" benchmark_results.csv | sort -k5 -n | head -5

# Compare throughput across libraries
grep "Throughput.*thrpt" benchmark_results.csv | sort -k5 -nr | head -10
```

## Troubleshooting

### Common Issues

**OutOfMemoryError**
```bash
# Increase heap size
export GRADLE_OPTS="-Xmx8g"
../gradlew jmh
```

**Long execution times**
```bash
# Run subset of benchmarks
../gradlew jmh -PjmhInclude=".*Get.*" -PjmhIterations=3
```

**Inconsistent results**
```bash
# Increase measurement iterations
../gradlew jmh -PjmhIterations=10 -PjmhForks=3
```

**JCache "Multiple CachingProviders" Error**
```bash
# Fixed: Updated dependencies to use only EHCache JCache implementation
# If still occurring, check classpath for conflicting JCache providers
```

**Quick Test Taking Too Long**
```bash
# Fixed: Now uses direct JMH execution instead of Gradle plugin
# Quick test should complete in ~20 seconds
./quick-test.sh
```

### Environment Considerations
- **CPU frequency scaling**: Disable for consistent results
- **Background processes**: Minimize during benchmarking
- **Thermal throttling**: Ensure adequate cooling
- **NUMA topology**: Consider CPU affinity for large systems

## Contributing

### Adding New Benchmarks
1. Create a new benchmark class extending `BaseBenchmark`
2. Add `@Benchmark` annotated methods
3. Update `run-benchmarks.sh` to include new benchmark
4. Add documentation to this README

### Benchmark Best Practices
- Use `@State` for benchmark state management
- Consume results with `Blackhole` to prevent dead code elimination
- Use appropriate `@Setup` and `@TearDown` levels
- Consider `@Param` for parameterized benchmarks

## Performance Expectations

Based on our testing, JCacheX typically demonstrates:

- **GET operations**: ~0.073 µs per operation
- **PUT operations**: ~0.113 µs per operation
- **Mixed workload**: 8-15 million operations per second
- **Concurrent read-heavy**: 10-20 million operations per second
- **Memory efficiency**: 40-60 bytes per cache entry

**Comparison (Quick Test Results):**
- **ConcurrentMap**: ~0.004 µs GET, ~0.010 µs PUT (baseline)
- **Caffeine**: ~0.014 µs GET, ~0.022 µs PUT (industry standard)
- **JCacheX**: ~0.073 µs GET, ~0.113 µs PUT (competitive performance)

*Note: Actual performance varies based on hardware, JVM version, and workload characteristics.*

## License

This benchmark suite is part of the JCacheX project and is licensed under the same terms as the main project.
