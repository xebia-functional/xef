package com.xebia.functional.prompt

import java.nio.file.Paths

import cats.effect.*

import com.xebia.functional.prompt.models.*
import munit.CatsEffectSuite

class PromptTemplateSpec extends CatsEffectSuite:

  test("PromptTemplate.fromTemplate should fail if the template is not valid") {

    val template = "Tell me {foo}."
    val io =
      for
        prompt <- PromptTemplate.fromTemplate[IO](template, List.empty)
        str <- prompt.format(Map("foo" -> "bar"))
      yield str

    interceptIO[InvalidTemplateError](io)
  }

  test("format with no input variables shouldn't have any effect") {

    val template = "Tell me a joke."
    val io =
      for
        prompt <- PromptTemplate.fromTemplate[IO](template, List.empty)
        str <- prompt.format(Map.empty)
      yield str

    assertIO(io, "Tell me a joke.")
  }

  test("format should return the expected result with a given set of variables") {

    val template = "My name is {name} and I'm {age} years old"
    val variables = Map("name" -> "John", "age" -> "47")
    val io =
      for
        config <- Config.make[IO](template, List("name", "age"))
        prompt = PromptTemplate[IO](config)
        str <- prompt.format(variables)
      yield str

    assertIO(io, "My name is John and I'm 47 years old")
  }

  test("fromTemplate should return a PromptTemplate instance with the given template and input variables") {

    val template = "My name is {name} and I'm {age} years old"
    val variables = Map("name" -> "Mary", "age" -> "25")
    val io =
      for
        prompt <- PromptTemplate.fromTemplate[IO](template, List("name", "age"))
        str <- prompt.format(variables)
      yield str

    assertIO(io, "My name is Mary and I'm 25 years old")
  }

  test("fromExamples should return a PromptTemplate instance with the given examples and input variables") {

    val prefix = """
    |I want you to act as a naming consultant for new companies.
    |Here are some examples of good company names:""".stripMargin

    val suffix = """
    |The name should be short, catchy and easy to remember.
    |What is a good name for a company that makes {product}?""".stripMargin

    val examples = List("search engine, Google", "social media, Facebook", "video sharing, YouTube")
    val variables = Map("product" -> "functional programming")
    val io =
      for
        prompt <- PromptTemplate.fromExamples[IO](examples, suffix = suffix, List("product"), prefix = prefix)
        str <- prompt.format(variables)
      yield str

    assertIO(
      io,
      """
        |I want you to act as a naming consultant for new companies.
        |Here are some examples of good company names:
        |
        |search engine, Google
        |social media, Facebook
        |video sharing, YouTube
        |
        |The name should be short, catchy and easy to remember.
        |What is a good name for a company that makes functional programming?""".stripMargin
    )
  }

  test("fromFile should return a PromptTemplate instance with the contents of the specified file") {
    val templateFile = Paths.get(getClass.getResource("/templates/example.txt").toURI)
    val inputVariables = List("name", "age")
    val variables = Map("name" -> "Angela", "age" -> "18")

    val io =
      for
        prompt <- PromptTemplate.fromFile[IO](templateFile, inputVariables)
        str <- prompt.format(variables)
      yield str

    assertIO(io, "My name is Angela and I'm 18 years old")
  }
