package com.xebia.functional.scala.chains

import cats.effect.IO

import com.xebia.functional.scala.chains.mock.OpenAIClientMock
import com.xebia.functional.scala.chains.models.InvalidChainInputError
import com.xebia.functional.scala.chains.models.InvalidChainInputsError
import com.xebia.functional.scala.llm.LLM
import com.xebia.functional.scala.llm.models.OpenAIRequest
import com.xebia.functional.scala.prompt.PromptTemplate
import eu.timepit.refined.types.string.NonEmptyString
import munit.CatsEffectSuite
class LLMChainSpec extends CatsEffectSuite:

  val outputVariable = NonEmptyString.unsafeFrom("answer")
  val maxTokens = 500

  test("run should return a prediction with just the output") {
    val llm = LLM.openAI[IO](OpenAIClientMock.make)
    val template = "Tell me {foo}."
    val promptTemplate = PromptTemplate.fromTemplate[IO](template, List("foo"))
    val result =
      for
        prompt <- promptTemplate
        chain = LLMChain.make[IO](llm, prompt, outputVariable, true)
        res <- chain.run("a joke")
      yield res

    assertIO(result, Map("answer" -> "I'm not good at jokes"))
  }

  test("run should return a prediction with both output and inputs") {
    val llm = LLM.openAI[IO](OpenAIClientMock.make)
    val template = "Tell me {foo}."
    val promptTemplate = PromptTemplate.fromTemplate[IO](template, List("foo"))
    val result =
      for
        prompt <- promptTemplate
        chain = LLMChain.make[IO](llm, prompt, outputVariable, false)
        res <- chain.run("a joke")
      yield res

    assertIO(result, Map("foo" -> "a joke", "answer" -> "I'm not good at jokes"))
  }

  test("run should return a prediction with a more complex template") {
    val llm = LLM.openAI[IO](OpenAIClientMock.make)
    val template = "My name is {name} and I'm {age} years old"
    val promptTemplate = PromptTemplate.fromTemplate[IO](template, List("name", "age"))
    val result =
      for
        prompt <- promptTemplate
        chain = LLMChain.make[IO](llm, prompt, outputVariable, false)
        res <- chain.run(Map("age" -> "28", "name" -> "foo"))
      yield res

    assertIO(result, Map("age" -> "28", "name" -> "foo", "answer" -> "Hello there! Nice to meet you foo"))
  }

  test("run should fail with a InvalidChainInputsError if the inputs are not the expected ones from the PromptTemplate") {
    val llm = LLM.openAI[IO](OpenAIClientMock.make)
    val template = "My name is {name} and I'm {age} years old"
    val promptTemplate = PromptTemplate.fromTemplate[IO](template, List("name", "age"))
    val result =
      for
        prompt <- promptTemplate
        chain = LLMChain.make[IO](llm, prompt, outputVariable, false)
        res <- chain.run(Map("age" -> "28", "brand" -> "foo"))
      yield res

    interceptMessageIO[InvalidChainInputsError](
      "The provided inputs (age, brand) do not match with chain's inputs (name, age)"
    )(result)
  }

  test("run should fail with a InvalidChainInputError if using just one input but expects more") {
    val llm = LLM.openAI[IO](OpenAIClientMock.make)
    val template = "My name is {name} and I'm {age} years old"
    val promptTemplate = PromptTemplate.fromTemplate[IO](template, List("name", "age"))
    val result =
      for
        prompt <- promptTemplate
        chain = LLMChain.make[IO](llm, prompt, outputVariable, false)
        res <- chain.run("foo")
      yield res

    interceptMessageIO[InvalidChainInputError](
      "The expected inputs are more than one: name, age"
    )(result)
  }
