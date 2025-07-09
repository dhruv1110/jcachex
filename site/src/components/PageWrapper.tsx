import React from 'react';
import { Helmet } from 'react-helmet-async';
import { MetaTags } from './SEO';

interface PageWrapperProps {
    children: React.ReactNode;
    title?: string;
    description?: string;
    keywords?: string | string[];
    className?: string;
}

const PageWrapper: React.FC<PageWrapperProps> = ({
    children,
    title,
    description,
    keywords,
    className = ''
}) => {
    // Convert keywords to array if it's a string
    const processedKeywords = keywords
        ? Array.isArray(keywords)
            ? keywords
            : keywords.split(',').map(k => k.trim())
        : undefined;

    // Create SEO data from props with defaults
    const finalSeoData = {
        title: title || 'JCacheX - High-Performance Java Caching Library',
        description: description || 'JCacheX is a high-performance, feature-rich Java caching library with async support, Spring integration, distributed caching, and advanced eviction strategies.',
        keywords: processedKeywords || ['Java cache', 'caching library', 'high performance', 'Spring integration'],
        canonical: `https://dhruv1110.github.io/jcachex${window.location.pathname}`,
        type: 'website' as const,
        author: 'JCacheX Team'
    };

    return (
        <div className={`page ${className}`.trim()}>
            {title && (
                <Helmet>
                    <title>{title}</title>
                    {description && <meta name="description" content={description} />}
                    {processedKeywords && <meta name="keywords" content={processedKeywords.join(', ')} />}
                </Helmet>
            )}
            <MetaTags seo={finalSeoData} />
            {children}
        </div>
    );
};

export default PageWrapper;
