@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    scala
    `maven-publish`
    signing
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(projects.xefCore)
    implementation(projects.kotlinLoom)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.circe.parser)
    implementation(libs.circe)
    implementation(libs.cats.effect)
    testImplementation(libs.munit.core)
    testImplementation(libs.munit.cats.effect)
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
    toolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Test>().configureEach {
    useJUnit()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            val scala3Suffix = "_3"
            from(components["java"])

            artifactId = base.archivesName.get() + scala3Suffix
        }
    }
}

spotless {
    scala {
        scalafmt("3.7.1").configFile(".scalafmt.conf").scalaMajorVersion("2.13")
    }
}