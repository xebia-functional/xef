plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlinx.serialization.get().pluginId)
    alias(libs.plugins.spotless)
}

repositories { mavenCentral() }

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain { languageVersion = JavaLanguageVersion.of(11) }
}

dependencies {
    implementation(projects.xefCore)
    implementation(projects.xefOpenai)
    implementation(libs.okio)
    implementation(libs.suspendApp.core)
    implementation(libs.bundles.arrow)
}

spotless {
    kotlin {
        target("**/*.kt")
        ktfmt().googleStyle().configure { it.setRemoveUnusedImport(true) }
    }
}

tasks.create<JavaExec>("test-example") {
    dependsOn("compileKotlin")
    group = "Execution"
    description = "Test example"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.xebia.funcional.xef.evaluator.examples.TestExample"
}
