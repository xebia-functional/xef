@file:Suppress("DSL_SCOPE_VIOLATION")

repositories {
  mavenCentral()
  mavenLocal()
}

plugins {
    `kotlin-dsl`
    base
		alias(libs.plugins.spotless)
}

spotless {
  kotlin {
    target("**/*.kt")
    ktfmt().googleStyle()
  }
}


dependencies {
		api(libs.detekt.api)
		testImplementation(libs.detekt.test)
    testImplementation(libs.kotest.assertions)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    implementation(libs.klogging)
}

tasks.withType<Jar>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Test>().configureEach {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    useJUnitPlatform()
		systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
		systemProperty("compile-snippet-tests", project.hasProperty("compile-test-snippets"))
    testLogging {
        setExceptionFormat("full")
        setEvents(listOf("passed", "skipped", "failed", "standardOut", "standardError"))
    }
}
