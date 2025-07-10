#!/bin/bash

# Quick test script to verify benchmark setup
# This runs a minimal subset of benchmarks to ensure everything works

set -e

echo "==========================================="
echo "JCacheX Benchmark Quick Test"
echo "==========================================="
echo "This script runs a minimal benchmark to verify setup"
echo "Full benchmarks can be run with ./run-benchmarks.sh"
echo

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

echo "Java version: $(java -version 2>&1 | head -n 1)"
echo

# Run a quick benchmark test (just a few operations)
echo "Running quick benchmark test..."
echo "This will take about 2-3 minutes..."
echo

# First, ensure the benchmark jar is built
echo "Building benchmark jar..."
../gradlew :benchmarks:jmhJar -q

# Run the specialized quick benchmark directly with JMH
echo "Running QuickBenchmark tests..."
java -jar build/libs/benchmarks-*-jmh.jar ".*QuickBenchmark.*" -wi 1 -i 2 -f 1

echo
echo "✓ Quick test completed successfully!"
echo
echo "Your benchmark setup is working correctly."
echo "To run the full benchmark suite:"
echo "  ./run-benchmarks.sh"
echo
echo "To run individual benchmarks:"
echo "  ../gradlew jmh -PjmhInclude=\".*BasicOperationsBenchmark.*\""
echo "  ../gradlew jmh -PjmhInclude=\".*ConcurrentBenchmark.*\""
echo "  ../gradlew jmh -PjmhInclude=\".*ThroughputBenchmark.*\""
echo
