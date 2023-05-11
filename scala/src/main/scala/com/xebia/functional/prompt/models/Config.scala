package com.xebia.functional.scala.prompt.models

import cats.*
import cats.data.*
import cats.effect.*
import cats.syntax.all.*

final case class Config private (
    inputVariables: List[String],
    template: String,
    templateFormat: TemplateFormat = TemplateFormat.FString
)

object Config:

  def make[F[_]: Sync](template: String, inputVariables: List[String]): F[Config] =
    apply(template, inputVariables)

  private def apply[F[_]: Sync](template: String, inputVariables: List[String]): F[Config] =
    val placeholders = placeholderValues(template)

    val errors =
      validate(template, inputVariables.toSet -- placeholders.toSet, "unused")
        .combine(
          validate(template, placeholders.toSet -- inputVariables.toSet, "missing")
        )
        .combine(
          validateDuplicated(template, placeholders)
        )

    errors.leftMap(_.map(_.getMessage).reduceLeft(_ + "; " + _)) match {
      case Validated.Invalid(error) => Sync[F].raiseError(new InvalidTemplateError(error))
      case Validated.Valid(_) => Sync[F].pure(Config(inputVariables, template))
    }

  private def validate(template: String, diffSet: Set[String], msg: String): ValidatedNel[InvalidTemplateError, Unit] =
    diffSet match {
      case s if s.isEmpty => Validated.valid(())
      case args =>
        Validated.invalidNel(InvalidTemplateError(s"Template '$template' has $msg arguments: ${args.map(idx => s"{$idx}").mkString(", ")}"))
    }

  private def validateDuplicated(template: String, placeholders: List[String]): ValidatedNel[InvalidTemplateError, Unit] =
    val args = placeholders.groupBy(identity).collect { case (x, List(_, _, _*)) => x }
    if (args.nonEmpty)
      Validated.invalidNel(InvalidTemplateError(s"Template '$template' has duplicate arguments: ${args.map(idx => s"{$idx}").mkString(", ")}"))
    else Validated.valid(())

  private def placeholderValues(template: String): List[String] =
    val regex = """\{([^\{\}]+)\}""".r
    regex.findAllMatchIn(template).toList.map(_.group(1))
