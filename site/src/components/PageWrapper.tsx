import React from 'react';
import { Helmet } from 'react-helmet-async';
import { useSEO } from '../hooks';
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
    const { getCurrentPageSEO } = useSEO();
    const seoData = getCurrentPageSEO();

    // Convert keywords to array if it's a string
    const processedKeywords = keywords
        ? Array.isArray(keywords)
            ? keywords
            : keywords.split(',').map(k => k.trim())
        : undefined;

    // Override SEO data if custom props are provided
    const finalSeoData = {
        ...seoData,
        ...(title && { title }),
        ...(description && { description }),
        ...(processedKeywords && { keywords: processedKeywords })
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
