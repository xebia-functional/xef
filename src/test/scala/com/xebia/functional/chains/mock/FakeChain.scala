package com.xebia.functional.chains.mock

import cats.effect.IO
import cats.implicits.*
import com.xebia.functional.chains.BaseChain
import com.xebia.functional.chains.models.Config
import cats.effect.unsafe.implicits.global

final case class FakeChain(inputVariables: Set[String], outputVariables: Set[String]) extends BaseChain[IO]:
  val config: Config = Config(
    inputKeys = inputVariables,
    outputKeys = outputVariables,
    onlyOutputs = true
  )

  def call(inputs: Map[String, String]): IO[Map[String, String]] =
    val variables = inputVariables.toSeq.traverse(inputs.get).get
    IO.pure(
      outputVariables.foldLeft(Map.empty[String, String])((outputs, outputVar) => outputs + (outputVar -> s"${variables.mkString}foo"))
    )
