import { useState, useCallback } from 'react';

/**
 * Custom hook for managing tab state
 * Provides tab switching functionality and active state management
 */
export const useTabState = (initialTab = null) => {
    const [activeTab, setActiveTab] = useState(initialTab);

    const handleTabChange = useCallback((tabId) => {
        setActiveTab(tabId);
    }, []);

    const initializeTab = useCallback((tabs) => {
        if (!activeTab && tabs && tabs.length > 0) {
            setActiveTab(tabs[0].id);
        }
    }, [activeTab]);

    return {
        activeTab,
        setActiveTab,
        handleTabChange,
        initializeTab
    };
};

export default useTabState;
