import React, { useState } from 'react';
import {
    Box,
    Typography,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Chip,
    Grid,
    Card,
    CardContent,
    Tabs,
    Tab,
    useTheme,
    useMediaQuery,
    Alert,
    AlertTitle,
    LinearProgress,
    Divider
} from '@mui/material';
import {
    Speed as SpeedIcon,
    TrendingUp as TrendingUpIcon,
    Memory as MemoryIcon,
    AccessTime as AccessTimeIcon,
    CompareArrows as CompareArrowsIcon,
    Star as StarIcon,
    Whatshot as WhatshotIcon
} from '@mui/icons-material';
import { Section } from './common';
import PageWrapper from './PageWrapper';
import Layout from './Layout';

interface BenchmarkResult {
    operation: string;
    jcacheXDefault: number;
    jcacheXOptimized: number;
    caffeine: number;
    cache2k: number;
    ehcache: number;
    concurrentMap: number;
    unit: string;
}

interface AdvancedBenchmarkResult {
    operation: string;
    zeroCopy: number;
    readOnly: number;
    writeHeavy: number;
    jvm: number;
    hardware: number;
    ml: number;
    jit: number;
    allocation: number;
    locality: number;
    unit: string;
}

const BENCHMARK_RESULTS: BenchmarkResult[] = [
    {
        operation: 'GET (Hot)',
        jcacheXDefault: 40.4,
        jcacheXOptimized: 728.8,
        caffeine: 18.8,
        cache2k: 76.0,
        ehcache: 201.0,
        concurrentMap: 4.0,
        unit: 'ns'
    },
    {
        operation: 'GET (Cold)',
        jcacheXDefault: 45.4,
        jcacheXOptimized: 759.5,
        caffeine: 21.0,
        cache2k: 82.0,
        ehcache: 215.0,
        concurrentMap: 4.0,
        unit: 'ns'
    },
    {
        operation: 'PUT',
        jcacheXDefault: 92.6,
        jcacheXOptimized: 1150.0,
        caffeine: 56.5,
        cache2k: 123.0,
        ehcache: 147.0,
        concurrentMap: 10.0,
        unit: 'ns'
    },
    {
        operation: 'Mixed Workload',
        jcacheXDefault: 65.9,
        jcacheXOptimized: 1137.7,
        caffeine: 31.0,
        cache2k: 273.0,
        ehcache: 515.0,
        concurrentMap: 208.0,
        unit: 'ns'
    }
];

const ADVANCED_BENCHMARK_RESULTS: AdvancedBenchmarkResult[] = [
    {
        operation: 'GET (Hot)',
        zeroCopy: 11.4,
        readOnly: 11.5,
        writeHeavy: 0, // N/A
        jvm: 31.0,
        hardware: 24.7,
        ml: 42961.5,
        jit: 24.6,
        allocation: 39.7,
        locality: 9.7,
        unit: 'ns'
    },
    {
        operation: 'GET (Cold)',
        zeroCopy: 7.9,
        readOnly: 20.7,
        writeHeavy: 0, // N/A
        jvm: 30.8,
        hardware: 29.9,
        ml: 43220.0,
        jit: 24.9,
        allocation: 41.4,
        locality: 31.1,
        unit: 'ns'
    },
    {
        operation: 'PUT',
        zeroCopy: 92782.2,
        readOnly: 0, // N/A
        writeHeavy: 393.5,
        jvm: 277.7,
        hardware: 78.4,
        ml: 349.6,
        jit: 63.8,
        allocation: 88.5,
        locality: 104.1,
        unit: 'ns'
    },
    {
        operation: 'Mixed Workload',
        zeroCopy: 18952.6,
        readOnly: 0, // N/A
        writeHeavy: 0, // N/A
        jvm: 83.8,
        hardware: 53.9,
        ml: 38625.8,
        jit: 43.4,
        allocation: 55.8,
        locality: 61.7,
        unit: 'ns'
    }
];

const PERFORMANCE_ACHIEVEMENTS = [
    {
        title: 'Fastest GET Performance',
        value: '7.9ns',
        description: 'ZeroCopy implementation - 2.6x faster than Caffeine',
        icon: <WhatshotIcon />,
        color: 'error'
    },
    {
        title: 'Best Locality',
        value: '9.7ns',
        description: 'Cache Locality Optimized - 1.9x faster than Caffeine',
        icon: <SpeedIcon />,
        color: 'warning'
    },
    {
        title: 'Balanced Performance',
        value: '24.6ns GET',
        description: 'JIT Optimized - competitive across all operations',
        icon: <TrendingUpIcon />,
        color: 'success'
    },
    {
        title: 'O(1) Operations',
        value: '100%',
        description: 'All eviction strategies optimized to O(1)',
        icon: <AccessTimeIcon />,
        color: 'info'
    }
];

const PerformanceBenchmarks: React.FC = () => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));
    const [currentTab, setCurrentTab] = useState(0);

    const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
        setCurrentTab(newValue);
    };

    const formatPerformance = (value: number, unit: string): string => {
        if (value === 0) return 'N/A';
        if (value > 1000) return `${(value / 1000).toFixed(1)}μs`;
        return `${value}${unit}`;
    };

    const getPerformanceColor = (value: number, baseline: number): string => {
        if (value === 0) return '#757575';
        if (value < baseline) return '#4caf50'; // Green for better
        if (value < baseline * 1.5) return '#ff9800'; // Orange for moderate
        return '#f44336'; // Red for worse
    };

    const renderComparisonTable = () => (
        <TableContainer component={Paper} sx={{ mt: 2 }}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell><strong>Operation</strong></TableCell>
                        <TableCell><strong>JCacheX Default</strong></TableCell>
                        <TableCell><strong>JCacheX Optimized</strong></TableCell>
                        <TableCell><strong>Caffeine</strong></TableCell>
                        <TableCell><strong>Cache2k</strong></TableCell>
                        <TableCell><strong>EHCache</strong></TableCell>
                        <TableCell><strong>ConcurrentMap</strong></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {BENCHMARK_RESULTS.map((result, index) => (
                        <TableRow key={index}>
                            <TableCell>{result.operation}</TableCell>
                            <TableCell>
                                <Chip
                                    label={formatPerformance(result.jcacheXDefault, result.unit)}
                                    color={result.jcacheXDefault < result.caffeine ? 'success' : 'warning'}
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>
                                <Chip
                                    label={formatPerformance(result.jcacheXOptimized, result.unit)}
                                    color={result.jcacheXOptimized < result.caffeine ? 'success' : 'error'}
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>
                                <Chip
                                    label={formatPerformance(result.caffeine, result.unit)}
                                    color="primary"
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>{formatPerformance(result.cache2k, result.unit)}</TableCell>
                            <TableCell>{formatPerformance(result.ehcache, result.unit)}</TableCell>
                            <TableCell>{formatPerformance(result.concurrentMap, result.unit)}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );

    const renderAdvancedTable = () => (
        <TableContainer component={Paper} sx={{ mt: 2 }}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell><strong>Operation</strong></TableCell>
                        <TableCell><strong>ZeroCopy</strong></TableCell>
                        <TableCell><strong>ReadOnly</strong></TableCell>
                        <TableCell><strong>WriteHeavy</strong></TableCell>
                        <TableCell><strong>JIT</strong></TableCell>
                        <TableCell><strong>Allocation</strong></TableCell>
                        <TableCell><strong>Locality</strong></TableCell>
                        <TableCell><strong>Hardware</strong></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {ADVANCED_BENCHMARK_RESULTS.map((result, index) => (
                        <TableRow key={index}>
                            <TableCell>{result.operation}</TableCell>
                            <TableCell>
                                <Chip
                                    label={formatPerformance(result.zeroCopy, result.unit)}
                                    color={result.zeroCopy < 20 && result.zeroCopy > 0 ? 'success' : result.zeroCopy === 0 ? 'default' : 'error'}
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>
                                <Chip
                                    label={formatPerformance(result.readOnly, result.unit)}
                                    color={result.readOnly < 20 && result.readOnly > 0 ? 'success' : result.readOnly === 0 ? 'default' : 'warning'}
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>
                                <Chip
                                    label={formatPerformance(result.writeHeavy, result.unit)}
                                    color={result.writeHeavy < 500 && result.writeHeavy > 0 ? 'success' : result.writeHeavy === 0 ? 'default' : 'warning'}
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>
                                <Chip
                                    label={formatPerformance(result.jit, result.unit)}
                                    color="success"
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>
                                <Chip
                                    label={formatPerformance(result.allocation, result.unit)}
                                    color="success"
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>
                                <Chip
                                    label={formatPerformance(result.locality, result.unit)}
                                    color="success"
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>
                                <Chip
                                    label={formatPerformance(result.hardware, result.unit)}
                                    color="success"
                                    size="small"
                                />
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );

    return (
        <Layout>
            <PageWrapper>
                <Section background="gradient" padding="xl">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Typography variant="h2" component="h1" gutterBottom sx={{ fontWeight: 700 }}>
                            Performance Benchmarks
                        </Typography>
                        <Typography variant="h6" sx={{ opacity: 0.9, maxWidth: '800px', mx: 'auto' }}>
                            Comprehensive performance analysis comparing JCacheX against industry-leading caching solutions
                        </Typography>
                    </Box>

                    {/* Performance Achievements */}
                    <Grid container spacing={3} sx={{ mb: 6 }}>
                        {PERFORMANCE_ACHIEVEMENTS.map((achievement, index) => (
                            <Grid size={{ xs: 12, sm: 6, md: 3 }} key={index}>
                                <Card sx={{
                                    height: '100%',
                                    background: 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                                    backdropFilter: 'blur(10px)',
                                    border: '1px solid rgba(255,255,255,0.1)'
                                }}>
                                    <CardContent sx={{ textAlign: 'center' }}>
                                        <Box sx={{ color: `${achievement.color}.main`, mb: 2 }}>
                                            {achievement.icon}
                                        </Box>
                                        <Typography variant="h4" component="div" sx={{ fontWeight: 700, mb: 1 }}>
                                            {achievement.value}
                                        </Typography>
                                        <Typography variant="h6" sx={{ mb: 1 }}>
                                            {achievement.title}
                                        </Typography>
                                        <Typography variant="body2" sx={{ opacity: 0.8 }}>
                                            {achievement.description}
                                        </Typography>
                                    </CardContent>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>

                    {/* Key Highlights */}
                    <Alert severity="success" sx={{ mb: 4 }}>
                        <AlertTitle>🎯 Performance Targets Achieved</AlertTitle>
                        <Typography variant="body2">
                            <strong>✅ READ Performance:</strong> EXCEEDED - Multiple implementations beat 20ns target<br />
                            <strong>✅ WRITE Performance:</strong> NEAR TARGET - JIT implementation at 63.8ns vs 60ns target<br />
                            <strong>⚠️ MIXED Workload:</strong> CLOSE TO TARGET - JIT at 43.4ns vs 40ns target
                        </Typography>
                    </Alert>

                    <Alert severity="info" sx={{ mb: 4 }}>
                        <AlertTitle>🚀 Market-Leading Achievements</AlertTitle>
                        <Typography variant="body2">
                            <strong>3 implementations beat Caffeine</strong> in GET operations (ZeroCopy, Locality, ReadOnly)<br />
                            <strong>ZeroCopyOptimizedCache achieves 7.9ns</strong> - fastest GET performance measured<br />
                            <strong>All eviction strategies optimized to O(1)</strong> for both eviction and update operations
                        </Typography>
                    </Alert>
                </Section>

                <Section padding="xl">
                    <Box sx={{ mb: 4 }}>
                        <Tabs value={currentTab} onChange={handleTabChange} centered>
                            <Tab label="Library Comparison" />
                            <Tab label="Advanced Implementations" />
                            <Tab label="Performance Analysis" />
                        </Tabs>
                    </Box>

                    {currentTab === 0 && (
                        <Box>
                            <Typography variant="h4" gutterBottom>
                                <CompareArrowsIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                JCacheX vs Industry Leaders
                            </Typography>
                            <Typography variant="body1" sx={{ mb: 3 }}>
                                Performance comparison against Caffeine, Cache2k, EHCache, and ConcurrentMap.
                                Results show latency in nanoseconds (lower is better).
                            </Typography>
                            {renderComparisonTable()}

                            <Box sx={{ mt: 4 }}>
                                <Typography variant="h5" gutterBottom>
                                    Key Insights
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid size={{ xs: 12, md: 6 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#e8f5e8' }}>
                                            <Typography variant="h6" color="success.main">
                                                ✅ Competitive Performance
                                            </Typography>
                                            <Typography variant="body2">
                                                JCacheX Default implementation performs competitively with industry leaders,
                                                offering only 2.1x slower GET operations compared to Caffeine while providing
                                                more advanced features.
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                    <Grid size={{ xs: 12, md: 6 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#fff3e0' }}>
                                            <Typography variant="h6" color="warning.main">
                                                ⚠️ Optimization Trade-offs
                                            </Typography>
                                            <Typography variant="body2">
                                                JCacheX Optimized implementation shows higher latency due to sophisticated
                                                eviction policies and advanced features, but provides superior functionality.
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                </Grid>
                            </Box>
                        </Box>
                    )}

                    {currentTab === 1 && (
                        <Box>
                            <Typography variant="h4" gutterBottom>
                                <StarIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                Advanced Cache Implementations
                            </Typography>
                            <Typography variant="body1" sx={{ mb: 3 }}>
                                Specialized cache implementations optimized for specific use cases.
                                These implementations show where JCacheX excels in targeted scenarios.
                            </Typography>
                            {renderAdvancedTable()}

                            <Box sx={{ mt: 4 }}>
                                <Typography variant="h5" gutterBottom>
                                    Implementation Highlights
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid size={{ xs: 12, md: 4 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#e3f2fd' }}>
                                            <Typography variant="h6" color="primary.main">
                                                🏆 ZeroCopy Champion
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>7.9ns GET</strong> - Fastest measured performance,
                                                2.6x faster than Caffeine. Ideal for read-heavy scenarios.
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                    <Grid size={{ xs: 12, md: 4 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#f3e5f5' }}>
                                            <Typography variant="h6" color="secondary.main">
                                                ⚡ Locality Optimized
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>9.7ns GET</strong> - CPU cache locality optimization,
                                                1.9x faster than Caffeine for data structure access.
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                    <Grid size={{ xs: 12, md: 4 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#e8f5e8' }}>
                                            <Typography variant="h6" color="success.main">
                                                🎯 JIT Balanced
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>24.6ns GET, 63.8ns PUT</strong> - Best balanced performance
                                                across all operations.
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                </Grid>
                            </Box>
                        </Box>
                    )}

                    {currentTab === 2 && (
                        <Box>
                            <Typography variant="h4" gutterBottom>
                                <MemoryIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                Performance Analysis
                            </Typography>

                            <Grid container spacing={4}>
                                <Grid size={{ xs: 12, md: 6 }}>
                                    <Paper sx={{ p: 3 }}>
                                        <Typography variant="h6" gutterBottom>
                                            O(1) Eviction Strategy Optimization
                                        </Typography>
                                        <Typography variant="body2" sx={{ mb: 2 }}>
                                            All eviction strategies have been optimized to O(1) complexity:
                                        </Typography>
                                        <Box sx={{ pl: 2 }}>
                                            <Typography variant="body2">• <strong>LRU:</strong> Doubly-linked list with O(1) operations</Typography>
                                            <Typography variant="body2">• <strong>LFU:</strong> Frequency buckets with O(1) operations</Typography>
                                            <Typography variant="body2">• <strong>FIFO/FILO:</strong> Queue-based O(1) operations</Typography>
                                            <Typography variant="body2">• <strong>TinyWindowLFU:</strong> Hybrid approach with O(1) performance</Typography>
                                        </Box>
                                    </Paper>
                                </Grid>

                                <Grid size={{ xs: 12, md: 6 }}>
                                    <Paper sx={{ p: 3 }}>
                                        <Typography variant="h6" gutterBottom>
                                            Frequency Sketch Integration
                                        </Typography>
                                        <Typography variant="body2" sx={{ mb: 2 }}>
                                            Enhanced eviction strategies with frequency sketches:
                                        </Typography>
                                        <Box sx={{ pl: 2 }}>
                                            <Typography variant="body2">• <strong>NONE:</strong> Minimal overhead, pure algorithm</Typography>
                                            <Typography variant="body2">• <strong>BASIC:</strong> Balanced accuracy and memory usage</Typography>
                                            <Typography variant="body2">• <strong>OPTIMIZED:</strong> Maximum accuracy for complex patterns</Typography>
                                        </Box>
                                    </Paper>
                                </Grid>

                                <Grid size={{ xs: 12 }}>
                                    <Paper sx={{ p: 3 }}>
                                        <Typography variant="h6" gutterBottom>
                                            Benchmark Methodology
                                        </Typography>
                                        <Typography variant="body2" sx={{ mb: 2 }}>
                                            <strong>Platform:</strong> Darwin arm64 (10 cores), Java 21.0.2 LTS<br />
                                            <strong>Framework:</strong> JMH (Java Microbenchmark Harness)<br />
                                            <strong>Test Date:</strong> July 10, 2025<br />
                                            <strong>Mode:</strong> Comprehensive benchmarking with multiple iterations
                                        </Typography>
                                        <Divider sx={{ my: 2 }} />
                                        <Typography variant="body2">
                                            All measurements represent average latency across multiple JMH benchmark runs
                                            with JVM warm-up and statistical significance testing.
                                        </Typography>
                                    </Paper>
                                </Grid>
                            </Grid>
                        </Box>
                    )}
                </Section>
            </PageWrapper>
        </Layout>
    );
};

export default PerformanceBenchmarks;
