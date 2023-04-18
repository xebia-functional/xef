package com.xebia.functional.chains

import cats.MonadThrow
import cats.implicits.*
import cats.data.NonEmptySeq

class SimpleSequentialChain[F[_]: MonadThrow] private (chains: NonEmptySeq[BaseChain[F]]):
  def run(inputs: Map[String, String]): F[Map[String, String]] =
    chains.foldLeft(MonadThrow[F].pure(inputs))((i, c) => i >>= c.run)

object SimpleSequentialChain:
  def make[F[_]: MonadThrow](chains: NonEmptySeq[BaseChain[F]]): SimpleSequentialChain[F] = SimpleSequentialChain(chains)
