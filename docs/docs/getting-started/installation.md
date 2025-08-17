---
id: installation
title: Installation
sidebar_label: Installation
description: Install JCacheX in your Java project
---

# Installation Guide

This guide covers installing JCacheX in various Java project types and build systems.

## Prerequisites

Before installing JCacheX, ensure you have:

- **Java 11 or higher** (JCacheX requires Java 11+)
- **Maven 3.6+** or **Gradle 7.0+**
- **Your preferred IDE** (IntelliJ IDEA, Eclipse, VS Code, etc.)

## Maven Installation

### Basic Installation

Add the core dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-core</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Complete Maven Configuration

For a complete setup with all modules:

```xml
<dependencies>
    <!-- Core JCacheX functionality -->
    <dependency>
        <groupId>io.github.dhruv1110</groupId>
        <artifactId>jcachex-core</artifactId>
        <version>2.0.1</version>
    </dependency>

    <!-- Spring Boot integration (optional) -->
    <dependency>
        <groupId>io.github.dhruv1110</groupId>
        <artifactId>jcachex-spring</artifactId>
        <version>2.0.1</version>
    </dependency>

    <!-- Kotlin DSL support (optional) -->
    <dependency>
        <groupId>io.github.dhruv1110</groupId>
        <artifactId>jcachex-kotlin</artifactId>
        <version>2.0.1</version>
    </dependency>
</dependencies>
```

### Maven with BOM (Bill of Materials)

For better dependency management, you can use a BOM:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.dhruv1110</groupId>
            <artifactId>jcachex-bom</artifactId>
            <version>2.0.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.dhruv1110</groupId>
        <artifactId>jcachex-core</artifactId>
    </dependency>
</dependencies>
```

## Gradle Installation

### Basic Installation (Groovy)

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.dhruv1110:jcachex-core:2.0.1'
}
```

### Complete Gradle Configuration (Groovy)

```gradle
dependencies {
    // Core JCacheX functionality
    implementation 'io.github.dhruv1110:jcachex-core:2.0.1'

    // Spring Boot integration (optional)
    implementation 'io.github.dhruv1110:jcachex-spring:2.0.1'

    // Kotlin DSL support (optional)
    implementation 'io.github.dhruv1110:jcachex-kotlin:2.0.1'
}
```

### Gradle with Kotlin DSL

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    // Core JCacheX functionality
    implementation("io.github.dhruv1110:jcachex-core:2.0.1")

    // Spring Boot integration (optional)
    implementation("io.github.dhruv1110:jcachex-spring:2.0.1")

    // Kotlin DSL support (optional)
    implementation("io.github.dhruv1110:jcachex-kotlin:2.0.1")
}
```

### Gradle with Version Catalog

Create `gradle/libs.versions.toml`:

```toml
[versions]
jcachex = "2.0.1"

[libraries]
jcachex-core = { group = "io.github.dhruv1110", name = "jcachex-core", version.ref = "jcachex" }
jcachex-spring = { group = "io.github.dhruv1110", name = "jcachex-spring", version.ref = "jcachex" }
jcachex-kotlin = { group = "io.github.dhruv1110", name = "jcachex-kotlin", version.ref = "jcachex" }
```

Then in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.jcachex.core)
    implementation(libs.jcachex.spring)
    implementation(libs.jcachex.kotlin)
}
```

## Spring Boot Integration

### Spring Boot Starter

If you're using Spring Boot, add the starter dependency:

```xml
<dependency>
    <groupId>io.github.dhruv1110</groupId>
    <artifactId>jcachex-spring-boot-starter</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Spring Boot Auto-configuration

The starter provides auto-configuration. Just add to your `application.yml`:

```yaml
jcachex:
  default:
    maximumSize: 1000
    expireAfterSeconds: 1800
    enableStatistics: true

  caches:
    users:
      profile: READ_HEAVY
      maximumSize: 5000
    sessions:
      profile: SESSION_CACHE
      maximumSize: 2000
```

## Kotlin DSL Support

### Kotlin Project Setup

For Kotlin projects, add the Kotlin DSL module:

```kotlin
dependencies {
    implementation("io.github.dhruv1110:jcachex-kotlin:2.0.1")
}
```

### Kotlin Extensions

The Kotlin module provides extension functions and DSL builders:

```kotlin
import io.github.dhruv1110.jcachex.kotlin.*

val cache = createReadHeavyCache<String, User> {
    name("users")
    maximumSize(1000)
}
```

## IDE Setup

### IntelliJ IDEA

1. **Import Project**: Open your project in IntelliJ IDEA
2. **Maven/Gradle Integration**: Ensure the build tool integration is working
3. **Auto-import**: Enable auto-import for Maven/Gradle dependencies
4. **Code Completion**: JCacheX provides full IntelliSense support

### Eclipse

1. **Import Project**: Import as Maven or Gradle project
2. **Maven Integration**: Install m2e plugin if not present
3. **Gradle Integration**: Install Gradle plugin if using Gradle
4. **Auto-build**: Enable auto-build for dependency resolution

### VS Code

1. **Java Extension Pack**: Install the Java Extension Pack
2. **Maven/Gradle Support**: Install Maven or Gradle extensions
3. **Java Language Support**: Ensure Java language support is enabled

## Verification

### Test Installation

Create a simple test class to verify the installation:

```java
import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.JCacheXBuilder;

public class InstallationTest {
    public static void main(String[] args) {
        try {
            Cache<String, String> cache = JCacheXBuilder.create()
                .name("test")
                .maximumSize(10L)
                .build();

            cache.put("test", "success");
            String result = cache.get("test");

            if ("success".equals(result)) {
                System.out.println("✅ JCacheX installation successful!");
            } else {
                System.out.println("❌ JCacheX installation failed!");
            }
        } catch (Exception e) {
            System.err.println("❌ JCacheX installation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

### Run the Test

```bash
# Maven
mvn compile exec:java -Dexec.mainClass="InstallationTest"

# Gradle
gradle run --args="InstallationTest"
```

## Troubleshooting

### Common Issues

**Dependency Resolution Problems**

```bash
# Maven - Clean and reinstall
mvn clean install -U

# Gradle - Clean and refresh
gradle clean build --refresh-dependencies
```

**Version Conflicts**

Check for conflicting dependencies:

```bash
# Maven
mvn dependency:tree

# Gradle
gradle dependencies
```

**Java Version Issues**

Ensure you're using Java 11+:

```bash
java -version
javac -version
```

### Getting Help

If you encounter issues:

1. **Check the logs** for detailed error messages
2. **Verify Java version** meets requirements
3. **Review dependency tree** for conflicts
4. **Check GitHub issues** for known problems
5. **Create a new issue** if the problem persists

## Next Steps

After successful installation:

1. **[Quick Start](/docs/getting-started/quick-start)** - Create your first cache
2. **[Examples](/docs/examples)** - Explore usage patterns
3. **[Spring Boot Guide](/docs/spring-boot)** - Integrate with Spring Boot
4. **[API Reference](/docs/api-reference)** - Learn the complete API

---

**Installation complete?** Great! Now [create your first cache](/docs/getting-started/quick-start) and start caching!
