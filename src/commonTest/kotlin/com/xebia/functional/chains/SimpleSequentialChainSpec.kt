package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.raise.either
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class SimpleSequentialChainSpec : StringSpec({
    "SimpleSequenceChain should return a prediction with one chain" {
        val chain1 = FakeChain(inputVariables = setOf("foo"), outputVariables = setOf("bar"))
        val chains = listOf(chain1)

        either {
            val ssc = SimpleSequentialChain(chains = chains, returnAll = true)
            ssc.run(mapOf("input" to "123")).bind()
        } shouldBeRight mapOf("input" to "123", "output" to "123dr")
    }

    "SimpleSequenceChain should return a prediction with more than one chain" {
        val chain1 = FakeChain(inputVariables = setOf("foo"), outputVariables = setOf("bar"))
        val chain2 = FakeChain(inputVariables = setOf("bar"), outputVariables = setOf("baz"))
        val chain3 = FakeChain(inputVariables = setOf("baz"), outputVariables = setOf("dre"))
        val chains = listOf(chain1, chain2, chain3)

        either {
            val ssc = SimpleSequentialChain(chains = chains, returnAll = true)
            ssc.run(mapOf("input" to "123")).bind()
        } shouldBeRight mapOf("input" to "123", "output" to "123drdrdr")
    }

    "SimpleSequentialChain should fail if multiple input variables are expected" {
        val chain1 = FakeChain(inputVariables = setOf("foo"), outputVariables = setOf("bar"))
        val chain2 = FakeChain(inputVariables = setOf("bar", "foo"), outputVariables = setOf("baz"))
        val chains = listOf(chain1, chain2)

        either {
            val ssc = SimpleSequentialChain(chains = chains, returnAll = true)
            ssc.run(mapOf("input" to "123")).bind()
        } shouldBeLeft SequenceChain.InvalidKeys("The expected inputs are more than one: {bar}, {foo}")
    }

    "SimpleSequentialChain should fail if multiple output variables are expected" {
        val chain1 = FakeChain(inputVariables = setOf("foo"), outputVariables = setOf("bar", "foo"))
        val chain2 = FakeChain(inputVariables = setOf("bar"), outputVariables = setOf("baz"))
        val chains = listOf(chain1, chain2)

        either {
            val ssc = SimpleSequentialChain(chains = chains, returnAll = true)
            ssc.run(mapOf("input" to "123")).bind()
        } shouldBeLeft SequenceChain.InvalidKeys("The expected outputs are more than one: {bar}, {foo}")
    }
})

data class FakeChain(private val inputVariables: Set<String>, private val outputVariables: Set<String>) : Chain {
    override val config: Chain.Config = Chain.Config(
        inputKeys = inputVariables,
        outputKeys = outputVariables,
        returnAll = false
    )

    override suspend fun call(inputs: Map<String, String>): Either<Chain.InvalidInputs, Map<String, String>> =
        either {
            val variables = inputVariables.map { inputs[it] }.requireNoNulls()
            outputVariables.fold(emptyMap()) { outputs, outputVar ->
                outputs + (outputVar to "${variables.joinToString(separator = "")}dr")
            }
        }
}
