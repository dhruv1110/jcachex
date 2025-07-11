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
        id: 'performance',
        label: 'Performance',
        path: '/performance'
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
                id: 'faq',
                label: 'FAQ',
                path: '/faq'
            }
        ]
    },
    {
        id: 'support',
        label: 'Support',
        children: [
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
        id: 'performance',
        label: 'Performance',
        path: '/performance'
    },
    {
        id: 'faq',
        label: 'FAQ',
        path: '/faq'
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
        id: 'performance',
        label: 'Performance Benchmarks',
        path: '/performance'
    },
    {
        id: 'api-reference',
        label: 'API Reference',
        href: 'https://javadoc.io/doc/io.github.dhruv1110/jcachex-core',
        target: '_blank'
    }
];
