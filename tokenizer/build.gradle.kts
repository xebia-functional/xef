plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.spotless)
  alias(libs.plugins.arrow.gradle.publish)
}

repositories {
  mavenCentral()
}

kotlin {
  jvm()
  js(IR) {
    browser()
    nodejs()
  }
  linuxX64()
  macosX64()
  mingwX64()
}
