package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.raise.either
import com.xebia.functional.llm.openai.CompletionChoice
import com.xebia.functional.llm.openai.CompletionRequest
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate

@Suppress("LongParameterList")
suspend fun LLMChain(
    llm: OpenAIClient,
    promptTemplate: PromptTemplate,
    llmModel: String = "text-davinci-003",
    user: String = "testing",
    echo: Boolean = false,
    n: Int = 1,
    temperature: Double = 0.0,
    outputVariable: String,
    chainOutput: Chain.ChainOutput = Chain.ChainOutput.OnlyOutput
): Chain = object : Chain {

    private val inputKeys: Set<String> = promptTemplate.inputKeys.toSet()
    private val outputKeys: Set<String> = setOf(outputVariable)

    override val config: Chain.Config = Chain.Config(inputKeys, outputKeys, chainOutput)

    override suspend fun call(inputs: Map<String, String>): Either<Chain.InvalidInputs, Map<String, String>> =
        either {
            val prompt = promptTemplate.format(inputs)

            val request = CompletionRequest(
                model = llmModel,
                user = user,
                prompt = prompt,
                echo = echo,
                n = n,
                temperature = temperature,
            )

            val completions = llm.createCompletion(request)
            formatOutput(completions)
        }

    private fun formatOutput(completions: List<CompletionChoice>): Map<String, String> =
        config.outputKeys.associateWith {
            completions.joinToString(", ") { it.text }
        }
}
