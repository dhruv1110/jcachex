/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
    tutorialSidebar: [
        {
            type: 'doc',
            id: 'introduction',
            label: 'Introduction',
        },
        {
            type: 'category',
            label: 'Getting Started',
            link: {
                type: 'doc',
                id: 'getting-started',
            },
            items: [
                'getting-started/installation',
            ],
        },
        {
            type: 'category',
            label: 'Core Concepts',
            items: [
                'core-concepts/cache-profiles',
            ],
        },
        {
            type: 'category',
            label: 'Examples',
            items: [
                'examples',
            ],
        },
        {
            type: 'category',
            label: 'Spring Boot Integration',
            items: [
                'spring-boot',
            ],
        },
        {
            type: 'category',
            label: 'API Reference',
            items: [
                'api-reference',
            ],
        },
        {
            type: 'category',
            label: 'Performance & Benchmarks',
            items: [
                'performance',
            ],
        },
    ],
};

module.exports = sidebars;
