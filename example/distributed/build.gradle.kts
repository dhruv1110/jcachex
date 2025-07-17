plugins {
    id("java")
    id("application")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    // JCacheX core dependencies
    implementation(project(":jcachex-core"))
    implementation(project(":jcachex-spring"))

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter:2.7.14")
    implementation("org.springframework.boot:spring-boot-starter-web:2.7.14")
    implementation("org.springframework.boot:spring-boot-autoconfigure:2.7.14")

    // HTTP client for Kubernetes API
    implementation("org.apache.httpcomponents:httpclient:4.5.14")

    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("org.slf4j:slf4j-api:2.0.7")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
}

application {
    mainClass.set("io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample")
}

tasks {
    test {
        useJUnitPlatform()
    }

        jar {
        archiveBaseName.set("jcachex-distributed-example")
        archiveVersion.set("1.0.0")

        manifest {
            attributes(
                "Main-Class" to "io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample"
            )
        }

        // Include all dependencies in the JAR
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    // Create a fat JAR with all dependencies
    register<Jar>("fatJar") {
        archiveBaseName.set("jcachex-distributed-example")
        archiveVersion.set("1.0.0")
        archiveClassifier.set("all")

        manifest {
            attributes(
                "Main-Class" to "io.github.dhruv1110.jcachex.example.distributed.KubernetesDiscoveryExample"
            )
        }

        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    register<Copy>("copyDependencies") {
        from(configurations.runtimeClasspath)
        into(layout.buildDirectory.dir("dependencies"))
    }

    build {
        dependsOn(jar)
    }
}

repositories {
    mavenCentral()
}
