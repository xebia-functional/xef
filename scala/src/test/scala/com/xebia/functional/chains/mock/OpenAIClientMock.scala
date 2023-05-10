package com.xebia.functional.scala.chains.mock

import scala.concurrent.duration._

import cats.effect.IO
import cats.syntax.all.*

import com.xebia.functional.scala.chains.TestData.*
import com.xebia.functional.scala.config.OpenAIConfig
import com.xebia.functional.scala.config.OpenAIConfigLLM
import com.xebia.functional.scala.llm.models.LLMResult
import com.xebia.functional.scala.llm.models.OpenAIRequest
import com.xebia.functional.scala.llm.openai.OpenAIClient
import com.xebia.functional.scala.llm.openai.models.EmbeddingRequest
import com.xebia.functional.scala.llm.openai.models.EmbeddingResult
import com.xebia.functional.scala.llm.openai.models.Usage

class OpenAIClientMock extends OpenAIClient[IO]:

  val config: OpenAIConfig =
    OpenAIConfig("foo", 5.seconds, 1, 1, OpenAIConfigLLM("model", "user", None, None, None, None, None, None, None, None, None, None, None))

  override def generate(request: OpenAIRequest): IO[List[LLMResult]] =
    request.prompt match
      case Some("Tell me a joke.") => IO(List(LLMResult("I'm not good at jokes")))
      case Some("My name is foo and I'm 28 years old") => IO(List(LLMResult("Hello there! Nice to meet you foo")))
      case Some(value) if value.eqv(templateFormatted) => IO(List(LLMResult("I don't know")))
      case Some(value) if value.eqv(templateInputsFormatted) => IO(List(LLMResult("Two inputs, right?")))
      case Some(value) if value.eqv(qaTemplateFormatted) => IO(List(LLMResult("I don't know")))
      case _ => IO(List(LLMResult("foo")))

  override def createEmbeddings(request: EmbeddingRequest): IO[EmbeddingResult] = IO(
    EmbeddingResult("foo", "bar", Vector(EmbeddingResult.Embedding("bar", Vector(0.1, 2.3), 1)), Usage(1, 2, 3))
  )

object OpenAIClientMock:
  def make: OpenAIClientMock = new OpenAIClientMock
