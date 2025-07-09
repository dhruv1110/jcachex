import React from 'react';
import './FeatureCard.css';

interface FeatureCardProps {
    icon: string;
    title: string;
    description: string;
    details?: string[];
    className?: string;
    onClick?: () => void;
    variant?: 'default' | 'compact' | 'horizontal';
}

const FeatureCard: React.FC<FeatureCardProps> = ({
    icon,
    title,
    description,
    details = [],
    className = '',
    onClick,
    variant = 'default'
}) => {
    return (
        <div
            className={`feature-card ${variant} ${className} ${onClick ? 'clickable' : ''}`}
            onClick={onClick}
            role={onClick ? 'button' : undefined}
            tabIndex={onClick ? 0 : undefined}
            onKeyDown={onClick ? (e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    onClick();
                }
            } : undefined}
        >
            <div className="feature-icon">{icon}</div>
            <div className="feature-content">
                <h3 className="feature-title">{title}</h3>
                <p className="feature-description">{description}</p>
                {details.length > 0 && (
                    <ul className="feature-details">
                        {details.map((detail, index) => (
                            <li key={index}>{detail}</li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
};

export default FeatureCard;
