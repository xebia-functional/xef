import org.availlang.artifact.AvailArtifactType
import org.availlang.artifact.PackageType
import org.availlang.artifact.environment.location.ProjectHome
import org.availlang.artifact.environment.location.Scheme
import org.availlang.artifact.jar.JvmComponent

plugins {
  id(libs.plugins.kotlin.jvm.get().pluginId)
  id(libs.plugins.avail.plugin.get().pluginId)
}

version = "0.0.1"

repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.avail)
}

avail {
  projectDescription = "Xef.ai prompting language"
  includeStdAvailLibDependency {
    version = libs.versions.avail.stdlib.get()
  }
  rootsDirectory = ProjectHome(
    "src/main/avail",
    Scheme.FILE,
    project.projectDir.absolutePath,
    null)
  createProjectRoot("xef").apply {
    modulePackage("Compiler").apply {
      versions = listOf("Avail-1.6.1")
      extends = listOf("Avail")
    }
  }
  artifact {
    artifactType = AvailArtifactType.APPLICATION
    packageType = PackageType.JAR
    artifactName = "xef-anvil"
    version = project.version.toString()
    implementationTitle = "Xef Anvil"
    jarManifestMainClass = "avail.project.AvailProjectManagerRunner"
    jvmComponent = JvmComponent(
      true,
      "$implementationTitle Runner",
      mapOf(jarManifestMainClass to "Execute Anvil for Xef.ai"))
    dependency(libs.avail.get())
  }
}

tasks {
  createProjectFile {
    fileName = "xef.json"
  }
  val launchAnvil by creating(JavaExec::class) {
    dependsOn(availArtifactJar)
    group = "avail"
    description = "Launch Anvil for Xef.ai"
    classpath = files("$buildDir/libs/xef-anvil-$version.jar")
  }
}
