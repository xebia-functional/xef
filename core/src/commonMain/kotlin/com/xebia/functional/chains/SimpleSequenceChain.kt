package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.recover
import arrow.core.raise.zipOrAccumulate
import com.xebia.functional.AIError
import com.xebia.functional.AIError.Chain.InvalidInputs
import com.xebia.functional.AIError.Chain.Sequence.InvalidKeys

fun Raise<AIError.Chain>.SimpleSequenceChain(
  chains: List<StringMapChain>,
  inputKey: String = "input",
  outputKey: String = "output",
  chainOutput: StringMapChain.ChainOutput = StringMapChain.ChainOutput.OnlyOutput
): SimpleSequenceChain = SimpleSequenceChain.either(chains, inputKey, outputKey, chainOutput).bind()

class SimpleSequenceChain
private constructor(
  private val chains: List<StringMapChain>,
  private val inputKey: String,
  private val outputKey: String,
  chainOutput: StringMapChain.ChainOutput
) : SequenceChain(chains, listOf(inputKey), listOf(outputKey), chainOutput) {

  override val config = StringMapChain.Config(setOf(inputKey), setOf(outputKey), chainOutput)

  override suspend fun call(
    inputs: Map<String, String>
  ): Either<AIError.Chain, Map<String, String>> = either {
    val input = validateInput(inputs, inputKey)
    val firstRes = chains.first().run(input).bind()
    val chainRes =
      chains.drop(1).fold(firstRes) { acc, chain -> chain.run(acc).bind() }.values.first()
    mapOf(outputKey to chainRes)
  }

  companion object {
    fun either(
      chains: List<StringMapChain>,
      inputKey: String,
      outputKey: String,
      chainOutput: StringMapChain.ChainOutput
    ): Either<InvalidKeys, SimpleSequenceChain> = either {
      val mappedChains: List<StringMapChain> =
        chains.map { chain ->
          recover({
            zipOrAccumulate(
              { validateInputKeys(chain.config.inputKeys) },
              { validateOutputKeys(chain.config.outputKeys) }
            ) { _, _ ->
              chain
            }
          }) {
            raise(InvalidKeys(reason = it.joinToString(transform = AIError.Chain::reason)))
          }
        }
      SimpleSequenceChain(mappedChains, inputKey, outputKey, chainOutput)
    }
  }
}

private fun Raise<AIError.Chain.Sequence.InvalidOutputs>.validateOutputKeys(
  outputKeys: Set<String>
): Unit =
  ensure(outputKeys.size == 1) {
    AIError.Chain.Sequence.InvalidOutputs(
      "The expected outputs are more than one: " + outputKeys.joinToString(", ") { "{$it}" }
    )
  }

private fun Raise<InvalidInputs>.validateInputKeys(inputKeys: Set<String>): Unit =
  ensure(inputKeys.size == 1) {
    InvalidInputs(
      "The expected inputs are more than one: " + inputKeys.joinToString(", ") { "{$it}" }
    )
  }
