#!/bin/bash

# Legacy quick test script - now redirects to unified run-benchmarks.sh
# Use run-benchmarks.sh --quick for the same functionality

echo "==========================================="
echo "JCacheX Quick Test (Legacy)"
echo "==========================================="
echo "This script has been superseded by the unified run-benchmarks.sh"
echo ""
echo "For quick validation, please use:"
echo "  ./run-benchmarks.sh --quick"
echo ""
echo "For full comprehensive benchmarks:"
echo "  ./run-benchmarks.sh"
echo ""
echo "Other options:"
echo "  ./run-benchmarks.sh --cleanup   # Clean up temporary files"
echo "  ./run-benchmarks.sh --help      # Show usage information"
echo ""

read -p "Would you like to run quick validation now? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Running quick validation via unified script..."
    exec ./run-benchmarks.sh --quick
else
    echo "Quick test cancelled. Use './run-benchmarks.sh --quick' when ready."
fi
