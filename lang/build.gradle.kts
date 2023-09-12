import org.availlang.artifact.AvailArtifactType
import org.availlang.artifact.PackageType
import org.availlang.artifact.environment.location.ProjectHome
import org.availlang.artifact.environment.location.Scheme

plugins {
  id(libs.plugins.kotlin.jvm.get().pluginId)
  id(libs.plugins.avail.plugin.get().pluginId)
}

version = "0.0.1"

repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.avail.core)
  avail(libs.avail.stdlib)
}

avail {
  projectDescription = "Xef.ai prompting language"
  availVersion = libs.versions.avail.core.get()
  includeAvailLibDependency(
    rootName = "avail-stdlib",
    rootNameInJar = "avail",
    dependency = libs.avail.stdlib.get().toString()
  )
  rootsDirectory = ProjectHome(
    "src/main/avail",
    Scheme.FILE,
    project.projectDir.absolutePath,
    null
  )
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
    dependency(libs.avail.core.get())
  }
}
