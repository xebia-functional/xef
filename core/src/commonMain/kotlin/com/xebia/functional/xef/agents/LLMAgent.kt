package com.xebia.functional.xef.agents

import arrow.core.raise.Raise
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.openai.ChatCompletionRequest
import com.xebia.functional.xef.llm.openai.CompletionRequest
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.llm.openai.Message
import com.xebia.functional.xef.llm.openai.OpenAIClient
import com.xebia.functional.xef.llm.openai.Role
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.VectorStore

class LLMAgent(
  private val llm: OpenAIClient,
  private val prompt: Prompt,
  private val model: LLMModel,
  private val context: VectorStore = VectorStore.EMPTY,
  private val user: String = "testing",
  private val echo: Boolean = false,
  private val n: Int = 1,
  private val temperature: Double = 0.0,
  private val bringFromContext: Int = 10
) : Agent<List<String>> {

  override val name = "LLM Agent"
  override val description: String = "Runs a query through a LLM agent"

  override suspend fun Raise<AIError>.call(): List<String> {
    val ctxInfo = context.similaritySearch(prompt.message, bringFromContext)
    val promptWithContext =
      if (ctxInfo.isNotEmpty()) {
        """|Instructions: Use the [Information] below delimited by 3 backticks to accomplish
           |the [Objective] at the end of the prompt.
           |Try to match the data returned in the [Objective] with this [Information] as best as you can.
           |[Information]:
           |```
           |${ctxInfo.joinToString("\n")}
           |```
           |$prompt"""
          .trimMargin()
      } else prompt.message

    return when (model.kind) {
      LLMModel.Kind.Completion -> callCompletionEndpoint(promptWithContext)
      LLMModel.Kind.Chat -> callChatEndpoint(promptWithContext)
    }
  }

  private suspend fun callCompletionEndpoint(prompt: String): List<String> {
    val request =
      CompletionRequest(
        model = model.name,
        user = user,
        prompt = prompt,
        echo = echo,
        n = n,
        temperature = temperature,
        maxTokens = 1024
      )
    return llm.createCompletion(request).map { it.text }
  }

  private suspend fun callChatEndpoint(prompt: String): List<String> {
    val request =
      ChatCompletionRequest(
        model = model.name,
        user = user,
        messages = listOf(Message(Role.system.name, prompt)),
        n = n,
        temperature = temperature,
        maxTokens = 1024
      )
    return llm.createChatCompletion(request).choices.map { it.message.content }
  }
}
