package com.xebia.functional.xef.auto

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.openai.ChatCompletionRequest
import com.xebia.functional.xef.llm.openai.CompletionRequest
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.llm.openai.Message
import com.xebia.functional.xef.llm.openai.Role
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
  modelType: ModelType,
  prompt: String,
  minResponseTokens: Int,
): String {
  val maxContextLength: Int = modelType.maxContextLength
  val promptTokens: Int = modelType.encoding.countTokens(prompt)
  val remainingTokens: Int = maxContextLength - promptTokens - minResponseTokens

  return if (ctxInfo.isNotEmpty() && remainingTokens > minResponseTokens) {
    val ctx: String = ctxInfo.joinToString("\n")

    ensure(promptTokens < maxContextLength) {
      raise(AIError.PromptExceedsMaxTokenLength(prompt, promptTokens, maxContextLength))
    }
    // truncate the context if it's too long based on the max tokens calculated considering the
    // existing prompt tokens
    // alternatively we could summarize the context, but that's not implemented yet
    val ctxTruncated: String = modelType.encoding.truncateText(ctx, remainingTokens)

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
  val (promptWithContext: String, maxTokens: Int) =
    promptWithContextAndRemainingTokens(
      "",
      prompt,
      bringFromContext,
      model.modelType,
      minResponseTokens
    )
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
  return openAIClient.createCompletion(request).choices.map { it.text }
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
  val role: String = Role.system.name
  val (promptWithContext: String, maxTokens: Int) =
    promptWithContextAndRemainingTokens(
      role,
      prompt,
      bringFromContext,
      model.modelType,
      minResponseTokens
    )
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
  modelType: ModelType,
  minResponseTokens: Int
): Pair<String, Int> {
  val ctxInfo: List<String> = context.similaritySearch(prompt, bringFromContext)
  val promptWithContext: String =
    createPromptWithContextAwareOfTokens(
      ctxInfo = ctxInfo,
      modelType = modelType,
      prompt = prompt,
      minResponseTokens = minResponseTokens
    )
  val totalLeftTokens: Int = checkTotalLeftTokens(modelType, role, promptWithContext)
  return Pair(promptWithContext, totalLeftTokens)
}

private fun AIScope.checkTotalLeftTokens(
  modelType: ModelType,
  role: String,
  promptWithContext: String
): Int =
  with(modelType) {
    val roleTokens: Int = encoding.countTokens(role)
    val padding = 20 // reserve 20 tokens for additional symbols around the context
    val promptTokens: Int = encoding.countTokens(promptWithContext)
    val takenTokens: Int = roleTokens + promptTokens + padding
    val totalLeftTokens: Int = maxContextLength - takenTokens
    if (totalLeftTokens < 0) {
      raise(AIError.PromptExceedsMaxTokenLength(promptWithContext, takenTokens, maxContextLength))
    }
    logger.debug {
      "Tokens :: used: $takenTokens, model max: $maxContextLength, left: $totalLeftTokens"
    }
    totalLeftTokens
  }
