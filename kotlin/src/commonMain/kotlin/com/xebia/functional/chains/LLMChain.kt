package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.raise.either
import com.xebia.functional.llm.openai.*
import com.xebia.functional.llm.openai.LLMModel.Kind.*
import com.xebia.functional.prompt.PromptTemplate

@Suppress("LongParameterList")
suspend fun LLMChain(
    llm: OpenAIClient,
    promptTemplate: PromptTemplate<String>,
    model: LLMModel,
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
            when (model.kind) {
                Completion -> callCompletionEndpoint(prompt)
                Chat -> callChatEndpoint(prompt)
            }
        }

    private suspend fun callCompletionEndpoint(prompt: String): Map<String, String> {
        val request = CompletionRequest(
            model = model.name,
            user = user,
            prompt = prompt,
            echo = echo,
            n = n,
            temperature = temperature,
            maxTokens = 256
        )

        val completions = llm.createCompletion(request)
        return formatCompletionOutput(completions)
    }

    private suspend fun callChatEndpoint(prompt: String): Map<String, String> {
        val request = ChatCompletionRequest(
            model = model.name,
            user = user,
            messages = listOf(
                Message(
                    Role.system.name,
                    prompt
                )
            ),
            n = n,
            temperature = temperature,
            maxTokens = 256
        )

        val completions = llm.createChatCompletion(request)
        return formatChatOutput(completions.choices)
    }

    private fun formatChatOutput(completions: List<Choice>): Map<String, String> =
        config.outputKeys.associateWith {
            completions.joinToString(", ") { it.message.content }
        }

    private fun formatCompletionOutput(completions: List<CompletionChoice>): Map<String, String> =
        config.outputKeys.associateWith {
            completions.joinToString(", ") { it.text }
        }
}
