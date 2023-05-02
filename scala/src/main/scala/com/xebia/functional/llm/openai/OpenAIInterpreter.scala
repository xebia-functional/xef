package com.xebia.functional.llm.openai

import java.time.Duration

import scala.jdk.CollectionConverters._

import cats.effect.Sync
import cats.syntax.all.*

import com.theokanning.openai.service.OpenAiService
import com.xebia.functional.config.OpenAIConfig
import com.xebia.functional.llm.models.OpenAIRequest
import com.xebia.functional.llm.models.*
import com.xebia.functional.llm.openai.models.*

class OpenAIClientInterpreter[F[_]: Sync](val config: OpenAIConfig) extends OpenAIClient[F]:

  private val service = new OpenAiService(config.token, Duration.ofSeconds(30))

  def generate(request: OpenAIRequest): F[List[LLMResult]] =
    Sync[F]
      .delay(
        service
          .createCompletion(request.asJava)
          .getChoices
          .asScala
          .toList
          .map(r => LLMResult(r.getText()))
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
