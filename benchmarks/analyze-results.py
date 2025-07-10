#!/usr/bin/env python3
"""
JCacheX Benchmark Results Analyzer
This script analyzes benchmark results and generates comparison reports.
"""

import argparse
import csv
import json
import re
import sys
from collections import defaultdict
from pathlib import Path
from typing import Dict, List, Tuple

def parse_jmh_results(file_path: Path) -> List[Dict]:
    """Parse JMH results from a text file."""
    results = []

    with open(file_path, 'r') as f:
        content = f.read()

    # Pattern to match JMH result lines
    pattern = r'(\w+\.[\w.]+)\s+(\w+)\s+(\d+)\s+([\d.]+)\s+±\s+([\d.]+)\s+(\w+/\w+)'
    matches = re.findall(pattern, content)

    for match in matches:
        benchmark, mode, cnt, score, error, unit = match
        results.append({
            'benchmark': benchmark,
            'mode': mode,
            'count': int(cnt),
            'score': float(score),
            'error': float(error),
            'unit': unit
        })

    return results

def extract_library_name(benchmark: str) -> str:
    """Extract library name from benchmark name."""
    if 'jcacheX' in benchmark:
        return 'JCacheX'
    elif 'caffeine' in benchmark:
        return 'Caffeine'
    elif 'cache2k' in benchmark:
        return 'Cache2k'
    elif 'ehcache' in benchmark:
        return 'EHCache'
    elif 'jcache' in benchmark:
        return 'JCache'
    elif 'concurrentMap' in benchmark:
        return 'ConcurrentMap'
    else:
        return 'Unknown'

def extract_operation_type(benchmark: str) -> str:
    """Extract operation type from benchmark name."""
    if 'Get' in benchmark:
        return 'GET'
    elif 'Put' in benchmark:
        return 'PUT'
    elif 'Remove' in benchmark:
        return 'REMOVE'
    elif 'Mixed' in benchmark:
        return 'MIXED'
    else:
        return 'OTHER'

def analyze_results(results: List[Dict]) -> Dict:
    """Analyze benchmark results and generate insights."""
    analysis = {
        'total_benchmarks': len(results),
        'libraries': set(),
        'operations': set(),
        'performance_by_library': defaultdict(list),
        'performance_by_operation': defaultdict(list),
        'comparisons': []
    }

    for result in results:
        library = extract_library_name(result['benchmark'])
        operation = extract_operation_type(result['benchmark'])

        analysis['libraries'].add(library)
        analysis['operations'].add(operation)

        performance_data = {
            'benchmark': result['benchmark'],
            'operation': operation,
            'score': result['score'],
            'error': result['error'],
            'unit': result['unit'],
            'mode': result['mode']
        }

        analysis['performance_by_library'][library].append(performance_data)
        analysis['performance_by_operation'][operation].append(performance_data)

    # Convert sets to lists for JSON serialization
    analysis['libraries'] = list(analysis['libraries'])
    analysis['operations'] = list(analysis['operations'])

    return analysis

def generate_comparison_report(analysis: Dict) -> str:
    """Generate a human-readable comparison report."""
    report = []
    report.append("# JCacheX Benchmark Analysis Report")
    report.append("")
    report.append(f"**Total Benchmarks:** {analysis['total_benchmarks']}")
    report.append(f"**Libraries Tested:** {', '.join(analysis['libraries'])}")
    report.append(f"**Operations Tested:** {', '.join(analysis['operations'])}")
    report.append("")

    # Performance by library
    report.append("## Performance by Library")
    report.append("")

    for library in analysis['libraries']:
        results = analysis['performance_by_library'][library]
        if not results:
            continue

        report.append(f"### {library}")
        report.append("")

        # Group by operation
        by_operation = defaultdict(list)
        for result in results:
            by_operation[result['operation']].append(result)

        for operation, op_results in by_operation.items():
            if not op_results:
                continue

            report.append(f"**{operation} Operations:**")

            # Calculate averages
            avg_score = sum(r['score'] for r in op_results) / len(op_results)
            avg_error = sum(r['error'] for r in op_results) / len(op_results)
            unit = op_results[0]['unit']

            report.append(f"- Average: {avg_score:.3f} ± {avg_error:.3f} {unit}")
            report.append(f"- Benchmarks: {len(op_results)}")
            report.append("")

    # Find best performers
    report.append("## Best Performers")
    report.append("")

    for operation in analysis['operations']:
        results = analysis['performance_by_operation'][operation]
        if not results:
            continue

        # Sort by score (lower is better for latency, higher for throughput)
        if results[0]['unit'].endswith('/op'):
            # Latency - lower is better
            best = min(results, key=lambda r: r['score'])
        else:
            # Throughput - higher is better
            best = max(results, key=lambda r: r['score'])

        library = extract_library_name(best['benchmark'])
        report.append(f"**{operation} Operations:** {library}")
        report.append(f"- Score: {best['score']:.3f} ± {best['error']:.3f} {best['unit']}")
        report.append(f"- Benchmark: {best['benchmark']}")
        report.append("")

    return "\n".join(report)

def generate_csv_report(analysis: Dict, output_file: Path):
    """Generate CSV report for further analysis."""
    with open(output_file, 'w', newline='') as csvfile:
        fieldnames = ['library', 'operation', 'benchmark', 'score', 'error', 'unit', 'mode']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

        writer.writeheader()
        for library, results in analysis['performance_by_library'].items():
            for result in results:
                writer.writerow({
                    'library': library,
                    'operation': result['operation'],
                    'benchmark': result['benchmark'],
                    'score': result['score'],
                    'error': result['error'],
                    'unit': result['unit'],
                    'mode': result['mode']
                })

def main():
    parser = argparse.ArgumentParser(description='Analyze JCacheX benchmark results')
    parser.add_argument('results_dir', help='Directory containing benchmark results')
    parser.add_argument('--output', '-o', help='Output directory for reports', default='.')
    parser.add_argument('--format', choices=['markdown', 'json', 'csv'], default='markdown')

    args = parser.parse_args()

    results_dir = Path(args.results_dir)
    if not results_dir.exists():
        print(f"Error: Results directory {results_dir} does not exist")
        sys.exit(1)

    # Find all result files
    result_files = list(results_dir.glob('*_results.txt'))
    if not result_files:
        print(f"Error: No result files found in {results_dir}")
        sys.exit(1)

    print(f"Found {len(result_files)} result files:")
    for file in result_files:
        print(f"  - {file.name}")
    print()

    # Parse all results
    all_results = []
    for file_path in result_files:
        results = parse_jmh_results(file_path)
        all_results.extend(results)
        print(f"Parsed {len(results)} results from {file_path.name}")

    if not all_results:
        print("Error: No benchmark results found in files")
        sys.exit(1)

    print(f"\nTotal results parsed: {len(all_results)}")

    # Analyze results
    analysis = analyze_results(all_results)

    # Generate reports
    output_dir = Path(args.output)
    output_dir.mkdir(exist_ok=True)

    if args.format == 'markdown':
        report = generate_comparison_report(analysis)
        output_file = output_dir / 'benchmark_analysis.md'
        with open(output_file, 'w') as f:
            f.write(report)
        print(f"Markdown report generated: {output_file}")

    elif args.format == 'json':
        # Convert defaultdict to regular dict for JSON
        analysis_json = dict(analysis)
        analysis_json['performance_by_library'] = dict(analysis_json['performance_by_library'])
        analysis_json['performance_by_operation'] = dict(analysis_json['performance_by_operation'])

        output_file = output_dir / 'benchmark_analysis.json'
        with open(output_file, 'w') as f:
            json.dump(analysis_json, f, indent=2)
        print(f"JSON report generated: {output_file}")

    elif args.format == 'csv':
        output_file = output_dir / 'benchmark_analysis.csv'
        generate_csv_report(analysis, output_file)
        print(f"CSV report generated: {output_file}")

    # Summary
    print("\n" + "="*50)
    print("BENCHMARK ANALYSIS SUMMARY")
    print("="*50)
    print(f"Libraries tested: {', '.join(analysis['libraries'])}")
    print(f"Operations tested: {', '.join(analysis['operations'])}")
    print(f"Total benchmarks: {analysis['total_benchmarks']}")

    # Show top performer for each operation
    print("\nTop Performers:")
    for operation in analysis['operations']:
        results = analysis['performance_by_operation'][operation]
        if not results:
            continue

        if results[0]['unit'].endswith('/op'):
            # Latency - lower is better
            best = min(results, key=lambda r: r['score'])
        else:
            # Throughput - higher is better
            best = max(results, key=lambda r: r['score'])

        library = extract_library_name(best['benchmark'])
        print(f"  {operation}: {library} ({best['score']:.3f} {best['unit']})")

if __name__ == '__main__':
    main()
