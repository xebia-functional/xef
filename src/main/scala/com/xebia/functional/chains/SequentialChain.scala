package com.xebia.functional.chains

import cats.MonadThrow
import cats.implicits.*
import cats.data.NonEmptySeq

class SequentialChain[F[_]: MonadThrow](
    chains: NonEmptySeq[BaseChain[F]]
):
  def run(inputs: Map[String, String]): F[Map[String, String]] =
    chains.foldLeft(MonadThrow[F].pure(inputs))((fi, c) => fi.flatMap(i => c.run(i).map(_ ++ i)))
