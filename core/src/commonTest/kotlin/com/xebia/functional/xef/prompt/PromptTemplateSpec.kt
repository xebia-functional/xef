package com.xebia.functional.xef.prompt

import arrow.core.raise.either
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class PromptTemplateSpec :
  StringSpec({
    "PromptTemplate(template, list) should fail if the template is not valid" {
      val template = "Tell me {foo}."

      either { PromptTemplate(template, emptyList()).format(mapOf("foo" to "bar")) } shouldBeLeft
        InvalidTemplate("Template 'Tell me {foo}.' has missing arguments: {foo}")
    }

    "format with no input variables shouldn't have any effect" {
      val template = "Tell me a joke."

      either { PromptTemplate(template).format(emptyMap()) } shouldBeRight Prompt("Tell me a joke.")
    }

    "format should return the expected result with a given set of variables" {
      val template = "My name is {name} and I'm {age} years old"
      val variables = mapOf("name" to "John", "age" to "47")

      either { PromptTemplate(template, listOf("name", "age")).format(variables) } shouldBeRight
        Prompt("My name is John and I'm 47 years old")
    }

    "PromptTemplate(template, list) should return a PromptTemplate instance with the given template and input variables" {
      val template = "My name is {name} and I'm {age} years old"
      val variables = mapOf("name" to "Mary", "age" to "25")
      either { PromptTemplate(template, listOf("name", "age")).format(variables) } shouldBeRight
        Prompt("My name is Mary and I'm 25 years old")
    }

    " PromptTemplate(examples, suffix, variables, prefix) should return a PromptTemplate instance with the given examples and input variables" {
      val prefix =
        """
    |I want you to act as a naming consultant for new companies.
    |Here are some examples of good company names:"""
          .trimMargin()

      val suffix =
        """
    |The name should be short, catchy and easy to remember.
    |What is a good name for a company that makes {product}?"""
          .trimMargin()

      val examples =
        listOf("search engine, Google", "social media, Facebook", "video sharing, YouTube")
      val variables = mapOf("product" to "functional programming")

      either {
        PromptTemplate(examples, suffix = suffix, variables = listOf("product"), prefix = prefix)
          .format(variables)
      } shouldBeRight
        Prompt(
          """
        |I want you to act as a naming consultant for new companies.
        |Here are some examples of good company names:
        |
        |search engine, Google
        |social media, Facebook
        |video sharing, YouTube
        |
        |The name should be short, catchy and easy to remember.
        |What is a good name for a company that makes functional programming?"""
            .trimMargin()
        )
    }

    "format should return the expected result for variables with functions" {
      val template = "My name is {name} and I'm {age} years old"
      fun getAge() = "21"

      val variables = mapOf("name" to "Charles", "age" to getAge())

      either { PromptTemplate(template, listOf("name", "age")).format(variables) } shouldBeRight
        Prompt("My name is Charles and I'm ${getAge()} years old")
    }

    "should fail with a InvalidTemplateError if the template has missing arguments" {
      val template = "Hello {name}, you are {age} years old."
      val variables = listOf("name")

      either { PromptTemplate(template, variables) } shouldBeLeft
        InvalidTemplate(
          "Template 'Hello {name}, you are {age} years old.' has missing arguments: {age}"
        )
    }

    "should fail with a InvalidTemplateError if the template has unused arguments" {
      val template = "Hello {name}, you are {age} years old."
      val variables = listOf("name", "age", "unused")

      either { PromptTemplate(template, variables) } shouldBeLeft
        InvalidTemplate(
          "Template 'Hello {name}, you are {age} years old.' has unused arguments: {unused}"
        )
    }

    "should fail with a InvalidTemplateError if there are duplicate input variables" {
      val template = "Hello {name}, you are {name} years old."
      val variables = listOf("name")

      either { PromptTemplate(template, variables) } shouldBeLeft
        InvalidTemplate(
          "Template 'Hello {name}, you are {name} years old.' has duplicate arguments: {name}"
        )
    }

    "should fail with a combination of InvalidTemplateErrors if there are multiple things wrong" {
      val template = "Hello {name}, you are {name} years old."
      val variables = listOf("name", "age")
      val unused = "Template 'Hello {name}, you are {name} years old.' has unused arguments: {age}"
      val duplicated =
        "Template 'Hello {name}, you are {name} years old.' has duplicate arguments: {name}"

      either { PromptTemplate(template, variables) } shouldBeLeft
        InvalidTemplate("$unused, $duplicated")
    }
  })
