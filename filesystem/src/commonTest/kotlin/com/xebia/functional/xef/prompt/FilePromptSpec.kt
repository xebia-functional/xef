package com.xebia.functional.xef.prompt

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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

    val variables = mapOf("name" to "Angela", "age" to "18")
    val prompt = Prompt("templates/example.txt".toPath(), fileSystem)

    prompt.format(variables) shouldBe Prompt("My name is Angela and I'm 18 years old")
  }
})
