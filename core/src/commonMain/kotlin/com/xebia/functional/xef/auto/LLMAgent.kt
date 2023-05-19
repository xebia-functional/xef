package com.xebia.functional.xef.auto

import com.xebia.functional.xef.llm.openai.ChatCompletionRequest
import com.xebia.functional.xef.llm.openai.CompletionRequest
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.llm.openai.Message
import com.xebia.functional.xef.llm.openai.Role
import com.xebia.functional.xef.prompt.Prompt

@AiDsl
suspend fun AIScope.promptMessage(
  question: String,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int = 10
): List<String> =
  promptMessage(Prompt(question), model, user, echo, n, temperature, bringFromContext)

@AiDsl
suspend fun AIScope.promptMessage(
  prompt: Prompt,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int = 10
): List<String> {
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
    LLMModel.Kind.Completion ->
      callCompletionEndpoint(promptWithContext, model, user, echo, n, temperature)
    LLMModel.Kind.Chat -> callChatEndpoint(promptWithContext, model, user, n, temperature)
  }
}

private suspend fun AIScope.callCompletionEndpoint(
  prompt: String,
  model: LLMModel,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0
): List<String> {
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
  return openAIClient.createCompletion(request).map { it.text }
}

private suspend fun AIScope.callChatEndpoint(
  prompt: String,
  model: LLMModel,
  user: String = "testing",
  n: Int = 1,
  temperature: Double = 0.0
): List<String> {
  val request =
    ChatCompletionRequest(
      model = model.name,
      user = user,
      messages = listOf(Message(Role.system.name, prompt)),
      n = n,
      temperature = temperature,
      maxTokens = 1024
    )
  return openAIClient.createChatCompletion(request).choices.map { it.message.content }
}
