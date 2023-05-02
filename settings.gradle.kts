pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    
}
rootProject.name = "langchain4k"

include("langchain4k-scala")
project(":langchain4k-scala").projectDir = file("scala")