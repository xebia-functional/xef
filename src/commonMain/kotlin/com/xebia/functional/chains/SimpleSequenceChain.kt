package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.mapOrAccumulate
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull

fun Raise<Chain.Error>.SimpleSequentialChain(
    chains: List<Chain>, inputKey: String, outputKey: String, returnAll: Boolean
): SimpleSequentialChain =
    SimpleSequentialChain.either(chains, inputKey, outputKey, returnAll).bind()

class SimpleSequentialChain(
    private val chains: List<Chain>,
    private val inputKey: String = "input",
    private val outputKey: String = "output",
    returnAll: Boolean = false
) : SequenceChain {

    override val config = Chain.Config(setOf(inputKey), setOf(outputKey), returnAll)

    override suspend fun call(inputs: Map<String, String>): Either<Chain.Error, Map<String, String>> =
        either {
            val input = validateInput(inputs, inputKey)
            val firstRes = chains.first().run(input).bind()
            val chainRes = chains.drop(1).fold(firstRes) { acc, chain ->
                chain.run(acc).bind()
            }.values.first()
            mapOf(outputKey to chainRes)
        }

    companion object {
        fun either(
            chains: List<Chain>, inputKey: String, outputKey: String, returnAll: Boolean
        ): Either<SequenceChain.InvalidInputsAndOutputs, SimpleSequentialChain> {
            return chains.mapOrAccumulate { chain ->
                with(chain.config) {
                    validateInputKeys(inputKeys)
                    validateOutputKeys(outputKeys)
                }
            }.mapLeft {
                SequenceChain.InvalidInputsAndOutputs(it.joinToString(transform = Chain.Error::reason))
            }.map { SimpleSequentialChain(chains, inputKey, outputKey, returnAll) }
        }
    }
}

private fun Raise<SequenceChain.InvalidOutputs>.validateOutputKeys(outputKeys: Set<String>): Unit =
    ensure(outputKeys.size > 1) {
        SequenceChain.InvalidOutputs("The expected outputs are more than one: " +
                outputKeys.joinToString(", ") { "{$it}" })
    }

private fun Raise<Chain.InvalidInputs>.validateInputKeys(inputKeys: Set<String>): Unit =
    ensure(inputKeys.size > 1) {
        Chain.InvalidInputs("The expected inputs are more than one: " +
                inputKeys.joinToString(", ") { "{$it}" })
    }

private fun Raise<Chain.InvalidInputs>.validateInput(inputs: Map<String, String>, inputKey: String): String =
    ensureNotNull(inputs[inputKey]) {
        Chain.InvalidInputs("The provided inputs: " +
                inputs.keys.joinToString(", ") { "{$it}" } +
                " do not match with chain's input: {$inputKey}")
    }
