package com.xebia.functional.chains

import cats.MonadThrow

import com.xebia.functional.chains.models.Config

trait BaseChain[F[_]: MonadThrow]:
  def call(inputs: Map[String, String]): F[Map[String, String]]
  def run(inputs: Map[String, String] | String): F[Map[String, String]]

  def prepareInputs(inputs: Map[String, String] | String)(config: Config): F[Map[String, String]] =
    inputs match
      case e: String => config.genInputsFromString(e)
      case e: Map[String, String] => config.genInputs(e)

  def prepareOutputs(inputs: Map[String, String], outputs: Map[String, String])(config: Config): F[Map[String, String]] =
    MonadThrow[F].pure(
      config.onlyOutputs match
        case true => outputs
        case false => inputs ++ outputs
    )
