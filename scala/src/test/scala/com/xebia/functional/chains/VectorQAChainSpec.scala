package com.xebia.functional.chains

import cats.effect.IO

import com.xebia.functional.chains.mock.OpenAIClientMock
import com.xebia.functional.chains.mock.VectorStoreMock
import com.xebia.functional.chains.models.InvalidChainInputError
import com.xebia.functional.chains.models.InvalidChainInputsError
import com.xebia.functional.chains.models.InvalidCombineDocumentsChainError
import com.xebia.functional.chains.retrievalqa.VectorQAChain
import com.xebia.functional.llm.LLM
import com.xebia.functional.llm.models.OpenAIRequest
import eu.timepit.refined.types.string.NonEmptyString
import munit.CatsEffectSuite

class VectorQAChainSpec extends CatsEffectSuite:

  val llm = LLM.openAI[IO](OpenAIClientMock.make)
  val outputVariable = NonEmptyString.unsafeFrom("answer")

  test("run should return the answer from the LLMChain") {
    val vectorStore = VectorStoreMock.make
    val qa = VectorQAChain.make[IO](llm, vectorStore, "stuff", 10, outputVariable, true)
    val result = qa.run("What do you think?")

    assertIO(result, TestData.outputIDK)
  }

  test("run should return the answer from the LLMChain when using question explicitly in the inputs") {
    val vectorStore = VectorStoreMock.make
    val qa = VectorQAChain.make[IO](llm, vectorStore, "stuff", 10, outputVariable, true)
    val result = qa.run(Map("question" -> "What do you think?"))

    assertIO(result, TestData.outputIDK)
  }

  test("run should return the answer from the LLMChain when using question explicitly in the inputs") {
    val vectorStore = VectorStoreMock.make
    val qa = VectorQAChain.make[IO](llm, vectorStore, "stuff", 10, outputVariable, true)
    val result = qa.run(Map("question" -> "What do you think?"))

    assertIO(result, TestData.outputIDK)
  }

  test("run should return the answer from the LLMChain when the input is more than one") {
    val vectorStore = VectorStoreMock.make
    val qa = VectorQAChain.make[IO](llm, vectorStore, "stuff", 10, outputVariable, true)
    val result = qa.run(Map("question" -> "What do you think?", "foo" -> "bla bla bla"))

    assertIO(result, TestData.outputIDK)
  }

  test("run should fail with an InvalidChainInputsError if the inputs don't match the expected") {
    val vectorStore = VectorStoreMock.make
    val qa = VectorQAChain.make[IO](llm, vectorStore, "stuff", 10, outputVariable, true)
    val result = qa.run(Map("foo" -> "What do you think?"))

    interceptMessageIO[InvalidChainInputsError](
      "The provided inputs (foo) do not match with chain's inputs (question)"
    )(result)
  }
