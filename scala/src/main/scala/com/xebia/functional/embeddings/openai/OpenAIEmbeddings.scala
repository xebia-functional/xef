package com.xebia.functional.scala.embeddings.openai

import scala.concurrent.duration.FiniteDuration

import cats.*
import cats.data.NonEmptyChain
import cats.effect.*
import cats.syntax.all.*

import com.xebia.functional.scala.config.OpenAIConfig
import com.xebia.functional.scala.embeddings.Embeddings
import com.xebia.functional.scala.embeddings.models.Embedding
import com.xebia.functional.scala.embeddings.openai.models.RequestConfig
import com.xebia.functional.scala.llm.openai.OpenAIClient
import com.xebia.functional.scala.llm.openai.models.EmbeddingRequest
import com.xebia.functional.scala.llm.openai.models.EmbeddingResult
import org.typelevel.log4cats.Logger
import retry.RetryDetails.*
import retry.RetryPolicies.*
import retry.*

class OpenAIEmbeddings[F[_]: MonadThrow: Sleep: Parallel](config: OpenAIConfig, oaiClient: OpenAIClient[F], logger: Logger[F]) extends Embeddings[F]:

  def embedQuery(text: String, rc: RequestConfig): F[Vector[Embedding]] =
    if (text.nonEmpty) embedDocuments(List(text), None, rc)
    else Monad[F].pure(Vector.empty[Embedding])

  def embedDocuments(texts: List[String], chunkSize: Option[Int], rc: RequestConfig): F[Vector[Embedding]] =
    chunkedEmbedDocuments(texts, chunkSize.getOrElse(config.chunkSize), rc)

  private def chunkedEmbedDocuments(
      texts: List[String],
      chunkSize: Int,
      rc: RequestConfig
  ): F[Vector[Embedding]] =
    if (texts.isEmpty) Monad[F].pure(Vector.empty)
    else {
      val batches = texts.sliding(chunkSize, chunkSize)
      batches.toList
        .parTraverse(batch => embedWithRetry(batch, rc))
        .map(vectors => vectors.reduce(_ ++ _))
    }

  private def embedWithRetry(texts: List[String], rc: RequestConfig): F[Vector[Embedding]] =
    retryingOnAllErrors[EmbeddingResult](
      policy = limitRetries[F](config.maxRetries) join exponentialBackoff[F](config.backoff),
      onError = logError
    )(oaiClient.createEmbeddings(EmbeddingRequest(rc.model.name, texts, rc.user.asString)))
      .map(_.data.map(result => Embedding(result.embedding)))

  private def logError(err: Throwable, details: RetryDetails): F[Unit] = details match {
    case WillDelayAndRetry(nextDelay: FiniteDuration, retriesSoFar: Int, cumulativeDelay: FiniteDuration) =>
      logger.warn(s"Open AI call failed. So far we have retried $retriesSoFar times.")
    case GivingUp(totalRetries: Int, totalDelay: FiniteDuration) =>
      logger.warn(s"Open AI call failed. Giving up after $totalRetries retries")
  }
