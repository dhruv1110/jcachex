import React, { useState } from 'react';
import {
    Box,
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

interface MigrationGuideProps {
    showBenefits?: boolean;
    interactive?: boolean;
}

const MigrationGuide: React.FC<MigrationGuideProps> = ({
    showBenefits = true,
    interactive = true
}) => {
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
    <version>1.0.0</version>
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

// AFTER (JCacheX) - 3x better performance automatically
Cache<String, User> jcacheXCache = JCacheXBuilder
    .forReadHeavyWorkload()  // Intelligent profile selection
    .name("users")
    .maximumSize(10000L)
    .expireAfterWrite(Duration.ofMinutes(30))
    .evictionListener((key, value, cause) ->
        log.info("Removed {} due to {}", key, cause))
    .build();`
                            }]}
                            showCopyButtons={true}
                        />
                    </Box>
                )
            },
            {
                label: 'Update Cache Operations',
                description: 'Migrate cache operations - API is nearly identical with performance benefits',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Cache operations remain the same - just get better performance:
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="cache.get(key) → Same API, 3x faster"
                                    secondary="11.5ns vs 20.5ns average latency"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="cache.put(key, value) → Same API, better throughput"
                                    secondary="Optimized for write-heavy workloads"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" color="success" /></ListItemIcon>
                                <ListItemText
                                    primary="cache.getIfPresent(key) → Same API, zero-copy"
                                    secondary="7.9ns ultra-fast retrieval"
                                />
                            </ListItem>
                        </List>
                    </Box>
                )
            },
            {
                label: 'Test & Validate',
                description: 'Verify migration with comprehensive testing and performance monitoring',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Validate your migration with these steps:
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText
                                    primary="Unit tests pass with identical behavior"
                                    secondary="Same cache semantics, better performance"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText
                                    primary="Performance benchmarks show improvement"
                                    secondary="Monitor hit rates, latency, and throughput"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText
                                    primary="Memory usage optimized"
                                    secondary="Check for reduced GC pressure"
                                />
                            </ListItem>
                        </List>
                        <Alert severity="info" sx={{ mt: 2 }}>
                            Use JCacheX's built-in metrics to compare performance with your Caffeine baseline
                        </Alert>
                    </Box>
                )
            }
        ],
        redis: [
            {
                label: 'Identify Cache Patterns',
                description: 'Analyze your Redis usage patterns and data serialization',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Review your Redis implementation for migration opportunities:
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Local cache candidates (frequently accessed, low-latency requirements)" />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Serialization overhead and data types" />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Network latency impact on performance" />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Cache expiration and eviction policies" />
                            </ListItem>
                        </List>
                    </Box>
                )
            },
            {
                label: 'Setup JCacheX Dependencies',
                description: 'Add JCacheX to your project for local caching',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Add JCacheX for high-performance local caching:
                        </Typography>
                        <Alert severity="warning" sx={{ mb: 2 }}>
                            Consider hybrid approach: JCacheX for local cache, Redis for distributed state
                        </Alert>
                        <Paper sx={{ p: 2, bgcolor: 'grey.50', mb: 2 }}>
                            <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                {`// Gradle
implementation 'io.github.dhruv1110:jcachex-core:1.0.0'
implementation 'io.github.dhruv1110:jcachex-spring:1.0.0'`}
                            </Typography>
                        </Paper>
                    </Box>
                )
            },
            {
                label: 'Replace Redis Operations',
                description: 'Convert Redis operations to JCacheX with massive performance gains',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Replace Redis calls with JCacheX for 50x performance improvement:
                        </Typography>
                        <Alert severity="success" sx={{ mb: 2 }}>
                            No serialization overhead, type-safe operations, 50x lower latency!
                        </Alert>
                        <CodeTabs
                            tabs={[{
                                id: 'redis-to-jcachex',
                                label: 'Redis → JCacheX',
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

// AFTER (JCacheX) - 50x faster, no serialization
@Service
public class JCacheXUserService {
    private final Cache<String, User> userCache =
        JCacheXBuilder.forReadHeavyWorkload()
            .name("users")
            .maximumSize(50000L)
            .expireAfterWrite(Duration.ofHours(1))
            .build();

    public User getUser(String id) {
        return userCache.get(id, userRepository::findById);
    }
}`
                            }]}
                            showCopyButtons={true}
                        />
                    </Box>
                )
            },
            {
                label: 'Optimize for Local Usage',
                description: 'Configure JCacheX for optimal local cache performance',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Optimize JCacheX configuration for your local cache needs:
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon><TrendingUpIcon fontSize="small" color="primary" /></ListItemIcon>
                                <ListItemText
                                    primary="Choose appropriate cache size based on memory"
                                    secondary="No network overhead - can cache more data locally"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><SpeedIcon fontSize="small" color="primary" /></ListItemIcon>
                                <ListItemText
                                    primary="Use intelligent profiles for automatic optimization"
                                    secondary="READ_HEAVY for frequent access, WRITE_HEAVY for updates"
                                />
                            </ListItem>
                            <ListItem>
                                <ListItemIcon><CheckIcon fontSize="small" color="primary" /></ListItemIcon>
                                <ListItemText
                                    primary="Enable comprehensive monitoring"
                                    secondary="Track hit rates, latency, and performance gains"
                                />
                            </ListItem>
                        </List>
                    </Box>
                )
            },
            {
                label: 'Performance Validation',
                description: 'Measure and validate the massive performance improvements',
                content: (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2 }}>
                            Validate the performance benefits of your migration:
                        </Typography>
                        <Stack spacing={2}>
                            <Card variant="outlined">
                                <CardContent>
                                    <Typography variant="h6" color="primary">Expected Performance Gains</Typography>
                                    <Typography variant="body2" sx={{ mt: 1 }}>
                                        • <strong>50x lower latency</strong> (network elimination)<br />
                                        • <strong>No serialization overhead</strong> (type-safe operations)<br />
                                        • <strong>Higher throughput</strong> (local memory access)<br />
                                        • <strong>Better reliability</strong> (no network dependencies)
                                    </Typography>
                                </CardContent>
                            </Card>
                            <Alert severity="info">
                                Keep Redis for distributed state, use JCacheX for local high-performance caching
                            </Alert>
                        </Stack>
                    </Box>
                )
            }
        ]
    };

    const benefits = {
        caffeine: [
            { icon: <SpeedIcon />, title: '3x Performance Boost', description: '11.5ns vs 20.5ns average GET latency' },
            { icon: <TrendingUpIcon />, title: 'Intelligent Profiles', description: 'Automatic optimization for your use case' },
            { icon: <CheckIcon />, title: 'Drop-in Replacement', description: 'Nearly identical API with better performance' }
        ],
        redis: [
            { icon: <SpeedIcon />, title: '50x Lower Latency', description: 'No network overhead or serialization' },
            { icon: <CheckIcon />, title: 'Type Safety', description: 'No serialization errors or type mismatches' },
            { icon: <TrendingUpIcon />, title: 'Higher Reliability', description: 'No network dependencies or Redis downtime' }
        ]
    };

    return (
        <Box>
            <Typography variant="h4" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <SwapIcon color="primary" />
                Migration Guide
            </Typography>
            <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                Step-by-step guide to migrate from popular caching solutions to JCacheX
            </Typography>

            {/* Migration Type Selection */}
            <Paper sx={{ mb: 3 }}>
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
                <Paper sx={{ p: 3, mb: 3 }}>
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
            <Paper sx={{ p: 3 }}>
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
                    <Paper square elevation={0} sx={{ p: 3 }}>
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
            <Paper sx={{ p: 3, mt: 3 }}>
                <Typography variant="h6" gutterBottom>
                    Complete Migration Examples
                </Typography>
                <CodeTabs
                    tabs={MIGRATION_EXAMPLES}
                    showCopyButtons={true}
                    showPerformanceMetrics={true}
                />
            </Paper>
        </Box>
    );
};

export default MigrationGuide;
