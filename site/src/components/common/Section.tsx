import React from 'react';
import type { SectionBackground, SectionPadding } from '../../types';

interface SectionProps {
    children: React.ReactNode;
    title?: string;
    subtitle?: string;
    className?: string;
    background?: SectionBackground;
    centered?: boolean;
    padding?: SectionPadding;
}

const Section: React.FC<SectionProps> = ({
    children,
    title,
    subtitle,
    className = '',
    background = 'default',
    centered = false,
    padding = 'default'
}) => {
    return (
        <section className={`section ${background} ${padding} ${className}`}>
            <div className={`section-container ${centered ? 'centered' : ''}`}>
                {(title || subtitle) && (
                    <div className="section-header">
                        {title && <h2 className="section-title">{title}</h2>}
                        {subtitle && <p className="section-subtitle">{subtitle}</p>}
                    </div>
                )}
                <div className="section-content">
                    {children}
                </div>
            </div>
        </section>
    );
};

export default Section;
