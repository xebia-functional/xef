@file:Suppress("DSL_SCOPE_VIOLATION")

group = "com.xebia.functional"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

plugins {
  base
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.spotless)
  alias(libs.plugins.kotlinx.serialization)
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
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
  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  when {
    hostOs == "Mac OS X" -> macosX64("native")
    hostOs == "Linux" -> linuxX64("native")
    isMingwX64 -> mingwX64("native")
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }


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
        implementation(libs.ktor.client.cio)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotest.junit5)
        implementation(libs.kotest.testcontainers)
        implementation(libs.testcontainers.postgresql)
      }
    }
    val jsMain by getting {
      dependencies {
        implementation(libs.ktor.client.js)
      }
    }
    val nativeMain by getting {
      dependencies {
        implementation(libs.ktor.client.cio)
      }
    }
  }
}

spotless {
  kotlin {
    ktfmt().googleStyle()
  }
}
