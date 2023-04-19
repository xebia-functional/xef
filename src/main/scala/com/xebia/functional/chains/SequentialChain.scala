package com.xebia.functional.chains

import cats.MonadThrow
import cats.implicits.*
import cats.effect.kernel.Resource
import cats.data.{NonEmptySeq, NonEmptySet}
import eu.timepit.refined.types.string.NonEmptyString
import com.xebia.functional.chains.models.*

class SequentialChain[F[_]: MonadThrow] private (
    chains: NonEmptySeq[BaseChain[F]],
    inputVariables: NonEmptySet[NonEmptyString],
    outputVariables: NonEmptySet[NonEmptyString]
):
  def run(inputs: Map[String, String]): F[Map[String, String]] =
    chains
      .foldLeft(MonadThrow[F].pure(inputs))((fi, c) => fi.flatMap(i => c.run(i).map(_ ++ i)))
      .map(_.filterKeys(outputVariables.map(_.toString()).contains_(_)).toMap)

object SequentialChain:
  private def validateChains[F[_]: MonadThrow](
      chains: NonEmptySeq[BaseChain[F]],
      inputVariables: NonEmptySet[NonEmptyString],
      outputVariables: NonEmptySet[NonEmptyString]
  ): F[SequentialChain[F]] =
    // known vars are, initially, the input vars + memory vars
    val knownVars0 = inputVariables.toSortedSet.map(_.toString) // TODO Add memory vars in the future
    // for each chain...
    val knownVars = chains.foldLeft(MonadThrow[F].pure(knownVars0))((fkv, c) =>
      fkv.flatMap(kv =>
        // extract the input keys
        val inputKeys = c.config.inputKeys
        // search for missing input keys
        val missingVars = inputKeys.diff(kv)
        // extract the output keys
        val outputKeys = c.config.outputKeys
        // search for overlapping vars
        val overlappingKeys = kv.intersect(outputKeys)
        // Different cases
        (missingVars, overlappingKeys) match
          // If there are missing vars but not overlapping => Error
          case (mv, ok) if !mv.isEmpty & ok.isEmpty => MonadThrow[F].raiseError(MissingInputVariablesError(mv, kv))
          // If there are not missing vars but overlapping => Error
          case (mv, ok) if mv.isEmpty & !ok.isEmpty => MonadThrow[F].raiseError(OverlappingInputError(ok))
          // Otherwise, everything it's okay, adding chain output to the known vars
          case (mv, ok) => MonadThrow[F].pure(kv ++ c.config.outputKeys)
      )
    )
    // Having all the knowing vars, search for missing output vars
    knownVars.map(outputVariables.toSortedSet.map(_.toString).diff(_)).flatMap { missingVars =>
      // No missing vars, then return the chains
      if missingVars.isEmpty then MonadThrow[F].pure(SequentialChain(chains, inputVariables, outputVariables))
      // If there are missing output vars, then error
      else MonadThrow[F].raiseError(MissingOutputVariablesError(missingVars))
    }

  def resource[F[_]: MonadThrow](
      chains: NonEmptySeq[BaseChain[F]],
      inputVariables: NonEmptySet[NonEmptyString],
      outputVariables: NonEmptySet[NonEmptyString]
  ): Resource[F, SequentialChain[F]] = Resource.eval(validateChains(chains, inputVariables, outputVariables))
