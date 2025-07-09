import React, { useState, useEffect } from 'react';
import type { CodeTab } from '../types';

interface CodeTabsProps {
    tabs: CodeTab[];
    className?: string;
}

const CodeTabs: React.FC<CodeTabsProps> = ({ tabs, className = '' }) => {
    const [activeTab, setActiveTab] = useState<string>(tabs[0]?.id || '');

    useEffect(() => {
        // Load Prism.js for syntax highlighting
        if ((window as any).Prism) {
            (window as any).Prism.highlightAll();
        }
    }, [activeTab]);

    const handleTabClick = (tabId: string) => {
        setActiveTab(tabId);
    };

    if (!tabs || tabs.length === 0) {
        return null;
    }

    return (
        <div className={`code-tabs ${className}`}>
            <div className="code-tabs-nav">
                {tabs.map((tab) => (
                    <button
                        key={tab.id}
                        className={`tab-button ${activeTab === tab.id ? 'active' : ''}`}
                        onClick={() => handleTabClick(tab.id)}
                        type="button"
                    >
                        {tab.label}
                    </button>
                ))}
            </div>
            <div className="code-tabs-content">
                {tabs.map((tab) => (
                    <div
                        key={tab.id}
                        className={`tab-pane ${activeTab === tab.id ? 'active' : ''}`}
                    >
                        <pre>
                            <code className={`language-${tab.language || 'text'}`}>
                                {tab.code}
                            </code>
                        </pre>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default CodeTabs;
