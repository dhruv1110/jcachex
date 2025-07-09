import React from 'react';
import './Section.css';

const Section = ({
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
