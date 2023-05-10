package com.xebia.functional.scala.llm.huggingface.models

import cats.syntax.all.*

import ciris.ConfigDecoder

final case class Model(name: String) extends AnyVal

object Model {
  def apply(value: String): Option[Model] =
    if (!value.eqv("")) Model(value)
    else None

  implicit val posIntConfigDecoder: ConfigDecoder[String, Model] =
    ConfigDecoder[String, String].mapOption("Model")(apply)
}
