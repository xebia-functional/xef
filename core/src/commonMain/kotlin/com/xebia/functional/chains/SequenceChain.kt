package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.flatten
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.recover
import arrow.core.raise.zipOrAccumulate
import com.xebia.functional.AIError
import com.xebia.functional.AIError.Chain.InvalidInputs
import com.xebia.functional.AIError.Chain.InvalidOutputs
import com.xebia.functional.AIError.Chain.Sequence.InvalidKeys

fun Raise<AIError.Chain>.SequenceChain(
  chains: List<StringMapChain>,
  inputVariables: List<String>,
  outputVariables: List<String>,
  chainOutput: StringMapChain.ChainOutput = StringMapChain.ChainOutput.OnlyOutput
): SequenceChain = SequenceChain.either(chains, inputVariables, outputVariables, chainOutput).bind()

open class SequenceChain(
  private val chains: List<StringMapChain>,
  private val inputVariables: List<String>,
  private val outputVariables: List<String>,
  chainOutput: StringMapChain.ChainOutput = StringMapChain.ChainOutput.OnlyOutput
) : StringMapChain {

  override val config = StringMapChain.Config(inputVariables.toSet(), outputVariables.toSet(), chainOutput)

  private val outputs =
    when (chainOutput) {
      StringMapChain.ChainOutput.OnlyOutput -> outputVariables
      StringMapChain.ChainOutput.InputAndOutput -> outputVariables.plus(inputVariables)
    }

  override suspend fun call(
    inputs: Map<String, String>
  ): Either<AIError.Chain, Map<String, String>> = either {
    val chainRes =
      chains.fold(inputs) { inputs0, chain -> chain.run(inputs0).map { inputs0 + it }.bind() }
    chainRes.filter { it.key in outputs }
  }

  companion object {
    fun either(
      chains: List<StringMapChain>,
      inputVariables: List<String>,
      outputVariables: List<String>,
      chainOutput: StringMapChain.ChainOutput
    ): Either<InvalidKeys, SequenceChain> = either {
      val allOutputs = chains.map { it.config.outputKeys }.toSet().flatten()
      val mappedChains: List<StringMapChain> =
        recover({
          zipOrAccumulate(
            { validateSequenceOutputs(outputVariables, allOutputs) },
            { validateInputsOverlapping(inputVariables, allOutputs) },
          ) { _, _ ->
            chains
          }
        }) {
          raise(InvalidKeys(reason = it.joinToString(transform = AIError.Chain::reason)))
        }
      SequenceChain(mappedChains, inputVariables, outputVariables, chainOutput)
    }
  }
}

private fun Raise<InvalidOutputs>.validateSequenceOutputs(
  sequenceOutputs: List<String>,
  chainOutputs: List<String>
): Unit =
  ensure(sequenceOutputs.isNotEmpty() && sequenceOutputs.all { it in chainOutputs }) {
    InvalidOutputs(
      "The provided outputs: " +
        sequenceOutputs.joinToString(", ") { "{$it}" } +
        " do not exist in chains' outputs: " +
        chainOutputs.joinToString { "{$it}" }
    )
  }

private fun Raise<InvalidInputs>.validateInputsOverlapping(
  sequenceInputs: List<String>,
  chainOutputs: List<String>
): Unit =
  ensure(sequenceInputs.isNotEmpty() && sequenceInputs.all { it !in chainOutputs }) {
    InvalidInputs(
      "The provided inputs: " +
        sequenceInputs.joinToString { "{$it}" } +
        " overlap with chain's outputs: " +
        chainOutputs.joinToString { "{$it}" }
    )
  }
