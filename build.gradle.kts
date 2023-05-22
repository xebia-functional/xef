@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
  base
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.spotless)
  alias(libs.plugins.dokka)
  alias(libs.plugins.arrow.gradle.nexus)
  alias(libs.plugins.arrow.gradle.publish) apply false
//  alias(libs.plugins.semver.gradle)
}

allprojects {
  group = property("project.group").toString()
}
