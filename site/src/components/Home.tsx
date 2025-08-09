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
            <Box className="jcx-hero-bg jcx-divider" sx={{ py: { xs: 8, md: 12 }, minHeight: '70vh', display: 'flex', alignItems: 'center' }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="h1" component="h1" className="jcx-accent-text" sx={{ fontSize: { xs: '2.5rem', md: '3.5rem', lg: '4rem' }, fontWeight: 700, mb: 3, lineHeight: 1.1 }}>
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
                                sx={{ px: 4, py: 1.5, borderColor: 'rgba(255,255,255,0.3)', color: 'grey.300' }}
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
                                    <Typography variant="h4" sx={{ fontWeight: 700, color: 'grey.100' }}>
                                        {stat.number}
                                    </Typography>
                                    <Typography variant="body2" sx={{ color: 'grey.400' }}>
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

            {/* Kubernetes Distributed Caching Section */}
            <Box sx={{ py: 8, bgcolor: 'background.default', borderTop: '1px solid rgba(255,255,255,0.06)' }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Chip
                            label="Kubernetes"
                            sx={{ mb: 2, px: 2, py: 1, borderColor: 'rgba(255,255,255,0.18)', color: 'grey.200' }}
                            variant="outlined"
                        />
                        <Typography variant="h2" component="h2" sx={{ mb: 2, fontWeight: 700 }}>
                            Kubernetes Distributed Caching
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'grey.400', maxWidth: '720px', mx: 'auto' }}>
                            Our native Kubernetes implementation provides pod-aware node discovery, consistent hashing
                            for key distribution, and graceful scaling to build low-latency, resilient clusters.
                        </Typography>
                    </Box>

                    <Box sx={{
                        display: 'grid',
                        gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                        gap: 3,
                        mb: 4
                    }}>
                        <Card sx={{ p: 2 }}>
                            <CardContent>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                    <Cloud sx={{ color: 'primary.light', mr: 1 }} />
                                    <Typography variant="h6" sx={{ fontWeight: 600 }}>Native K8s Discovery</Typography>
                                </Box>
                                <Typography variant="body2" sx={{ color: 'grey.400' }}>
                                    Pod name identification, service-based discovery, and health-aware topology updates
                                    ensure the cluster adapts seamlessly to rolling updates and auto-scaling events.
                                </Typography>
                            </CardContent>
                        </Card>

                        <Card sx={{ p: 2 }}>
                            <CardContent>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                    <Security sx={{ color: 'secondary.light', mr: 1 }} />
                                    <Typography variant="h6" sx={{ fontWeight: 600 }}>Consistent Hashing</Typography>
                                </Box>
                                <Typography variant="body2" sx={{ color: 'grey.400' }}>
                                    Balanced partitioning with minimal rebalancing on node changes. Low overhead network
                                    protocol and timeouts tuned for high throughput and predictable latency.
                                </Typography>
                            </CardContent>
                        </Card>
                    </Box>

                    <Box sx={{ textAlign: 'center' }}>
                        <Button
                            component={Link}
                            to="/documentation#kubernetes-distributed"
                            variant="contained"
                            size="large"
                            endIcon={<ArrowIcon />}
                            sx={{ px: 4, py: 1.5, mr: 2 }}
                        >
                            Learn More
                        </Button>
                        <Button
                            component={Link}
                            to="/examples"
                            variant="outlined"
                            size="large"
                            sx={{ px: 4, py: 1.5 }}
                        >
                            View Examples
                        </Button>
                    </Box>
                </Container>
            </Box>

            {/* Installation Section (concise) */}
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
