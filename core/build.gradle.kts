@file:Suppress("DSL_SCOPE_VIOLATION")

import love.forte.plugin.suspendtrans.*
import love.forte.plugin.suspendtrans.SuspendTransformConfiguration.Companion.jvmApi4JAnnotationClassInfo
import love.forte.plugin.suspendtrans.Transformer
import org.jetbrains.dokka.gradle.DokkaTask

repositories {
  mavenCentral()
}

plugins {
  base
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.spotless)
  alias(libs.plugins.dokka)
  alias(libs.plugins.arrow.gradle.publish)
  alias(libs.plugins.semver.gradle)
  alias(libs.plugins.suspend.transform.plugin)
  //id("com.xebia.asfuture").version("0.0.1")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
  toolchain {
    languageVersion = JavaLanguageVersion.of(11)
  }
}

kotlin {
  jvm {
    compilations {
      val integrationTest by compilations.creating {
        // Create a test task to run the tests produced by this compilation:
        tasks.register<Test>("integrationTest") {
          description = "Run the integration tests"
          group = "verification"
          classpath = compileDependencyFiles + runtimeDependencyFiles + output.allOutputs
          testClassesDirs = output.classesDirs

          testLogging {
            events("passed")
          }
        }
      }
      val test by compilations.getting
      integrationTest.associateWith(test)
    }
  }
  js(IR) {
    browser()
    nodejs()
  }

  linuxX64()
  macosX64()
  macosArm64()
  mingwX64()

  sourceSets {
    all {
      languageSettings.optIn("kotlin.ExperimentalStdlibApi")
    }

    val commonMain by getting {
      dependencies {
        api(libs.bundles.arrow)
        api(libs.kotlinx.serialization.json)
        api(libs.ktor.utils)
        api(projects.xefTokenizer)

        implementation(libs.klogging)
        implementation(libs.uuid)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(libs.kotest.property)
        implementation(libs.kotest.framework)
        implementation(libs.kotest.assertions)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(libs.ktor.http)
        implementation(libs.logback)

        api(libs.jackson)
        api(libs.jackson.schema)
        api(libs.jackson.schema.jakarta)
        api(libs.jakarta.validation)

        //TODO remove these from here and move to tools
        implementation(libs.skrape)
        implementation(libs.rss.reader)
      }
    }

    val jsMain by getting

    val jvmTest by getting {
      dependencies {
        implementation(libs.kotest.junit5)
      }
    }

    val linuxX64Main by getting
    val macosX64Main by getting
    val macosArm64Main by getting
    val mingwX64Main by getting
    val linuxX64Test by getting
    val macosX64Test by getting
    val macosArm64Test by getting
    val mingwX64Test by getting

    create("nativeMain") {
      dependsOn(commonMain)
      linuxX64Main.dependsOn(this)
      macosX64Main.dependsOn(this)
      macosArm64Main.dependsOn(this)
      mingwX64Main.dependsOn(this)
    }

    create("nativeTest") {
      dependsOn(commonTest)
      linuxX64Test.dependsOn(this)
      macosX64Test.dependsOn(this)
      macosArm64Test.dependsOn(this)
      mingwX64Test.dependsOn(this)
    }
  }
}

spotless {
  kotlin {
    target("**/*.kt")
    ktfmt().googleStyle()
  }
}

tasks {
  withType<Test>().configureEach {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    useJUnitPlatform()
    testLogging {
      setExceptionFormat("full")
      setEvents(listOf("passed", "skipped", "failed", "standardOut", "standardError"))
    }
  }

  withType<DokkaTask>().configureEach {
    kotlin.sourceSets.forEach { kotlinSourceSet ->
      dokkaSourceSets.named(kotlinSourceSet.name) {
        perPackageOption {
          matchingRegex.set(".*\\.internal.*")
          suppress.set(true)
        }
        skipDeprecated.set(true)
        reportUndocumented.set(false)
        val baseUrl: String = checkNotNull(project.properties["pom.smc.url"]?.toString())

        kotlinSourceSet.kotlin.srcDirs.filter { it.exists() }.forEach { srcDir ->
          sourceLink {
            localDirectory.set(srcDir)
            remoteUrl.set(uri("$baseUrl/blob/main/${srcDir.relativeTo(rootProject.rootDir)}").toURL())
            remoteLineSuffix.set("#L")
          }
        }
      }
    }
  }
}

suspendTransform {
  // enabled suspend transform plugin
  enabled = true
  // include 'love.forte.plugin.suspend-transform:suspend-transform-runtime' to the runtime environment
  includeRuntime = true
  // the configuration name for including 'love.forte.plugin.suspend-transform:suspend-transform-runtime'
  // runtimeConfigurationName = "implementation"

  val jvmBlockingTransformer = Transformer(
    // mark annotation info, e.g. `@JvmBlocking`
    markAnnotation = MarkAnnotation(
      classInfo = ClassInfo("love.forte.plugin.suspendtrans.annotation", "JvmBlocking"), // class info for this annotation
      baseNameProperty = "baseName",      // The property used to represent the 'base name' in the annotation, e.g. `@JvmBlocking(baseName = ...)`
      suffixProperty = "suffix",          // The property used to represent the 'suffix' in the annotation, e.g. `@JvmBlocking(suffix = ...)`
      asPropertyProperty = "asProperty",  // The property used to represent the 'asProperty' in the annotation, e.g. `@JvmBlocking(asProperty = true|false)`
      defaultSuffix = "Blocking",         // Default value used when property 'suffix' (the value of suffixProperty) does not exist (when not specified by the user) (the compiler plugin cannot detect property defaults directly, so the default value must be specified from here)
      // e.g. @JvmBlocking(suffix = "Abc"), the suffix is 'Abc', but `@JvmBlocking()`, the suffix is null in compiler plugin, so use the default suffix value.
      defaultAsProperty = false,          // Default value used when property 'suffix' (the value of suffixProperty) does not exist (Similar to defaultSuffix)
    ),
    // the transform function, e.g.
    // 👇 `love.forte.plugin.suspendtrans.runtime.$runInBlocking$`
    // it will be like
    // ```
    // @JvmBlocking suspend fun runXxx() { ... }
    // fun runXxxBlocking() = `$runInBlocking$` { runXxx() /* suspend  */ } // generated function
    // ```
    transformFunctionInfo = FunctionInfo(
      packageName = "com.xebia.functional.xef.suspendtrans.runtime",
      className = null, // null if top-level function
      functionName = "\$runInBlocking\$"
    ),
    transformReturnType = null, // return type, or null if return the return type of origin function, e.g. `ClassInfo("java.util.concurrent", "CompletableFuture")`
    transformReturnTypeGeneric = true, // if you return like `CompletableFuture<T>`, make it `true`
    originFunctionIncludeAnnotations = listOf(IncludeAnnotation(ClassInfo("kotlin.jvm", "JvmSynthetic"))), // include into origin function
    copyAnnotationsToSyntheticFunction = true,
    copyAnnotationExcludes = listOf(ClassInfo("kotlin.jvm", "JvmSynthetic")), // do not copy from origin function
    syntheticFunctionIncludeAnnotations = listOf(IncludeAnnotation(jvmApi4JAnnotationClassInfo)) // include into synthetic function
  )

  val jvmAsyncTransformer = Transformer(
    // mark annotation info, e.g. `@JvmAsync`
    markAnnotation = MarkAnnotation(
      classInfo = ClassInfo("love.forte.plugin.suspendtrans.annotation", "JvmAsync"), // class info for this annotation
      baseNameProperty = "baseName",      // The property used to represent the 'base name' in the annotation, e.g. `@JvmBlocking(baseName = ...)`
      suffixProperty = "suffix",          // The property used to represent the 'suffix' in the annotation, e.g. `@JvmBlocking(suffix = ...)`
      asPropertyProperty = "asProperty",  // The property used to represent the 'asProperty' in the annotation, e.g. `@JvmBlocking(asProperty = true|false)`
      defaultSuffix = "Async",         // Default value used when property 'suffix' (the value of suffixProperty) does not exist (when not specified by the user) (the compiler plugin cannot detect property defaults directly, so the default value must be specified from here)
      // e.g. @JvmAsync(suffix = "Abc"), the suffix is 'Abc', but `@JvmBlocking()`, the suffix is null in compiler plugin, so use the default suffix value.
      defaultAsProperty = false,          // Default value used when property 'suffix' (the value of suffixProperty) does not exist (Similar to defaultSuffix)
    ),
    // the transform function, e.g.
    // 👇 `love.forte.plugin.suspendtrans.runtime.$runInBlocking$`
    // it will be like
    // ```
    // @JvmBlocking suspend fun runXxx() { ... }
    // fun runXxxBlocking() = `$runInBlocking$` { runXxx() /* suspend  */ } // generated function
    // ```
    transformFunctionInfo = FunctionInfo(
      packageName = "com.xebia.functional.xef.suspendtrans.runtime",
      className = null, // null if top-level function
      functionName = "\$runInAsync\$"
    ),
    transformReturnType = ClassInfo("java.util.concurrent", "CompletableFuture"), // return type, or null if return the return type of origin function, e.g. `ClassInfo("java.util.concurrent", "CompletableFuture")`
    transformReturnTypeGeneric = true, // if you return like `CompletableFuture<T>`, make it `true`
    originFunctionIncludeAnnotations = listOf(IncludeAnnotation(ClassInfo("kotlin.jvm", "JvmSynthetic"))), // include into origin function
    copyAnnotationsToSyntheticFunction = true,
    copyAnnotationExcludes = listOf(ClassInfo("kotlin.jvm", "JvmSynthetic")), // do not copy from origin function
    syntheticFunctionIncludeAnnotations = listOf(IncludeAnnotation(jvmApi4JAnnotationClassInfo)) // include into synthetic function
  )

  addJvmTransformers(jvmBlockingTransformer, jvmAsyncTransformer)

  // or addJsTransformers(...)

}

tasks.withType<AbstractPublishToMaven> {
  dependsOn(tasks.withType<Sign>())
}
