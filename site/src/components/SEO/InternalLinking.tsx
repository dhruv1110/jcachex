import React from 'react';
import { Link as RouterLink, LinkProps as RouterLinkProps } from 'react-router-dom';
import { Link as MuiLink, LinkProps as MuiLinkProps } from '@mui/material';
import { styled } from '@mui/material/styles';

interface SEOLinkProps extends Omit<RouterLinkProps, 'to'> {
    to: string;
    children: React.ReactNode;
    title?: string;
    rel?: string;
    'aria-label'?: string;
    muiProps?: Omit<MuiLinkProps, 'component' | 'to'>;
}

// SEO-optimized internal link component
export const SEOLink: React.FC<SEOLinkProps> = ({
    to,
    children,
    title,
    rel,
    'aria-label': ariaLabel,
    muiProps,
    ...props
}) => {
    // Ensure proper internal linking structure
    const isExternal = to.startsWith('http') || to.startsWith('//');

    if (isExternal) {
        return (
            <MuiLink
                href={to}
                target="_blank"
                rel={rel || "noopener noreferrer"}
                title={title}
                aria-label={ariaLabel}
                {...muiProps}
                {...props}
            >
                {children}
            </MuiLink>
        );
    }

    return (
        <MuiLink
            component={RouterLink}
            to={to}
            title={title}
            rel={rel}
            aria-label={ariaLabel}
            {...muiProps}
            {...props}
        >
            {children}
        </MuiLink>
    );
};

// Styled navigation links for better SEO
export const SEONavLink = styled(SEOLink)(({ theme }) => ({
    textDecoration: 'none',
    color: theme.palette.text.primary,
    fontWeight: 500,
    transition: 'color 0.2s ease-in-out',
    '&:hover': {
        color: theme.palette.primary.main,
        textDecoration: 'underline'
    },
    '&:focus': {
        outline: `2px solid ${theme.palette.primary.main}`,
        outlineOffset: '2px'
    }
}));

// Related pages component for better internal linking
interface RelatedPagesProps {
    pages: Array<{
        title: string;
        path: string;
        description?: string;
        category?: string;
    }>;
    title?: string;
    limit?: number;
}

export const RelatedPages: React.FC<RelatedPagesProps> = ({
    pages,
    title = 'Related Pages',
    limit = 5
}) => {
    const displayPages = pages.slice(0, limit);

    return (
        <section className="related-pages" aria-labelledby="related-pages-heading">
            <h2 id="related-pages-heading" style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>
                {title}
            </h2>
            <nav>
                <ul style={{ listStyle: 'none', padding: 0 }}>
                    {displayPages.map((page, index) => (
                        <li key={index} style={{ marginBottom: '0.5rem' }}>
                            <SEOLink
                                to={page.path}
                                title={page.description || `Navigate to ${page.title}`}
                                aria-label={`Go to ${page.title} page`}
                            >
                                {page.title}
                            </SEOLink>
                            {page.description && (
                                <p style={{
                                    fontSize: '0.875rem',
                                    color: '#666',
                                    margin: '0.25rem 0 0 0'
                                }}>
                                    {page.description}
                                </p>
                            )}
                        </li>
                    ))}
                </ul>
            </nav>
        </section>
    );
};

// Site-wide navigation structure for better SEO
export const SITE_NAVIGATION = {
    main: [
        { path: '/', title: 'Home', description: 'JCacheX homepage' },
        { path: '/getting-started', title: 'Getting Started', description: 'Quick start guide' },
        { path: '/examples', title: 'Examples', description: 'Code examples and tutorials' },
        { path: '/spring', title: 'Spring Boot', description: 'Spring Boot integration guide' },
        { path: '/faq', title: 'FAQ', description: 'Frequently asked questions' }
    ],
    documentation: [
        { path: '/getting-started', title: 'Getting Started', description: 'Installation and setup' },
        { path: '/examples', title: 'Examples', description: 'Code examples' },
        { path: '/spring', title: 'Spring Boot Guide', description: 'Spring Boot integration' },
        { path: '/faq', title: 'FAQ', description: 'Common questions' }
    ],
    features: [
        { path: '/examples?section=basic', title: 'Basic Caching', description: 'Core caching features' },
        { path: '/examples?section=async', title: 'Async Operations', description: 'Asynchronous caching' },
        { path: '/examples?section=distributed', title: 'Distributed Caching', description: 'Multi-node caching' },
        { path: '/spring', title: 'Spring Integration', description: 'Spring Boot support' }
    ]
};

// Breadcrumb navigation for better SEO structure
export const generateBreadcrumbs = (pathname: string) => {
    const pathSegments = pathname.split('/').filter(Boolean);
    const breadcrumbs: Array<{ label: string; path: string; current?: boolean }> = [{ label: 'Home', path: '/' }];

    let currentPath = '';

    pathSegments.forEach((segment, index) => {
        currentPath += `/${segment}`;

        // Map segments to proper titles
        const titleMap: { [key: string]: string } = {
            'getting-started': 'Getting Started',
            'examples': 'Examples',
            'spring': 'Spring Boot',
            'spring-boot': 'Spring Boot',
            'faq': 'FAQ',
            'docs': 'Documentation',
            'documentation': 'Documentation'
        };

        const title = titleMap[segment] || segment.charAt(0).toUpperCase() + segment.slice(1);

        breadcrumbs.push({
            label: title,
            path: currentPath,
            current: index === pathSegments.length - 1
        });
    });

    return breadcrumbs;
};

export default SEOLink;
