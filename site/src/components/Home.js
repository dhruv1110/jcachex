import React from 'react';
import { useVersion } from '../hooks';
import { INSTALLATION_TABS, FEATURES, EVICTION_STRATEGIES, MODULES, PERFORMANCE_STATS, ARCHITECTURE, BASIC_USAGE_JAVA, BASIC_USAGE_KOTLIN, SPRING_USAGE } from '../constants';
import { Section, Grid, FeatureCard, InstallationGuide, Badge } from './common';
import CodeTabs from './CodeTabs';
import './Home.css';

const Home = () => {
    const { version } = useVersion();

    const heroCodeTabs = [
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
        <div className="home">
            {/* Hero Section */}
            <Section background="gradient" padding="xlarge" centered>
                <div className="hero-content">
                    <div className="hero-badges">
                        <Badge variant="github" href="https://github.com/dhruv1110/JCacheX">
                            GitHub
                        </Badge>
                        <Badge variant="maven" href="https://central.sonatype.com/artifact/io.github.dhruv1110/jcachex-core">
                            Maven Central
                        </Badge>
                        <Badge variant="primary">
                            v{version}
                        </Badge>
                    </div>

                    <h1 className="hero-title">
                        JCacheX
                    </h1>
                    <p className="hero-subtitle">
                        High-performance, developer-friendly caching library for Java and Kotlin
                    </p>

                    <div className="hero-stats">
                        {PERFORMANCE_STATS.map((stat, index) => (
                            <div key={index} className="stat-item">
                                <div className="stat-value">{stat.value}</div>
                                <div className="stat-label">{stat.label}</div>
                            </div>
                        ))}
                    </div>
                </div>
            </Section>

            {/* Quick Start Code Section */}
            <Section padding="large" centered>
                <h2 className="section-title">Quick Start</h2>
                <p className="section-subtitle">
                    Get started with JCacheX in minutes. Choose your preferred language:
                </p>
                <div className="hero-code-container">
                    <CodeTabs tabs={heroCodeTabs} />
                </div>
            </Section>

            {/* Features Section */}
            <Section
                background="light"
                padding="large"
                title="Why JCacheX?"
                subtitle="Built for modern applications with performance, simplicity, and flexibility in mind"
                centered
            >
                <Grid columns={3} gap="large">
                    {FEATURES.map((feature, index) => (
                        <FeatureCard
                            key={index}
                            icon={feature.icon}
                            title={feature.title}
                            description={feature.description}
                            details={feature.details}
                        />
                    ))}
                </Grid>
            </Section>

            {/* Installation Section */}
            <Section padding="large">
                <InstallationGuide tabs={INSTALLATION_TABS} />
            </Section>

            {/* Modules Section */}
            <Section
                background="light"
                padding="large"
                title="Modules"
                subtitle="JCacheX is designed as a modular library. Choose the modules you need:"
                centered
            >
                <Grid columns={3} gap="large">
                    {MODULES.map((module, index) => (
                        <FeatureCard
                            key={index}
                            icon="📦"
                            title={module.title}
                            description={module.description}
                            details={module.features}
                            variant="compact"
                        />
                    ))}
                </Grid>
            </Section>

            {/* Eviction Strategies Section */}
            <Section
                padding="large"
                title="Eviction Strategies"
                subtitle="Choose the right eviction strategy for your use case"
                centered
            >
                <Grid columns={3} gap="default">
                    {EVICTION_STRATEGIES.map((strategy, index) => (
                        <FeatureCard
                            key={index}
                            icon="🔄"
                            title={strategy.title}
                            description={strategy.description}
                            details={[strategy.useCase]}
                            variant="compact"
                        />
                    ))}
                </Grid>
            </Section>

            {/* Architecture Section */}
            <Section
                background="light"
                padding="large"
                title="Architecture"
                subtitle="Clean, modular design with pluggable components"
                centered
            >
                <Grid columns={3} gap="large">
                    {ARCHITECTURE.map((component, index) => (
                        <FeatureCard
                            key={index}
                            icon="🏗️"
                            title={component.name}
                            description={component.description}
                            variant="compact"
                        />
                    ))}
                </Grid>
            </Section>

            {/* Call to Action */}
            <Section background="gradient" padding="large" centered>
                <div className="cta-content">
                    <h2 className="cta-title">Ready to get started?</h2>
                    <p className="cta-subtitle">
                        Join thousands of developers using JCacheX in production
                    </p>
                    <div className="cta-buttons">
                        <Badge variant="primary" size="large" href="/getting-started">
                            Get Started
                        </Badge>
                        <Badge variant="default" size="large" href="/examples">
                            View Examples
                        </Badge>
                    </div>
                </div>
            </Section>
        </div>
    );
};

export default Home;
