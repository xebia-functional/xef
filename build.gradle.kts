@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.dokka.gradle.DokkaTask

version = "0.0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

plugins {
  base
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.spotless)
  alias(libs.plugins.dokka)
  alias(libs.plugins.arrow.gradle.nexus)
  alias(libs.plugins.arrow.gradle.publish)
}

allprojects {
  group = property("project.group").toString()
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
  toolchain {
    languageVersion = JavaLanguageVersion.of(11)
  }
}

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = JavaVersion.VERSION_11.majorVersion
    }
    withJava()
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }
  js(IR) {
    browser()
    nodejs()
  }
  linuxX64()
  macosX64()
  mingwX64()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.arrow.fx)
        implementation(libs.arrow.resilience)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.bundles.ktor.client)
        implementation(libs.okio)
        implementation(libs.uuid)
        implementation(libs.klogging)
      }
    }

    commonTest {
      dependencies {
        implementation(libs.okio.fakefilesystem)
        implementation(libs.kotest.property)
        implementation(libs.kotest.framework)
        implementation(libs.kotest.assertions)
        implementation(libs.kotest.assertions.arrow)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.hikari)
        implementation(libs.postgresql)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotest.junit5)
        implementation(libs.kotest.testcontainers)
        implementation(libs.testcontainers.postgresql)
      }
    }

    val commonMain by getting
    val linuxX64Main by getting
    val macosX64Main by getting
    val mingwX64Main by getting
    create("nativeMain") {
      dependsOn(commonMain)
      linuxX64Main.dependsOn(this)
      macosX64Main.dependsOn(this)
      mingwX64Main.dependsOn(this)
    }

    val commonTest by getting
    val linuxX64Test by getting
    val macosX64Test by getting
    val mingwX64Test by getting
    create("nativeTest") {
      dependsOn(commonTest)
      linuxX64Test.dependsOn(this)
      macosX64Test.dependsOn(this)
      mingwX64Test.dependsOn(this)
    }
  }
}

spotless {
  kotlin {
    ktfmt().googleStyle()
  }
}

tasks {
  withType<Test>().configureEach {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    useJUnitPlatform()
    testLogging {
      setExceptionFormat("full")
      setEvents(listOf("passed", "skipped", "failed", "standardOut", "standardError"))
    }
  }

  withType<DokkaTask>().configureEach {
    kotlin.sourceSets.forEach { kotlinSourceSet ->
      dokkaSourceSets.named(kotlinSourceSet.name) {
        perPackageOption {
          matchingRegex.set(".*\\.internal.*")
          suppress.set(true)
        }
        skipDeprecated.set(true)
        reportUndocumented.set(false)
        val baseUrl: String = checkNotNull(project.properties["pom.smc.url"]?.toString())

        kotlinSourceSet.kotlin.srcDirs.filter { it.exists() }.forEach { srcDir ->
          sourceLink {
            localDirectory.set(srcDir)
            remoteUrl.set(uri("$baseUrl/blob/main/${srcDir.relativeTo(rootProject.rootDir)}").toURL())
            remoteLineSuffix.set("#L")
          }
        }
      }
    }
  }
}
