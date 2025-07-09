import type { NavigationItem } from '../types';

export const NAVIGATION_ITEMS: NavigationItem[] = [
    {
        id: 'home',
        label: 'Home',
        path: '/'
    },
    {
        id: 'getting-started',
        label: 'Getting Started',
        path: '/getting-started'
    },
    {
        id: 'features',
        label: 'Features',
        children: [
            {
                id: 'overview',
                label: 'Overview',
                path: '/features'
            },
            {
                id: 'performance',
                label: 'Performance',
                path: '/features/performance'
            },
            {
                id: 'async-support',
                label: 'Async Support',
                path: '/features/async'
            },
            {
                id: 'monitoring',
                label: 'Monitoring',
                path: '/features/monitoring'
            }
        ]
    },
    {
        id: 'strategies',
        label: 'Cache Strategies',
        children: [
            {
                id: 'eviction-strategies',
                label: 'Eviction Strategies',
                path: '/strategies/eviction'
            },
            {
                id: 'lru-strategy',
                label: 'LRU (Least Recently Used)',
                path: '/strategies/lru'
            },
            {
                id: 'lfu-strategy',
                label: 'LFU (Least Frequently Used)',
                path: '/strategies/lfu'
            },
            {
                id: 'fifo-strategy',
                label: 'FIFO (First In, First Out)',
                path: '/strategies/fifo'
            },
            {
                id: 'weight-strategy',
                label: 'Weight-Based',
                path: '/strategies/weight-based'
            },
            {
                id: 'custom-strategy',
                label: 'Custom Strategies',
                path: '/strategies/custom'
            }
        ]
    },
    {
        id: 'examples',
        label: 'Examples',
        children: [
            {
                id: 'basic-usage',
                label: 'Basic Usage',
                path: '/examples/basic'
            },
            {
                id: 'async-operations',
                label: 'Async Operations',
                path: '/examples/async'
            },
            {
                id: 'spring-integration',
                label: 'Spring Boot',
                path: '/examples/spring'
            },
            {
                id: 'distributed-caching',
                label: 'Distributed Caching',
                path: '/examples/distributed'
            },
            {
                id: 'kotlin-examples',
                label: 'Kotlin Examples',
                path: '/examples/kotlin'
            },
            {
                id: 'advanced-patterns',
                label: 'Advanced Patterns',
                path: '/examples/advanced'
            }
        ]
    },
    {
        id: 'spring',
        label: 'Spring Boot',
        path: '/spring'
    },
    {
        id: 'documentation',
        label: 'Documentation',
        children: [
            {
                id: 'api-docs',
                label: 'API Reference',
                href: 'https://javadoc.io/doc/io.github.dhruv1110/jcachex-core',
                target: '_blank'
            },
            {
                id: 'configuration',
                label: 'Configuration Guide',
                path: '/docs/configuration'
            },
            {
                id: 'migration',
                label: 'Migration Guide',
                path: '/docs/migration'
            },
            {
                id: 'troubleshooting',
                label: 'Troubleshooting',
                path: '/docs/troubleshooting'
            }
        ]
    },
    {
        id: 'support',
        label: 'Support',
        children: [
            {
                id: 'faq',
                label: 'FAQ',
                path: '/faq'
            },
            {
                id: 'community',
                label: 'Community',
                path: '/community'
            },
            {
                id: 'github',
                label: 'GitHub Issues',
                href: 'https://github.com/dhruv1110/JCacheX/issues',
                target: '_blank'
            },
            {
                id: 'discussions',
                label: 'Discussions',
                href: 'https://github.com/dhruv1110/JCacheX/discussions',
                target: '_blank'
            }
        ]
    },
    {
        id: 'github',
        label: 'GitHub',
        href: 'https://github.com/dhruv1110/JCacheX',
        target: '_blank'
    }
];

// Mobile navigation items (simplified for mobile menu)
export const MOBILE_NAVIGATION_ITEMS: NavigationItem[] = [
    {
        id: 'home',
        label: 'Home',
        path: '/'
    },
    {
        id: 'getting-started',
        label: 'Getting Started',
        path: '/getting-started'
    },
    {
        id: 'features',
        label: 'Features',
        path: '/features'
    },
    {
        id: 'strategies',
        label: 'Cache Strategies',
        path: '/strategies'
    },
    {
        id: 'examples',
        label: 'Examples',
        path: '/examples'
    },
    {
        id: 'spring',
        label: 'Spring Boot',
        path: '/spring'
    },
    {
        id: 'faq',
        label: 'FAQ',
        path: '/faq'
    },
    {
        id: 'api-docs',
        label: 'API Docs',
        href: 'https://javadoc.io/doc/io.github.dhruv1110/jcachex-core',
        target: '_blank'
    },
    {
        id: 'github',
        label: 'GitHub',
        href: 'https://github.com/dhruv1110/JCacheX',
        target: '_blank'
    }
];

// Quick access links for footer or quick navigation
export const QUICK_LINKS: NavigationItem[] = [
    {
        id: 'installation',
        label: 'Installation',
        path: '/getting-started#installation'
    },
    {
        id: 'basic-usage',
        label: 'Basic Usage',
        path: '/examples/basic'
    },
    {
        id: 'spring-setup',
        label: 'Spring Setup',
        path: '/spring#setup'
    },
    {
        id: 'api-reference',
        label: 'API Reference',
        href: 'https://javadoc.io/doc/io.github.dhruv1110/jcachex-core',
        target: '_blank'
    }
];
