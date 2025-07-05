rootProject.name = "JCacheX"

include(
    "jcachex-core",
    "jcachex-kotlin",
    "jcachex-spring",
    "example:java",
    "example:kotlin",
    "example:springboot"
)

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
