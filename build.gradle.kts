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
    browser {
      commonWebpackConfig {
        cssSupport {
          enabled.set(true)
        }
      }
    }
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
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.bundles.ktor.client)

        implementation("com.squareup.okio:okio:3.3.0")
      }
    }
    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}

spotless {
  kotlin {
    ktfmt().googleStyle()
  }
}