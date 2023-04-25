package com.xebia.functional.embeddings.openai

import scala.concurrent.duration.*
import scala.util.Try

import cats.data.NonEmptyChain
import cats.effect.IO
import cats.syntax.all.*

import com.xebia.functional.config.OpenAIConfig
import com.xebia.functional.config.OpenAIConfigLLM
import com.xebia.functional.embeddings.models.*
import com.xebia.functional.embeddings.openai.models.*
import com.xebia.functional.llm.models.LLMResult
import com.xebia.functional.llm.models.OpenAIRequest
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.llm.openai.models.*
import munit.CatsEffectSuite
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import retry.RetryDetails.*
import retry.RetryPolicies.*
import retry.*

class OpenAIEmbeddingsSpec extends CatsEffectSuite:

  private val fakeClient = new OpenAIClient[IO] {
    val config: OpenAIConfig = OpenAIConfig("foo", 5.seconds, 1, 1, OpenAIConfigLLM())
    def generate(request: OpenAIRequest): IO[List[LLMResult]] =
      IO.pure(List.empty[LLMResult])

    def createEmbeddings(request: EmbeddingRequest): IO[EmbeddingResult] = {
      val embedding = request.input.toVector.map { i =>
        val weight = Try(i.toInt).getOrElse(1)
        EmbeddingResult.Embedding(
          "sample_object",
          Vector.fill(16)(weight * 0.5),
          1
        )
      }
      IO.pure(
        EmbeddingResult(
          request.model,
          "sample_object",
          embedding,
          Usage(0, 0, 0)
        )
      )
    }
  }

  val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private val config = OpenAIConfig.configValue[IO].load[IO].unsafeRunSync()
  private val defaultEmbeddingConfig = RequestConfig(EmbeddingModel.TextEmbeddingAda002, RequestConfig.User("test"))
  private val embeddings = new OpenAIEmbeddings[IO](config, fakeClient, logger)

  test("embedQuery should return an empty Vector for an empty input") {
    assertIO(embeddings.embedQuery("", defaultEmbeddingConfig), Vector.empty[Embedding])
  }

  test("embedQuery should embed the input text") {
    assertIO(embeddings.embedQuery("hello", defaultEmbeddingConfig), Vector(Embedding(Vector.fill(16)(0.5))))
  }

  test("embedDocuments should return an empty Vector for an empty input") {
    assertIO(embeddings.embedDocuments(List.empty, None, defaultEmbeddingConfig), Vector.empty[Embedding])
  }

  test("embedDocuments should embed the input texts") {
    val texts = List("hello", "world")
    val expectedEmbeddings = Vector(
      Embedding(Vector.fill(16)(0.5)),
      Embedding(Vector.fill(16)(0.5))
    )
    assertIO(embeddings.embedDocuments(texts, None, defaultEmbeddingConfig), expectedEmbeddings)
  }

  test("embedDocuments should split the input into chunks of default size") {
    val texts = List.fill(2000)("hello")
    val expectedEmbeddings = Vector.fill(2000)(Embedding(Vector.fill(16)(0.5)))
    assertIO(embeddings.embedDocuments(texts, None, defaultEmbeddingConfig), expectedEmbeddings)
  }

  test("embedDocuments should split the input into chunks of given size and maintain the order") {
    val weights = 1 to 2000
    val texts = weights.toList.map(_.show)
    val chunkSize = 100
    val expectedEmbeddings = weights.toVector.map(w => Embedding(Vector.fill(16)(w * 0.5)))
    assertIO(embeddings.embedDocuments(texts, Some(chunkSize), defaultEmbeddingConfig), expectedEmbeddings)
  }
