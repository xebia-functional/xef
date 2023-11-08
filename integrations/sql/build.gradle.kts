plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    alias(libs.plugins.arrow.gradle.publish)
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies { detektPlugins(project(":detekt-rules")) }

repositories { mavenCentral() }

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain { languageVersion = JavaLanguageVersion.of(11) }
}

detekt {
    toolVersion = "1.23.1"
    source = files("src/main/kotlin")
    config.setFrom("../../config/detekt/detekt.yml")
    autoCorrect = true
}

dependencies {
    implementation(projects.xefCore)
    implementation(projects.xefTokenizer)
    implementation(libs.klogging)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
}

tasks {
    withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        dependsOn(":detekt-rules:assemble")
        autoCorrect = true
    }
    named("detekt") {
        dependsOn(":detekt-rules:assemble")
        getByName("build").dependsOn(this)
    }
    withType<AbstractPublishToMaven> { dependsOn(withType<Sign>()) }
}
