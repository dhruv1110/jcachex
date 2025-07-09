import React from 'react';
import './GettingStarted.css';

const GettingStarted = () => {
    const version = process.env.REACT_APP_VERSION || '1.0.0';

    return (
        <div className="getting-started">
            <div className="container">
                <header className="docs-header">
                    <nav className="breadcrumb">
                        <a href="/">Home</a>
                        <span>/</span>
                        <span>Getting Started</span>
                    </nav>
                    <h1>Getting Started with JCacheX</h1>
                    <p className="lead">Learn how to integrate JCacheX into your Java or Kotlin application and start caching data efficiently.</p>
                </header>

                <main className="docs-content">
                    <section className="docs-section">
                        <h2>Installation</h2>
                        <p>JCacheX is available on Maven Central. Visit <a href="https://central.sonatype.com/artifact/io.github.dhruv1110/jcachex-core" target="_blank" rel="noopener noreferrer">Maven Central</a> to find the latest version.</p>

                        <h3>Maven</h3>
                        <pre><code className="language-xml">{`<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>${version}</version>
</dependency>`}</code></pre>

                        <h3>Gradle</h3>
                        <pre><code className="language-gradle">implementation 'io.github.dhruv1110:jcachex-core:${version}'</code></pre>
                    </section>

                    <section className="docs-section">
                        <h2>Basic Usage</h2>
                        <p>Get started with JCacheX using these simple examples:</p>

                        <pre><code className="language-java">{`import io.github.dhruv1110.jcachex.*;

// Create a simple cache
CacheConfig<String, String> config = CacheConfig.<String, String>builder()
    .maximumSize(1000L)
    .build();

Cache<String, String> cache = new DefaultCache<>(config);

// Basic operations
cache.put("key1", "value1");
String value = cache.get("key1");
System.out.println("Value: " + value); // Output: Value: value1`}</code></pre>
                    </section>

                    <section className="docs-section">
                        <h2>Next Steps</h2>
                        <div className="callout callout-info">
                            <h4>📚 More Documentation</h4>
                            <p>This is a placeholder for the full documentation. Check out our <a href="/examples">examples page</a> for more code samples.</p>
                        </div>
                    </section>
                </main>
            </div>
        </div>
    );
};

export default GettingStarted;
