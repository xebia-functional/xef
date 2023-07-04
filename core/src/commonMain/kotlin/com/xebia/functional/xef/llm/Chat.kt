package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.VectorStore

interface Chat : LLM {
  val modelType: ModelType

  suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse

  fun tokensFromMessages(messages: List<Message>): Int

  @AiDsl
  suspend fun promptMessage(
    question: String,
    context: VectorStore,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): String =
    promptMessage(Prompt(question), context, emptyList(), promptConfiguration).firstOrNull()
      ?: throw AIError.NoResponse()

  @AiDsl
  suspend fun promptMessage(
    question: String,
    context: VectorStore,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): List<String> = promptMessage(Prompt(question), context, functions, promptConfiguration)

  @AiDsl
  suspend fun promptMessage(
    prompt: Prompt,
    context: VectorStore,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): List<String> {

    val promptWithContext: String =
      createPromptWithContextAwareOfTokens(
        ctxInfo = context.similaritySearch(prompt.message, promptConfiguration.docsInContext),
        modelType = modelType,
        prompt = prompt.message,
        minResponseTokens = promptConfiguration.minResponseTokens
      )

    fun checkTotalLeftChatTokens(messages: List<Message>): Int {
      val maxContextLength: Int = modelType.maxContextLength
      val messagesTokens: Int = tokensFromMessages(messages)
      val totalLeftTokens: Int = maxContextLength - messagesTokens
      if (totalLeftTokens < 0) {
        throw AIError.MessagesExceedMaxTokenLength(messages, messagesTokens, maxContextLength)
      }
      return totalLeftTokens
    }

    fun buildChatRequest(): ChatCompletionRequest {
      val messages: List<Message> = listOf(Message(Role.USER, promptWithContext, Role.USER.name))
      return ChatCompletionRequest(
        model = name,
        user = promptConfiguration.user,
        messages = messages,
        n = promptConfiguration.numberOfPredictions,
        temperature = promptConfiguration.temperature,
        maxTokens = checkTotalLeftChatTokens(messages),
        streamToStandardOut = promptConfiguration.streamToStandardOut
      )
    }

    fun chatWithFunctionsRequest(): ChatCompletionRequestWithFunctions {
      val firstFnName: String? = functions.firstOrNull()?.name
      val messages: List<Message> = listOf(Message(Role.USER, promptWithContext, Role.USER.name))
      return ChatCompletionRequestWithFunctions(
        model = name,
        user = promptConfiguration.user,
        messages = messages,
        n = promptConfiguration.numberOfPredictions,
        temperature = promptConfiguration.temperature,
        maxTokens = checkTotalLeftChatTokens(messages),
        functions = functions,
        functionCall = mapOf("name" to (firstFnName ?: ""))
      )
    }

    return when (this) {
      is ChatWithFunctions ->
        // we only support functions for now with GPT_3_5_TURBO_FUNCTIONS
        if (modelType == ModelType.GPT_3_5_TURBO_FUNCTIONS) {
          createChatCompletionWithFunctions(chatWithFunctionsRequest()).choices.mapNotNull {
            it.message?.functionCall?.arguments
          }
        } else {
          createChatCompletion(buildChatRequest()).choices.mapNotNull { it.message?.content }
        }
      else -> createChatCompletion(buildChatRequest()).choices.mapNotNull { it.message?.content }
    }
  }

  private fun createPromptWithContextAwareOfTokens(
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

      if (promptTokens >= maxContextLength) {
        throw AIError.PromptExceedsMaxTokenLength(prompt, promptTokens, maxContextLength)
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
}
