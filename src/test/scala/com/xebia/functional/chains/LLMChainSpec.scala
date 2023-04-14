package com.xebia.functional.chains

import cats.effect.IO

import com.xebia.functional.chains.models.InvalidChainInputError
import com.xebia.functional.chains.models.InvalidChainInputsError
import com.xebia.functional.prompt.PromptTemplate
import munit.CatsEffectSuite

class LLMChainSpec extends CatsEffectSuite:

  test("LLMChain should return a prediction with just the output") {
    val llm = OpenAIClientMock.make
    val template = "Tell me {foo}."
    val promptTemplate = PromptTemplate.fromTemplate[IO](template, List("foo"))
    val result =
      for
        prompt <- promptTemplate
        chain = LLMChain.make[IO](llm, prompt, "davinci", "testing", false, 1, 0.0, true)
        res <- chain.run("a joke")
      yield res

    assertIO(result, Map("text" -> "I'm not good at jokes"))
  }

  test("LLMChain should return a prediction with both output and inputs") {
    val llm = OpenAIClientMock.make
    val template = "Tell me {foo}."
    val promptTemplate = PromptTemplate.fromTemplate[IO](template, List("foo"))
    val result =
      for
        prompt <- promptTemplate
        chain = LLMChain.make[IO](llm, prompt, "davinci", "testing", false, 1, 0.0, false)
        res <- chain.run("a joke")
      yield res

    assertIO(result, Map("foo" -> "a joke", "text" -> "I'm not good at jokes"))
  }

  test("LLMChain should return a prediction with a more complex template") {
    val llm = OpenAIClientMock.make
    val template = "My name is {name} and I'm {age} years old"
    val promptTemplate = PromptTemplate.fromTemplate[IO](template, List("name", "age"))
    val result =
      for
        prompt <- promptTemplate
        chain = LLMChain.make[IO](llm, prompt, "davinci", "testing", false, 1, 0.0, false)
        res <- chain.run(Map("age" -> "28", "name" -> "foo"))
      yield res

    assertIO(result, Map("age" -> "28", "name" -> "foo", "text" -> "Hello there! Nice to meet you foo"))
  }

  test("LLMChain should fail when inputs are not the expected ones from the PromptTemplate") {
    val llm = OpenAIClientMock.make
    val template = "My name is {name} and I'm {age} years old"
    val promptTemplate = PromptTemplate.fromTemplate[IO](template, List("name", "age"))
    val result =
      for
        prompt <- promptTemplate
        chain = LLMChain.make[IO](llm, prompt, "davinci", "testing", false, 1, 0.0, false)
        res <- chain.run(Map("age" -> "28", "brand" -> "foo"))
      yield res

    interceptMessageIO[InvalidChainInputsError](
      "The provided inputs (age, brand) do not match with chain's inputs (name, age)"
    )(result)
  }

  test("LLMChain should fail when using just one input but expecting more") {
    val llm = OpenAIClientMock.make
    val template = "My name is {name} and I'm {age} years old"
    val promptTemplate = PromptTemplate.fromTemplate[IO](template, List("name", "age"))
    val result =
      for
        prompt <- promptTemplate
        chain = LLMChain.make[IO](llm, prompt, "davinci", "testing", false, 1, 0.0, false)
        res <- chain.run("foo")
      yield res

    interceptMessageIO[InvalidChainInputError](
      "The expected inputs are more than one: name, age"
    )(result)
  }
