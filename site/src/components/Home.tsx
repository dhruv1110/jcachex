import React from 'react';
import { Link } from 'react-router-dom';
import { Container, Typography, Button, Box, Card, CardContent, Chip, Paper } from '@mui/material';
import { ArrowForward as ArrowIcon, PlayArrow as PlayIcon, Speed, Security, Cloud, Settings, Star, Delete, Extension, Memory, Code } from '@mui/icons-material';
import type { CodeTab, Module } from '../types';
import { useVersion } from '../hooks';
import { BASIC_USAGE_JAVA, BASIC_USAGE_KOTLIN, SPRING_USAGE, INSTALLATION_TABS, MODULES, FEATURES } from '../constants';
import PageWrapper from './PageWrapper';
import Layout from './Layout';
import CodeTabs from './CodeTabs';

const HomeComponent: React.FC = () => {
    const { version } = useVersion();

    const heroCodeTabs: CodeTab[] = [
        {
            id: 'java',
            label: 'Java',
            language: 'java',
            code: BASIC_USAGE_JAVA
        },
        {
            id: 'kotlin',
            label: 'Kotlin',
            language: 'kotlin',
            code: BASIC_USAGE_KOTLIN
        },
        {
            id: 'spring',
            label: 'Spring Boot',
            language: 'java',
            code: SPRING_USAGE
        }
    ];

    // Convert features from constants to MUI compatible format
    const featureIcons: { [key: string]: JSX.Element } = {
        '⚡': <Speed sx={{ fontSize: 40 }} />,
        '🔧': <Settings sx={{ fontSize: 40 }} />,
        '🔄': <Code sx={{ fontSize: 40 }} />,
        '🍃': <Extension sx={{ fontSize: 40 }} />,
        '🌐': <Cloud sx={{ fontSize: 40 }} />,
        '📊': <Star sx={{ fontSize: 40 }} />
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
                            A high performance, open source
                            <br />
                            Java caching framework
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
                            JCacheX provides modern caching capabilities with distributed support,
                            intelligent eviction strategies, and seamless Spring Boot integration
                            for enterprise Java applications.
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
                                { number: '7.9ns', label: 'Fastest GET (ZeroCopy)' },
                                { number: '2.6x', label: 'Faster than Caffeine' },
                                { number: 'O(1)', label: 'Eviction Strategies' }
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

            {/* Features Section */}
            <Box sx={{ py: 8 }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Chip
                            label="✨ Modern Caching"
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

            {/* Quick Start Section */}
            <Box sx={{ py: 8, bgcolor: 'grey.50' }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Chip
                            label="🚀 Get Started"
                            sx={{ mb: 2, px: 2, py: 1 }}
                            variant="outlined"
                            color="primary"
                        />
                        <Typography variant="h2" component="h2" sx={{ mb: 2, fontWeight: 700 }}>
                            Quick Start
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'text.secondary' }}>
                            Get up and running with JCacheX in minutes
                        </Typography>
                    </Box>

                    <Box sx={{ maxWidth: 800, mx: 'auto', mb: 4 }}>
                        <CodeTabs tabs={heroCodeTabs} />
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

            {/* Performance Section */}
            <Box sx={{ py: 8, bgcolor: 'linear-gradient(135deg, #1e3a8a 0%, #7c3aed 100%)' }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Chip
                            label="🚀 Performance"
                            sx={{ mb: 2, px: 2, py: 1, bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                            variant="outlined"
                        />
                        <Typography variant="h2" component="h2" sx={{ mb: 2, fontWeight: 700, color: 'white' }}>
                            Industry-Leading Performance
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'rgba(255,255,255,0.8)', maxWidth: '600px', mx: 'auto' }}>
                            Outperform industry leaders with our optimized cache implementations
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
                        {[
                            {
                                title: 'ZeroCopy Cache',
                                performance: '7.9ns',
                                description: '2.6x faster than Caffeine',
                                icon: '🏆'
                            },
                            {
                                title: 'Locality Optimized',
                                performance: '9.7ns',
                                description: '1.9x faster than Caffeine',
                                icon: '⚡'
                            },
                            {
                                title: 'O(1) Eviction',
                                performance: '100%',
                                description: 'All strategies optimized',
                                icon: '🎯'
                            }
                        ].map((perf, index) => (
                            <Card
                                key={index}
                                sx={{
                                    bgcolor: 'rgba(255,255,255,0.1)',
                                    backdropFilter: 'blur(10px)',
                                    border: '1px solid rgba(255,255,255,0.2)',
                                    transition: 'transform 0.2s ease-in-out',
                                    '&:hover': {
                                        transform: 'translateY(-4px)',
                                        bgcolor: 'rgba(255,255,255,0.15)'
                                    }
                                }}
                            >
                                <CardContent sx={{ p: 3, textAlign: 'center' }}>
                                    <Typography variant="h3" sx={{ mb: 1 }}>
                                        {perf.icon}
                                    </Typography>
                                    <Typography variant="h6" sx={{ mb: 1, fontWeight: 600, color: 'white' }}>
                                        {perf.title}
                                    </Typography>
                                    <Typography variant="h4" sx={{ fontWeight: 700, color: '#fbbf24', mb: 1 }}>
                                        {perf.performance}
                                    </Typography>
                                    <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.8)' }}>
                                        {perf.description}
                                    </Typography>
                                </CardContent>
                            </Card>
                        ))}
                    </Box>

                    <Box sx={{ textAlign: 'center' }}>
                        <Button
                            component={Link}
                            to="/performance"
                            variant="contained"
                            size="large"
                            sx={{
                                bgcolor: 'white',
                                color: 'primary.main',
                                px: 4,
                                py: 1.5,
                                '&:hover': {
                                    bgcolor: 'rgba(255,255,255,0.9)'
                                }
                            }}
                        >
                            View Detailed Performance Benchmarks
                        </Button>
                    </Box>
                </Container>
            </Box>

            {/* Modules Section */}
            <Box sx={{ py: 8, bgcolor: 'grey.50' }}>
                <Container maxWidth="lg">
                    <Box sx={{ textAlign: 'center', mb: 6 }}>
                        <Chip
                            label="📦 Modules"
                            sx={{ mb: 2, px: 2, py: 1 }}
                            variant="outlined"
                            color="primary"
                        />
                        <Typography variant="h2" component="h2" sx={{ mb: 2, fontWeight: 700 }}>
                            Choose Your Components
                        </Typography>
                        <Typography variant="h6" sx={{ color: 'text.secondary' }}>
                            Modular architecture - use only what you need
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
                        {MODULES.map((module: Module, index) => (
                            <Card
                                key={module.name}
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
                                    <Box sx={{ color: 'secondary.main', mb: 2 }}>
                                        {moduleIcons[module.name] || <Extension sx={{ fontSize: 40 }} />}
                                    </Box>
                                    <Typography variant="h6" component="h3" sx={{ mb: 1, fontWeight: 600 }}>
                                        {module.title}
                                    </Typography>
                                    <Typography
                                        variant="caption"
                                        sx={{
                                            color: 'text.secondary',
                                            fontFamily: 'monospace',
                                            bgcolor: 'grey.100',
                                            px: 1,
                                            py: 0.5,
                                            borderRadius: 1,
                                            display: 'block',
                                            mb: 2
                                        }}
                                    >
                                        {module.name}
                                    </Typography>
                                    <Typography variant="body2" sx={{ color: 'text.secondary', lineHeight: 1.6, mb: 2 }}>
                                        {module.description}
                                    </Typography>
                                    {module.features && (
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
                                                            color: 'secondary.main',
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
                                    )}
                                </CardContent>
                            </Card>
                        ))}
                    </Box>
                </Container>
            </Box>
        </Layout>
    );
};

export default HomeComponent;
