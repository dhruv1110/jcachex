#!/bin/bash

# Legacy test setup script - now integrated into unified run-benchmarks.sh
# Use run-benchmarks.sh for complete setup validation and testing

echo "==========================================="
echo "JCacheX Test Setup (Legacy)"
echo "==========================================="
echo "This script has been superseded by the unified run-benchmarks.sh"
echo ""
echo "The new unified script provides:"
echo "  - Automatic compilation validation"
echo "  - Complete setup verification"
echo "  - Temporary file cleanup"
echo "  - Both quick and full benchmark modes"
echo ""
echo "Available commands:"
echo "  ./run-benchmarks.sh --quick      # Quick validation (5-10 min)"
echo "  ./run-benchmarks.sh              # Full benchmarks (30-45 min)"
echo "  ./run-benchmarks.sh --cleanup    # Clean up temporary files"
echo "  ./run-benchmarks.sh --help       # Show usage information"
echo ""

read -p "Would you like to run setup validation now? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Running setup validation via unified script..."
    echo "This will compile, validate, and run a quick test..."
    exec ./run-benchmarks.sh --quick
else
    echo "Setup validation cancelled."
    echo ""
    echo "Manual setup verification:"
    echo "  1. Compile: ../gradlew :benchmarks:compileJmhJava"
    echo "  2. Build JAR: ../gradlew :benchmarks:jmhJar"
    echo "  3. Quick test: ./run-benchmarks.sh --quick"
fi
