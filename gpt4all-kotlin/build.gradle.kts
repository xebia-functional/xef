plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.spotless)
  alias(libs.plugins.arrow.gradle.publish)
  alias(libs.plugins.semver.gradle)
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
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation("net.java.dev.jna:jna-platform:5.13.0")
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation(libs.kotest.junit5)
      }
    }

    js {
      nodejs {}
      browser {}
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

tasks.withType<AbstractPublishToMaven> {
  dependsOn(tasks.withType<Sign>())
}
