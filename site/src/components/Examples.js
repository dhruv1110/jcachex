import React from 'react';
import CodeTabs from './CodeTabs';
import './Examples.css';

const Examples = () => {
    const version = process.env.REACT_APP_VERSION || '1.0.0';

    const basicTabs = [
        {
            id: 'java',
            label: 'Java',
            language: 'java',
            code: `import io.github.dhruv1110.jcachex.*;

// Create a simple cache
CacheConfig<String, String> config = CacheConfig.<String, String>builder()
    .maximumSize(1000L)
    .build();

Cache<String, String> cache = new DefaultCache<>(config);

// Basic operations
cache.put("key1", "value1");
String value = cache.get("key1");
System.out.println("Value: " + value); // Output: Value: value1`
        },
        {
            id: 'kotlin',
            label: 'Kotlin',
            language: 'kotlin',
            code: `import io.github.dhruv1110.jcachex.kotlin.*

// Create a simple cache with Kotlin DSL
val cache = cache<String, String> {
    maxSize = 1000
}

// Basic operations
cache["key1"] = "value1"
val value = cache["key1"]
println("Value: $value") // Output: Value: value1`
        }
    ];

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

    return (
        <div className="examples">
            <div className="container">
                <header className="docs-header">
                    <nav className="breadcrumb">
                        <a href="/">Home</a>
                        <span>/</span>
                        <span>Examples</span>
                    </nav>
                    <h1>Examples</h1>
                    <p className="lead">Comprehensive examples and code samples to help you get the most out of JCacheX.</p>
                </header>

                <main className="docs-content">
                    <section className="docs-section">
                        <h2>Installation</h2>
                        <p>First, add JCacheX to your project. Visit <a href="https://central.sonatype.com/artifact/io.github.dhruv1110/jcachex-core" target="_blank" rel="noopener noreferrer">Maven Central</a> to find the latest version.</p>

                        <CodeTabs tabs={installTabs} className="light" />

                        <div className="callout callout-info">
                            <h4>📦 Latest Version</h4>
                            <p>Always check <a href="https://central.sonatype.com/artifact/io.github.dhruv1110/jcachex-core" target="_blank" rel="noopener noreferrer">Maven Central</a> for the latest version. You can also check the <a href="https://github.com/dhruv1110/JCacheX/releases" target="_blank" rel="noopener noreferrer">GitHub releases</a> page.</p>
                        </div>
                    </section>

                    <section className="docs-section">
                        <h2>Basic Examples</h2>
                        <p>Simple examples to get you started with JCacheX quickly.</p>

                        <h3>Simple Cache Creation</h3>
                        <CodeTabs tabs={basicTabs} className="light" />
                    </section>

                    <section className="docs-section">
                        <h2>Advanced Examples</h2>
                        <div className="callout callout-info">
                            <h4>🚧 Coming Soon</h4>
                            <p>Advanced examples including Spring Boot integration, custom eviction strategies, and distributed caching will be added soon. Check back later or contribute on <a href="https://github.com/dhruv1110/JCacheX" target="_blank" rel="noopener noreferrer">GitHub</a>.</p>
                        </div>
                    </section>
                </main>
            </div>
        </div>
    );
};

export default Examples;
