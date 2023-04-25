package com.xebia.functional.chains

import com.xebia.functional.llm.openai.CompletionChoice
import com.xebia.functional.llm.openai.CompletionRequest
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate

@Suppress("LongParameterList")
suspend fun LLMChain(
    llm: OpenAIClient,
    promptTemplate: PromptTemplate,
    llmModel: String,
    user: String,
    echo: Boolean,
    n: Int,
    temperature: Double,
    onlyOutput: Boolean
): BaseChain = object : BaseChain {

    override val config = Config(promptTemplate.inputKeys.toSet(), setOf("answer"), onlyOutput)

    override suspend fun call(inputs: Map<String, String>): Map<String, String> {
        val prompt = preparePrompt(inputs)

        val request = CompletionRequest(
            model = llmModel,
            user = user,
            prompt = prompt,
            echo = echo,
            n = n,
            temperature = temperature,
        )

        val completions = llm.createCompletion(request)
        return formatOutput(completions)
    }

    private suspend fun preparePrompt(inputs: Map<String, String>): String = promptTemplate.format(inputs)

    private fun formatOutput(completions: List<CompletionChoice>): Map<String, String> =
        config.outputKeys.associateWith {
            completions.joinToString(", ") { it.text }
        }
}
