plugins {
    id("java")
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.4"
    id("application")
}

group = "io.github.dhruv1110.jcachex.example"
// version inherited from root project

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jcachex-core"))
    // Note: jcachex-spring has compilation issues, using core only
    // implementation(project(":jcachex-spring"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("io.github.dhruv1110.jcachex.example.distributed.kubernetes.Application")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
