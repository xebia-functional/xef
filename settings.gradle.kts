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

//<editor-fold desc="Core">
include("xef-core")
project(":xef-core").projectDir = file("core")

include("xef-filesystem")
project(":xef-filesystem").projectDir = file("filesystem")

include("xef-tokenizer")
project(":xef-tokenizer").projectDir = file("tokenizer")

include("xef-gpt4all")
project(":xef-gpt4all").projectDir = file("gpt4all-kotlin")

//<editor-fold desc="Integrations">
include("xef-lucene")
project(":xef-lucene").projectDir = file("integrations/lucene")

include("xef-pdf")
project(":xef-pdf").projectDir = file("integrations/pdf")

include("xef-postgresql")
project(":xef-postgresql").projectDir = file("integrations/postgresql")

include("xef-sql")
project(":xef-sql").projectDir = file("integrations/sql")
//</editor-fold>
//</editor-fold>

//<editor-fold desc="Kotlin">
include("xef-kotlin")
project(":xef-kotlin").projectDir = file("kotlin")

include("xef-kotlin-examples")
project(":xef-kotlin-examples").projectDir = file("examples/kotlin")

include("kotlin-loom")
//</editor-fold>

//<editor-fold desc="Scala">
include("xef-scala-examples")
project(":xef-scala-examples").projectDir = file("examples/scala")

include("xef-scala")
project(":xef-scala").projectDir = file("scala")

include("xef-scala-cats")
project(":xef-scala-cats").projectDir = file("scala-cats")
//</editor-fold>
