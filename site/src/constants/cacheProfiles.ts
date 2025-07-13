// Cache profiles with their characteristics
export const CACHE_PROFILES: { name: string; title: string; description: string; useCase: string; category: string; performance?: string; }[] = [
    // Core Profiles
    {
        name: 'DEFAULT',
        title: 'Default Profile',
        description: 'General-purpose cache with balanced performance',
        useCase: 'Standard caching needs, good all-around performance',
        category: 'Core',
        performance: '40.4ns GET, 92.6ns PUT'
    },
    {
        name: 'READ_HEAVY',
        title: 'Read-Heavy Profile',
        description: 'Optimized for read-intensive workloads (80%+ reads)',
        useCase: 'Read-heavy applications, reference data, configuration',
        category: 'Core',
        performance: '11.5ns GET'
    },
    {
        name: 'WRITE_HEAVY',
        title: 'Write-Heavy Profile',
        description: 'Optimized for write-intensive workloads (50%+ writes)',
        useCase: 'Write-heavy applications, session storage, logging',
        category: 'Core',
        performance: '393.5ns PUT'
    },
    {
        name: 'MEMORY_EFFICIENT',
        title: 'Memory-Efficient Profile',
        description: 'Minimizes memory usage for constrained environments',
        useCase: 'Memory-constrained environments, embedded systems',
        category: 'Core',
        performance: '39.7ns GET, 88.5ns PUT'
    },
    {
        name: 'HIGH_PERFORMANCE',
        title: 'High-Performance Profile',
        description: 'Maximum throughput optimization',
        useCase: 'High-throughput applications, performance-critical systems',
        category: 'Core',
        performance: '24.6ns GET, 63.8ns PUT'
    },
    // Specialized Profiles
    {
        name: 'SESSION_CACHE',
        title: 'Session Cache Profile',
        description: 'Optimized for user session storage with time-based expiration',
        useCase: 'User sessions, temporary data with TTL',
        category: 'Specialized',
        performance: '40.4ns GET, 92.6ns PUT'
    },
    {
        name: 'API_CACHE',
        title: 'API Cache Profile',
        description: 'Optimized for API response caching with short TTL',
        useCase: 'External API responses, network-bound operations',
        category: 'Specialized',
        performance: '728.8ns GET, 1150.0ns PUT'
    },
    {
        name: 'COMPUTE_CACHE',
        title: 'Compute Cache Profile',
        description: 'Optimized for expensive computation results',
        useCase: 'Heavy computations, ML inference, complex calculations',
        category: 'Specialized',
        performance: '728.8ns GET, 1150.0ns PUT'
    },
    // Advanced Profiles
    {
        name: 'ML_OPTIMIZED',
        title: 'ML-Optimized Profile',
        description: 'Machine learning optimized with predictive capabilities',
        useCase: 'ML applications, predictive caching scenarios',
        category: 'Advanced',
        performance: '42961.5ns GET, 349.6ns PUT'
    },
    {
        name: 'ZERO_COPY',
        title: 'Zero-Copy Profile',
        description: 'Ultra-low latency with zero-copy operations',
        useCase: 'High-frequency trading, ultra-low latency requirements',
        category: 'Advanced',
        performance: '7.9ns GET (2.6x faster than Caffeine)'
    },
    {
        name: 'HARDWARE_OPTIMIZED',
        title: 'Hardware-Optimized Profile',
        description: 'Hardware-optimized with CPU-specific features',
        useCase: 'Hardware-specific optimizations, embedded systems',
        category: 'Advanced',
        performance: '24.7ns GET, 78.4ns PUT'
    },
    {
        name: 'DISTRIBUTED',
        title: 'Distributed Profile',
        description: 'Distributed cache optimized for cluster environments',
        useCase: 'Multi-node applications, distributed systems',
        category: 'Advanced',
        performance: 'Network-dependent latency'
    }
];
