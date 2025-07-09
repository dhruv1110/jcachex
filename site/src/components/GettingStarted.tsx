import React from 'react';
import {
    Box,
    Container,
    Typography,
    Card,
    CardContent,
    Button,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    Stepper,
    Step,
    StepLabel,
    Alert,
    Chip,
    Accordion,
    AccordionSummary,
    AccordionDetails,
    useTheme,
    useMediaQuery,
} from '@mui/material';
import {
    GetApp as GetAppIcon,
    Build as BuildIcon,
    PlayArrow as PlayArrowIcon,
    Check as CheckIcon,
    Code as CodeIcon,
    Settings as SettingsIcon,
    Dashboard as DashboardIcon,
    Launch as LaunchIcon,
    BookmarkBorder as BookmarkIcon,
    School as SchoolIcon,
    Speed as SpeedIcon,
    Coffee as JavaIcon,
    Extension as ExtensionIcon,
    Api as ApiIcon,
    ExpandMore as ExpandMoreIcon,
    CloudDownload as CloudDownloadIcon,
    RocketLaunch as RocketLaunchIcon,
    IntegrationInstructions as IntegrationInstructionsIcon,
    Security as SecurityIcon,
    Verified as VerifiedIcon,
} from '@mui/icons-material';
import CodeTabs from './CodeTabs';
import Layout from './Layout';
import { Breadcrumbs } from './SEO';

const GettingStarted: React.FC = () => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    const steps = [
        'Add Dependency',
        'Configure Cache',
        'Start Using',
        'Explore Features'
    ];

    const installationMethods = [
        {
            title: 'Maven',
            icon: <BuildIcon />,
            description: 'Add to your pom.xml',
            code: `<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>1.0.0</version>
</dependency>`
        },
        {
            title: 'Gradle',
            icon: <BuildIcon />,
            description: 'Add to your build.gradle',
            code: `implementation 'io.github.dhruv1110:jcachex-core:1.0.0'`
        },
        {
            title: 'Gradle (Kotlin DSL)',
            icon: <ExtensionIcon />,
            description: 'Add to your build.gradle.kts',
            code: `implementation("io.github.dhruv1110:jcachex-core:1.0.0")`
        }
    ];

    const quickExamples = [
        {
            title: 'Basic Cache',
            description: 'Simple in-memory cache with size limits',
            code: `import io.github.dhruv1110.jcachex.*;
import java.time.Duration;

public class FirstCache {
    public static void main(String[] args) {
        // Create cache configuration
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
            .maximumSize(1000L)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats(true)
            .build();

        // Create cache instance
        Cache<String, String> cache = new DefaultCache<>(config);

        // Basic operations
        cache.put("user:123", "John Doe");
        String user = cache.get("user:123");
        System.out.println("User: " + user);

        // Check cache stats
        CacheStats stats = cache.stats();
        System.out.println("Hit rate: " + stats.hitRate());
    }
}`
        },
        {
            title: 'Async Operations',
            description: 'Non-blocking cache operations',
            code: `// Async operations
CompletableFuture<String> future = cache.getAsync("key1");
CompletableFuture<Void> putFuture = cache.putAsync("key2", "value2");

// Chain operations
CompletableFuture<String> result = cache.getAsync("key1")
    .thenCompose(value -> {
        if (value == null) {
            return cache.putAsync("key1", "default").thenApply(v -> "default");
        }
        return CompletableFuture.completedFuture(value);
    });`
        }
    ];

    const bestPractices = [
        {
            title: 'Choose the Right Size',
            description: 'Set appropriate maximum size based on your available memory',
            tip: 'Monitor memory usage and adjust cache size accordingly'
        },
        {
            title: 'Use Expiration Policies',
            description: 'Set expiration times to prevent stale data',
            tip: 'Consider both write and access-based expiration'
        },
        {
            title: 'Monitor Cache Performance',
            description: 'Enable statistics to track hit rates and performance',
            tip: 'Use cache.stats() to monitor effectiveness'
        },
        {
            title: 'Handle Null Values',
            description: 'Decide how to handle null or missing values',
            tip: 'Use Optional<T> for better null safety'
        }
    ];

    // Define navigation items for the sidebar
    const navigationItems = [
        {
            id: 'getting-started-intro',
            title: 'Getting Started',
            icon: <RocketLaunchIcon />,
            children: [
                { id: 'installation', title: 'Installation', icon: <CloudDownloadIcon /> },
                { id: 'first-cache', title: 'Your First Cache', icon: <CodeIcon /> },
                { id: 'quick-examples', title: 'Quick Examples', icon: <PlayArrowIcon /> },
            ],
        },
        {
            id: 'configuration',
            title: 'Configuration',
            icon: <SettingsIcon />,
            children: [
                { id: 'basic-config', title: 'Basic Configuration', icon: <ApiIcon /> },
                { id: 'advanced-config', title: 'Advanced Options', icon: <BuildIcon /> },
                { id: 'eviction-setup', title: 'Eviction Setup', icon: <DashboardIcon /> },
            ],
        },
        {
            id: 'best-practices',
            title: 'Best Practices',
            icon: <SecurityIcon />,
            children: [
                { id: 'sizing-guidelines', title: 'Sizing Guidelines', icon: <SettingsIcon /> },
                { id: 'monitoring', title: 'Monitoring', icon: <DashboardIcon /> },
                { id: 'troubleshooting', title: 'Troubleshooting', icon: <BuildIcon /> },
            ],
        },
        {
            id: 'next-steps',
            title: 'Next Steps',
            icon: <LaunchIcon />,
            children: [
                { id: 'advanced-features', title: 'Advanced Features', icon: <BuildIcon /> },
                { id: 'spring-integration', title: 'Spring Integration', icon: <SpeedIcon /> },
                { id: 'examples', title: 'More Examples', icon: <SchoolIcon /> },
            ],
        },
    ];

    const sidebarConfig = {
        title: "Getting Started",
        navigationItems: navigationItems,
        expandedByDefault: true
    };

    return (
        <Layout sidebarConfig={sidebarConfig}>
            <Breadcrumbs items={[
                { label: 'Home', path: '/' },
                { label: 'Getting Started', path: '/getting-started', current: true }
            ]} />

            <Container
                maxWidth="lg"
                sx={{
                    py: 4,
                    px: { xs: 2, sm: 3, md: 4 },
                    ml: { xs: 0, md: 0 },
                    mt: { xs: 1, md: 0 },
                    minHeight: { xs: 'calc(100vh - 80px)', md: 'auto' },
                }}
            >
                {/* Header */}
                <Box sx={{ textAlign: 'center', mb: 6 }}>
                    <Typography variant="h2" component="h1" gutterBottom sx={{ fontWeight: 700, mb: 2 }}>
                        Getting Started with JCacheX
                    </Typography>
                    <Typography variant="h5" color="text.secondary" sx={{ mb: 4 }}>
                        Get up and running with JCacheX in minutes
                    </Typography>
                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, flexWrap: 'wrap' }}>
                        <Chip
                            icon={<RocketLaunchIcon />}
                            label="Quick Setup"
                            color="primary"
                            sx={{ px: 2, py: 1 }}
                        />
                        <Chip
                            icon={<CodeIcon />}
                            label="Code Examples"
                            color="secondary"
                            sx={{ px: 2, py: 1 }}
                        />
                        <Chip
                            icon={<VerifiedIcon />}
                            label="Best Practices"
                            color="success"
                            sx={{ px: 2, py: 1 }}
                        />
                    </Box>
                </Box>

                {/* Progress Stepper */}
                <Box sx={{ mb: 6 }}>
                    <Stepper activeStep={0} alternativeLabel={!isMobile}>
                        {steps.map((label) => (
                            <Step key={label}>
                                <StepLabel>{label}</StepLabel>
                            </Step>
                        ))}
                    </Stepper>
                </Box>

                {/* Installation Section */}
                <Box id="installation" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <CloudDownloadIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Installation
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        Choose your preferred build tool and add JCacheX to your project.
                        All artifacts are available on Maven Central.
                    </Typography>

                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
                        gap: 3
                    }}>
                        {installationMethods.map((method, index) => (
                            <Card key={index} sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                                <CardContent sx={{ flex: 1 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                        <Box sx={{ color: 'primary.main', mr: 1 }}>
                                            {method.icon}
                                        </Box>
                                        <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                            {method.title}
                                        </Typography>
                                    </Box>
                                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                        {method.description}
                                    </Typography>
                                    <Box component="pre" sx={{
                                        backgroundColor: 'grey.100',
                                        p: 2,
                                        borderRadius: 1,
                                        fontSize: '0.875rem',
                                        overflow: 'auto',
                                        fontFamily: 'monospace'
                                    }}>
                                        {method.code}
                                    </Box>
                                </CardContent>
                            </Card>
                        ))}
                    </Box>
                </Box>

                {/* First Cache Section */}
                <Box id="first-cache" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <CodeIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Your First Cache
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        Create your first cache with just a few lines of code. This example shows the basic
                        configuration and usage patterns.
                    </Typography>

                    <CodeTabs
                        tabs={[
                            {
                                id: 'java-basic',
                                label: 'Java',
                                language: 'java',
                                code: `import io.github.dhruv1110.jcachex.*;
import java.time.Duration;

public class FirstCache {
    public static void main(String[] args) {
        // Create cache configuration
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
            .maximumSize(1000L)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats(true)
            .build();

        // Create cache instance
        Cache<String, String> cache = new DefaultCache<>(config);

        // Basic operations
        cache.put("user:123", "John Doe");
        String user = cache.get("user:123");
        System.out.println("User: " + user);

        // Check cache stats
        CacheStats stats = cache.stats();
        System.out.println("Hit rate: " + stats.hitRate());
    }
}`
                            },
                            {
                                id: 'kotlin-basic',
                                label: 'Kotlin',
                                language: 'kotlin',
                                code: `import io.github.dhruv1110.jcachex.kotlin.*
import java.time.Duration

fun main() {
    // Create cache with DSL
    val cache = createCache<String, String> {
        maximumSize(1000L)
        expireAfterWrite(Duration.ofMinutes(30))
        recordStats(true)
    }

    // Use operator overloading
    cache["user:123"] = "John Doe"
    val user = cache["user:123"]
    println("User: " + user)

    // Check cache stats
    val stats = cache.stats()
    println("Hit rate: " + stats.hitRate())
}`
                            }
                        ]}
                    />
                </Box>

                {/* Quick Examples Section */}
                <Box id="quick-examples" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <PlayArrowIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Quick Examples
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        Here are some common usage patterns to get you started quickly.
                    </Typography>

                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
                        gap: 3
                    }}>
                        {quickExamples.map((example, index) => (
                            <Card key={index} sx={{ height: '100%' }}>
                                <CardContent>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                                        {example.title}
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                        {example.description}
                                    </Typography>
                                    <Box component="pre" sx={{
                                        backgroundColor: 'grey.100',
                                        p: 2,
                                        borderRadius: 1,
                                        fontSize: '0.875rem',
                                        overflow: 'auto',
                                        fontFamily: 'monospace'
                                    }}>
                                        {example.code}
                                    </Box>
                                </CardContent>
                            </Card>
                        ))}
                    </Box>
                </Box>

                {/* Configuration Section */}
                <Box id="configuration" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        Configuration
                    </Typography>

                    <Box id="basic-config" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            Basic Configuration
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 3, fontSize: '1.1rem', lineHeight: 1.7 }}>
                            Essential configuration options for most use cases.
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="maximumSize"
                                    secondary="Set the maximum number of entries the cache can hold"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="expireAfterWrite"
                                    secondary="Entries expire after being written to the cache"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="expireAfterAccess"
                                    secondary="Entries expire after being accessed"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="recordStats"
                                    secondary="Enable statistics collection for monitoring"
                                />
                            </ListItem>
                        </List>
                    </Box>

                    <Box id="advanced-config" sx={{ mb: 6 }}>
                        <Typography variant="h4" component="h3" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                            Advanced Options
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 3, fontSize: '1.1rem', lineHeight: 1.7 }}>
                            Additional configuration for fine-tuning cache behavior.
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="initialCapacity"
                                    secondary="Set initial capacity to avoid resizing during startup"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="concurrencyLevel"
                                    secondary="Configure concurrency for multi-threaded access"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="weigher"
                                    secondary="Custom weight calculation for entries"
                                />
                            </ListItem>
                        </List>
                    </Box>
                </Box>

                {/* Best Practices Section */}
                <Box id="best-practices" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Best Practices
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        Follow these recommendations for optimal cache performance and reliability.
                    </Typography>

                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
                        gap: 3
                    }}>
                        {bestPractices.map((practice, index) => (
                            <Card key={index} sx={{ height: '100%' }}>
                                <CardContent>
                                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                                        {practice.title}
                                    </Typography>
                                    <Typography variant="body2" sx={{ mb: 2 }}>
                                        {practice.description}
                                    </Typography>
                                    <Alert severity="info" sx={{ mt: 2 }}>
                                        <strong>Tip:</strong> {practice.tip}
                                    </Alert>
                                </CardContent>
                            </Card>
                        ))}
                    </Box>
                </Box>

                {/* Next Steps Section */}
                <Box id="next-steps" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <LaunchIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Next Steps
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        Now that you have JCacheX up and running, explore these advanced features and integrations.
                    </Typography>

                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
                        gap: 3
                    }}>
                        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                            <CardContent sx={{ flex: 1 }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <BuildIcon color="primary" sx={{ mr: 1 }} />
                                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                        Advanced Features
                                    </Typography>
                                </Box>
                                <Typography variant="body2" sx={{ mb: 2 }}>
                                    Explore distributed caching, custom eviction strategies, and async operations.
                                </Typography>
                                <Button
                                    variant="outlined"
                                    href="/documentation"
                                    sx={{ mt: 'auto' }}
                                >
                                    Read Documentation
                                </Button>
                            </CardContent>
                        </Card>
                        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                            <CardContent sx={{ flex: 1 }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <SpeedIcon color="secondary" sx={{ mr: 1 }} />
                                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                        Spring Integration
                                    </Typography>
                                </Box>
                                <Typography variant="body2" sx={{ mb: 2 }}>
                                    Learn about Spring Boot auto-configuration and annotation-based caching.
                                </Typography>
                                <Button
                                    variant="outlined"
                                    href="/spring-guide"
                                    sx={{ mt: 'auto' }}
                                >
                                    Spring Guide
                                </Button>
                            </CardContent>
                        </Card>
                        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                            <CardContent sx={{ flex: 1 }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <SchoolIcon color="success" sx={{ mr: 1 }} />
                                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                        More Examples
                                    </Typography>
                                </Box>
                                <Typography variant="body2" sx={{ mb: 2 }}>
                                    Browse complete examples for Java, Kotlin, and Spring Boot applications.
                                </Typography>
                                <Button
                                    variant="outlined"
                                    href="/examples"
                                    sx={{ mt: 'auto' }}
                                >
                                    View Examples
                                </Button>
                            </CardContent>
                        </Card>
                    </Box>
                </Box>
            </Container>
        </Layout>
    );
};

export default GettingStarted;
