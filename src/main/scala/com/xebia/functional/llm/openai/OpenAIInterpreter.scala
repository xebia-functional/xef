package com.xebia.functional.llm.openai

import scala.jdk.CollectionConverters._

import cats.effect.Sync
import cats.syntax.all.*

import com.theokanning.openai.service.OpenAiService
import com.xebia.functional.config.OpenAIConfig
import com.xebia.functional.llm.openai.models.*
import java.time.Duration

class OpenAIClientInterpreter[F[_]: Sync](config: OpenAIConfig) extends OpenAIClient[F]:

  private val service = new OpenAiService(config.token, Duration.ofSeconds(30))

  def createCompletion(request: CompletionRequest): F[List[CompletionChoice]] =
    Sync[F]
      .delay(
        service
          .createCompletion(request.asJava)
          .getChoices
          .asScala
          .toList
          .map(CompletionChoice.fromJava)
      )
      .adaptErr(toOpenAIError)

  def createEmbeddings(request: EmbeddingRequest): F[EmbeddingResult] =
    Sync[F]
      .delay(
        EmbeddingResult.fromJava(
          service.createEmbeddings(request.asJava)
        )
      )
      .adaptErr(toOpenAIError)

  def toOpenAIError: PartialFunction[Throwable, Throwable] = { case e: Throwable =>
    OpenAIError(Option(e.getMessage()).getOrElse("<null>"))
  }
