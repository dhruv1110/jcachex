import React from 'react';
import { Link } from 'react-router-dom';
import type { CodeTab } from '../types';
import { useVersion } from '../hooks';
import { INSTALLATION_TABS, FEATURES, EVICTION_STRATEGIES, MODULES, PERFORMANCE_STATS, ARCHITECTURE, BASIC_USAGE_JAVA, BASIC_USAGE_KOTLIN, SPRING_USAGE } from '../constants';
import type { Module } from '../types';
import { Section, Grid, FeatureCard, InstallationGuide, Badge } from './common';
import PageWrapper from './PageWrapper';
import CodeTabs from './CodeTabs';

// Icons (you can replace these with actual icon components)
const PlayIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
        <path d="M8 5v14l11-7z" />
    </svg>
);

const ArrowRightIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
        <path d="M5 12h14m-7-7l7 7-7 7" />
    </svg>
);

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
            <section className="hero">
                <div className="hero__container">
                    <h1 className="hero__title">
                        A high performance, open source
                        <br />
                        <span className="gradient-text">Java caching framework</span>
                    </h1>

                    <p className="hero__subtitle">
                        JCacheX provides modern caching capabilities with distributed support,
                        intelligent eviction strategies, and seamless Spring Boot integration
                        for enterprise Java applications.
                    </p>

                    <div className="hero__actions">
                        <Link to="/getting-started" className="btn btn--primary btn--lg">
                            Get Started
                            <ArrowRightIcon />
                        </Link>
                        <a
                            href="https://github.com/dhruv1110/JCacheX"
                            className="btn btn--secondary btn--lg"
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            <PlayIcon />
                            View on GitHub
                        </a>
                    </div>

                    <div className="hero__stats">
                        <div className="hero__stat">
                            <div className="hero__stat-number">10x</div>
                            <div className="hero__stat-label">Faster than Standard</div>
                        </div>
                        <div className="hero__stat">
                            <div className="hero__stat-number">100%</div>
                            <div className="hero__stat-label">Type Safe</div>
                        </div>
                        <div className="hero__stat">
                            <div className="hero__stat-number">Zero</div>
                            <div className="hero__stat-label">Dependencies</div>
                        </div>
                    </div>
                </div>
            </section>

            {/* Features Section */}
            <section className="section">
                <div className="container">
                    <div className="section__header">
                        <h2 className="section__title">Why JCacheX?</h2>
                        <p className="section__subtitle">
                            Modern caching capabilities designed for high-performance Java applications
                        </p>
                    </div>

                    <div className="features__grid">
                        <div className="features__card">
                            <div className="card__icon card__icon--primary">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M13 3l3.5 6L20 7l-3.5 6 3.5 2-3.5 6L13 21l-3.5-6L6 17l3.5-6L6 9l3.5-6L13 3z" />
                                </svg>
                            </div>
                            <div className="card__title">High Performance</div>
                            <div className="card__description">
                                Optimized algorithms and efficient memory management for maximum throughput
                                and minimal latency in production environments.
                            </div>
                        </div>

                        <div className="features__card">
                            <div className="card__icon card__icon--secondary">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z" />
                                </svg>
                            </div>
                            <div className="card__title">Smart Eviction</div>
                            <div className="card__description">
                                Multiple eviction strategies including LRU, LFU, FIFO, and TTL-based
                                policies with composable configurations for complex scenarios.
                            </div>
                        </div>

                        <div className="features__card">
                            <div className="card__icon card__icon--success">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                                </svg>
                            </div>
                            <div className="card__title">Distributed Ready</div>
                            <div className="card__description">
                                Built-in support for distributed caching with network protocols,
                                automatic failover, and cluster management capabilities.
                            </div>
                        </div>

                        <div className="features__card">
                            <div className="card__icon card__icon--warning">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z" />
                                </svg>
                            </div>
                            <div className="card__title">Spring Integration</div>
                            <div className="card__description">
                                First-class Spring Boot support with auto-configuration,
                                annotations, and seamless integration with Spring ecosystem.
                            </div>
                        </div>

                        <div className="features__card">
                            <div className="card__icon card__icon--info">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
                                </svg>
                            </div>
                            <div className="card__title">Production Ready</div>
                            <div className="card__description">
                                Comprehensive monitoring, metrics, circuit breakers, and resilience
                                patterns for mission-critical applications.
                            </div>
                        </div>

                        <div className="features__card">
                            <div className="card__icon card__icon--error">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M9 11H7v6h2v-6zm4 0h-2v6h2v-6zm4 0h-2v6h2v-6zm2-7v2H3V4h3.5l1-1h5l1 1H17z" />
                                </svg>
                            </div>
                            <div className="card__title">Zero Dependencies</div>
                            <div className="card__description">
                                Minimal footprint with no external dependencies, ensuring
                                compatibility and reducing potential security vulnerabilities.
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* Quick Start Section */}
            <section className="section quick-start">
                <div className="container">
                    <div className="quick-start__content">
                        <div className="quick-start__text">
                            <h3>Get started in minutes</h3>
                            <p>
                                Add JCacheX to your project and start caching with just a few lines of code.
                                Our intuitive API makes it easy to implement caching in both new and existing applications.
                            </p>
                            <Link to="/getting-started" className="btn btn--primary">
                                View Documentation
                                <ArrowRightIcon />
                            </Link>
                        </div>

                        <div className="quick-start__code">
                            <div className="code-card__header">
                                <div className="code-card__header-title">Quick Start Example</div>
                            </div>
                            <div className="code-card__content">
                                <pre><code>{`// Create a cache with TTL
Cache<String, User> userCache = CacheFactory
    .builder(String.class, User.class)
    .maxSize(1000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();

// Store and retrieve data
userCache.put("user123", user);
User cachedUser = userCache.get("user123");`}</code></pre>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* Showcase Section */}
            <section className="section showcase">
                <div className="container">
                    <div className="section__header">
                        <h2 className="section__title">Trusted by developers worldwide</h2>
                        <p className="section__subtitle">
                            Join thousands of developers building high-performance applications with JCacheX
                        </p>
                    </div>

                    <div className="showcase__logos">
                        {/* Add your company logos here */}
                        <div className="badge badge--neutral badge--lg">Enterprise Ready</div>
                        <div className="badge badge--success badge--lg">Production Tested</div>
                        <div className="badge badge--primary badge--lg">Open Source</div>
                    </div>
                </div>
            </section>

            {/* CTA Section */}
            <section className="section cta">
                <div className="container">
                    <div className="cta__content">
                        <h2 className="section__title">Ready to accelerate your application?</h2>
                        <p className="section__subtitle">
                            Start using JCacheX today and experience the difference high-performance caching can make.
                        </p>

                        <div className="cta__actions">
                            <Link to="/getting-started" className="btn btn--lg">
                                Get Started Now
                                <ArrowRightIcon />
                            </Link>
                            <Link to="/examples" className="btn btn--secondary btn--lg">
                                View Examples
                            </Link>
                        </div>
                    </div>
                </div>
            </section>
        </PageWrapper>
    );
};

export default HomeComponent;
