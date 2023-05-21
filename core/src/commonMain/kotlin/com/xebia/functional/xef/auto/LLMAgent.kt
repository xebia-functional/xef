package com.xebia.functional.xef.auto

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.openai.*
import com.xebia.functional.xef.prompt.Prompt
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging

private val logger: KLogger by lazy { KotlinLogging.logger {} }

@AiDsl
suspend fun AIScope.promptMessage(
  question: String,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int = 10,
  minResponseTokens: Int = 500
): List<String> =
  promptMessage(
    Prompt(question),
    model,
    user,
    echo,
    n,
    temperature,
    bringFromContext,
    minResponseTokens
  )

@AiDsl
suspend fun AIScope.promptMessage(
  prompt: Prompt,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int = 10,
  minResponseTokens: Int
): List<String> {
  return when (model.kind) {
    LLMModel.Kind.Completion ->
      callCompletionEndpoint(
        prompt.message,
        model,
        user,
        echo,
        n,
        temperature,
        bringFromContext,
        minResponseTokens
      )
    LLMModel.Kind.Chat ->
      callChatEndpoint(
        prompt.message,
        model,
        user,
        n,
        temperature,
        bringFromContext,
        minResponseTokens
      )
  }
}

private fun Raise<AIError>.createPromptWithContextAwareOfTokens(
  ctxInfo: List<String>,
  model: LLMModel,
  prompt: String,
  minResponseTokens: Int,
): String {
  val remaininingTokens =
    model.modelType.maxContextLength -
      model.modelType.encoding.countTokens(prompt) -
      minResponseTokens
  return if (ctxInfo.isNotEmpty() && remaininingTokens > minResponseTokens) {
    val ctx = ctxInfo.joinToString("\n")
    val promptTokens = model.modelType.encoding.countTokens(prompt)
    ensure(promptTokens < model.modelType.maxContextLength) {
      raise(
        AIError.PromptExceedsMaxTokenLength(prompt, promptTokens, model.modelType.maxContextLength)
      )
    }
    // truncate the context if it's too long based on the max tokens calculated considering the
    // existing prompt tokens
    // alternatively we could summarize the context, but that's not implemented yet
    val maxTokens = model.modelType.maxContextLength - promptTokens - minResponseTokens
    val ctxTruncated = model.modelType.encoding.truncateText(ctx, maxTokens)
    """|```Context
         |${ctxTruncated}
         |```
         |The context is related to the question try to answer the `goal` as best as you can
         |or provide information about the found content
         |```goal
         |${prompt}
         |```
         |ANSWER:
         |"""
      .trimMargin()
  } else prompt
}

private suspend fun AIScope.callCompletionEndpoint(
  prompt: String,
  model: LLMModel,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int,
  minResponseTokens: Int,
): List<String> {
  val (promptWithContext, maxTokens) =
    promptWithContextAndRemainingTokens("", prompt, bringFromContext, model, minResponseTokens)
  val request =
    CompletionRequest(
      model = model.name,
      user = user,
      prompt = promptWithContext,
      echo = echo,
      n = n,
      temperature = temperature,
      maxTokens = maxTokens
    )
  return openAIClient.createCompletion(request).map { it.text }
}

private suspend fun AIScope.callChatEndpoint(
  prompt: String,
  model: LLMModel,
  user: String = "testing",
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int,
  minResponseTokens: Int
): List<String> {
  val role = Role.system.name
  val (promptWithContext, maxTokens) =
    promptWithContextAndRemainingTokens(role, prompt, bringFromContext, model, minResponseTokens)
  val request =
    ChatCompletionRequest(
      model = model.name,
      user = user,
      messages = listOf(Message(role, promptWithContext)),
      n = n,
      temperature = temperature,
      maxTokens = maxTokens
    )
  return openAIClient.createChatCompletion(request).choices.map { it.message.content }
}

private suspend fun AIScope.promptWithContextAndRemainingTokens(
  role: String,
  prompt: String,
  bringFromContext: Int,
  model: LLMModel,
  minResponseTokens: Int
): Pair<String, Int> {
  val ctxInfo = context.similaritySearch(prompt, bringFromContext)
  val promptWithContext =
    createPromptWithContextAwareOfTokens(
      ctxInfo = ctxInfo,
      model = model,
      prompt = prompt,
      minResponseTokens = minResponseTokens
    )
  val roleTokens = model.modelType.encoding.countTokens(role)
  val padding = 20 // reserve 20 tokens for additional symbols around the context
  val promptTokens = model.modelType.encoding.countTokens(promptWithContext)
  val takenTokens = roleTokens + promptTokens + padding
  val totalLeftTokens = model.modelType.maxContextLength - takenTokens
  logger.debug {
    "Tokens: used: $takenTokens, model max: ${model.modelType.maxContextLength}, left: $totalLeftTokens"
  }
  return Pair(promptWithContext, totalLeftTokens)
}
