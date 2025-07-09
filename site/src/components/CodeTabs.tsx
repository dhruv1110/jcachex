import React, { useState, useEffect } from 'react';
import { Box, Tabs, Tab, Paper, useTheme } from '@mui/material';
import type { CodeTab } from '../types';

interface CodeTabsProps {
    tabs: CodeTab[];
    className?: string;
}

const CodeTabs: React.FC<CodeTabsProps> = ({ tabs, className = '' }) => {
    const [activeTab, setActiveTab] = useState<number>(0);
    const theme = useTheme();

    useEffect(() => {
        // Load Prism.js for syntax highlighting
        if ((window as any).Prism) {
            (window as any).Prism.highlightAll();
        }
    }, [activeTab]);

    const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
        setActiveTab(newValue);
    };

    if (!tabs || tabs.length === 0) {
        return null;
    }

    return (
        <Paper
            elevation={2}
            sx={{
                borderRadius: 2,
                overflow: 'hidden',
                border: `1px solid ${theme.palette.divider}`,
                bgcolor: 'background.paper'
            }}
        >
            <Tabs
                value={activeTab}
                onChange={handleTabChange}
                sx={{
                    bgcolor: 'grey.50',
                    borderBottom: `1px solid ${theme.palette.divider}`,
                    minHeight: 48,
                    '& .MuiTab-root': {
                        color: 'text.secondary',
                        fontWeight: 500,
                        minHeight: 48,
                        textTransform: 'none',
                        fontSize: '0.875rem',
                        '&.Mui-selected': {
                            color: 'primary.main',
                            fontWeight: 600
                        },
                        '&:hover': {
                            color: 'primary.main',
                            opacity: 0.8
                        }
                    },
                    '& .MuiTabs-indicator': {
                        backgroundColor: 'primary.main',
                        height: 3
                    }
                }}
            >
                {tabs.map((tab, index) => (
                    <Tab
                        key={tab.id}
                        label={tab.label}
                        id={`code-tab-${index}`}
                        aria-controls={`code-tabpanel-${index}`}
                    />
                ))}
            </Tabs>

            {tabs.map((tab, index) => (
                <Box
                    key={tab.id}
                    role="tabpanel"
                    hidden={activeTab !== index}
                    id={`code-tabpanel-${index}`}
                    aria-labelledby={`code-tab-${index}`}
                    sx={{
                        bgcolor: '#f8fafc',
                        maxHeight: '500px',
                        overflow: 'auto',
                        '& pre': {
                            margin: 0,
                            padding: '1.5rem',
                            bgcolor: 'transparent',
                            overflow: 'visible',
                            fontSize: '14px',
                            lineHeight: 1.6,
                            fontFamily: 'Monaco, Menlo, "Ubuntu Mono", Consolas, monospace',
                            color: '#334155'
                        },
                        '& code': {
                            fontFamily: 'inherit',
                            fontSize: 'inherit',
                            background: 'none !important',
                            color: 'inherit'
                        },
                        // Custom syntax highlighting for better readability
                        '& .token.comment': { color: '#64748b', fontStyle: 'italic' },
                        '& .token.keyword': { color: '#7c3aed', fontWeight: 600 },
                        '& .token.string': { color: '#059669' },
                        '& .token.number': { color: '#dc2626' },
                        '& .token.class-name': { color: '#2563eb' },
                        '& .token.function': { color: '#7c2d12' },
                        '& .token.operator': { color: '#374151' },
                        '& .token.punctuation': { color: '#6b7280' },
                        // Scrollbar styling
                        '&::-webkit-scrollbar': {
                            width: '8px',
                            height: '8px'
                        },
                        '&::-webkit-scrollbar-track': {
                            background: theme.palette.grey[100],
                            borderRadius: '4px'
                        },
                        '&::-webkit-scrollbar-thumb': {
                            background: theme.palette.grey[400],
                            borderRadius: '4px',
                            '&:hover': {
                                background: theme.palette.grey[500]
                            }
                        }
                    }}
                >
                    {activeTab === index && (
                        <pre>
                            <code className={`language-${tab.language || 'text'}`}>
                                {tab.code}
                            </code>
                        </pre>
                    )}
                </Box>
            ))}
        </Paper>
    );
};

export default CodeTabs;
