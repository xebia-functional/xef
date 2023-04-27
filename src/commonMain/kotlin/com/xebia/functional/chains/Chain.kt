package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.NonEmptySet
import arrow.core.raise.either
import arrow.core.raise.ensure

interface Chain {
    data class InvalidInputs(val reason: String)

    data class Config(
        val inputKeys: Set<String>,
        val outputKeys: Set<String>,
        val returnAll: Boolean = false
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

    suspend fun call(inputs: Map<String, String>): Either<InvalidInputs, Map<String, String>>

    suspend fun run(input: String): Either<InvalidInputs, Map<String, String>> =
        either {
            val preparedInputs = config.createInputs(input).bind()
            val result = call(preparedInputs).bind()
            prepareOutputs(preparedInputs, result)
        }

    suspend fun run(inputs: Map<String, String>): Either<InvalidInputs, Map<String, String>> =
        either {
            val preparedInputs = config.createInputs(inputs).bind()
            val result = call(preparedInputs).bind()
            prepareOutputs(preparedInputs, result)
        }

    private fun prepareOutputs(
        inputs: Map<String, String>, outputs: Map<String, String>
    ): Map<String, String> =
        if (config.returnAll) inputs + outputs else outputs
}
