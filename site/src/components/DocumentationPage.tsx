import React from 'react';
import { Link } from 'react-router-dom';
import {
    Box,
    Container,
    Typography,
    Card,
    CardContent,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    Chip,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Alert,
    useTheme,
    useMediaQuery,
    Button,
    Accordion,
    AccordionSummary,
    AccordionDetails,
    Stack,
} from '@mui/material';
import {
    Check as CheckIcon,
    Build as BuildIcon,
    Info as InfoIcon,
    Rocket as RocketIcon,
    Link as LinkIcon,
    Coffee as JavaIcon,
    Android as AndroidIcon,
    Sync as SyncIcon,
    GitHub as GitHubIcon,
    CloudSync as CloudSyncIcon,
    Analytics as AnalyticsIcon,
    Memory as MemoryIcon,
    Security as SecurityIcon,
    ExpandMore as ExpandMoreIcon,
    Api as ApiIcon,
    Architecture as ArchitectureIcon,
    Storage as StorageIcon,
    Timer as TimerIcon,
    Extension as ExtensionIcon,
    Speed as SpeedIcon,
    Dashboard as DashboardIcon,
    Settings as SettingsIcon,
    Code as CodeIcon,
} from '@mui/icons-material';
import CodeTabs from './CodeTabs';
import PageWrapper from './PageWrapper';
import Layout from './Layout';

const DocumentationPage: React.FC = () => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    const platformsData = [
        {
            name: 'Java 8+',
            icon: <JavaIcon />,
            version: '8, 11, 17, 21',
            status: 'Fully Supported'
        },
        {
            name: 'Kotlin',
            icon: <ExtensionIcon />,
            version: '1.8+',
            status: 'Native Support'
        },
        {
            name: 'Spring Boot',
            icon: <SpeedIcon />,
            version: '2.7+, 3.x',
            status: 'Auto-Configuration'
        },
        {
            name: 'Android',
            icon: <AndroidIcon />,
            version: 'API 21+',
            status: 'Compatible'
        }
    ];

    const evictionStrategies = [
        {
            name: 'TinyWindowLFU (Default)',
            description: 'High-performance hybrid eviction combining recency and frequency',
            useCase: 'Optimal for most workloads - combines benefits of LRU and LFU',
            performance: 'O(1) operations, best overall performance',
            icon: <DashboardIcon />,
            className: 'WindowTinyLFUEvictionStrategy',
            highlight: true
        },
        {
            name: 'Enhanced LRU',
            description: 'LRU with optional frequency sketch for improved accuracy',
            useCase: 'Temporal locality with optional frequency awareness',
            performance: 'O(1) operations with frequency tracking',
            icon: <DashboardIcon />,
            className: 'EnhancedLRUEvictionStrategy'
        },
        {
            name: 'Enhanced LFU',
            description: 'LFU with frequency buckets and optional sketch',
            useCase: 'Workloads with clear frequency patterns',
            performance: 'O(1) operations with frequency buckets',
            icon: <AnalyticsIcon />,
            className: 'EnhancedLFUEvictionStrategy'
        },
        {
            name: 'LRU (Least Recently Used)',
            description: 'Evicts the least recently accessed entries first',
            useCase: 'General purpose caching, temporal locality',
            performance: 'O(1) operations with doubly-linked list',
            icon: <DashboardIcon />,
            className: 'LRUEvictionStrategy'
        },
        {
            name: 'LFU (Least Frequently Used)',
            description: 'Evicts the least frequently accessed entries',
            useCase: 'Data with clear frequency patterns',
            performance: 'O(1) operations with frequency buckets',
            icon: <AnalyticsIcon />,
            className: 'LFUEvictionStrategy'
        },
        {
            name: 'FIFO (First In, First Out)',
            description: 'Evicts entries in insertion order',
            useCase: 'Simple queue-like behavior',
            performance: 'O(1) operations with queue structure',
            icon: <StorageIcon />,
            className: 'FIFOEvictionStrategy'
        },
        {
            name: 'FILO (First In, Last Out)',
            description: 'Evicts entries in reverse insertion order',
            useCase: 'Stack-like behavior',
            performance: 'O(1) operations with stack structure',
            icon: <StorageIcon />,
            className: 'FILOEvictionStrategy'
        },
        {
            name: 'Weight-Based',
            description: 'Evicts entries based on custom weight calculation',
            useCase: 'Memory-conscious caching',
            performance: 'O(1) operations with read-write locks',
            icon: <MemoryIcon />,
            className: 'WeightBasedEvictionStrategy'
        },
        {
            name: 'Time-Based',
            description: 'Evicts entries based on idle time',
            useCase: 'TTL-based cache management',
            performance: 'O(n) scan for expired items',
            icon: <TimerIcon />,
            className: 'IdleTimeEvictionStrategy'
        }
    ];

    const configurationOptions = [
        {
            category: 'Size Limits',
            options: [
                { name: 'maximumSize', description: 'Maximum number of entries', example: '1000L' },
                { name: 'maximumWeight', description: 'Maximum total weight', example: '10000L' },
                { name: 'weigher', description: 'Custom weight calculation', example: '(key, value) -> value.size()' }
            ]
        },
        {
            category: 'Expiration',
            options: [
                { name: 'expireAfterWrite', description: 'Expire after write duration', example: 'Duration.ofMinutes(30)' },
                { name: 'expireAfterAccess', description: 'Expire after access duration', example: 'Duration.ofMinutes(10)' },
                { name: 'refreshAfterWrite', description: 'Refresh after write duration', example: 'Duration.ofMinutes(5)' }
            ]
        },
        {
            category: 'Performance',
            options: [
                { name: 'recordStats', description: 'Enable statistics collection', example: 'true' },
                { name: 'initialCapacity', description: 'Initial cache capacity', example: '32' },
                { name: 'concurrencyLevel', description: 'Concurrency level', example: '8' }
            ]
        }
    ];

    const kotlinExtensions = [
        {
            name: 'Operator Overloading',
            description: 'Array-like syntax for cache operations',
            examples: [
                'cache["key"] = value',
                'val value = cache["key"]',
                'cache += "key" to value',
                'cache -= "key"'
            ]
        },
        {
            name: 'Coroutine Support',
            description: 'Suspending functions for async operations',
            examples: [
                'cache.getOrPut("key") { suspendingFunction() }',
                'cache.getDeferred("key", scope)',
                'cache.putAsync("key", value)'
            ]
        },
        {
            name: 'Collection Operations',
            description: 'Collection-like operations for filtering and mapping',
            examples: [
                'cache.filterValues { it.isActive }',
                'cache.mapValues { it.toUpperCase() }',
                'cache.forEach { key, value -> ... }'
            ]
        },
        {
            name: 'DSL Configuration',
            description: 'Kotlin DSL for fluent configuration',
            examples: [
                'val cache = createCache<String, User> {',
                '    maximumSize(1000L)',
                '    expireAfterWrite(Duration.ofMinutes(30))',
                '}'
            ]
        }
    ];

    // Define navigation items for the sidebar
    const navigationItems = [
        {
            id: 'introduction',
            title: 'Introduction',
            icon: <InfoIcon />,
            children: [
                { id: 'what-is-jcachex', title: 'What is JCacheX?', icon: <InfoIcon /> },
                { id: 'supported-platforms', title: 'Supported Platforms', icon: <AndroidIcon /> },
                { id: 'quick-start', title: '1 Minute Quick Start', icon: <RocketIcon /> },
            ],
        },
        {
            id: 'cache-profiles',
            title: 'Cache Profiles',
            icon: <ArchitectureIcon />,
            children: [
                { id: 'profiles-overview', title: 'Profiles Overview', icon: <InfoIcon /> },
                { id: 'core-profiles', title: 'Core Profiles', icon: <DashboardIcon /> },
                { id: 'specialized-profiles', title: 'Specialized Profiles', icon: <SettingsIcon /> },
                { id: 'advanced-profiles', title: 'Advanced Profiles', icon: <SpeedIcon /> },
                { id: 'profile-selection-guide', title: 'Selection Guide', icon: <SecurityIcon /> },
            ],
        },
        {
            id: 'advanced-configurations',
            title: 'Advanced Configurations',
            icon: <SettingsIcon />,
            children: [
                { id: 'eviction-strategies', title: 'Eviction Strategies', icon: <DashboardIcon /> },
                { id: 'all-configuration-options', title: 'All Configuration Options', icon: <ApiIcon /> },
                { id: 'kotlin-extensions', title: 'Kotlin Extensions', icon: <ExtensionIcon /> },
            ],
        },
        {
            id: 'java-configuration',
            title: 'Java Configuration',
            icon: <JavaIcon />,
            children: [
                { id: 'java-basic-usage', title: 'Basic Usage', icon: <CodeIcon /> },
                { id: 'java-advanced-features', title: 'Advanced Features', icon: <BuildIcon /> },
                { id: 'java-best-practices', title: 'Best Practices', icon: <SecurityIcon /> },
            ],
        },
        {
            id: 'kotlin-configuration',
            title: 'Kotlin Configuration',
            icon: <ExtensionIcon />,
            children: [
                { id: 'kotlin-dsl', title: 'DSL Configuration', icon: <CodeIcon /> },
                { id: 'kotlin-coroutines', title: 'Coroutine Support', icon: <CloudSyncIcon /> },
                { id: 'kotlin-operators', title: 'Operator Overloading', icon: <ApiIcon /> },
            ],
        },
        {
            id: 'spring-boot-configuration',
            title: 'Spring Boot Configuration',
            icon: <SpeedIcon />,
            children: [
                { id: 'spring-annotations', title: 'Annotations', icon: <CodeIcon /> },
                { id: 'spring-properties', title: 'Properties Configuration', icon: <SettingsIcon /> },
                { id: 'spring-auto-configuration', title: 'Auto Configuration', icon: <BuildIcon /> },
            ],
        },
    ];

    const sidebarConfig = {
        title: "Documentation",
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

                {/* Header */}
                <Box sx={{ textAlign: 'center', mb: 6 }}>
                    <Typography variant="h2" component="h1" gutterBottom sx={{ fontWeight: 700, mb: 2 }}>
                        JCacheX Documentation
                    </Typography>
                    <Typography variant="h5" color="text.secondary" sx={{ mb: 4 }}>
                        Comprehensive guide to high-performance caching in Java and Kotlin
                    </Typography>
                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr 1fr', sm: '1fr 1fr 1fr 1fr' },
                        gap: 2,
                        justifyItems: 'center',
                        maxWidth: '600px',
                        mx: 'auto'
                    }}>
                        <Chip
                            icon={<RocketIcon />}
                            label="High Performance"
                            color="primary"
                            sx={{ px: 2, py: 1 }}
                        />
                        <Chip
                            icon={<ApiIcon />}
                            label="Simple API"
                            color="secondary"
                            sx={{ px: 2, py: 1 }}
                        />
                        <Chip
                            icon={<CloudSyncIcon />}
                            label="Async Support"
                            color="success"
                            sx={{ px: 2, py: 1 }}
                        />
                        <Chip
                            icon={<SpeedIcon />}
                            label="Spring Integration"
                            color="info"
                            sx={{ px: 2, py: 1 }}
                        />
                    </Box>
                </Box>

                {/* Introduction Section */}
                <Box id="introduction" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        Introduction
                    </Typography>

                    {/* What is JCacheX */}
                    <Box id="what-is-jcachex" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <InfoIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            What is JCacheX?
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                            JCacheX is a high-performance, modern caching library for Java and Kotlin applications.
                            It provides a simple, intuitive API while offering enterprise-grade features for production use.
                        </Typography>

                        <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                            <RocketIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            Core Features
                        </Typography>
                        <List dense sx={{ mb: 3 }}>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="High Performance"
                                    secondary="Optimized for speed with minimal overhead and efficient memory usage"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="Simple API"
                                    secondary="Intuitive, fluent interface that's easy to learn and use"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="Async Support"
                                    secondary="Built-in CompletableFuture and Kotlin Coroutines support for non-blocking operations"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="Spring Integration"
                                    secondary="Seamless Spring Boot integration with annotations and auto-configuration"
                                />
                            </ListItem>
                        </List>

                        <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                            <BuildIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            Enterprise Features
                        </Typography>
                        <List dense sx={{ mb: 3 }}>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="Distributed Caching"
                                    secondary="Multi-node clustering with consistency guarantees and automatic failover"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="Comprehensive Monitoring"
                                    secondary="Built-in metrics, observability, and health checks for production monitoring"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="Flexible Configuration"
                                    secondary="Multiple eviction strategies, expiration policies, and custom cache loaders"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="Production Ready"
                                    secondary="Circuit breakers, cache warming, graceful degradation, and resilience patterns"
                                />
                            </ListItem>
                        </List>
                    </Box>

                    {/* Supported Platforms */}
                    <Box id="supported-platforms" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <AndroidIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            Supported Platforms
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                            JCacheX is designed to work seamlessly across different platforms and frameworks.
                            Below are the officially supported platforms with their version requirements:
                        </Typography>

                        <Stack spacing={2} sx={{ mb: 4 }}>
                            {platformsData.map((platform, index) => (
                                <Box key={index} sx={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    p: { xs: 1.5, sm: 2 }, // Responsive padding
                                    borderRadius: 1,
                                    backgroundColor: 'grey.50',
                                    border: '1px solid',
                                    borderColor: 'grey.200',
                                    flexDirection: { xs: 'column', sm: 'row' }, // Stack on mobile
                                    textAlign: { xs: 'center', sm: 'left' },
                                    gap: { xs: 1, sm: 0 }
                                }}>
                                    <Box sx={{ color: 'primary.main', mr: { xs: 0, sm: 2 } }}>
                                        {platform.icon}
                                    </Box>
                                    <Box sx={{ flex: 1 }}>
                                        <Typography variant="h6" sx={{ fontWeight: 600, mb: 0.5 }}>
                                            {platform.name}
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary">
                                            Version: {platform.version}
                                        </Typography>
                                    </Box>
                                    <Chip
                                        label={platform.status}
                                        color="success"
                                        size="small"
                                        sx={{ fontWeight: 500, mt: { xs: 1, sm: 0 } }}
                                    />
                                </Box>
                            ))}
                        </Stack>

                        <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                            Platform-Specific Features
                        </Typography>
                        <Box sx={{
                            display: 'grid',
                            gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                            gap: 3,
                            mt: 2
                        }}>
                            <Box sx={{
                                p: 2,
                                borderRadius: 2,
                                backgroundColor: 'primary.50',
                                border: '1px solid',
                                borderColor: 'primary.100',
                                display: 'flex',
                                alignItems: 'flex-start',
                                gap: 2
                            }}>
                                <JavaIcon color="primary" sx={{ mt: 0.5 }} />
                                <Box>
                                    <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 1 }}>
                                        Java 8+ Support
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Full compatibility with Java 8 through 21+, including Virtual Threads in Java 21
                                    </Typography>
                                </Box>
                            </Box>
                            <Box sx={{
                                p: 2,
                                borderRadius: 2,
                                backgroundColor: 'secondary.50',
                                border: '1px solid',
                                borderColor: 'secondary.100',
                                display: 'flex',
                                alignItems: 'flex-start',
                                gap: 2
                            }}>
                                <ExtensionIcon color="secondary" sx={{ mt: 0.5 }} />
                                <Box>
                                    <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 1 }}>
                                        Kotlin Native Extensions
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Operator overloading, coroutines, and DSL builders for idiomatic Kotlin code
                                    </Typography>
                                </Box>
                            </Box>
                            <Box sx={{
                                p: 2,
                                borderRadius: 2,
                                backgroundColor: 'success.50',
                                border: '1px solid',
                                borderColor: 'success.100',
                                display: 'flex',
                                alignItems: 'flex-start',
                                gap: 2
                            }}>
                                <SpeedIcon color="success" sx={{ mt: 0.5 }} />
                                <Box>
                                    <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 1 }}>
                                        Spring Boot Auto-Configuration
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Zero-configuration setup with Spring Boot 2.7+ and 3.x support
                                    </Typography>
                                </Box>
                            </Box>
                            <Box sx={{
                                p: 2,
                                borderRadius: 2,
                                backgroundColor: 'info.50',
                                border: '1px solid',
                                borderColor: 'info.100',
                                display: 'flex',
                                alignItems: 'flex-start',
                                gap: 2
                            }}>
                                <AndroidIcon color="info" sx={{ mt: 0.5 }} />
                                <Box>
                                    <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 1 }}>
                                        Android Compatibility
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Optimized for Android API 21+ with ProGuard and R8 support
                                    </Typography>
                                </Box>
                            </Box>
                        </Box>
                    </Box>

                    {/* Quick Start */}
                    <Box id="quick-start" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <RocketIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            1 Minute Quick Start
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                            Get started with JCacheX in less than a minute. Choose your preferred language and follow the simple setup.
                            Each example below provides a complete working solution that you can copy and run immediately.
                        </Typography>
                        <CodeTabs
                            tabs={[
                                {
                                    id: 'java',
                                    label: 'Java',
                                    language: 'java',
                                    code: `// Maven dependency
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>1.0.0</version>
</dependency>

// Basic usage
import io.github.dhruv1110.jcachex.*;

// Create cache
CacheConfig<String, User> config = CacheConfig.<String, User>builder()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();

Cache<String, User> cache = new DefaultCache<>(config);

// Use cache
cache.put("user123", new User("Alice"));
User user = cache.get("user123");`
                                },
                                {
                                    id: 'kotlin',
                                    label: 'Kotlin',
                                    language: 'kotlin',
                                    code: `// Gradle dependency
implementation 'io.github.dhruv1110:jcachex-kotlin:1.0.0'

// Kotlin DSL usage
import io.github.dhruv1110.jcachex.kotlin.*

// Create cache with DSL
val cache = createCache<String, User> {
    maximumSize(1000)
    expireAfterWrite(Duration.ofMinutes(30))
}

// Use cache with operators
cache["user123"] = User("Alice")
val user = cache["user123"]`
                                },
                                {
                                    id: 'spring',
                                    label: 'Spring Boot',
                                    language: 'java',
                                    code: `// Maven dependency
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-spring</artifactId>
    <version>1.0.0</version>
</dependency>

// Configuration
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new JCacheXCacheManager();
    }
}

// Usage with annotations
@Service
public class UserService {
    @JCacheXCacheable(cacheName = "users")
    public User findUser(String id) {
        return userRepository.findById(id);
    }
}`
                                }
                            ]}
                        />
                    </Box>
                </Box>

                {/* Cache Profiles Section */}
                <Box id="cache-profiles" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <ArchitectureIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Cache Profiles
                    </Typography>

                    {/* Profiles Overview */}
                    <Box id="profiles-overview" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <InfoIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            Profiles Overview
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                            JCacheX profiles eliminate complex configuration decisions by providing pre-optimized settings
                            for specific use cases. Simply choose a profile that matches your workload characteristics
                            and get optimal performance automatically.
                        </Typography>

                        <Alert severity="success" sx={{ mb: 4 }}>
                            <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                                Why Use Profiles?
                            </Typography>
                            <Typography variant="body2" sx={{ mb: 2 }}>
                                Profiles automatically configure eviction strategies, initial capacities, concurrency levels,
                                and other performance-critical settings based on real-world usage patterns.
                            </Typography>
                            <List dense>
                                <ListItem sx={{ py: 0 }}>
                                    <ListItemText
                                        primary="• No guessing about optimal configurations"
                                        primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                </ListItem>
                                <ListItem sx={{ py: 0 }}>
                                    <ListItemText
                                        primary="• Performance optimized for specific workloads"
                                        primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                </ListItem>
                                <ListItem sx={{ py: 0 }}>
                                    <ListItemText
                                        primary="• Easy to switch between profiles as needs change"
                                        primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                </ListItem>
                                <ListItem sx={{ py: 0 }}>
                                    <ListItemText
                                        primary="• Override defaults when needed for fine-tuning"
                                        primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                </ListItem>
                            </List>
                        </Alert>

                        <CodeTabs
                            tabs={[
                                {
                                    id: 'java-profiles',
                                    label: 'Java',
                                    language: 'java',
                                    code: `// Profile-based approach - automatically optimized
Cache<String, Product> productCache = CacheBuilder
    .profile("READ_HEAVY")  // Optimized for read-intensive workloads
    .name("products")
    .maximumSize(5000L)     // Override default if needed
    .build();

// Session storage with built-in TTL
Cache<String, UserSession> sessionCache = CacheBuilder
    .profile("SESSION_CACHE")  // Pre-configured TTL and size
    .name("sessions")
    .build();

// Distributed caching across nodes
Cache<String, Order> orderCache = CacheBuilder
    .profile("DISTRIBUTED")
    .name("orders")
    .clusterNodes("cache-1:8080", "cache-2:8080")
    .build();`
                                },
                                {
                                    id: 'kotlin-profiles',
                                    label: 'Kotlin',
                                    language: 'kotlin',
                                    code: `// Profile-based approach with Kotlin DSL
val productCache = createCache<String, Product> {
    profile("READ_HEAVY")  // Optimized for read-intensive workloads
    name("products")
    maximumSize(5000)      // Override default if needed
}

// Session storage with built-in TTL
val sessionCache = createCache<String, UserSession> {
    profile("SESSION_CACHE")  // Pre-configured TTL and size
    name("sessions")
}

// Distributed caching across nodes
val orderCache = createCache<String, Order> {
    profile("DISTRIBUTED")
    name("orders")
    clusterNodes("cache-1:8080", "cache-2:8080")
}`
                                }
                            ]}
                        />
                    </Box>

                    {/* Core Profiles */}
                    <Box id="core-profiles" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <DashboardIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            Core Profiles
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                            Core profiles cover 80% of use cases with proven configurations for common access patterns.
                        </Typography>

                        <TableContainer component={Paper} sx={{ mb: 4 }}>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell><strong>Profile</strong></TableCell>
                                        <TableCell><strong>Best For</strong></TableCell>
                                        <TableCell><strong>Eviction Strategy</strong></TableCell>
                                        <TableCell><strong>Default Size</strong></TableCell>
                                        <TableCell><strong>Performance</strong></TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    <TableRow>
                                        <TableCell><code>DEFAULT</code></TableCell>
                                        <TableCell>General-purpose caching</TableCell>
                                        <TableCell>TinyWindowLFU</TableCell>
                                        <TableCell>1,000</TableCell>
                                        <TableCell>40.4ns GET</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell><code>READ_HEAVY</code></TableCell>
                                        <TableCell>80%+ read operations</TableCell>
                                        <TableCell>Enhanced LFU</TableCell>
                                        <TableCell>1,000</TableCell>
                                        <TableCell>11.5ns GET</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell><code>WRITE_HEAVY</code></TableCell>
                                        <TableCell>50%+ write operations</TableCell>
                                        <TableCell>Enhanced LRU</TableCell>
                                        <TableCell>1,000</TableCell>
                                        <TableCell>393.5ns PUT</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell><code>MEMORY_EFFICIENT</code></TableCell>
                                        <TableCell>Memory-constrained environments</TableCell>
                                        <TableCell>LRU</TableCell>
                                        <TableCell>100</TableCell>
                                        <TableCell>39.7ns GET</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell><code>HIGH_PERFORMANCE</code></TableCell>
                                        <TableCell>Maximum throughput</TableCell>
                                        <TableCell>Enhanced LFU</TableCell>
                                        <TableCell>10,000</TableCell>
                                        <TableCell>24.6ns GET</TableCell>
                                    </TableRow>
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </Box>

                    {/* Specialized Profiles */}
                    <Box id="specialized-profiles" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <SettingsIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            Specialized Profiles
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                            Specialized profiles are optimized for specific scenarios with built-in configurations.
                        </Typography>

                        <TableContainer component={Paper} sx={{ mb: 4 }}>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell><strong>Profile</strong></TableCell>
                                        <TableCell><strong>Best For</strong></TableCell>
                                        <TableCell><strong>Eviction Strategy</strong></TableCell>
                                        <TableCell><strong>Default Size</strong></TableCell>
                                        <TableCell><strong>TTL</strong></TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    <TableRow>
                                        <TableCell><code>SESSION_CACHE</code></TableCell>
                                        <TableCell>User session storage</TableCell>
                                        <TableCell>LRU</TableCell>
                                        <TableCell>2,000</TableCell>
                                        <TableCell>30 minutes</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell><code>API_CACHE</code></TableCell>
                                        <TableCell>External API responses</TableCell>
                                        <TableCell>TinyWindowLFU</TableCell>
                                        <TableCell>500</TableCell>
                                        <TableCell>15 minutes</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell><code>COMPUTE_CACHE</code></TableCell>
                                        <TableCell>Expensive computations</TableCell>
                                        <TableCell>Enhanced LFU</TableCell>
                                        <TableCell>1,000</TableCell>
                                        <TableCell>2 hours</TableCell>
                                    </TableRow>
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </Box>

                    {/* Advanced Profiles */}
                    <Box id="advanced-profiles" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <SpeedIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            Advanced Profiles
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                            Advanced profiles provide cutting-edge optimizations for specialized requirements.
                        </Typography>

                        <TableContainer component={Paper} sx={{ mb: 4 }}>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell><strong>Profile</strong></TableCell>
                                        <TableCell><strong>Best For</strong></TableCell>
                                        <TableCell><strong>Special Features</strong></TableCell>
                                        <TableCell><strong>Default Size</strong></TableCell>
                                        <TableCell><strong>Performance</strong></TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    <TableRow>
                                        <TableCell><code>ML_OPTIMIZED</code></TableCell>
                                        <TableCell>Machine learning workloads</TableCell>
                                        <TableCell>Predictive caching</TableCell>
                                        <TableCell>500</TableCell>
                                        <TableCell>Adaptive</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell><code>ZERO_COPY</code></TableCell>
                                        <TableCell>Ultra-low latency (HFT)</TableCell>
                                        <TableCell>Direct memory buffers</TableCell>
                                        <TableCell>10,000</TableCell>
                                        <TableCell>7.9ns GET</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell><code>HARDWARE_OPTIMIZED</code></TableCell>
                                        <TableCell>CPU-intensive workloads</TableCell>
                                        <TableCell>SIMD optimizations</TableCell>
                                        <TableCell>1,000</TableCell>
                                        <TableCell>24.7ns GET</TableCell>
                                    </TableRow>
                                    <TableRow>
                                        <TableCell><code>DISTRIBUTED</code></TableCell>
                                        <TableCell>Multi-node clustering</TableCell>
                                        <TableCell>Network-aware</TableCell>
                                        <TableCell>5,000</TableCell>
                                        <TableCell>Network-dependent</TableCell>
                                    </TableRow>
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </Box>

                    {/* Profile Selection Guide */}
                    <Box id="profile-selection-guide" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            Profile Selection Guide
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                            Choose the right profile based on your application's characteristics and requirements.
                        </Typography>

                        <Box sx={{
                            display: 'grid',
                            gridTemplateColumns: { xs: '1fr', lg: 'repeat(2, 1fr)' },
                            gap: 4,
                            mb: 4
                        }}>
                            <Card>
                                <CardContent>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, color: 'primary.main' }}>
                                        🎯 Read-Heavy Applications
                                    </Typography>
                                    <Typography variant="body2" sx={{ mb: 2 }}>
                                        Choose when 80%+ of operations are reads:
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• Reference data and configuration" />
                                        </ListItem>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• Product catalogs and menus" />
                                        </ListItem>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• Static content and templates" />
                                        </ListItem>
                                    </List>
                                    <Chip label="Use: READ_HEAVY" color="primary" size="small" sx={{ mt: 2 }} />
                                </CardContent>
                            </Card>

                            <Card>
                                <CardContent>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, color: 'secondary.main' }}>
                                        ✏️ Write-Heavy Applications
                                    </Typography>
                                    <Typography variant="body2" sx={{ mb: 2 }}>
                                        Choose when 50%+ of operations are writes:
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• User activity tracking" />
                                        </ListItem>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• Logging and analytics" />
                                        </ListItem>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• Real-time data processing" />
                                        </ListItem>
                                    </List>
                                    <Chip label="Use: WRITE_HEAVY" color="secondary" size="small" sx={{ mt: 2 }} />
                                </CardContent>
                            </Card>

                            <Card>
                                <CardContent>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, color: 'info.main' }}>
                                        💾 Memory-Constrained
                                    </Typography>
                                    <Typography variant="body2" sx={{ mb: 2 }}>
                                        Choose when memory is limited:
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• Embedded systems" />
                                        </ListItem>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• Mobile applications" />
                                        </ListItem>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• Containerized microservices" />
                                        </ListItem>
                                    </List>
                                    <Chip label="Use: MEMORY_EFFICIENT" color="info" size="small" sx={{ mt: 2 }} />
                                </CardContent>
                            </Card>

                            <Card>
                                <CardContent>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, color: 'success.main' }}>
                                        🚀 High-Performance
                                    </Typography>
                                    <Typography variant="body2" sx={{ mb: 2 }}>
                                        Choose when maximum throughput is needed:
                                    </Typography>
                                    <List dense>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• Trading systems" />
                                        </ListItem>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• Gaming backends" />
                                        </ListItem>
                                        <ListItem sx={{ py: 0 }}>
                                            <ListItemText primary="• High-frequency APIs" />
                                        </ListItem>
                                    </List>
                                    <Chip label="Use: HIGH_PERFORMANCE or ZERO_COPY" color="success" size="small" sx={{ mt: 2 }} />
                                </CardContent>
                            </Card>
                        </Box>

                        <Alert severity="warning" sx={{ mb: 3 }}>
                            <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                                Profile Tradeoffs
                            </Typography>
                            <Typography variant="body2" sx={{ mb: 2 }}>
                                While profiles provide optimal defaults, consider these tradeoffs:
                            </Typography>
                            <List dense>
                                <ListItem sx={{ py: 0 }}>
                                    <ListItemText
                                        primary="• Advanced profiles may have higher memory overhead"
                                        primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                </ListItem>
                                <ListItem sx={{ py: 0 }}>
                                    <ListItemText
                                        primary="• Some profiles sacrifice flexibility for performance"
                                        primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                </ListItem>
                                <ListItem sx={{ py: 0 }}>
                                    <ListItemText
                                        primary="• Distributed profiles require network infrastructure"
                                        primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                </ListItem>
                                <ListItem sx={{ py: 0 }}>
                                    <ListItemText
                                        primary="• You can always override profile defaults if needed"
                                        primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                </ListItem>
                            </List>
                        </Alert>
                    </Box>
                </Box>

                {/* Advanced Configurations Section */}
                <Box id="advanced-configurations" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        Advanced Configurations
                    </Typography>

                    {/* Eviction Strategies */}
                    <Box id="eviction-strategies" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <DashboardIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            Eviction Strategies
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                            JCacheX provides multiple eviction strategies to manage memory usage when the cache reaches its capacity.
                            Each strategy is optimized for different use cases and access patterns.
                        </Typography>

                        <Stack spacing={3}>
                            {evictionStrategies.map((strategy, index) => (
                                <Box key={index} sx={{
                                    p: 3,
                                    borderRadius: 2,
                                    backgroundColor: 'grey.50',
                                    border: '1px solid',
                                    borderColor: 'grey.200'
                                }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                        <Box sx={{ color: 'primary.main', mr: 2 }}>
                                            {strategy.icon}
                                        </Box>
                                        <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                            {strategy.name}
                                        </Typography>
                                    </Box>
                                    <Typography variant="body1" sx={{ mb: 2, lineHeight: 1.6 }}>
                                        {strategy.description}
                                    </Typography>
                                    <Box sx={{ mb: 2 }}>
                                        <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 1 }}>
                                            Best Use Cases:
                                        </Typography>
                                        <Typography variant="body2" sx={{ lineHeight: 1.5 }}>
                                            {strategy.useCase}
                                        </Typography>
                                    </Box>
                                    <Box sx={{ mb: 2 }}>
                                        <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 1 }}>
                                            Performance Characteristics:
                                        </Typography>
                                        <Typography variant="body2" sx={{ lineHeight: 1.5 }}>
                                            {strategy.performance}
                                        </Typography>
                                    </Box>
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                        <Typography variant="caption" color="text.secondary">
                                            Implementation:
                                        </Typography>
                                        <Chip
                                            label={strategy.className}
                                            size="small"
                                            variant="outlined"
                                            sx={{
                                                fontFamily: 'monospace',
                                                fontSize: '0.75rem'
                                            }}
                                        />
                                    </Box>
                                </Box>
                            ))}
                        </Stack>

                        <Typography variant="h6" sx={{ mt: 4, mb: 2, fontWeight: 600 }}>
                            Choosing the Right Strategy
                        </Typography>
                        <Typography variant="body1" sx={{ lineHeight: 1.7 }}>
                            The choice of eviction strategy depends on your application's access patterns:
                        </Typography>
                        <List dense sx={{ mt: 2 }}>
                            <ListItem>
                                <ListItemText
                                    primary="• LRU - Best for general-purpose caching with temporal locality"
                                    secondary="Most recently accessed items are more likely to be accessed again"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemText
                                    primary="• LFU - Ideal for workloads with clear frequency patterns"
                                    secondary="Some items are accessed much more frequently than others"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemText
                                    primary="• FIFO/FILO - Simple strategies for predictable access patterns"
                                    secondary="When insertion order is the primary factor for eviction"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemText
                                    primary="• Weight-based - Memory-conscious caching for variable-size objects"
                                    secondary="When cache entries have significantly different memory footprints"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemText
                                    primary="• Time-based - TTL-based management for time-sensitive data"
                                    secondary="When data freshness is more important than access patterns"
                                />
                            </ListItem>
                        </List>
                    </Box>

                    {/* Performance Benchmarks */}
                    <Box id="performance-benchmarks" sx={{ mb: 6 }}>
                        <Alert severity="info" sx={{ mb: 3 }}>
                            <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                                🚀 Outstanding Performance
                            </Typography>
                            <Typography variant="body2" sx={{ mb: 2 }}>
                                JCacheX achieves market-leading performance with specialized cache implementations:
                            </Typography>
                            <List dense>
                                <ListItem sx={{ py: 0 }}>
                                    <ListItemText
                                        primary="• ZeroCopy Cache: 7.9ns GET (2.6x faster than Caffeine)"
                                        primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                </ListItem>
                                <ListItem sx={{ py: 0 }}>
                                    <ListItemText
                                        primary="• Locality Optimized: 9.7ns GET (1.9x faster than Caffeine)"
                                        primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                </ListItem>
                                <ListItem sx={{ py: 0 }}>
                                    <ListItemText
                                        primary="• All eviction strategies optimized to O(1) operations"
                                        primaryTypographyProps={{ variant: 'body2' }}
                                    />
                                </ListItem>
                            </List>
                            <Button
                                component={Link}
                                to="/performance"
                                variant="outlined"
                                size="small"
                                startIcon={<SpeedIcon />}
                                sx={{ mt: 2 }}
                            >
                                View Comprehensive Benchmarks
                            </Button>
                        </Alert>
                    </Box>

                    {/* All Configuration Options */}
                    <Box id="all-configuration-options" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <ApiIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            All Configuration Options
                        </Typography>
                        {configurationOptions.map((category, index) => (
                            <Accordion key={index} defaultExpanded={index === 0}>
                                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                        {category.category}
                                    </Typography>
                                </AccordionSummary>
                                <AccordionDetails>
                                    <TableContainer component={Paper}>
                                        <Table>
                                            <TableHead>
                                                <TableRow>
                                                    <TableCell>Option</TableCell>
                                                    <TableCell>Description</TableCell>
                                                    <TableCell>Example</TableCell>
                                                </TableRow>
                                            </TableHead>
                                            <TableBody>
                                                {category.options.map((option, optionIndex) => (
                                                    <TableRow key={optionIndex}>
                                                        <TableCell>
                                                            <code>{option.name}</code>
                                                        </TableCell>
                                                        <TableCell>{option.description}</TableCell>
                                                        <TableCell>
                                                            <code>{option.example}</code>
                                                        </TableCell>
                                                    </TableRow>
                                                ))}
                                            </TableBody>
                                        </Table>
                                    </TableContainer>
                                </AccordionDetails>
                            </Accordion>
                        ))}
                    </Box>

                    {/* Kotlin Extensions */}
                    <Box id="kotlin-extensions" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <ExtensionIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            Kotlin Extensions
                        </Typography>
                        <Box sx={{
                            display: 'grid',
                            gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                            gap: 3
                        }}>
                            {kotlinExtensions.map((extension, index) => (
                                <Card key={index} sx={{ height: '100%' }}>
                                    <CardContent>
                                        <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                                            {extension.name}
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                            {extension.description}
                                        </Typography>
                                        <Box component="pre" sx={{
                                            backgroundColor: 'grey.100',
                                            p: 2,
                                            borderRadius: 1,
                                            fontSize: '0.875rem',
                                            overflow: 'auto'
                                        }}>
                                            {extension.examples.join('\n')}
                                        </Box>
                                    </CardContent>
                                </Card>
                            ))}
                        </Box>
                    </Box>
                </Box>

                {/* Java Configuration Section */}
                <Box id="java-configuration" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        Java Configuration
                    </Typography>

                    <Box id="java-basic-usage" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            Basic Usage
                        </Typography>
                        <CodeTabs
                            tabs={[
                                {
                                    id: 'basic',
                                    label: 'Basic Setup',
                                    language: 'java',
                                    code: `// Create basic cache
CacheConfig<String, String> config = CacheConfig.<String, String>builder()
    .maximumSize(1000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();

Cache<String, String> cache = new DefaultCache<>(config);

// Basic operations
cache.put("key1", "value1");
String value = cache.get("key1");
cache.remove("key1");`
                                },
                                {
                                    id: 'async',
                                    label: 'Async Operations',
                                    language: 'java',
                                    code: `// Async operations
CompletableFuture<String> future = cache.getAsync("key1");
CompletableFuture<Void> putFuture = cache.putAsync("key2", "value2");
CompletableFuture<Void> removeFuture = cache.removeAsync("key1");

// Combining async operations
CompletableFuture<String> result = cache.getAsync("key1")
    .thenCompose(value -> {
        if (value == null) {
            return cache.putAsync("key1", "new_value")
                .thenApply(v -> "new_value");
        }
        return CompletableFuture.completedFuture(value);
    });`
                                }
                            ]}
                        />
                    </Box>
                </Box>

                {/* Kotlin Configuration Section */}
                <Box id="kotlin-configuration" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        Kotlin Configuration
                    </Typography>

                    <Box id="kotlin-dsl" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            DSL Configuration
                        </Typography>
                        <CodeTabs
                            tabs={[
                                {
                                    id: 'dsl-basic',
                                    label: 'Basic DSL',
                                    language: 'kotlin',
                                    code: `// DSL configuration
val cache = createCache<String, User> {
    maximumSize(1000L)
    expireAfterWrite(Duration.ofMinutes(30))
    recordStats(true)
    evictionStrategy(LRUEvictionStrategy())
}

// Usage with operators
cache["user123"] = User("Alice")
val user = cache["user123"]
cache += "user456" to User("Bob")
cache -= "user123"`
                                },
                                {
                                    id: 'dsl-advanced',
                                    label: 'Advanced DSL',
                                    language: 'kotlin',
                                    code: `// Advanced DSL configuration
val cache = createCache<String, ApiResponse> {
    maximumSize(500L)
    expireAfterAccess(Duration.ofMinutes(10))
    refreshAfterWrite(Duration.ofMinutes(5))

    // Custom loader
    loader { key -> apiClient.fetchData(key) }

    // Event listener
    listener(object : CacheEventListener<String, ApiResponse> {
        override fun onEvict(key: String, value: ApiResponse, reason: EvictionReason) {
            logger.info("Evicted $key due to $reason")
        }
    })`
                                }
                            ]}
                        />
                    </Box>
                </Box>

                {/* Spring Boot Configuration Section */}
                <Box id="spring-boot-configuration" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        Spring Boot Configuration
                    </Typography>

                    <Box id="spring-annotations" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            Annotations
                        </Typography>
                        <CodeTabs
                            tabs={[
                                {
                                    id: 'cacheable',
                                    label: '@JCacheXCacheable',
                                    language: 'java',
                                    code: `@Service
public class UserService {

    @JCacheXCacheable(cacheName = "users")
    public User findUser(String id) {
        return userRepository.findById(id);
    }

    @JCacheXCacheable(
        cacheName = "userProfiles",
        expireAfterWrite = 30,
        expireAfterWriteUnit = TimeUnit.MINUTES
    )
    public UserProfile getUserProfile(String userId) {
        return buildUserProfile(userId);
    }
}`
                                },
                                {
                                    id: 'evict',
                                    label: '@JCacheXCacheEvict',
                                    language: 'java',
                                    code: `@Service
public class UserService {

    @JCacheXCacheEvict(cacheName = "users", key = "#user.id")
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @JCacheXCacheEvict(cacheName = "users", allEntries = true)
    public void clearAllUsers() {
        userRepository.deleteAll();
    }
}`
                                }
                            ]}
                        />
                    </Box>
                </Box>

                {/* Footer */}
                <Box sx={{ textAlign: 'center', mt: 8, pt: 4, borderTop: 1, borderColor: 'divider' }}>
                    <Typography variant="body2" color="text.secondary">
                        JCacheX Documentation - Built with ❤️ for modern Java and Kotlin applications
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
                            href="/examples"
                            variant="outlined"
                        >
                            Examples
                        </Button>
                    </Box>
                </Box>
            </Container>
        </Layout>
    );
};

export default DocumentationPage;
