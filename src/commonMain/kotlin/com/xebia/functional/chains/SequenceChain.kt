package com.xebia.functional.chains.sequence

import com.xebia.functional.chains.Chain

interface SequenceChain : Chain {
    data class InvalidOutputs(override val reason: String): Chain.Error(reason)
    data class InvalidKeys(override val reason: String): Chain.Error(reason)
}