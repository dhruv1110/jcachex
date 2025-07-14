import org.jreleaser.model.Active

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0" apply false
    id("jacoco")
    id("maven-publish")
    id("signing")

    id("org.jreleaser") version "1.18.0"
    id("org.jetbrains.dokka") version "1.9.10" apply false
}

group = "io.github.dhruv1110"
version = "0.1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

// Root-level tasks that depend on all subproject tasks
tasks.register("testAll") {
    group = "verification"
    description = "Runs tests for all subprojects"
    dependsOn(subprojects.map { "${it.path}:test" })
}

tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt for core modules (excludes examples and benchmarks)"
    dependsOn(subprojects.filter {
        !it.path.startsWith(":example") && it.path != ":benchmarks"
    }.map { "${it.path}:detekt" })
}

// Make root-level test and check tasks depend on all subprojects
tasks.named("test") {
    dependsOn("testAll")
}

tasks.register("detekt") {
    group = "verification"
    description = "Runs detekt for all subprojects"
    dependsOn("detektAll")
}

tasks.named("check") {
    dependsOn("testAll", "detektAll")
}

// JReleaser configuration - use external jreleaser.yml file
jreleaser {
    configFile = file("jreleaser.yml")
}



subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
    apply(plugin = "jacoco")
    // Only apply ktlint and detekt to core modules, not examples or benchmarks
    if (!project.path.startsWith(":example") && project.path != ":benchmarks") {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
        apply(plugin = "io.gitlab.arturbosch.detekt")

        configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            disabledRules.set(setOf("standard:no-consecutive-comments"))
        }
    }
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    // Only apply Dokka plugin to core modules, not examples or benchmarks
    if (!project.path.startsWith(":example") && project.path != ":benchmarks") {
        apply(plugin = "org.jetbrains.dokka")
    }

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // Use JDK 11+ for javadoc generation (HTML5 support)
        // while maintaining JDK 8 compatibility for runtime
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
        withSourcesJar()
        // Only generate javadoc JARs for core modules, not examples or benchmarks
        if (!project.path.startsWith(":example") && project.path != ":benchmarks") {
            withJavadocJar()
        }
    }

    // Publishing configuration - only for core modules, not examples or benchmarks
    if (!project.path.startsWith(":example") && project.path != ":benchmarks") {
        publishing {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])

                    pom {
                        name.set("JCacheX - ${project.name}")
                        description.set("High-performance caching library for Java and Kotlin applications")
                        url.set("https://github.com/dhruv1110/JCacheX")

                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }

                        developers {
                            developer {
                                id.set("dhruv1110")
                                name.set("dhruv1110")
                                email.set("dhruv1110@users.noreply.github.com") // Update with your email
                            }
                        }

                        scm {
                            connection.set("scm:git:git://github.com/dhruv1110/JCacheX.git")
                            developerConnection.set("scm:git:ssh://github.com/dhruv1110/JCacheX.git")
                            url.set("https://github.com/dhruv1110/JCacheX")
                        }

                        issueManagement {
                            system.set("GitHub")
                            url.set("https://github.com/dhruv1110/JCacheX/issues")
                        }
                    }
                }
            }

            repositories {
                maven {
                    url = rootProject.layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
                }
            }
        }
    }

    // Signing configuration - JReleaser will handle all signing
    signing {
        // Disable Gradle signing - JReleaser will handle all signing
        isRequired = false
        // Do not sign publications - JReleaser will sign them
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        finalizedBy(tasks.jacocoTestReport)

        // Ensure JaCoCo uses the same JDK as the test execution
        jvmArgs("-XX:+EnableDynamicAgentLoading")
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
        // Exclude example packages from coverage analysis
        executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec"))
        classDirectories.setFrom(files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/io/github/dhruv1110/jcachex/example/**",
                    "**/example/**",
                    "**/io/github/dhruv1110/jcachex/benchmarks/**",
                )
            }
        }))
        finalizedBy(tasks.jacocoTestCoverageVerification)
    }

    tasks.jacocoTestCoverageVerification {
        dependsOn(tasks.test)
        // Exclude example packages from coverage requirements
        executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec"))
        classDirectories.setFrom(files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/io/github/dhruv1110/jcachex/example/**",
                    "**/example/**",
                    "**/io/github/dhruv1110/jcachex/benchmarks/**",
                )
            }
        }))
        violationRules {
            rule {
                limit {
                    minimum = "0.0".toBigDecimal()
                }
            }
        }
    }

    tasks.check {
        dependsOn(tasks.jacocoTestCoverageVerification)
    }

    afterEvaluate {
        // Only configure detekt for core modules, not examples or benchmarks
        if (plugins.hasPlugin("io.gitlab.arturbosch.detekt") &&
            !project.path.startsWith(":example") && project.path != ":benchmarks") {
            configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
                buildUponDefaultConfig = true
                config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
                baseline = file("${rootProject.projectDir}/config/detekt/baseline.xml")
            }

            tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
                jvmTarget = "1.8"
                reports {
                    html.required.set(true)
                    xml.required.set(true)
                    txt.required.set(false)
                    sarif.required.set(true)
                    md.required.set(true)
                }
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    // Documentation tasks - only for core modules, not examples or benchmarks
    if (!project.path.startsWith(":example") && project.path != ":benchmarks") {
        tasks.withType<Javadoc> {
            // Force javadoc to use JDK 11+ for HTML5 support
            options.jFlags = listOf("-Djava.awt.headless=true")

            options {
                (this as StandardJavadocDocletOptions).apply {
                    // Always use HTML5 since we're using JDK 11+ for javadoc
                    addBooleanOption("html5", true)
                    addStringOption("Xdoclint:none", "-quiet")
                    // Can now safely use external links since we have proper JDK
                    links("https://docs.oracle.com/en/java/javase/11/docs/api/")
                    windowTitle = "JCacheX ${project.name} API"
                    docTitle = "JCacheX ${project.name} API"
                    header = "<b>JCacheX ${project.name}</b>"
                    bottom = "Copyright © 2024 dhruv1110. All rights reserved."
                }
            }
            isFailOnError = false
        }
    }

    // Dokka configuration for Kotlin projects - only for core modules, not examples or benchmarks
    afterEvaluate {
        if (project.path.startsWith(":example") || project.path == ":benchmarks") {
            // Disable Dokka tasks for example modules and benchmarks
            tasks.matching { it.name.contains("dokka", ignoreCase = true) }.configureEach {
                enabled = false
            }
            // Explicitly disable common Dokka task names
            listOf("dokkaJavadoc", "dokkaHtml", "dokkaGfm").forEach { taskName ->
                tasks.findByName(taskName)?.enabled = false
            }
        } else {
            // Configure Dokka for core modules only
            if (plugins.hasPlugin("org.jetbrains.kotlin.jvm") && plugins.hasPlugin("org.jetbrains.dokka")) {
                // Configure Dokka javadoc generation
                tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
                    dokkaSourceSets {
                        configureEach {
                            jdkVersion.set(8)
                            // Ensure proper module name
                            moduleName.set("JCacheX ${project.name}")
                            // Note: Removed sourceLink due to Gradle URL serialization warning
                            // This is not critical for javadoc.io functionality
                        }
                    }
                }

                // Ensure javadoc jar properly uses Dokka output for Kotlin modules
                tasks.withType<Jar>().matching { it.name == "javadocJar" }.configureEach {
                    archiveClassifier.set("javadoc")
                    dependsOn("dokkaJavadoc")
                    // Add Dokka javadoc output
                    from(tasks.named("dokkaJavadoc")) {
                        exclude("**/*.map")  // Exclude source maps if any
                    }
                    // Ensure we have content
                    doFirst {
                        val dokkaOutput = tasks.named("dokkaJavadoc").get().outputs.files
                        if (dokkaOutput.isEmpty) {
                            throw GradleException("Dokka javadoc output is empty for project ${project.name}")
                        }
                    }
                }
            } else {
                // For pure Java projects, ensure javadoc jar uses standard javadoc output
                tasks.withType<Jar>().matching { it.name == "javadocJar" }.configureEach {
                    archiveClassifier.set("javadoc")
                    dependsOn("javadoc")
                    from(tasks.javadoc)
                }
            }
        }
    }

    // Documentation coverage task
    tasks.register("documentationCoverage") {
        group = "documentation"
        description = "Generates documentation coverage report"

        doLast {
            val sourceFiles = mutableListOf<File>()
            val documentedFiles = mutableListOf<File>()

            // Check Java files
            fileTree("src/main/java").matching {
                include("**/*.java")
            }.forEach { file ->
                sourceFiles.add(file)
                val content = file.readText()
                if (content.contains("/**") || content.contains("* ")) {
                    documentedFiles.add(file)
                }
            }

            // Check Kotlin files
            if (file("src/main/kotlin").exists()) {
                fileTree("src/main/kotlin").matching {
                    include("**/*.kt")
                }.forEach { file ->
                    sourceFiles.add(file)
                    val content = file.readText()
                    if (content.contains("/**") || content.contains("* ")) {
                        documentedFiles.add(file)
                    }
                }
            }

            val coverage = if (sourceFiles.isNotEmpty()) {
                (documentedFiles.size.toDouble() / sourceFiles.size.toDouble()) * 100
            } else {
                100.0
            }

            println("Documentation Coverage for ${project.name}:")
            println("  Total files: ${sourceFiles.size}")
            println("  Documented files: ${documentedFiles.size}")
            println("  Coverage: %.2f%%".format(coverage))

            // Write coverage report
            val reportDir = file("build/reports/documentation")
            reportDir.mkdirs()
            val reportFile = reportDir.resolve("coverage.txt")
            val currentTime = System.currentTimeMillis().toString()
            reportFile.writeText("""
                Documentation Coverage Report for ${project.name}
                Generated: $currentTime

                Summary:
                  Total source files: ${sourceFiles.size}
                  Documented files: ${documentedFiles.size}
                  Coverage: %.2f%%

                Undocumented files:
                ${sourceFiles.subtract(documentedFiles.toSet()).joinToString("\n") { "  - ${it.relativeTo(projectDir)}" }}
            """.trimIndent().format(coverage))

            println("Documentation coverage report written to: ${reportFile.relativeTo(rootDir)}")
        }
    }
}

// Root-level documentation tasks
tasks.register("allDocumentationCoverage") {
    group = "documentation"
    description = "Generates documentation coverage report for all modules"
    dependsOn(subprojects.map { it.tasks.named("documentationCoverage") })

    doLast {
        println("\n=== Overall Documentation Coverage Summary ===")
        subprojects.forEach { subproject ->
            val reportFile = subproject.file("build/reports/documentation/coverage.txt")
            if (reportFile.exists()) {
                val content = reportFile.readText()
                val coverageMatch = Regex("Coverage: ([0-9.]+)%").find(content)
                val coverage = coverageMatch?.groupValues?.get(1) ?: "N/A"
                println("${subproject.name}: $coverage%")
            }
        }
        println("==============================================\n")
    }
}

tasks.register("generateAllDocs") {
    group = "documentation"
    description = "Generates documentation for core modules only (excludes examples and benchmarks)"

    // Only include core modules (jcachex-core, jcachex-kotlin, jcachex-spring)
    val coreModules = subprojects.filter { !it.name.startsWith("example") && it.name != "benchmarks" }

    dependsOn(coreModules.map {
        try { it.tasks.named("javadoc") } catch (e: Exception) { null }
    }.filterNotNull())

    if (coreModules.any { it.plugins.hasPlugin("org.jetbrains.kotlin.jvm") }) {
        dependsOn(coreModules.map {
            try { it.tasks.named("dokkaHtml") } catch (e: Exception) { null }
        }.filterNotNull())
    }
}

project(":jcachex-core") {

    dependencies {
        // Logging
        implementation("org.slf4j:slf4j-api:1.7.36")

        // Testing
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testImplementation("org.mockito:mockito-core:4.11.0") {
            // Force version to maintain Java 8 compatibility
            version {
                strictly("4.11.0")
            }
        }
    }

    // Force Mockito version for Java 8 compatibility
    configurations.all {
        resolutionStrategy {
            force("org.mockito:mockito-core:4.11.0")
            force("org.mockito:mockito-junit-jupiter:4.11.0")
        }
    }
}

project(":jcachex-kotlin") {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        implementation(project(":jcachex-core"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

        // Testing
        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testImplementation("org.mockito:mockito-core:4.11.0") {
            version {
                strictly("4.11.0")
            }
        }
        testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    }

    // Force Mockito version for Java 8 compatibility
    configurations.all {
        resolutionStrategy {
            force("org.mockito:mockito-core:4.11.0")
            force("org.mockito:mockito-junit-jupiter:4.11.0")
        }
    }
}

project(":jcachex-spring") {
    // Spring module is now pure Java - no Kotlin plugin needed

    dependencies {
        implementation(project(":jcachex-core"))
        // Keep Spring dependencies for compilation
        implementation("org.springframework.boot:spring-boot-starter:2.7.18")
        implementation("org.springframework.boot:spring-boot-configuration-processor:2.7.18")

        // Testing
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.18")
        testImplementation("org.mockito:mockito-core:4.11.0") {
            version {
                strictly("4.11.0")
            }
        }
    }

    // Force Mockito version for Java 8 compatibility
    configurations.all {
        resolutionStrategy {
            force("org.mockito:mockito-core:4.11.0")
            force("org.mockito:mockito-junit-jupiter:4.11.0")
        }
    }
}
