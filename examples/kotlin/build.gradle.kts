import org.gradle.internal.io.NullOutputStream
import java.io.OutputStream

plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlinx.serialization.get().pluginId)
    alias(libs.plugins.spotless)
}

repositories { mavenCentral() }

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain { languageVersion = JavaLanguageVersion.of(11) }
}

dependencies {
    implementation(projects.xefKotlin)
    implementation(projects.xefFilesystem)
    implementation(projects.xefPdf)
    implementation(projects.xefSql)
    implementation(projects.xefTokenizer)
    implementation(projects.xefGpt4all)
    implementation(projects.xefGcp)
    implementation(projects.xefOpenai)
    implementation(projects.xefReasoning)
    implementation(projects.xefOpentelemetry)
    implementation(projects.xefMlflow)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback)
    implementation(libs.klogging)
    implementation(libs.bundles.arrow)
    implementation(libs.okio)
    implementation(libs.jdbc.mysql.connector)
    api(libs.ktor.client)
}

spotless {
    kotlin {
        target("**/*.kt")
        ktfmt().googleStyle().configure { it.setRemoveUnusedImport(true) }
    }
}

tasks.getByName<Copy>("processResources") {
    dependsOn(projects.xefGpt4all.dependencyProject.tasks.getByName("jvmProcessResources"))
    from("${projects.xefGpt4all.dependencyProject.layout.buildDirectory}/processedResources/jvm/main")
    into("${layout.buildDirectory}/resources/main")
}

@Suppress("MaxLineLength")
tasks.create<Exec>("docker-sql-example-up") {

    commandLine("docker", "compose", "-f", "$projectDir/src/main/resources/sql/stack.yml", "up", "-d", "mysql")

    doLast {
        println(">> Docker compose up done!")
        println(">> IMPORTANT: Execute `./gradlew docker-sql-example-populate` to populate the database that you want")
    }
}

@Suppress("MaxLineLength")
tasks.create<Exec>("docker-sql-example-populate") {
    this.standardOutput = OutputStream.nullOutputStream()

    commandLine("docker", "exec", "-i", "xef-sql-example-mysql", "bash", "-c", "cd /root/; mysql -ptoor < mysql_dump.sql")

    doLast {
        println(">> Database populated!")
    }
}

@Suppress("MaxLineLength")
tasks.create<Exec>("docker-sql-example-down") {
    commandLine("docker", "compose", "-f", "$projectDir/src/main/resources/sql/stack.yml", "down", "-v", "--rmi", "local", "--remove-orphans")

    doLast {
        println(">> Docker compose down done!")
    }
}
