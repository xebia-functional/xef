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

include("xef-openai")
project(":xef-openai").projectDir = file("openai")

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

include("xef-gcp")
project(":xef-gcp").projectDir = file("integrations/gcp")
//</editor-fold>
//</editor-fold>

//<editor-fold desc="Kotlin">
include("xef-kotlin")
project(":xef-kotlin").projectDir = file("kotlin")

include("xef-kotlin-examples")
project(":xef-kotlin-examples").projectDir = file("examples/kotlin")

//</editor-fold>

//<editor-fold desc="Scala">
include("xef-scala-examples")
project(":xef-scala-examples").projectDir = file("examples/scala")

include("xef-scala")
project(":xef-scala").projectDir = file("scala")

//</editor-fold>

//<editor-fold desc="Java">
include("xef-java")
project(":xef-java").projectDir = file("java")

include("xef-java-examples")
project(":xef-java-examples").projectDir = file("examples/java")
//</editor-fold>

//<editor-fold desc="Java">
include("xef-reasoning")
project(":xef-reasoning").projectDir = file("reasoning")

include("xef-java-examples")
project(":xef-java-examples").projectDir = file("examples/java")
//</editor-fold>

//<editor-fold desc="Kotlin">
include("xef-server")
project(":xef-server").projectDir = file("server")
//</editor-fold>

//<editor-fold desc="Kotlin">
include("detekt-rules")
project(":detekt-rules").projectDir = file("detekt-rules")
//</editor-fold>

//<editor-fold desc="Avail">
include("lang")
project(":lang").projectDir = file("lang")
//</editor-fold>
