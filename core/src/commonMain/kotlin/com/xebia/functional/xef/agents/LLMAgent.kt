package com.xebia.functional.xef.agents

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.openai.ChatCompletionRequest
import com.xebia.functional.xef.llm.openai.CompletionRequest
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.llm.openai.Message
import com.xebia.functional.xef.llm.openai.OpenAIClient
import com.xebia.functional.xef.llm.openai.Role
import com.xebia.functional.xef.prompt.PromptTemplate
import com.xebia.functional.xef.vectorstores.VectorStore

class LLMAgent(
  private val llm: OpenAIClient,
  private val template: PromptTemplate<String>,
  private val model: LLMModel,
  private val context: VectorStore = VectorStore.EMPTY,
  private val user: String = "testing",
  private val echo: Boolean = false,
  private val n: Int = 1,
  private val temperature: Double = 0.0,
  private val bringFromContext: Int = 10,
  private val maxTokens: Int = Int.MAX_VALUE
) : Agent<Map<String, String>, List<String>> {

  override val name = "LLM Agent"
  override val description: String = "Runs a query through a LLM agent"

  override suspend fun Raise<AIError>.call(input: Map<String, String>): List<String> {
    val prompt = template.format(checkInput(template, input))

    val ctxInfo = context.similaritySearch(prompt, bringFromContext)
    val promptWithContext =
      if (ctxInfo.isNotEmpty())
        """
                |Instructions: Use the [Information] below delimited by 3 backticks to accomplish
                |the [Objective] at the end of the prompt.
                |Try to match the data returned in the [Objective] with this [Information] as best as you can.
                |[Information]:
                |```
                |${ctxInfo.joinToString("\n")}
                |```
                |$prompt
                """
          .trimMargin()
      else prompt

    return when (model.kind) {
      LLMModel.Kind.Completion -> callCompletionEndpoint(promptWithContext)
      LLMModel.Kind.Chat -> callChatEndpoint(promptWithContext)
    }
  }

  private suspend fun Raise<AIError>.callCompletionEndpoint(prompt: String): List<String> {
    val configMaxTokens: Int = calculateMaxTokens(prompt)

    val request =
      CompletionRequest(
        model = model.name,
        user = user,
        prompt = prompt,
        echo = echo,
        n = n,
        temperature = temperature,
        maxTokens = configMaxTokens
      )
    return llm.createCompletion(request).map { it.text }
  }

  private suspend fun Raise<AIError>.callChatEndpoint(prompt: String): List<String> {
    val configMaxTokens: Int = calculateMaxTokens(prompt)

    val request =
      ChatCompletionRequest(
        model = model.name,
        user = user,
        messages = listOf(Message(Role.system.name, prompt)),
        n = n,
        temperature = temperature,
        maxTokens = configMaxTokens
      )
    return llm.createChatCompletion(request).choices.map { it.message.content }
  }

  private fun Raise<AIError>.calculateMaxTokens(prompt: String): Int {
    val contextLength: Int = model.maxContextLength
    val promptSize: Int = model.encodingType.encoding.encode(prompt).size

    val requestMaxTokens: Int = checkContextLength(contextLength, promptSize)
    return if (maxTokens < requestMaxTokens) maxTokens else requestMaxTokens
  }

  private fun Raise<AIError>.checkContextLength(maxContextLength: Int, promptSize: Int): Int {
    val maxTokens: Int = maxContextLength - promptSize
    ensure(maxTokens > 0) { AIError.ExceedModelContextLength(maxContextLength, promptSize) }
    return maxTokens
  }
}

fun Raise<AIError>.checkInput(
  template: PromptTemplate<String>,
  input: Map<String, String>
): Map<String, String> {
  ensure((template.inputKeys subtract input.keys).isEmpty()) {
    AIError.InvalidInputs(
      "The provided inputs: " +
        input.keys.joinToString(", ") { "{$it}" } +
        " do not match with chain's inputs: " +
        template.inputKeys.joinToString(", ") { "{$it}" }
    )
  }
  return input
}
