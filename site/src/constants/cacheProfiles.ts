// Cache profiles with their characteristics
export const CACHE_PROFILES: { name: string; title: string; description: string; useCase: string; category: string; performance?: string; }[] = [
    // Core Profiles
    {
        name: 'DEFAULT',
        title: 'Default Profile',
        description: 'General-purpose cache with balanced performance',
        useCase: 'Standard caching needs, good all-around performance',
        category: 'Core',
        performance: '19.3M ops/s, 10.4μs GET'
    },
    {
        name: 'READ_HEAVY',
        title: 'Read-Heavy Profile',
        description: 'Optimized for read-intensive workloads (80%+ reads)',
        useCase: 'Read-heavy applications, reference data, configuration',
        category: 'Core',
        performance: '22.6M ops/s, 93.7% efficiency'
    },
    {
        name: 'WRITE_HEAVY',
        title: 'Write-Heavy Profile',
        description: 'Optimized for write-intensive workloads (50%+ writes)',
        useCase: 'Write-heavy applications, session storage, logging',
        category: 'Core',
        performance: '224.6M ops/s, 97.2% efficiency'
    },
    {
        name: 'MEMORY_EFFICIENT',
        title: 'Memory-Efficient Profile',
        description: 'Minimizes memory usage for constrained environments',
        useCase: 'Memory-constrained environments, embedded systems',
        category: 'Core',
        performance: '31.0M ops/s, 9.9μs GET'
    },
    {
        name: 'HIGH_PERFORMANCE',
        title: 'High-Performance Profile',
        description: 'Maximum throughput optimization',
        useCase: 'High-throughput applications, performance-critical systems',
        category: 'Core',
        performance: '198.4M ops/s, 82.9% efficiency'
    },
    // Specialized Profiles
    {
        name: 'SESSION_CACHE',
        title: 'Session Cache Profile',
        description: 'Optimized for user session storage with time-based expiration',
        useCase: 'User sessions, temporary data with TTL',
        category: 'Specialized',
        performance: '37.3M ops/s, 9.3μs GET'
    },
    {
        name: 'API_CACHE',
        title: 'API Cache Profile',
        description: 'Optimized for API response caching with short TTL',
        useCase: 'External API responses, network-bound operations',
        category: 'Specialized',
        performance: '19.0M ops/s, 10.1μs GET'
    },
    {
        name: 'COMPUTE_CACHE',
        title: 'Compute Cache Profile',
        description: 'Optimized for expensive computation results',
        useCase: 'Heavy computations, ML inference, complex calculations',
        category: 'Specialized',
        performance: '20.9M ops/s, 9.4μs GET'
    },
    // Advanced Profiles
    {
        name: 'ML_OPTIMIZED',
        title: 'ML-Optimized Profile',
        description: 'Machine learning optimized with predictive capabilities',
        useCase: 'ML applications, predictive caching scenarios',
        category: 'Advanced',
        performance: '0.1M ops/s, 89.4% efficiency'
    },
    {
        name: 'ZERO_COPY',
        title: 'Zero-Copy Profile',
        description: 'Ultra-low latency with zero-copy operations',
        useCase: 'High-frequency trading, ultra-low latency requirements',
        category: 'Advanced',
        performance: '501.1M ops/s, 98.4% efficiency'
    },
    {
        name: 'HARDWARE_OPTIMIZED',
        title: 'Hardware-Optimized Profile',
        description: 'Hardware-optimized with CPU-specific features',
        useCase: 'Hardware-specific optimizations, embedded systems',
        category: 'Advanced',
        performance: '143.9M ops/s, 80.6% efficiency'
    },
    {
        name: 'DISTRIBUTED',
        title: 'Distributed Profile',
        description: 'Distributed cache optimized for cluster environments',
        useCase: 'Multi-node applications, distributed systems',
        category: 'Advanced',
        performance: '0.3M ops/s, 30.5% efficiency'
    }
];
