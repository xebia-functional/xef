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

rootProject.name = "xef"
include("xef-filesystem")
project(":xef-filesystem").projectDir = file("filesystem")

include("example")

include("xef-scala")
project(":xef-scala").projectDir = file("scala")

include("xef-core")
project(":xef-core").projectDir = file("core")

include("xef-lucene")
project(":xef-lucene").projectDir = file("integrations/lucene")

include("kotlin-loom")

include("xef-postgresql")
project(":xef-postgresql").projectDir = file("integrations/postgresql")

include("xef-pdf")
project(":xef-pdf").projectDir = file("integrations/pdf")
