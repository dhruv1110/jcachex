import React from 'react';
import { useVersion } from '../hooks';
import { INSTALLATION_TABS, BASIC_USAGE_JAVA, BASIC_USAGE_KOTLIN } from '../constants';
import { Section, Grid, FeatureCard, InstallationGuide } from './common';
import CodeTabs from './CodeTabs';
import { CodeTab, Feature } from '../types';
import PageWrapper from './PageWrapper';

const GettingStarted: React.FC = () => {
    const { version } = useVersion();

    const basicUsageTabs: CodeTab[] = [
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
        }
    ];

    const asyncTabs: CodeTab[] = [
        {
            id: 'java',
            label: 'Java',
            language: 'java',
            code: `import java.util.concurrent.CompletableFuture;

// Async operations with CompletableFuture
CompletableFuture<User> futureUser = cache.getAsync("user123");

// Handle result
futureUser.thenAccept(user -> {
    if (user != null) {
        System.out.println("User found: " + user.getName());
    } else {
        System.out.println("User not found");
    }
});

// Async put operation
CompletableFuture<Void> putFuture = cache.putAsync("user456", new User("Bob", "bob@example.com"));

// Combine async operations
CompletableFuture<String> combined = cache.getAsync("user123")
    .thenCompose(user -> {
        if (user != null) {
            return CompletableFuture.completedFuture(user.getName());
        } else {
            return cache.putAsync("user123", new User("Default", "default@example.com"))
                .thenApply(v -> "Default");
        }
    });`
        },
        {
            id: 'kotlin',
            label: 'Kotlin',
            language: 'kotlin',
            code: `import kotlinx.coroutines.*

// Async operations with coroutines
suspend fun getUserAsync(id: String): User? {
    return cache.getAsync(id).await()
}

// Usage in coroutine
runBlocking {
    val user = getUserAsync("user123")
    if (user != null) {
        println("User found: \${user.name}")
    } else {
        println("User not found")
    }
}

// Async put operation
runBlocking {
    cache.putAsync("user456", User("Bob", "bob@example.com")).await()
    println("User stored successfully")
}

// Combine async operations
suspend fun getOrCreate(id: String): User = withContext(Dispatchers.IO) {
    cache.getAsync(id).await() ?: run {
        val newUser = User("Default", "default@example.com")
        cache.putAsync(id, newUser).await()
        newUser
    }
}`
        }
    ];

    const springConfigTabs: CodeTab[] = [
        {
            id: 'yaml',
            label: 'application.yml',
            language: 'yaml',
            code: `# JCacheX Configuration
jcachex:
  enabled: true
  default:
    maximum-size: 1000
    expire-after-write-seconds: 1800
    eviction-strategy: LRU
    record-stats: true

  caches:
    users:
      maximum-size: 5000
      expire-after-write-seconds: 3600
      eviction-strategy: LFU

    products:
      maximum-size: 10000
      expire-after-write-seconds: 7200
      eviction-strategy: LRU`
        },
        {
            id: 'properties',
            label: 'application.properties',
            language: 'properties',
            code: `# JCacheX Configuration
jcachex.enabled=true
jcachex.default.maximum-size=1000
jcachex.default.expire-after-write-seconds=1800
jcachex.default.eviction-strategy=LRU
jcachex.default.record-stats=true

# Named caches
jcachex.caches.users.maximum-size=5000
jcachex.caches.users.expire-after-write-seconds=3600
jcachex.caches.users.eviction-strategy=LFU

jcachex.caches.products.maximum-size=10000
jcachex.caches.products.expire-after-write-seconds=7200
jcachex.caches.products.eviction-strategy=LRU`
        },
        {
            id: 'java',
            label: 'Java Config',
            language: 'java',
            code: `@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new JCacheXCacheManager();
    }

    @Bean
    public CacheConfigurer cacheConfigurer() {
        return new CacheConfigurer() {
            @Override
            public CacheManager cacheManager() {
                return new JCacheXCacheManager(
                    CacheConfig.<String, Object>builder()
                        .maximumSize(1000L)
                        .expireAfterWrite(Duration.ofMinutes(30))
                        .evictionStrategy(EvictionStrategy.LRU)
                        .recordStats(true)
                        .build()
                );
            }
        };
    }
}`
        }
    ];

    const configurationOptions = [
        {
            icon: 'config',
            title: 'Size Limits',
            description: 'Control cache size with maximum entries and memory-based limits',
            details: ['Maximum size', 'Weighted size', 'Memory-based eviction']
        },
        {
            icon: 'time',
            title: 'Expiration Policies',
            description: 'Configure time-based expiration policies',
            details: ['Expire after write', 'Expire after access', 'Custom TTL']
        },
        {
            icon: 'strategy',
            title: 'Eviction Strategies',
            description: 'Choose from multiple eviction algorithms',
            details: ['LRU', 'LFU', 'FIFO', 'Weight-based']
        },
        {
            icon: 'monitoring',
            title: 'Performance Monitoring',
            description: 'Built-in metrics and monitoring capabilities',
            details: ['Hit/miss ratios', 'Performance metrics', 'Health checks']
        }
    ];

    return (
        <PageWrapper
            title="Getting Started - JCacheX Documentation"
            description="Get up and running with JCacheX in minutes. Learn installation, basic usage, and advanced configuration for Java and Kotlin projects."
            keywords="JCacheX, getting started, installation, documentation, Java cache, Kotlin"
            className="getting-started-page"
        >

            {/* Header */}
            <Section background="gradient" padding="lg" centered>
                <div className="docs-header">
                    <h1 className="docs-title">Getting Started</h1>
                    <p className="docs-subtitle">
                        Get up and running with JCacheX in minutes. This guide covers installation,
                        basic usage, and advanced configuration options for both Java and Kotlin projects.
                    </p>
                </div>
            </Section>

            {/* Installation */}
            <Section background="dark" padding="lg" className="installation-section">
                <div className="installation-header">
                    <h2>Installation</h2>
                    <p>Add JCacheX to your project with your preferred build tool</p>
                </div>
                <InstallationGuide
                    tabs={INSTALLATION_TABS}
                    title="Installation"
                    description="Add JCacheX to your project with your preferred build tool"
                />
            </Section>

            {/* Quick Start */}
            <Section background="primary" padding="lg" className="quick-start-section">
                <div className="quick-start-header">
                    <h2>Quick Start</h2>
                    <p>Basic cache setup and operations in Java and Kotlin</p>
                </div>
                <CodeTabs tabs={basicUsageTabs} />
            </Section>

            {/* Async Operations */}
            <Section background="dark" padding="lg" className="async-section">
                <div className="async-header">
                    <h2>Async Operations</h2>
                    <p>Leverage async capabilities for high-performance applications</p>
                </div>
                <CodeTabs tabs={asyncTabs} />
            </Section>

            {/* Spring Configuration */}
            <Section background="primary" padding="lg" className="spring-config-section">
                <div className="spring-config-header">
                    <h2>Spring Boot Configuration</h2>
                    <p>Configure JCacheX in your Spring Boot application</p>
                </div>
                <CodeTabs tabs={springConfigTabs} />
            </Section>

            {/* Configuration Options */}
            <Section background="dark" padding="lg" className="config-options-section">
                <div className="config-options-header">
                    <h2>Configuration Options</h2>
                    <p>Customize JCacheX behavior for your specific needs</p>
                </div>
                <Grid>
                    {configurationOptions.map((option, index) => (
                        <FeatureCard
                            key={index}
                            icon={option.icon}
                            title={option.title}
                            description={option.description}
                            details={option.details}
                        />
                    ))}
                </Grid>
            </Section>

            {/* Next Steps */}
            <Section background="gradient" padding="lg" centered>
                <div className="next-steps-cta">
                    <h3>Ready to Explore More?</h3>
                    <p>
                        Check out our comprehensive examples and framework-specific guides
                        to master JCacheX in your applications.
                    </p>
                    <div className="cta-buttons">
                        <a href="/examples" className="btn btn-primary">
                            View Examples
                        </a>
                        <a href="/spring" className="btn btn-secondary">
                            Spring Guide
                        </a>
                    </div>
                </div>
            </Section>
        </PageWrapper>
    );
};

export default GettingStarted;
