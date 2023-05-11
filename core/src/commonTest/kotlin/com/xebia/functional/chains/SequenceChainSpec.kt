package com.xebia.functional.chains

import arrow.core.raise.either
import com.xebia.functional.AIError
import com.xebia.functional.AIError.Chain.Sequence.InvalidKeys
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class SequenceChainSpec :
  StringSpec({
    "SequenceChain should return a prediction with one Chain" {
      val chain1 = FakeChain(inputVariables = setOf("foo"), outputVariables = setOf("bar"))
      val chains = listOf(chain1)

      either {
        val sc =
          SequenceChain(
            chains = chains,
            inputVariables = listOf("foo"),
            outputVariables = listOf("bar"),
            chainOutput = Chain.ChainOutput.InputAndOutput
          )
        sc.run(mapOf("foo" to "123")).bind()
      } shouldBeRight mapOf("foo" to "123", "bar" to "123dr")
    }

    "SequenceChain should return a prediction on a single input chain" {
      val chain1 = FakeChain(inputVariables = setOf("foo"), outputVariables = setOf("bar"))
      val chain2 = FakeChain(inputVariables = setOf("bar"), outputVariables = setOf("baz"))
      val chains = listOf(chain1, chain2)

      either {
        val sc =
          SequenceChain(
            chains = chains,
            inputVariables = listOf("foo"),
            outputVariables = listOf("baz"),
            chainOutput = Chain.ChainOutput.InputAndOutput
          )
        sc.run(mapOf("foo" to "123")).bind()
      } shouldBeRight mapOf("foo" to "123", "baz" to "123drdr")
    }

    "SequenceChain should return a prediction on a multiple input chain" {
      val chain1 = FakeChain(inputVariables = setOf("foo", "test"), outputVariables = setOf("bar"))
      val chain2 = FakeChain(inputVariables = setOf("bar", "foo"), outputVariables = setOf("baz"))
      val chains = listOf(chain1, chain2)

      either {
        val sc =
          SequenceChain(
            chains = chains,
            inputVariables = listOf("foo", "test"),
            outputVariables = listOf("baz"),
            chainOutput = Chain.ChainOutput.InputAndOutput
          )
        sc.run(mapOf("foo" to "123", "test" to "456")).bind()
      } shouldBeRight mapOf("foo" to "123", "test" to "456", "baz" to "123456dr123dr")
    }

    "SequenceChain should return a prediction on a multiple output chain" {
      val chain1 = FakeChain(inputVariables = setOf("foo"), outputVariables = setOf("bar", "test"))
      val chain2 = FakeChain(inputVariables = setOf("bar", "foo"), outputVariables = setOf("baz"))
      val chains = listOf(chain1, chain2)

      either {
        val sc =
          SequenceChain(
            chains = chains,
            inputVariables = listOf("foo"),
            outputVariables = listOf("baz"),
            chainOutput = Chain.ChainOutput.InputAndOutput
          )
        sc.run(mapOf("foo" to "123")).bind()
      } shouldBeRight mapOf("foo" to "123", "baz" to "123dr123dr")
    }

    "SequenceChain should fail when input variables are missing" {
      val chain1 = FakeChain(inputVariables = setOf("foo"), outputVariables = setOf("bar"))
      val chain2 = FakeChain(inputVariables = setOf("bar", "test"), outputVariables = setOf("baz"))
      val chains = listOf(chain1, chain2)

      either {
        val sc =
          SequenceChain(
            chains = chains,
            inputVariables = listOf("foo"),
            outputVariables = listOf("baz"),
            chainOutput = Chain.ChainOutput.InputAndOutput
          )
        sc.run(mapOf("foo" to "123")).bind()
      } shouldBeLeft
        AIError.Chain.InvalidInputs(
          "The provided inputs: {foo}, {bar} do not match with chain's inputs: {bar}, {test}"
        )
    }

    "SequenceChain should fail when output variables are missing" {
      val chain1 = FakeChain(inputVariables = setOf("foo"), outputVariables = setOf("bar"))
      val chain2 = FakeChain(inputVariables = setOf("bar"), outputVariables = setOf("baz"))
      val chains = listOf(chain1, chain2)

      either {
        val sc =
          SequenceChain.either(
              chains = chains,
              inputVariables = listOf("foo"),
              outputVariables = listOf("test"),
              chainOutput = Chain.ChainOutput.InputAndOutput
            )
            .bind()
        sc.run(mapOf("foo" to "123")).bind()
      } shouldBeLeft
        InvalidKeys("The provided outputs: {test} do not exist in chains' outputs: {bar}, {baz}")
    }

    "SequenceChain should fail when input variables are overlapping" {
      val chain1 = FakeChain(inputVariables = setOf("foo"), outputVariables = setOf("bar", "test"))
      val chain2 = FakeChain(inputVariables = setOf("bar"), outputVariables = setOf("baz"))
      val chains = listOf(chain1, chain2)

      either {
        val sc =
          SequenceChain.either(
              chains = chains,
              inputVariables = listOf("foo", "test"),
              outputVariables = listOf("baz"),
              chainOutput = Chain.ChainOutput.InputAndOutput
            )
            .bind()
        sc.run(mapOf("foo" to "123")).bind()
      } shouldBeLeft
        InvalidKeys(
          "The provided inputs: {foo}, {test} overlap with chain's outputs: {bar}, {test}, {baz}"
        )
    }

    "SequenceChain should fail when output variables are missing and input variables are overlapping" {
      val chain1 = FakeChain(inputVariables = setOf("foo"), outputVariables = setOf("bar"))
      val chain2 = FakeChain(inputVariables = setOf("bar"), outputVariables = setOf("baz"))
      val chains = listOf(chain1, chain2)

      either {
        val sc =
          SequenceChain.either(
              chains = chains,
              inputVariables = listOf("foo", "bar"),
              outputVariables = listOf("potato"),
              chainOutput = Chain.ChainOutput.InputAndOutput
            )
            .bind()
        sc.run(mapOf("foo" to "123")).bind()
      } shouldBeLeft
        InvalidKeys(
          "The provided outputs: {potato} do not exist in chains' outputs: {bar}, {baz}, " +
            "The provided inputs: {foo}, {bar} overlap with chain's outputs: {bar}, {baz}"
        )
    }
  })
