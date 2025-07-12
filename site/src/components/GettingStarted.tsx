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
    AccountTree as ProfileIcon,
    TrendingUp as PerformanceIcon,
} from '@mui/icons-material';
import CodeTabs from './CodeTabs';
import Layout from './Layout';
import { Breadcrumbs } from './SEO';

const GettingStarted: React.FC = () => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    const steps = [
        'Add Dependency',
        'Choose Profile',
        'Start Using',
        'Monitor & Tune'
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

    const profileExamples = [
        {
            title: 'Read-Heavy Profile',
            description: 'Perfect for reference data and configuration',
            profile: 'READ_HEAVY',
            performance: '11.5ns GET',
            code: `// Optimized for read-intensive workloads
Cache<String, Product> productCache = CacheBuilder
    .profile("READ_HEAVY")
    .name("products")
    .maximumSize(5000L)
    .build();

// Basic operations
productCache.put("product:123", product);
Product found = productCache.get("product:123"); // Ultra-fast reads`
        },
        {
            title: 'Session Cache Profile',
            description: 'Optimized for user session storage',
            profile: 'SESSION_CACHE',
            performance: 'TTL: 30 min',
            code: `// Automatically configured for sessions
Cache<String, UserSession> sessionCache = CacheBuilder
    .profile("SESSION_CACHE")
    .name("sessions")
    .build(); // TTL and size pre-configured

// Session management
sessionCache.put("session:abc123", userSession);
UserSession session = sessionCache.get("session:abc123");`
        },
        {
            title: 'Distributed Profile',
            description: 'Scale across multiple nodes',
            profile: 'DISTRIBUTED',
            performance: 'Network-aware',
            code: `// Multi-node caching made simple
Cache<String, Order> orderCache = CacheBuilder
    .profile("DISTRIBUTED")
    .name("orders")
    .clusterNodes("cache-1:8080", "cache-2:8080")
    .build();

// Works just like local cache
orderCache.put("order:456", order);
Order found = orderCache.get("order:456");`
        }
    ];

    const profileSelection = [
        {
            category: 'Core Profiles',
            description: 'Essential profiles for common use cases',
            profiles: [
                { name: 'READ_HEAVY', usage: '80%+ read operations', performance: '11.5ns GET' },
                { name: 'WRITE_HEAVY', usage: '50%+ write operations', performance: '393.5ns PUT' },
                { name: 'DEFAULT', usage: 'General-purpose caching', performance: '40.4ns GET' },
                { name: 'HIGH_PERFORMANCE', usage: 'Maximum throughput', performance: '24.6ns GET' }
            ]
        },
        {
            category: 'Specialized Profiles',
            description: 'Optimized for specific scenarios',
            profiles: [
                { name: 'SESSION_CACHE', usage: 'User session storage', performance: 'TTL: 30 min' },
                { name: 'API_CACHE', usage: 'External API responses', performance: 'TTL: 15 min' },
                { name: 'COMPUTE_CACHE', usage: 'Expensive computations', performance: 'TTL: 2 hours' }
            ]
        },
        {
            category: 'Advanced Profiles',
            description: 'Cutting-edge optimizations',
            profiles: [
                { name: 'ZERO_COPY', usage: 'Ultra-low latency (HFT)', performance: '7.9ns GET' },
                { name: 'DISTRIBUTED', usage: 'Multi-node clustering', performance: 'Network-aware' },
                { name: 'ML_OPTIMIZED', usage: 'Machine learning workloads', performance: 'Predictive' }
            ]
        }
    ];

    const bestPractices = [
        {
            title: 'Choose the Right Profile',
            description: 'Select a profile that matches your workload characteristics',
            tip: 'Start with core profiles like READ_HEAVY or WRITE_HEAVY based on your access patterns'
        },
        {
            title: 'Monitor Performance',
            description: 'Enable statistics to track hit rates and performance metrics',
            tip: 'Use cache.stats() to monitor effectiveness and adjust if needed'
        },
        {
            title: 'Size Appropriately',
            description: 'Override default sizes based on your memory constraints',
            tip: 'Profiles provide good defaults, but you can override maximumSize if needed'
        },
        {
            title: 'Consider TTL Requirements',
            description: 'Some profiles have built-in TTL, others let you configure it',
            tip: 'Session and API profiles have automatic TTL, others need explicit configuration'
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
                { id: 'choose-profile', title: 'Choose Profile', icon: <ProfileIcon /> },
                { id: 'first-cache', title: 'Your First Cache', icon: <CodeIcon /> },
                { id: 'profile-examples', title: 'Profile Examples', icon: <PlayArrowIcon /> },
            ],
        },
        {
            id: 'profiles',
            title: 'Cache Profiles',
            icon: <ProfileIcon />,
            children: [
                { id: 'core-profiles', title: 'Core Profiles', icon: <ApiIcon /> },
                { id: 'specialized-profiles', title: 'Specialized Profiles', icon: <BuildIcon /> },
                { id: 'advanced-profiles', title: 'Advanced Profiles', icon: <PerformanceIcon /> },
            ],
        },
        {
            id: 'best-practices',
            title: 'Best Practices',
            icon: <SecurityIcon />,
            children: [
                { id: 'profile-selection', title: 'Profile Selection', icon: <SettingsIcon /> },
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
                        Profile-based caching made simple - no more complex configurations
                    </Typography>
                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, flexWrap: 'wrap' }}>
                        <Chip
                            icon={<ProfileIcon />}
                            label="Profile-based"
                            color="primary"
                            sx={{ px: 2, py: 1 }}
                        />
                        <Chip
                            icon={<CodeIcon />}
                            label="Quick Setup"
                            color="secondary"
                            sx={{ px: 2, py: 1 }}
                        />
                        <Chip
                            icon={<PerformanceIcon />}
                            label="High Performance"
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

                {/* Choose Profile Section */}
                <Box id="choose-profile" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <ProfileIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Choose Your Profile
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        JCacheX profiles eliminate complex configuration decisions. Simply choose a profile that
                        matches your use case and get optimal performance automatically.
                    </Typography>

                    {profileSelection.map((category, categoryIndex) => (
                        <Box key={categoryIndex} sx={{ mb: 4 }}>
                            <Typography variant="h5" sx={{ fontWeight: 600, mb: 2 }}>
                                {category.category}
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                                {category.description}
                            </Typography>
                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
                                gap: 2
                            }}>
                                {category.profiles.map((profile, profileIndex) => (
                                    <Card key={profileIndex} variant="outlined">
                                        <CardContent sx={{ p: 2 }}>
                                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                <Box>
                                                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                                        {profile.name}
                                                    </Typography>
                                                    <Typography variant="body2" color="text.secondary">
                                                        {profile.usage}
                                                    </Typography>
                                                </Box>
                                                <Chip
                                                    label={profile.performance}
                                                    size="small"
                                                    color="primary"
                                                    variant="outlined"
                                                />
                                            </Box>
                                        </CardContent>
                                    </Card>
                                ))}
                            </Box>
                        </Box>
                    ))}
                </Box>

                {/* First Cache Section */}
                <Box id="first-cache" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <CodeIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Your First Cache
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        Create your first cache using profiles. Notice how much simpler it is compared to
                        traditional configuration-based approaches.
                    </Typography>

                    <CodeTabs
                        tabs={[
                            {
                                id: 'java-profile',
                                label: 'Java',
                                language: 'java',
                                code: `import io.github.dhruv1110.jcachex.*;

public class FirstCache {
    public static void main(String[] args) {
        // Profile-based approach - automatically optimized
        Cache<String, String> cache = CacheBuilder
            .profile("READ_HEAVY")  // Optimized for read-intensive workloads
            .name("users")
            .maximumSize(1000L)     // Override default if needed
            .build();

        // Basic operations
        cache.put("user:123", "John Doe");
        String user = cache.get("user:123");
        System.out.println("User: " + user);

        // Check performance
        CacheStats stats = cache.stats();
        System.out.println("Hit rate: " + (stats.hitRate() * 100) + "%");
    }
}`
                            },
                            {
                                id: 'kotlin-profile',
                                label: 'Kotlin',
                                language: 'kotlin',
                                code: `import io.github.dhruv1110.jcachex.kotlin.*

fun main() {
    // Profile-based approach with Kotlin DSL
    val cache = createCache<String, String> {
        profile("WRITE_HEAVY")  // Optimized for write-intensive workloads
        name("users")
        maximumSize(1000)       // Override default if needed
    }

    // Use operator overloading
    cache["user:123"] = "John Doe"
    val user = cache["user:123"]
    println("User: $user")

    // Check performance
    val stats = cache.stats()
    println("Hit rate: \${stats.hitRatePercent()}%")
}`
                            }
                        ]}
                    />
                </Box>

                {/* Profile Examples Section */}
                <Box id="profile-examples" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <PlayArrowIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Profile Examples
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        Here are real-world examples showing how different profiles optimize for specific use cases.
                    </Typography>

                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', lg: 'repeat(3, 1fr)' },
                        gap: 3
                    }}>
                        {profileExamples.map((example, index) => (
                            <Card key={index} sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                                <CardContent sx={{ flex: 1 }}>
                                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                                        <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                            {example.title}
                                        </Typography>
                                        <Chip
                                            label={example.performance}
                                            size="small"
                                            color="success"
                                            variant="outlined"
                                        />
                                    </Box>
                                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                        {example.description}
                                    </Typography>
                                    <Box component="pre" sx={{
                                        backgroundColor: 'grey.100',
                                        p: 2,
                                        borderRadius: 1,
                                        fontSize: '0.75rem',
                                        overflow: 'auto',
                                        fontFamily: 'monospace',
                                        lineHeight: 1.4
                                    }}>
                                        {example.code}
                                    </Box>
                                </CardContent>
                            </Card>
                        ))}
                    </Box>
                </Box>

                {/* Best Practices Section */}
                <Box id="best-practices" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 4 }}>
                        <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Best Practices
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        Follow these recommendations for optimal cache performance with profiles.
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
                        Now that you understand profile-based caching, explore these advanced features and integrations.
                    </Typography>

                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
                        gap: 3
                    }}>
                        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                            <CardContent sx={{ flex: 1 }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <ProfileIcon color="primary" sx={{ mr: 1 }} />
                                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                        All Profiles
                                    </Typography>
                                </Box>
                                <Typography variant="body2" sx={{ mb: 2 }}>
                                    Explore all 12 cache profiles including advanced features like ML optimization and zero-copy.
                                </Typography>
                                <Button
                                    variant="outlined"
                                    href="/documentation"
                                    sx={{ mt: 'auto' }}
                                >
                                    View All Profiles
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
                                    Learn about Spring Boot auto-configuration and profile-based annotation caching.
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
                                    <PerformanceIcon color="success" sx={{ mr: 1 }} />
                                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                        Performance Benchmarks
                                    </Typography>
                                </Box>
                                <Typography variant="body2" sx={{ mb: 2 }}>
                                    See detailed performance comparisons between profiles and industry competitors.
                                </Typography>
                                <Button
                                    variant="outlined"
                                    href="/performance"
                                    sx={{ mt: 'auto' }}
                                >
                                    View Benchmarks
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
