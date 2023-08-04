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
  implementation(projects.xefKotlin)
  implementation(projects.xefFilesystem)
  implementation(projects.xefPdf)
  implementation(projects.xefSql)
  implementation(projects.xefTokenizer)
  implementation(projects.xefGpt4all)
  implementation(projects.xefGcp)
  implementation(projects.xefAws)
  implementation(projects.xefOpenai)
  implementation(projects.xefReasoning)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.logback)
  implementation(libs.klogging)
  implementation(libs.bundles.arrow)
  implementation(libs.okio)
  implementation(libs.jdbc.mysql.connector)
  api(libs.ktor.client)
}

tasks.getByName<Copy>("processResources") {
  dependsOn(projects.xefGpt4all.dependencyProject.tasks.getByName("jvmProcessResources"))
  from("${projects.xefGpt4all.dependencyProject.buildDir}/processedResources/jvm/main")
  into("$buildDir/resources/main")
}


