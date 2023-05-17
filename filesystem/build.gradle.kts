plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.spotless)
  alias(libs.plugins.arrow.gradle.publish)
}

repositories {
  mavenCentral()
}

kotlin {
  jvm()
  js(IR) {
    nodejs()
  }

  /*
  * Native support currently blocked due to regex inconsistencies not happening on JVM & JS on KMP tokenizer project.
  * Link to the issue: https://youtrack.jetbrains.com/issue/KT-58678/Native-Regex-inconsistency-with-JVM-Native-Regex
  */

  /*
  linuxX64()
  macosX64()
  macosArm64()
  mingwX64()
  */

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

    /*
    * Commenting also the source sets because of the KT-58678 issue specified above.
    */

    /*
    val linuxX64Main by getting
    val macosX64Main by getting
    val mingwX64Main by getting

    create("nativeMain") {
      dependsOn(commonMain)
      linuxX64Main.dependsOn(this)
      macosX64Main.dependsOn(this)
      mingwX64Main.dependsOn(this)
    }
   */
  }
}
