import React from 'react';
import './Badge.css';

const Badge = ({
    children,
    variant = 'default',
    size = 'medium',
    className = '',
    href,
    target = '_blank',
    rel = 'noopener noreferrer'
}) => {
    const Component = href ? 'a' : 'span';

    return (
        <Component
            className={`badge ${variant} ${size} ${className}`}
            href={href}
            target={href ? target : undefined}
            rel={href ? rel : undefined}
        >
            {children}
        </Component>
    );
};

export default Badge;
