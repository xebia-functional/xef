package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.raise.either
import com.xebia.functional.AIError.Chain.InvalidInputs
import com.xebia.functional.llm.openai.*
import com.xebia.functional.prompt.PromptTemplate

@Suppress("LongParameterList")
suspend fun llmChain(
  llm: OpenAIClient,
  promptTemplate: PromptTemplate<String>,
  llmModel: String = "gpt-3.5-turbo",
  user: String = "testing",
  n: Int = 1,
  temperature: Double = 0.0,
  outputVariable: String,
  chainOutput: Chain.ChainOutput = Chain.ChainOutput.OnlyOutput
): Chain = object : Chain {

  private val inputKeys: Set<String> = promptTemplate.inputKeys.toSet()
  private val outputKeys: Set<String> = setOf(outputVariable)

  override val config: Chain.Config = Chain.Config(inputKeys, outputKeys, chainOutput)

  override suspend fun call(inputs: Map<String, String>): Either<InvalidInputs, Map<String, String>> =
    either {
      val prompt = promptTemplate.format(inputs)

      val request = ChatCompletionRequest(
        model = llmModel,
        user = user,
        messages = listOf(
          Message(
            role = Role.system.name,
            content = prompt
          )
        ),
        n = n,
        temperature = temperature,
        maxTokens = 256
      )

      val completions = llm.createChatCompletion(request)
      formatOutput(completions.choices)
    }

  private fun formatOutput(completions: List<Choice>): Map<String, String> =
    config.outputKeys.associateWith {
      completions.joinToString(", ") { it.message.content }
    }
}
