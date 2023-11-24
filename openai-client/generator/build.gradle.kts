@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    java
    alias(libs.plugins.spotless)
}

dependencies {
    implementation("org.openapitools:openapi-generator-cli:7.1.0")
}

tasks.test {
    useJUnitPlatform()
}

task("downloadOpenAIAPI", JavaExec::class) {
    group = "GenerateTasks"
    mainClass = "ai.xef.openai.generator.DownloadOpenAIAPI"
    classpath = sourceSets["main"].runtimeClasspath
}

task("openaiClientGenerate", JavaExec::class) {
    group = "GenerateTasks"
    mainClass = "org.openapitools.codegen.OpenAPIGenerator"
    args = listOf(
        "generate",
        "-i",
        "config/openai-api.yaml",
        "-g",
        "ai.xef.openai.generator.KMMGeneratorConfig",
        "-o",
        "../client",
        "--skip-validate-spec",
        "-c",
        "config/openai-config.json",
    )
    classpath = sourceSets["main"].runtimeClasspath
}.finalizedBy(":xef-openai-client:spotlessApply")