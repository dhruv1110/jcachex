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
