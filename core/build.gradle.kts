@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.dokka.gradle.DokkaTask

repositories {
  mavenCentral()
}

plugins {
  base
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.spotless)
  alias(libs.plugins.dokka)
  alias(libs.plugins.arrow.gradle.publish)
  alias(libs.plugins.semver.gradle)
  //id("com.xebia.asfuture").version("0.0.1")
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
    withJava()
    compilations {
      val integrationTest by compilations.creating {
        // Create a test task to run the tests produced by this compilation:
        tasks.register<Test>("integrationTest") {
          description = "Run the integration tests"
          group = "verification"
          classpath = compileDependencyFiles + runtimeDependencyFiles + output.allOutputs
          testClassesDirs = output.classesDirs

          testLogging {
            events("passed")
          }
        }
      }
      val test by compilations.getting
      integrationTest.associateWith(test)
    }
  }
  js(IR) {
    browser()
    nodejs()
  }

  linuxX64()
  macosX64()
  macosArm64()
  mingwX64()

  sourceSets {
    all {
      languageSettings.optIn("kotlin.ExperimentalStdlibApi")
    }

    val commonMain by getting {
      dependencies {
        api(libs.bundles.arrow)
        api(libs.kotlinx.serialization.json)
        api(libs.ktor.utils)
        api(projects.xefTokenizer)
        implementation(libs.bundles.ktor.client)
        implementation(libs.klogging)
        implementation(libs.uuid)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(libs.kotest.property)
        implementation(libs.kotest.framework)
        implementation(libs.kotest.assertions)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(libs.ktor.http)
        implementation(libs.logback)
        implementation(libs.skrape)
        implementation(libs.rss.reader)
        api(libs.jackson)
        api(libs.jackson.schema)
        api(libs.jackson.schema.jakarta)
        api(libs.jakarta.validation)
        implementation(libs.kotlinx.coroutines.reactive)
        api(libs.ktor.client.cio)
      }
    }

    val jsMain by getting {
      dependencies {
        api(libs.ktor.client.js)
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation(libs.kotest.junit5)
      }
    }

    val linuxX64Main by getting {
      dependencies {
        implementation(libs.ktor.client.cio)
      }
    }
    val macosX64Main by getting {
      dependencies {
        implementation(libs.ktor.client.cio)
      }
    }
    val macosArm64Main by getting {
      dependencies {
        implementation(libs.ktor.client.cio)
      }
    }
    val mingwX64Main by getting {
      dependencies {
        implementation(libs.ktor.client.winhttp)
      }
    }
    val linuxX64Test by getting
    val macosX64Test by getting
    val macosArm64Test by getting
    val mingwX64Test by getting

    create("nativeMain") {
      dependsOn(commonMain)
      linuxX64Main.dependsOn(this)
      macosX64Main.dependsOn(this)
      macosArm64Main.dependsOn(this)
      mingwX64Main.dependsOn(this)
    }

    create("nativeTest") {
      dependsOn(commonTest)
      linuxX64Test.dependsOn(this)
      macosX64Test.dependsOn(this)
      macosArm64Test.dependsOn(this)
      mingwX64Test.dependsOn(this)
    }
  }
}

spotless {
  kotlin {
    target("**/*.kt")
    ktfmt().googleStyle().configure {
      it.setRemoveUnusedImport(true)
    }
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

tasks.withType<AbstractPublishToMaven> {
  dependsOn(tasks.withType<Sign>())
}
