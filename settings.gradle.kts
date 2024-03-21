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

include("xef-tokenizer")
project(":xef-tokenizer").projectDir = file("tokenizer")

include("xef-openai-client")
project(":xef-openai-client").projectDir = file("openai-client/client")

include("xef-openai-client-generator")
project(":xef-openai-client-generator").projectDir = file("openai-client/generator")

include("detekt-rules")
project(":detekt-rules").projectDir = file("detekt-rules")
