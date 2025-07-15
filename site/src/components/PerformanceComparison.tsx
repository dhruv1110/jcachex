import React, { useState } from 'react';
import {
    Box,
    Typography,
    Paper,
    Stack,
    Chip,
    LinearProgress,
    Card,
    CardContent,
    Tooltip,
    Button,
    Divider,
    useTheme
} from '@mui/material';
import {
    Speed as SpeedIcon,
    TrendingUp as TrendingUpIcon,
    Memory as MemoryIcon,
    Info as InfoIcon
} from '@mui/icons-material';
import { PERFORMANCE_COMPARISONS } from '../constants/performanceComparisons';

interface PerformanceComparisonProps {
    showDetails?: boolean;
    interactive?: boolean;
}

const PerformanceComparison: React.FC<PerformanceComparisonProps> = ({
    showDetails = true,
    interactive = true
}) => {
    const [selectedMetric, setSelectedMetric] = useState<'get' | 'put' | 'throughput'>('get');
    const theme = useTheme();

    const getMetricValue = (item: typeof PERFORMANCE_COMPARISONS[0], metric: typeof selectedMetric) => {
        switch (metric) {
            case 'get':
                return parseFloat(item.getLatency.replace('ns', ''));
            case 'put':
                return parseFloat(item.putLatency.replace('ns', ''));
            case 'throughput':
                return parseFloat(item.throughput.replace(/[^\d.]/g, ''));
            default:
                return 0;
        }
    };

    const getMetricDisplay = (item: typeof PERFORMANCE_COMPARISONS[0], metric: typeof selectedMetric) => {
        switch (metric) {
            case 'get':
                return item.getLatency;
            case 'put':
                return item.putLatency;
            case 'throughput':
                return item.throughput;
            default:
                return '';
        }
    };

    const getMetricLabel = (metric: typeof selectedMetric) => {
        switch (metric) {
            case 'get':
                return 'GET Latency';
            case 'put':
                return 'PUT Latency';
            case 'throughput':
                return 'Throughput';
            default:
                return '';
        }
    };

    const getMetricIcon = (metric: typeof selectedMetric) => {
        switch (metric) {
            case 'get':
                return <SpeedIcon fontSize="small" />;
            case 'put':
                return <MemoryIcon fontSize="small" />;
            case 'throughput':
                return <TrendingUpIcon fontSize="small" />;
            default:
                return <SpeedIcon fontSize="small" />;
        }
    };

    const getBenchmarkNote = (metric: typeof selectedMetric) => {
        switch (metric) {
            case 'get':
                return 'Lower is better - measured in nanoseconds';
            case 'put':
                return 'Lower is better - measured in nanoseconds';
            case 'throughput':
                return 'Higher is better - operations per second';
            default:
                return '';
        }
    };

    const getBarColor = (library: string, index: number) => {
        if (library.includes('JCacheX')) {
            return theme.palette.primary.main;
        }
        const greyShades = [theme.palette.grey[400], theme.palette.grey[500], theme.palette.grey[600], theme.palette.grey[700]];
        return greyShades[index % greyShades.length] || theme.palette.grey[400];
    };

    const getPerformanceAdvantage = (jcacheXValue: number, otherValue: number, metric: typeof selectedMetric) => {
        if (metric === 'throughput') {
            return `${Math.round((jcacheXValue / otherValue) * 10) / 10}x`;
        } else {
            return `${Math.round((otherValue / jcacheXValue) * 10) / 10}x`;
        }
    };

    const sortedData = [...PERFORMANCE_COMPARISONS].sort((a, b) => {
        const aValue = getMetricValue(a, selectedMetric);
        const bValue = getMetricValue(b, selectedMetric);

        if (selectedMetric === 'throughput') {
            return bValue - aValue; // Higher is better
        } else {
            return aValue - bValue; // Lower is better
        }
    });

    const maxValue = Math.max(...sortedData.map(item => getMetricValue(item, selectedMetric)));
    const jcacheXBest = sortedData.find(item => item.library.includes('JCacheX'));

    return (
        <Paper elevation={2} sx={{ p: 3, borderRadius: 2 }}>
            <Box sx={{ mb: 3 }}>
                <Typography variant="h5" component="h2" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <SpeedIcon color="primary" />
                    Performance vs Alternatives
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    Benchmark results from comprehensive performance testing
                </Typography>
            </Box>

            {/* Metric Selection */}
            {interactive && (
                <Stack direction="row" spacing={1} sx={{ mb: 3 }}>
                    <Button
                        variant={selectedMetric === 'get' ? 'contained' : 'outlined'}
                        onClick={() => setSelectedMetric('get')}
                        startIcon={<SpeedIcon />}
                        size="small"
                    >
                        GET Latency
                    </Button>
                    <Button
                        variant={selectedMetric === 'put' ? 'contained' : 'outlined'}
                        onClick={() => setSelectedMetric('put')}
                        startIcon={<MemoryIcon />}
                        size="small"
                    >
                        PUT Latency
                    </Button>
                    <Button
                        variant={selectedMetric === 'throughput' ? 'contained' : 'outlined'}
                        onClick={() => setSelectedMetric('throughput')}
                        startIcon={<TrendingUpIcon />}
                        size="small"
                    >
                        Throughput
                    </Button>
                </Stack>
            )}

            {/* Performance Chart */}
            <Box sx={{ mb: 3 }}>
                <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {getMetricIcon(selectedMetric)}
                    {getMetricLabel(selectedMetric)} Comparison
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    {getBenchmarkNote(selectedMetric)}
                </Typography>

                <Stack spacing={2}>
                    {sortedData.map((item, index) => {
                        const value = getMetricValue(item, selectedMetric);
                        const percentage = selectedMetric === 'throughput'
                            ? (value / maxValue) * 100
                            : (maxValue / value) * 100;

                        const isJCacheX = item.library.includes('JCacheX');
                        const advantage = jcacheXBest && !isJCacheX
                            ? getPerformanceAdvantage(getMetricValue(jcacheXBest, selectedMetric), value, selectedMetric)
                            : null;

                        return (
                            <Box key={item.library}>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                        <Typography variant="body2" fontWeight={isJCacheX ? 600 : 400}>
                                            {item.library}
                                        </Typography>
                                        {isJCacheX && (
                                            <Chip
                                                label="Best"
                                                size="small"
                                                color="primary"
                                                sx={{ height: 20 }}
                                            />
                                        )}
                                        {advantage && (
                                            <Chip
                                                label={`${advantage} faster`}
                                                size="small"
                                                color="success"
                                                variant="outlined"
                                                sx={{ height: 20 }}
                                            />
                                        )}
                                    </Box>
                                    <Typography variant="body2" fontWeight={600}>
                                        {getMetricDisplay(item, selectedMetric)}
                                    </Typography>
                                </Box>
                                <LinearProgress
                                    variant="determinate"
                                    value={percentage}
                                    sx={{
                                        height: 8,
                                        borderRadius: 4,
                                        bgcolor: 'grey.200',
                                        '& .MuiLinearProgress-bar': {
                                            bgcolor: getBarColor(item.library, index),
                                            borderRadius: 4
                                        }
                                    }}
                                />
                            </Box>
                        );
                    })}
                </Stack>
            </Box>

            {/* Performance Highlights */}
            {showDetails && (
                <>
                    <Divider sx={{ my: 3 }} />
                    <Typography variant="h6" gutterBottom>
                        Performance Highlights
                    </Typography>
                    <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
                        <Card variant="outlined" sx={{ flex: 1 }}>
                            <CardContent>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                    <SpeedIcon color="primary" fontSize="small" />
                                    <Typography variant="subtitle2">Ultra-Low Latency</Typography>
                                </Box>
                                <Typography variant="h6" color="primary">7.9ns</Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Zero-copy GET operations with lock-free algorithms
                                </Typography>
                            </CardContent>
                        </Card>
                        <Card variant="outlined" sx={{ flex: 1 }}>
                            <CardContent>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                    <TrendingUpIcon color="primary" fontSize="small" />
                                    <Typography variant="subtitle2">High Throughput</Typography>
                                </Box>
                                <Typography variant="h6" color="primary">50M+ ops/sec</Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Sustained operations per second under load
                                </Typography>
                            </CardContent>
                        </Card>
                        <Card variant="outlined" sx={{ flex: 1 }}>
                            <CardContent>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                    <MemoryIcon color="primary" fontSize="small" />
                                    <Typography variant="subtitle2">Memory Efficient</Typography>
                                </Box>
                                <Typography variant="h6" color="primary">Minimal GC</Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Optimized allocation patterns reduce pressure
                                </Typography>
                            </CardContent>
                        </Card>
                    </Stack>
                </>
            )}

            {/* Benchmark Information */}
            <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                <Typography variant="body2" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <InfoIcon fontSize="small" />
                    Benchmark Environment: Intel Core i7-9750H @ 2.6GHz, 32GB RAM, OpenJDK 17
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    All tests use equivalent configurations and workloads. Results are averaged over 1M+ operations.
                </Typography>
            </Box>
        </Paper>
    );
};

export default PerformanceComparison;
