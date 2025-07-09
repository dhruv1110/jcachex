import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { SEOData, StructuredData, UseSEOReturn } from '../types';

// Default SEO configuration
const DEFAULT_SEO: SEOData = {
    title: 'JCacheX - High-Performance Java Caching Library',
    description: 'JCacheX is a high-performance, feature-rich Java caching library with async support, Spring integration, distributed caching, and advanced eviction strategies.',
    keywords: [
        'Java cache',
        'caching library',
        'high performance',
        'Spring integration',
        'eviction strategies',
        'distributed cache',
        'async cache',
        'JVM cache',
        'memory cache',
        'Java performance'
    ],
    type: 'website',
    author: 'JCacheX Team'
};

// Page-specific SEO configurations
const PAGE_SEO_CONFIG: Record<string, Partial<SEOData>> = {
    '/': {
        title: 'JCacheX - High-Performance Java Caching Library',
        description: 'High-performance Java caching library with async support, Spring Boot integration, distributed caching, and advanced eviction strategies. Zero-config setup, enterprise-ready.',
        keywords: ['Java cache', 'caching library', 'high performance', 'Spring Boot', 'async cache', 'distributed cache']
    },
    '/getting-started': {
        title: 'Getting Started with JCacheX',
        description: 'Learn how to integrate JCacheX into your Java or Kotlin application in just a few minutes. Step-by-step guide with code examples.',
        keywords: ['JCacheX tutorial', 'Java cache setup', 'getting started', 'installation guide', 'quick start'],
        type: 'article',
        section: 'Documentation'
    },
    '/examples': {
        title: 'JCacheX Examples and Code Samples',
        description: 'Comprehensive examples and code samples for JCacheX. Learn async operations, Spring Boot integration, distributed caching, and advanced patterns.',
        keywords: ['JCacheX examples', 'cache examples', 'Java cache tutorial', 'Spring cache examples', 'async cache'],
        type: 'article',
        section: 'Examples'
    },
    '/spring': {
        title: 'Spring Boot Integration Guide - JCacheX',
        description: 'Complete guide to integrating JCacheX with Spring Boot applications. Auto-configuration, annotations, health checks, and monitoring.',
        keywords: ['Spring Boot cache', 'JCacheX Spring', 'Spring cache integration', 'Spring Boot starter', 'cache annotations'],
        type: 'article',
        section: 'Spring Boot'
    },
    '/faq': {
        title: 'Frequently Asked Questions - JCacheX',
        description: 'Find answers to common questions about JCacheX. Performance, configuration, troubleshooting, and migration guides.',
        keywords: ['JCacheX FAQ', 'cache questions', 'troubleshooting', 'performance optimization', 'migration guide'],
        type: 'article',
        section: 'Support'
    }
};

export const useSEO = (): UseSEOReturn => {
    const location = useLocation();

    const updateSEO = (seoData: SEOData) => {
        // This would be implemented with a context or state management
        // For now, we'll handle it through the MetaTags component
        console.log('SEO updated:', seoData);
    };

    const addStructuredData = (data: StructuredData) => {
        // Remove existing structured data of the same type
        removeStructuredData(data['@type']);

        // Add new structured data
        const script = document.createElement('script');
        script.type = 'application/ld+json';
        script.id = `structured-data-${data['@type']}`;
        script.textContent = JSON.stringify(data);
        document.head.appendChild(script);
    };

    const removeStructuredData = (type: string) => {
        const existingScript = document.getElementById(`structured-data-${type}`);
        if (existingScript) {
            existingScript.remove();
        }
    };

    // Get SEO data for current page
    const getCurrentPageSEO = (): SEOData => {
        const pageSEO = PAGE_SEO_CONFIG[location.pathname] || {};
        return {
            ...DEFAULT_SEO,
            ...pageSEO,
            canonical: `https://dhruv1110.github.io/jcachex${location.pathname}`
        };
    };

    // Add default structured data based on page
    useEffect(() => {
        const pathname = location.pathname;

        // Website structured data for home page
        if (pathname === '/') {
            addStructuredData({
                '@context': 'https://schema.org',
                '@type': 'WebSite',
                name: 'JCacheX',
                description: 'High-performance Java caching library',
                url: 'https://dhruv1110.github.io/jcachex/',
                potentialAction: {
                    '@type': 'SearchAction',
                    target: 'https://dhruv1110.github.io/jcachex/search?q={search_term_string}',
                    'query-input': 'required name=search_term_string'
                }
            });

            addStructuredData({
                '@context': 'https://schema.org',
                '@type': 'SoftwareApplication',
                name: 'JCacheX',
                description: 'High-performance Java caching library with async support, Spring integration, distributed caching, and advanced eviction strategies',
                url: 'https://dhruv1110.github.io/jcachex/',
                applicationCategory: 'DeveloperApplication',
                operatingSystem: 'Java',
                programmingLanguage: ['Java', 'Kotlin'],
                author: {
                    '@type': 'Organization',
                    name: 'JCacheX Team'
                },
                license: 'https://github.com/dhruv1110/JCacheX/blob/main/LICENSE',
                codeRepository: 'https://github.com/dhruv1110/JCacheX',
                downloadUrl: 'https://github.com/dhruv1110/JCacheX/releases',
                releaseNotes: 'https://github.com/dhruv1110/JCacheX/releases',
                offers: {
                    '@type': 'Offer',
                    price: '0',
                    priceCurrency: 'USD'
                }
            });
        }

        // Organization structured data
        addStructuredData({
            '@context': 'https://schema.org',
            '@type': 'Organization',
            name: 'JCacheX',
            url: 'https://dhruv1110.github.io/jcachex/',
            logo: 'https://dhruv1110.github.io/jcachex/logo.png',
            sameAs: [
                'https://github.com/dhruv1110/JCacheX',
                'https://mvnrepository.com/artifact/io.github.dhruv1110/jcachex-core'
            ]
        });

        // Cleanup function
        return () => {
            // Clean up structured data when component unmounts or location changes
            const scripts = document.querySelectorAll('script[id^="structured-data-"]');
            scripts.forEach(script => script.remove());
        };
    }, [location.pathname]);

    return {
        updateSEO,
        addStructuredData,
        removeStructuredData,
        getCurrentPageSEO
    };
};

export const getPageSEO = (pathname: string): SEOData => {
    const pageSEO = PAGE_SEO_CONFIG[pathname] || {};
    return {
        ...DEFAULT_SEO,
        ...pageSEO,
        canonical: `https://dhruv1110.github.io/jcachex${pathname}`
    };
};
