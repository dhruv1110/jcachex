import React, { useState } from 'react';
import {
    Box,
    Container,
    Typography,
    Paper,
    Stack,
    Accordion,
    AccordionSummary,
    AccordionDetails,
    Alert,
    Button,
    Card,
    CardContent,
    Chip,
    Divider,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    TextField,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Tabs,
    Tab,
    LinearProgress
} from '@mui/material';
import {
    ExpandMore as ExpandMoreIcon,
    Warning as WarningIcon,
    CheckCircle as CheckIcon,
    Error as ErrorIcon,
    Info as InfoIcon,
    Speed as SpeedIcon,
    Memory as MemoryIcon,
    Troubleshoot as TroubleshootIcon,
    TrendingUp as TrendingUpIcon,
    MonitorHeart as MonitorIcon,
    BugReport as BugIcon,
    Settings as SettingsIcon,
    Analytics as AnalyticsIcon
} from '@mui/icons-material';
import CodeTabs from './CodeTabs';
import Layout from './Layout';

interface TroubleshootingGuideProps {
    showDiagnostics?: boolean;
    interactive?: boolean;
}

const TroubleshootingGuide: React.FC<TroubleshootingGuideProps> = ({
    showDiagnostics = true,
    interactive = true
}) => {
    const [selectedTab, setSelectedTab] = useState(0);
    const [hitRate, setHitRate] = useState<number>(75);
    const [evictionRate, setEvictionRate] = useState<number>(10);
    const [memoryUsage, setMemoryUsage] = useState<number>(60);

    // Diagnostic flowchart data
    const diagnosticFlowchart = {
        performanceIssues: [
            {
                condition: 'Hit rate < 80%',
                solutions: [
                    'Switch to READ_HEAVY profile for optimized read performance (501.1M ops/sec)',
                    'Increase cache size if memory allows - use JCacheXBuilder.forReadHeavyWorkload()',
                    'Review TTL settings - extend expireAfterAccess for frequently accessed data',
                    'Analyze access patterns - consider ZERO_COPY profile for ultra-performance'
                ]
            },
            {
                condition: 'High write latency (>100ms)',
                solutions: [
                    'Switch to WRITE_HEAVY profile for optimized writes (224.6M ops/sec)',
                    'Use JCacheXBuilder.forWriteHeavyWorkload() for frequent updates',
                    'Consider reducing cache size to minimize eviction overhead',
                    'Monitor eviction patterns using built-in statistics'
                ]
            },
            {
                condition: 'High eviction frequency',
                solutions: [
                    'Check if cache size is appropriate for working set',
                    'Use MEMORY_EFFICIENT profile for constrained environments',
                    'Consider JCacheXBuilder.fromProfile(ProfileName.MEMORY_EFFICIENT)',
                    'Analyze eviction reasons in logs and metrics'
                ]
            },
            {
                condition: 'High latency operations (>10ms)',
                solutions: [
                    'Switch to HIGH_PERFORMANCE profile for ultra-low latency',
                    'Use JCacheXBuilder.forHighPerformance() for critical performance',
                    'Consider ZERO_COPY profile for maximum speed (501.1M ops/sec)',
                    'Check for lock contention in concurrent scenarios'
                ]
            }
        ],
        memoryIssues: [
            {
                condition: 'Cache size > 25% of heap',
                solutions: [
                    'Use MEMORY_EFFICIENT profile to reduce memory footprint',
                    'Switch to JCacheXBuilder.fromProfile(ProfileName.MEMORY_EFFICIENT)',
                    'Consider reducing cache maximum size',
                    'Monitor heap usage patterns and GC activity'
                ]
            },
            {
                condition: 'Memory leaks detected',
                solutions: [
                    'Implement proper eviction listeners using .evictionListener()',
                    'Review cache configuration for proper TTL settings',
                    'Use profiling tools (JProfiler, VisualVM) to identify retention',
                    'Monitor cache growth over time with built-in statistics'
                ]
            },
            {
                condition: 'High GC pressure',
                solutions: [
                    'Switch to MEMORY_EFFICIENT profile for reduced allocations',
                    'Use shorter TTL values to promote faster eviction',
                    'Consider profile-based optimization for your workload pattern',
                    'Monitor cache statistics for eviction effectiveness'
                ]
            }
        ]
    };

    // Common issues and solutions
    const commonIssues = [
        {
            category: 'Performance',
            icon: <SpeedIcon />,
            issues: [
                {
                    problem: 'Slow cache operations',
                    symptoms: ['High latency', 'Thread contention', 'Poor throughput'],
                    solution: 'Switch to HIGH_PERFORMANCE profile for ultra-low latency',
                    code: `// Modern JCacheXBuilder with HIGH_PERFORMANCE profile
Cache<String, Data> cache = JCacheXBuilder
    .forHighPerformance()
    .maximumSize(100000)
    .expireAfterWrite(Duration.ofHours(1))
    .recordStats(false)      // Disable stats for max performance
    .build();

// Or use ZeroCopy for ultimate performance (501.1M ops/sec)
Cache<String, Data> zeroCopyCache = JCacheXBuilder
    .fromProfile(ProfileName.ZERO_COPY)
    .maximumSize(50000)
    .build();`
                },
                {
                    problem: 'Low hit rate',
                    symptoms: ['Frequent cache misses', 'High database load', 'Poor response times'],
                    solution: 'Use READ_HEAVY profile with optimized TTL settings',
                    code: `// READ_HEAVY profile optimized for high hit rates
Cache<String, Data> cache = JCacheXBuilder
    .forReadHeavyWorkload()  // Optimized for frequent reads
    .maximumSize(50000)
    .expireAfterWrite(Duration.ofHours(2))
    .expireAfterAccess(Duration.ofHours(4))  // Keep hot data longer
    .recordStats(true)       // Monitor hit rates
    .build();

// Alternative: Use one-liner for quick setup
Cache<String, Data> quickCache = JCacheXBuilder
    .createReadHeavyCache("data", 50000);`
                },
                {
                    problem: 'High write latency',
                    symptoms: ['Slow PUT operations', 'Write contention', 'Eviction delays'],
                    solution: 'Use WRITE_HEAVY profile optimized for frequent updates',
                    code: `// WRITE_HEAVY profile for frequent updates (224.6M ops/sec)
Cache<String, Data> cache = JCacheXBuilder
    .forWriteHeavyWorkload()  // Optimized for frequent writes
    .maximumSize(25000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .recordStats(true)
    .build();

// Alternative: Use convenience method
Cache<String, Data> writeCache = JCacheXBuilder
    .createWriteHeavyCache("counters", 25000);`
                }
            ]
        },
        {
            category: 'Memory',
            icon: <MemoryIcon />,
            issues: [
                {
                    problem: 'OutOfMemoryError',
                    symptoms: ['Heap exhaustion', 'GC pressure', 'Application crashes'],
                    solution: 'Use MEMORY_EFFICIENT profile with appropriate size limits',
                    code: `// MEMORY_EFFICIENT profile for constrained environments
Cache<String, Data> cache = JCacheXBuilder
    .fromProfile(ProfileName.MEMORY_EFFICIENT)
    .maximumSize(10000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .recordStats(true)
    .build();

// Or use manual memory optimization
Cache<String, Data> optimizedCache = JCacheXBuilder
    .forReadHeavyWorkload()
    .maximumSize(5000)      // Smaller cache size
    .expireAfterWrite(Duration.ofMinutes(15))  // Shorter TTL
    .build();`
                },
                {
                    problem: 'Memory leaks',
                    symptoms: ['Growing heap usage', 'Cache not releasing memory', 'GC thrashing'],
                    solution: 'Implement proper eviction listeners and monitoring',
                    code: `// Proper eviction monitoring with modern JCacheXBuilder
Cache<String, Data> cache = JCacheXBuilder
    .forReadHeavyWorkload()
    .maximumSize(20000)
    .expireAfterWrite(Duration.ofHours(1))
    .evictionListener(this::onEviction)
    .recordStats(true)
    .build();

private void onEviction(String key, Data data, RemovalCause cause) {
    log.info("Evicted {} due to {}", key, cause);
    // Monitor eviction patterns
    if (cause == RemovalCause.SIZE) {
        metrics.counter("cache.eviction.size").increment();
    } else if (cause == RemovalCause.EXPIRED) {
        metrics.counter("cache.eviction.expired").increment();
    }
}`
                }
            ]
        },
        {
            category: 'Configuration',
            icon: <SettingsIcon />,
            issues: [
                {
                    problem: 'Wrong profile selection',
                    symptoms: ['Suboptimal performance', 'Resource waste', 'Inconsistent behavior'],
                    solution: 'Choose profile based on access patterns and requirements',
                    code: `// Profile selection guide based on workload patterns

// For read-heavy workloads (80%+ reads) - 501.1M ops/sec
Cache<String, Data> readCache = JCacheXBuilder
    .forReadHeavyWorkload()
    .maximumSize(50000)
    .build();

// For write-heavy workloads (50%+ writes) - 224.6M ops/sec
Cache<String, Data> writeCache = JCacheXBuilder
    .forWriteHeavyWorkload()
    .maximumSize(25000)
    .build();

// For ultra-low latency requirements
Cache<String, Data> performanceCache = JCacheXBuilder
    .forHighPerformance()
    .maximumSize(100000)
    .build();

// For memory-constrained environments
Cache<String, Data> memoryCache = JCacheXBuilder
    .fromProfile(ProfileName.MEMORY_EFFICIENT)
    .maximumSize(10000)
    .build();`
                },
                {
                    problem: 'Profile migration issues',
                    symptoms: ['Performance degradation', 'Configuration errors', 'Unexpected behavior'],
                    solution: 'Migrate from legacy builders to modern JCacheXBuilder patterns',
                    code: `// BEFORE: Legacy builder pattern
// CacheBuilder.newBuilder()
//     .maximumSize(1000L)
//     .expireAfterWrite(Duration.ofMinutes(30))
//     .build();

// AFTER: Modern JCacheXBuilder with profiles
Cache<String, Data> modernCache = JCacheXBuilder
    .forReadHeavyWorkload()  // Choose appropriate profile
    .maximumSize(1000)       // Note: no 'L' suffix needed
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();

// Use type-safe profile enum
Cache<String, Data> typeSafeCache = JCacheXBuilder
    .fromProfile(ProfileName.READ_HEAVY)
    .maximumSize(1000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();`
                }
            ]
        }
    ];

    // Monitoring setup examples
    const monitoringExamples = [
        {
            id: 'basic-monitoring',
            label: 'Basic Monitoring',
            language: 'java',
            code: `@Component
public class CacheMonitoring {
    private final MeterRegistry meterRegistry;

    public CacheMonitoring(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @EventListener
    public void handleCacheStats(CacheStatsEvent event) {
        CacheStats stats = event.getStats();
        String cacheName = event.getCacheName();

        // Log key metrics
        log.info("Cache {}: hit_rate={:.2f}%, evictions={}, load_time={:.2f}ms",
            cacheName,
            stats.hitRate() * 100,
            stats.evictionCount(),
            stats.averageLoadPenalty() / 1_000_000.0);

        // Send to monitoring system
        Metrics.gauge("cache.hit_rate",
            Tags.of("cache", cacheName), stats.hitRate());
        Metrics.counter("cache.evictions",
            Tags.of("cache", cacheName)).increment(stats.evictionCount());
        Metrics.timer("cache.load_time",
            Tags.of("cache", cacheName)).record(stats.averageLoadPenalty(), TimeUnit.NANOSECONDS);
    }
}`
        },
        {
            id: 'advanced-monitoring',
            label: 'Advanced Monitoring',
            language: 'java',
            code: `@Configuration
public class CacheMonitoringConfig {

    @Bean
    public CacheHealthIndicator cacheHealthIndicator(
            @Qualifier("userCache") Cache<String, User> userCache) {
        return new CacheHealthIndicator(userCache);
    }

    @Bean
    public MeterFilter cacheMetricsFilter() {
        return MeterFilter.maximumExpectedValue("cache.load_time",
            Duration.ofSeconds(1));
    }
}

@Component
public class CacheHealthIndicator implements HealthIndicator {
    private final Cache<String, User> cache;

    public CacheHealthIndicator(Cache<String, User> cache) {
        this.cache = cache;
    }

    @Override
    public Health health() {
        CacheStats stats = cache.stats();

        Health.Builder builder = stats.hitRate() > 0.7
            ? Health.up()
            : Health.down();

        return builder
            .withDetail("hitRate", stats.hitRate())
            .withDetail("size", cache.size())
            .withDetail("evictions", stats.evictionCount())
            .withDetail("averageLoadTime", stats.averageLoadPenalty() / 1_000_000.0)
            .build();
    }
}`
        },
        {
            id: 'alerting',
            label: 'Alerting Setup',
            language: 'java',
            code: `@Component
public class CacheAlerting {
    private final AlertingService alertingService;
    private final MeterRegistry meterRegistry;

    @EventListener
    public void checkCacheHealth(CacheStatsEvent event) {
        CacheStats stats = event.getStats();
        String cacheName = event.getCacheName();

        // Alert on low hit rate
        if (stats.hitRate() < 0.7) {
            alertingService.sendAlert(AlertLevel.WARNING,
                "Cache hit rate below threshold",
                Map.of(
                    "cache", cacheName,
                    "hitRate", stats.hitRate(),
                    "threshold", 0.7
                ));
        }

        // Alert on high eviction rate
        if (stats.evictionCount() > 1000) {
            alertingService.sendAlert(AlertLevel.INFO,
                "High cache eviction rate",
                Map.of(
                    "cache", cacheName,
                    "evictions", stats.evictionCount()
                ));
        }

        // Alert on slow load times
        if (stats.averageLoadPenalty() > 10_000_000) { // 10ms
            alertingService.sendAlert(AlertLevel.WARNING,
                "Slow cache load times",
                Map.of(
                    "cache", cacheName,
                    "loadTime", stats.averageLoadPenalty() / 1_000_000.0
                ));
        }
    }
}`
        }
    ];

    // Performance diagnostic based on user inputs
    const getDiagnosticRecommendations = () => {
        const recommendations = [];

        if (hitRate < 80) {
            recommendations.push({
                severity: 'warning',
                title: 'Low Hit Rate',
                description: 'Consider increasing cache size or reviewing TTL settings',
                action: 'Switch to READ_HEAVY profile for better hit rates'
            });
        }

        if (evictionRate > 15) {
            recommendations.push({
                severity: 'warning',
                title: 'High Eviction Rate',
                description: 'Cache may be too small for working set',
                action: 'Increase cache size or use MEMORY_EFFICIENT profile'
            });
        }

        if (memoryUsage > 80) {
            recommendations.push({
                severity: 'error',
                title: 'High Memory Usage',
                description: 'Cache is consuming too much memory',
                action: 'Reduce cache size or enable soft values'
            });
        }

        if (recommendations.length === 0) {
            recommendations.push({
                severity: 'success',
                title: 'Cache Performance Looks Good',
                description: 'All metrics are within acceptable ranges',
                action: 'Continue monitoring for any changes'
            });
        }

        return recommendations;
    };

    const navigationItems = [
        {
            id: 'troubleshooting-issues',
            title: 'Common Issues',
            icon: <BugIcon />,
            children: [
                { id: 'troubleshooting-performance', title: 'Performance', icon: <SpeedIcon /> },
                { id: 'troubleshooting-memory', title: 'Memory', icon: <MemoryIcon /> },
                { id: 'troubleshooting-configuration', title: 'Configuration', icon: <SettingsIcon /> }
            ]
        },
        {
            id: 'troubleshooting-diagnostics',
            title: 'Diagnostics',
            icon: <AnalyticsIcon />,
            children: [
                { id: 'troubleshooting-tools', title: 'Tools', icon: <SettingsIcon /> },
                { id: 'troubleshooting-metrics', title: 'Metrics', icon: <AnalyticsIcon /> },
                { id: 'troubleshooting-profiling', title: 'Profiling', icon: <SpeedIcon /> }
            ]
        },
        {
            id: 'troubleshooting-monitoring',
            title: 'Monitoring',
            icon: <MonitorIcon />,
            children: [
                { id: 'troubleshooting-health', title: 'Health Checks', icon: <AnalyticsIcon /> },
                { id: 'troubleshooting-alerts', title: 'Alerts', icon: <WarningIcon /> },
                { id: 'troubleshooting-logging', title: 'Logging', icon: <InfoIcon /> }
            ]
        },
        {
            id: 'troubleshooting-optimization',
            title: 'Performance',
            icon: <TrendingUpIcon />,
            children: [
                { id: 'troubleshooting-tuning', title: 'Tuning', icon: <SettingsIcon /> },
                { id: 'troubleshooting-best-practices', title: 'Best Practices', icon: <InfoIcon /> },
                { id: 'troubleshooting-scaling', title: 'Scaling', icon: <TrendingUpIcon /> }
            ]
        }
    ];

    const sidebarConfig = {
        title: "Troubleshooting Guide",
        navigationItems: navigationItems,
        expandedByDefault: true
    };

    return (
        <Layout sidebarConfig={sidebarConfig}>
            <Container
                maxWidth={false}
                sx={{
                    py: 4,
                    px: { xs: 2, sm: 3, md: 0 }, // Remove horizontal padding on desktop since Layout handles sidebar offset
                    pr: { xs: 2, sm: 3, md: 4 }, // Keep right padding on desktop
                    pl: { xs: 2, sm: 3, md: 0 }, // Remove left padding on desktop
                    ml: { xs: 0, md: 0 }, // No extra margin on mobile
                    mt: { xs: 1, md: 0 }, // Small top margin on mobile to avoid FAB overlap
                    minHeight: { xs: 'calc(100vh - 80px)', md: 'auto' }, // Ensure full height on mobile
                }}
            >
                <Typography variant="h2" component="h1" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1, fontWeight: 700, mb: 2 }}>
                    <TroubleshootIcon color="primary" />
                    Troubleshooting Guide
                </Typography>
                <Typography variant="h6" color="text.secondary" sx={{ mb: 4 }}>
                    Comprehensive guide to diagnose and resolve JCacheX performance issues
                </Typography>

                <Tabs value={selectedTab} onChange={(_, value) => setSelectedTab(value)} sx={{ mb: 3 }}>
                    <Tab icon={<BugIcon />} label="Common Issues" />
                    <Tab icon={<AnalyticsIcon />} label="Diagnostics" />
                    <Tab icon={<MonitorIcon />} label="Monitoring" />
                    <Tab icon={<TrendingUpIcon />} label="Performance" />
                </Tabs>

                {/* Common Issues Tab */}
                {selectedTab === 0 && (
                    <Box>
                        <Typography variant="h6" gutterBottom>
                            Common Issues & Solutions
                        </Typography>
                        {commonIssues.map((category, categoryIndex) => (
                            <Paper key={categoryIndex} sx={{ mb: 2 }}>
                                <Box sx={{ p: 2, bgcolor: 'grey.50', display: 'flex', alignItems: 'center', gap: 1 }}>
                                    {category.icon}
                                    <Typography variant="h6">{category.category}</Typography>
                                </Box>
                                {category.issues.map((issue, issueIndex) => (
                                    <Accordion key={issueIndex}>
                                        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                                            <Box>
                                                <Typography variant="subtitle1">{issue.problem}</Typography>
                                                <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
                                                    {issue.symptoms.map((symptom, index) => (
                                                        <Chip key={index} label={symptom} size="small" variant="outlined" />
                                                    ))}
                                                </Box>
                                            </Box>
                                        </AccordionSummary>
                                        <AccordionDetails>
                                            <Alert severity="info" sx={{ mb: 2 }}>
                                                <strong>Solution:</strong> {issue.solution}
                                            </Alert>
                                            <CodeTabs
                                                tabs={[{
                                                    id: `solution-${categoryIndex}-${issueIndex}`,
                                                    label: 'Solution Code',
                                                    language: 'java',
                                                    code: issue.code
                                                }]}
                                                showCopyButtons={true}
                                            />
                                        </AccordionDetails>
                                    </Accordion>
                                ))}
                            </Paper>
                        ))}
                    </Box>
                )}

                {/* Diagnostics Tab */}
                {selectedTab === 1 && (
                    <Box>
                        <Typography variant="h6" gutterBottom>
                            Performance Diagnostics
                        </Typography>

                        {showDiagnostics && interactive && (
                            <Paper sx={{ p: 3, mb: 3 }}>
                                <Typography variant="h6" gutterBottom>
                                    Cache Health Check
                                </Typography>
                                <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                                    Enter your cache metrics to get personalized recommendations
                                </Typography>

                                <Stack spacing={3}>
                                    <Box>
                                        <Typography variant="body2" gutterBottom>
                                            Hit Rate: {hitRate}%
                                        </Typography>
                                        <LinearProgress
                                            variant="determinate"
                                            value={hitRate}
                                            sx={{ height: 8, borderRadius: 4 }}
                                        />
                                        <TextField
                                            type="number"
                                            value={hitRate}
                                            onChange={(e) => setHitRate(Number(e.target.value))}
                                            size="small"
                                            sx={{ mt: 1 }}
                                            InputProps={{ inputProps: { min: 0, max: 100 } }}
                                        />
                                    </Box>

                                    <Box>
                                        <Typography variant="body2" gutterBottom>
                                            Eviction Rate: {evictionRate}%
                                        </Typography>
                                        <LinearProgress
                                            variant="determinate"
                                            value={evictionRate}
                                            color="warning"
                                            sx={{ height: 8, borderRadius: 4 }}
                                        />
                                        <TextField
                                            type="number"
                                            value={evictionRate}
                                            onChange={(e) => setEvictionRate(Number(e.target.value))}
                                            size="small"
                                            sx={{ mt: 1 }}
                                            InputProps={{ inputProps: { min: 0, max: 100 } }}
                                        />
                                    </Box>

                                    <Box>
                                        <Typography variant="body2" gutterBottom>
                                            Memory Usage: {memoryUsage}%
                                        </Typography>
                                        <LinearProgress
                                            variant="determinate"
                                            value={memoryUsage}
                                            color="error"
                                            sx={{ height: 8, borderRadius: 4 }}
                                        />
                                        <TextField
                                            type="number"
                                            value={memoryUsage}
                                            onChange={(e) => setMemoryUsage(Number(e.target.value))}
                                            size="small"
                                            sx={{ mt: 1 }}
                                            InputProps={{ inputProps: { min: 0, max: 100 } }}
                                        />
                                    </Box>
                                </Stack>

                                <Typography variant="h6" sx={{ mt: 3, mb: 2 }}>
                                    Recommendations
                                </Typography>
                                <Stack spacing={2}>
                                    {getDiagnosticRecommendations().map((rec, index) => (
                                        <Alert key={index} severity={rec.severity as any}>
                                            <strong>{rec.title}:</strong> {rec.description}
                                            <br />
                                            <em>Action: {rec.action}</em>
                                        </Alert>
                                    ))}
                                </Stack>
                            </Paper>
                        )}

                        {/* Diagnostic Flowchart */}
                        <Paper sx={{ p: 3 }}>
                            <Typography variant="h6" gutterBottom>
                                Diagnostic Flowchart
                            </Typography>
                            <Stack spacing={3}>
                                <Box>
                                    <Typography variant="subtitle1" gutterBottom>
                                        Performance Issues
                                    </Typography>
                                    {diagnosticFlowchart.performanceIssues.map((item, index) => (
                                        <Card key={index} sx={{ mb: 2 }}>
                                            <CardContent>
                                                <Typography variant="subtitle2" color="warning.main">
                                                    {item.condition}
                                                </Typography>
                                                <List dense>
                                                    {item.solutions.map((solution, sIndex) => (
                                                        <ListItem key={sIndex}>
                                                            <ListItemIcon>
                                                                <CheckIcon fontSize="small" />
                                                            </ListItemIcon>
                                                            <ListItemText primary={solution} />
                                                        </ListItem>
                                                    ))}
                                                </List>
                                            </CardContent>
                                        </Card>
                                    ))}
                                </Box>

                                <Box>
                                    <Typography variant="subtitle1" gutterBottom>
                                        Memory Issues
                                    </Typography>
                                    {diagnosticFlowchart.memoryIssues.map((item, index) => (
                                        <Card key={index} sx={{ mb: 2 }}>
                                            <CardContent>
                                                <Typography variant="subtitle2" color="error.main">
                                                    {item.condition}
                                                </Typography>
                                                <List dense>
                                                    {item.solutions.map((solution, sIndex) => (
                                                        <ListItem key={sIndex}>
                                                            <ListItemIcon>
                                                                <CheckIcon fontSize="small" />
                                                            </ListItemIcon>
                                                            <ListItemText primary={solution} />
                                                        </ListItem>
                                                    ))}
                                                </List>
                                            </CardContent>
                                        </Card>
                                    ))}
                                </Box>
                            </Stack>
                        </Paper>
                    </Box>
                )}

                {/* Monitoring Tab */}
                {selectedTab === 2 && (
                    <Box>
                        <Typography variant="h6" gutterBottom>
                            Monitoring & Observability
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                            Set up comprehensive monitoring for your JCacheX instances
                        </Typography>

                        <CodeTabs
                            tabs={monitoringExamples}
                            showCopyButtons={true}
                            showTryOnlineButtons={false}
                        />
                    </Box>
                )}

                {/* Performance Tab */}
                {selectedTab === 3 && (
                    <Box>
                        <Typography variant="h6" gutterBottom>
                            Performance Optimization
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                            Advanced performance tuning strategies for different scenarios
                        </Typography>

                        <Stack spacing={3}>
                            <Card>
                                <CardContent>
                                    <Typography variant="h6" gutterBottom>
                                        Memory-Constrained Environment
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                        Optimize for environments with limited memory (&lt; 2GB heap)
                                    </Typography>
                                    <CodeTabs
                                        tabs={[{
                                            id: 'memory-constrained',
                                            label: 'Memory Optimization',
                                            language: 'java',
                                            code: `// MEMORY_EFFICIENT profile for constrained environments
Cache<String, Data> cache = JCacheXBuilder
    .fromProfile(ProfileName.MEMORY_EFFICIENT)
    .maximumSize(10000)           // Smaller cache size
    .expireAfterWrite(Duration.ofMinutes(30))
    .recordStats(true)            // Monitor memory usage
    .build();

// Alternative: Manual memory optimization
Cache<String, Data> manualCache = JCacheXBuilder
    .forReadHeavyWorkload()
    .maximumSize(5000)            // Reduced size
    .expireAfterWrite(Duration.ofMinutes(15))  // Shorter TTL
    .build();`
                                        }]}
                                        showCopyButtons={true}
                                    />
                                </CardContent>
                            </Card>

                            <Card>
                                <CardContent>
                                    <Typography variant="h6" gutterBottom>
                                        High-Throughput Scenario
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                        Optimize for high-throughput scenarios (&gt; 100M ops/sec)
                                    </Typography>
                                    <CodeTabs
                                        tabs={[{
                                            id: 'high-throughput',
                                            label: 'Throughput Optimization',
                                            language: 'java',
                                            code: `// HIGH_PERFORMANCE profile for maximum throughput
Cache<String, Data> cache = JCacheXBuilder
    .forHighPerformance()
    .maximumSize(1000000)
    .expireAfterWrite(Duration.ofHours(1))
    .recordStats(false)           // Disable stats for max performance
    .build();

// For read-heavy scenarios: 501.1M ops/sec with ZeroCopy
Cache<String, Data> readCache = JCacheXBuilder
    .fromProfile(ProfileName.ZERO_COPY)
    .maximumSize(500000)
    .build();

// For write-heavy scenarios: 224.6M ops/sec
Cache<String, Data> writeCache = JCacheXBuilder
    .forWriteHeavyWorkload()
    .maximumSize(250000)
    .recordStats(false)
    .build();`
                                        }]}
                                        showCopyButtons={true}
                                    />
                                </CardContent>
                            </Card>

                            <Card>
                                <CardContent>
                                    <Typography variant="h6" gutterBottom>
                                        Ultra-Low Latency
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                        Optimize for ultra-low latency scenarios (501.1M ops/sec)
                                    </Typography>
                                    <CodeTabs
                                        tabs={[{
                                            id: 'ultra-low-latency',
                                            label: 'Latency Optimization',
                                            language: 'java',
                                            code: `// ZERO_COPY profile for ultimate performance (501.1M ops/sec)
Cache<String, Data> cache = JCacheXBuilder
    .fromProfile(ProfileName.ZERO_COPY)
    .maximumSize(100000)
    .expireAfterWrite(Duration.ofHours(24)) // Long TTL
    .recordStats(false)           // Disable stats collection
    .build();

// Alternative: HIGH_PERFORMANCE profile
Cache<String, Data> performanceCache = JCacheXBuilder
    .forHighPerformance()
    .maximumSize(200000)
    .expireAfterAccess(Duration.ofHours(2))
    .recordStats(false)
    .build();

// For trading/HFT scenarios
Cache<String, Data> hftCache = JCacheXBuilder
    .fromProfile(ProfileName.ZERO_COPY)
    .maximumSize(50000)          // Smaller for predictable performance
    .build();`
                                        }]}
                                        showCopyButtons={true}
                                    />
                                </CardContent>
                            </Card>
                        </Stack>
                    </Box>
                )}
            </Container>
        </Layout>
    );
};

export default TroubleshootingGuide;
