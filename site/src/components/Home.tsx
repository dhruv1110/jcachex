import React from 'react';
import { Link } from 'react-router-dom';
import { Container, Typography, Button, Box, Card, CardContent, Chip, Paper } from '@mui/material';
import { ArrowForward as ArrowIcon, PlayArrow as PlayIcon, Speed, Security, Cloud, Settings, Star, Delete, Extension, Memory, Code } from '@mui/icons-material';
import type { CodeTab, Module } from '../types';
import { useVersion } from '../hooks';
import { BASIC_USAGE_JAVA, BASIC_USAGE_KOTLIN, SPRING_USAGE, INSTALLATION_TABS, MODULES, FEATURES } from '../constants';
import { PROGRESSIVE_LEARNING_EXAMPLES } from '../constants/progressiveLearning';
import { REAL_WORLD_EXAMPLES } from '../constants/realWorldExamples';
import { CACHE_PROFILES } from '../constants/cacheProfiles';
import PageWrapper from './PageWrapper';
import Layout from './Layout';
import CodeTabs from './CodeTabs';
import PerformanceComparison from './PerformanceComparison';

const HomeComponent: React.FC = () => {
    const { version } = useVersion();

    // Use progressive learning examples for hero section
    const heroCodeTabs: CodeTab[] = PROGRESSIVE_LEARNING_EXAMPLES;

    // Convert features from constants to MUI compatible format
    const featureIcons: { [key: string]: JSX.Element } = {
        'performance': <Speed sx={{ fontSize: 40 }} />,
        'api': <Settings sx={{ fontSize: 40 }} />,
        'async': <Code sx={{ fontSize: 40 }} />,
        'spring': <Extension sx={{ fontSize: 40 }} />,
        'distributed': <Cloud sx={{ fontSize: 40 }} />,
        'monitoring': <Star sx={{ fontSize: 40 }} />
    };

    const moduleIcons: { [key: string]: JSX.Element } = {
        'jcachex-core': <Memory sx={{ fontSize: 40 }} />,
        'jcachex-spring': <Extension sx={{ fontSize: 40 }} />,
        'jcachex-kotlin': <Code sx={{ fontSize: 40 }} />
    };

    return (
        <Layout>
            {/* Hero Section */}
            <Box
                sx={{
                    background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, rgba(139, 92, 246, 0.05) 100%)',
                    py: { xs: 8, md: 12 },
                    minHeight: '70vh',
                    display: 'flex',
                    alignItems: 'center'
                }}
            >
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center' }}>
                        <Typography
                            variant="h1"
                            component="h1"
                            sx={{
                                fontSize: { xs: '2.5rem', md: '3.5rem', lg: '4rem' },
                                fontWeight: 700,
                                mb: 3,
                                background: 'linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%)',
                                backgroundClip: 'text',
                                WebkitBackgroundClip: 'text',
                                WebkitTextFillColor: 'transparent',
                                lineHeight: 1.1
                            }}
                        >
                            High-performance Java caching
                            <br />
                            made simple with profiles
                        </Typography>

                        <Typography
                            variant="h5"
                            sx={{
                                mx: 'auto',
                                mt: 2,
                                mb: 4,
                                maxWidth: '800px',
                                color: 'text.secondary',
                                fontSize: { xs: '1.1rem', md: '1.25rem' }
                            }}
                        >
                            JCacheX eliminates complex caching decisions with intelligent profiles.
                            Choose READ_HEAVY, WRITE_HEAVY, or DISTRIBUTED and get optimal performance automatically.
                        </Typography>

                        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap', mb: 6 }}>
                            <Button
                                component={Link}
                                to="/getting-started"
                                variant="contained"
                                size="large"
                                endIcon={<ArrowIcon />}
                                sx={{ px: 4, py: 1.5 }}
                            >
                                Get Started
                            </Button>
                            <Button
                                href="https://github.com/dhruv1110/JCacheX"
                                target="_blank"
                                rel="noopener noreferrer"
                                variant="outlined"
                                size="large"
                                startIcon={<PlayIcon />}
                                sx={{ px: 4, py: 1.5 }}
                            >
                                View on GitHub
                            </Button>
                        </Box>

                        <Box sx={{ display: 'flex', gap: 4, justifyContent: 'center', flexWrap: 'wrap' }}>
                            {[
                                { number: '7.9ns', label: 'Fastest GET (Zero-copy)' },
                                { number: '50M+', label: 'Operations/second' },
                                { number: '12', label: 'Intelligent Profiles' }
                            ].map((stat, index) => (
                                <Box key={index} sx={{ textAlign: 'center' }}>
                                    <Typography variant="h4" sx={{ fontWeight: 700, color: 'primary.main' }}>
                                        {stat.number}
                                    </Typography>
                                    <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                                        {stat.label}
                                    </Typography>
                                </Box>
                            ))}
                        </Box>

                        <Box sx={{ textAlign: 'center', mt: 4 }}>
                            <Button
                                component={Link}
                                to="/performance"
                                variant="outlined"
                                size="medium"
                                sx={{ px: 3, py: 1 }}
                            >
                                View Performance Benchmarks
                            </Button>
                        </Box>
                    </Box>
                </Container>
            </Box>

            {/* Cache Profiles Section */}
            <Box sx={{ py: 8, bgcolor: 'grey.50' }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Chip
                            label="Profile-based Caching"
                            sx={{ mb: 2, px: 2, py: 1 }}
                            variant="outlined"
                            color="primary"
                        />
                        <Typography variant="h2" component="h2" sx={{ mb: 2, fontWeight: 700 }}>
                            Choose Your Profile
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'text.secondary', maxWidth: '600px', mx: 'auto' }}>
                            No more guessing about eviction strategies or configurations.
                            Pick a profile that matches your use case and get optimal performance automatically.
                        </Typography>
                    </Box>

                    <Box
                        sx={{
                            display: 'grid',
                            gridTemplateColumns: {
                                xs: '1fr',
                                md: 'repeat(3, 1fr)'
                            },
                            gap: 4,
                            mb: 6
                        }}
                    >
                        {CACHE_PROFILES.filter(profile => ['READ_HEAVY', 'WRITE_HEAVY', 'SESSION_CACHE', 'API_CACHE', 'DISTRIBUTED', 'ZERO_COPY'].includes(profile.name)).map((profile, index) => (
                            <Card
                                key={index}
                                sx={{
                                    height: '100%',
                                    transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                                    '&:hover': {
                                        transform: 'translateY(-4px)',
                                        boxShadow: (theme) => theme.shadows[8]
                                    }
                                }}
                            >
                                <CardContent sx={{ p: 3 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                        <Chip
                                            label={profile.category}
                                            size="small"
                                            variant="outlined"
                                            color={profile.category === 'Core' ? 'primary' : profile.category === 'Specialized' ? 'secondary' : 'default'}
                                            sx={{ mr: 1 }}
                                        />
                                        <Typography variant="h6" component="h3" sx={{ fontWeight: 600 }}>
                                            {profile.name}
                                        </Typography>
                                    </Box>
                                    <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>
                                        {profile.description}
                                    </Typography>
                                    <Typography variant="caption" sx={{ color: 'text.secondary', fontStyle: 'italic' }}>
                                        {profile.useCase}
                                    </Typography>
                                    {profile.performance && (
                                        <Box sx={{ mt: 2, p: 1, bgcolor: 'grey.100', borderRadius: 1 }}>
                                            <Typography variant="caption" sx={{ color: 'primary.main', fontWeight: 600 }}>
                                                {profile.performance}
                                            </Typography>
                                        </Box>
                                    )}
                                </CardContent>
                            </Card>
                        ))}
                    </Box>

                    <Box sx={{ textAlign: 'center' }}>
                        <Button
                            component={Link}
                            to="/documentation"
                            variant="outlined"
                            size="large"
                            sx={{ px: 4, py: 1.5 }}
                        >
                            View All Profiles
                        </Button>
                    </Box>
                </Container>
            </Box>

            {/* Features Section */}
            <Box sx={{ py: 8 }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Chip
                            label="Modern Caching"
                            sx={{ mb: 2, px: 2, py: 1 }}
                            variant="outlined"
                            color="primary"
                        />
                        <Typography variant="h2" component="h2" sx={{ mb: 2, fontWeight: 700 }}>
                            Why JCacheX?
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'text.secondary', maxWidth: '600px', mx: 'auto' }}>
                            Modern caching capabilities designed for high-performance Java applications
                        </Typography>
                    </Box>

                    <Box
                        sx={{
                            display: 'grid',
                            gridTemplateColumns: {
                                xs: '1fr',
                                sm: 'repeat(2, 1fr)',
                                md: 'repeat(3, 1fr)'
                            },
                            gap: 4
                        }}
                    >
                        {FEATURES.map((feature, index) => (
                            <Card
                                key={index}
                                sx={{
                                    height: '100%',
                                    transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                                    '&:hover': {
                                        transform: 'translateY(-4px)',
                                        boxShadow: (theme) => theme.shadows[8]
                                    }
                                }}
                            >
                                <CardContent sx={{ p: 3, textAlign: 'center' }}>
                                    <Box sx={{ color: 'primary.main', mb: 2 }}>
                                        {featureIcons[feature.icon] || <Star sx={{ fontSize: 40 }} />}
                                    </Box>
                                    <Typography variant="h6" component="h3" sx={{ mb: 2, fontWeight: 600 }}>
                                        {feature.title}
                                    </Typography>
                                    <Typography variant="body2" sx={{ color: 'text.secondary', lineHeight: 1.6, mb: 2 }}>
                                        {feature.description}
                                    </Typography>
                                    {feature.details && (
                                        <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0 }}>
                                            {feature.details.map((detail, detailIndex) => (
                                                <Typography
                                                    key={detailIndex}
                                                    component="li"
                                                    variant="caption"
                                                    sx={{
                                                        color: 'text.secondary',
                                                        fontSize: '0.75rem',
                                                        '&:before': {
                                                            content: '"•"',
                                                            color: 'primary.main',
                                                            fontWeight: 'bold',
                                                            display: 'inline-block',
                                                            width: '1em',
                                                            marginLeft: '-1em'
                                                        }
                                                    }}
                                                >
                                                    {detail}
                                                </Typography>
                                            ))}
                                        </Box>
                                    )}
                                </CardContent>
                            </Card>
                        ))}
                    </Box>
                </Container>
            </Box>

            {/* Progressive Learning Section */}
            <Box sx={{ py: 8, bgcolor: 'grey.50' }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Chip
                            label="Progressive Learning"
                            sx={{ mb: 2, px: 2, py: 1 }}
                            variant="outlined"
                            color="primary"
                        />
                        <Typography variant="h2" component="h2" sx={{ mb: 2, fontWeight: 700 }}>
                            Start Your Journey
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'text.secondary', maxWidth: '700px', mx: 'auto' }}>
                            From 30-second success to production-ready deployment.
                            Choose your learning path and see immediate results.
                        </Typography>
                    </Box>

                    <Box sx={{ maxWidth: 1000, mx: 'auto', mb: 4 }}>
                        <CodeTabs
                            tabs={heroCodeTabs}
                            showPerformanceMetrics={true}
                            showTryOnlineButtons={true}
                            showCopyButtons={true}
                            interactive={true}
                        />
                    </Box>

                    <Box sx={{ textAlign: 'center' }}>
                        <Button
                            component={Link}
                            to="/getting-started"
                            variant="contained"
                            size="large"
                            endIcon={<ArrowIcon />}
                            sx={{ px: 4, py: 1.5 }}
                        >
                            View Full Documentation
                        </Button>
                    </Box>
                </Container>
            </Box>

            {/* Real-World Examples Section */}
            <Box sx={{ py: 8 }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Chip
                            label="Real-World Examples"
                            sx={{ mb: 2, px: 2, py: 1 }}
                            variant="outlined"
                            color="secondary"
                        />
                        <Typography variant="h2" component="h2" sx={{ mb: 2, fontWeight: 700 }}>
                            Production Use Cases
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'text.secondary', maxWidth: '700px', mx: 'auto' }}>
                            See how JCacheX solves real-world problems in high-traffic applications
                        </Typography>
                    </Box>

                    <Box sx={{ maxWidth: 1000, mx: 'auto', mb: 4 }}>
                        <CodeTabs
                            tabs={REAL_WORLD_EXAMPLES}
                            showPerformanceMetrics={true}
                            showTryOnlineButtons={true}
                            showCopyButtons={true}
                            interactive={true}
                        />
                    </Box>

                    <Box sx={{ textAlign: 'center' }}>
                        <Button
                            component={Link}
                            to="/examples"
                            variant="outlined"
                            size="large"
                            endIcon={<ArrowIcon />}
                            sx={{ px: 4, py: 1.5 }}
                        >
                            View All Examples
                        </Button>
                    </Box>
                </Container>
            </Box>

            {/* Installation Section */}
            <Box sx={{ py: 8 }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Typography variant="h3" component="h2" sx={{ mb: 2, fontWeight: 700 }}>
                            Installation
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'text.secondary' }}>
                            Add JCacheX to your project with your preferred build tool
                        </Typography>
                    </Box>

                    <Box sx={{ maxWidth: 900, mx: 'auto' }}>
                        <CodeTabs tabs={INSTALLATION_TABS} />
                    </Box>
                </Container>
            </Box>

            {/* Performance Highlights Section */}
            <Box sx={{ py: 8, bgcolor: 'grey.50' }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Chip
                            label="Performance"
                            sx={{ mb: 2, px: 2, py: 1 }}
                            variant="outlined"
                            color="primary"
                        />
                        <Typography variant="h2" component="h2" sx={{ mb: 2, fontWeight: 700 }}>
                            Industry-Leading Performance
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'text.secondary', maxWidth: '700px', mx: 'auto' }}>
                            JCacheX delivers exceptional performance with profile-based optimization
                        </Typography>
                    </Box>

                    {/* Performance Metrics Grid */}
                    <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr 1fr' }, gap: 4, mb: 6 }}>
                        <Paper sx={{ p: 4, textAlign: 'center', background: 'linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%)' }}>
                            <Typography variant="h3" sx={{ fontWeight: 700, color: 'success.main', mb: 1 }}>
                                501.1M
                            </Typography>
                            <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                                ops/second
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                JCacheX-ZeroCopy peak throughput
                            </Typography>
                        </Paper>

                        <Paper sx={{ p: 4, textAlign: 'center', background: 'linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%)' }}>
                            <Typography variant="h3" sx={{ fontWeight: 700, color: 'primary.main', mb: 1 }}>
                                98.4%
                            </Typography>
                            <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                                efficiency
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                CPU scaling efficiency
                            </Typography>
                        </Paper>

                        <Paper sx={{ p: 4, textAlign: 'center', background: 'linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%)' }}>
                            <Typography variant="h3" sx={{ fontWeight: 700, color: 'secondary.main', mb: 1 }}>
                                7.1μs
                            </Typography>
                            <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
                                latency
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                JCacheX-WriteHeavy GET operations
                            </Typography>
                        </Paper>
                    </Box>

                    {/* Top Performing Profiles */}
                    <Box sx={{ mb: 6 }}>
                        <Typography variant="h4" sx={{ textAlign: 'center', mb: 4, fontWeight: 600 }}>
                            Top Performing Profiles
                        </Typography>
                        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 3 }}>
                            <Card sx={{ p: 3, background: 'linear-gradient(135deg, rgba(76, 175, 80, 0.1) 0%, rgba(139, 195, 74, 0.1) 100%)' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <Speed sx={{ color: 'success.main', mr: 1 }} />
                                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                        JCacheX-ZeroCopy
                                    </Typography>
                                </Box>
                                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                    Ultra-high throughput with zero-copy optimization
                                </Typography>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                    <Typography variant="body2" sx={{ fontWeight: 600 }}>
                                        501.1M ops/s
                                    </Typography>
                                    <Chip label="98.4% efficiency" color="success" size="small" />
                                </Box>
                            </Card>

                            <Card sx={{ p: 3, background: 'linear-gradient(135deg, rgba(33, 150, 243, 0.1) 0%, rgba(30, 136, 229, 0.1) 100%)' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <Security sx={{ color: 'primary.main', mr: 1 }} />
                                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                        JCacheX-WriteHeavy
                                    </Typography>
                                </Box>
                                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                    Optimized for write-intensive workloads
                                </Typography>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                    <Typography variant="body2" sx={{ fontWeight: 600 }}>
                                        224.6M ops/s
                                    </Typography>
                                    <Chip label="97.2% efficiency" color="primary" size="small" />
                                </Box>
                            </Card>
                        </Box>
                    </Box>

                    <Box sx={{ textAlign: 'center' }}>
                        <Button
                            component={Link}
                            to="/performance"
                            variant="contained"
                            size="large"
                            endIcon={<ArrowIcon />}
                            sx={{ px: 4, py: 1.5 }}
                        >
                            View Complete Benchmarks
                        </Button>
                    </Box>
                </Container>
            </Box>

            {/* Modules Section */}
            <Box sx={{ py: 8 }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Typography variant="h3" component="h2" sx={{ mb: 2, fontWeight: 700 }}>
                            Modules
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'text.secondary' }}>
                            Choose the modules that fit your technology stack
                        </Typography>
                    </Box>

                    <Box
                        sx={{
                            display: 'grid',
                            gridTemplateColumns: {
                                xs: '1fr',
                                md: 'repeat(3, 1fr)'
                            },
                            gap: 4
                        }}
                    >
                        {MODULES.map((module, index) => (
                            <Card
                                key={index}
                                sx={{
                                    height: '100%',
                                    transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                                    '&:hover': {
                                        transform: 'translateY(-4px)',
                                        boxShadow: (theme) => theme.shadows[8]
                                    }
                                }}
                            >
                                <CardContent sx={{ p: 3, textAlign: 'center' }}>
                                    <Box sx={{ color: 'primary.main', mb: 2 }}>
                                        {moduleIcons[module.name] || <Extension sx={{ fontSize: 40 }} />}
                                    </Box>
                                    <Typography variant="h6" component="h3" sx={{ mb: 2, fontWeight: 600 }}>
                                        {module.title}
                                    </Typography>
                                    <Typography variant="body2" sx={{ color: 'text.secondary', lineHeight: 1.6, mb: 2 }}>
                                        {module.description}
                                    </Typography>
                                    <Box component="ul" sx={{ listStyle: 'none', p: 0, m: 0 }}>
                                        {module.features.map((feature, featureIndex) => (
                                            <Typography
                                                key={featureIndex}
                                                component="li"
                                                variant="caption"
                                                sx={{
                                                    color: 'text.secondary',
                                                    fontSize: '0.75rem',
                                                    '&:before': {
                                                        content: '"•"',
                                                        color: 'primary.main',
                                                        fontWeight: 'bold',
                                                        display: 'inline-block',
                                                        width: '1em',
                                                        marginLeft: '-1em'
                                                    }
                                                }}
                                            >
                                                {feature}
                                            </Typography>
                                        ))}
                                    </Box>
                                </CardContent>
                            </Card>
                        ))}
                    </Box>
                </Container>
            </Box>

            {/* Call to Action */}
            <Box sx={{ py: 8, bgcolor: 'grey.50' }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="h3" component="h2" sx={{ mb: 2, fontWeight: 700 }}>
                            Ready to optimize your caching?
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'text.secondary', mb: 4 }}>
                            Start with intelligent profiles and scale from local to distributed
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
                            <Button
                                component={Link}
                                to="/getting-started"
                                variant="contained"
                                size="large"
                                sx={{ px: 4, py: 1.5 }}
                            >
                                Get Started
                            </Button>
                            <Button
                                component={Link}
                                to="/documentation"
                                variant="outlined"
                                size="large"
                                sx={{ px: 4, py: 1.5 }}
                            >
                                View Documentation
                            </Button>
                        </Box>
                    </Box>
                </Container>
            </Box>
        </Layout>
    );
};

export default HomeComponent;
