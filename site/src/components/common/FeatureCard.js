import React from 'react';
import './FeatureCard.css';

const FeatureCard = ({
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
