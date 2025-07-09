import { useState, useEffect, useCallback } from 'react';
import type { UseResponsiveReturn } from '@/types';

interface WindowSize {
    width: number;
    height: number;
}

/**
 * Custom hook for responsive design
 * Provides screen size information and responsive utilities
 */
export const useResponsive = (): UseResponsiveReturn => {
    const [windowSize, setWindowSize] = useState<WindowSize>({
        width: typeof window !== 'undefined' ? window.innerWidth : 0,
        height: typeof window !== 'undefined' ? window.innerHeight : 0
    });

    const handleResize = useCallback(() => {
        setWindowSize({
            width: window.innerWidth,
            height: window.innerHeight
        });
    }, []);

    useEffect(() => {
        if (typeof window === 'undefined') return;

        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, [handleResize]);

    const isMobile = windowSize.width < 768;
    const isTablet = windowSize.width >= 768 && windowSize.width < 1024;
    const isDesktop = windowSize.width >= 1024;

    const screenSize: 'mobile' | 'tablet' | 'desktop' = isMobile ? 'mobile' : isTablet ? 'tablet' : 'desktop';

    return {
        isMobile,
        isTablet,
        isDesktop,
        screenSize
    };
};

export default useResponsive;
