@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    scala
    `maven-publish`
    signing
    id("com.github.prokod.gradle-crossbuild").version("0.15.0")
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(projects.xefCore)
    implementation(projects.xefTokenizer)
    implementation(projects.xefPdf)
    implementation(projects.kotlinLoom)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.circe.parser)
    implementation(libs.circe)
    implementation(libs.cats.effect)
    testImplementation(libs.munit.core)
    testImplementation(libs.munit.cats.effect)
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
    toolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Test>().configureEach {
    useJUnit()
}

crossBuild {
    scalaVersionsCatalog = mapOf("3" to "3.2.2", "2.13" to "2.13.10")
    builds {
        register("scala") {
            scalaVersions = setOf("3", "2.13")
        }
    }
}

val crossBuildScala_3Jar by tasks.getting
val crossBuildScala_213Jar by tasks.getting

publishing {
    publications {
        mapOf(
            "3" to crossBuildScala_3Jar,
            "2.13" to crossBuildScala_213Jar
        ).forEach { (scalaVersion, versionedClassJar) ->
            create<MavenPublication>("maven_$scalaVersion") {
                val scalaSuffix = "_${scalaVersion.replace(".", "")}"
                from(components["java"])

                artifactId = base.archivesName.get() + scalaSuffix

                pom {
                    name.set(project.properties["pom.name"]?.toString())
                    description.set(project.properties["pom.description"]?.toString())
                    url.set(project.properties["pom.url"]?.toString())

                    licenses {
                        license {
                            name.set(project.properties["pom.license.name"]?.toString())
                            url.set(project.properties["pom.license.url"]?.toString())
                        }
                    }

                    developers {
                        developer {
                            id.set(project.properties["pom.developer.id"].toString())
                            name.set(project.properties["pom.developer.name"].toString())
                        }
                    }

                    scm {
                        url.set(project.properties["pom.smc.url"].toString())
                        connection.set(project.properties["pom.smc.connection"].toString())
                        developerConnection.set(project.properties["pom.smc.developerConnection"].toString())
                    }
                }
            }
        }
    }
}

signing {
    val isLocal = gradle.startParameter.taskNames.any { it.contains("publishToMavenLocal", ignoreCase = true) }
    val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
    val signingKey: String? = System.getenv("SIGNING_KEY")
    val signingPassphrase: String? = System.getenv("SIGNING_KEY_PASSPHRASE")

    isRequired = !isLocal
    useGpgCmd()
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassphrase)
    sign(publishing.publications)
}

spotless {
    scala {
        scalafmt("3.7.1").configFile(".scalafmt.conf").scalaMajorVersion("2.13")
    }
}