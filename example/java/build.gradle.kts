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
