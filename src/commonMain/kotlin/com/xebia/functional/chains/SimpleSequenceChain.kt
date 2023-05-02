package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.raise.zipOrAccumulate

fun Raise<Chain.Error>.SimpleSequentialChain(
    chains: List<Chain>,
    inputKey: String = "input",
    outputKey: String = "output",
    chainOutput: Chain.ChainOutput = Chain.ChainOutput.OnlyOutput
): SimpleSequentialChain =
    SimpleSequentialChain.either(chains, inputKey, outputKey, chainOutput).bind()

class SimpleSequentialChain private constructor(
    private val chains: List<Chain>,
    private val inputKey: String,
    private val outputKey: String,
    chainOutput: Chain.ChainOutput
) : SequenceChain {

    override val config = Chain.Config(setOf(inputKey), setOf(outputKey), chainOutput)

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
            chains: List<Chain>, inputKey: String, outputKey: String, chainOutput: Chain.ChainOutput
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
            }.map { SimpleSequentialChain(chains, inputKey, outputKey, chainOutput) }
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
