package com.xebia.functional.chains.models

import cats.ApplicativeThrow
import cats.syntax.all.*

final case class Config(
    inputKeys: Set[String],
    outputKeys: Set[String],
    onlyOutputs: Boolean
) {

  private def xor[A](set1: Set[A], set2: Set[A]) = (set1 ++ set2) -- (set1 intersect set2)

  def genInputs[F[_]: ApplicativeThrow](inputs: Map[String, String]): F[Map[String, String]] =
    (if xor(inputKeys, inputs.keySet).isEmpty then Some(inputs) else None).liftTo[F](InvalidChainInputsError(inputKeys, inputs))

  def genInputsFromString[F[_]: ApplicativeThrow](input: String): F[Map[String, String]] =
    (
      if inputKeys.size.eqv(1) then Some(inputKeys.map((_, input)).toMap) else None
    ).liftTo[F](InvalidChainInputError(inputKeys))
}
