#!/usr/bin/env python3
import re
import csv
import glob
import os

def parse_jmh_results(file_path):
    """Parse JMH results from a text file."""
    results = []
    with open(file_path, 'r') as f:
        content = f.read()

    # Pattern to match JMH result lines
    pattern = r'(\w+\.\w+)\s+(\w+)\s+(\d+)\s+([\d.]+)\s+±?\s*([\d.]*)\s+(\w+/\w+)'
    matches = re.findall(pattern, content)

    for match in matches:
        benchmark, mode, cnt, score, error, unit = match
        results.append({
            'benchmark': benchmark,
            'mode': mode,
            'count': int(cnt),
            'score': float(score),
            'error': float(error) if error else 0.0,
            'unit': unit
        })

    return results

def analyze_batch_eviction_results(all_results):
    """Extract and analyze batch eviction specific results."""
    batch_results = []

    # Look for batch eviction specific benchmarks
    batch_patterns = [
        'jcacheXBatch', 'jcacheXDefault', 'jcacheXBatchEviction',
        'OptimizedBenchmark', 'QuickBenchmark'
    ]

    for result in all_results:
        benchmark_name = result['benchmark']
        for pattern in batch_patterns:
            if pattern in benchmark_name:
                batch_results.append(result)
                break

    return batch_results

def main():
    # Find all result files
    result_files = glob.glob('*_results.txt')

    if not result_files:
        print("No result files found!")
        return

    all_results = []
    for file_path in result_files:
        benchmark_type = os.path.basename(file_path).replace('_results.txt', '')
        results = parse_jmh_results(file_path)
        for result in results:
            result['benchmark_type'] = benchmark_type
            all_results.append(result)

    # Write main results to CSV
    if all_results:
        fieldnames = ['benchmark_type', 'benchmark', 'mode', 'count', 'score', 'error', 'unit']
        with open('benchmark_results.csv', 'w', newline='') as csvfile:
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerows(all_results)

        print(f"CSV report generated: benchmark_results.csv ({len(all_results)} results)")

        # Generate batch eviction analysis
        batch_results = analyze_batch_eviction_results(all_results)
        if batch_results:
            with open('batch_eviction_analysis.csv', 'w', newline='') as csvfile:
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                writer.writeheader()
                writer.writerows(batch_results)
            print(f"Batch eviction analysis: batch_eviction_analysis.csv ({len(batch_results)} results)")
    else:
        print("No results parsed successfully!")

if __name__ == '__main__':
    main()
