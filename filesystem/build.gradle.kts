plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.spotless)
  alias(libs.plugins.arrow.gradle.publish)
  alias(libs.plugins.semver.gradle)
}

repositories {
  mavenCentral()
}

kotlin {
  jvm()
  js(IR) {
    nodejs()
  }

  linuxX64()
  macosX64()
  mingwX64()

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(projects.xefCore)
        implementation(libs.okio)
        implementation(libs.klogging)
      }
    }

    val jsMain by getting {
      dependencies {
        implementation(libs.okio.nodefilesystem)
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

    val jvmTest by getting {
      dependencies {
        implementation(libs.kotest.junit5)
      }
    }

    val linuxX64Main by getting
    val macosX64Main by getting
    val mingwX64Main by getting

    create("nativeMain") {
      dependsOn(commonMain)
      linuxX64Main.dependsOn(this)
      macosX64Main.dependsOn(this)
      mingwX64Main.dependsOn(this)
    }
  }
}

tasks.withType<AbstractPublishToMaven> {
  dependsOn(tasks.withType<Sign>())
}
