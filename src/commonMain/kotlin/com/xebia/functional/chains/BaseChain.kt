package com.xebia.functional.chains

interface BaseChain {
    suspend fun call(inputs: Map<String, String>): Map<String, String>
    suspend fun run(inputs: String)
    suspend fun run(inputs: Map<String, String>)
    suspend fun prepareInputs(inputs: String)
    suspend fun prepareInputs(inputs: Map<String, String>)
    suspend fun prepareOutputs(inputs: Map<String, String>, outputs: Map<String, String>)
}
