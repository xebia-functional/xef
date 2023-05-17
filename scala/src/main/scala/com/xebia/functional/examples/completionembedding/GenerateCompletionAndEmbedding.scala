package com.xebia.functional.scala.completionembedding

import scala.concurrent.duration._

import cats.effect.IO
import cats.effect.IOApp

import com.xebia.functional.scala.config.OpenAIConfig
import com.xebia.functional.scala.config.OpenAIConfigLLM
import com.xebia.functional.scala.embeddings.openai.models.EmbeddingModel
import com.xebia.functional.scala.embeddings.openai.models.RequestConfig
import com.xebia.functional.scala.embeddings.openai.models.RequestConfig.User
import com.xebia.functional.scala.llm.models.OpenAIRequest
import com.xebia.functional.scala.llm.openai.OpenAIClient
import com.xebia.functional.scala.llm.openai.models.EmbeddingRequest
import org.http4s.ember.client.EmberClientBuilder

object GenerateCompletionAndEmbedding extends IOApp.Simple {

  override def run: IO[Unit] =
    val OPENAI_TOKEN = "<place-your-openai-token-here>"
    val openAIConfig = OpenAIConfig(
      OPENAI_TOKEN,
      5.seconds,
      5,
      1000,
      OpenAIConfigLLM()
    )
    val openAIClient = OpenAIClient[IO](openAIConfig)

    for
      o1 <- openAIExample(openAIClient)
      o2 <- openAIEmbeddingsExample(openAIClient)
      _ = println(o1)
      _ = println(o2)
    yield ()

  def openAIExample(client: OpenAIClient[IO]) =
    client
      .generate(
        OpenAIRequest
          .builder(model = "ada", user = "testing")
          .withPrompt("Write a tagline for an ice cream shop.")
          .withEcho(true)
          .withN(3)
          .build()
      )

  def openAIEmbeddingsExample(client: OpenAIClient[IO]) =
    client
      .createEmbeddings(
        EmbeddingRequest(model = "text-embedding-ada-002", input = List("How much is 2+2"), user = "testing")
      )
}
