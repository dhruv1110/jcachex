plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.5" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
    id("jacoco")
    id("maven-publish")
    id("signing")

    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.jetbrains.dokka") version "1.9.10" apply false
}

group = "io.github.dpflux"
version = "0.1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

// Nexus publishing configuration
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME"))
            password.set(project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD"))
        }
    }
}



subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.dokka")

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        withSourcesJar()
        withJavadocJar()
    }

    // Publishing configuration
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                pom {
                    name.set("JCacheX - ${project.name}")
                    description.set("High-performance caching library for Java and Kotlin applications")
                    url.set("https://github.com/dpflux/JCacheX")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("dpflux")
                            name.set("DPFlux")
                            email.set("dpflux@users.noreply.github.com") // Update with your email
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/dpflux/JCacheX.git")
                        developerConnection.set("scm:git:ssh://github.com/dpflux/JCacheX.git")
                        url.set("https://github.com/dpflux/JCacheX")
                    }

                    issueManagement {
                        system.set("GitHub")
                        url.set("https://github.com/dpflux/JCacheX/issues")
                    }
                }
            }
        }

                // Repository configuration is handled by nexus-publish plugin
    }

    // Signing configuration
    signing {
        val signingKey = project.findProperty("signing.key") as String? ?: System.getenv("GPG_PRIVATE_KEY")
        val signingPassword = project.findProperty("signing.password") as String? ?: System.getenv("GPG_PASSWORD")

        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications["maven"])
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
        finalizedBy(tasks.jacocoTestCoverageVerification)
    }

    tasks.jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.6".toBigDecimal()
                }
            }
        }
    }

    tasks.check {
        dependsOn(tasks.jacocoTestCoverageVerification)
    }

    afterEvaluate {
        if (plugins.hasPlugin("io.gitlab.arturbosch.detekt")) {
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

    // Documentation tasks
    tasks.withType<Javadoc> {
        options {
            (this as StandardJavadocDocletOptions).apply {
                addBooleanOption("html5", true)
                addStringOption("Xdoclint:none", "-quiet")
                links("https://docs.oracle.com/javase/8/docs/api/")
                windowTitle = "JCacheX ${project.name} API"
                docTitle = "JCacheX ${project.name} API"
                header = "<b>JCacheX ${project.name}</b>"
                bottom = "Copyright © 2024 DPFlux. All rights reserved."
            }
        }
        isFailOnError = false
    }

    // Simple Dokka configuration for Kotlin projects
    afterEvaluate {
        if (plugins.hasPlugin("org.jetbrains.kotlin.jvm") && plugins.hasPlugin("org.jetbrains.dokka")) {
            tasks.findByName("dokkaHtml")?.apply {
                // Basic dokka configuration - can be expanded later
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
    description = "Generates documentation for all modules"
    dependsOn(subprojects.map { it.tasks.named("javadoc") })

    if (subprojects.any { it.plugins.hasPlugin("org.jetbrains.kotlin.jvm") }) {
        dependsOn(subprojects.map {
            try { it.tasks.named("dokkaHtml") } catch (e: Exception) { null }
        }.filterNotNull())
    }
}

project(":jcachex-core") {

    dependencies {

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
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        implementation(project(":jcachex-core"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.springframework.boot:spring-boot-starter:2.7.18")
        implementation("org.springframework.boot:spring-boot-configuration-processor:2.7.18")

        // Testing
        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.18")
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
