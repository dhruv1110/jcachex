#!/bin/bash

# JCacheX Performance Benchmarks Runner
# This script runs benchmarks comparing JCacheX profiles against EhCache, Caffeine, and Guava Cache
# Generates clean tabular output with GET latency, PUT latency, and throughput metrics

set -e

# Parse command line arguments
QUICK_MODE=false
CLEANUP_ONLY=false

show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --quick         Run quick validation with reduced iterations"
    echo "  --cleanup       Clean up temporary files and exit"
    echo "  --help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0              # Run full benchmarks"
    echo "  $0 --quick      # Run quick validation"
    echo "  $0 --cleanup    # Clean up temporary files only"
}

while [[ $# -gt 0 ]]; do
    case $1 in
        --quick)
            QUICK_MODE=true
            shift
            ;;
        --cleanup)
            CLEANUP_ONLY=true
            shift
            ;;
        --help)
            show_usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Function to clean up temporary files
cleanup_temp_files() {
    echo "Cleaning up temporary files..."
    find . -name "jcachex-mmap-*" -type f -delete 2>/dev/null || true
    find .. -name "jcachex-mmap-*" -type f -delete 2>/dev/null || true
    rm -f quick_test_results.txt 2>/dev/null || true
    rm -f *.hprof 2>/dev/null || true
    rm -f hs_err_pid*.log 2>/dev/null || true
    echo "✓ Temporary files cleaned up"
}

# If cleanup only mode, do cleanup and exit
if [ "$CLEANUP_ONLY" = true ]; then
    cleanup_temp_files
    exit 0
fi

# Collect hardware information
collect_hardware_info() {
    echo "=== Hardware Information ==="

    # Operating System
    OS_NAME=$(uname -s)
    OS_VERSION=$(uname -r)
    ARCH=$(uname -m)

    # CPU Information
    if [[ "$OS_NAME" == "Darwin" ]]; then
        CPU_MODEL=$(sysctl -n machdep.cpu.brand_string 2>/dev/null || echo "Unknown")
        CPU_CORES=$(sysctl -n hw.ncpu 2>/dev/null || echo "Unknown")
        CPU_THREADS=$(sysctl -n hw.logicalcpu 2>/dev/null || echo "Unknown")
        MEMORY_GB=$(echo "scale=2; $(sysctl -n hw.memsize 2>/dev/null || echo "0") / 1024 / 1024 / 1024" | bc 2>/dev/null || echo "Unknown")
    elif [[ "$OS_NAME" == "Linux" ]]; then
        CPU_MODEL=$(grep "model name" /proc/cpuinfo | head -1 | cut -d':' -f2 | sed 's/^ *//' 2>/dev/null || echo "Unknown")
        CPU_CORES=$(nproc 2>/dev/null || echo "Unknown")
        CPU_THREADS=$(grep "processor" /proc/cpuinfo | wc -l 2>/dev/null || echo "Unknown")
        MEMORY_GB=$(echo "scale=2; $(grep MemTotal /proc/meminfo | awk '{print $2}' 2>/dev/null || echo "0") / 1024 / 1024" | bc 2>/dev/null || echo "Unknown")
    else
        CPU_MODEL="Unknown"
        CPU_CORES="Unknown"
        CPU_THREADS="Unknown"
        MEMORY_GB="Unknown"
    fi

    # Java Information
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 2>/dev/null || echo "Unknown")
    JAVA_VENDOR=$(java -version 2>&1 | tail -n 1 | cut -d' ' -f1-2 2>/dev/null || echo "Unknown")

    echo "OS: $OS_NAME $OS_VERSION ($ARCH)"
    echo "CPU: $CPU_MODEL"
    echo "CPU Cores: $CPU_CORES (Physical) / $CPU_THREADS (Logical)"
    echo "Memory: ${MEMORY_GB}GB"
    echo "Java: $JAVA_VERSION ($JAVA_VENDOR)"
    echo "JVM Args: -Xms2g -Xmx4g -XX:+UseG1GC"
    echo
}

# Function to run a benchmark and capture results
run_benchmark() {
    local benchmark_name=$1
    local benchmark_class=$2
    local description=$3

    echo "Running $benchmark_name..."
    echo "Description: $description"

    # Set JMH parameters based on mode
    if [ "$QUICK_MODE" = true ]; then
        WARMUP_ITERATIONS=1
        MEASUREMENT_ITERATIONS=2
        FORKS=1
        BENCHMARK_TIME=1
    else
        WARMUP_ITERATIONS=3
        MEASUREMENT_ITERATIONS=5
        FORKS=2
        BENCHMARK_TIME=2
    fi

    # Run the benchmark
    java -jar build/libs/benchmarks-*-jmh.jar ".*$benchmark_class.*" \
        -wi $WARMUP_ITERATIONS -i $MEASUREMENT_ITERATIONS -f $FORKS \
        -w ${BENCHMARK_TIME}s -r ${BENCHMARK_TIME}s \
        -tu ns -rf json -rff "$RESULTS_DIR/${benchmark_name}_results.json" \
        -jvmArgs "-Xms2g -Xmx4g -XX:+UseG1GC" \
        2>&1 | tee "$RESULTS_DIR/${benchmark_name}_results.txt"

    if [ $? -eq 0 ]; then
        echo "✓ $benchmark_name completed successfully"
    else
        echo "✗ $benchmark_name failed"
    fi
    echo
}

# Function to parse JMH JSON results and extract metrics
parse_results() {
    local results_dir=$1

    echo "Parsing benchmark results..."

    # Create Python script to parse JSON results
    cat > "$results_dir/parse_results.py" << 'EOF'
import json
import sys
import os
from collections import defaultdict

def parse_jmh_results(results_dir):
    """Parse JMH JSON results and extract key metrics"""

    # Initialize results structure
    results = defaultdict(lambda: defaultdict(dict))

    # Process each benchmark result file
    for filename in os.listdir(results_dir):
        if filename.endswith('_results.json'):
            benchmark_type = filename.replace('_results.json', '')

            try:
                with open(os.path.join(results_dir, filename), 'r') as f:
                    data = json.load(f)

                for result in data:
                    benchmark_name = result['benchmark']

                    # Safely extract and convert score and error to float
                    try:
                        score = float(result['primaryMetric']['score'])
                    except (ValueError, TypeError, KeyError):
                        score = 0.0

                    try:
                        error = float(result['primaryMetric']['scoreError'])
                    except (ValueError, TypeError, KeyError):
                        error = 0.0

                    unit = result['primaryMetric']['scoreUnit']

                    # Extract cache implementation and operation
                    parts = benchmark_name.split('.')[-1]  # Get method name

                    # Determine cache implementation
                    cache_impl = "Unknown"
                    if 'jcacheXDefault' in parts:
                        cache_impl = "JCacheX-Default"
                    elif 'jcacheXReadHeavy' in parts:
                        cache_impl = "JCacheX-ReadHeavy"
                    elif 'jcacheXWriteHeavy' in parts:
                        cache_impl = "JCacheX-WriteHeavy"
                    elif 'jcacheXMemoryEfficient' in parts:
                        cache_impl = "JCacheX-MemoryEfficient"
                    elif 'jcacheXHighPerformance' in parts:
                        cache_impl = "JCacheX-HighPerformance"
                    elif 'jcacheXSessionCache' in parts:
                        cache_impl = "JCacheX-SessionCache"
                    elif 'jcacheXApiCache' in parts:
                        cache_impl = "JCacheX-ApiCache"
                    elif 'jcacheXComputeCache' in parts:
                        cache_impl = "JCacheX-ComputeCache"
                    elif 'jcacheXMlOptimized' in parts:
                        cache_impl = "JCacheX-MlOptimized"
                    elif 'jcacheXZeroCopy' in parts:
                        cache_impl = "JCacheX-ZeroCopy"
                    elif 'jcacheXHardwareOptimized' in parts:
                        cache_impl = "JCacheX-HardwareOptimized"
                    elif 'jcacheXDistributed' in parts:
                        cache_impl = "JCacheX-Distributed"
                    elif 'caffeine' in parts:
                        cache_impl = "Caffeine"
                    elif 'ehcache' in parts:
                        cache_impl = "EhCache"
                    elif 'cache2k' in parts:
                        cache_impl = "Cache2k"
                    elif 'concurrentMap' in parts:
                        cache_impl = "ConcurrentHashMap"

                    # Determine operation type
                    operation = "Unknown"
                    if 'Get' in parts or 'get' in parts:
                        if 'Throughput' in parts:
                            operation = "GET_THROUGHPUT"
                        else:
                            operation = "GET_LATENCY"
                    elif 'Put' in parts or 'put' in parts:
                        if 'Throughput' in parts:
                            operation = "PUT_THROUGHPUT"
                        else:
                            operation = "PUT_LATENCY"
                    elif 'Mixed' in parts or 'mixed' in parts:
                        operation = "MIXED_THROUGHPUT"

                    # Store result
                    results[cache_impl][operation] = {
                        'score': score,
                        'error': error,
                        'unit': unit,
                        'benchmark_type': benchmark_type
                    }

            except Exception as e:
                print(f"Error parsing {filename}: {e}", file=sys.stderr)

    return results

def generate_table(results, test_config):
    """Generate formatted table output"""

        # Define cache implementations in order - All 12 JCacheX Profiles + Industry Leaders
    cache_implementations = [
        # JCacheX Core Profiles (5)
        "JCacheX-Default",
        "JCacheX-ReadHeavy",
        "JCacheX-WriteHeavy",
        "JCacheX-MemoryEfficient",
        "JCacheX-HighPerformance",
        # JCacheX Specialized Profiles (3)
        "JCacheX-SessionCache",
        "JCacheX-ApiCache",
        "JCacheX-ComputeCache",
        # JCacheX Advanced Profiles (4)
        "JCacheX-MlOptimized",
        "JCacheX-ZeroCopy",
        "JCacheX-HardwareOptimized",
        "JCacheX-Distributed",
        # Industry-leading implementations
        "Caffeine",
        "EhCache",
        "Cache2k",
        "ConcurrentHashMap"
    ]

    # Generate hardware info header
    print("=" * 80)
    print("JCacheX Performance Benchmark Results")
    print("=" * 80)
    print()

    for key, value in test_config.items():
        print(f"{key}: {value}")
    print()

    # Generate main performance table
    print("Performance Comparison Table")
    print("-" * 80)
    print(f"{'Cache Implementation':<25} {'GET Latency (ns)':<18} {'PUT Latency (ns)':<18} {'Throughput (ops/s)':<18}")
    print("-" * 80)

    for cache_impl in cache_implementations:
        if cache_impl in results:
            cache_results = results[cache_impl]

            # Get metrics with fallback
            get_latency = cache_results.get('GET_LATENCY', {}).get('score', 'N/A')
            put_latency = cache_results.get('PUT_LATENCY', {}).get('score', 'N/A')
            throughput = cache_results.get('GET_THROUGHPUT', {}).get('score', 'N/A')

            # Format numbers
            get_str = f"{get_latency:.2f}" if isinstance(get_latency, (int, float)) else str(get_latency)
            put_str = f"{put_latency:.2f}" if isinstance(put_latency, (int, float)) else str(put_latency)
            throughput_str = f"{throughput:.0f}" if isinstance(throughput, (int, float)) else str(throughput)

            print(f"{cache_impl:<25} {get_str:<18} {put_str:<18} {throughput_str:<18}")
        else:
            print(f"{cache_impl:<25} {'N/A':<18} {'N/A':<18} {'N/A':<18}")

    print("-" * 80)
    print()

    # Generate detailed metrics table
    print("Detailed Metrics by Benchmark Type")
    print("-" * 80)

    for cache_impl in cache_implementations:
        if cache_impl in results:
            print(f"\n{cache_impl}:")
            cache_results = results[cache_impl]

            for operation, data in cache_results.items():
                score = data['score']
                error = data['error']
                unit = data['unit']
                benchmark_type = data['benchmark_type']

                # Safely format score and error
                try:
                    score_str = f"{float(score):>10.2f}"
                except (ValueError, TypeError):
                    score_str = f"{str(score):>10}"

                try:
                    error_str = f"{float(error):>8.2f}"
                except (ValueError, TypeError):
                    error_str = f"{str(error):>8}"

                print(f"  {operation:<20} {score_str} ± {error_str} {unit} [{benchmark_type}]")

    print()

def main():
    if len(sys.argv) != 2:
        print("Usage: python parse_results.py <results_directory>")
        sys.exit(1)

    results_dir = sys.argv[1]

    # Parse results
    results = parse_jmh_results(results_dir)

    # Read test configuration
    test_config = {}
    config_file = os.path.join(results_dir, 'test_config.txt')
    if os.path.exists(config_file):
        with open(config_file, 'r') as f:
            for line in f:
                if ':' in line:
                    key, value = line.strip().split(':', 1)
                    test_config[key.strip()] = value.strip()

    # Generate table
    generate_table(results, test_config)

if __name__ == '__main__':
    main()
EOF

    # Run the parser
    python3 "$results_dir/parse_results.py" "$results_dir"
}

# Main execution
echo "=================================================="
if [ "$QUICK_MODE" = true ]; then
    echo "JCacheX Quick Performance Benchmark"
    echo "Sample Size: Reduced iterations (1 warmup, 2 measurement, 1 fork)"
else
    echo "JCacheX Comprehensive Performance Benchmark"
    echo "Sample Size: Full iterations (3 warmup, 5 measurement, 2 forks)"
fi
echo "=================================================="
echo

# Check Java availability
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "Error: Java 11 or higher is required"
    exit 1
fi

# Collect and display hardware information
collect_hardware_info

# Clean up any existing temporary files
cleanup_temp_files

# Build the benchmark JAR
echo "Building benchmark JAR..."
../gradlew :benchmarks:jmhJar -q
if [ $? -ne 0 ]; then
    echo "✗ Build failed. Please check compilation errors."
    cleanup_temp_files
    exit 1
fi

# Create results directory
if [ "$QUICK_MODE" = true ]; then
    RESULTS_DIR="benchmark-results/quick-$(date +%Y%m%d_%H%M%S)"
else
    RESULTS_DIR="benchmark-results/full-$(date +%Y%m%d_%H%M%S)"
fi
mkdir -p "$RESULTS_DIR"

# Save test configuration
cat > "$RESULTS_DIR/test_config.txt" << EOF
Date: $(date)
Test Mode: $(if [ "$QUICK_MODE" = true ]; then echo "Quick Validation"; else echo "Comprehensive Benchmark"; fi)
Sample Size: $(if [ "$QUICK_MODE" = true ]; then echo "1 warmup, 2 measurement, 1 fork"; else echo "3 warmup, 5 measurement, 2 forks"; fi)
OS: $OS_NAME $OS_VERSION ($ARCH)
CPU: $CPU_MODEL
CPU Cores: $CPU_CORES (Physical) / $CPU_THREADS (Logical)
- **Memory:** ${MEMORY_GB}GB
- **Java:** $JAVA_VERSION ($JAVA_VENDOR)
JVM Args: -Xms2g -Xmx4g -XX:+UseG1GC
EOF

echo "Results will be saved to: $RESULTS_DIR"
echo

# Run the three key benchmarks
echo "Running benchmarks..."
echo

# 1. Basic Operations Benchmark
run_benchmark "basic_operations" "BasicOperationsBenchmark" \
    "Single-threaded performance for fundamental cache operations (GET, PUT, REMOVE)"

# 2. Concurrent Operations Benchmark
run_benchmark "concurrent_operations" "ConcurrentBenchmark" \
    "Multi-threaded performance under concurrent load scenarios"

# 3. Throughput Benchmark
run_benchmark "throughput" "ThroughputBenchmark" \
    "Maximum sustained throughput measurements for different thread counts"

# Parse results and generate table
echo "Generating performance summary table..."
parse_results "$RESULTS_DIR"

# Generate summary report
cat > "$RESULTS_DIR/benchmark_summary.md" << EOF
# JCacheX Performance Benchmark Summary

## Test Configuration
- **Date:** $(date)
- **Test Mode:** $(if [ "$QUICK_MODE" = true ]; then echo "Quick Validation"; else echo "Comprehensive Benchmark"; fi)
- **Sample Size:** $(if [ "$QUICK_MODE" = true ]; then echo "1 warmup, 2 measurement, 1 fork"; else echo "3 warmup, 5 measurement, 2 forks"; fi)

## Hardware Information
- **OS:** $OS_NAME $OS_VERSION ($ARCH)
- **CPU:** $CPU_MODEL
- **CPU Cores:** $CPU_CORES (Physical) / $CPU_THREADS (Logical)
- **Memory:** ${MEMORY_GB}GB
- **Java:** $JAVA_VERSION ($JAVA_VENDOR)

## Benchmarks Executed
1. **Basic Operations:** Single-threaded GET, PUT, REMOVE operations
2. **Concurrent Operations:** Multi-threaded performance under load
3. **Throughput:** Maximum sustained throughput measurements

## Cache Implementations Tested

### JCacheX Cache Profiles (12 total)
**Core Profiles (5):**
- **JCacheX-Default:** General-purpose balanced cache
- **JCacheX-ReadHeavy:** Optimized for read-intensive workloads (80%+ reads)
- **JCacheX-WriteHeavy:** Optimized for write-intensive workloads (50%+ writes)
- **JCacheX-MemoryEfficient:** Minimized memory usage for constrained environments
- **JCacheX-HighPerformance:** Maximum throughput optimization

**Specialized Profiles (3):**
- **JCacheX-SessionCache:** Optimized for user session storage with time-based expiration
- **JCacheX-ApiCache:** Optimized for API response caching with short TTL
- **JCacheX-ComputeCache:** Optimized for expensive computation results

**Advanced Profiles (4):**
- **JCacheX-MlOptimized:** Machine learning optimized with predictive capabilities
- **JCacheX-ZeroCopy:** Zero-copy optimized for minimal memory allocation
- **JCacheX-HardwareOptimized:** Hardware-optimized leveraging CPU-specific features
- **JCacheX-Distributed:** Distributed cache optimized for cluster environments

### Industry-Leading Implementations
- **Caffeine:** Industry-standard high-performance cache
- **EhCache:** Enterprise caching solution
- **Cache2k:** High-performance Java cache
- **ConcurrentHashMap:** JDK baseline comparison

## Results
See detailed performance table in the console output above.

## Files Generated
- \`basic_operations_results.txt\` - Basic operations benchmark output
- \`concurrent_operations_results.txt\` - Concurrent operations benchmark output
- \`throughput_results.txt\` - Throughput benchmark output
- \`*.json\` - Machine-readable JMH results
- \`parse_results.py\` - Results parsing script
- \`test_config.txt\` - Test configuration details

## Usage
To rerun benchmarks:
- Full benchmark: \`./run-benchmarks.sh\`
- Quick validation: \`./run-benchmarks.sh --quick\`
- Cleanup: \`./run-benchmarks.sh --cleanup\`
EOF

echo "✓ Summary report generated: $RESULTS_DIR/benchmark_summary.md"

# Clean up temporary files
cleanup_temp_files

echo
echo "=================================================="
echo "JCacheX Performance Benchmark Complete!"
echo "=================================================="
echo
echo "Results directory: $RESULTS_DIR"
echo "Key files:"
echo "- benchmark_summary.md: Complete benchmark summary"
echo "- test_config.txt: Test configuration details"
echo "- parse_results.py: Results parsing script"
echo "- *_results.txt: Detailed benchmark outputs"
echo "- *_results.json: Machine-readable results"
echo

if [ "$QUICK_MODE" = true ]; then
    echo "Status: Quick performance validation complete!"
    echo "To run full comprehensive benchmarks: ./run-benchmarks.sh"
else
    echo "Status: Comprehensive performance benchmark complete!"
    echo "For quick validation: ./run-benchmarks.sh --quick"
fi

echo
echo "The performance table above shows:"
echo "- All 12 JCacheX cache profiles + 4 industry implementations as rows"
echo "- GET latency, PUT latency, and throughput as columns"
echo "- Hardware details and sample size information"
echo "- Complete comparison: Core, Specialized, and Advanced JCacheX profiles vs Caffeine, EhCache, Cache2k, and ConcurrentHashMap"
