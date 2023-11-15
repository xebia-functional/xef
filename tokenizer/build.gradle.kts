plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.spotless)
    alias(libs.plugins.arrow.gradle.publish)
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.resources)
    alias(libs.plugins.detekt)
    `xef-gradle-cross-compilation`
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
    source.setFrom(files("src/commonMain/kotlin", "src/jvmMain/kotlin"))
    config.setFrom("../config/detekt/detekt.yml")
    autoCorrect = true
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
    sourceSets {
        val commonMain by getting
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotest.property)
                implementation(libs.kotest.framework)
                implementation(libs.kotest.assertions)
                implementation("com.goncalossilva:resources:0.3.2")
            }
        }
        val jvmTest by getting { dependencies { implementation(libs.kotest.junit5) } }
        js {
            nodejs { testTask { useMocha { timeout = "10000" } } }
            browser { testTask { useMocha { timeout = "10000" } } }
        }
        val linuxX64Main by getting
        val macosX64Main by getting
        val macosArm64Main by getting
        val mingwX64Main by getting
        create("nativeMain") {
            dependsOn(commonMain)
            linuxX64Main.dependsOn(this)
            macosX64Main.dependsOn(this)
            macosArm64Main.dependsOn(this)
            mingwX64Main.dependsOn(this)
        }
    }
}

tasks {
    withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        dependsOn(":detekt-rules:assemble")
        autoCorrect = true
    }
    named("detektJvmMain") {
        dependsOn(":detekt-rules:assemble")
        getByName("build").dependsOn(this)
    }
    named("detekt") {
        dependsOn(":detekt-rules:assemble")
        getByName("build").dependsOn(this)
    }
    withType<AbstractPublishToMaven> { dependsOn(withType<Sign>()) }
}
