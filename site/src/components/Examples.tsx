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
    Chip,
    Alert,
    Button,
    Stack,
    useTheme,
    useMediaQuery
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

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                When to use Basic Cache:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Simple key-value caching for frequently accessed data" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="User session management and preferences" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Configuration data and reference lookups" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Caching expensive computation results" />
                                </ListItem>
                            </List>
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

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Advanced Configuration Features:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Custom eviction strategies and policies" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Cache event listeners for monitoring" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Weight-based memory management" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Composite eviction strategies" />
                                </ListItem>
                            </List>
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

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Non-blocking Benefits:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Improved application responsiveness" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Better resource utilization" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Reduced thread contention" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Graceful degradation under load" />
                                </ListItem>
                            </List>
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

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Thread Safety Considerations:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Concurrent read/write operations" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Atomic cache operations" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Lock-free data structures" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Consistent state management" />
                                </ListItem>
                            </List>
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

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Multi-Cache Architecture:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="L1 (Memory) and L2 (Disk) cache layers" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Domain-specific cache partitioning" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Cache coordination and consistency" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Unified cache management" />
                                </ListItem>
                            </List>
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

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Observability Features:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Real-time metrics collection" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Performance dashboards" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Alerting and notifications" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Distributed tracing" />
                                </ListItem>
                            </List>
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

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Kotlin Extensions Benefits:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="DSL-style configuration" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Operator overloading for intuitive access" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Extension functions for common operations" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Type-safe builders" />
                                </ListItem>
                            </List>
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

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Coroutines Integration:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Suspend functions for non-blocking operations" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Flow-based cache events" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Structured concurrency support" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Cancellation-aware operations" />
                                </ListItem>
                            </List>
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

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Spring JPA Integration:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Repository-level caching" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Query result caching" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Entity-level cache management" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Transactional cache operations" />
                                </ListItem>
                            </List>
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

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                REST API Caching:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="HTTP response caching" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="External API call caching" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Request-level cache control" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Cache invalidation strategies" />
                                </ListItem>
                            </List>
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

                    <Card sx={{ mb: 4 }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Heavy Computation Caching:
                            </Typography>
                            <List>
                                <ListItem>
                                    <ListItemText primary="Mathematical computations" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Data processing results" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Report generation" />
                                </ListItem>
                                <ListItem>
                                    <ListItemText primary="Machine learning predictions" />
                                </ListItem>
                            </List>
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
