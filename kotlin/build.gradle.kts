@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.dokka.gradle.DokkaTask

repositories {
  mavenCentral()
}

plugins {
  base
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.spotless)
  alias(libs.plugins.dokka)
  alias(libs.plugins.arrow.gradle.publish)
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
    compilations {
      val main by getting
      val integrationTest by compilations.creating {
        // Create a test task to run the tests produced by this compilation:
        tasks.register<Test>("integrationTest") {
          description = "Run the integration tests"
          group = "verification"
          // Run the tests with the classpath containing the compile dependencies (including 'main'),
          // runtime dependencies, and the outputs of this compilation:
          classpath = compileDependencyFiles + runtimeDependencyFiles + output.allOutputs

          // Run only the tests from this compilation's outputs:
          testClassesDirs = output.classesDirs

          testLogging {
            events("passed")
          }
        }
      }
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
    val commonMain by getting {
      dependencies {
        api(libs.bundles.arrow)
        api(libs.bundles.ktor.client)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.uuid)
        implementation(libs.klogging)
      }
    }

    commonTest {
      dependencies {
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
        api(libs.ktor.client.cio)
        implementation(libs.logback)
      }
    }

    val jsMain by getting {
      dependencies {
        api(libs.ktor.client.js)
      }
    }

    val linuxX64Main by getting {
      dependencies {
        api(libs.ktor.client.cio)
      }
    }

    val macosX64Main by getting {
      dependencies {
        api(libs.ktor.client.cio)
      }
    }

    val mingwX64Main by getting {
      dependencies {
        api(libs.ktor.client.winhttp)
      }
    }

    val commonTest by getting
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotest.junit5)
      }
    }
    val linuxX64Test by getting
    val macosX64Test by getting
    val mingwX64Test by getting

    val jvmIntegrationTest by getting {
      dependsOn(jvmMain)
      dependsOn(jvmTest)
      dependencies {
        implementation(libs.kotest.testcontainers)
        implementation(libs.testcontainers.postgresql)
      }
    }

    create("nativeMain") {
      dependsOn(commonMain)
      linuxX64Main.dependsOn(this)
      macosX64Main.dependsOn(this)
      mingwX64Main.dependsOn(this)
    }

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
