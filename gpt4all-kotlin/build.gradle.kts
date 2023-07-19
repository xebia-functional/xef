plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.spotless)
  alias(libs.plugins.arrow.gradle.publish)
  alias(libs.plugins.semver.gradle)
  alias(libs.plugins.kotlinx.serialization)
}

repositories {
  mavenCentral()
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
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(projects.xefCore)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotest.property)
        implementation(libs.kotest.framework)
        implementation(libs.kotest.assertions)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(libs.gpt4all.java.bindings)
        implementation(libs.ai.djl.huggingface.tokenizers)
        implementation(libs.kotlinx.coroutines.reactive)
      }
    }

    val jsMain by getting {
    }

    val jvmTest by getting {
      dependencies {
        implementation(libs.kotest.junit5)
      }
    }

  }
}

tasks.withType<Test>().configureEach {
  maxParallelForks = Runtime.getRuntime().availableProcessors()
  useJUnitPlatform()
  testLogging {
    setExceptionFormat("full")
    setEvents(listOf("passed", "skipped", "failed", "standardOut", "standardError"))
  }
}

tasks.withType<AbstractPublishToMaven> {
  dependsOn(tasks.withType<Sign>())
}
