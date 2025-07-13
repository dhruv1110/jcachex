import React, { useState } from 'react';
import {
    Box,
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
                    'Analyze access patterns - check for random access vs temporal locality',
                    'Increase cache size if memory allows',
                    'Review TTL settings - may be too aggressive',
                    'Consider switching to READ_HEAVY profile for better hit rates'
                ]
            },
            {
                condition: 'High eviction frequency',
                solutions: [
                    'Check if cache size is appropriate for working set',
                    'Monitor memory pressure and GC activity',
                    'Consider MEMORY_EFFICIENT profile',
                    'Analyze eviction reasons in logs'
                ]
            },
            {
                condition: 'High latency operations',
                solutions: [
                    'Switch to ZERO_COPY profile for ultra-low latency',
                    'Check for lock contention in concurrent scenarios',
                    'Review cache loader performance',
                    'Enable detailed performance metrics'
                ]
            }
        ],
        memoryIssues: [
            {
                condition: 'Cache size > 25% of heap',
                solutions: [
                    'Reduce cache maximum size',
                    'Use softValues() for memory-constrained environments',
                    'Consider distributed caching for large datasets',
                    'Monitor heap usage patterns'
                ]
            },
            {
                condition: 'Memory leaks detected',
                solutions: [
                    'Check for proper cache invalidation',
                    'Review event listener implementations',
                    'Use profiling tools (JProfiler, VisualVM)',
                    'Monitor cache growth over time'
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
                    solution: 'Switch to ZERO_COPY or HIGH_PERFORMANCE profile',
                    code: `Cache<String, Data> cache = JCacheXBuilder.forUltraLowLatency()
    .maximumSize(100000L)
    .evictionStrategy(NONE)  // No eviction overhead
    .recordStats(false)      // Disable stats collection
    .build();`
                },
                {
                    problem: 'Low hit rate',
                    symptoms: ['Frequent cache misses', 'High database load', 'Poor response times'],
                    solution: 'Analyze access patterns and optimize TTL settings',
                    code: `Cache<String, Data> cache = JCacheXBuilder.forReadHeavyWorkload()
    .maximumSize(50000L)
    .expireAfterWrite(Duration.ofHours(2))
    .expireAfterAccess(Duration.ofHours(4))  // Keep hot data longer
    .build();`
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
                    solution: 'Use memory-efficient configuration',
                    code: `Cache<String, Data> cache = JCacheXBuilder.forMemoryConstrainedEnvironment()
    .maximumSize(10000L)
    .softValues()                    // Allow GC to reclaim under pressure
    .evictionStrategy(ENHANCED_LRU)  // More memory-efficient
    .build();`
                },
                {
                    problem: 'Memory leaks',
                    symptoms: ['Growing heap usage', 'Cache not releasing memory', 'GC thrashing'],
                    solution: 'Implement proper eviction listeners and monitoring',
                    code: `Cache<String, Data> cache = JCacheXBuilder.create()
    .maximumSize(20000L)
    .expireAfterWrite(Duration.ofHours(1))
    .evictionListener(this::onEviction)
    .build();

private void onEviction(String key, Data data, RemovalCause cause) {
    log.info("Evicted {} due to {}", key, cause);
    // Check for unexpected eviction patterns
    if (cause == RemovalCause.SIZE) {
        metrics.counter("cache.eviction.size").increment();
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
                    solution: 'Choose profile based on access patterns',
                    code: `// For read-heavy workloads (80%+ reads)
Cache<String, Data> readCache = JCacheXBuilder.forReadHeavyWorkload()
    .build();

// For write-heavy workloads (50%+ writes)
Cache<String, Data> writeCache = JCacheXBuilder.forWriteHeavyWorkload()
    .build();

// For API response caching
Cache<String, ApiResponse> apiCache = JCacheXBuilder.forApiResponseCaching()
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

    return (
        <Box>
            <Typography variant="h4" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <TroubleshootIcon color="primary" />
                Troubleshooting Guide
            </Typography>
            <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
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
                                        code: `Cache<String, Data> cache = JCacheXBuilder.forMemoryConstrainedEnvironment()
    .maximumSize(10000L)           // Smaller cache size
    .softValues()                  // Allow GC to reclaim under pressure
    .evictionStrategy(ENHANCED_LRU) // More memory-efficient than LFU
    .expireAfterWrite(Duration.ofMinutes(30))
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
                                    Optimize for high-throughput scenarios (&gt; 1M ops/sec)
                                </Typography>
                                <CodeTabs
                                    tabs={[{
                                        id: 'high-throughput',
                                        label: 'Throughput Optimization',
                                        language: 'java',
                                        code: `Cache<String, Data> cache = JCacheXBuilder.forHighPerformance()
    .maximumSize(1000000L)
    .concurrencyLevel(64)          // Match CPU cores * 2
    .evictionStrategy(TINY_WINDOW_LFU) // Optimal for high throughput
    .recordStats(false)            // Disable stats for max performance
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
                                    Optimize for ultra-low latency scenarios (&lt; 10ns)
                                </Typography>
                                <CodeTabs
                                    tabs={[{
                                        id: 'ultra-low-latency',
                                        label: 'Latency Optimization',
                                        language: 'java',
                                        code: `Cache<String, Data> cache = JCacheXBuilder.forUltraLowLatency()
    .maximumSize(100000L)
    .evictionStrategy(NONE)        // No eviction overhead
    .recordStats(false)            // Disable stats collection
    .expireAfterWrite(Duration.ofHours(24)) // Long TTL
    .build();`
                                    }]}
                                    showCopyButtons={true}
                                />
                            </CardContent>
                        </Card>
                    </Stack>
                </Box>
            )}
        </Box>
    );
};

export default TroubleshootingGuide;
