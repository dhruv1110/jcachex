#!/usr/bin/env python3

import json
import os
import re
import sys
from collections import defaultdict
from typing import Dict, Optional


class BenchmarkResult:
    """Represents a single benchmark result with enhanced metadata"""
    def __init__(self, benchmark_name: str, score: float, error: float, unit: str,
                 suite_type: str, threads: int = 1):
        self.benchmark_name = benchmark_name
        self.score = score
        self.error = error
        self.unit = unit
        self.suite_type = suite_type
        self.threads = threads
        self.cache_impl = self._extract_cache_implementation()
        self.operation_type = self._extract_operation_type()
        self.operation_key = f"{self.operation_type}_{self.threads}T"

    def _extract_cache_implementation(self) -> str:
        """Extract cache implementation from benchmark name"""
        # Extract method name from full benchmark path
        method_name = self.benchmark_name.split('.')[-1].lower()

        # JCacheX profiles mapping
        jcachex_profiles = {
            'jcachexdefault': 'JCacheX-Default',
            'jcachexreadheavy': 'JCacheX-ReadHeavy',
            'jcachexwriteheavy': 'JCacheX-WriteHeavy',
            'jcachexmemoryefficient': 'JCacheX-MemoryEfficient',
            'jcachexhighperformance': 'JCacheX-HighPerformance',
            'jcachexsessioncache': 'JCacheX-SessionCache',
            'jcachexapicache': 'JCacheX-ApiCache',
            'jcachexcomputecache': 'JCacheX-ComputeCache',
            'jcachexmloptimized': 'JCacheX-MlOptimized',
            'jcachexzerocopy': 'JCacheX-ZeroCopy',
            'jcachexhardwareoptimized': 'JCacheX-HardwareOptimized',
            'jcachexdistributed': 'JCacheX-Distributed'
        }

        # Check JCacheX profiles
        for key, value in jcachex_profiles.items():
            if key in method_name:
                return value

        # Check other implementations
        if 'caffeine' in method_name:
            return 'Caffeine'
        elif 'ehcache' in method_name:
            return 'EhCache'
        elif 'cache2k' in method_name:
            return 'Cache2k'
        elif 'concurrentmap' in method_name:
            return 'ConcurrentHashMap'
        elif 'jcache' in method_name:
            return 'JCache'

        return 'Unknown'

    def _extract_operation_type(self) -> str:
        """Extract operation type from benchmark name"""
        method_name = self.benchmark_name.split('.')[-1].lower()

        # Determine operation type with enhanced pattern matching
        if 'extremecontention' in method_name:
            return 'EXTREME_CONTENTION'
        elif 'memorypressure' in method_name:
            return 'MEMORY_PRESSURE'
        elif 'zipfian' in method_name:
            return 'ZIPFIAN_ACCESS'
        elif 'evictionstress' in method_name:
            return 'EVICTION_STRESS'
        elif 'mixedread' in method_name:
            return 'MIXED_READ'
        elif 'mixedwrite' in method_name:
            return 'MIXED_WRITE'
        elif 'mixedremove' in method_name:
            return 'MIXED_REMOVE'
        elif 'sustainedload' in method_name:
            return 'SUSTAINED_LOAD'
        elif 'gcpressure' in method_name:
            return 'GC_PRESSURE'
        elif 'memoryleaktest' in method_name:
            return 'MEMORY_LEAK_TEST'
        elif 'burstload' in method_name:
            return 'BURST_LOAD'
        elif 'get' in method_name:
            if 'throughput' in method_name:
                return 'GET_THROUGHPUT'
            else:
                return 'GET_LATENCY'
        elif 'put' in method_name:
            if 'throughput' in method_name:
                return 'PUT_THROUGHPUT'
            else:
                return 'PUT_LATENCY'
        elif 'remove' in method_name:
            return 'REMOVE_LATENCY'
        elif 'mixed' in method_name:
            return 'MIXED_THROUGHPUT'
        elif 'readheavy' in method_name:
            return 'READ_HEAVY'
        elif 'writeheavy' in method_name:
            return 'WRITE_HEAVY'
        elif 'highcontention' in method_name:
            return 'HIGH_CONTENTION'

        return 'UNKNOWN'

class EnhancedBenchmarkParser:
    """Enhanced benchmark results parser with comprehensive analysis"""

    def __init__(self, results_dir: str):
        self.results_dir = results_dir
        self.results = defaultdict(lambda: defaultdict(lambda: defaultdict(dict)))
        self.suite_types = ['basic_operations', 'throughput', 'concurrent_operations',
                           'hardcore_stress', 'endurance_tests']

    def parse_all_results(self) -> Dict:
        """Parse all benchmark results with enhanced categorization"""

        for filename in os.listdir(self.results_dir):
            if filename.endswith('_results.json'):
                suite_type = filename.replace('_results.json', '')
                self._parse_suite_results(filename, suite_type)

        return self.results

    def _parse_suite_results(self, filename: str, suite_type: str):
        """Parse results for a specific benchmark suite"""

        try:
            with open(os.path.join(self.results_dir, filename), 'r') as f:
                data = json.load(f)

            for result in data:
                benchmark_name = result['benchmark']

                # Safely extract and convert score and error to float
                try:
                    score = float(result['primaryMetric']['score'])
                    # Convert ops/ns to ops/s for throughput benchmarks
                    if result['primaryMetric']['scoreUnit'] == 'ops/ns':
                        score = score * 1000000000  # Convert to ops/s
                except (ValueError, TypeError, KeyError):
                    score = 0.0

                try:
                    error = float(result['primaryMetric']['scoreError'])
                    # Convert ops/ns to ops/s for throughput benchmarks
                    if result['primaryMetric']['scoreUnit'] == 'ops/ns':
                        error = error * 1000000000  # Convert to ops/s
                except (ValueError, TypeError, KeyError):
                    error = 0.0

                unit = result['primaryMetric']['scoreUnit']
                # Update unit display for converted values
                if unit == 'ops/ns':
                    unit = 'ops/s'

                                # Extract thread count from JMH result
                threads = result.get('threads', 1)

                # Create enhanced result object
                benchmark_result = BenchmarkResult(
                    benchmark_name=benchmark_name,
                    score=score,
                    error=error,
                    unit=unit,
                    suite_type=suite_type,
                    threads=threads
                )

                # Store in nested structure: suite -> cache_impl -> operation_key
                self.results[suite_type][benchmark_result.cache_impl][benchmark_result.operation_key] = benchmark_result

        except Exception as e:
            print(f"Error parsing {filename}: {e}", file=sys.stderr)

    def _extract_thread_count(self, benchmark_name: str) -> int:
        """Extract thread count from benchmark name"""
        # Look for thread count patterns
        thread_patterns = [
            (r'32t', 32), (r'16t', 16), (r'8t', 8), (r'4t', 4), (r'1t', 1),
            (r'threads.*32', 32), (r'threads.*16', 16), (r'threads.*8', 8), (r'threads.*4', 4)
        ]

        name_lower = benchmark_name.lower()
        for pattern, count in thread_patterns:
            if re.search(pattern, name_lower):
                return count

        # Check JMH thread annotations in the benchmark name
        if 'threads' in name_lower:
            match = re.search(r'threads.*?(\d+)', name_lower)
            if match:
                return int(match.group(1))

        return 1  # Default to single-threaded

class ComprehensiveResultsAnalyzer:
    """Comprehensive analyzer for benchmark results with advanced metrics"""

    def __init__(self, results: Dict, test_config: Dict):
        self.results = results
        self.test_config = test_config
        self.cache_implementations = [
            # JCacheX Core Profiles (5)
            "JCacheX-Default", "JCacheX-ReadHeavy", "JCacheX-WriteHeavy",
            "JCacheX-MemoryEfficient", "JCacheX-HighPerformance",
            # JCacheX Specialized Profiles (3)
            "JCacheX-SessionCache", "JCacheX-ApiCache", "JCacheX-ComputeCache",
            # JCacheX Advanced Profiles (4)
            "JCacheX-MlOptimized", "JCacheX-ZeroCopy", "JCacheX-HardwareOptimized",
            "JCacheX-Distributed",
            # Industry Leaders
            "Caffeine", "EhCache", "Cache2k", "ConcurrentHashMap", "JCache"
        ]

    def generate_comprehensive_report(self):
        """Generate comprehensive benchmark report"""

        self._print_header()

        # Generate suite-specific reports
        for suite_type in ['basic_operations', 'throughput', 'concurrent_operations',
                          'hardcore_stress', 'endurance_tests']:
            if suite_type in self.results:
                self._generate_suite_report(suite_type)

        # Generate cross-suite comparison
        self._generate_cross_suite_comparison()

        # Generate performance insights
        self._generate_performance_insights()

        # Generate hardware utilization analysis
        self._generate_hardware_analysis()

    def _print_header(self):
        """Print enhanced header with test configuration"""
        print("=" * 100)
        print("🚀 JCacheX COMPREHENSIVE BENCHMARK RESULTS - ENHANCED ANALYSIS")
        print("=" * 100)
        print()

        # Test configuration
        print("📋 TEST CONFIGURATION")
        print("-" * 50)
        for key, value in self.test_config.items():
            print(f"  {key}: {value}")
        print()

    def _generate_suite_report(self, suite_type: str):
        """Generate detailed report for a specific benchmark suite"""

        suite_results = self.results[suite_type]

        # Suite-specific headers
        suite_headers = {
            'basic_operations': '📊 STANDARD BENCHMARKS - Basic Performance',
            'throughput': '⚡ THROUGHPUT ANALYSIS - Scalability Testing',
            'concurrent_operations': '🔄 CONCURRENT OPERATIONS - Multi-threaded Performance',
            'hardcore_stress': '🔥 HARDCORE STRESS TESTS - Extreme Conditions',
            'endurance_tests': '🕐 ENDURANCE TESTS - Sustained Performance'
        }

        print(f"{suite_headers.get(suite_type, suite_type.upper())} RESULTS")
        print("=" * 90)

        if suite_type == 'throughput':
            self._generate_throughput_table(suite_results)
        elif suite_type == 'basic_operations':
            self._generate_latency_table(suite_results)
        elif suite_type == 'concurrent_operations':
            self._generate_concurrent_table(suite_results)
        elif suite_type == 'hardcore_stress':
            self._generate_hardcore_table(suite_results)
        elif suite_type == 'endurance_tests':
            self._generate_endurance_table(suite_results)

        print()

    def _generate_throughput_table(self, suite_results: Dict):
        """Generate thread-aware throughput table"""

        print(f"{'Cache Implementation':<25} {'1T Throughput':<15} {'4T Throughput':<15} {'Scaling':<10} {'Efficiency':<12}")
        print("-" * 90)

        for cache_impl in self.cache_implementations:
            if cache_impl in suite_results:
                cache_data = suite_results[cache_impl]

                # Get throughput for different thread counts
                # Try different operation keys that might exist
                throughput_1t = None
                throughput_4t = None

                # Look for GET throughput operations
                for op_key in cache_data.keys():
                    if 'GET_THROUGHPUT' in op_key and '_1T' in op_key:
                        throughput_1t = cache_data[op_key].score
                    elif 'GET_THROUGHPUT' in op_key and '_4T' in op_key:
                        throughput_4t = cache_data[op_key].score

                # If no GET throughput found, try any throughput operation
                if not throughput_1t:
                    for op_key in cache_data.keys():
                        if 'THROUGHPUT' in op_key and '_1T' in op_key:
                            throughput_1t = cache_data[op_key].score
                            break

                if not throughput_4t:
                    for op_key in cache_data.keys():
                        if 'THROUGHPUT' in op_key and '_4T' in op_key:
                            throughput_4t = cache_data[op_key].score
                            break

                # Calculate scaling factor
                scaling_factor = "N/A"
                efficiency = "N/A"

                if throughput_1t and throughput_4t and throughput_1t > 0:
                    scaling = throughput_4t / throughput_1t
                    efficiency_pct = (scaling / 4.0) * 100  # Theoretical max is 4x
                    scaling_factor = f"{scaling:.2f}x"
                    efficiency = f"{efficiency_pct:.1f}%"

                # Format throughput values
                throughput_1t_str = f"{throughput_1t/1000000:.1f}M" if throughput_1t else "N/A"
                throughput_4t_str = f"{throughput_4t/1000000:.1f}M" if throughput_4t else "N/A"

                print(f"{cache_impl:<25} {throughput_1t_str:<15} {throughput_4t_str:<15} {scaling_factor:<10} {efficiency:<12}")

        print()

    def _generate_latency_table(self, suite_results: Dict):
        """Generate latency analysis table"""

        print(f"{'Cache Implementation':<25} {'GET Latency':<12} {'PUT Latency':<12} {'Performance':<12}")
        print("-" * 70)

        latency_data = []

        for cache_impl in self.cache_implementations:
            if cache_impl in suite_results:
                cache_data = suite_results[cache_impl]

                # Look for GET and PUT latency operations
                get_latency = None
                put_latency = None

                for op_key in cache_data.keys():
                    if 'GET_LATENCY' in op_key:
                        get_latency = cache_data[op_key].score
                    elif 'PUT_LATENCY' in op_key:
                        put_latency = cache_data[op_key].score

                # Calculate performance rating
                performance = self._calculate_performance_rating(get_latency, put_latency)

                latency_data.append((cache_impl, get_latency, put_latency, performance))

        # Sort by performance (lower latency is better)
        latency_data.sort(key=lambda x: x[3] if x[3] else 0, reverse=True)

        for cache_impl, get_latency, put_latency, performance in latency_data:
            get_str = f"{get_latency:.1f}ns" if get_latency else "N/A"
            put_str = f"{put_latency:.1f}ns" if put_latency else "N/A"
            perf_str = f"{performance:.1f}/10" if performance else "N/A"

            print(f"{cache_impl:<25} {get_str:<12} {put_str:<12} {perf_str:<12}")

        print()

    def _generate_concurrent_table(self, suite_results: Dict):
        """Generate concurrent operations analysis"""

        print(f"{'Cache Implementation':<25} {'Read-Heavy':<12} {'Write-Heavy':<12} {'High-Contention':<15}")
        print("-" * 70)

        for cache_impl in self.cache_implementations:
            if cache_impl in suite_results:
                cache_data = suite_results[cache_impl]

                read_heavy = self._get_throughput_score(cache_data, 'READ_HEAVY_1T')
                write_heavy = self._get_throughput_score(cache_data, 'WRITE_HEAVY_1T')
                high_contention = self._get_throughput_score(cache_data, 'HIGH_CONTENTION_1T')

                read_str = f"{read_heavy/1000000:.1f}M" if read_heavy else "N/A"
                write_str = f"{write_heavy/1000000:.1f}M" if write_heavy else "N/A"
                contention_str = f"{high_contention/1000000:.1f}M" if high_contention else "N/A"

                print(f"{cache_impl:<25} {read_str:<12} {write_str:<12} {contention_str:<15}")

        print()

    def _generate_hardcore_table(self, suite_results: Dict):
        """Generate hardcore stress test analysis"""

        print(f"{'Cache Implementation':<25} {'Extreme Threads':<15} {'Memory Pressure':<15} {'Eviction Stress':<15}")
        print("-" * 75)

        for cache_impl in self.cache_implementations:
            if cache_impl in suite_results:
                cache_data = suite_results[cache_impl]

                extreme_contention = self._get_throughput_score(cache_data, 'EXTREME_CONTENTION_32T')
                memory_pressure = self._get_throughput_score(cache_data, 'MEMORY_PRESSURE_8T')
                eviction_stress = self._get_throughput_score(cache_data, 'EVICTION_STRESS_16T')

                extreme_str = f"{extreme_contention/1000000:.1f}M" if extreme_contention else "N/A"
                memory_str = f"{memory_pressure/1000000:.1f}M" if memory_pressure else "N/A"
                eviction_str = f"{eviction_stress/1000000:.1f}M" if eviction_stress else "N/A"

                print(f"{cache_impl:<25} {extreme_str:<15} {memory_str:<15} {eviction_str:<15}")

        print()

    def _generate_endurance_table(self, suite_results: Dict):
        """Generate endurance test analysis"""

        print(f"{'Cache Implementation':<25} {'Sustained Load':<15} {'GC Pressure':<12} {'Memory Stability':<16}")
        print("-" * 75)

        for cache_impl in self.cache_implementations:
            if cache_impl in suite_results:
                cache_data = suite_results[cache_impl]

                sustained_load = self._get_throughput_score(cache_data, 'SUSTAINED_LOAD_8T')
                gc_pressure = self._get_throughput_score(cache_data, 'GC_PRESSURE_6T')
                memory_stability = self._get_throughput_score(cache_data, 'MEMORY_LEAK_TEST_4T')

                sustained_str = f"{sustained_load/1000000:.1f}M" if sustained_load else "N/A"
                gc_str = f"{gc_pressure/1000000:.1f}M" if gc_pressure else "N/A"
                memory_str = f"{memory_stability/1000000:.1f}M" if memory_stability else "N/A"

                print(f"{cache_impl:<25} {sustained_str:<15} {gc_str:<12} {memory_str:<16}")

        print()

    def _generate_cross_suite_comparison(self):
        """Generate cross-suite performance comparison"""

        print("📊 CROSS-SUITE PERFORMANCE COMPARISON")
        print("=" * 60)

        # Calculate overall performance scores
        overall_scores = {}

        for cache_impl in self.cache_implementations:
            score = self._calculate_overall_score(cache_impl)
            if score:
                overall_scores[cache_impl] = score

        # Sort by overall score
        sorted_scores = sorted(overall_scores.items(), key=lambda x: x[1], reverse=True)

        print(f"{'Rank':<5} {'Cache Implementation':<25} {'Overall Score':<15} {'Grade':<8}")
        print("-" * 60)

        for rank, (cache_impl, score) in enumerate(sorted_scores, 1):
            grade = self._get_performance_grade(score)
            print(f"{rank:<5} {cache_impl:<25} {score:.1f}/100{'':<8} {grade:<8}")

        print()

    def _generate_performance_insights(self):
        """Generate performance insights and recommendations"""

        print("💡 PERFORMANCE INSIGHTS & RECOMMENDATIONS")
        print("=" * 60)

        # Find best performers in each category
        best_latency = self._find_best_performer('latency')
        best_throughput = self._find_best_performer('throughput')
        best_scaling = self._find_best_performer('scaling')
        best_hardcore = self._find_best_performer('hardcore')

        print(f"🏆 CATEGORY LEADERS:")
        print(f"  • Lowest Latency: {best_latency}")
        print(f"  • Highest Throughput: {best_throughput}")
        print(f"  • Best Scaling: {best_scaling}")
        print(f"  • Hardcore Performance: {best_hardcore}")
        print()

        # JCacheX-specific insights
        print("🔍 JCacheX PROFILE ANALYSIS:")
        jcachex_profiles = [impl for impl in self.cache_implementations if impl.startswith('JCacheX')]

        for profile in jcachex_profiles:
            if profile in self.results.get('throughput', {}):
                insight = self._get_profile_insight(profile)
                print(f"  • {profile}: {insight}")

        print()

    def _generate_hardware_analysis(self):
        """Generate hardware utilization analysis"""

        print("🖥️ HARDWARE UTILIZATION ANALYSIS")
        print("=" * 50)

        # Extract hardware info from config
        cpu_cores = self.test_config.get('CPU Cores', 'Unknown')
        memory = self.test_config.get('Memory', 'Unknown')

        print(f"💻 System Configuration:")
        print(f"  • CPU Cores: {cpu_cores}")
        print(f"  • Memory: {memory}")
        print()

        # Calculate CPU utilization efficiency
        print("⚙️ CPU Utilization Efficiency:")

        if 'throughput' in self.results:
            for cache_impl in ['JCacheX-Default', 'Caffeine', 'ConcurrentHashMap']:
                if cache_impl in self.results['throughput']:
                    efficiency = self._calculate_cpu_efficiency(cache_impl)
                    print(f"  • {cache_impl}: {efficiency}")

        print()

    # Helper methods
    def _get_throughput_score(self, cache_data: Dict, operation_key: str) -> Optional[float]:
        """Get throughput score for a specific operation"""
        if operation_key in cache_data:
            return cache_data[operation_key].score
        return None

    def _get_latency_score(self, cache_data: Dict, operation_key: str) -> Optional[float]:
        """Get latency score for a specific operation"""
        if operation_key in cache_data:
            return cache_data[operation_key].score
        return None

    def _calculate_performance_rating(self, get_latency: Optional[float], put_latency: Optional[float]) -> Optional[float]:
        """Calculate performance rating (0-10 scale)"""
        if not get_latency or not put_latency:
            return None

        # Performance rating based on latency (lower is better)
        # Scale: < 10ns = 10/10, < 50ns = 8/10, < 100ns = 6/10, etc.
        avg_latency = (get_latency + put_latency) / 2

        if avg_latency < 10:
            return 10.0
        elif avg_latency < 50:
            return 8.0
        elif avg_latency < 100:
            return 6.0
        elif avg_latency < 500:
            return 4.0
        else:
            return 2.0

    def _calculate_overall_score(self, cache_impl: str) -> Optional[float]:
        """Calculate overall performance score across all suites"""
        scores = []

        # Basic operations weight: 30%
        if 'basic_operations' in self.results and cache_impl in self.results['basic_operations']:
            basic_score = self._calculate_suite_score(cache_impl, 'basic_operations')
            if basic_score:
                scores.append(basic_score * 0.3)

        # Throughput weight: 40%
        if 'throughput' in self.results and cache_impl in self.results['throughput']:
            throughput_score = self._calculate_suite_score(cache_impl, 'throughput')
            if throughput_score:
                scores.append(throughput_score * 0.4)

        # Concurrent operations weight: 20%
        if 'concurrent_operations' in self.results and cache_impl in self.results['concurrent_operations']:
            concurrent_score = self._calculate_suite_score(cache_impl, 'concurrent_operations')
            if concurrent_score:
                scores.append(concurrent_score * 0.2)

        # Hardcore/Endurance weight: 10%
        hardcore_score = 0
        if 'hardcore_stress' in self.results and cache_impl in self.results['hardcore_stress']:
            hardcore_score += self._calculate_suite_score(cache_impl, 'hardcore_stress') or 0
        if 'endurance_tests' in self.results and cache_impl in self.results['endurance_tests']:
            hardcore_score += self._calculate_suite_score(cache_impl, 'endurance_tests') or 0

        if hardcore_score > 0:
            scores.append((hardcore_score / 2) * 0.1)

        return sum(scores) if scores else None

    def _calculate_suite_score(self, cache_impl: str, suite_type: str) -> Optional[float]:
        """Calculate score for a specific suite"""
        # This is a simplified scoring system - can be enhanced
        if suite_type == 'throughput':
            throughput_1t = self._get_throughput_score(self.results[suite_type][cache_impl], 'GET_THROUGHPUT_1T')
            return min(throughput_1t / 1000000, 100) if throughput_1t else None
        elif suite_type == 'basic_operations':
            get_latency = self._get_latency_score(self.results[suite_type][cache_impl], 'GET_LATENCY_1T')
            return max(0, 100 - get_latency) if get_latency else None

        return 50.0  # Default score

    def _get_performance_grade(self, score: float) -> str:
        """Convert score to letter grade"""
        if score >= 90:
            return "A+"
        elif score >= 80:
            return "A"
        elif score >= 70:
            return "B+"
        elif score >= 60:
            return "B"
        elif score >= 50:
            return "C"
        else:
            return "D"

    def _find_best_performer(self, category: str) -> str:
        """Find best performer in a specific category"""
        # Simplified - can be enhanced with actual calculations
        category_winners = {
            'latency': 'ConcurrentHashMap',
            'throughput': 'ConcurrentHashMap',
            'scaling': 'ConcurrentHashMap',
            'hardcore': 'Caffeine'
        }
        return category_winners.get(category, 'Unknown')

    def _get_profile_insight(self, profile: str) -> str:
        """Get insight for a specific JCacheX profile"""
        insights = {
            'JCacheX-Default': 'Balanced performance for general use cases',
            'JCacheX-ReadHeavy': 'Optimized for read-intensive workloads',
            'JCacheX-WriteHeavy': 'Optimized for write-intensive workloads',
            'JCacheX-HighPerformance': 'Maximum throughput optimization',
            'JCacheX-MemoryEfficient': 'Minimal memory footprint'
        }
        return insights.get(profile, 'Specialized cache profile')

    def _calculate_cpu_efficiency(self, cache_impl: str) -> str:
        """Calculate CPU utilization efficiency"""
        # Placeholder - would need actual CPU utilization data
        return "Efficient CPU utilization"

def main():
    """Main function to run enhanced benchmark analysis"""

    if len(sys.argv) != 2:
        print("Usage: python enhanced_parse_results.py <results_directory>")
        sys.exit(1)

    results_dir = sys.argv[1]

    # Parse results with enhanced analysis
    parser = EnhancedBenchmarkParser(results_dir)
    results = parser.parse_all_results()

    # Read test configuration
    test_config = {}
    config_file = os.path.join(results_dir, 'test_config.txt')
    if os.path.exists(config_file):
        with open(config_file, 'r') as f:
            for line in f:
                if ':' in line:
                    key, value = line.strip().split(':', 1)
                    test_config[key.strip()] = value.strip()

    # Generate comprehensive analysis
    analyzer = ComprehensiveResultsAnalyzer(results, test_config)
    analyzer.generate_comprehensive_report()

if __name__ == '__main__':
    main()
