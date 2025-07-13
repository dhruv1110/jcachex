#!/bin/bash

# Test script to validate benchmark infrastructure
echo "Testing JCacheX benchmark setup..."

# Check if Java is available
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

echo "Java version: $(java -version 2>&1 | head -n 1)"

# Build the benchmark JAR
echo "Building benchmark JAR..."
../gradlew :benchmarks:jmhJar -q

if [ $? -eq 0 ]; then
    echo "✓ Benchmark JAR built successfully"
else
    echo "✗ Benchmark JAR build failed"
    exit 1
fi

# Test if we can list available benchmarks
echo "Available benchmarks:"
java -jar build/libs/benchmarks-*-jmh.jar -l | head -10

echo
echo "Benchmark infrastructure is ready!"
echo "Run './run-benchmarks.sh --quick' for quick validation"
echo "Run './run-benchmarks.sh' for full benchmarks"
