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

include("xef-openai-client")
project(":xef-openai-client").projectDir = file("openai-client/client")

include("xef-openai-client-generator")
project(":xef-openai-client-generator").projectDir = file("openai-client/generator")

//</editor-fold>

//<editor-fold desc="Integrations">

include("xef-pdf")
project(":xef-pdf").projectDir = file("integrations/pdf")

include("xef-postgresql")
project(":xef-postgresql").projectDir = file("integrations/postgresql")

include("xef-opentelemetry")
project(":xef-opentelemetry").projectDir = file("integrations/opentelemetry")

include("xef-mlflow")
project(":xef-mlflow").projectDir = file("integrations/mlflow")
//</editor-fold>

include("xef-examples")
project(":xef-examples").projectDir = file("examples")

include("xef-reasoning")
project(":xef-reasoning").projectDir = file("reasoning")

include("xef-evaluator")
project(":xef-evaluator").projectDir = file("evaluator")

//<editor-fold desc="Kotlin">
include("detekt-rules")
project(":detekt-rules").projectDir = file("detekt-rules")
//</editor-fold>
