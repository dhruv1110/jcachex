import React from 'react';
import type { GridColumns, GridGap } from '../../types';

interface GridProps {
    children: React.ReactNode;
    columns?: GridColumns;
    gap?: GridGap;
    className?: string;
    responsive?: boolean;
}

const Grid: React.FC<GridProps> = ({
    children,
    columns = 3,
    gap = 'md',
    className = '',
    responsive = true
}) => {
    return (
        <div
            className={`grid ${gap} ${responsive ? 'responsive' : ''} ${className}`}
            style={{ '--columns': columns } as React.CSSProperties}
        >
            {children}
        </div>
    );
};

export default Grid;
