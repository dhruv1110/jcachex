import React from 'react';
import { Helmet } from 'react-helmet-async';
import { MetaTagsProps } from '../../types';

const MetaTags: React.FC<MetaTagsProps> = ({ seo, structuredData }) => {
    const {
        title,
        description,
        keywords = [],
        canonical,
        image = 'https://dhruv1110.github.io/jcachex/og-image.png',
        type = 'website',
        author,
        publishedTime,
        modifiedTime,
        section,
        tags = []
    } = seo;

    const fullTitle = title.includes('JCacheX') ? title : `${title} | JCacheX`;
    const url = canonical || `https://dhruv1110.github.io/jcachex${window.location.pathname}`;

    return (
        <Helmet>
            {/* Basic Meta Tags */}
            <title>{fullTitle}</title>
            <meta name="description" content={description} />
            {keywords.length > 0 && <meta name="keywords" content={keywords.join(', ')} />}
            {author && <meta name="author" content={author} />}
            <link rel="canonical" href={url} />

            {/* Open Graph / Facebook */}
            <meta property="og:type" content={type} />
            <meta property="og:url" content={url} />
            <meta property="og:title" content={fullTitle} />
            <meta property="og:description" content={description} />
            <meta property="og:image" content={image} />
            <meta property="og:image:alt" content={`${title} - JCacheX`} />
            <meta property="og:site_name" content="JCacheX" />
            <meta property="og:locale" content="en_US" />

            {/* Article specific Open Graph tags */}
            {type === 'article' && (
                <>
                    {author && <meta property="article:author" content={author} />}
                    {publishedTime && <meta property="article:published_time" content={publishedTime} />}
                    {modifiedTime && <meta property="article:modified_time" content={modifiedTime} />}
                    {section && <meta property="article:section" content={section} />}
                    {tags.map((tag, index) => (
                        <meta key={index} property="article:tag" content={tag} />
                    ))}
                </>
            )}

            {/* Twitter Card */}
            <meta name="twitter:card" content="summary_large_image" />
            <meta name="twitter:url" content={url} />
            <meta name="twitter:title" content={fullTitle} />
            <meta name="twitter:description" content={description} />
            <meta name="twitter:image" content={image} />
            <meta name="twitter:image:alt" content={`${title} - JCacheX`} />
            <meta name="twitter:site" content="@jcachex" />
            <meta name="twitter:creator" content="@jcachex" />

            {/* Additional SEO Meta Tags */}
            <meta name="robots" content="index, follow, max-image-preview:large, max-snippet:-1, max-video-preview:-1" />
            <meta name="googlebot" content="index, follow" />
            <meta name="bingbot" content="index, follow" />

            {/* Accessibility and Structure */}
            <meta name="rating" content="general" />
            <meta name="distribution" content="global" />
            <meta name="revisit-after" content="1 week" />
            <meta name="subject" content="Java caching library" />
            <meta name="classification" content="Software Development" />
            <meta name="coverage" content="Worldwide" />
            <meta name="target" content="software developers" />

            {/* Language and Content */}
            <meta httpEquiv="content-language" content="en" />
            <meta name="language" content="en" />

            {/* Mobile and Responsive */}
            <meta name="format-detection" content="telephone=no" />
            <meta name="mobile-web-app-capable" content="yes" />
            <meta name="apple-mobile-web-app-capable" content="yes" />
            <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
            <meta name="apple-mobile-web-app-title" content="JCacheX" />
            <meta name="apple-touch-fullscreen" content="yes" />
            <meta name="msapplication-tap-highlight" content="no" />
            <meta name="mobile-web-app-title" content="JCacheX" />
            <meta name="application-name" content="JCacheX" />
            <meta name="msapplication-TileColor" content="#1976d2" />
            <meta name="msapplication-config" content="browserconfig.xml" />
            <meta name="msapplication-navbutton-color" content="#1976d2" />

            {/* Performance and Caching */}
            <meta httpEquiv="cache-control" content="public, max-age=31536000, immutable" />
            <meta httpEquiv="expires" content="31536000" />
            <meta httpEquiv="pragma" content="cache" />
            <meta name="robots" content="index, follow, max-snippet:-1, max-image-preview:large, max-video-preview:-1" />
            <link rel="dns-prefetch" href="//fonts.googleapis.com" />
            <link rel="dns-prefetch" href="//fonts.gstatic.com" />
            <link rel="preconnect" href="https://fonts.googleapis.com" crossOrigin="anonymous" />
            <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />

            {/* Structured Data */}
            {structuredData && (
                <script type="application/ld+json">
                    {JSON.stringify(Array.isArray(structuredData) ? structuredData : [structuredData])}
                </script>
            )}
        </Helmet>
    );
};

export default MetaTags;
