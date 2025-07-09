import React from 'react';
import { Link } from 'react-router-dom';
import { BreadcrumbItem } from '../../types';

interface BreadcrumbsProps {
    items: BreadcrumbItem[];
    className?: string;
}

const Breadcrumbs: React.FC<BreadcrumbsProps> = ({ items, className = '' }) => {
    if (!items.length) return null;

    // Generate structured data for breadcrumbs
    const structuredData = {
        '@context': 'https://schema.org',
        '@type': 'BreadcrumbList',
        itemListElement: items.map((item, index) => ({
            '@type': 'ListItem',
            position: index + 1,
            name: item.label,
            ...(item.path && { item: `https://dhruv1110.github.io/jcachex${item.path}` })
        }))
    };

    return (
        <>
            <nav
                className={`breadcrumbs ${className}`}
                aria-label="Breadcrumb navigation"
                role="navigation"
            >
                <ol className="breadcrumbs-list">
                    {items.map((item, index) => (
                        <li
                            key={index}
                            className={`breadcrumbs-item ${item.current ? 'current' : ''}`}
                        >
                            {item.path && !item.current ? (
                                <Link
                                    to={item.path}
                                    className="breadcrumbs-link"
                                    aria-current={item.current ? 'page' : undefined}
                                >
                                    {item.label}
                                </Link>
                            ) : (
                                <span
                                    className="breadcrumbs-text"
                                    aria-current={item.current ? 'page' : undefined}
                                >
                                    {item.label}
                                </span>
                            )}
                            {index < items.length - 1 && (
                                <svg
                                    className="breadcrumbs-separator"
                                    width="16"
                                    height="16"
                                    viewBox="0 0 16 16"
                                    fill="none"
                                    aria-hidden="true"
                                >
                                    <path
                                        d="M6 12L10 8L6 4"
                                        stroke="currentColor"
                                        strokeWidth="1.5"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                    />
                                </svg>
                            )}
                        </li>
                    ))}
                </ol>
            </nav>

            {/* Structured data for breadcrumbs */}
            <script
                type="application/ld+json"
                dangerouslySetInnerHTML={{ __html: JSON.stringify(structuredData) }}
            />
        </>
    );
};

export default Breadcrumbs;
