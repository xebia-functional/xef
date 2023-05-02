package com.xebia.functional.chains.combine

import arrow.core.Either
import com.xebia.functional.Document
import com.xebia.functional.chains.Chain
import com.xebia.functional.chains.LLMChain
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate

interface CombineDocumentsChain : Chain {
    suspend fun combineDocs(documents: List<Document>): Map<String, String>
}

@Suppress("LongParameterList")
suspend fun CombineDocumentsChain(
    llm: OpenAIClient,
    promptTemplate: PromptTemplate,
    documents: List<Document>,
    documentVariableName: String,
    outputVariable: String,
    chainOutput: Chain.ChainOutput = Chain.ChainOutput.OnlyOutput
): CombineDocumentsChain = object : CombineDocumentsChain {

    private val inputKeys = promptTemplate.inputKeys.toSet() - setOf(documentVariableName)
    private val outputKeys = setOf("answer")

    override val config: Chain.Config = Chain.Config(inputKeys, outputKeys, chainOutput)

    override suspend fun combineDocs(documents: List<Document>): Map<String, String> {
        val mergedDocs = documents.joinToString("\n") { it.content }
        return mapOf(documentVariableName to mergedDocs)
    }

    override suspend fun call(inputs: Map<String, String>): Either<Chain.Error, Map<String, String>> {
        val llmChain = LLMChain(
            llm = llm,
            promptTemplate = promptTemplate,
            outputVariable = outputVariable,
            chainOutput = chainOutput)

        val documentInput = combineDocs(documents)
        val totalInputs = documentInput + inputs
        return llmChain.run(totalInputs)
    }
}
