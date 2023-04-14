package com.xebia.functional.chains

import cats.effect.IO

import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.llm.openai.models.CompletionChoice
import com.xebia.functional.llm.openai.models.CompletionRequest
import com.xebia.functional.llm.openai.models.EmbeddingRequest
import com.xebia.functional.llm.openai.models.EmbeddingResult

class OpenAIClientMock extends OpenAIClient[IO]:

  override def createCompletion(request: CompletionRequest): IO[List[CompletionChoice]] =
    request.prompt match
      case Some("Tell me a joke.") => IO(List(CompletionChoice("I'm not good at jokes", 1, "foo")))
      case Some("My name is foo and I'm 28 years old") => IO(List(CompletionChoice("Hello there! Nice to meet you foo", 1, "foo")))
      case _ => IO(List(CompletionChoice("foo", 1, "bar")))

  override def createEmbeddings(request: EmbeddingRequest): IO[EmbeddingResult] = ???

object OpenAIClientMock:
  def make: OpenAIClientMock = new OpenAIClientMock
