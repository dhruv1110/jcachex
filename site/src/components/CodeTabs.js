import React, { useState, useEffect } from 'react';
import './CodeTabs.css';

const CodeTabs = ({ tabs, className = '' }) => {
    const [activeTab, setActiveTab] = useState(tabs[0]?.id || '');

    useEffect(() => {
        // Load Prism.js for syntax highlighting
        if (window.Prism) {
            window.Prism.highlightAll();
        }
    }, [activeTab]);

    const handleTabClick = (tabId) => {
        setActiveTab(tabId);
    };

    return (
        <div className={`code-tabs ${className}`}>
            <div className="code-tabs-nav">
                {tabs.map((tab) => (
                    <button
                        key={tab.id}
                        className={`tab-button ${activeTab === tab.id ? 'active' : ''}`}
                        onClick={() => handleTabClick(tab.id)}
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
