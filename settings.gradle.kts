rootProject.name = "JCacheX"

include(
    "jcachex-core",
    "jcachex-kotlin",
    "jcachex-spring"
)

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
