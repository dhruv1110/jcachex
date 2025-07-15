import React, { useState } from 'react';
import {
    Box,
    Typography,
    Container,
    Button,
    Card,
    CardContent,
    Chip,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    Alert,
    AlertTitle,
    Stepper,
    Step,
    StepLabel,
    StepContent,
    useTheme,
    useMediaQuery,
    Paper,
    Grid,
} from '@mui/material';
import { useVersion } from '../hooks';
import {
    CloudDownload as CloudDownloadIcon,
    Build as BuildIcon,
    Code as CodeIcon,
    RocketLaunch as RocketLaunchIcon,
    PlayArrow as PlayArrowIcon,
    Extension as ExtensionIcon,
    Speed as SpeedIcon,
    Dashboard as DashboardIcon,
    Settings as SettingsIcon,
    Security as SecurityIcon,
    Launch as LaunchIcon,
    School as SchoolIcon,
    Api as ApiIcon,
    AccountTree as ProfileIcon,
    TrendingUp as PerformanceIcon,
} from '@mui/icons-material';
import CodeTabs from './CodeTabs';
import Layout from './Layout';
import { Breadcrumbs } from './SEO';

const GettingStarted: React.FC = () => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));
    const { version } = useVersion();
    const [activeTab, setActiveTab] = useState('installation');
    const [selectedProfile, setSelectedProfile] = useState('READ_HEAVY');
    const [codeExample, setCodeExample] = useState('');

    const steps = [
        'Add Dependency',
        'Choose Profile',
        'Start Using',
        'Monitor & Tune'
    ];

    const installationTabs = [
        {
            id: 'maven',
            label: 'Maven',
            language: 'xml',
            code: `<!-- JCacheX Core -->
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>${version}</version>
</dependency>

<!-- For Spring Boot integration -->
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-spring</artifactId>
    <version>${version}</version>
</dependency>

<!-- For Kotlin DSL -->
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-kotlin</artifactId>
    <version>${version}</version>
</dependency>`
        },
        {
            id: 'gradle',
            label: 'Gradle',
            language: 'groovy',
            code: `// JCacheX Core
implementation 'io.github.dhruv1110:jcachex-core:${version}'

// For Spring Boot integration
implementation 'io.github.dhruv1110:jcachex-spring:${version}'

// For Kotlin DSL
implementation 'io.github.dhruv1110:jcachex-kotlin:${version}'`
        },
        {
            id: 'sbt',
            label: 'SBT',
            language: 'scala',
            code: `libraryDependencies ++= Seq(
  "io.github.dhruv1110" % "jcachex-core" % "${version}",
  "io.github.dhruv1110" % "jcachex-spring" % "${version}",
  "io.github.dhruv1110" % "jcachex-kotlin" % "${version}"
)`
        },
        {
            id: 'kotlin',
            label: 'Kotlin DSL',
            language: 'kotlin',
            code: `dependencies {
    implementation("io.github.dhruv1110:jcachex-core:${version}")
    implementation("io.github.dhruv1110:jcachex-spring:${version}")
    implementation("io.github.dhruv1110:jcachex-kotlin:${version}")
}`
        }
    ];

    const profileExamples = [
        {
            title: 'Profile-Based Creation (Type Safe)',
            description: 'Using ProfileName enum for compile-time safety',
            profile: 'READ_HEAVY',
            performance: '22.6M ops/s, 93.7% efficiency',
            code: `// Using ProfileName enum for type safety
Cache<String, User> userCache = JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY)
    .name("users")
    .maximumSize(1000L)
    .build();

// Basic operations
userCache.put("user:123", user);
User found = userCache.get("user:123"); // Ultra-fast reads`
        },
        {
            title: 'Convenience Methods (One-liner)',
            description: 'One-line cache creation for common use cases',
            profile: 'SESSION_CACHE',
            performance: '37.3M ops/s, 9.3μs GET',
            code: `// Read-heavy workloads (80%+ reads)
Cache<String, User> users = JCacheXBuilder.forReadHeavyWorkload()
    .name("users").maximumSize(1000L).build();

// Write-heavy workloads (50%+ writes)
Cache<String, Session> sessions = JCacheXBuilder.forWriteHeavyWorkload()
    .name("sessions").maximumSize(2000L).build();

// Memory-constrained environments
Cache<String, Data> memCache = JCacheXBuilder.forMemoryConstrainedEnvironment()
    .name("memory-cache").maximumSize(100L).build();`
        },
        {
            title: 'Smart Defaults (Automatic)',
            description: 'Let JCacheX choose optimal profile automatically',
            profile: 'SMART_DEFAULTS',
            performance: 'Adaptive optimization',
            code: `// Let JCacheX choose optimal profile based on workload characteristics
Cache<String, Data> smartCache = JCacheXBuilder.withSmartDefaults()
    .workloadCharacteristics(WorkloadCharacteristics.builder()
        .readToWriteRatio(8.0) // Read-heavy
        .accessPattern(WorkloadCharacteristics.AccessPattern.TEMPORAL_LOCALITY)
        .build())
    .build();`
        },
        {
            title: 'Kotlin DSL Integration',
            description: 'Idiomatic Kotlin with DSL syntax',
            profile: 'KOTLIN_DSL',
            performance: 'Type-safe DSL',
            code: `// Convenience methods with DSL
val readHeavyCache = createReadHeavyCache<String, Product> {
    name("products")
    maximumSize(5000L)
}

val sessionCache = createSessionCache<String, UserSession> {
    name("sessions")
    maximumSize(2000L)
}

// Profile-based with DSL
val profileCache = createCacheWithProfile<String, Data>(ProfileName.HIGH_PERFORMANCE) {
    name("high-perf")
    maximumSize(10000L)
}`
        },
        {
            title: 'Spring Boot Integration',
            description: 'Configuration-based cache setup',
            profile: 'SPRING_BOOT',
            performance: 'Auto-configuration',
            code: `# application.yml
jcachex:
  caches:
    users:
      profile: READ_HEAVY
      maximumSize: 5000
    sessions:
      profile: SESSION_CACHE
      maximumSize: 2000
    products:
      profile: HIGH_PERFORMANCE
      maximumSize: 10000`
        }
    ];

    const profileSelection = [
        {
            category: 'Core Profiles',
            description: 'Essential profiles for common use cases',
            profiles: [
                { name: 'READ_HEAVY', usage: '80%+ read operations', performance: '22.6M ops/s, 93.7% efficiency' },
                { name: 'WRITE_HEAVY', usage: '50%+ write operations', performance: '224.6M ops/s, 97.2% efficiency' },
                { name: 'DEFAULT', usage: 'General-purpose caching', performance: '19.3M ops/s, 10.4μs GET' },
                { name: 'HIGH_PERFORMANCE', usage: 'Maximum throughput', performance: '198.4M ops/s, 82.9% efficiency' }
            ]
        },
        {
            category: 'Specialized Profiles',
            description: 'Optimized for specific scenarios',
            profiles: [
                { name: 'SESSION_CACHE', usage: 'User session storage', performance: '37.3M ops/s, 9.3μs GET' },
                { name: 'API_CACHE', usage: 'External API responses', performance: '19.0M ops/s, 10.1μs GET' },
                { name: 'COMPUTE_CACHE', usage: 'Expensive computations', performance: '20.9M ops/s, 9.4μs GET' }
            ]
        },
        {
            category: 'Advanced Profiles',
            description: 'Cutting-edge optimizations',
            profiles: [
                { name: 'ZERO_COPY', usage: 'Ultra-low latency (HFT)', performance: '501.1M ops/s, 98.4% efficiency' },
                { name: 'DISTRIBUTED', usage: 'Multi-node clustering', performance: '0.3M ops/s, 30.5% efficiency' },
                { name: 'ML_OPTIMIZED', usage: 'Machine learning workloads', performance: '0.1M ops/s, 89.4% efficiency' }
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
            title: 'Use Convenience Methods',
            description: 'Take advantage of one-liner cache creation methods',
            tip: 'JCacheXBuilder.forReadHeavyWorkload() is easier than manual configuration'
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
                <Box sx={{ display: 'flex', justifyContent: 'center', mb: 6 }}>
                    <Stepper
                        activeStep={0}
                        orientation={isMobile ? 'vertical' : 'horizontal'}
                        sx={{ width: '100%', maxWidth: 600 }}
                    >
                        {steps.map((label, index) => (
                            <Step key={index}>
                                <StepLabel>{label}</StepLabel>
                            </Step>
                        ))}
                    </Stepper>
                </Box>

                {/* Quick Start Section */}
                <Box id="quick-start" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        🚀 Quick Start
                    </Typography>

                    <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 4, mb: 6 }}>
                        <Box sx={{ flex: 1 }}>
                            <Paper elevation={2} sx={{ p: 3, height: '100%' }}>
                                <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
                                    1. Add Dependency
                                </Typography>
                                <CodeTabs tabs={installationTabs} />
                            </Paper>
                        </Box>
                        <Box sx={{ flex: 1 }}>
                            <Paper elevation={2} sx={{ p: 3, height: '100%' }}>
                                <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
                                    2. Create Your First Cache
                                </Typography>
                                <CodeTabs
                                    tabs={[
                                        {
                                            id: 'java',
                                            label: 'Java',
                                            language: 'java',
                                            code: `// One-liner cache creation
Cache<String, User> cache = JCacheXBuilder.forReadHeavyWorkload()
    .name("users").maximumSize(1000L).build();

// Use it
cache.put("user:123", new User("Alice"));
User user = cache.get("user:123"); // Lightning fast`
                                        },
                                        {
                                            id: 'kotlin',
                                            label: 'Kotlin',
                                            language: 'kotlin',
                                            code: `// Kotlin DSL
val cache = createReadHeavyCache<String, User> {
    name("users")
    maximumSize(1000L)
}

// Use it
cache["user:123"] = User("Alice")
val user = cache["user:123"] // Idiomatic Kotlin`
                                        }
                                    ]}
                                />
                            </Paper>
                        </Box>
                    </Box>
                </Box>

                {/* Profile Examples Section */}
                <Box id="profile-examples" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        🎯 Easy-to-Use Examples
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        JCacheX provides multiple easy patterns for creating caches. Choose the one that fits your style:
                    </Typography>

                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', lg: 'repeat(2, 1fr)' },
                        gap: 4
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
                                            color="success"
                                            size="small"
                                            sx={{ fontSize: '0.75rem' }}
                                        />
                                    </Box>
                                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                        {example.description}
                                    </Typography>
                                    <Box component="pre" sx={{
                                        backgroundColor: 'grey.100',
                                        p: 2,
                                        borderRadius: 1,
                                        fontSize: '0.85rem',
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

                {/* Continue with rest of the component structure... */}
                {/* Profile Selection Section */}
                <Box id="choose-profile" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <ProfileIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Choose Your Profile
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        JCacheX profiles automatically configure optimal settings for your specific use case.
                        Select based on your workload characteristics:
                    </Typography>

                    {profileSelection.map((category, categoryIndex) => (
                        <Box key={categoryIndex} sx={{ mb: 4 }}>
                            <Typography variant="h5" gutterBottom sx={{ fontWeight: 600, mb: 2 }}>
                                {category.category}
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                                {category.description}
                            </Typography>

                            <Box sx={{
                                display: 'grid',
                                gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)', lg: 'repeat(3, 1fr)' },
                                gap: 2
                            }}>
                                {category.profiles.map((profile, profileIndex) => (
                                    <Card
                                        key={profileIndex}
                                        sx={{
                                            cursor: 'pointer',
                                            transition: 'transform 0.2s, box-shadow 0.2s',
                                            '&:hover': {
                                                transform: 'translateY(-2px)',
                                                boxShadow: 3
                                            }
                                        }}
                                        onClick={() => setSelectedProfile(profile.name)}
                                    >
                                        <CardContent>
                                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                                <Typography variant="h6" sx={{ fontWeight: 600, fontSize: '1rem' }}>
                                                    {profile.name}
                                                </Typography>
                                                <Chip
                                                    label={profile.performance}
                                                    color="primary"
                                                    size="small"
                                                    sx={{ fontSize: '0.7rem' }}
                                                />
                                            </Box>
                                            <Typography variant="body2" color="text.secondary">
                                                {profile.usage}
                                            </Typography>
                                        </CardContent>
                                    </Card>
                                ))}
                            </Box>
                        </Box>
                    ))}
                </Box>

                {/* Best Practices Section */}
                <Box id="best-practices" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Best Practices
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        Follow these guidelines to get the most out of JCacheX:
                    </Typography>

                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
                        gap: 3
                    }}>
                        {bestPractices.map((practice, index) => (
                            <Card key={index} sx={{ height: '100%' }}>
                                <CardContent>
                                    <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
                                        {practice.title}
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                        {practice.description}
                                    </Typography>
                                    <Alert severity="info" sx={{ mt: 2 }}>
                                        <Typography variant="body2" sx={{ fontWeight: 500 }}>
                                            💡 {practice.tip}
                                        </Typography>
                                    </Alert>
                                </CardContent>
                            </Card>
                        ))}
                    </Box>
                </Box>

                {/* Next Steps Section */}
                <Box id="next-steps" sx={{ mb: 8 }}>
                    <Typography variant="h3" component="h2" gutterBottom sx={{ fontWeight: 600, mb: 3 }}>
                        <LaunchIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                        Next Steps
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 4, fontSize: '1.1rem', lineHeight: 1.7 }}>
                        Ready to explore more? Here are some recommended next steps:
                    </Typography>

                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
                        gap: 3
                    }}>
                        <Card sx={{ textAlign: 'center', p: 3 }}>
                            <ApiIcon sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
                            <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
                                Explore Examples
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                See real-world usage patterns in Java, Kotlin, and Spring Boot
                            </Typography>
                            <Button variant="outlined" href="/examples" sx={{ mt: 1 }}>
                                View Examples
                            </Button>
                        </Card>

                        <Card sx={{ textAlign: 'center', p: 3 }}>
                            <SpeedIcon sx={{ fontSize: 48, color: 'secondary.main', mb: 2 }} />
                            <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
                                Spring Boot Guide
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                Learn how to integrate JCacheX with Spring Boot applications
                            </Typography>
                            <Button variant="outlined" href="/spring-guide" sx={{ mt: 1 }}>
                                Spring Guide
                            </Button>
                        </Card>

                        <Card sx={{ textAlign: 'center', p: 3 }}>
                            <PerformanceIcon sx={{ fontSize: 48, color: 'success.main', mb: 2 }} />
                            <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
                                Performance Tips
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                Advanced performance optimization techniques and benchmarks
                            </Typography>
                            <Button variant="outlined" href="/performance" sx={{ mt: 1 }}>
                                Performance Guide
                            </Button>
                        </Card>
                    </Box>
                </Box>

                {/* Quick Reference */}
                <Box id="quick-reference" sx={{ mb: 8 }}>
                    <Alert severity="success" sx={{ p: 3 }}>
                        <AlertTitle sx={{ fontWeight: 600, mb: 1 }}>
                            🎉 You're Ready to Go!
                        </AlertTitle>
                        <Typography variant="body1" sx={{ mb: 2 }}>
                            You now have everything you need to start using JCacheX in your application.
                            Remember to choose the right profile for your workload and monitor performance using cache statistics.
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
                            <Button variant="contained" href="/examples" startIcon={<PlayArrowIcon />}>
                                Try Examples
                            </Button>
                            <Button variant="outlined" href="/documentation" startIcon={<SchoolIcon />}>
                                Read Docs
                            </Button>
                        </Box>
                    </Alert>
                </Box>
            </Container>
        </Layout>
    );
};

export default GettingStarted;
