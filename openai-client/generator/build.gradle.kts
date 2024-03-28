@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    java
    alias(libs.plugins.spotless)
}

dependencies {
    implementation("org.openapitools:openapi-generator-cli:7.4.0")
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
    val command = (listOf(
        "generate",
        "-i",
        "config/openai-api.yaml",
        "-g",
        "ai.xef.openai.generator.KMMGeneratorConfig",
        "-o",
        "../client/build/generated/OpenAI/",
        "--skip-validate-spec"
    ) + if (project.hasProperty("debugModels") && !project.hasProperty("debugOperations")) {
        listOf("--global-property", "debugModels=true")
    } else if(!project.hasProperty("debugModels") && project.hasProperty("debugOperations")) {
        listOf("--global-property", "debugOperations=true")
    } else emptyList())

    args = command
    classpath = sourceSets["main"].runtimeClasspath
}
