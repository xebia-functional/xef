package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.raise.either

interface BaseChain {

    val config: Config

    suspend fun call(inputs: Map<String, String>): Map<String, String>

    suspend fun run(input: String): Either<InvalidChainInputs, Map<String, String>> =
        either {
            val preparedInputs = config.genInputsFromString(input).bind()
            val result = call(preparedInputs)
            prepareOutputs(preparedInputs, result)
        }

    suspend fun run(inputs: Map<String, String>): Either<InvalidChainInputs, Map<String, String>> =
        either {
            val preparedInputs = config.genInputs(inputs).bind()
            val result = call(preparedInputs)
            prepareOutputs(preparedInputs, result)
        }

    private suspend fun prepareOutputs(
        inputs: Map<String, String>, outputs: Map<String, String>
    ): Map<String, String> =
        if (config.onlyOutputs) outputs else inputs + outputs
}
