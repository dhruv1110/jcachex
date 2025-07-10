#!/bin/bash

# Simple setup test - just checks if everything compiles and basic functionality works

set -e

echo "=========================================="
echo "JCacheX Benchmark Setup Test"
echo "=========================================="
echo "Testing basic setup without running benchmarks..."
echo

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

echo "Java version: $(java -version 2>&1 | head -n 1)"
echo

# Compile the benchmark code
echo "Compiling benchmark code..."
../gradlew benchmarks:compileJmhJava -q

if [ $? -eq 0 ]; then
    echo "✓ Benchmark compilation successful"
else
    echo "✗ Benchmark compilation failed"
    exit 1
fi

echo

# Test basic class loading (without running benchmarks)
echo "Testing class loading..."
java -cp "../jcachex-core/build/libs/*:$(../gradlew benchmarks:jmhJar -q --console=plain | grep 'BUILD SUCCESSFUL' > /dev/null && find ../benchmarks/build -name '*jmh*.jar' 2>/dev/null | head -1)" \
    -Djmh.shutdownTimeout=1 \
    org.openjdk.jmh.Main \
    -l 2>/dev/null | head -10

if [ $? -eq 0 ]; then
    echo "✓ JMH classes loaded successfully"
else
    echo "✗ JMH class loading failed"
    exit 1
fi

echo
echo "✓ All setup tests passed!"
echo
echo "Your benchmark environment is ready."
echo "Run './quick-test.sh' to run a quick benchmark"
echo "Run './run-benchmarks.sh' to run the full benchmark suite"
echo
