package com.xebia.functional.prompt

import arrow.core.raise.either
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

class PromptTemplateSpec : StringSpec({
  "PromptTemplate(template, list) should fail if the template is not valid" {
    val template = "Tell me {foo}."

    either {
      val prompt = PromptTemplate(template, emptyList())
      prompt.format(mapOf("foo" to "bar"))
    } shouldBeLeft InvalidTemplate("Template 'Tell me {foo}.' has missing arguments: {foo}")
  }

  "format with no input variables shouldn't have any effect" {
    val template = "Tell me a joke."

    either {
      val prompt = PromptTemplate(template, emptyList())
      prompt.format(emptyMap())
    } shouldBeRight "Tell me a joke."
  }

  "format should return the expected result with a given set of variables" {
    val template = "My name is {name} and I'm {age} years old"
    val variables = mapOf("name" to "John", "age" to "47")

    either {
      val config = Config(template, listOf("name", "age"))
      PromptTemplate(config).format(variables)
    } shouldBeRight "My name is John and I'm 47 years old"
  }

  "PromptTemplate(template, list) should return a PromptTemplate instance with the given template and input variables" {
    val template = "My name is {name} and I'm {age} years old"
    val variables = mapOf("name" to "Mary", "age" to "25")
    either {
      PromptTemplate(template, listOf("name", "age")).format(variables)
    } shouldBeRight "My name is Mary and I'm 25 years old"
  }

  " PromptTemplate(examples, suffix, variables, prefix) should return a PromptTemplate instance with the given examples and input variables" {
    val prefix = """
    |I want you to act as a naming consultant for new companies.
    |Here are some examples of good company names:""".trimMargin()

    val suffix = """
    |The name should be short, catchy and easy to remember.
    |What is a good name for a company that makes {product}?""".trimMargin()

    val examples = listOf("search engine, Google", "social media, Facebook", "video sharing, YouTube")
    val variables = mapOf("product" to "functional programming")

    either {
      PromptTemplate(examples, suffix = suffix, variables = listOf("product"), prefix = prefix).format(variables)
    } shouldBeRight """
        |I want you to act as a naming consultant for new companies.
        |Here are some examples of good company names:
        |
        |search engine, Google
        |social media, Facebook
        |video sharing, YouTube
        |
        |The name should be short, catchy and easy to remember.
        |What is a good name for a company that makes functional programming?""".trimMargin()
  }

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

  "format should return the expected result for variables with functions" {
    val template = "My name is {name} and I'm {age} years old"
    fun getAge() = "21"

    val variables = mapOf("name" to "Charles", "age" to getAge())

    either {
      val config = Config(template, listOf("name", "age"))
      PromptTemplate(config).format(variables)
    } shouldBeRight "My name is Charles and I'm ${getAge()} years old"
  }

  "format for human should return a HumanMessage" {
    val template = "My name is {name} and I'm {age} years old"
    val variables: Map<String, String> = mapOf("name" to "Charles", "age" to "21")

    either {
      val prompt: PromptTemplate<String> = PromptTemplate(template, listOf("name", "age"))
      val humanPrompt: PromptTemplate<HumanMessage> = PromptTemplate.human(prompt)
      humanPrompt.format(variables)

    } shouldBeRight HumanMessage("My name is Charles and I'm 21 years old")
  }

  "format for system should return a SystemMessage" {
    val template = "{sounds}"
    val variables: Map<String, String> = mapOf("sounds" to "Beep bep")

    either {
      val prompt: PromptTemplate<String> = PromptTemplate(template, listOf("sounds"))
      val systemPrompt: PromptTemplate<SystemMessage> =  PromptTemplate.system(prompt)
      systemPrompt.format(variables)

    } shouldBeRight SystemMessage("Beep bep")
  }

  "format for ai should return a AIMessage" {
    val template = "Hi, I'm an {machine}"
    val variables: Map<String, String> = mapOf("machine" to "AI")

    either {
      val prompt: PromptTemplate<String> = PromptTemplate(template, listOf("machine"))
      val aiPrompt: PromptTemplate<AIMessage> = PromptTemplate.ai(prompt)
      aiPrompt.format(variables)

    } shouldBeRight AIMessage("Hi, I'm an AI")
  }

  "format for chat should return a ChatMessage" {
    val role = "Yoda"
    val template = "Lost a {action}, master {name} has."
    val variables: Map<String, String> = mapOf("action" to "battle", "name" to "Obi-Wan")

    either {
      val prompt: PromptTemplate<String> = PromptTemplate(template, listOf("action", "name"))
      val chatPrompt: PromptTemplate<ChatMessage> = PromptTemplate.chat(prompt, role)
      chatPrompt.format(variables)

    } shouldBeRight ChatMessage("Lost a battle, master Obi-Wan has.", "Yoda")
  }
})
