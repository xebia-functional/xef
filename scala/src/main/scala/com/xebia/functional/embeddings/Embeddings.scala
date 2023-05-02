package com.xebia.functional.embeddings

import cats.*
import cats.effect.*

import com.xebia.functional.config.OpenAIConfig
import com.xebia.functional.embeddings.models._
import com.xebia.functional.embeddings.openai.models.RequestConfig
import com.xebia.functional.llm.openai.OpenAIClient
import org.typelevel.log4cats.Logger
import retry.*

trait Embeddings[F[_]]:
  def embedDocuments(texts: List[String], chunkSize: Option[Int], config: RequestConfig): F[Vector[Embedding]]

  def embedQuery(text: String, config: RequestConfig): F[Vector[Embedding]]

object Embeddings:
  def buildOpenAI[F[_]: MonadThrow: Sleep: Parallel](aiConfig: OpenAIConfig, aiClient: OpenAIClient[F], logger: Logger[F]): Embeddings[F] =
    new openai.OpenAIEmbeddings[F](aiConfig, aiClient, logger)
