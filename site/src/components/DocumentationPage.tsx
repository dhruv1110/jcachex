import React, { useState } from 'react';
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
    Fab,
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
    Menu as MenuIcon,
    Settings as SettingsIcon,
} from '@mui/icons-material';
import CodeTabs from './CodeTabs';
import MobileSidebar from './MobileSidebar';
import PageWrapper from './PageWrapper';

const DocumentationPage: React.FC = () => {
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    const handleSidebarToggle = () => {
        setSidebarOpen(!sidebarOpen);
    };

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
            name: 'LRU (Least Recently Used)',
            description: 'Evicts the least recently accessed entries first',
            useCase: 'General purpose caching, temporal locality',
            performance: 'O(1) update, O(n) eviction',
            icon: <DashboardIcon />,
            className: 'LRUEvictionStrategy'
        },
        {
            name: 'LFU (Least Frequently Used)',
            description: 'Evicts the least frequently accessed entries',
            useCase: 'Data with clear frequency patterns',
            performance: 'O(1) update, O(n) eviction',
            icon: <AnalyticsIcon />,
            className: 'LFUEvictionStrategy'
        },
        {
            name: 'FIFO (First In, First Out)',
            description: 'Evicts entries in insertion order',
            useCase: 'Simple queue-like behavior',
            performance: 'O(1) update, O(n) eviction',
            icon: <StorageIcon />,
            className: 'FIFOEvictionStrategy'
        },
        {
            name: 'FILO (First In, Last Out)',
            description: 'Evicts entries in reverse insertion order',
            useCase: 'Stack-like behavior',
            performance: 'O(1) update, O(n) eviction',
            icon: <StorageIcon />,
            className: 'FILOEvictionStrategy'
        },
        {
            name: 'Weight-Based',
            description: 'Evicts entries based on custom weight calculation',
            useCase: 'Memory-conscious caching',
            performance: 'O(1) update, O(n) eviction',
            icon: <MemoryIcon />,
            className: 'WeightBasedEvictionStrategy'
        },
        {
            name: 'Time-Based',
            description: 'Evicts entries based on idle time',
            useCase: 'TTL-based cache management',
            performance: 'O(1) update, O(n) eviction',
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
            name: 'DSL Builder',
            description: 'Type-safe configuration with Kotlin DSL',
            examples: [
                'val config = cacheConfig<String, User> { ... }',
                'maximumSize(1000L)',
                'expireAfterWrite(Duration.ofMinutes(30))'
            ]
        }
    ];

    return (
        <PageWrapper>
            <MobileSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

            <Container maxWidth="lg" sx={{ py: 4 }}>
                {/* Mobile FAB */}
                {isMobile && (
                    <Fab
                        color="primary"
                        aria-label="open documentation menu"
                        onClick={handleSidebarToggle}
                        sx={{
                            position: 'fixed',
                            bottom: 16,
                            right: 16,
                            zIndex: 1000,
                        }}
                    >
                        <MenuIcon />
                    </Fab>
                )}

                {/* Header */}
                <Box sx={{ textAlign: 'center', mb: 6 }}>
                    <Typography variant="h2" component="h1" gutterBottom sx={{ fontWeight: 700, mb: 2 }}>
                        JCacheX Documentation
                    </Typography>
                    <Typography variant="h5" color="text.secondary" sx={{ mb: 4 }}>
                        Comprehensive guide to high-performance caching in Java and Kotlin
                    </Typography>
                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, flexWrap: 'wrap' }}>
                        <Chip icon={<RocketIcon />} label="High Performance" color="primary" />
                        <Chip icon={<ApiIcon />} label="Simple API" color="secondary" />
                        <Chip icon={<CloudSyncIcon />} label="Async Support" color="success" />
                        <Chip icon={<SpeedIcon />} label="Spring Integration" color="info" />
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
                        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 3 }}>
                            <Box sx={{ flex: 1 }}>
                                <Card>
                                    <CardContent>
                                        <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                                            <RocketIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                            Core Features
                                        </Typography>
                                        <List dense>
                                            <ListItem>
                                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                                <ListItemText primary="High Performance" secondary="Optimized for speed with minimal overhead" />
                                            </ListItem>
                                            <ListItem>
                                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                                <ListItemText primary="Simple API" secondary="Intuitive, fluent interface that's easy to learn" />
                                            </ListItem>
                                            <ListItem>
                                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                                <ListItemText primary="Async Support" secondary="Built-in CompletableFuture and Kotlin Coroutines support" />
                                            </ListItem>
                                            <ListItem>
                                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                                <ListItemText primary="Spring Integration" secondary="Seamless Spring Boot integration with annotations" />
                                            </ListItem>
                                        </List>
                                    </CardContent>
                                </Card>
                            </Box>
                            <Box sx={{ flex: 1 }}>
                                <Card>
                                    <CardContent>
                                        <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                                            <BuildIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                                            Enterprise Features
                                        </Typography>
                                        <List dense>
                                            <ListItem>
                                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                                <ListItemText primary="Distributed Caching" secondary="Multi-node clustering with consistency guarantees" />
                                            </ListItem>
                                            <ListItem>
                                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                                <ListItemText primary="Comprehensive Monitoring" secondary="Built-in metrics and observability" />
                                            </ListItem>
                                            <ListItem>
                                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                                <ListItemText primary="Flexible Configuration" secondary="Multiple eviction strategies and policies" />
                                            </ListItem>
                                            <ListItem>
                                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                                <ListItemText primary="Production Ready" secondary="Circuit breakers, warming, and resilience" />
                                            </ListItem>
                                        </List>
                                    </CardContent>
                                </Card>
                            </Box>
                        </Box>
                    </Box>

                    {/* Supported Platforms */}
                    <Box id="supported-platforms" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <AndroidIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            Supported Platforms
                        </Typography>
                        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, flexWrap: 'wrap', gap: 3 }}>
                            {platformsData.map((platform, index) => (
                                <Box key={index} sx={{ flex: { xs: '1 1 100%', sm: '1 1 45%', md: '1 1 22%' } }}>
                                    <Card sx={{ height: '100%' }}>
                                        <CardContent sx={{ textAlign: 'center' }}>
                                            <Box sx={{ color: 'primary.main', mb: 2 }}>
                                                {platform.icon}
                                            </Box>
                                            <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                                                {platform.name}
                                            </Typography>
                                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                                {platform.version}
                                            </Typography>
                                            <Chip
                                                label={platform.status}
                                                color="success"
                                                size="small"
                                                sx={{ fontWeight: 500 }}
                                            />
                                        </CardContent>
                                    </Card>
                                </Box>
                            ))}
                        </Box>
                    </Box>

                    {/* Quick Start */}
                    <Box id="quick-start" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            <RocketIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                            1 Minute Quick Start
                        </Typography>
                        <Alert severity="info" sx={{ mb: 3 }}>
                            Get started with JCacheX in less than a minute. Choose your preferred language and follow the simple setup.
                        </Alert>
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
                        <Box sx={{
                            display: 'grid',
                            gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                            gap: 3
                        }}>
                            {evictionStrategies.map((strategy, index) => (
                                <Card key={index} sx={{ height: '100%' }}>
                                    <CardContent>
                                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                            <Box sx={{ color: 'primary.main', mr: 2 }}>
                                                {strategy.icon}
                                            </Box>
                                            <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                                {strategy.name}
                                            </Typography>
                                        </Box>
                                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                            {strategy.description}
                                        </Typography>
                                        <Box sx={{ mb: 2 }}>
                                            <Typography variant="caption" color="text.secondary">
                                                Use Case:
                                            </Typography>
                                            <Typography variant="body2">
                                                {strategy.useCase}
                                            </Typography>
                                        </Box>
                                        <Box sx={{ mb: 2 }}>
                                            <Typography variant="caption" color="text.secondary">
                                                Performance:
                                            </Typography>
                                            <Typography variant="body2">
                                                {strategy.performance}
                                            </Typography>
                                        </Box>
                                        <Chip
                                            label={strategy.className}
                                            size="small"
                                            sx={{
                                                fontFamily: 'monospace',
                                                fontSize: '0.75rem'
                                            }}
                                        />
                                    </CardContent>
                                </Card>
                            ))}
                        </Box>
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
        </PageWrapper>
    );
};

export default DocumentationPage;
