plugins {
    java
    id("me.champeau.jmh") version "0.7.2"
}

repositories {
    mavenCentral()
}

dependencies {
    // JCacheX
    implementation(project(":jcachex-core"))

    // JMH
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    // Competing cache libraries
    jmh("com.github.ben-manes.caffeine:caffeine:3.1.8")
    jmh("org.ehcache:ehcache:3.10.8")
    jmh("org.cache2k:cache2k-core:2.6.1.Final")
    jmh("com.google.guava:guava:32.1.3-jre")

    // JCache API and implementations (avoid multiple providers)
    jmh("javax.cache:cache-api:1.1.1")
    // Use only EHCache JCache implementation to avoid provider conflicts
    jmh("org.ehcache:ehcache:3.10.8")

    // Utilities
    jmh("org.apache.commons:commons-lang3:3.14.0")
    jmh("com.fasterxml.jackson.core:jackson-databind:2.16.1")
}

jmh {
    jmhVersion = "1.37"
    includeTests = false
    warmupIterations = 3
    iterations = 5
    fork = 2
    threads = 1
    timeUnit = "us" // microseconds
    benchmarkMode =
        listOf("avgt", "thrpt") // Average time and throughput
    jvmArgs =
        listOf(
            "-Xms16g",
            "-Xmx16g",
            "-XX:MaxDirectMemorySize=64g",
            "-XX:+UseParallelGC",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+UseCompressedOops",
            "-XX:+DisableExplicitGC",
            "-XX:+AlwaysPreTouch"
        )
}

tasks.named<JavaCompile>("compileJmhJava") {
    options.compilerArgs.addAll(
        listOf(
            "-parameters",
            "-Xlint:unchecked",
        ),
    )
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
