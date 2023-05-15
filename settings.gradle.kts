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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "langchain4k"
include("langchain4k-filesystem")
include("example")


include("langchain4k-scala")
project(":langchain4k-scala").projectDir = file("scala")

include("langchain4k-core")
project(":langchain4k-core").projectDir = file("core")

include("langchain4k-lucene")
project(":langchain4k-lucene").projectDir = file("integrations/lucene")
