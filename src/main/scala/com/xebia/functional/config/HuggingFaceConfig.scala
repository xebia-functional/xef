package com.xebia.functional.config

import scala.concurrent.duration._

import cats.syntax.all._

import ciris.*
import ciris.http4s.*
import ciris.refined.*
import eu.timepit.refined.types.string.NonEmptyString
import org.http4s.Uri
import org.http4s.implicits.uri

final case class HuggingFaceConfig(token: String, baseUri: Uri)

object HuggingFaceConfig:

  def configValue[F[_]]: ConfigValue[F, HuggingFaceConfig] =
    (
      env("HF_TOKEN").as[String].default(""),
      env("HF_BASE_URI").as[Uri].default(uri"https://api-inference.huggingface.co")
    ).parMapN(HuggingFaceConfig.apply)
