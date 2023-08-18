plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    alias(libs.plugins.spotless)
    alias(libs.plugins.arrow.gradle.publish)
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.detekt)
}

dependencies { detektPlugins(project(":detekt-rules")) }

detekt {
    toolVersion = "1.23.1"
    source = files("src/main/kotlin")
    config.setFrom("../config/detekt/detekt.yml")
    autoCorrect = true
}

repositories { mavenCentral() }

sourceSets {
    main { java.srcDirs("src/main/kotlin") }
    test { java.srcDirs("src/test/kotlin") }
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
    toolchain { languageVersion = JavaLanguageVersion.of(19) }
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
            listOfNotNull("--enable-preview", "--add-modules", "jdk.incubator.concurrent")
    )
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("--enable-preview", "--add-modules", "jdk.incubator.concurrent")
    testLogging { events("passed", "skipped", "failed") }
}

spotless {
    java { palantirJavaFormat() }
    kotlin {
        target("**/*.kt")
        ktfmt().googleStyle()
    }
}

tasks {
    withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        dependsOn(":detekt-rules:assemble")
        autoCorrect = true
    }
		named("detekt") {
        dependsOn(":detekt-rules:assemble")
        getByName("build").dependsOn(this)
    }
    withType<AbstractPublishToMaven> { dependsOn(withType<Sign>()) }
}
