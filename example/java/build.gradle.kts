plugins {
    id("java")
    id("application")
}

group = "io.github.dhruv.jcachex.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.dhruv:jcachex-core:1.0.0")
    implementation("io.github.dhruv:jcachex-java:1.0.0")

    implementation("org.slf4j:slf4j-api:2.0.9")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.11")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

application {
    mainClass.set("io.github.dhruv.jcachex.example.java.Main")
}

tasks.test {
    useJUnitPlatform()
}
