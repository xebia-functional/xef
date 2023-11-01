@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    scala
    alias(libs.plugins.spotless)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

dependencies {
    implementation(projects.xefCore)
    implementation(projects.xefScala)
    implementation(projects.xefReasoning)
    implementation(projects.xefOpenai)
    implementation(libs.circe.parser)
    implementation(libs.scala.lang)
    implementation(libs.logback)
}

tasks.withType<Test>().configureEach { useJUnit() }

tasks.withType<ScalaCompile> {
    scalaCompileOptions.additionalParameters = listOf("-Wunused:all", "-Wvalue-discard")
}

spotless { scala { scalafmt("3.7.15").configFile(".scalafmt.conf") } }
