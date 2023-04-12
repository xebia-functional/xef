package com.xebia.functional.config

import scala.concurrent.duration._

import cats.syntax.all._

import ciris.*
import eu.timepit.refined.types.string.NonEmptyString

final case class OpenAIConfig(token: String, backoff: FiniteDuration, maxRetries: Int, chunkSize: Int)

object OpenAIConfig:

  def configValue[F[_]]: ConfigValue[F, OpenAIConfig] =
    (
      env("OPENAI_TOKEN").as[String].default(""),
      env("OPENAI_BACKOFF").as[FiniteDuration].default(5.seconds),
      env("OPENAI_MAX_RETRIES").as[Int].default(5),
      env("OPENAI_CHUNK_SIZE").as[Int].default(1000)
    ).parMapN(OpenAIConfig.apply)
