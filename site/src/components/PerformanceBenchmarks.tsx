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
    Whatshot as WhatshotIcon,
    Bolt as BoltIcon,
    Psychology as PsychologyIcon
} from '@mui/icons-material';
import { Section } from './common';
import PageWrapper from './PageWrapper';
import Layout from './Layout';
import {
    JCACHEX_PERFORMANCE_PROFILES,
    STRESS_TEST_PERFORMANCE,
    ENDURANCE_TEST_PERFORMANCE,
    PERFORMANCE_INSIGHTS
} from '../constants/performanceComparisons';

const PerformanceBenchmarks: React.FC = () => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));
    const [currentTab, setCurrentTab] = useState(0);

    const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
        setCurrentTab(newValue);
    };

    const formatThroughput = (throughput: string): string => {
        return throughput.replace('ops/sec', 'ops/s');
    };

    const getPerformanceColor = (rating: number): string => {
        if (rating >= 5) return '#4caf50'; // Green for excellent
        if (rating >= 4) return '#ff9800'; // Orange for good
        return '#757575'; // Gray for average
    };

    const getRatingStars = (rating: number): string => {
        return '⭐'.repeat(Math.min(rating, 5));
    };

    const renderProfilesTable = () => (
        <TableContainer component={Paper} sx={{ mt: 2 }}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell><strong>Profile</strong></TableCell>
                        <TableCell><strong>Throughput</strong></TableCell>
                        <TableCell><strong>GET Latency</strong></TableCell>
                        <TableCell><strong>PUT Latency</strong></TableCell>
                        <TableCell><strong>Scaling Efficiency</strong></TableCell>
                        <TableCell><strong>Rating</strong></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {JCACHEX_PERFORMANCE_PROFILES.map((profile, index) => (
                        <TableRow key={index} sx={{ backgroundColor: profile.highlight ? '#f5f5f5' : 'inherit' }}>
                            <TableCell>
                                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                    <Typography variant="body2" sx={{ fontWeight: profile.highlight ? 'bold' : 'normal' }}>
                                        {profile.profile}
                                    </Typography>
                                    {profile.highlight && <StarIcon sx={{ ml: 1, color: '#ffc107', fontSize: 16 }} />}
                                </Box>
                            </TableCell>
                            <TableCell>
                                <Chip
                                    label={formatThroughput(profile.throughput)}
                                    color={profile.rating >= 5 ? 'success' : profile.rating >= 4 ? 'warning' : 'default'}
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>{profile.getLatency}</TableCell>
                            <TableCell>{profile.putLatency}</TableCell>
                            <TableCell>
                                <LinearProgress
                                    variant="determinate"
                                    value={parseFloat(profile.scalingEfficiency)}
                                    sx={{ width: 100, mr: 1, display: 'inline-block' }}
                                />
                                <Typography variant="body2" sx={{ display: 'inline' }}>
                                    {profile.scalingEfficiency}
                                </Typography>
                            </TableCell>
                            <TableCell>{getRatingStars(profile.rating)}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );

    const renderStressTestTable = () => (
        <TableContainer component={Paper} sx={{ mt: 2 }}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell><strong>Profile</strong></TableCell>
                        <TableCell><strong>Extreme Threads</strong></TableCell>
                        <TableCell><strong>Memory Pressure</strong></TableCell>
                        <TableCell><strong>Eviction Stress</strong></TableCell>
                        <TableCell><strong>Description</strong></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {STRESS_TEST_PERFORMANCE.map((test, index) => (
                        <TableRow key={index}>
                            <TableCell>
                                <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                                    {test.profile}
                                </Typography>
                            </TableCell>
                            <TableCell>
                                <Chip
                                    label={test.extremeThreads}
                                    color="error"
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>
                                {test.memoryPressure ? (
                                    <Chip label={test.memoryPressure} color="warning" size="small" />
                                ) : (
                                    <Typography variant="body2" color="textSecondary">N/A</Typography>
                                )}
                            </TableCell>
                            <TableCell>
                                {test.evictionStress ? (
                                    <Chip label={test.evictionStress} color="warning" size="small" />
                                ) : (
                                    <Typography variant="body2" color="textSecondary">N/A</Typography>
                                )}
                            </TableCell>
                            <TableCell>
                                <Typography variant="body2">
                                    {test.description}
                                </Typography>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );

    const renderEnduranceTestTable = () => (
        <TableContainer component={Paper} sx={{ mt: 2 }}>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell><strong>Profile</strong></TableCell>
                        <TableCell><strong>Sustained Load</strong></TableCell>
                        <TableCell><strong>GC Pressure</strong></TableCell>
                        <TableCell><strong>Memory Stability</strong></TableCell>
                        <TableCell><strong>Description</strong></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {ENDURANCE_TEST_PERFORMANCE.map((test, index) => (
                        <TableRow key={index}>
                            <TableCell>
                                <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                                    {test.profile}
                                </Typography>
                            </TableCell>
                            <TableCell>
                                <Chip
                                    label={test.sustainedLoad}
                                    color="primary"
                                    size="small"
                                />
                            </TableCell>
                            <TableCell>
                                {test.gcPressure ? (
                                    <Chip label={test.gcPressure} color="info" size="small" />
                                ) : (
                                    <Typography variant="body2" color="textSecondary">N/A</Typography>
                                )}
                            </TableCell>
                            <TableCell>
                                {test.memoryStability ? (
                                    <Chip label={test.memoryStability} color="success" size="small" />
                                ) : (
                                    <Typography variant="body2" color="textSecondary">N/A</Typography>
                                )}
                            </TableCell>
                            <TableCell>
                                <Typography variant="body2">
                                    {test.description}
                                </Typography>
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
                <Section padding="xl">
                    <Box sx={{ textAlign: 'center', mb: 4 }}>
                        <Typography variant="h2" gutterBottom>
                            <SpeedIcon sx={{ mr: 2, fontSize: 'inherit', color: 'primary.main' }} />
                            JCacheX Performance Benchmarks
                        </Typography>
                        <Typography variant="h5" color="text.secondary" sx={{ mb: 4 }}>
                            Discover the power of profile-based optimization with industry-leading performance
                        </Typography>
                    </Box>

                    <Alert severity="success" sx={{ mb: 4 }}>
                        <AlertTitle>🚀 Performance Breakthrough - July 2025</AlertTitle>
                        <Typography variant="body2">
                            <strong>JCacheX-ZeroCopy achieves 501.1M ops/sec</strong> with 98.4% scaling efficiency<br />
                            <strong>JCacheX-WriteHeavy delivers 224.6M ops/sec</strong> with 97.2% scaling efficiency<br />
                            <strong>JCacheX-HighPerformance provides 198.4M ops/sec</strong> with balanced performance across all operations
                        </Typography>
                    </Alert>
                </Section>

                <Section padding="xl">
                    <Box sx={{ mb: 4 }}>
                        <Tabs value={currentTab} onChange={handleTabChange} centered>
                            <Tab label="Profile Performance" />
                            <Tab label="Stress Testing" />
                            <Tab label="Endurance Testing" />
                            <Tab label="Performance Insights" />
                        </Tabs>
                    </Box>

                    {currentTab === 0 && (
                        <Box>
                            <Typography variant="h4" gutterBottom>
                                <BoltIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                JCacheX Profile Performance
                            </Typography>
                            <Typography variant="body1" sx={{ mb: 3 }}>
                                Each JCacheX profile is optimized for specific use cases, delivering targeted performance
                                improvements where you need them most. Our benchmarks show exceptional throughput and
                                scaling efficiency across all profiles.
                            </Typography>
                            {renderProfilesTable()}

                            <Box sx={{ mt: 4 }}>
                                <Typography variant="h5" gutterBottom>
                                    Performance Highlights
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid size={{ xs: 12, md: 4 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#e8f5e8' }}>
                                            <Typography variant="h6" color="success.main">
                                                🏆 Throughput Champion
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>JCacheX-ZeroCopy: 501.1M ops/sec</strong><br />
                                                Ultra-high throughput with minimal memory allocation overhead.
                                                Perfect for high-frequency, low-latency applications.
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                    <Grid size={{ xs: 12, md: 4 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#e3f2fd' }}>
                                            <Typography variant="h6" color="primary.main">
                                                ⚡ Scaling Excellence
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>JCacheX-WriteHeavy: 97.2% efficiency</strong><br />
                                                Near-perfect CPU utilization with excellent scaling
                                                across multiple threads for write-intensive workloads.
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                    <Grid size={{ xs: 12, md: 4 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#f3e5f5' }}>
                                            <Typography variant="h6" color="secondary.main">
                                                🎯 Balanced Performance
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>JCacheX-HighPerformance: 198.4M ops/sec</strong><br />
                                                Excellent all-around performance with 82.9% scaling efficiency
                                                for general-purpose high-performance caching.
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
                                <WhatshotIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                Stress Testing Performance
                            </Typography>
                            <Typography variant="body1" sx={{ mb: 3 }}>
                                JCacheX profiles are rigorously tested under extreme conditions including high thread
                                contention, memory pressure, and eviction stress to ensure reliable performance in
                                production environments.
                            </Typography>
                            {renderStressTestTable()}

                            <Box sx={{ mt: 4 }}>
                                <Typography variant="h5" gutterBottom>
                                    Stress Test Insights
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid size={{ xs: 12, md: 6 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#ffebee' }}>
                                            <Typography variant="h6" color="error.main">
                                                🔥 Extreme Thread Performance
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>JCacheX-HighPerformance: 1,008.6M ops/sec</strong><br />
                                                Exceptional performance under extreme thread contention,
                                                demonstrating superior concurrent data structure design.
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                    <Grid size={{ xs: 12, md: 6 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#fff3e0' }}>
                                            <Typography variant="h6" color="warning.main">
                                                💪 Multi-Condition Resilience
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>JCacheX-Default: 541.8M ops/sec</strong><br />
                                                Robust performance across multiple stress conditions
                                                including memory pressure and eviction stress scenarios.
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
                                <AccessTimeIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                Endurance Testing Performance
                            </Typography>
                            <Typography variant="body1" sx={{ mb: 3 }}>
                                Long-running endurance tests validate JCacheX's ability to maintain consistent
                                performance over extended periods, with comprehensive analysis of GC pressure
                                and memory stability.
                            </Typography>
                            {renderEnduranceTestTable()}

                            <Box sx={{ mt: 4 }}>
                                <Typography variant="h5" gutterBottom>
                                    Endurance Test Insights
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid size={{ xs: 12, md: 4 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#e8f5e8' }}>
                                            <Typography variant="h6" color="success.main">
                                                🏃 Sustained Excellence
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>20.5M ops/sec sustained load</strong><br />
                                                JCacheX-HighPerformance maintains consistent
                                                performance over extended periods without degradation.
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                    <Grid size={{ xs: 12, md: 4 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#e3f2fd' }}>
                                            <Typography variant="h6" color="primary.main">
                                                🧠 Memory Efficiency
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>JCacheX-MemoryEfficient: 20.4M ops/sec</strong><br />
                                                Excellent sustained performance with minimal
                                                GC pressure and optimal memory utilization.
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                    <Grid size={{ xs: 12, md: 4 }}>
                                        <Paper sx={{ p: 2, backgroundColor: '#f3e5f5' }}>
                                            <Typography variant="h6" color="secondary.main">
                                                🔄 Stability Assurance
                                            </Typography>
                                            <Typography variant="body2">
                                                <strong>Multi-dimensional stability</strong><br />
                                                JCacheX-Default demonstrates balanced performance
                                                across sustained load, GC pressure, and memory stability.
                                            </Typography>
                                        </Paper>
                                    </Grid>
                                </Grid>
                            </Box>
                        </Box>
                    )}

                    {currentTab === 3 && (
                        <Box>
                            <Typography variant="h4" gutterBottom>
                                <PsychologyIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                Performance Insights & Recommendations
                            </Typography>

                            <Grid container spacing={4}>
                                <Grid size={{ xs: 12, md: 6 }}>
                                    <Paper sx={{ p: 3 }}>
                                        <Typography variant="h6" gutterBottom>
                                            🏆 Category Leaders
                                        </Typography>
                                        {PERFORMANCE_INSIGHTS.topPerformers.map((performer, index) => (
                                            <Box key={index} sx={{ mb: 2 }}>
                                                <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                                                    {performer.category}
                                                </Typography>
                                                <Typography variant="body2">
                                                    <strong>{performer.profile}:</strong> {performer.achievement}
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    {performer.improvement}
                                                </Typography>
                                            </Box>
                                        ))}
                                    </Paper>
                                </Grid>

                                <Grid size={{ xs: 12, md: 6 }}>
                                    <Paper sx={{ p: 3 }}>
                                        <Typography variant="h6" gutterBottom>
                                            🎯 Optimization Opportunities
                                        </Typography>
                                        {PERFORMANCE_INSIGHTS.optimizationOpportunities.map((opportunity, index) => (
                                            <Box key={index} sx={{ mb: 2 }}>
                                                <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                                                    {opportunity.profile}
                                                </Typography>
                                                <Typography variant="body2">
                                                    Current: {opportunity.currentEfficiency} → Target: {opportunity.targetEfficiency}
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    {opportunity.improvement}
                                                </Typography>
                                            </Box>
                                        ))}
                                    </Paper>
                                </Grid>

                                <Grid size={{ xs: 12 }}>
                                    <Paper sx={{ p: 3 }}>
                                        <Typography variant="h6" gutterBottom>
                                            Benchmark Methodology
                                        </Typography>
                                        <Typography variant="body2" sx={{ mb: 2 }}>
                                            <strong>Hardware:</strong> Apple M1 Pro, 10 cores, 32GB RAM<br />
                                            <strong>Software:</strong> OpenJDK 21.0.2, macOS Darwin 24.5.0<br />
                                            <strong>Framework:</strong> JMH (Java Microbenchmark Harness)<br />
                                            <strong>Configuration:</strong> 5 warmup/10 measurement iterations, 3 forks<br />
                                            <strong>Test Date:</strong> July 14, 2025
                                        </Typography>
                                        <Divider sx={{ my: 2 }} />
                                        <Typography variant="body2">
                                            All measurements represent statistically significant results across comprehensive
                                            benchmark suites including standard operations, throughput testing, concurrent
                                            operations, hardcore stress testing, and endurance validation.
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
