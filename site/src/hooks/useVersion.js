import { useMemo } from 'react';
import { VERSION } from '../constants';

/**
 * Custom hook for version management
 * Handles dynamic version replacement in strings and tabs
 */
export const useVersion = () => {
    return useMemo(() => {
        return {
            version: VERSION,
            replaceVersion: (text) => {
                if (typeof text === 'string') {
                    return text.replace(/\${VERSION}/g, VERSION);
                }
                return text;
            },
            replaceVersionInTabs: (tabs) => {
                return tabs.map(tab => ({
                    ...tab,
                    code: tab.code.replace(/\${VERSION}/g, VERSION)
                }));
            }
        };
    }, []);
};

export default useVersion;
