package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.*

fun Raise<Chain.Error>.SimpleSequentialChain(
    chains: List<Chain>, inputKey: String = "input", outputKey: String = "output", returnAll: Boolean = false
): SimpleSequentialChain =
    SimpleSequentialChain.either(chains, inputKey, outputKey, returnAll).bind()

class SimpleSequentialChain private constructor(
    private val chains: List<Chain>,
    private val inputKey: String,
    private val outputKey: String,
    returnAll: Boolean
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
        ): Either<SequenceChain.InvalidKeys, SimpleSequentialChain> =
            either {
                chains.map { chain ->
                    either<NonEmptyList<Chain.Error>, Chain> {
                        zipOrAccumulate(
                            { validateInputKeys(chain.config.inputKeys) },
                            { validateOutputKeys(chain.config.outputKeys) }
                        ) { _, _ -> chain }
                    }.bind()
                }
            }.mapLeft {
                SequenceChain.InvalidKeys(it.joinToString(transform = Chain.Error::reason))
            }.map { SimpleSequentialChain(chains, inputKey, outputKey, returnAll) }
    }
}

private fun Raise<SequenceChain.InvalidOutputs>.validateOutputKeys(outputKeys: Set<String>): Unit =
    ensure(outputKeys.size == 1) {
        SequenceChain.InvalidOutputs("The expected outputs are more than one: " +
                outputKeys.joinToString(", ") { "{$it}" })
    }

private fun Raise<Chain.InvalidInputs>.validateInputKeys(inputKeys: Set<String>): Unit =
    ensure(inputKeys.size == 1) {
        Chain.InvalidInputs("The expected inputs are more than one: " +
                inputKeys.joinToString(", ") { "{$it}" })
    }

private fun Raise<Chain.InvalidInputs>.validateInput(inputs: Map<String, String>, inputKey: String): String =
    ensureNotNull(inputs[inputKey]) {
        Chain.InvalidInputs("The provided inputs: " +
                inputs.keys.joinToString(", ") { "{$it}" } +
                " do not match with chain's input: {$inputKey}")
    }
