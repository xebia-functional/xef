package com.xebia.functional.chains

import arrow.core.raise.Raise

interface BaseChain {

    val config: Config

    suspend fun call(inputs: Map<String, String>): Map<String, String>

    suspend fun Raise<InvalidChainInputs>.run(input: String): Map<String, String> {
        val preparedInputs = prepareInputs(input)
        val result = call(preparedInputs)
        return prepareOutputs(preparedInputs, result)
    }

    suspend fun Raise<InvalidChainInputs>.run(inputs: Map<String, String>): Map<String, String> {
        val preparedInputs = prepareInputs(inputs)
        val result = call(preparedInputs)
        return prepareOutputs(preparedInputs, result)
    }

    private suspend fun Raise<InvalidChainInputs>.prepareInputs(
        inputs: String
    ): Map<String, String> =
        config.genInputsFromString(inputs).bind()

    private suspend fun Raise<InvalidChainInputs>.prepareInputs(
        inputs: Map<String, String>
    ): Map<String, String> =
        config.genInputs(inputs).bind()

    private suspend fun prepareOutputs(
        inputs: Map<String, String>, outputs: Map<String, String>
    ): Map<String, String> =
        if (config.onlyOutputs) outputs else inputs + outputs
}
