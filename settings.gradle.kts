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
include("tokenizer")

include("langchain4k-scala")
project(":langchain4k-scala").projectDir = file("scala")

include("langchain4k-kotlin")
project(":langchain4k-kotlin").projectDir = file("kotlin")
