pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "JCacheX"

include(
    "jcachex-core",
    "jcachex-kotlin",
    "jcachex-spring",
    "benchmarks",
    "example:java",
    "example:kotlin",
    "example:springboot",
    "example:distributed:staticnode",
    "example:distributed:kubernetes"
)

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
