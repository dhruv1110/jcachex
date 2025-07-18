import React, { useState, useEffect } from 'react';
import {
    Box,
    Tabs,
    Tab,
    Paper,
    useTheme,
    IconButton,
    Tooltip,
    Typography,
    Chip,
    Button,
    Alert,
    Snackbar,
    Stack
} from '@mui/material';
import {
    ContentCopy as CopyIcon,
    PlayArrow as PlayIcon,
    OpenInNew as OpenInNewIcon,
    Speed as SpeedIcon,
    Info as InfoIcon
} from '@mui/icons-material';
import type { CodeTab } from '../types';

interface CodeTabsProps {
    tabs: CodeTab[];
    className?: string;
    showPerformanceMetrics?: boolean;
    showTryOnlineButtons?: boolean;
    showCopyButtons?: boolean;
    interactive?: boolean;
}

// Performance metrics for different code examples
const PERFORMANCE_METRICS: { [key: string]: string } = {
    'thirty-second': '7.9ns GET, Production-ready defaults',
    'five-minute': '11.5ns GET, Automatic optimization',
    'production-ready': '11.5ns GET, Full monitoring',
    'java': '11.5ns GET, Zero configuration',
    'kotlin': '11.5ns GET, DSL optimized',
    'spring': '11.5ns GET, Auto-configured',
    'ecommerce': '11.5ns GET, Multi-layer caching',
    'api-gateway': '5min TTL, Circuit breaker enabled',
    'session-management': '30min TTL, High throughput',
    'from-caffeine': '3x faster than Caffeine',
    'from-redis': '50x lower latency than Redis'
};

// Try online URLs for different examples
const TRY_ONLINE_URLS: { [key: string]: string } = {
    'thirty-second': 'https://replit.com/@jcachex/thirty-second-start',
    'five-minute': 'https://replit.com/@jcachex/five-minute-power',
    'production-ready': 'https://replit.com/@jcachex/production-ready',
    'java': 'https://replit.com/@jcachex/basic-java',
    'kotlin': 'https://replit.com/@jcachex/basic-kotlin',
    'spring': 'https://replit.com/@jcachex/spring-boot',
    'ecommerce': 'https://replit.com/@jcachex/ecommerce-catalog',
    'api-gateway': 'https://replit.com/@jcachex/api-gateway',
    'session-management': 'https://replit.com/@jcachex/session-management'
};

// Code explanations for hover tooltips
const CODE_EXPLANATIONS: { [key: string]: { [key: string]: string } } = {
    'thirty-second': {
        'JCacheX.create().build()': 'Creates a cache with production-ready defaults in one line',
        'cache.put': 'Stores data with O(1) complexity',
        'cache.get': 'Retrieves data in ~7.9ns with zero-copy optimization'
    },
    'five-minute': {
        'forReadHeavyWorkload()': 'Automatically configures cache for 11.5ns GET performance',
        'maximumSize(10000L)': 'Sets cache capacity with intelligent eviction',
        'cache.stats()': 'Real-time performance metrics available'
    },
    'production-ready': {
        'evictionListener': 'Custom logic for when items are removed',
        'loadingCache': 'Automatic database loading with error handling',
        'recordStats(true)': 'Enables comprehensive performance monitoring'
    }
};

const CodeTabs: React.FC<CodeTabsProps> = ({
    tabs,
    className = '',
    showPerformanceMetrics = true,
    showTryOnlineButtons = true,
    showCopyButtons = true,
    interactive = true
}) => {
    const [activeTab, setActiveTab] = useState<number>(0);
    const [copySuccess, setCopySuccess] = useState<string | null>(null);
    const [showCopyNotification, setShowCopyNotification] = useState(false);
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

    const handleCopyCode = async (code: string, tabLabel: string) => {
        try {
            await navigator.clipboard.writeText(code);
            setCopySuccess(`${tabLabel} code copied!`);
            setShowCopyNotification(true);
        } catch (err) {
            setCopySuccess('Failed to copy code');
            setShowCopyNotification(true);
        }
    };

    const handleCloseCopyNotification = () => {
        setShowCopyNotification(false);
    };

    const handleTryOnline = (tabId: string) => {
        const url = TRY_ONLINE_URLS[tabId];
        if (url) {
            window.open(url, '_blank');
        }
    };

    if (!tabs || tabs.length === 0) {
        return null;
    }

    const currentTab = tabs[activeTab];
    const performanceMetric = PERFORMANCE_METRICS[currentTab.id];
    const tryOnlineUrl = TRY_ONLINE_URLS[currentTab.id];

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
            {/* Enhanced Header with Performance Metrics */}
            <Box sx={{
                bgcolor: 'grey.50',
                borderBottom: `1px solid ${theme.palette.divider}`,
                p: 2,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: 1
            }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Tabs
                        value={activeTab}
                        onChange={handleTabChange}
                        sx={{
                            minHeight: 36,
                            '& .MuiTab-root': {
                                color: 'text.secondary',
                                fontWeight: 500,
                                minHeight: 36,
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

                    {showPerformanceMetrics && performanceMetric && (
                        <Tooltip title="Performance metric for this example">
                            <Chip
                                icon={<SpeedIcon />}
                                label={performanceMetric}
                                color="primary"
                                variant="outlined"
                                size="small"
                            />
                        </Tooltip>
                    )}
                </Box>

                {/* Action Buttons */}
                <Stack direction="row" spacing={1}>
                    {showCopyButtons && (
                        <Tooltip title="Copy code to clipboard">
                            <IconButton
                                onClick={() => handleCopyCode(currentTab.code, currentTab.label)}
                                size="small"
                                sx={{ color: 'text.secondary' }}
                            >
                                <CopyIcon fontSize="small" />
                            </IconButton>
                        </Tooltip>
                    )}

                    {showTryOnlineButtons && tryOnlineUrl && (
                        <Tooltip title="Try this example online">
                            <Button
                                onClick={() => handleTryOnline(currentTab.id)}
                                startIcon={<PlayIcon />}
                                endIcon={<OpenInNewIcon />}
                                variant="outlined"
                                size="small"
                                sx={{ textTransform: 'none' }}
                            >
                                Try Online
                            </Button>
                        </Tooltip>
                    )}
                </Stack>
            </Box>

            {/* Code Content */}
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
                        position: 'relative',
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
                        // Enhanced syntax highlighting
                        '& .token.comment': { color: '#64748b', fontStyle: 'italic' },
                        '& .token.keyword': { color: '#7c3aed', fontWeight: 600 },
                        '& .token.string': { color: '#059669' },
                        '& .token.number': { color: '#dc2626' },
                        '& .token.class-name': { color: '#2563eb' },
                        '& .token.function': { color: '#7c2d12' },
                        '& .token.operator': { color: '#374151' },
                        '& .token.punctuation': { color: '#6b7280' },
                        '& .token.annotation': { color: '#9333ea' },

                        // Interactive hover effects for explanations
                        ...(interactive && {
                            '& .hover-explanation': {
                                cursor: 'help',
                                borderBottom: '1px dotted #6b7280',
                                '&:hover': {
                                    bgcolor: 'rgba(99, 102, 241, 0.1)',
                                    borderRadius: '2px'
                                }
                            }
                        }),

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

            {/* Code Explanation Section */}
            {interactive && currentTab && CODE_EXPLANATIONS[currentTab.id] && (
                <Box sx={{
                    p: 2,
                    bgcolor: 'grey.50',
                    borderTop: `1px solid ${theme.palette.divider}`
                }}>
                    <Typography variant="subtitle2" sx={{ mb: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                        <InfoIcon fontSize="small" />
                        Code Explanation
                    </Typography>
                    <Stack spacing={1}>
                        {Object.entries(CODE_EXPLANATIONS[currentTab.id]).map(([key, explanation]) => (
                            <Box key={key} sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                                <Chip
                                    label={key}
                                    size="small"
                                    variant="outlined"
                                    sx={{
                                        fontFamily: 'monospace',
                                        fontSize: '0.75rem',
                                        minWidth: 'auto'
                                    }}
                                />
                                <Typography variant="body2" color="text.secondary">
                                    {explanation}
                                </Typography>
                            </Box>
                        ))}
                    </Stack>
                </Box>
            )}

            {/* Copy Success Notification */}
            <Snackbar
                open={showCopyNotification}
                autoHideDuration={2000}
                onClose={handleCloseCopyNotification}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            >
                <Alert
                    onClose={handleCloseCopyNotification}
                    severity="success"
                    sx={{ width: '100%' }}
                >
                    {copySuccess}
                </Alert>
            </Snackbar>
        </Paper>
    );
};

export default CodeTabs;
