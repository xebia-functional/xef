package com.xebia.functional.chains

import cats.effect.IO

import com.xebia.functional.chains.combine.StuffChain
import com.xebia.functional.chains.mock.OpenAIClientMock
import com.xebia.functional.chains.models.InvalidChainInputsError
import com.xebia.functional.domain.Document
import com.xebia.functional.prompt.PromptTemplate
import munit.CatsEffectSuite
import eu.timepit.refined.types.string.NonEmptyString

class StuffChainSpec extends CatsEffectSuite:

  val outputVariable = NonEmptyString.unsafeFrom("answer")

  test("combineDocs should return all the documents properly combined") {
    val promptTemplate = PromptTemplate.fromTemplate[IO](TestData.template, List("context", "question"))
    val docs = List(Document("foo foo foo"), Document("bar bar bar"), Document("baz baz baz"))
    val result =
      for
        prompt <- promptTemplate
        stuff = StuffChain.make[IO](docs, OpenAIClientMock.make, prompt, "context", "foo", "user", false, 1, 0.0, outputVariable, true)
        res <- stuff.combineDocs(docs)
      yield res

    assertIO(result, TestData.contextOutput)
  }

  test("run should return the proper LLMChain response with one input") {
    val promptTemplate = PromptTemplate.fromTemplate[IO](TestData.template, List("context", "question"))
    val docs = List(Document("foo foo foo"), Document("bar bar bar"), Document("baz baz baz"))
    val result =
      for
        prompt <- promptTemplate
        stuff = StuffChain.make[IO](docs, OpenAIClientMock.make, prompt, "context", "foo", "user", false, 1, 0.0, outputVariable, true)
        res <- stuff.run("What do you think?")
      yield res

    assertIO(result, TestData.outputIDK)
  }

  test("run should return the proper LLMChain response wiht more than one input") {
    val promptTemplate = PromptTemplate.fromTemplate[IO](TestData.templateInputs, List("context", "name", "age"))
    val docs = List(Document("foo foo foo"), Document("bar bar bar"), Document("baz baz baz"))
    val result =
      for
        prompt <- promptTemplate
        stuff = StuffChain.make[IO](docs, OpenAIClientMock.make, prompt, "context", "foo", "user", false, 1, 0.0, outputVariable, false)
        res <- stuff.run(Map("name" -> "Scala", "age" -> "28"))
      yield res

    assertIO(result, TestData.outputInputs ++ Map("context" -> TestData.contextFormatted, "name" -> "Scala", "age" -> "28"))
  }

  test("run should fail with a InvalidCombineDocumentsChainError if the inputs don't match the expected") {
    val promptTemplate = PromptTemplate.fromTemplate[IO](TestData.templateInputs, List("context", "name", "age"))
    val docs = List(Document("foo foo foo"), Document("bar bar bar"), Document("baz baz baz"))
    val result =
      for
        prompt <- promptTemplate
        stuff = StuffChain.make[IO](docs, OpenAIClientMock.make, prompt, "context", "foo", "user", false, 1, 0.0, outputVariable, false)
        res <- stuff.run(Map("name" -> "Scala", "city" -> "Seattle"))
      yield res

    interceptMessageIO[InvalidChainInputsError](
      "The provided inputs (name, city) do not match with chain's inputs (name, age)"
    )(result)
  }
