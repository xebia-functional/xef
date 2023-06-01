import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

class ScalaDocumentationPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    with(project) {
      tasks.register("scaladocJar", Jar::class.java) {
        archiveClassifier.set("javadoc")
        dependsOn(tasks.named("scaladoc"))
        from("${project.buildDir}/docs/scaladoc")
      }
    }
  }
}