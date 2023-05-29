import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id(libs.plugins.kotlin.jvm.get().pluginId)
  id(libs.plugins.kotlinx.serialization.get().pluginId)
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

dependencies {
  implementation(projects.xefCore)
  implementation(projects.xefFilesystem)
  implementation(projects.xefPdf)
  implementation(projects.xefSql)
  implementation(projects.xefTokenizer)
  implementation(projects.xefGit)
  implementation(projects.xefAntlr)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.logback)
  implementation(libs.klogging)
  implementation(libs.bundles.arrow)
  implementation(libs.okio)
  implementation(libs.jdbc.mysql.connector)
  api(libs.bundles.ktor.client)
  implementation("org.antlr:antlr4-runtime:4.13.0")
}
