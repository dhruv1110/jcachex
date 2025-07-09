import { useState, useCallback } from 'react';
import type { UseTabStateReturn, CodeTab } from '@/types';

/**
 * Custom hook for managing tab state
 * Provides tab switching functionality and active state management
 */
export const useTabState = (initialTab: string | null = null): UseTabStateReturn => {
    const [activeTab, setActiveTab] = useState<string>(initialTab || '');

    const switchTab = useCallback((tabId: string) => {
        setActiveTab(tabId);
    }, []);

    const initializeTab = useCallback((tabs: CodeTab[]) => {
        if (!activeTab && tabs && tabs.length > 0) {
            setActiveTab(tabs[0].id);
        }
    }, [activeTab]);

    return {
        activeTab,
        setActiveTab,
        switchTab,
        initializeTab
    };
};

export default useTabState;
