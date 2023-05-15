package com.xebia.functional.xef.prompt

import arrow.core.raise.either
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

class PromptTemplateSpec : StringSpec({
  "should return a PromptTemplate instance with the contents of the specified file" {
    val fileSystem = FakeFileSystem().apply {
      val templates = "templates".toPath()
      createDirectory(templates)
      val example = templates / "example.txt"
      write(example) { writeUtf8("My name is {name} and I'm {age} years old") }
    }
    val inputVariables = listOf("name", "age")
    val variables = mapOf("name" to "Angela", "age" to "18")

    either {
      val prompt = PromptTemplate("templates/example.txt".toPath(), inputVariables, fileSystem)
      prompt.format(variables)
    } shouldBeRight "My name is Angela and I'm 18 years old"
  }
})
