import React from 'react';
import type { CodeTab } from '../types';
import { useVersion, useSEO } from '../hooks';
import { INSTALLATION_TABS, FEATURES, EVICTION_STRATEGIES, MODULES, PERFORMANCE_STATS, ARCHITECTURE, BASIC_USAGE_JAVA, BASIC_USAGE_KOTLIN, SPRING_USAGE } from '../constants';
import { Section, Grid, FeatureCard, InstallationGuide, Badge } from './common';
import { MetaTags } from './SEO';
import CodeTabs from './CodeTabs';
import './Home.css';

const Home: React.FC = () => {
    const { version } = useVersion();

    const { getCurrentPageSEO } = useSEO();
    const seoData = getCurrentPageSEO();

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
        <div className="home">
            <MetaTags seo={seoData} />

            {/* Hero Section */}
            <Section background="gradient" padding="lg" centered>
                <div className="hero-content">
                    <div className="hero-badges">
                        <Badge variant="github" size="medium" href="https://github.com/dhruv1110/JCacheX">
                            GitHub
                        </Badge>
                        <Badge variant="maven" size="medium" href="https://mvnrepository.com/artifact/io.github.dhruv1110/jcachex-core">
                            Maven Central
                        </Badge>
                        <Badge variant="primary" size="medium">
                            v{version}
                        </Badge>
                    </div>

                    <h1 className="hero-title">JCacheX</h1>
                    <p className="hero-subtitle">
                        High-performance, thread-safe caching library for Java & Kotlin applications with
                        async support, multiple eviction strategies, and Spring Boot integration.
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

            {/* Code Examples */}
            <Section background="dark" padding="lg" centered>
                <div className="hero-code-container">
                    <CodeTabs tabs={heroCodeTabs} />
                </div>
            </Section>

            {/* Installation */}
            <Section
                padding="lg"
                title="Installation"
                subtitle="Get started with JCacheX in your project"
                centered
            >
                <InstallationGuide tabs={INSTALLATION_TABS} />
            </Section>

            {/* Features */}
            <Section
                background="dark"
                padding="lg"
                title="Core Features"
                subtitle="Everything you need for modern caching"
                centered
            >
                <Grid columns={3} gap="lg">
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

            {/* Modules */}
            <Section
                padding="lg"
                title="Modules"
                subtitle="Choose the right modules for your needs"
                centered
            >
                <Grid columns={3} gap="lg">
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

            {/* Eviction Strategies */}
            <Section
                background="dark"
                padding="lg"
                title="Eviction Strategies"
                subtitle="Choose the right eviction strategy for your use case"
                centered
            >
                <Grid columns={3} gap="md">
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
                padding="lg"
                title="Architecture"
                subtitle="Clean, modular design with pluggable components"
                centered
            >
                <Grid columns={3} gap="lg">
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
            <Section background="gradient" padding="lg" centered>
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
