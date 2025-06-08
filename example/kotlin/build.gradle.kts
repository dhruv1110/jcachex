plugins {
    kotlin("jvm")
    id("application")
}

group = "io.github.dhruv.jcachex.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.dhruv:jcachex-core:1.0.0")
    implementation("io.github.dhruv:jcachex-kotlin:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.slf4j:slf4j-api:2.0.9")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.11")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

application {
    mainClass.set("io.github.dhruv.jcachex.example.kotlin.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
