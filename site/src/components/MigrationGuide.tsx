import React, { useState } from 'react';
import { useVersion } from '../hooks';
import {
    Box,
    Container,
    Typography,
    Paper,
    Stack,
    Stepper,
    Step,
    StepLabel,
    StepContent,
    Button,
    Alert,
    Chip,
    Card,
    CardContent,
    Divider,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    Tabs,
    Tab
} from '@mui/material';
import {
    CheckCircle as CheckIcon,
    Warning as WarningIcon,
    Speed as SpeedIcon,
    SwapHoriz as SwapIcon,
    Code as CodeIcon,
    TrendingUp as TrendingUpIcon
} from '@mui/icons-material';
import { MIGRATION_EXAMPLES } from '../constants/basicExamples';
import CodeTabs from './CodeTabs';
import Layout from './Layout';

interface MigrationGuideProps {
    showBenefits?: boolean;
    interactive?: boolean;
}

const MigrationGuide: React.FC<MigrationGuideProps> = ({
    showBenefits = true,
    interactive = true
}) => {
    const { version } = useVersion();
    const [activeStep, setActiveStep] = useState(0);
    const [selectedMigration, setSelectedMigration] = useState<'caffeine' | 'redis'>('caffeine');

    const handleNext = () => {
        setActiveStep((prevActiveStep) => prevActiveStep + 1);
    };

    const handleBack = () => {
        setActiveStep((prevActiveStep) => prevActiveStep - 1);
    };

    const handleReset = () => {
        setActiveStep(0);
    };

    const migrationSteps = {
        caffeine: [
            {
                label: 'Analyze Current Implementation',
                description: 'Review your existing Caffeine cache configuration and usage patterns',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Before migrating, understand your current cache configuration:
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Maximum cache size and eviction policies" />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="TTL and expiration configurations" />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Cache event listeners and callbacks" />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Performance requirements and access patterns" />
                            </ListItem>
                        </List>
                    </Box>
                )
            },
            {
                label: 'Add JCacheX Dependencies',
                description: 'Update your build configuration to include JCacheX',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Add JCacheX to your project dependencies:
                        </Typography>
                        <Alert severity="info" sx={{ mb: 2 }}>
                            Keep Caffeine dependencies during migration for gradual transition
                        </Alert>
                        <Typography variant="body2" sx={{ mb: 1, fontFamily: 'monospace' }}>
                            Maven:
                        </Typography>
                        <Paper sx={{ p: 2, bgcolor: 'grey.50', mb: 2 }}>
                            <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                {`<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>${version}</version>
</dependency>`}
                            </Typography>
                        </Paper>
                    </Box>
                )
            },
            {
                label: 'Replace Cache Creation',
                description: 'Switch from Caffeine builder to JCacheX builder with intelligent profiles',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Replace your Caffeine cache creation with JCacheX:
                        </Typography>
                        <Alert severity="success" sx={{ mb: 2 }}>
                            JCacheX profiles automatically optimize for your use case - no complex configuration needed!
                        </Alert>
                        <CodeTabs
                            tabs={[{
                                id: 'before-after',
                                label: 'Before & After',
                                language: 'java',
                                code: `// BEFORE (Caffeine)
Cache<String, User> caffeineCache = Caffeine.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .removalListener((key, value, cause) ->
        log.info("Removed {} due to {}", key, cause))
    .build();

// AFTER (JCacheX) - Profile-based optimization
Cache<String, User> jcacheXCache = JCacheXBuilder
    .forReadHeavyWorkload()  // Automatically optimized for read-heavy patterns
    .maximumSize(10000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .evictionListener((key, value, cause) ->
        log.info("Removed {} due to {}", key, cause))
    .build();

// Alternative: Use profile enum for type safety
Cache<String, User> profileCache = JCacheXBuilder
    .fromProfile(ProfileName.READ_HEAVY)
    .maximumSize(10000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .evictionListener((key, value, cause) ->
        log.info("Removed {} due to {}", key, cause))
    .build();

// One-liner for common cases
Cache<String, User> simpleCache = JCacheXBuilder
    .createReadHeavyCache("users", 10000);`
                            }]}
                            showCopyButtons={true}
                        />
                    </Box>
                )
            },
            {
                label: 'Update Cache Operations',
                description: 'Cache operations remain identical - just with better performance through profiles',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Cache operations remain exactly the same - just get automatic optimization:
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="cache.get(key) → Same API, 501.1M ops/sec with ZeroCopy"
                                    secondary="Profile-optimized for your workload pattern"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="cache.put(key, value) → Same API, 224.6M ops/sec for WriteHeavy"
                                    secondary="Optimized eviction strategies based on profile"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="cache.getIfPresent(key) → Same API, ultra-fast retrieval"
                                    secondary="Profile-based memory layout optimization"
                                />
                            </ListItem>
                        </List>
                    </Box>
                )
            },
            {
                label: 'Test & Validate',
                description: 'Verify migration with comprehensive testing and profile-based performance monitoring',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Validate your migration with these profile-aware steps:
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText
                                    primary="Unit tests pass with identical behavior"
                                    secondary="Same cache semantics, profile-optimized performance"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText
                                    primary="Performance benchmarks show significant improvement"
                                    secondary="Monitor profile-specific metrics: hit rates, latency, throughput"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText
                                    primary="Memory usage optimized per profile"
                                    secondary="Profile-based allocation strategies reduce GC pressure"
                                />
                            </ListItem>
                        </List>
                        <Alert severity="info" sx={{ mt: 2 }}>
                            Use JCacheX's built-in metrics to compare profile-optimized performance with your Caffeine baseline
                        </Alert>
                    </Box>
                )
            }
        ],
        redis: [
            {
                label: 'Identify Cache Patterns',
                description: 'Analyze your Redis usage patterns to select optimal JCacheX profiles',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Review your Redis implementation to choose the right JCacheX profile:
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Local cache candidates (frequent access → READ_HEAVY profile)" />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Update-heavy data (frequent writes → WRITE_HEAVY profile)" />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Performance-critical data (low latency → HIGH_PERFORMANCE profile)" />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Serialization overhead and data types" />
                            </ListItem>
                        </List>
                    </Box>
                )
            },
            {
                label: 'Setup JCacheX Dependencies',
                description: 'Add JCacheX to your project for profile-based local caching',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Add JCacheX for high-performance profile-based local caching:
                        </Typography>
                        <Alert severity="warning" sx={{ mb: 2 }}>
                            Consider hybrid approach: JCacheX profiles for local cache, Redis for distributed state
                        </Alert>
                        <Paper sx={{ p: 2, bgcolor: 'grey.50', mb: 2 }}>
                            <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                {`// Gradle
implementation 'io.github.dhruv1110:jcachex-core:${version}'
implementation 'io.github.dhruv1110:jcachex-spring:${version}'

// Maven
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>${version}</version>
</dependency>`}
                            </Typography>
                        </Paper>
                    </Box>
                )
            },
            {
                label: 'Replace Redis Operations',
                description: 'Convert Redis operations to JCacheX with profile-based massive performance gains',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Replace Redis calls with JCacheX profiles for 50x+ performance improvement:
                        </Typography>
                        <Alert severity="success" sx={{ mb: 2 }}>
                            No serialization overhead, type-safe operations, profile-optimized performance!
                        </Alert>
                        <CodeTabs
                            tabs={[{
                                id: 'redis-to-jcachex',
                                label: 'Redis → JCacheX Profiles',
                                language: 'java',
                                code: `// BEFORE (Redis/Lettuce)
@Service
public class RedisUserService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public User getUser(String id) {
        String json = redisTemplate.opsForValue().get("user:" + id);
        if (json != null) {
            return objectMapper.readValue(json, User.class);
        }
        User user = userRepository.findById(id);
        redisTemplate.opsForValue().set("user:" + id,
            objectMapper.writeValueAsString(user), Duration.ofHours(1));
        return user;
    }
}

// AFTER (JCacheX) - Profile-based optimization
@Service
public class JCacheXUserService {
    // For frequently accessed user data
    private final Cache<String, User> userCache =
        JCacheXBuilder.forReadHeavyWorkload()
            .maximumSize(50000)
            .expireAfterWrite(Duration.ofHours(1))
            .build();

    // For frequently updated preferences
    private final Cache<String, UserPreferences> prefCache =
        JCacheXBuilder.forWriteHeavyWorkload()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(15))
            .build();

    // For performance-critical session data
    private final Cache<String, UserSession> sessionCache =
        JCacheXBuilder.forHighPerformance()
            .maximumSize(100000)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    public User getUser(String id) {
        return userCache.get(id, userRepository::findById);
    }

    public UserPreferences getPreferences(String userId) {
        return prefCache.get(userId, userRepository::findPreferencesById);
    }

    public UserSession getSession(String sessionId) {
        return sessionCache.get(sessionId, sessionRepository::findById);
    }
}`
                            }]}
                            showCopyButtons={true}
                        />
                    </Box>
                )
            },
            {
                label: 'Optimize with Profiles',
                description: 'Configure JCacheX profiles for optimal local cache performance',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Optimize JCacheX configuration using intelligent profiles:
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon><TrendingUpIcon fontSize="small" color="primary" /></ListItemIcon>
                                <ListItemText
                                    primary="Use READ_HEAVY profile for frequently accessed data"
                                    secondary="Optimized for 501.1M ops/sec with ZeroCopy implementation"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><SpeedIcon fontSize="small" color="primary" /></ListItemIcon>
                                <ListItemText
                                    primary="Use WRITE_HEAVY profile for frequently updated data"
                                    secondary="Optimized for 224.6M ops/sec with efficient eviction"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" color="primary" /></ListItemIcon>
                                <ListItemText
                                    primary="Use HIGH_PERFORMANCE profile for ultra-low latency"
                                    secondary="Optimized memory layout for fastest possible access"
                                />
                            </ListItem>
                        </List>
                    </Box>
                )
            },
            {
                label: 'Performance Validation',
                description: 'Measure and validate the massive profile-based performance improvements',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Validate the profile-based performance benefits of your migration:
                        </Typography>
                        <Stack spacing={2}>
                            <Card variant="outlined">
                                <CardContent>
                                    <Typography variant="h6" color="primary">Expected Performance Gains by Profile</Typography>
                                    <Typography variant="body2" sx={{ mt: 1 }}>
                                        • <strong>READ_HEAVY Profile:</strong> 501.1M ops/sec (ZeroCopy)<br />
                                        • <strong>WRITE_HEAVY Profile:</strong> 224.6M ops/sec (WriteHeavy)<br />
                                        • <strong>HIGH_PERFORMANCE Profile:</strong> Ultra-low latency<br />
                                        • <strong>No serialization overhead</strong> (type-safe operations)<br />
                                        • <strong>Better reliability</strong> (no network dependencies)
                                    </Typography>
                                </CardContent>
                            </Card>
                            <Alert severity="info">
                                Keep Redis for distributed state, use JCacheX profiles for local high-performance caching
                            </Alert>
                        </Stack>
                    </Box>
                )
            }
        ]
    };

    const benefits = {
        caffeine: [
            { icon: <SpeedIcon />, title: 'Profile-Based Optimization', description: '501.1M ops/sec with ZeroCopy, 224.6M ops/sec WriteHeavy' },
            { icon: <TrendingUpIcon />, title: 'Intelligent Profiles', description: 'Automatic optimization: READ_HEAVY, WRITE_HEAVY, HIGH_PERFORMANCE' },
            { icon: <CheckIcon />, title: 'Drop-in Replacement', description: 'Nearly identical API with profile-based performance' }
        ],
        redis: [
            { icon: <SpeedIcon />, title: '50x+ Lower Latency', description: 'No network overhead, profile-optimized memory layout' },
            { icon: <CheckIcon />, title: 'Type Safety + Profiles', description: 'No serialization errors, intelligent caching profiles' },
            { icon: <TrendingUpIcon />, title: 'Higher Reliability', description: 'No network dependencies, profile-based local optimization' }
        ]
    };

    const navigationItems = [
        {
            id: 'migration-from-caffeine',
            title: 'From Caffeine',
            icon: <SwapIcon />,
            children: [
                { id: 'migration-caffeine-steps', title: 'Steps', icon: <SwapIcon /> },
                { id: 'migration-caffeine-performance', title: 'Performance', icon: <SpeedIcon /> }
            ]
        },
        {
            id: 'migration-from-redis',
            title: 'From Redis',
            icon: <CodeIcon />,
            children: [
                { id: 'migration-redis-steps', title: 'Steps', icon: <SwapIcon /> },
                { id: 'migration-redis-benefits', title: 'Benefits', icon: <SpeedIcon /> }
            ]
        },
        {
            id: 'migration-from-guava',
            title: 'From Guava',
            icon: <CodeIcon />,
            children: [
                { id: 'migration-guava-comparison', title: 'Comparison', icon: <CodeIcon /> },
                { id: 'migration-guava-steps', title: 'Steps', icon: <SwapIcon /> },
                { id: 'migration-guava-migration', title: 'Migration', icon: <TrendingUpIcon /> }
            ]
        },
        {
            id: 'migration-best-practices',
            title: 'Best Practices',
            icon: <CheckIcon />,
            children: [
                { id: 'migration-planning', title: 'Planning', icon: <CodeIcon /> },
                { id: 'migration-testing', title: 'Testing', icon: <WarningIcon /> },
                { id: 'migration-monitoring', title: 'Monitoring', icon: <SpeedIcon /> }
            ]
        }
    ];

    const sidebarConfig = {
        title: "Migration Guide",
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
                {/* Anchor containers for sidebar */}
                <Box id="migration-from-caffeine" />
                <Box id="migration-from-redis" />
                <Box id="migration-from-guava" />
                <Box id="migration-best-practices" />
                <Typography variant="h2" component="h1" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1, fontWeight: 700, mb: 2 }}>
                    <SwapIcon color="primary" />
                    Migration Guide
                </Typography>
                <Typography variant="h6" color="text.secondary" sx={{ mb: 4 }}>
                    Step-by-step guide to migrate from popular caching solutions to JCacheX
                </Typography>

                {/* Migration Type Selection */}
                <Paper className="jcx-surface" sx={{ mb: 3 }}>
                    <Tabs
                        value={selectedMigration}
                        onChange={(_, value) => setSelectedMigration(value)}
                        sx={{ borderBottom: 1, borderColor: 'divider' }}
                    >
                        <Tab
                            label="From Caffeine"
                            value="caffeine"
                            icon={<CodeIcon />}
                        />
                        <Tab
                            label="From Redis"
                            value="redis"
                            icon={<SwapIcon />}
                        />
                    </Tabs>
                </Paper>

                {/* Benefits Overview */}
                {showBenefits && (
                    <Paper className="jcx-surface" sx={{ p: 3, mb: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Migration Benefits
                        </Typography>
                        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
                            {benefits[selectedMigration].map((benefit, index) => (
                                <Card key={index} variant="outlined" sx={{ flex: 1 }}>
                                    <CardContent>
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                            {benefit.icon}
                                            <Typography variant="subtitle2">{benefit.title}</Typography>
                                        </Box>
                                        <Typography variant="body2" color="text.secondary">
                                            {benefit.description}
                                        </Typography>
                                    </CardContent>
                                </Card>
                            ))}
                        </Stack>
                    </Paper>
                )}

                {/* Migration Steps */}
                <Paper className="jcx-surface" sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        Migration Steps
                    </Typography>
                    <Stepper activeStep={activeStep} orientation="vertical">
                        {migrationSteps[selectedMigration].map((step, index) => (
                            <Step key={index}>
                                <StepLabel>{step.label}</StepLabel>
                                <StepContent>
                                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                        {step.description}
                                    </Typography>
                                    {step.content}
                                    <Box sx={{ mt: 2 }}>
                                        <Button
                                            variant="contained"
                                            onClick={handleNext}
                                            sx={{ mt: 1, mr: 1 }}
                                        >
                                            {index === migrationSteps[selectedMigration].length - 1 ? 'Finish' : 'Continue'}
                                        </Button>
                                        <Button
                                            disabled={index === 0}
                                            onClick={handleBack}
                                            sx={{ mt: 1, mr: 1 }}
                                        >
                                            Back
                                        </Button>
                                    </Box>
                                </StepContent>
                            </Step>
                        ))}
                    </Stepper>
                    {activeStep === migrationSteps[selectedMigration].length && (
                        <Paper square elevation={0} className="jcx-surface" sx={{ p: 3 }}>
                            <Typography variant="h6" gutterBottom>
                                Migration Complete! 🎉
                            </Typography>
                            <Typography variant="body1" sx={{ mb: 2 }}>
                                You've successfully migrated to JCacheX. You should now see significant performance improvements in your application.
                            </Typography>
                            <Alert severity="success" sx={{ mb: 2 }}>
                                Monitor your application metrics to validate the performance gains and optimize further if needed.
                            </Alert>
                            <Button onClick={handleReset} sx={{ mt: 1, mr: 1 }}>
                                Reset Guide
                            </Button>
                        </Paper>
                    )}
                </Paper>

                {/* Code Examples */}
                <Paper className="jcx-surface" sx={{ p: 3, mt: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        Complete Migration Examples
                    </Typography>
                    <CodeTabs
                        tabs={MIGRATION_EXAMPLES}
                        showCopyButtons={true}
                        showPerformanceMetrics={true}
                    />
                </Paper>
            </Container>
        </Layout>
    );
};

export default MigrationGuide;
