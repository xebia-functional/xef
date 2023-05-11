package com.xebia.functional.scala.prompt

import java.nio.file.Path

import scala.io.Source

import cats.effect.*
import cats.effect.kernel.syntax.all.*
import cats.syntax.all.*

import com.xebia.functional.scala.prompt.models.*

sealed trait PromptTemplate[F[_]]:
  def inputKeys: List[String]
  def format(variables: Map[String, String]): F[String]

object PromptTemplate:

  def apply[F[_]: Sync](config: Config): PromptTemplate[F] =
    new PromptTemplate[F]:
      def inputKeys: List[String] = config.inputVariables

      def format(variables: Map[String, String]): F[String] =
        for
          mergedArgs <- Sync[F].delay(mergePartialAndUserVariables(variables, config.inputVariables))
          formattedTemplate = defaultFormatterMapping(config.templateFormat)(config.template, mergedArgs)
        yield formattedTemplate

      private def mergePartialAndUserVariables(variables: Map[String, String], inputVariables: List[String]): Map[String, String] =
        inputVariables.foldLeft(variables) { case (acc, k) =>
          acc.updatedWith(k)(_.orElse(Some(s"{$k}")))
        }

  def fromTemplate[F[_]: Sync](template: String, inputVariables: List[String]): F[PromptTemplate[F]] =
    Config.make(template, inputVariables).map(apply)

  def fromExamples[F[_]: Sync](
      examples: List[String],
      suffix: String,
      inputVariables: List[String],
      prefix: String
  ): F[PromptTemplate[F]] =
    val separator: String = "\n"
    val template = s"""|$prefix
      |
      |${examples.mkString(separator)}
      |$suffix""".stripMargin
    Config.make(template, inputVariables).map(apply)

  def fromFile[F[_]: Sync](templateFile: Path, inputVariables: List[String])(implicit
      MC: MonadCancel[F, Throwable]
  ): F[PromptTemplate[F]] =
    val fileIO = Sync[F].delay(Source.fromFile(templateFile.toFile))
    fileIO.bracket { source =>
      val template = source.mkString
      Config.make(template, inputVariables).map(apply)
    } { source =>
      Sync[F].delay(source.close()).handleErrorWith(_ => Sync[F].unit)
    }

  private val defaultFormatterMapping: Map[TemplateFormat, (String, Map[String, String]) => String] = Map(
    TemplateFormat.FString -> { (template, variables) =>
      val sortedArgs = variables.toList.sortBy(_._1)
      sortedArgs.foldLeft(template) { case (acc, (k, v)) => acc.replace(s"{$k}", v) }
    }
  )
