package com.xebia.functional.scala.chains

import cats.MonadThrow
import cats.syntax.all.*

import com.xebia.functional.scala.chains.models.Config

trait BaseChain[F[_]: MonadThrow]:

  val config: Config

  def call(inputs: Map[String, String]): F[Map[String, String]]

  private def prepareInputs(inputs: Map[String, String] | String): F[Map[String, String]] =
    inputs match
      case e: String => config.genInputsFromString(e)
      case e: Map[String, String] => config.genInputs(e)

  private def prepareOutputs(inputs: Map[String, String])(outputs: Map[String, String]): F[Map[String, String]] =
    MonadThrow[F].pure(if config.onlyOutputs then outputs else inputs ++ outputs)

  def run(inputs: Map[String, String] | String): F[Map[String, String]] =
    prepareInputs(inputs).flatMap(is => call(is) >>= prepareOutputs(is))
