// JCacheX Performance Data - Latest Benchmark Results (July 2025)
export const JCACHEX_PERFORMANCE_PROFILES = [
    {
        profile: 'JCacheX-ZeroCopy',
        description: 'Ultra-high throughput with zero-copy optimization',
        getLatency: '8.9μs',
        putLatency: '5.0μs',
        throughput: '501.1M ops/sec',
        scalingEfficiency: '98.4%',
        useCase: 'Ultra-high throughput applications',
        rating: 5,
        highlight: true
    },
    {
        profile: 'JCacheX-WriteHeavy',
        description: 'Optimized for write-intensive workloads',
        getLatency: '7.1μs',
        putLatency: '4.3μs',
        throughput: '224.6M ops/sec',
        scalingEfficiency: '97.2%',
        useCase: 'Write-intensive workloads (50%+ writes)',
        rating: 5,
        highlight: true
    },
    {
        profile: 'JCacheX-HighPerformance',
        description: 'Balanced high-performance configuration',
        getLatency: '7.2μs',
        putLatency: '4.2μs',
        throughput: '198.4M ops/sec',
        scalingEfficiency: '82.9%',
        useCase: 'Balanced high-performance needs',
        rating: 5,
        highlight: true
    },
    {
        profile: 'JCacheX-HardwareOptimized',
        description: 'CPU-specific feature optimization',
        getLatency: '14.7μs',
        putLatency: '4.6μs',
        throughput: '143.9M ops/sec',
        scalingEfficiency: '80.6%',
        useCase: 'CPU-specific optimization',
        rating: 4,
        highlight: false
    },
    {
        profile: 'JCacheX-ReadHeavy',
        description: 'Optimized for read-intensive workloads',
        getLatency: '7.4μs',
        putLatency: '16.5μs',
        throughput: '22.6M ops/sec',
        scalingEfficiency: '93.7%',
        useCase: 'Read-intensive workloads (80%+ reads)',
        rating: 4,
        highlight: false
    },
    {
        profile: 'JCacheX-MemoryEfficient',
        description: 'Memory-optimized for constrained environments',
        getLatency: '9.9μs',
        putLatency: '4.9μs',
        throughput: '31.0M ops/sec',
        scalingEfficiency: '23.7%',
        useCase: 'Memory-constrained environments',
        rating: 3,
        highlight: false
    },
    {
        profile: 'JCacheX-SessionCache',
        description: 'User session storage optimization',
        getLatency: '9.3μs',
        putLatency: '5.0μs',
        throughput: '37.3M ops/sec',
        scalingEfficiency: '24.4%',
        useCase: 'Session management',
        rating: 3,
        highlight: false
    },
    {
        profile: 'JCacheX-ApiCache',
        description: 'API response caching optimization',
        getLatency: '10.1μs',
        putLatency: '5.5μs',
        throughput: '19.0M ops/sec',
        scalingEfficiency: '16.3%',
        useCase: 'API response caching',
        rating: 3,
        highlight: false
    },
    {
        profile: 'JCacheX-ComputeCache',
        description: 'Computation result caching',
        getLatency: '9.4μs',
        putLatency: '10.9μs',
        throughput: '20.9M ops/sec',
        scalingEfficiency: '17.5%',
        useCase: 'Expensive computation results',
        rating: 3,
        highlight: false
    },
    {
        profile: 'JCacheX-Default',
        description: 'General-purpose balanced configuration',
        getLatency: '10.4μs',
        putLatency: '5.4μs',
        throughput: '19.3M ops/sec',
        scalingEfficiency: '16.7%',
        useCase: 'General-purpose caching',
        rating: 3,
        highlight: false
    }
];

// Stress test performance data
export const STRESS_TEST_PERFORMANCE = [
    {
        profile: 'JCacheX-HighPerformance',
        extremeThreads: '1,008.6M ops/sec',
        description: 'Extreme thread contention performance'
    },
    {
        profile: 'JCacheX-Default',
        extremeThreads: '541.8M ops/sec',
        memoryPressure: '0.3M ops/sec',
        evictionStress: '0.1M ops/sec',
        description: 'Multi-condition stress testing'
    }
];

// Endurance test performance data
export const ENDURANCE_TEST_PERFORMANCE = [
    {
        profile: 'JCacheX-HighPerformance',
        sustainedLoad: '20.5M ops/sec',
        description: 'Sustained performance over extended periods'
    },
    {
        profile: 'JCacheX-MemoryEfficient',
        sustainedLoad: '20.4M ops/sec',
        gcPressure: '0.7M ops/sec',
        description: 'Memory-efficient sustained performance'
    },
    {
        profile: 'JCacheX-Default',
        sustainedLoad: '19.7M ops/sec',
        gcPressure: '0.4M ops/sec',
        memoryStability: '0.4M ops/sec',
        description: 'Balanced sustained performance'
    }
];

// Performance insights and recommendations
export const PERFORMANCE_INSIGHTS = {
    topPerformers: [
        {
            category: 'Throughput Champion',
            profile: 'JCacheX-ZeroCopy',
            achievement: '501.1M ops/sec',
            improvement: '100% scaling efficiency target'
        },
        {
            category: 'Scaling Excellence',
            profile: 'JCacheX-WriteHeavy',
            achievement: '97.2% efficiency',
            improvement: 'Near-perfect CPU utilization'
        },
        {
            category: 'Balanced Performance',
            profile: 'JCacheX-HighPerformance',
            achievement: '198.4M ops/sec',
            improvement: '82.9% scaling efficiency'
        }
    ],
    optimizationOpportunities: [
        {
            profile: 'JCacheX-Default',
            currentEfficiency: '16.7%',
            targetEfficiency: '90%+',
            improvement: 'Significant concurrency optimization potential'
        },
        {
            profile: 'JCacheX-ApiCache',
            currentEfficiency: '16.3%',
            targetEfficiency: '80%+',
            improvement: 'Threading optimization needed'
        },
        {
            profile: 'JCacheX-SessionCache',
            currentEfficiency: '24.4%',
            targetEfficiency: '80%+',
            improvement: 'Concurrency improvements required'
        }
    ]
};
