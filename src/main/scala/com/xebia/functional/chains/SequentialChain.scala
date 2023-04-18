package com.xebia.functional.chains

import cats.MonadThrow
import cats.implicits.*
import cats.data.NonEmptySeq
import eu.timepit.refined.types.string.NonEmptyString

class SequentialChain[F[_]: MonadThrow] private (
    chains: NonEmptySeq[BaseChain[F]],
    outputVariables: NonEmptySeq[NonEmptyString]
):
  def run(inputs: Map[String, String]): F[Map[String, String]] =
    chains
      .foldLeft(MonadThrow[F].pure(inputs))((fi, c) => fi.flatMap(i => c.run(i).map(_ ++ i)))
      .map(_.filterKeys(outputVariables.map(_.toString()).contains_(_)).toMap)

object SequentialChain:
  def make[F[_]: MonadThrow](chains: NonEmptySeq[BaseChain[F]], outputVariables: NonEmptySeq[NonEmptyString]): SequentialChain[F] =
    SequentialChain[F](chains, outputVariables)
