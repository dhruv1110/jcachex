// @ts-check
// Note: type checking disabled in script.

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'JCacheX',
    tagline: 'High-performance Java caching — profile-based, from in-memory to distributed (Kubernetes), with Kotlin DSL and Spring Boot integration.',
    favicon: 'img/favicon.ico',

    // Set the production url of your site here
    url: 'https://jcachex.readthedocs.io',
    // Set the /<baseUrl>/ pathname under which your site is served
    // For GitHub pages deployment, it is often '/<projectName>/'
    baseUrl: '/',

    // GitHub pages deployment config.
    // If you aren't using GitHub pages, you don't need these.
    organizationName: 'dhruv1110', // Usually your GitHub org/user name.
    projectName: 'jcachex', // Usually your repo name.

    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',

    // Even if you don't use internalization, you can use this field to set useful
    // metadata like html lang. For example, if your site is Chinese, you may want
    // to replace "en" with "zh-Hans".
    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },

    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    sidebarPath: require.resolve('./sidebars.js'),
                    // Please change this to your repo.
                    // Remove this to remove the "edit this page" links.
                    editUrl:
                        'https://github.com/dhruv1110/jcachex/edit/main/docs/',
                },
                blog: {
                    showReadingTime: true,
                    // Please change this to your repo.
                    // Remove this to remove the "edit this page" links.
                    editUrl:
                        'https://github.com/dhruv1110/jcachex/edit/main/docs/',
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    themeConfig:
        /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            // Replace with your project's social card
            image: 'img/docusaurus-social-card.jpg',
            navbar: {
                title: 'JCacheX',
                logo: {
                    alt: 'JCacheX Logo',
                    src: 'img/logo.svg',
                },
                items: [
                    {
                        type: 'docSidebar',
                        sidebarId: 'tutorialSidebar',
                        position: 'left',
                        label: 'Documentation',
                    },
                    {
                        to: '/docs/getting-started',
                        label: 'Getting Started',
                        position: 'left',
                    },
                    {
                        to: '/docs/examples',
                        label: 'Examples',
                        position: 'left',
                    },
                    {
                        to: '/docs/spring-boot',
                        label: 'Spring Boot',
                        position: 'left',
                    },
                    {
                        to: '/docs/performance',
                        label: 'Performance',
                        position: 'left',
                    },
                    {
                        href: 'https://github.com/dhruv1110/jcachex',
                        label: 'GitHub',
                        position: 'right',
                    },
                ],
            },
            footer: {
                style: 'dark',
                links: [
                    {
                        title: 'Docs',
                        items: [
                            {
                                label: 'Getting Started',
                                to: '/docs/getting-started',
                            },
                            {
                                label: 'Examples',
                                to: '/docs/examples',
                            },
                            {
                                label: 'API Reference',
                                to: '/docs/api-reference',
                            },
                        ],
                    },
                    {
                        title: 'Community',
                        items: [
                            {
                                label: 'GitHub',
                                href: 'https://github.com/dhruv1110/jcachex',
                            },
                            {
                                label: 'Issues',
                                href: 'https://github.com/dhruv1110/jcachex/issues',
                            },
                            {
                                label: 'Discussions',
                                href: 'https://github.com/dhruv1110/jcachex/discussions',
                            },
                        ],
                    },
                    {
                        title: 'More',
                        items: [
                            {
                                label: 'Blog',
                                to: '/blog',
                            },
                            {
                                label: 'Benchmarks',
                                to: '/docs/performance',
                            },
                            {
                                label: 'Examples',
                                to: '/docs/examples',
                            },
                        ],
                    },
                ],
                copyright: `Copyright © ${new Date().getFullYear()} JCacheX. Built with Docusaurus.`,
            },
            prism: {
                additionalLanguages: ['java', 'kotlin', 'yaml', 'gradle'],
            },
            colorMode: {
                defaultMode: 'dark',
                disableSwitch: false,
                respectPrefersColorScheme: true,
            },
        }),
};

module.exports = config;
