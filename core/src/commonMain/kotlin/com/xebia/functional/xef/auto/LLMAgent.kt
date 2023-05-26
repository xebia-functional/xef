@file:JvmMultifileClass
@file:JvmName("Agent")

package com.xebia.functional.xef.auto

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.xebia.functional.tokenizer.Encoding
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
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

private val logger: KLogger by lazy { KotlinLogging.logger {} }

@AiDsl
suspend fun AIScope.promptMessage(
  question: String,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.5,
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
  temperature: Double = 0.5,
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
  temperature: Double = 0.5,
  bringFromContext: Int,
  minResponseTokens: Int
): List<String> {
  val promptWithContext: String =
    promptWithContext(prompt, bringFromContext, model.modelType, minResponseTokens)

  val maxTokens: Int = checkTotalLeftTokens(model.modelType, "", promptWithContext)

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
  temperature: Double = 0.5,
  bringFromContext: Int,
  minResponseTokens: Int
): List<String> {
  val role: String = Role.system.name
  val promptWithContext: String =
    promptWithContext(prompt, bringFromContext, model.modelType, minResponseTokens)
  val messages: List<Message> = listOf(Message(role, promptWithContext))
  val maxTokens: Int = checkTotalLeftChatTokens(messages, model)
  val request =
    ChatCompletionRequest(
      model = model.name,
      user = user,
      messages = messages,
      n = n,
      temperature = temperature,
      maxTokens = maxTokens
    )
  return openAIClient.createChatCompletion(request).choices.map { it.message.content }
}

private suspend fun AIScope.promptWithContext(
  prompt: String,
  bringFromContext: Int,
  modelType: ModelType,
  minResponseTokens: Int
): String {
  val ctxInfo: List<String> = context.similaritySearch(prompt, bringFromContext)
  return createPromptWithContextAwareOfTokens(
    ctxInfo = ctxInfo,
    modelType = modelType,
    prompt = prompt,
    minResponseTokens = minResponseTokens
  )
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
      "Tokens -- used: $takenTokens, model max: $maxContextLength, left: $totalLeftTokens"
    }
    totalLeftTokens
  }

private fun AIScope.checkTotalLeftChatTokens(messages: List<Message>, model: LLMModel): Int {
  val maxContextLength: Int = model.modelType.maxContextLength
  val messagesTokens: Int = tokensFromMessages(messages, model)
  val totalLeftTokens: Int = maxContextLength - messagesTokens
  if (totalLeftTokens < 0) {
    raise(AIError.MessagesExceedMaxTokenLength(messages, messagesTokens, maxContextLength))
  }
  logger.debug {
    "Tokens -- used: $messagesTokens, model max: $maxContextLength, left: $totalLeftTokens"
  }
  return totalLeftTokens
}

private fun tokensFromMessages(messages: List<Message>, model: LLMModel): Int =
  when (model) {
    LLMModel.GPT_3_5_TURBO -> {
      val paddingTokens = 5 // otherwise if the model changes, it might later fail
      val fallbackModel: LLMModel = LLMModel.GPT_3_5_TURBO_0301
      logger.debug {
        "Warning: ${model.name} may change over time. " +
          "Returning messages num tokens assuming ${fallbackModel.name} + $paddingTokens padding tokens."
      }
      tokensFromMessages(messages, fallbackModel) + paddingTokens
    }
    LLMModel.GPT_4,
    LLMModel.GPT_4_32K -> {
      val paddingTokens = 5 // otherwise if the model changes, it might later fail
      val fallbackModel: LLMModel = LLMModel.GPT_4_0314
      logger.debug {
        "Warning: ${model.name} may change over time. " +
          "Returning messages num tokens assuming ${fallbackModel.name} + $paddingTokens padding tokens."
      }
      tokensFromMessages(messages, fallbackModel) + paddingTokens
    }
    LLMModel.GPT_3_5_TURBO_0301 ->
      model.modelType.encoding.countTokensFromMessages(
        messages,
        tokensPerMessage = 4,
        tokensPerName = 0
      )
    LLMModel.GPT_4_0314 ->
      model.modelType.encoding.countTokensFromMessages(
        messages,
        tokensPerMessage = 3,
        tokensPerName = 2
      )
    else -> {
      val paddingTokens = 20
      val fallbackModel: LLMModel = LLMModel.GPT_3_5_TURBO_0301
      logger.debug {
        "Warning: calculation of tokens is partially supported for ${model.name} . " +
          "Returning messages num tokens assuming ${fallbackModel.name} + $paddingTokens padding tokens."
      }
      tokensFromMessages(messages, fallbackModel) + paddingTokens
    }
  }

private fun Encoding.countTokensFromMessages(
  messages: List<Message>,
  tokensPerMessage: Int,
  tokensPerName: Int
): Int =
  messages.sumOf { message ->
    countTokens(message.role) +
      countTokens(message.content) +
      tokensPerMessage +
      (message.name?.let { tokensPerName } ?: 0)
  } + 3
