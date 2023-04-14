package com.xebia.functional.chains

import cats.MonadThrow
import cats.syntax.all.*

import com.xebia.functional.chains.models.Config

trait BaseChain[F[_]: MonadThrow]:
  def call(inputs: Map[String, String]): F[Map[String, String]]
  def run(inputs: Map[String, String] | String): F[Map[String, String]]
  def prepareInputs(inputs: Map[String, String] | String): F[Map[String, String]]
  def prepareOutputs(inputs: Map[String, String], outputs: Map[String, String]): F[Map[String, String]]
