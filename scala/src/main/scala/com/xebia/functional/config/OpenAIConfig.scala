package com.xebia.functional.config

import scala.concurrent.duration._

import cats.syntax.all._

import ciris.*
import eu.timepit.refined.types.string.NonEmptyString

final case class OpenAIConfig(token: String, backoff: FiniteDuration, maxRetries: Int, chunkSize: Int, llmConfig: OpenAIConfigLLM)

object OpenAIConfig:

  def configValue[F[_]]: ConfigValue[F, OpenAIConfig] =
    (
      env("OPENAI_TOKEN").as[String].default(""),
      env("OPENAI_BACKOFF").as[FiniteDuration].default(5.seconds),
      env("OPENAI_MAX_RETRIES").as[Int].default(5),
      env("OPENAI_CHUNK_SIZE").as[Int].default(1000),
      OpenAIConfigLLM.configValue[F]
    ).parMapN(OpenAIConfig.apply)

// TODO: add support for the commented parameters
final case class OpenAIConfigLLM(
    model: String = "text-davinci-003",
    user: String = "testing",
    suffix: Option[String] = None,
    maxTokens: Option[Int] = None,
    temperature: Option[Double] = Some(0.0),
    topP: Option[Double] = None,
    n: Option[Int] = Some(1),
    stream: Option[Boolean] = None,
    logprobs: Option[Int] = None,
    echo: Option[Boolean] = Some(false),
    // stop: Option[List[String]],
    presencePenalty: Option[Double] = None,
    frequencyPenalty: Option[Double] = None,
    bestOf: Option[Int] = None
    // logitBias: Option[Map[String, Int]]
)

object OpenAIConfigLLM:
  def configValue[F[_]]: ConfigValue[F, OpenAIConfigLLM] =
    (
      env("OPENAI_LLM_MODEL").as[String].default("text-davinci-003"),
      env("OPENAI_LLM_USER").as[String].default("testing"),
      env("OPENAI_LLM_SUFFIX").as[String].option,
      env("OPENAI_LLL_MAXTOKENS").as[Int].option,
      env("OPENAI_LLM_TEMPERATURE").as[Double].option.default(Some(0.0)),
      env("OPENAI_LLM_TOPP").as[Double].option,
      env("OPENAI_LLM_N").as[Int].option.default(Some(1)),
      env("OPENAI_LLM_STREAM").as[Boolean].option,
      env("OPENAI_LLM_LOGPROBS").as[Int].option,
      env("OPENAI_LLM_ECHO").as[Boolean].option.default(Some(false)),
      // env("OPENAI_LLM_STOP").as[List[String]].option,
      env("OPENAI_LLM_PRESENCEPENALTY").as[Double].option,
      env("OPENAI_LLM_FREQUENCEPENALTY").as[Double].option,
      env("OPENAI_LLM_BESTOF").as[Int].option
      // env("OPENAI_LLM_LOGITBIAS").as[Map[String, Int]].option
    ).parMapN(OpenAIConfigLLM.apply)
