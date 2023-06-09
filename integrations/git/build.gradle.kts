plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.spotless)
    alias(libs.plugins.arrow.gradle.publish)
    alias(libs.plugins.semver.gradle)
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    linuxX64()
    macosX64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.xefCore)
                implementation(projects.xefFilesystem)
                implementation(projects.xefTokenizer)
                implementation(libs.okio)
                implementation(libs.klogging)
            }
        }

        val linuxX64Main by getting
        val macosX64Main by getting
        val mingwX64Main by getting

        create("nativeMain") {
            dependsOn(commonMain)
            linuxX64Main.dependsOn(this)
            macosX64Main.dependsOn(this)
            mingwX64Main.dependsOn(this)
        }
    }
}

tasks.withType<AbstractPublishToMaven> {
    dependsOn(tasks.withType<Sign>())
}
