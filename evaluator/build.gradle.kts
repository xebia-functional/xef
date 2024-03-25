plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlinx.serialization.get().pluginId)
    alias(libs.plugins.arrow.gradle.publish)
    alias(libs.plugins.detekt)
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.spotless)
}

repositories { mavenCentral() }

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain { languageVersion = JavaLanguageVersion.of(11) }
}

dependencies {
    api(libs.kotlinx.serialization.json)
    detektPlugins(project(":detekt-rules"))
    implementation(projects.xefCore)
    implementation(projects.xefOpenaiClient)
}

detekt {
    toolVersion = "1.23.1"
    source.setFrom(files("src/main/kotlin"))
    config.setFrom("../config/detekt/detekt.yml")
    autoCorrect = true
}

spotless {
    kotlin {
        target("**/*.kt")
        ktfmt().googleStyle().configure { it.setRemoveUnusedImport(true) }
    }
}
