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

                    <Card sx={{
                        mb: 4,
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: (theme) => theme.shadows[12],
                        }
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Box sx={{
                                    p: 2,
                                    bgcolor: 'primary.main',
                                    color: 'white',
                                    borderRadius: 2,
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <CodeIcon sx={{ fontSize: 28 }} />
                                </Box>
                                <Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        When to use Basic Cache
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Simple, fast caching for everyday use cases
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                                Perfect for getting started with caching or when you need straightforward key-value storage
                                without complex configurations. Ideal for most common caching scenarios.
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                                gap: 3
                            }}>
                                <Paper sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <CheckCircleIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Common Use Cases
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <CachedIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Frequently Accessed Data"
                                                secondary="Cache database query results, API responses"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <AccountTreeIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="User Sessions"
                                                secondary="Store user preferences, shopping carts, temp data"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SettingsIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Configuration Data"
                                                secondary="Application settings, feature flags, lookup tables"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <FunctionsIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Computation Results"
                                                secondary="Cache expensive calculations, processed data"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>

                                <Paper sx={{ p: 3, bgcolor: 'primary.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <StarIcon sx={{ mr: 1, color: 'primary.main' }} />
                                        Key Benefits
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <FlashOnIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Fast Setup"
                                                secondary="Get started in minutes with minimal configuration"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SchoolIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Easy to Learn"
                                                secondary="Simple API suitable for beginners"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <MemoryIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Low Memory Footprint"
                                                secondary="Efficient storage with automatic cleanup"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SecurityIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Thread Safe"
                                                secondary="Concurrent access without additional locking"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Box>

                            <Divider sx={{ my: 3 }} />

                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                <Chip
                                    icon={<StarIcon />}
                                    label="Beginner Friendly"
                                    color="primary"
                                    size="small"
                                />
                                <Chip
                                    icon={<SpeedIcon />}
                                    label="Fast Performance"
                                    color="success"
                                    size="small"
                                />
                                <Chip
                                    icon={<SchoolIcon />}
                                    label="Easy Setup"
                                    color="info"
                                    size="small"
                                />
                            </Box>
                        </CardContent>
                    </Card>

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

                    <Card sx={{
                        mb: 4,
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: (theme) => theme.shadows[12],
                        }
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Box sx={{
                                    p: 2,
                                    bgcolor: 'info.main',
                                    color: 'white',
                                    borderRadius: 2,
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <SettingsIcon sx={{ fontSize: 28 }} />
                                </Box>
                                <Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        Advanced Configuration Features
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Enterprise-grade configurations for production environments
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                                Take full control of your cache behavior with advanced configuration options. Perfect for
                                production environments that require fine-tuned performance, custom eviction policies,
                                and comprehensive monitoring.
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                                gap: 3
                            }}>
                                <Paper sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <CheckCircleIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Configuration Options
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <LayersIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Custom Eviction Strategies"
                                                secondary="LRU, LFU, FIFO, TTL, weight-based policies"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <QueryStatsIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Event Listeners"
                                                secondary="Monitor cache operations, evictions, updates"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <MemoryIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Weight-based Management"
                                                secondary="Dynamic memory allocation based on entry size"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <AccountTreeIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Composite Strategies"
                                                secondary="Combine multiple eviction policies intelligently"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>

                                <Paper sx={{ p: 3, bgcolor: 'info.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <EngineeringIcon sx={{ mr: 1, color: 'info.main' }} />
                                        Enterprise Features
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <InsightsIcon color="info" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Advanced Monitoring"
                                                secondary="Detailed metrics, performance analytics, alerts"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <FlashOnIcon color="info" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Performance Tuning"
                                                secondary="Optimize for specific workload patterns"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <ShieldIcon color="info" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Resource Protection"
                                                secondary="Prevent memory leaks, control resource usage"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <TransformIcon color="info" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Dynamic Reconfiguration"
                                                secondary="Adjust settings without service restart"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Box>

                            <Divider sx={{ my: 3 }} />

                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                <Chip
                                    icon={<EngineeringIcon />}
                                    label="Enterprise Ready"
                                    color="primary"
                                    size="small"
                                />
                                <Chip
                                    icon={<SettingsIcon />}
                                    label="Highly Configurable"
                                    color="info"
                                    size="small"
                                />
                                <Chip
                                    icon={<InsightsIcon />}
                                    label="Production Monitoring"
                                    color="success"
                                    size="small"
                                />
                            </Box>
                        </CardContent>
                    </Card>

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

                    <Card sx={{
                        mb: 4,
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: (theme) => theme.shadows[12],
                        }
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Box sx={{
                                    p: 2,
                                    bgcolor: 'success.main',
                                    color: 'white',
                                    borderRadius: 2,
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <SyncIcon sx={{ fontSize: 28 }} />
                                </Box>
                                <Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        Non-blocking Benefits
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Asynchronous operations that keep your application responsive
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                                Eliminate blocking operations that can freeze your application threads. Perfect for
                                high-throughput systems where responsiveness is critical and every millisecond counts.
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                                gap: 3
                            }}>
                                <Paper sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <CheckCircleIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Performance Improvements
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <FlashOnIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Improved Responsiveness"
                                                secondary="UI stays responsive during cache operations"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <DataUsageIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Better Resource Utilization"
                                                secondary="Efficient CPU and memory usage patterns"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SecurityIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Reduced Thread Contention"
                                                secondary="Minimize lock conflicts and deadlocks"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <ShieldIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Graceful Degradation"
                                                secondary="Maintain service quality under high load"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>

                                <Paper sx={{ p: 3, bgcolor: 'success.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <TrendingUpIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Async Patterns
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <CloudSyncIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="CompletableFuture Support"
                                                secondary="Full async/await pattern integration"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <PlayArrowIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Reactive Streams"
                                                secondary="Backpressure handling and flow control"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <TimerIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Timeout Management"
                                                secondary="Configurable timeouts for async operations"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <RefreshIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Retry Mechanisms"
                                                secondary="Intelligent retry with exponential backoff"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Box>

                            <Divider sx={{ my: 3 }} />

                            <Box sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                flexWrap: 'wrap',
                                gap: 2
                            }}>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                    <Chip
                                        icon={<FlashOnIcon />}
                                        label="High Performance"
                                        color="primary"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<CloudSyncIcon />}
                                        label="Async Ready"
                                        color="success"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<TrendingUpIcon />}
                                        label="Scalable"
                                        color="info"
                                        size="small"
                                    />
                                </Box>
                                <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                                    ⚡ Up to 10x throughput improvement
                                </Typography>
                            </Box>
                        </CardContent>
                    </Card>

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

                    <Card sx={{
                        mb: 4,
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: (theme) => theme.shadows[12],
                        }
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Box sx={{
                                    p: 2,
                                    bgcolor: 'warning.main',
                                    color: 'white',
                                    borderRadius: 2,
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <SecurityIcon sx={{ fontSize: 28 }} />
                                </Box>
                                <Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        Thread Safety Considerations
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Concurrent operations without compromising data integrity
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                                Ensure your cache operations are completely thread-safe in multi-threaded environments.
                                Critical for applications handling concurrent requests where data consistency is paramount.
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                                gap: 3
                            }}>
                                <Paper sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <CheckCircleIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Safety Guarantees
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SyncIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Concurrent Read/Write"
                                                secondary="Safe simultaneous access from multiple threads"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <FlashOnIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Atomic Operations"
                                                secondary="All cache operations are atomic and consistent"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <LayersIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Lock-free Structures"
                                                secondary="High-performance concurrent data structures"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <ShieldIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Consistent State"
                                                secondary="Guaranteed consistency across all operations"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>

                                <Paper sx={{ p: 3, bgcolor: 'warning.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <LightbulbIcon sx={{ mr: 1, color: 'warning.main' }} />
                                        Concurrency Features
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <TrendingUpIcon color="warning" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="High Throughput"
                                                secondary="Optimized for concurrent high-load scenarios"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <TimerIcon color="warning" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Deadlock Prevention"
                                                secondary="Sophisticated locking strategies prevent deadlocks"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <DataUsageIcon color="warning" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Memory Barriers"
                                                secondary="Proper memory synchronization guarantees"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <InsightsIcon color="warning" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Contention Monitoring"
                                                secondary="Built-in metrics for thread contention analysis"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Box>

                            <Divider sx={{ my: 3 }} />

                            <Box sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                flexWrap: 'wrap',
                                gap: 2
                            }}>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                    <Chip
                                        icon={<SecurityIcon />}
                                        label="Thread Safe"
                                        color="primary"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<FlashOnIcon />}
                                        label="Lock-free"
                                        color="warning"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<TrendingUpIcon />}
                                        label="High Concurrency"
                                        color="success"
                                        size="small"
                                    />
                                </Box>
                                <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                                    🔐 100% thread-safe guaranteed
                                </Typography>
                            </Box>
                        </CardContent>
                    </Card>

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

                    <Card sx={{
                        mb: 4,
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: (theme) => theme.shadows[12],
                        }
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Box sx={{
                                    p: 2,
                                    bgcolor: 'secondary.main',
                                    color: 'white',
                                    borderRadius: 2,
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <ArchitectureIcon sx={{ fontSize: 28 }} />
                                </Box>
                                <Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        Multi-Cache Architecture
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Hierarchical caching with intelligent coordination
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                                Design sophisticated multi-tier cache architectures for complex applications. Perfect for
                                enterprise systems requiring different cache strategies for different data types and access patterns.
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                                gap: 3
                            }}>
                                <Paper sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <CheckCircleIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Cache Layers
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <MemoryIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="L1 Memory Cache"
                                                secondary="Fast in-memory storage for hot data"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <StorageIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="L2 Disk Cache"
                                                secondary="Persistent storage for larger datasets"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <LayersIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Domain Partitioning"
                                                secondary="Separate caches for different business domains"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SyncIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Cache Coordination"
                                                secondary="Intelligent synchronization between cache layers"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>

                                <Paper sx={{ p: 3, bgcolor: 'secondary.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <DashboardIcon sx={{ mr: 1, color: 'secondary.main' }} />
                                        Management Features
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <DashboardIcon color="secondary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Unified Management"
                                                secondary="Central control panel for all cache instances"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <InsightsIcon color="secondary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Cross-Cache Analytics"
                                                secondary="Comprehensive metrics across all cache layers"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <ShieldIcon color="secondary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Consistency Guarantees"
                                                secondary="Maintain data consistency across cache tiers"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <TransformIcon color="secondary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Dynamic Scaling"
                                                secondary="Auto-scale cache tiers based on demand"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Box>

                            <Divider sx={{ my: 3 }} />

                            <Box sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                flexWrap: 'wrap',
                                gap: 2
                            }}>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                    <Chip
                                        icon={<ArchitectureIcon />}
                                        label="Multi-Tier"
                                        color="primary"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<LayersIcon />}
                                        label="Hierarchical"
                                        color="secondary"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<EngineeringIcon />}
                                        label="Enterprise Scale"
                                        color="success"
                                        size="small"
                                    />
                                </Box>
                                <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                                    🏗️ Scalable architecture design
                                </Typography>
                            </Box>
                        </CardContent>
                    </Card>

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

                    <Card sx={{
                        mb: 4,
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: (theme) => theme.shadows[12],
                        }
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Box sx={{
                                    p: 2,
                                    bgcolor: 'info.main',
                                    color: 'white',
                                    borderRadius: 2,
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <AnalyticsIcon sx={{ fontSize: 28 }} />
                                </Box>
                                <Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        Observability Features
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Complete monitoring and analytics for production systems
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                                Gain deep insights into your cache performance with comprehensive monitoring, metrics, and
                                alerting capabilities. Essential for maintaining optimal performance in production environments.
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                                gap: 3
                            }}>
                                <Paper sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <CheckCircleIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Monitoring Tools
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <QueryStatsIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Real-time Metrics"
                                                secondary="Hit rates, miss rates, response times, throughput"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <DashboardIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Performance Dashboards"
                                                secondary="Visual analytics with charts and graphs"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <InsightsIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Alerting System"
                                                secondary="Configurable alerts for performance thresholds"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <TimelineIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Distributed Tracing"
                                                secondary="End-to-end request tracking across services"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>

                                <Paper sx={{ p: 3, bgcolor: 'info.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <TrendingUpIcon sx={{ mr: 1, color: 'info.main' }} />
                                        Analytics & Insights
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <AssessmentIcon color="info" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Performance Analytics"
                                                secondary="Deep dive into cache behavior patterns"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <TableChartIcon color="info" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Usage Statistics"
                                                secondary="Detailed breakdowns of cache utilization"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <LightbulbIcon color="info" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Optimization Recommendations"
                                                secondary="AI-powered suggestions for performance tuning"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <DataUsageIcon color="info" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Cost Analysis"
                                                secondary="Resource usage and cost optimization insights"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Box>

                            <Divider sx={{ my: 3 }} />

                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                <Chip
                                    icon={<AnalyticsIcon />}
                                    label="Full Observability"
                                    color="primary"
                                    size="small"
                                />
                                <Chip
                                    icon={<InsightsIcon />}
                                    label="Real-time Monitoring"
                                    color="info"
                                    size="small"
                                />
                                <Chip
                                    icon={<LightbulbIcon />}
                                    label="AI Insights"
                                    color="success"
                                    size="small"
                                />
                            </Box>
                        </CardContent>
                    </Card>

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
                <Box id="extensions-usage" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <ExtensionIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Extensions Usage
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Idiomatic Kotlin extensions for JCacheX providing a more fluent API.
                    </Typography>

                    <Card sx={{
                        mb: 4,
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: (theme) => theme.shadows[12],
                        }
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Box sx={{
                                    p: 2,
                                    bgcolor: 'secondary.main',
                                    color: 'white',
                                    borderRadius: 2,
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <ExtensionIcon sx={{ fontSize: 28 }} />
                                </Box>
                                <Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        Kotlin Extensions Benefits
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Idiomatic Kotlin with a more fluent and expressive API
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                                Leverage Kotlin's powerful language features to create a more expressive and type-safe
                                caching API. Perfect for Kotlin developers who want idiomatic code with enhanced readability.
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                                gap: 3
                            }}>
                                <Paper sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <CheckCircleIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Language Features
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <CodeIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="DSL-style Configuration"
                                                secondary="Builder pattern with lambda expressions"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <FunctionsIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Operator Overloading"
                                                secondary="Intuitive syntax: cache[key] = value"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <ExtensionIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Extension Functions"
                                                secondary="Additional methods on existing cache types"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <ShieldIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Type-safe Builders"
                                                secondary="Compile-time safety with intuitive syntax"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>

                                <Paper sx={{ p: 3, bgcolor: 'secondary.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <StarIcon sx={{ mr: 1, color: 'secondary.main' }} />
                                        Developer Experience
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SchoolIcon color="secondary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Intuitive API"
                                                secondary="Natural Kotlin syntax feels familiar"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <LightbulbIcon color="secondary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="IDE Support"
                                                secondary="Full IntelliJ IDEA autocomplete and refactoring"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <TransformIcon color="secondary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Null Safety"
                                                secondary="Kotlin's null safety prevents runtime errors"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <FlashOnIcon color="secondary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Concise Code"
                                                secondary="Reduce boilerplate with smart defaults"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Box>

                            <Divider sx={{ my: 3 }} />

                            <Box sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                flexWrap: 'wrap',
                                gap: 2
                            }}>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                    <Chip
                                        icon={<ExtensionIcon />}
                                        label="Kotlin Native"
                                        color="primary"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<SchoolIcon />}
                                        label="Developer Friendly"
                                        color="secondary"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<ShieldIcon />}
                                        label="Type Safe"
                                        color="success"
                                        size="small"
                                    />
                                </Box>
                                <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                                    🎯 Idiomatic Kotlin experience
                                </Typography>
                            </Box>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'kotlin-extensions',
                            label: 'Kotlin Extensions',
                            language: 'kotlin',
                            code: EXTENSIONS_EXAMPLE
                        }
                    ]} />
                </Box>

                {/* Coroutines Support */}
                <Box id="coroutines-support" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <CloudSyncIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Coroutines Support
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Seamless integration with Kotlin coroutines for asynchronous caching operations.
                    </Typography>

                    <Card sx={{
                        mb: 4,
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: (theme) => theme.shadows[12],
                        }
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Box sx={{
                                    p: 2,
                                    bgcolor: 'success.main',
                                    color: 'white',
                                    borderRadius: 2,
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <CloudSyncIcon sx={{ fontSize: 28 }} />
                                </Box>
                                <Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        Coroutines Integration
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Seamless async operations with structured concurrency
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                                Harness the power of Kotlin coroutines for truly asynchronous caching operations. Perfect for
                                reactive applications that need non-blocking I/O with structured concurrency and cancellation support.
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                                gap: 3
                            }}>
                                <Paper sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <CheckCircleIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Async Features
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SyncIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Suspend Functions"
                                                secondary="Non-blocking cache operations with suspend/resume"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <TimelineIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Flow-based Events"
                                                secondary="Reactive streams for cache events and updates"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <AccountTreeIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Structured Concurrency"
                                                secondary="Hierarchical task management with scopes"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <DeleteIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Cancellation Support"
                                                secondary="Cooperative cancellation for long-running operations"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>

                                <Paper sx={{ p: 3, bgcolor: 'success.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <TrendingUpIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Performance Benefits
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <FlashOnIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Non-blocking I/O"
                                                secondary="Threads never block waiting for cache operations"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <DataUsageIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Resource Efficiency"
                                                secondary="Handle thousands of concurrent operations"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <ShieldIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Exception Safety"
                                                secondary="Structured exception handling with coroutines"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <InsightsIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Backpressure Control"
                                                secondary="Flow-based backpressure for high-load scenarios"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Box>

                            <Divider sx={{ my: 3 }} />

                            <Box sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                flexWrap: 'wrap',
                                gap: 2
                            }}>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                    <Chip
                                        icon={<CloudSyncIcon />}
                                        label="Coroutines Ready"
                                        color="primary"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<FlashOnIcon />}
                                        label="Non-blocking"
                                        color="success"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<AccountTreeIcon />}
                                        label="Structured"
                                        color="info"
                                        size="small"
                                    />
                                </Box>
                                <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                                    ⚡ Truly reactive caching
                                </Typography>
                            </Box>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'coroutines-cache',
                            label: 'Coroutines Cache',
                            language: 'kotlin',
                            code: COROUTINES_EXAMPLE
                        }
                    ]} />
                </Box>

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
                <Box id="spring-jpa" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <StorageIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Spring JPA
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Integrating JCacheX with Spring Data JPA for efficient database caching.
                    </Typography>

                    <Card sx={{
                        mb: 4,
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: (theme) => theme.shadows[12],
                        }
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Box sx={{
                                    p: 2,
                                    bgcolor: 'success.main',
                                    color: 'white',
                                    borderRadius: 2,
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <StorageIcon sx={{ fontSize: 28 }} />
                                </Box>
                                <Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        Spring JPA Integration
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Seamless database caching with Spring Data JPA
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                                Supercharge your Spring Data JPA applications with intelligent caching. Reduce database load,
                                improve response times, and scale your application effortlessly with automatic cache management.
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                                gap: 3
                            }}>
                                <Paper sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <CheckCircleIcon sx={{ mr: 1, color: 'success.main' }} />
                                        JPA Features
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <StorageIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Repository-level Caching"
                                                secondary="Automatic caching for repository methods"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <QueryStatsIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Query Result Caching"
                                                secondary="Cache complex JPQL and native SQL queries"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <AccountTreeIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Entity-level Management"
                                                secondary="Individual entity caching with relationships"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SyncIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Transactional Operations"
                                                secondary="Cache operations within database transactions"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>

                                <Paper sx={{ p: 3, bgcolor: 'success.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <TrendingUpIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Database Benefits
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <FlashOnIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Reduced DB Load"
                                                secondary="Minimize database queries and connection usage"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SpeedIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Faster Response Times"
                                                secondary="Serve cached data without database roundtrips"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <ShieldIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Automatic Invalidation"
                                                secondary="Smart cache eviction on entity updates"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <InsightsIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Relationship Caching"
                                                secondary="Intelligent caching of entity associations"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Box>

                            <Divider sx={{ my: 3 }} />

                            <Box sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                flexWrap: 'wrap',
                                gap: 2
                            }}>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                    <Chip
                                        icon={<StorageIcon />}
                                        label="JPA Ready"
                                        color="primary"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<SpeedIcon />}
                                        label="Spring Boot"
                                        color="success"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<SettingsIcon />}
                                        label="Auto-config"
                                        color="info"
                                        size="small"
                                    />
                                </Box>
                                <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                                    🏎️ Database performance boost
                                </Typography>
                            </Box>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'spring-jpa-cache',
                            label: 'Spring JPA Cache',
                            language: 'java',
                            code: SPRING_JPA_EXAMPLE
                        }
                    ]} />
                </Box>

                {/* Rest API */}
                <Box id="rest-api" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <ApiIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Rest API
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Caching REST API responses and external service calls in Spring Boot.
                    </Typography>

                    <Card sx={{
                        mb: 4,
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: (theme) => theme.shadows[12],
                        }
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Box sx={{
                                    p: 2,
                                    bgcolor: 'primary.main',
                                    color: 'white',
                                    borderRadius: 2,
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <ApiIcon sx={{ fontSize: 28 }} />
                                </Box>
                                <Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        REST API Caching
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Optimize API performance and reduce external service calls
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                                Enhance your REST API performance by caching responses, reducing latency, and minimizing
                                external service dependencies. Perfect for high-traffic applications with frequent API calls.
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                                gap: 3
                            }}>
                                <Paper sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <CheckCircleIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Core Features
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <HttpIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="HTTP Response Caching"
                                                secondary="Cache GET/POST responses with TTL control"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <PublicIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="External API Caching"
                                                secondary="Cache third-party API responses intelligently"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SettingsIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Request-Level Control"
                                                secondary="Fine-grained cache control per endpoint"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <DeleteIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Smart Invalidation"
                                                secondary="Automatic cache invalidation strategies"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>

                                <Paper sx={{ p: 3, bgcolor: 'success.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <TrendingUpIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Performance Benefits
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <FlashOnIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Faster Response Times"
                                                secondary="Reduce latency by up to 90% for cached responses"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <ShieldIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Reduced External Dependencies"
                                                secondary="Minimize risk of third-party service failures"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <DataUsageIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Lower Bandwidth Usage"
                                                secondary="Reduce network traffic and API costs"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <TrendingUpIcon color="success" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Better Scalability"
                                                secondary="Handle more concurrent requests efficiently"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Box>

                            <Divider sx={{ my: 3 }} />

                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                <Chip
                                    icon={<StarIcon />}
                                    label="Production Ready"
                                    color="primary"
                                    size="small"
                                />
                                <Chip
                                    icon={<CloudIcon />}
                                    label="Microservices"
                                    color="secondary"
                                    size="small"
                                />
                                <Chip
                                    icon={<SpeedIcon />}
                                    label="High Performance"
                                    color="success"
                                    size="small"
                                />
                                <Chip
                                    icon={<SecurityIcon />}
                                    label="Enterprise Grade"
                                    color="info"
                                    size="small"
                                />
                            </Box>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'rest-api-cache',
                            label: 'REST API Cache',
                            language: 'java',
                            code: REST_API_EXAMPLE
                        }
                    ]} />
                </Box>

                {/* Heavy Computation Process */}
                <Box id="heavy-computation-process" sx={{ mb: 6 }}>
                    <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <ComputerIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Heavy Computation Process
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Caching results of expensive computations and long-running processes.
                    </Typography>

                    <Card sx={{
                        mb: 4,
                        transition: 'all 0.3s ease-in-out',
                        '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: (theme) => theme.shadows[12],
                        }
                    }}>
                        <CardContent sx={{ p: 4 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                <Box sx={{
                                    p: 2,
                                    bgcolor: 'secondary.main',
                                    color: 'white',
                                    borderRadius: 2,
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    <ComputerIcon sx={{ fontSize: 28 }} />
                                </Box>
                                <Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        Heavy Computation Caching
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Cache expensive computations and long-running processes
                                    </Typography>
                                </Box>
                            </Box>

                            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                                Transform your application performance by caching results of expensive computations,
                                complex algorithms, and resource-intensive operations. Essential for applications with
                                heavy computational workloads.
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                                gap: 3
                            }}>
                                <Paper sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <CheckCircleIcon sx={{ mr: 1, color: 'success.main' }} />
                                        Computation Types
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <CalculateIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Mathematical Computations"
                                                secondary="Complex algorithms, statistical analysis, matrix operations"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <DataUsageIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Data Processing"
                                                secondary="Large dataset transformations, aggregations, analytics"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <AssessmentIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Report Generation"
                                                secondary="Complex reports, dashboards, business intelligence"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <PsychologyIcon color="primary" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="ML Predictions"
                                                secondary="Machine learning inference, model predictions"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>

                                <Paper sx={{ p: 3, bgcolor: 'warning.50', borderRadius: 2 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                                        <LightbulbIcon sx={{ mr: 1, color: 'warning.main' }} />
                                        Smart Optimizations
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <TimerIcon color="warning" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Intelligent TTL"
                                                secondary="Adaptive expiration based on computation cost"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <MemoryIcon color="warning" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Memory Optimization"
                                                secondary="Efficient storage for large computation results"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <SyncIcon color="warning" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Async Processing"
                                                secondary="Non-blocking computation with future-based caching"
                                            />
                                        </ListItem>
                                        <ListItem sx={{ pl: 0 }}>
                                            <ListItemIcon sx={{ minWidth: 32 }}>
                                                <InsightsIcon color="warning" fontSize="small" />
                                            </ListItemIcon>
                                            <ListItemText
                                                primary="Usage Analytics"
                                                secondary="Monitor hit rates and computation savings"
                                            />
                                        </ListItem>
                                    </List>
                                </Paper>
                            </Box>

                            <Divider sx={{ my: 3 }} />

                            <Box sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                flexWrap: 'wrap',
                                gap: 2
                            }}>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                    <Chip
                                        icon={<FlashOnIcon />}
                                        label="Performance Critical"
                                        color="primary"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<SmartToyIcon />}
                                        label="AI/ML Ready"
                                        color="secondary"
                                        size="small"
                                    />
                                    <Chip
                                        icon={<EngineeringIcon />}
                                        label="Scientific Computing"
                                        color="success"
                                        size="small"
                                    />
                                </Box>
                                <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                                    💡 Save up to 95% computation time
                                </Typography>
                            </Box>
                        </CardContent>
                    </Card>

                    <CodeTabs tabs={[
                        {
                            id: 'heavy-computation-cache',
                            label: 'Heavy Computation Cache',
                            language: 'java',
                            code: COMPUTATION_SERVICE_EXAMPLE
                        }
                    ]} />
                </Box>

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
