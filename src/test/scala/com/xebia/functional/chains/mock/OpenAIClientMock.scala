package com.xebia.functional.chains.mock

import cats.effect.IO
import cats.syntax.all.*

import com.xebia.functional.chains.TestData.*
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.llm.openai.models.CompletionChoice
import com.xebia.functional.llm.openai.models.CompletionRequest
import com.xebia.functional.llm.openai.models.EmbeddingRequest
import com.xebia.functional.llm.openai.models.EmbeddingResult
import com.xebia.functional.llm.openai.models.Usage

class OpenAIClientMock extends OpenAIClient[IO]:

  override def createCompletion(request: CompletionRequest): IO[List[CompletionChoice]] =
    request.prompt match
      case Some("Tell me a joke.") => IO(List(CompletionChoice("I'm not good at jokes", 1, "foo")))
      case Some("My name is foo and I'm 28 years old") => IO(List(CompletionChoice("Hello there! Nice to meet you foo", 1, "foo")))
      case Some(value) if value.eqv(templateFormatted) => IO(List(CompletionChoice("I don't know", 1, "bar")))
      case Some(value) if value.eqv(templateInputsFormatted) => IO(List(CompletionChoice("Two inputs, right?", 1, "foo")))
      case Some(value) if value.eqv(qaTemplateFormatted) => IO(List(CompletionChoice("I don't know", 1, "foo")))
      case _ => IO(List(CompletionChoice("foo", 1, "bar")))

  override def createEmbeddings(request: EmbeddingRequest): IO[EmbeddingResult] = IO(
    EmbeddingResult("foo", "bar", Vector(EmbeddingResult.Embedding("bar", Vector(0.1, 2.3), 1)), Usage(1, 2, 3))
  )

object OpenAIClientMock:
  def make: OpenAIClientMock = new OpenAIClientMock
