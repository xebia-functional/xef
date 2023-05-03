dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "langchain4k"
include("nodejs-commandexecutor")
include("example")


include("langchain4k-scala")
project(":langchain4k-scala").projectDir = file("scala")