import React from 'react';
import { Typography, TypographyProps } from '@mui/material';

interface HeadingProps extends Omit<TypographyProps, 'variant'> {
    level: 1 | 2 | 3 | 4 | 5 | 6;
    children: React.ReactNode;
    seoOptimized?: boolean;
}

// SEO-optimized heading component that ensures proper hierarchy
export const SEOHeading: React.FC<HeadingProps> = ({
    level,
    children,
    seoOptimized = true,
    ...props
}) => {
    const variants = {
        1: 'h1',
        2: 'h2',
        3: 'h3',
        4: 'h4',
        5: 'h5',
        6: 'h6'
    } as const;

    const seoStyles = seoOptimized ? {
        1: {
            fontWeight: 700,
            fontSize: { xs: '2rem', sm: '2.5rem', md: '3rem' },
            lineHeight: 1.1,
            letterSpacing: '-0.01em'
        },
        2: {
            fontWeight: 600,
            fontSize: { xs: '1.5rem', sm: '2rem', md: '2.5rem' },
            lineHeight: 1.2,
            letterSpacing: '-0.005em'
        },
        3: {
            fontWeight: 600,
            fontSize: { xs: '1.25rem', sm: '1.5rem', md: '2rem' },
            lineHeight: 1.3
        },
        4: {
            fontWeight: 600,
            fontSize: { xs: '1.125rem', sm: '1.25rem', md: '1.5rem' },
            lineHeight: 1.4
        },
        5: {
            fontWeight: 600,
            fontSize: { xs: '1rem', sm: '1.125rem', md: '1.25rem' },
            lineHeight: 1.5
        },
        6: {
            fontWeight: 600,
            fontSize: { xs: '0.875rem', sm: '1rem', md: '1.125rem' },
            lineHeight: 1.6
        }
    } : {};

    return (
        <Typography
            variant={variants[level]}
            component={`h${level}`}
            sx={{
                ...seoStyles[level],
                ...props.sx
            }}
            {...props}
        >
            {children}
        </Typography>
    );
};

// Hook to validate heading structure
export const useHeadingStructure = () => {
    const validateHeadingStructure = () => {
        if (typeof window === 'undefined') return;

        const headings = document.querySelectorAll('h1, h2, h3, h4, h5, h6');
        const headingLevels: number[] = [];

        headings.forEach((heading) => {
            const level = parseInt(heading.tagName.charAt(1), 10);
            headingLevels.push(level);
        });

        // Check for proper hierarchy
        const issues: string[] = [];

        // Should have exactly one H1
        const h1Count = headingLevels.filter(level => level === 1).length;
        if (h1Count === 0) {
            issues.push('Missing H1 heading');
        } else if (h1Count > 1) {
            issues.push('Multiple H1 headings found');
        }

        // Check for skipped levels
        for (let i = 1; i < headingLevels.length; i++) {
            const current = headingLevels[i];
            const previous = headingLevels[i - 1];

            if (current > previous + 1) {
                issues.push(`Heading hierarchy skipped from H${previous} to H${current}`);
            }
        }

        if (issues.length > 0 && process.env.NODE_ENV === 'development') {
            console.warn('Heading structure issues:', issues);
        }

        return issues;
    };

    return { validateHeadingStructure };
};

export default SEOHeading;
