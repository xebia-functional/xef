package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.orNull
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.xebia.functional.AIError
import com.xebia.functional.AIError.Chain.InvalidInputs

interface Chain {

  enum class ChainOutput { InputAndOutput, OnlyOutput }

  data class Config(
    val inputKeys: Set<String>,
    val outputKeys: Set<String>,
    val chainOutput: ChainOutput = ChainOutput.OnlyOutput
  ) {
    fun createInputs(
      inputs: String
    ): Either<InvalidInputs, Map<String, String>> =
      either {
        ensure(inputKeys.size == 1) {
          InvalidInputs("The expected inputs are more than one: " +
                  inputKeys.joinToString(", ") { "{$it}" })
        }
        inputKeys.associateWith { inputs }
      }

    fun createInputs(
      inputs: Map<String, String>
    ): Either<InvalidInputs, Map<String, String>> =
      either {
        ensure((inputKeys subtract inputs.keys).isEmpty()) {
          InvalidInputs("The provided inputs: " +
                  inputs.keys.joinToString(", ") { "{$it}" } +
                  " do not match with chain's inputs: " +
                  inputKeys.joinToString(", ") { "{$it}" })
        }
        inputs
      }
  }

  val config: Config

  suspend fun call(inputs: Map<String, String>): Either<AIError.Chain, Map<String, String>>

  suspend fun unsafeCall(inputs: Map<String, String>): Map<String, String> =
    when (val result = call(inputs)) {
      is Either.Right -> result.value
      is Either.Left -> throw Exception(result.value.reason)
    }

  suspend fun run(input: String): Either<AIError.Chain, Map<String, String>> =
    either {
      val preparedInputs = config.createInputs(input).bind()
      val result = call(preparedInputs).bind()
      prepareOutputs(preparedInputs, result)
    }

  suspend fun unsafeRun(input: String): Map<String, String> =
    when (val result = run(input)) {
      is Either.Right -> result.value
      is Either.Left -> throw Exception(result.value.reason)
    }

  suspend fun run(inputs: Map<String, String>): Either<AIError.Chain, Map<String, String>> =
    either {
      val preparedInputs = config.createInputs(inputs).bind()
      val result = call(preparedInputs).bind()
      prepareOutputs(preparedInputs, result)
    }

  suspend fun unsafeRun(inputs: Map<String, String>): Map<String, String> =
    when (val result = run(inputs)) {
      is Either.Right -> result.value
      is Either.Left -> throw Exception(result.value.reason)
    }

  private fun prepareOutputs(
    inputs: Map<String, String>, outputs: Map<String, String>
  ): Map<String, String> =
    when (config.chainOutput) {
      ChainOutput.InputAndOutput -> inputs + outputs
      ChainOutput.OnlyOutput -> outputs
    }
}

fun Raise<InvalidInputs>.validateInput(inputs: Map<String, String>, inputKey: String): String =
  ensureNotNull(inputs[inputKey]) {
    InvalidInputs("The provided inputs: " +
            inputs.keys.joinToString(", ") { "{$it}" } +
            " do not match with chain's input: {$inputKey}")
  }
