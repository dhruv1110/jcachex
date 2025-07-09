import React from 'react';
import type { BadgeVariant, BadgeSize } from '../../types';
import './Badge.css';

interface BadgeProps {
    children: React.ReactNode;
    variant?: BadgeVariant;
    size?: BadgeSize;
    className?: string;
    href?: string;
    target?: '_blank' | '_self';
    rel?: string;
}

const Badge: React.FC<BadgeProps> = ({
    children,
    variant = 'default',
    size = 'medium',
    className = '',
    href,
    target = '_blank',
    rel = 'noopener noreferrer'
}) => {
    if (href) {
        return (
            <a
                className={`badge ${variant} ${size} ${className}`}
                href={href}
                target={target}
                rel={rel}
            >
                {children}
            </a>
        );
    }

    return (
        <span className={`badge ${variant} ${size} ${className}`}>
            {children}
        </span>
    );
};

export default Badge;
