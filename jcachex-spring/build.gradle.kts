/*
 * JCacheX Spring Integration Build Configuration
 *
 * This module provides Spring Framework integration for JCacheX with the following design principles:
 *
 * 1. MAXIMUM COMPATIBILITY:
 *    - Java 1.8+ compatibility (not requiring Java 11+)
 *    - Spring Framework 4.3+ compatibility (covers Spring Boot 1.5+)
 *    - Uses minimal dependencies to avoid version conflicts
 *
 * 2. MINIMAL DEPENDENCIES:
 *    - All Spring dependencies are compileOnly to avoid forcing versions
 *    - Optional features only activated when dependencies are present
 *    - No transitive dependency conflicts with user projects
 *
 * 3. GRACEFUL DEGRADATION:
 *    - Auto-configuration only when Spring Boot is present
 *    - Actuator integration only when actuator is present
 *    - Micrometer integration only when micrometer is present
 *
 * This ensures users can integrate JCacheX into any Spring project without dependency hell.
 */

plugins {
    java
    id("maven-publish")
    id("signing")
}

group = "io.github.dhruv1110"
// version inherited from root project

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    // Core JCacheX dependencies
    implementation(project(":jcachex-core"))

    // Minimal Spring Framework dependencies (compatible with Spring 4.3+)
    compileOnly("org.springframework:spring-context:6.2.9")
    compileOnly("org.springframework:spring-aop:6.2.9")
    compileOnly("org.aspectj:aspectjrt:1.9.21")
    compileOnly("org.springframework:spring-beans:6.2.9")
    compileOnly("org.springframework:spring-core:6.2.9")

    // Spring Boot auto-configuration (optional, when present)
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:1.5.22.RELEASE")
    compileOnly("org.springframework.boot:spring-boot:1.5.22.RELEASE")

    // Configuration properties support
    compileOnly("org.springframework.boot:spring-boot-configuration-processor:1.5.22.RELEASE")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:1.5.22.RELEASE")

    // Optional integrations (only when user has them)
    // Note: spring-cache was part of spring-context in 4.3.x
    compileOnly("javax.cache:cache-api:1.1.1")

    // Optional metrics integration (when user has micrometer)
    compileOnly("io.micrometer:micrometer-core:1.0.0")

    // Optional actuator integration (when user has actuator)
    compileOnly("org.springframework.boot:spring-boot-actuator:1.5.22.RELEASE")

    // Optional validation support
    compileOnly("javax.validation:validation-api:2.0.1.Final")

    // Optional JSON support for configuration
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.9.10")

    // Testing dependencies - use older versions compatible with Java 1.8
    testImplementation("org.springframework:spring-test:6.2.9")
    testImplementation("org.springframework.boot:spring-boot-test:1.5.22.RELEASE")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure:1.5.22.RELEASE")
    testImplementation("org.springframework:spring-aop:6.2.9")
    testImplementation("org.aspectj:aspectjrt:1.9.21")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.mockito:mockito-core:3.12.4")

    // Test runtime only
    testRuntimeOnly("com.h2database:h2:1.4.200")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// JAR configuration
tasks.jar {
    enabled = true
    archiveClassifier = ""
}

// Source and Javadoc JARs
java {
    withSourcesJar()
    withJavadocJar()
}

// Publishing configuration is handled by the root build.gradle.kts file
// which applies to all core modules (non-example projects)
