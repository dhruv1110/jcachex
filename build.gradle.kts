plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.5" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
    id("jacoco")
}

group = "io.github.dhruv"
version = "0.1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.8".toBigDecimal()
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
}

project(":jcachex-core") {

    dependencies {

        // Testing
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testImplementation("org.mockito:mockito-core:5.8.0")
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
        testImplementation("org.mockito:mockito-core:5.8.0")
        testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
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
        testImplementation("org.mockito:mockito-core:5.8.0")
        testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    }
}
