import React from 'react';
import CodeTabs from './CodeTabs';
import './Home.css';

const Home = () => {
    const heroCodeTabs = [
        {
            id: 'java',
            label: 'Java',
            language: 'java',
            code: `// Simple and powerful
Cache<String, User> userCache = CacheFactory.newBuilder()
    .maxSize(1000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .evictionStrategy(EvictionStrategy.LRU)
    .build();

// Async operations
CompletableFuture<User> user = userCache.getAsync("user123");

// Event listeners
userCache.addListener(event -> {
    System.out.println("Cache event: " + event.getType());
});`
        },
        {
            id: 'kotlin',
            label: 'Kotlin',
            language: 'kotlin',
            code: `// Kotlin extensions
val userCache = cache<String, User> {
    maxSize = 1000
    expireAfterWrite = 30.minutes
    evictionStrategy = EvictionStrategy.LRU
}

// Coroutine support
val user = userCache.getAsync("user123").await()

// DSL configuration
userCache.configure {
    onEviction { key, value, reason ->
        println("Evicted $key: $reason")
    }
}`
        }
    ];

    const version = process.env.REACT_APP_VERSION || '1.0.0';

    const installTabs = [
        {
            id: 'maven',
            label: 'Maven',
            language: 'xml',
            code: `<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>${version}</version>
</dependency>`
        },
        {
            id: 'gradle',
            label: 'Gradle',
            language: 'gradle',
            code: `implementation 'io.github.dhruv1110:jcachex-core:${version}'`
        },
        {
            id: 'sbt',
            label: 'SBT',
            language: 'scala',
            code: `libraryDependencies += "io.github.dhruv1110" % "jcachex-core" % "${version}"`
        }
    ];

    const features = [
        {
            icon: '⚡',
            title: 'High Performance',
            description: 'Optimized for speed with minimal memory overhead and efficient algorithms.'
        },
        {
            icon: '🔧',
            title: 'Simple API',
            description: 'Clean, intuitive API that\'s easy to learn and use in your applications.'
        },
        {
            icon: '🔄',
            title: 'Async Support',
            description: 'Built-in asynchronous operations with CompletableFuture and Kotlin coroutines.'
        },
        {
            icon: '🍃',
            title: 'Spring Integration',
            description: 'Seamless integration with Spring Boot and Spring Cache abstraction.'
        },
        {
            icon: '🌐',
            title: 'Distributed Caching',
            description: 'Scale across multiple nodes with built-in distributed caching support.'
        },
        {
            icon: '📊',
            title: 'Monitoring',
            description: 'Built-in metrics and monitoring capabilities for production environments.'
        }
    ];

    return (
        <div className="home">
            <main>
                <section id="home" className="hero">
                    <div className="container">
                        <div className="hero-content">
                            <h1>High-Performance Java Caching Library</h1>
                            <p>JCacheX provides lightning-fast, feature-rich caching with async support, Spring integration, distributed caching, and advanced eviction strategies.</p>
                            <div className="hero-buttons">
                                <a href="#quick-start" className="btn btn-primary">Get Started</a>
                                <a href="https://github.com/dhruv1110/JCacheX" className="btn btn-secondary" target="_blank" rel="noopener noreferrer">View on GitHub</a>
                            </div>
                        </div>
                        <div className="hero-code">
                            <CodeTabs tabs={heroCodeTabs} />
                        </div>
                    </div>
                </section>

                <section id="features" className="features">
                    <div className="container">
                        <div className="section-header">
                            <h2 className="section-title">Key Features</h2>
                        </div>
                        <div className="features-grid">
                            {features.map((feature, index) => (
                                <div key={index} className="feature-card">
                                    <div className="feature-icon">{feature.icon}</div>
                                    <h3>{feature.title}</h3>
                                    <p>{feature.description}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                </section>

                <section id="quick-start" className="quick-start">
                    <div className="container">
                        <div className="section-header">
                            <h2 className="section-title">Quick Start</h2>
                        </div>
                        <div className="quick-start-steps">
                            <div className="step">
                                <div className="step-number">1</div>
                                <div className="step-content">
                                    <h3>Add Dependency</h3>
                                    <p>Add JCacheX to your project. Visit <a href="https://central.sonatype.com/artifact/io.github.dhruv1110/jcachex-core" target="_blank" rel="noopener noreferrer">Maven Central</a> for the latest version.</p>
                                    <CodeTabs tabs={installTabs} className="light" />
                                </div>
                            </div>
                            <div className="step">
                                <div className="step-number">2</div>
                                <div className="step-content">
                                    <h3>Create Cache</h3>
                                    <p>Configure and create your cache instance.</p>
                                    <pre><code className="language-java">Cache&lt;String, String&gt; cache = CacheFactory.newBuilder()
                                        .maxSize(1000)
                                        .expireAfterWrite(Duration.ofMinutes(30))
                                        .build();</code></pre>
                                </div>
                            </div>
                            <div className="step">
                                <div className="step-number">3</div>
                                <div className="step-content">
                                    <h3>Use Cache</h3>
                                    <p>Store and retrieve data efficiently.</p>
                                    <pre><code className="language-java">cache.put("key", "value");
                                        String value = cache.get("key");
                                        System.out.println(value); // Output: value</code></pre>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <section className="stats">
                    <div className="container">
                        <div className="stats-grid">
                            <div className="stat-card">
                                <div className="stat-number">10x</div>
                                <div className="stat-label">Faster than traditional caching</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-number">6</div>
                                <div className="stat-label">Eviction strategies built-in</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-number">100%</div>
                                <div className="stat-label">Thread-safe operations</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-number">3</div>
                                <div className="stat-label">Integration modules</div>
                            </div>
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
};

export default Home;
