package com.xebia.functional.llm.models

import java.util.{List => JList}
import java.util.{Map => JMap}

import scala.jdk.CollectionConverters._

import cats.ApplicativeThrow
import cats.syntax.all.*

import com.theokanning.openai.completion.{CompletionRequest => JCompletionRequest}
import com.xebia.functional.config.HuggingFaceConfig
import com.xebia.functional.config.OpenAIConfig
import com.xebia.functional.llm.LLM
import com.xebia.functional.llm.huggingface.HuggingFaceClient
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.llm.openai.models.OpenAIError
import io.circe.Encoder

final case class HFRequest(inputs: String, maxLength: Int = 1000) derives Encoder.AsObject

object HFRequest:
  def fromPrompt[F[_]: ApplicativeThrow](prompt: String, config: HuggingFaceConfig): F[HFRequest] =
    ApplicativeThrow[F]
      .catchNonFatal(HFRequest(prompt, config.maxLength))
      .adaptErr { case e: Throwable =>
        PromptGenerationError(Option(e.getMessage).getOrElse("<null>"))
      }

final case class OpenAIRequest private (
    model: String,
    user: String,
    prompt: Option[String],
    suffix: Option[String],
    maxTokens: Option[Int],
    temperature: Option[Double],
    topP: Option[Double],
    n: Option[Int],
    stream: Option[Boolean],
    logprobs: Option[Int],
    echo: Option[Boolean],
    stop: Option[List[String]],
    presencePenalty: Option[Double],
    frequencyPenalty: Option[Double],
    bestOf: Option[Int],
    logitBias: Option[Map[String, Int]]
)

object OpenAIRequest:

  def builder(model: String, user: String): Builder = Builder(model, user)

  final case class Builder(
      model: String,
      user: String,
      prompt: Option[String] = None,
      suffix: Option[String] = None,
      maxTokens: Option[Int] = None,
      temperature: Option[Double] = None,
      topP: Option[Double] = None,
      n: Option[Int] = None,
      stream: Option[Boolean] = None,
      logprobs: Option[Int] = None,
      echo: Option[Boolean] = None,
      stop: Option[List[String]] = None,
      presencePenalty: Option[Double] = None,
      frequencyPenalty: Option[Double] = None,
      bestOf: Option[Int] = None,
      logitBias: Option[Map[String, Int]] = None
  ) {
    def withPrompt(prompt: String): Builder = copy(prompt = Some(prompt))
    def withSuffix(suffix: String): Builder = copy(suffix = Some(suffix))
    def withMaxTokens(maxTokens: Int): Builder = copy(maxTokens = Some(maxTokens))
    def withTemperature(temperature: Double): Builder = copy(temperature = Some(temperature))
    def withTopP(topP: Double): Builder = copy(topP = Some(topP))
    def withN(n: Int): Builder = copy(n = Some(n))
    def withStream(stream: Boolean): Builder = copy(stream = Some(stream))
    def withLogprobs(logprobs: Int): Builder = copy(logprobs = Some(logprobs))
    def withEcho(echo: Boolean): Builder = copy(echo = Some(echo))
    def withStop(stop: List[String]): Builder = copy(stop = Some(stop))
    def withPresencePenalty(presencePenalty: Double): Builder = copy(presencePenalty = Some(presencePenalty))
    def withFrequencyPenalty(frequencyPenalty: Double): Builder = copy(frequencyPenalty = Some(frequencyPenalty))
    def withBestOf(bestOf: Int): Builder = copy(bestOf = Some(bestOf))
    def withLogitBias(logitBias: Map[String, Int]): Builder = copy(logitBias = Some(logitBias))

    def build(): OpenAIRequest =
      OpenAIRequest(
        model,
        user,
        prompt,
        suffix,
        maxTokens,
        temperature,
        topP,
        n,
        stream,
        logprobs,
        echo,
        stop,
        presencePenalty,
        frequencyPenalty,
        bestOf,
        logitBias
      )
  }

  extension (e: OpenAIRequest)
    def asJava: JCompletionRequest =
      val javaStop: JList[String] = e.stop.map(_.asJava).orNull
      val javaLogitBias: JMap[String, Integer] = e.logitBias.map(_.map { case (k, v) => k -> int2Integer(v) }.asJava).orNull
      new JCompletionRequest(
        e.model,
        e.prompt.orNull,
        e.suffix.orNull,
        e.maxTokens.map(Integer.valueOf).orNull,
        e.temperature.map(java.lang.Double.valueOf).orNull,
        e.topP.map(java.lang.Double.valueOf).orNull,
        e.n.map(Integer.valueOf).orNull,
        e.stream.map(java.lang.Boolean.valueOf).orNull,
        e.logprobs.map(Integer.valueOf).orNull,
        e.echo.map(java.lang.Boolean.valueOf).orNull,
        javaStop,
        e.presencePenalty.map(java.lang.Double.valueOf).orNull,
        e.frequencyPenalty.map(java.lang.Double.valueOf).orNull,
        e.bestOf.map(Integer.valueOf).orNull,
        javaLogitBias,
        e.user
      )

  def fromPrompt[F[_]: ApplicativeThrow](prompt: String, config: OpenAIConfig): F[OpenAIRequest] =
    ApplicativeThrow[F]
      .catchNonFatal {
        OpenAIRequest
          .Builder(
            config.llmConfig.model,
            config.llmConfig.user,
            Some(prompt),
            config.llmConfig.suffix,
            config.llmConfig.maxTokens,
            config.llmConfig.temperature,
            config.llmConfig.topP,
            config.llmConfig.n,
            config.llmConfig.stream,
            config.llmConfig.logprobs,
            config.llmConfig.echo,
            None,
            config.llmConfig.presencePenalty,
            config.llmConfig.frequencyPenalty,
            config.llmConfig.bestOf,
            None
          ).build()
      }
      .adaptErr { case e: Throwable =>
        PromptGenerationError(Option(e.getMessage).getOrElse("<null>"))
      }

end OpenAIRequest
