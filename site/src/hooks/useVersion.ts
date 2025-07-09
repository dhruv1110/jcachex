import { useMemo } from 'react';
import type { UseVersionReturn, CodeTab } from '@/types';
import { VERSION } from '../constants';

/**
 * Custom hook for version management
 * Handles dynamic version replacement in strings and tabs
 */
export const useVersion = (): UseVersionReturn => {
    return useMemo(() => {
        return {
            version: VERSION,
            replaceVersion: (text: string): string => {
                if (typeof text === 'string') {
                    return text.replace(/\${VERSION}/g, VERSION);
                }
                return text;
            },
            replaceVersionInTabs: (tabs: CodeTab[]): CodeTab[] => {
                return tabs.map(tab => ({
                    ...tab,
                    code: tab.code.replace(/\${VERSION}/g, VERSION)
                }));
            }
        };
    }, []);
};

export default useVersion;
