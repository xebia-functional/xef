package com.xebia.functional.chains.models

import cats.ApplicativeThrow
import cats.syntax.all.*

import com.xebia.functional.chains.models.InvalidChainInputsError

final case class Config(
    inputKeys: Set[String],
    outputKeys: Set[String],
    onlyOutputs: Boolean
) {
  def genInputs[F[_]: ApplicativeThrow](inputs: Map[String, String]): F[Map[String, String]] =
    (
      if ((inputKeys diff inputs.keySet).isEmpty) Some(inputs) else None
    ).liftTo[F](InvalidChainInputsError(inputKeys, inputs))

  def genInputsFromString[F[_]: ApplicativeThrow](input: String): F[Map[String, String]] =
    (
      if (inputKeys.size.eqv(1)) Some(inputKeys.map((_, input)).toMap) else None
    ).liftTo[F](InvalidChainInputError(inputKeys))
}
