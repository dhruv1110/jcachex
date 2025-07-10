import React from 'react';
import {
    Container,
    Typography,
    Box,
    Card,
    CardContent,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
    Chip,
    Alert,
    Button,
    Stack,
    useTheme,
    useMediaQuery,
    Divider,
    Paper
} from '@mui/material';
import {
    Code as CodeIcon,
    Coffee as JavaIcon,
    Extension as ExtensionIcon,
    Speed as SpeedIcon,
    Settings as SettingsIcon,
    Memory as MemoryIcon,
    Security as SecurityIcon,
    Sync as SyncIcon,
    Dashboard as DashboardIcon,
    Storage as StorageIcon,
    Timeline as TimelineIcon,
    Api as ApiIcon,
    Build as BuildIcon,
    Analytics as AnalyticsIcon,
    Architecture as ArchitectureIcon,
    Timer as TimerIcon,
    GitHub as GitHubIcon,
    Link as LinkIcon,
    PlayArrow as PlayArrowIcon,
    Computer as ComputerIcon,
    CloudSync as CloudSyncIcon,
    CheckCircle as CheckCircleIcon,
    Star as StarIcon,
    TrendingUp as TrendingUpIcon,
    Shield as ShieldIcon,
    FlashOn as FlashOnIcon,
    Cloud as CloudIcon,
    Cached as CachedIcon,
    Http as HttpIcon,
    Refresh as RefreshIcon,
    Delete as DeleteIcon,
    DataUsage as DataUsageIcon,
    Psychology as PsychologyIcon,
    Functions as FunctionsIcon,
    QueryStats as QueryStatsIcon,
    Transform as TransformIcon,
    School as SchoolIcon,
    Engineering as EngineeringIcon,
    Layers as LayersIcon,
    AccountTree as AccountTreeIcon,
    SmartToy as SmartToyIcon,
    Calculate as CalculateIcon,
    Assessment as AssessmentIcon,
    TableChart as TableChartIcon,
    Public as PublicIcon,
    Lightbulb as LightbulbIcon,
    Insights as InsightsIcon,
} from '@mui/icons-material';
import Layout from './Layout';
import CodeTabs from './CodeTabs';
import { MetaTags, Breadcrumbs } from './SEO';
import type { CodeTab } from '../types';
import EnhancedCard, { type FeatureItem, ExampleCardInfoItem } from './EnhancedCard';

// Import example code from auto-generated constants
import {
    BASIC_CACHE_EXAMPLE,
    OBJECT_CACHING_EXAMPLE,
    COMPLEX_CACHE_CONFIG_EXAMPLE,
    ASYNC_CACHE_EXAMPLE,
    THREAD_SAFETY_EXAMPLE,
    MULTI_CACHE_SYSTEM_EXAMPLE,
    OBSERVABLE_CACHE_SERVICE_EXAMPLE,
    COROUTINES_EXAMPLE,
    EXTENSIONS_EXAMPLE,
    SPRING_JPA_EXAMPLE,
    REST_API_EXAMPLE,
    COMPUTATION_SERVICE_EXAMPLE
} from '../utils/loadExamples';

const Examples: React.FC = () => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    // Define navigation items for the sidebar
    const navigationItems = [
        {
            id: 'java-examples',
            title: 'Java Examples',
            icon: <JavaIcon />,
            children: [
                { id: 'basic-cache', title: 'Basic Cache', icon: <CodeIcon /> },
                { id: 'complex-cache-configurations', title: 'Complex Cache Configurations', icon: <SettingsIcon /> },
                { id: 'non-blocking-caching', title: 'Non-blocking Caching', icon: <SyncIcon /> },
                { id: 'thread-safety', title: 'Thread Safety', icon: <SecurityIcon /> },
                { id: 'multi-cache-system', title: 'Multi Cache System', icon: <ArchitectureIcon /> },
                { id: 'observability', title: 'Observability', icon: <AnalyticsIcon /> },
            ],
        },
        {
            id: 'kotlin-examples',
            title: 'Kotlin Examples',
            icon: <ExtensionIcon />,
            children: [
                { id: 'extensions-usage', title: 'Extensions Usage', icon: <ExtensionIcon /> },
                { id: 'coroutines-support', title: 'Coroutines Support', icon: <CloudSyncIcon /> },
            ],
        },
        {
            id: 'springboot-examples',
            title: 'Spring Boot Examples',
            icon: <SpeedIcon />,
            children: [
                { id: 'spring-jpa', title: 'Spring JPA', icon: <StorageIcon /> },
                { id: 'rest-api', title: 'Rest API', icon: <ApiIcon /> },
                { id: 'heavy-computation-process', title: 'Heavy Computation Process', icon: <ComputerIcon /> },
            ],
        },
    ];

    const sidebarConfig = {
        title: "Examples",
        navigationItems: navigationItems,
        expandedByDefault: true
    };

    // SEO data
    const seoData = {
        title: 'JCacheX Examples and Code Samples',
        description: 'Comprehensive examples and code samples for JCacheX. Learn Java, Kotlin, and Spring Boot integration patterns.',
        keywords: ['JCacheX examples', 'cache examples', 'Java cache tutorial', 'Spring cache examples', 'Kotlin cache'],
        canonical: 'https://dhruv1110.github.io/jcachex/examples'
    };

    return (
        <Layout sidebarConfig={sidebarConfig}>
            <MetaTags seo={seoData} />
            <Breadcrumbs items={[
                { label: 'Home', path: '/' },
                { label: 'Examples', path: '/examples', current: true }
            ]} />

            <Container
                maxWidth={false}
                sx={{
                    py: 4,
                    px: { xs: 2, sm: 3, md: 0 },
                    pr: { xs: 2, sm: 3, md: 4 },
                    pl: { xs: 2, sm: 3, md: 0 },
                    ml: { xs: 0, md: 0 },
                    mt: { xs: 1, md: 0 },
                    minHeight: { xs: 'calc(100vh - 80px)', md: 'auto' },
                }}
            >
                {/* Header */}
                <Box sx={{ textAlign: 'center', mb: 6 }}>
                    <Typography variant="h2" component="h1" gutterBottom sx={{ fontWeight: 700, mb: 2 }}>
                        JCacheX Examples
                    </Typography>
                    <Typography variant="h5" color="text.secondary" sx={{ mb: 4 }}>
                        Comprehensive code examples and integration patterns for production applications
                    </Typography>
                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr 1fr' },
                        gap: 2,
                        justifyItems: 'center',
                        maxWidth: '600px',
                        mx: 'auto'
                    }}>
                        <Chip
                            icon={<JavaIcon />}
                            label="Java Examples"
                            color="primary"
                            sx={{ px: 2, py: 1 }}
                        />
                        <Chip
                            icon={<ExtensionIcon />}
                            label="Kotlin Extensions"
                            color="secondary"
                            sx={{ px: 2, py: 1 }}
                        />
                        <Chip
                            icon={<SpeedIcon />}
                            label="Spring Boot"
                            color="success"
                            sx={{ px: 2, py: 1 }}
                        />
                    </Box>
                </Box>

                {/* Java Examples Section */}
                <Box id="java-examples" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <JavaIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Java Examples
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
                        Comprehensive Java examples covering basic usage to advanced enterprise patterns.
                    </Typography>
                </Box>

                {/* Basic Cache */}
                <Box id="basic-cache" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <CodeIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Basic Cache
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Essential cache setup and usage patterns for getting started with JCacheX.
                    </Typography>

                    <EnhancedCard
                        header={{
                            icon: <CodeIcon />,
                            title: "When to use Basic Cache",
                            subtitle: "Simple, fast caching for everyday use cases",
                            bgColor: "primary.main"
                        }}
                        description="Perfect for getting started with caching or when you need straightforward key-value storage without complex configurations. Ideal for most common caching scenarios."
                        features={{
                            leftTitle: "Common Use Cases",
                            leftIcon: <CheckCircleIcon />,
                            leftItems: [
                                ExampleCardInfoItem(<CachedIcon />, "Frequently Accessed Data", "Cache database query results, API responses"),
                                ExampleCardInfoItem(<AccountTreeIcon />, "User Sessions", "Store user preferences, shopping carts, temp data"),
                                ExampleCardInfoItem(<SettingsIcon />, "Configuration Data", "Application settings, feature flags, lookup tables"),
                                ExampleCardInfoItem(<FunctionsIcon />, "Computation Results", "Cache expensive calculations, processed data")
                            ],
                            rightTitle: "Key Benefits",
                            rightIcon: <StarIcon />,
                            rightItems: [
                                ExampleCardInfoItem(<FlashOnIcon />, "Fast Setup", "Get started in minutes with minimal configuration"),
                                ExampleCardInfoItem(<SchoolIcon />, "Easy to Learn", "Simple API suitable for beginners"),
                                ExampleCardInfoItem(<MemoryIcon />, "Low Memory Footprint", "Efficient storage with automatic cleanup"),
                                ExampleCardInfoItem(<SecurityIcon />, "Thread Safe", "Concurrent access without additional locking")
                            ],
                            rightBgColor: "primary.50"
                        }}
                        footer={{
                            chips: [
                                { icon: <StarIcon />, label: "Beginner Friendly", color: "primary" },
                                { icon: <SpeedIcon />, label: "Fast Performance", color: "success" },
                                { icon: <SchoolIcon />, label: "Easy Setup", color: "info" }
                            ]
                        }}
                    />

                    <CodeTabs tabs={[
                        {
                            id: 'basic-usage',
                            label: 'Basic Usage',
                            language: 'java',
                            code: BASIC_CACHE_EXAMPLE
                        },
                        {
                            id: 'object-caching',
                            label: 'Object Caching',
                            language: 'java',
                            code: OBJECT_CACHING_EXAMPLE
                        }
                    ]} />
                </Box>

                {/* Complex Cache Configurations */}
                <Box id="complex-cache-configurations" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <SettingsIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Complex Cache Configurations
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Advanced configuration patterns for production environments with custom eviction policies and listeners.
                    </Typography>

                    <EnhancedCard
                        header={{
                            icon: <SettingsIcon />,
                            title: "Advanced Configuration Features",
                            subtitle: "Enterprise-grade configurations for production environments",
                            bgColor: "info.main"
                        }}
                        description="Take full control of your cache behavior with advanced configuration options. Perfect for production environments that require fine-tuned performance, custom eviction policies, and comprehensive monitoring."
                        features={{
                            leftTitle: "Configuration Options",
                            leftIcon: <CheckCircleIcon />,
                            leftItems: [
                                ExampleCardInfoItem(<LayersIcon />, "Custom Eviction Strategies", "LRU, LFU, FIFO, TTL, weight-based policies"),
                                ExampleCardInfoItem(<QueryStatsIcon />, "Event Listeners", "Monitor cache operations, evictions, updates"),
                                ExampleCardInfoItem(<MemoryIcon />, "Weight-based Management", "Dynamic memory allocation based on entry size"),
                                ExampleCardInfoItem(<AccountTreeIcon />, "Composite Strategies", "Combine multiple eviction policies intelligently")
                            ],
                            rightTitle: "Enterprise Features",
                            rightIcon: <EngineeringIcon />,
                            rightItems: [
                                ExampleCardInfoItem(<InsightsIcon />, "Advanced Monitoring", "Detailed metrics, performance analytics, alerts"),
                                ExampleCardInfoItem(<FlashOnIcon />, "Performance Tuning", "Optimize for specific workload patterns"),
                                ExampleCardInfoItem(<ShieldIcon />, "Resource Protection", "Prevent memory leaks, control resource usage"),
                                ExampleCardInfoItem(<TransformIcon />, "Dynamic Reconfiguration", "Adjust settings without service restart")
                            ],
                            rightBgColor: "info.50"
                        }}
                        footer={{
                            chips: [
                                { icon: <EngineeringIcon />, label: "Enterprise Ready", color: "primary" },
                                { icon: <SettingsIcon />, label: "Highly Configurable", color: "info" },
                                { icon: <InsightsIcon />, label: "Production Monitoring", color: "success" }
                            ]
                        }}
                    />

                    <CodeTabs tabs={[
                        {
                            id: 'advanced-config',
                            label: 'Advanced Configuration',
                            language: 'java',
                            code: COMPLEX_CACHE_CONFIG_EXAMPLE
                        }
                    ]} />
                </Box>

                {/* Non-blocking Caching */}
                <Box id="non-blocking-caching" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <SyncIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Non-blocking Caching
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Asynchronous caching patterns that don't block application threads during cache operations.
                    </Typography>

                    <EnhancedCard
                        header={{
                            icon: <SyncIcon />,
                            title: "Non-blocking Benefits",
                            subtitle: "Asynchronous operations that keep your application responsive",
                            bgColor: "success.main"
                        }}
                        description="Eliminate blocking operations that can freeze your application threads. Perfect for high-throughput systems where responsiveness is critical and every millisecond counts."
                        features={{
                            leftTitle: "Performance Improvements",
                            leftIcon: <CheckCircleIcon />,
                            leftItems: [
                                ExampleCardInfoItem(<FlashOnIcon />, "Improved Responsiveness", "UI stays responsive during cache operations"),
                                ExampleCardInfoItem(<DataUsageIcon />, "Better Resource Utilization", "Efficient CPU and memory usage patterns"),
                                ExampleCardInfoItem(<SecurityIcon />, "Reduced Thread Contention", "Minimize lock conflicts and deadlocks"),
                                ExampleCardInfoItem(<ShieldIcon />, "Graceful Degradation", "Maintain service quality under high load")
                            ],
                            rightTitle: "Async Patterns",
                            rightIcon: <TrendingUpIcon />,
                            rightItems: [
                                ExampleCardInfoItem(<CloudSyncIcon />, "CompletableFuture Support", "Full async/await pattern integration"),
                                ExampleCardInfoItem(<PlayArrowIcon />, "Reactive Streams", "Backpressure handling and flow control"),
                                ExampleCardInfoItem(<TimerIcon />, "Timeout Management", "Configurable timeouts for async operations"),
                                ExampleCardInfoItem(<RefreshIcon />, "Retry Mechanisms", "Intelligent retry with exponential backoff")
                            ],
                            rightBgColor: "success.50"
                        }}
                        footer={{
                            chips: [
                                { icon: <FlashOnIcon />, label: "High Performance", color: "primary" },
                                { icon: <CloudSyncIcon />, label: "Async Ready", color: "success" },
                                { icon: <TrendingUpIcon />, label: "Scalable", color: "info" }
                            ],
                            performanceText: "⚡ Up to 10x throughput improvement"
                        }}
                    />

                    <CodeTabs tabs={[
                        {
                            id: 'async-cache-operations',
                            label: 'Async Cache Operations',
                            language: 'java',
                            code: ASYNC_CACHE_EXAMPLE
                        }
                    ]} />
                </Box>

                {/* Thread Safety */}
                <Box id="thread-safety" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Thread Safety
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Ensuring cache operations are thread-safe in concurrent environments.
                    </Typography>

                    <EnhancedCard
                        header={{
                            icon: <SecurityIcon />,
                            title: "Thread Safety Considerations",
                            subtitle: "Concurrent operations without compromising data integrity",
                            bgColor: "warning.main"
                        }}
                        description="Ensure your cache operations are completely thread-safe in multi-threaded environments. Critical for applications handling concurrent requests where data consistency is paramount."
                        features={{
                            leftTitle: "Safety Guarantees",
                            leftIcon: <CheckCircleIcon />,
                            leftItems: [
                                ExampleCardInfoItem(<SyncIcon />, "Concurrent Read/Write", "Safe simultaneous access from multiple threads"),
                                ExampleCardInfoItem(<FlashOnIcon />, "Atomic Operations", "All cache operations are atomic and consistent"),
                                ExampleCardInfoItem(<LayersIcon />, "Lock-free Structures", "High-performance concurrent data structures"),
                                ExampleCardInfoItem(<ShieldIcon />, "Consistent State", "Guaranteed consistency across all operations")
                            ],
                            rightTitle: "Concurrency Features",
                            rightIcon: <LightbulbIcon />,
                            rightItems: [
                                ExampleCardInfoItem(<TrendingUpIcon />, "High Throughput", "Optimized for concurrent high-load scenarios"),
                                ExampleCardInfoItem(<TimerIcon />, "Deadlock Prevention", "Sophisticated locking strategies prevent deadlocks"),
                                ExampleCardInfoItem(<DataUsageIcon />, "Memory Barriers", "Proper memory synchronization guarantees"),
                                ExampleCardInfoItem(<InsightsIcon />, "Contention Monitoring", "Built-in metrics for thread contention analysis")
                            ],
                            rightBgColor: "warning.50"
                        }}
                        footer={{
                            chips: [
                                { icon: <SecurityIcon />, label: "Thread Safe", color: "primary" },
                                { icon: <FlashOnIcon />, label: "Lock-free", color: "warning" },
                                { icon: <TrendingUpIcon />, label: "High Concurrency", color: "success" }
                            ],
                            performanceText: "🔐 100% thread-safe guaranteed"
                        }}
                    />

                    <CodeTabs tabs={[
                        {
                            id: 'thread-safe-cache',
                            label: 'Thread-Safe Cache',
                            language: 'java',
                            code: THREAD_SAFETY_EXAMPLE
                        }
                    ]} />
                </Box>

                {/* Multi Cache System */}
                <Box id="multi-cache-system" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <ArchitectureIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Multi Cache System
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Managing multiple cache instances with different configurations and purposes.
                    </Typography>

                    <EnhancedCard
                        header={{
                            icon: <ArchitectureIcon />,
                            title: "Multi-Cache Architecture",
                            subtitle: "Hierarchical caching with intelligent coordination",
                            bgColor: "secondary.main"
                        }}
                        description="Design sophisticated multi-tier cache architectures for complex applications. Perfect for enterprise systems requiring different cache strategies for different data types and access patterns."
                        features={{
                            leftTitle: "Cache Layers",
                            leftIcon: <CheckCircleIcon />,
                            leftItems: [
                                ExampleCardInfoItem(<MemoryIcon />, "L1 Memory Cache", "Fast in-memory storage for hot data"),
                                ExampleCardInfoItem(<StorageIcon />, "L2 Disk Cache", "Persistent storage for larger datasets"),
                                ExampleCardInfoItem(<LayersIcon />, "Domain Partitioning", "Separate caches for different business domains"),
                                ExampleCardInfoItem(<SyncIcon />, "Cache Coordination", "Intelligent synchronization between cache layers")
                            ],
                            rightTitle: "Management Features",
                            rightIcon: <DashboardIcon />,
                            rightItems: [
                                ExampleCardInfoItem(<DashboardIcon />, "Unified Management", "Central control panel for all cache instances"),
                                ExampleCardInfoItem(<InsightsIcon />, "Cross-Cache Analytics", "Comprehensive metrics across all cache layers"),
                                ExampleCardInfoItem(<ShieldIcon />, "Consistency Guarantees", "Maintain data consistency across cache tiers"),
                                ExampleCardInfoItem(<TransformIcon />, "Dynamic Scaling", "Auto-scale cache tiers based on demand")
                            ],
                            rightBgColor: "secondary.50"
                        }}
                        footer={{
                            chips: [
                                { icon: <ArchitectureIcon />, label: "Multi-Tier", color: "primary" },
                                { icon: <LayersIcon />, label: "Hierarchical", color: "secondary" },
                                { icon: <EngineeringIcon />, label: "Enterprise Scale", color: "success" }
                            ],
                            performanceText: "🏗️ Scalable architecture design"
                        }}
                    />

                    <CodeTabs tabs={[
                        {
                            id: 'multi-cache-manager',
                            label: 'Multi-Cache Manager',
                            language: 'java',
                            code: MULTI_CACHE_SYSTEM_EXAMPLE
                        }
                    ]} />
                </Box>

                {/* Observability */}
                <Box id="observability" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <AnalyticsIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Observability
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Monitoring, metrics, and observability patterns for production cache systems.
                    </Typography>

                    <EnhancedCard
                        header={{
                            icon: <AnalyticsIcon />,
                            title: "Observability Features",
                            subtitle: "Complete monitoring and analytics for production systems",
                            bgColor: "info.main"
                        }}
                        description="Gain deep insights into your cache performance with comprehensive monitoring, metrics, and alerting capabilities. Essential for maintaining optimal performance in production environments."
                        features={{
                            leftTitle: "Monitoring Tools",
                            leftIcon: <CheckCircleIcon />,
                            leftItems: [
                                ExampleCardInfoItem(<QueryStatsIcon />, "Real-time Metrics", "Hit rates, miss rates, response times, throughput"),
                                ExampleCardInfoItem(<DashboardIcon />, "Performance Dashboards", "Visual analytics with charts and graphs"),
                                ExampleCardInfoItem(<InsightsIcon />, "Alerting System", "Configurable alerts for performance thresholds"),
                                ExampleCardInfoItem(<TimelineIcon />, "Distributed Tracing", "End-to-end request tracking across services")
                            ],
                            rightTitle: "Analytics & Insights",
                            rightIcon: <TrendingUpIcon />,
                            rightItems: [
                                ExampleCardInfoItem(<AssessmentIcon />, "Performance Analytics", "Deep dive into cache behavior patterns"),
                                ExampleCardInfoItem(<TableChartIcon />, "Usage Statistics", "Detailed breakdowns of cache utilization"),
                                ExampleCardInfoItem(<LightbulbIcon />, "Optimization Recommendations", "AI-powered suggestions for performance tuning"),
                                ExampleCardInfoItem(<DataUsageIcon />, "Cost Analysis", "Resource usage and cost optimization insights")
                            ],
                            rightBgColor: "info.50"
                        }}
                        footer={{
                            chips: [
                                { icon: <AnalyticsIcon />, label: "Full Observability", color: "primary" },
                                { icon: <InsightsIcon />, label: "Real-time Monitoring", color: "info" },
                                { icon: <LightbulbIcon />, label: "AI Insights", color: "success" }
                            ]
                        }}
                    />

                    <CodeTabs tabs={[
                        {
                            id: 'cache-observability',
                            label: 'Cache Observability',
                            language: 'java',
                            code: OBSERVABLE_CACHE_SERVICE_EXAMPLE
                        }
                    ]} />
                </Box>

                {/* Kotlin Examples Section */}
                <Box id="kotlin-examples" sx={{ mb: 8, mt: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <ExtensionIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Kotlin Examples
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
                        Kotlin-specific examples showcasing extensions and coroutines support.
                    </Typography>
                </Box>

                {/* Extensions Usage */}
                <EnhancedCard
                    header={{
                        icon: <ExtensionIcon />,
                        title: "Kotlin Extensions",
                        subtitle: "Idiomatic Kotlin extensions for JCacheX providing a more fluent API.",
                        bgColor: "primary.main"
                    }}
                    description="Leverage Kotlin's language features for a more expressive and concise caching API. Enjoy null safety, extension functions, and seamless integration with Kotlin collections."
                    features={{
                        leftTitle: "Language Features",
                        leftIcon: <CheckCircleIcon />,
                        leftItems: [
                            ExampleCardInfoItem(<ExtensionIcon />, "Extension Functions", "Add cache operations directly to your types"),
                            ExampleCardInfoItem(<SchoolIcon />, "Type Inference", "No need to specify types explicitly"),
                            ExampleCardInfoItem(<AccountTreeIcon />, "Collection Support", "Work with lists, sets, and maps natively"),
                            ExampleCardInfoItem(<ShieldIcon />, "Null Safety", "Avoid null pointer exceptions with safe calls")
                        ],
                        rightTitle: "Developer Experience",
                        rightIcon: <TrendingUpIcon />,
                        rightItems: [
                            ExampleCardInfoItem(<ExtensionIcon />, "Intuitive API", "Natural Kotlin syntax feels familiar"),
                            ExampleCardInfoItem(<LightbulbIcon />, "IDE Support", "Full IntelliJ IDEA autocomplete and refactoring"),
                            ExampleCardInfoItem(<TransformIcon />, "Null Safety", "Kotlin's null safety prevents runtime errors"),
                            ExampleCardInfoItem(<FlashOnIcon />, "Concise Code", "Reduce boilerplate with smart defaults")
                        ],
                        rightBgColor: "secondary.50"
                    }}
                    footer={{
                        chips: [
                            { icon: <ExtensionIcon />, label: "Kotlin Native", color: "primary" },
                            { icon: <SchoolIcon />, label: "Developer Friendly", color: "secondary" },
                            { icon: <ShieldIcon />, label: "Type Safe", color: "success" }
                        ],
                        performanceText: "🎯 Idiomatic Kotlin experience"
                    }}
                />

                <CodeTabs tabs={[
                    {
                        id: 'kotlin-extensions',
                        label: 'Kotlin Extensions',
                        language: 'kotlin',
                        code: EXTENSIONS_EXAMPLE
                    }
                ]} />

                {/* Coroutines Support */}
                <EnhancedCard
                    header={{
                        icon: <CloudSyncIcon />,
                        title: "Coroutines Integration",
                        subtitle: "Seamless async operations with structured concurrency",
                        bgColor: "success.main"
                    }}
                    description="Harness the power of Kotlin coroutines for truly asynchronous caching operations. Perfect for reactive applications that need non-blocking I/O with structured concurrency and cancellation support."
                    features={{
                        leftTitle: "Async Features",
                        leftIcon: <CheckCircleIcon />,
                        leftItems: [
                            ExampleCardInfoItem(<SyncIcon />, "Suspend Functions", "Non-blocking cache operations with suspend/resume"),
                            ExampleCardInfoItem(<TimelineIcon />, "Flow-based Events", "Reactive streams for cache events and updates"),
                            ExampleCardInfoItem(<AccountTreeIcon />, "Structured Concurrency", "Hierarchical task management with scopes"),
                            ExampleCardInfoItem(<DeleteIcon />, "Cancellation Support", "Cooperative cancellation for long-running operations")
                        ],
                        rightTitle: "Performance Benefits",
                        rightIcon: <TrendingUpIcon />,
                        rightItems: [
                            ExampleCardInfoItem(<FlashOnIcon />, "Non-blocking I/O", "Threads never block waiting for cache operations"),
                            ExampleCardInfoItem(<DataUsageIcon />, "Resource Efficiency", "Handle thousands of concurrent operations"),
                            ExampleCardInfoItem(<ShieldIcon />, "Exception Safety", "Structured exception handling with coroutines"),
                            ExampleCardInfoItem(<InsightsIcon />, "Backpressure Control", "Flow-based backpressure for high-load scenarios")
                        ],
                        rightBgColor: "success.50"
                    }}
                    footer={{
                        chips: [
                            { icon: <CloudSyncIcon />, label: "Coroutines Ready", color: "primary" },
                            { icon: <FlashOnIcon />, label: "Non-blocking", color: "success" },
                            { icon: <AccountTreeIcon />, label: "Structured", color: "info" }
                        ],
                        performanceText: "⚡ Truly reactive caching"
                    }}
                />

                <CodeTabs tabs={[
                    {
                        id: 'coroutines-cache',
                        label: 'Coroutines Cache',
                        language: 'kotlin',
                        code: COROUTINES_EXAMPLE
                    }
                ]} />

                {/* Spring Boot Examples Section */}
                <Box id="springboot-examples" sx={{ mb: 8, mt: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <SpeedIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Spring Boot Examples
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
                        Spring Boot integration examples for enterprise applications.
                    </Typography>
                </Box>

                {/* Spring JPA */}
                <EnhancedCard
                    header={{
                        icon: <StorageIcon />,
                        title: "Spring JPA Integration",
                        subtitle: "Seamless database caching with Spring Data JPA",
                        bgColor: "success.main"
                    }}
                    description="Supercharge your Spring Data JPA applications with intelligent caching. Reduce database load, improve response times, and scale your application effortlessly with automatic cache management."
                    features={{
                        leftTitle: "JPA Features",
                        leftIcon: <CheckCircleIcon />,
                        leftItems: [
                            ExampleCardInfoItem(<StorageIcon />, "Repository-level Caching", "Automatic caching for repository methods"),
                            ExampleCardInfoItem(<QueryStatsIcon />, "Query Result Caching", "Cache complex JPQL and native SQL queries"),
                            ExampleCardInfoItem(<AccountTreeIcon />, "Entity-level Management", "Individual entity caching with relationships"),
                            ExampleCardInfoItem(<SyncIcon />, "Transactional Operations", "Cache operations within database transactions")
                        ],
                        rightTitle: "Database Benefits",
                        rightIcon: <TrendingUpIcon />,
                        rightItems: [
                            ExampleCardInfoItem(<FlashOnIcon />, "Reduced DB Load", "Minimize database queries and connection usage"),
                            ExampleCardInfoItem(<SpeedIcon />, "Faster Response Times", "Serve cached data without database roundtrips"),
                            ExampleCardInfoItem(<ShieldIcon />, "Automatic Invalidation", "Smart cache eviction on entity updates"),
                            ExampleCardInfoItem(<InsightsIcon />, "Relationship Caching", "Intelligent caching of entity associations")
                        ],
                        rightBgColor: "success.50"
                    }}
                    footer={{
                        chips: [
                            { icon: <StorageIcon />, label: "JPA Ready", color: "primary" },
                            { icon: <SpeedIcon />, label: "Spring Boot", color: "success" },
                            { icon: <SettingsIcon />, label: "Auto-config", color: "info" }
                        ],
                        performanceText: "🏎️ Database performance boost"
                    }}
                />

                <CodeTabs tabs={[
                    {
                        id: 'spring-jpa-cache',
                        label: 'Spring JPA Cache',
                        language: 'java',
                        code: SPRING_JPA_EXAMPLE
                    }
                ]} />

                {/* Rest API */}
                <EnhancedCard
                    header={{
                        icon: <ApiIcon />,
                        title: "REST API Caching",
                        subtitle: "Optimize API performance and reduce external service calls",
                        bgColor: "primary.main"
                    }}
                    description="Enhance your REST API performance by caching responses, reducing latency, and minimizing external service dependencies. Perfect for high-traffic applications with frequent API calls."
                    features={{
                        leftTitle: "Core Features",
                        leftIcon: <CheckCircleIcon />,
                        leftItems: [
                            ExampleCardInfoItem(<HttpIcon />, "HTTP Response Caching", "Cache GET/POST responses with TTL control"),
                            ExampleCardInfoItem(<PublicIcon />, "External API Caching", "Cache third-party API responses intelligently"),
                            ExampleCardInfoItem(<SettingsIcon />, "Request-Level Control", "Fine-grained cache control per endpoint"),
                            ExampleCardInfoItem(<DeleteIcon />, "Smart Invalidation", "Automatic cache invalidation strategies")
                        ],
                        rightTitle: "Performance Benefits",
                        rightIcon: <TrendingUpIcon />,
                        rightItems: [
                            ExampleCardInfoItem(<FlashOnIcon />, "Faster Response Times", "Reduce latency by up to 90% for cached responses"),
                            ExampleCardInfoItem(<ShieldIcon />, "Reduced External Dependencies", "Minimize risk of third-party service failures"),
                            ExampleCardInfoItem(<DataUsageIcon />, "Lower Bandwidth Usage", "Reduce network traffic and API costs"),
                            ExampleCardInfoItem(<TrendingUpIcon />, "Better Scalability", "Handle more concurrent requests efficiently")
                        ],
                        rightBgColor: "success.50"
                    }}
                    footer={{
                        chips: [
                            { icon: <StarIcon />, label: "Production Ready", color: "primary" },
                            { icon: <CloudIcon />, label: "Microservices", color: "secondary" },
                            { icon: <SpeedIcon />, label: "High Performance", color: "success" },
                            { icon: <SecurityIcon />, label: "Enterprise Grade", color: "info" }
                        ]
                    }}
                />

                <CodeTabs tabs={[
                    {
                        id: 'rest-api-cache',
                        label: 'REST API Cache',
                        language: 'java',
                        code: REST_API_EXAMPLE
                    }
                ]} />

                {/* Heavy Computation Process */}
                <EnhancedCard
                    header={{
                        icon: <ComputerIcon />,
                        title: "Heavy Computation Caching",
                        subtitle: "Cache expensive computations and long-running processes",
                        bgColor: "secondary.main"
                    }}
                    description="Transform your application performance by caching results of expensive computations, complex algorithms, and resource-intensive operations. Essential for applications with heavy computational workloads."
                    features={{
                        leftTitle: "Computation Types",
                        leftIcon: <CheckCircleIcon />,
                        leftItems: [
                            ExampleCardInfoItem(<CalculateIcon />, "Mathematical Computations", "Complex algorithms, statistical analysis, matrix operations"),
                            ExampleCardInfoItem(<DataUsageIcon />, "Data Processing", "Large dataset transformations, aggregations, analytics"),
                            ExampleCardInfoItem(<AssessmentIcon />, "Report Generation", "Complex reports, dashboards, business intelligence"),
                            ExampleCardInfoItem(<PsychologyIcon />, "ML Predictions", "Machine learning inference, model predictions")
                        ],
                        rightTitle: "Smart Optimizations",
                        rightIcon: <LightbulbIcon />,
                        rightItems: [
                            ExampleCardInfoItem(<TimerIcon />, "Intelligent TTL", "Adaptive expiration based on computation cost"),
                            ExampleCardInfoItem(<MemoryIcon />, "Memory Optimization", "Efficient storage for large computation results"),
                            ExampleCardInfoItem(<SyncIcon />, "Async Processing", "Non-blocking computation with future-based caching"),
                            ExampleCardInfoItem(<InsightsIcon />, "Usage Analytics", "Monitor hit rates and computation savings")
                        ],
                        rightBgColor: "warning.50"
                    }}
                    footer={{
                        chips: [
                            { icon: <FlashOnIcon />, label: "Performance Critical", color: "primary" },
                            { icon: <SmartToyIcon />, label: "AI/ML Ready", color: "secondary" },
                            { icon: <EngineeringIcon />, label: "Scientific Computing", color: "success" }
                        ],
                        performanceText: "💡 Save up to 95% computation time"
                    }}
                />

                <CodeTabs tabs={[
                    {
                        id: 'heavy-computation-cache',
                        label: 'Heavy Computation Cache',
                        language: 'java',
                        code: COMPUTATION_SERVICE_EXAMPLE
                    }
                ]} />

                {/* Footer */}
                <Box sx={{ textAlign: 'center', mt: 8, pt: 4, borderTop: 1, borderColor: 'divider' }}>
                    <Typography variant="body2" color="text.secondary">
                        JCacheX Examples - Production-ready caching patterns for Java, Kotlin, and Spring Boot
                    </Typography>
                    <Box sx={{ mt: 2 }}>
                        <Button
                            startIcon={<GitHubIcon />}
                            href="https://github.com/dhruv1110/JCacheX"
                            target="_blank"
                            variant="outlined"
                            sx={{ mr: 2 }}
                        >
                            GitHub
                        </Button>
                        <Button
                            startIcon={<LinkIcon />}
                            href="/docs"
                            variant="outlined"
                        >
                            Documentation
                        </Button>
                    </Box>
                </Box>
            </Container>
        </Layout>
    );
};

export default Examples;
