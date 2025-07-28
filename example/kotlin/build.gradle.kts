plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("application")
}

group = "io.github.dhruv1110.jcachex.example"
// version inherited from root project

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jcachex-core"))
    implementation(project(":jcachex-kotlin"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.slf4j:slf4j-api:2.0.9")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.11")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

application {
    mainClass.set("io.github.dhruv1110.jcachex.example.kotlin.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
