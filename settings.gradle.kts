rootProject.name = "JCacheX"

include(
    "jcachex-core",
    "jcachex-kotlin",
    "jcachex-spring",
    "benchmarks",
    "example:java",
    "example:kotlin",
    "example:springboot",
    "example:distributed:staticnode"
)

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
