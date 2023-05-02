package com.xebia.functional.prompt

import cats.effect.*

import com.xebia.functional.prompt.models.*
import munit.CatsEffectSuite

class ConfigSpec extends CatsEffectSuite:

  test("make should return a valid Config if the template and input variables are valid") {

    val template = "Hello {name}, you are {age} years old."
    val inputVariables = List("name", "age")
    val configIO = Config.make[IO](template, inputVariables)

    val config = configIO.unsafeRunSync()
    assertEquals(config.inputVariables, inputVariables)
    assertEquals(config.template, template)
    assertEquals(config.templateFormat, TemplateFormat.FString)
  }

  test("make should fail with a InvalidTemplateError if the template has missing arguments") {
    val template = "Hello {name}, you are {age} years old."
    val inputVariables = List("name")
    val configIO = Config.make[IO](template, inputVariables)

    interceptMessageIO[InvalidTemplateError]("Template 'Hello {name}, you are {age} years old.' has missing arguments: {age}")(configIO)
  }

  test("make should fail with a InvalidTemplateError if the template has unused arguments") {
    val template = "Hello {name}, you are {age} years old."
    val inputVariables = List("name", "age", "city")
    val configIO = Config.make[IO](template, inputVariables)

    interceptMessageIO[InvalidTemplateError]("Template 'Hello {name}, you are {age} years old.' has unused arguments: {city}")(configIO)
  }

  test("make should fail with a InvalidTemplateError if there are duplicate input variables") {
    val template = "Hello {name}, you are {name} years old."
    val inputVariables = List("name")
    val configIO = Config.make[IO](template, inputVariables)

    interceptMessageIO[InvalidTemplateError]("Template 'Hello {name}, you are {name} years old.' has duplicate arguments: {name}")(configIO)
  }

  test("make should fail with a combination of InvalidTemplateErrors if there are multiple things wrong") {
    val template = "Hello {name}, you are {name} years old."
    val inputVariables = List("name", "age")
    val configIO = Config.make[IO](template, inputVariables)

    interceptMessageIO[InvalidTemplateError](
      "Template 'Hello {name}, you are {name} years old.' has unused arguments: {age}; " +
        "Template 'Hello {name}, you are {name} years old.' has duplicate arguments: {name}"
    )(configIO)
  }
