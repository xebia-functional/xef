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

include("xef-kotlin-examples")
project(":xef-kotlin-examples").projectDir = file("examples/kotlin")

include("xef-scala-examples")
project(":xef-scala-examples").projectDir = file("examples/scala")

include("kotlin-loom")

include("xef-core")
project(":xef-core").projectDir = file("core")

include("xef-filesystem")
project(":xef-filesystem").projectDir = file("filesystem")

include("xef-scala")
project(":xef-scala").projectDir = file("scala")

include("xef-tokenizer")
project(":xef-tokenizer").projectDir = file("tokenizer")

// Integration modules
include("xef-lucene")
project(":xef-lucene").projectDir = file("integrations/lucene")

include("xef-pdf")
project(":xef-pdf").projectDir = file("integrations/pdf")

include("xef-postgresql")
project(":xef-postgresql").projectDir = file("integrations/postgresql")

include("xef-sql")
project(":xef-sql").projectDir = file("integrations/sql")
