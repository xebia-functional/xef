package com.xebia.functional.scala.chains

import cats.MonadThrow
import cats.data.NonEmptySeq
import cats.effect.Resource
import cats.implicits.*

import com.xebia.functional.scala.chains.models.Config
import com.xebia.functional.scala.chains.models.*
import eu.timepit.refined.types.string.NonEmptyString

class SimpleSequentialChain[F[_]: MonadThrow] private (
                                                        chains: NonEmptySeq[Chain[F]],
                                                        inputKey: NonEmptyString,
                                                        outputKey: NonEmptyString,
                                                        onlyOutputs: Boolean
) extends Chain[F]:
  val config: Config = Config(
    inputKeys = Set(inputKey.toString()),
    outputKeys = Set(outputKey.toString()),
    onlyOutputs = onlyOutputs
  )

  def call(inputs: Map[String, String]): F[Map[String, String]] =
    def inner(chains: Seq[Chain[F]])(input: Map[String, String] | String): F[Map[String, String]] =
      chains match
        case h :: Nil =>
          h.run(input).map(response =>
              h.config.outputKeys.foldLeft(response)((resp, output) =>
                val respValue = response(output) // TODO: Unpure! but should never fail. Temporal! Change it.
                response - output + (outputKey.toString -> respValue)
              )
            )
        case h :: t => h.run(input) >>= inner(t)
    inputs.get(inputKey.toString).liftTo[F](InvalidChainInputError(inputs.keySet)).flatMap(inner(chains.toSeq))

object SimpleSequentialChain:
  def make[F[_]: MonadThrow](
                              chains: NonEmptySeq[Chain[F]],
                              inputKey: NonEmptyString,
                              outputKey: NonEmptyString,
                              onlyOutputs: Boolean = false
  ): F[SimpleSequentialChain[F]] =
    chains
      .traverse_(c =>
        if c.config.inputKeys.size > 1 then MonadThrow[F].raiseError(InvalidChainInputError(c.config.inputKeys))
        else if c.config.outputKeys.size > 1 then MonadThrow[F].raiseError(InvalidChainOutputError(c.config.outputKeys))
        else MonadThrow[F].unit
      ).as(SimpleSequentialChain(chains, inputKey, outputKey, onlyOutputs))

  def resource[F[_]: MonadThrow](
                                  chains: NonEmptySeq[Chain[F]],
                                  inputKey: NonEmptyString,
                                  outputKey: NonEmptyString,
                                  onlyOutputs: Boolean = false
  ): Resource[F, SimpleSequentialChain[F]] = Resource.eval(make(chains, inputKey, outputKey, onlyOutputs))
