import React from 'react';
import { Link } from 'react-router-dom';
import { Container, Typography, Button, Box, Card, CardContent } from '@mui/material';
import { ArrowForward as ArrowIcon, PlayArrow as PlayIcon } from '@mui/icons-material';
import type { CodeTab } from '../types';
import { useVersion } from '../hooks';
import { INSTALLATION_TABS, FEATURES, EVICTION_STRATEGIES, MODULES, PERFORMANCE_STATS, ARCHITECTURE, BASIC_USAGE_JAVA, BASIC_USAGE_KOTLIN, SPRING_USAGE } from '../constants';
import type { Module } from '../types';
import { Section, Grid, FeatureCard, InstallationGuide, Badge } from './common';
import PageWrapper from './PageWrapper';
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

    return (
        <PageWrapper
            title="JCacheX - High Performance Java Caching Framework"
            description="JCacheX is a modern, high-performance Java caching framework with advanced features like distributed caching, smart eviction strategies, and Spring Boot integration."
            keywords="Java, cache, caching, framework, performance, distributed, spring boot"
            className="home"
        >
            {/* Hero Section */}
            <Box className="hero-section">
                <Container maxWidth="lg">
                    <Box className="hero-content" sx={{ textAlign: 'center' }}>
                        <Typography
                            variant="h1"
                            component="h1"
                            className="fade-in"
                        >
                            A high performance, open source
                            <br />
                            <span className="gradient-text">Java caching framework</span>
                        </Typography>

                        <Typography
                            variant="h5"
                            className="hero-subtitle fade-in"
                            sx={{ mx: 'auto', mt: 2 }}
                        >
                            JCacheX provides modern caching capabilities with distributed support,
                            intelligent eviction strategies, and seamless Spring Boot integration
                            for enterprise Java applications.
                        </Typography>

                        <Box className="hero-actions fade-in">
                            <Button
                                component={Link}
                                to="/getting-started"
                                variant="contained"
                                size="large"
                                className="gradient-button"
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
                                className="glass-button"
                                startIcon={<PlayIcon />}
                                sx={{ px: 4, py: 1.5 }}
                            >
                                View on GitHub
                            </Button>
                        </Box>

                        <Box className="hero-stats fade-in">
                            <Box className="stat-item">
                                <Typography className="stat-number">10x</Typography>
                                <Typography className="stat-label">Faster than Standard</Typography>
                            </Box>
                            <Box className="stat-item">
                                <Typography className="stat-number">100%</Typography>
                                <Typography className="stat-label">Type Safe</Typography>
                            </Box>
                            <Box className="stat-item">
                                <Typography className="stat-number">Zero</Typography>
                                <Typography className="stat-label">Dependencies</Typography>
                            </Box>
                        </Box>
                    </Box>
                </Container>
            </Box>

            {/* Features Section */}
            <Box className="section">
                <Container maxWidth="lg">
                    <Box className="section-header">
                        <Typography className="section-badge">
                            ✨ Modern Caching
                        </Typography>
                        <Typography variant="h2" component="h2">
                            Why JCacheX?
                        </Typography>
                        <Typography className="section-subtitle">
                            Modern caching capabilities designed for high-performance Java applications
                        </Typography>
                    </Box>

                    <Box className="features-grid">
                        <Card className="feature-card fade-in">
                            <CardContent>
                                <Box className="card-icon primary">
                                    <svg viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M13 3l3.5 6L20 7l-3.5 6 3.5 2-3.5 6L13 21l-3.5-6L6 17l3.5-6L6 9l3.5-6L13 3z" />
                                    </svg>
                                </Box>
                                <Typography className="card-title">High Performance</Typography>
                                <Typography className="card-description">
                                    Optimized algorithms and efficient memory management for maximum throughput
                                    and minimal latency in production environments.
                                </Typography>
                            </CardContent>
                        </Card>

                        <Card className="feature-card fade-in">
                            <CardContent>
                                <Box className="card-icon secondary">
                                    <svg viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z" />
                                    </svg>
                                </Box>
                                <Typography className="card-title">Smart Eviction</Typography>
                                <Typography className="card-description">
                                    Multiple eviction strategies including LRU, LFU, FIFO, and TTL-based
                                    policies with composable configurations for complex scenarios.
                                </Typography>
                            </CardContent>
                        </Card>

                        <Card className="feature-card fade-in">
                            <CardContent>
                                <Box className="card-icon success">
                                    <svg viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                                    </svg>
                                </Box>
                                <Typography className="card-title">Distributed Ready</Typography>
                                <Typography className="card-description">
                                    Built-in support for distributed caching with network protocols,
                                    automatic failover, and cluster management capabilities.
                                </Typography>
                            </CardContent>
                        </Card>

                        <Card className="feature-card fade-in">
                            <CardContent>
                                <Box className="card-icon warning">
                                    <svg viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z" />
                                    </svg>
                                </Box>
                                <Typography className="card-title">Spring Integration</Typography>
                                <Typography className="card-description">
                                    First-class Spring Boot support with auto-configuration,
                                    annotations, and seamless integration with Spring ecosystem.
                                </Typography>
                            </CardContent>
                        </Card>

                        <Card className="feature-card fade-in">
                            <CardContent>
                                <Box className="card-icon info">
                                    <svg viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
                                    </svg>
                                </Box>
                                <Typography className="card-title">Production Ready</Typography>
                                <Typography className="card-description">
                                    Comprehensive monitoring, metrics, circuit breakers, and resilience
                                    patterns for mission-critical applications.
                                </Typography>
                            </CardContent>
                        </Card>

                        <Card className="feature-card fade-in">
                            <CardContent>
                                <Box className="card-icon error">
                                    <svg viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M9 11H7v6h2v-6zm4 0h-2v6h2v-6zm4 0h-2v6h2v-6zm2-7v2H3V4h3.5l1-1h5l1 1H17z" />
                                    </svg>
                                </Box>
                                <Typography className="card-title">Zero Dependencies</Typography>
                                <Typography className="card-description">
                                    Minimal footprint with no external dependencies, ensuring
                                    compatibility and reducing potential security vulnerabilities.
                                </Typography>
                            </CardContent>
                        </Card>
                    </Box>
                </Container>
            </Box>

            {/* Quick Start Section */}
            <Box className="section section-alternate">
                <Container maxWidth="lg">
                    <Box className="section-header">
                        <Typography className="section-badge">
                            🚀 Get Started
                        </Typography>
                        <Typography variant="h2" component="h2">
                            Quick Start
                        </Typography>
                        <Typography className="section-subtitle">
                            Get up and running with JCacheX in minutes
                        </Typography>
                    </Box>

                    <Box className="code-tabs fade-in" sx={{ maxWidth: 800, mx: 'auto' }}>
                        <CodeTabs tabs={heroCodeTabs} />
                    </Box>

                    <Box sx={{ textAlign: 'center', mt: 4 }}>
                        <Button
                            component={Link}
                            to="/getting-started"
                            variant="contained"
                            size="large"
                            className="gradient-button"
                            endIcon={<ArrowIcon />}
                        >
                            View Full Documentation
                        </Button>
                    </Box>
                </Container>
            </Box>

            {/* Installation Section */}
            <Section>
                <div className="container">
                    <div className="section__header">
                        <h2 className="section__title">Installation</h2>
                        <p className="section__subtitle">
                            Add JCacheX to your project with your preferred build tool
                        </p>
                    </div>

                    <InstallationGuide
                        tabs={INSTALLATION_TABS}
                    />
                </div>
            </Section>

            {/* Modules Section */}
            <Section>
                <div className="container">
                    <div className="section__header">
                        <h2 className="section__title">Modules</h2>
                        <p className="section__subtitle">
                            Choose the components that fit your needs
                        </p>
                    </div>

                    <div className="grid grid--3-col">
                        {MODULES.map((module: Module) => (
                            <FeatureCard
                                key={module.name}
                                title={module.title}
                                description={module.description}
                                icon="module"
                            />
                        ))}
                    </div>
                </div>
            </Section>
        </PageWrapper>
    );
};

export default HomeComponent;
