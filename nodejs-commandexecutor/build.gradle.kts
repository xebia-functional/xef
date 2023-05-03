plugins {
  id(libs.plugins.kotlin.js.get().pluginId)
}

group = "com.xebia.functional.langchain4k"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

kotlin {
  js(IR) {
    nodejs()
  }

  sourceSets {
    val main by getting {
      dependencies {
        implementation(project(":langchain4k-kotlin"))
      }
    }
  }
}
