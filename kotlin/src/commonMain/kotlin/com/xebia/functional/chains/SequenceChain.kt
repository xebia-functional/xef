package com.xebia.functional.chains

interface SequenceChain : Chain {
    data class InvalidOutputs(override val reason: String): Chain.Error(reason)
    data class InvalidKeys(override val reason: String): Chain.Error(reason)
}
