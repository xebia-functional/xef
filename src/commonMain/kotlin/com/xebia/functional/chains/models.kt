package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure

data class InvalidChainInputs(val reason: String)

data class Config(
    val inputKeys: Set<String>,
    val outputKeys: Set<String>,
    val onlyOutputs: Boolean
) {
    fun genInputs(
        inputs: Map<String, String>
    ): Either<InvalidChainInputs, Map<String, String>> =
        either {
            ensure((inputKeys subtract inputs.keys).isEmpty()) {
                InvalidChainInputs("The provided inputs: " +
                        inputs.keys.joinToString(", ") { "{$it}" } +
                        " do not match with chain's inputs: " +
                        inputKeys.joinToString(", ") { "{$it}" })
            }
            inputs
        }

    fun genInputsFromString(
        inputs: String
    ): Either<InvalidChainInputs, Map<String, String>> =
        either {
            ensure(inputKeys.size == 1) {
                InvalidChainInputs("The expected inputs are more than one: " +
                        inputKeys.joinToString(", ") { "{$it}" })
            }
            inputKeys.associateWith { inputs }
        }
}
