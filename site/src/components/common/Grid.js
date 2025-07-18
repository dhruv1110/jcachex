import React from 'react';

const Grid = ({
    children,
    columns = 3,
    gap = 'default',
    className = '',
    responsive = true
}) => {
    return (
        <div
            className={`grid ${gap} ${responsive ? 'responsive' : ''} ${className}`}
            style={{ '--columns': columns }}
        >
            {children}
        </div>
    );
};

export default Grid;
