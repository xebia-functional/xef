package com.xebia.functional.prompt

import arrow.core.raise.either
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ConfigSpec : StringSpec({

  "should return a valid Config if the template and input variables are valid" {
    val template = "Hello {name}, you are {age} years old."
    val variables = listOf("name", "age")

    val config = either { Config(template, variables) }.shouldBeRight()

    config.inputVariables shouldBe variables
    config.template shouldBe template
    config.templateFormat shouldBe TemplateFormat.FString
  }

  "should fail with a InvalidTemplateError if the template has missing arguments" {
    val template = "Hello {name}, you are {age} years old."
    val variables = listOf("name")

    either {
      Config(template, variables)
    } shouldBeLeft InvalidTemplate("Template 'Hello {name}, you are {age} years old.' has missing arguments: {age}")
  }

  "should fail with a InvalidTemplateError if the template has unused arguments" {
    val template = "Hello {name}, you are {age} years old."
    val variables = listOf("name", "age", "unused")

    either {
      Config(template, variables)
    } shouldBeLeft InvalidTemplate("Template 'Hello {name}, you are {age} years old.' has unused arguments: {unused}")
  }

  "should fail with a InvalidTemplateError if there are duplicate input variables" {
    val template = "Hello {name}, you are {name} years old."
    val variables = listOf("name")

    either {
      Config(template, variables)
    } shouldBeLeft InvalidTemplate("Template 'Hello {name}, you are {name} years old.' has duplicate arguments: {name}")
  }

  "should fail with a combination of InvalidTemplateErrors if there are multiple things wrong" {
    val template = "Hello {name}, you are {name} years old."
    val variables = listOf("name", "age")
    val unused = "Template 'Hello {name}, you are {name} years old.' has unused arguments: {age}"
    val duplicated = "Template 'Hello {name}, you are {name} years old.' has duplicate arguments: {name}"

    either {
      Config(template, variables)
    } shouldBeLeft InvalidTemplate("$unused, $duplicated")
  }
})
