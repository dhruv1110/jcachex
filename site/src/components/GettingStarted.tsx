import React from 'react';
import { useVersion } from '../hooks';
import { INSTALLATION_TABS, BASIC_USAGE_JAVA, BASIC_USAGE_KOTLIN } from '../constants';
import { Section, Grid, FeatureCard, InstallationGuide } from './common';
import CodeTabs from './CodeTabs';
import { CodeTab, Feature } from '../types';
import './GettingStarted.css';

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

    const configurationOptions: Feature[] = [
        {
            icon: '⚙️',
            title: 'Size Limits',
            description: 'Control cache size with maximum entries and memory-based limits',
            details: ['Maximum size', 'Weighted size', 'Memory-based eviction']
        },
        {
            icon: '⏰',
            title: 'Expiration',
            description: 'Configure time-based expiration policies',
            details: ['Expire after write', 'Expire after access', 'Custom TTL']
        },
        {
            icon: '🔄',
            title: 'Eviction Strategies',
            description: 'Choose from multiple eviction algorithms',
            details: ['LRU', 'LFU', 'FIFO', 'Weight-based']
        },
        {
            icon: '📊',
            title: 'Monitoring',
            description: 'Built-in statistics and metrics collection',
            details: ['Hit/miss ratios', 'Access patterns', 'Performance metrics']
        }
    ];

    const bestPractices: Feature[] = [
        {
            icon: '🎯',
            title: 'Cache Strategy',
            description: 'Choose appropriate cache patterns for your use case',
            details: ['Cache-aside', 'Write-through', 'Write-behind', 'Refresh-ahead']
        },
        {
            icon: '🔧',
            title: 'Configuration',
            description: 'Optimize cache settings for your workload',
            details: ['Right-size your cache', 'Choose appropriate eviction', 'Monitor performance']
        },
        {
            icon: '⚡',
            title: 'Performance',
            description: 'Maximize cache effectiveness',
            details: ['Batch operations', 'Async loading', 'Preloading strategies']
        }
    ];

    return (
        <div className="getting-started">
            {/* Header */}
            <Section background="gradient" padding="lg" centered>
                <div className="header-content">
                    <h1 className="page-title">Getting Started with JCacheX</h1>
                    <p className="page-subtitle">
                        Learn how to integrate JCacheX into your Java or Kotlin application in just a few minutes
                    </p>
                </div>
            </Section>

            {/* Installation */}
            <Section padding="lg">
                <InstallationGuide
                    tabs={INSTALLATION_TABS}
                    title="1. Installation"
                    description="Add JCacheX to your project using your preferred build tool:"
                />
            </Section>

            {/* Basic Usage */}
            <Section
                background="light"
                padding="lg"
                title="2. Basic Usage"
                subtitle="Create and use caches with simple, intuitive API"
                centered
            >
                <div className="code-section">
                    <CodeTabs tabs={basicUsageTabs} />
                </div>
            </Section>

            {/* Asynchronous Operations */}
            <Section
                padding="lg"
                title="3. Asynchronous Operations"
                subtitle="Leverage async capabilities for better performance"
                centered
            >
                <div className="code-section">
                    <CodeTabs tabs={asyncTabs} />
                </div>
            </Section>

            {/* Spring Boot Configuration */}
            <Section
                background="light"
                padding="lg"
                title="4. Spring Boot Configuration"
                subtitle="Configure JCacheX in your Spring Boot application"
                centered
            >
                <div className="code-section">
                    <CodeTabs tabs={springConfigTabs} />
                </div>
            </Section>

            {/* Configuration Options */}
            <Section
                padding="lg"
                title="Configuration Options"
                subtitle="Customize cache behavior to match your requirements"
                centered
            >
                <Grid columns={2} gap="lg">
                    {configurationOptions.map((option, index) => (
                        <FeatureCard
                            key={index}
                            icon={option.icon}
                            title={option.title}
                            description={option.description}
                            details={option.details}
                            variant="horizontal"
                        />
                    ))}
                </Grid>
            </Section>

            {/* Best Practices */}
            <Section
                background="light"
                padding="lg"
                title="Best Practices"
                subtitle="Tips for getting the most out of JCacheX"
                centered
            >
                <Grid columns={3} gap="lg">
                    {bestPractices.map((practice, index) => (
                        <FeatureCard
                            key={index}
                            icon={practice.icon}
                            title={practice.title}
                            description={practice.description}
                            details={practice.details}
                            variant="compact"
                        />
                    ))}
                </Grid>
            </Section>

            {/* Next Steps */}
            <Section background="gradient" padding="lg" centered>
                <div className="next-steps">
                    <h2 className="next-steps-title">Next Steps</h2>
                    <p className="next-steps-subtitle">
                        Ready to explore more advanced features?
                    </p>
                    <div className="next-steps-buttons">
                        <a href="/examples" className="btn btn-primary">
                            View Examples
                        </a>
                        <a href="/spring" className="btn btn-secondary">
                            Spring Boot Guide
                        </a>
                    </div>
                </div>
            </Section>
        </div>
    );
};

export default GettingStarted;
