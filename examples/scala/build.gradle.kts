@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
  scala
  alias(libs.plugins.scala.multiversion)
  alias(libs.plugins.spotless)
}

java {
  sourceCompatibility = JavaVersion.VERSION_19
  targetCompatibility = JavaVersion.VERSION_19
  toolchain {
    languageVersion = JavaLanguageVersion.of(19)
  }
}

dependencies {
  implementation(projects.xefCore)
  implementation(projects.xefScala)
}

tasks.withType<Test>().configureEach {
  useJUnit()
}

spotless {
  scala {
    scalafmt("3.7.1").configFile(".scalafmt.conf").scalaMajorVersion("2.13")
  }
}