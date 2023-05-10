package com.xebia.functional.scala.config

import cats.syntax.all._

import ciris.*

final case class Config(openAI: OpenAIConfig, huggingFace: HuggingFaceConfig, dbConfig: DBConfig)

object Config:

  def configValue[F[_]]: ConfigValue[F, Config] =
    (
      OpenAIConfig.configValue[F],
      HuggingFaceConfig.configValue[F],
      DBConfig.configValue[F]
    ).parMapN(Config.apply)
