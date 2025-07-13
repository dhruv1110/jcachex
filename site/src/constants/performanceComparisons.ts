// Performance comparison data
export const PERFORMANCE_COMPARISONS = [
    {
        library: 'JCacheX (Zero-Copy)',
        getLatency: '7.9ns',
        putLatency: '42.1ns',
        throughput: '50M+ ops/sec',
        percentage: 100
    },
    {
        library: 'JCacheX (Read-Heavy)',
        getLatency: '11.5ns',
        putLatency: '89.2ns',
        throughput: '45M+ ops/sec',
        percentage: 95
    },
    {
        library: 'Caffeine',
        getLatency: '23.4ns',
        putLatency: '156.8ns',
        throughput: '32M+ ops/sec',
        percentage: 75
    },
    {
        library: 'Guava Cache',
        getLatency: '67.2ns',
        putLatency: '234.5ns',
        throughput: '18M+ ops/sec',
        percentage: 45
    },
    {
        library: 'EhCache',
        getLatency: '145.6ns',
        putLatency: '389.7ns',
        throughput: '12M+ ops/sec',
        percentage: 30
    },
    {
        library: 'Redis (Local)',
        getLatency: '0.2ms',
        putLatency: '0.3ms',
        throughput: '100K+ ops/sec',
        percentage: 8
    }
];
