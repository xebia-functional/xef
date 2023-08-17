package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.vectorstores.Memory
import io.ktor.util.date.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

interface Chat : LLM {
  val modelType: ModelType

  suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse

  suspend fun createChatCompletions(request: ChatCompletionRequest): Flow<ChatCompletionChunk>

  fun tokensFromMessages(messages: List<Message>): Int

  @AiDsl
  fun promptStreaming(
    prompt: Prompt,
    scope: Conversation,
    functions: List<CFunction> = emptyList()
  ): Flow<String> = flow {
    val messagesForRequest =
      fitMessagesByTokens(prompt.messages, scope, modelType, prompt.configuration)

    val request =
      ChatCompletionRequest(
        model = name,
        user = prompt.configuration.user,
        messages = messagesForRequest,
        n = prompt.configuration.numberOfPredictions,
        temperature = prompt.configuration.temperature,
        maxTokens = prompt.configuration.minResponseTokens,
        streamToStandardOut = true
      )

    val buffer = StringBuilder()
    createChatCompletions(request)
      .onEach {
        it.choices.forEach { choice ->
          val text = choice.delta?.content ?: ""
          buffer.append(text)
        }
      }
      .onCompletion { addMemoriesAfterStream(request, scope, buffer) }
      .collect { emit(it.choices.mapNotNull { it.delta?.content }.joinToString("")) }
  }

  @AiDsl
  suspend fun promptMessage(prompt: Prompt, scope: Conversation): String =
    promptMessages(prompt, scope, emptyList()).firstOrNull() ?: throw AIError.NoResponse()

  @AiDsl
  suspend fun promptMessages(
    prompt: Prompt,
    scope: Conversation,
    functions: List<CFunction> = emptyList()
  ): List<String> {

    val messagesForRequest =
      fitMessagesByTokens(prompt.messages, scope, modelType, prompt.configuration)

    fun chatRequest(): ChatCompletionRequest =
      ChatCompletionRequest(
        model = name,
        user = prompt.configuration.user,
        messages = messagesForRequest,
        n = prompt.configuration.numberOfPredictions,
        temperature = prompt.configuration.temperature,
        maxTokens = prompt.configuration.minResponseTokens,
      )

    fun withFunctionsRequest(): ChatCompletionRequestWithFunctions =
      ChatCompletionRequestWithFunctions(
        model = name,
        user = prompt.configuration.user,
        messages = messagesForRequest,
        n = prompt.configuration.numberOfPredictions,
        temperature = prompt.configuration.temperature,
        maxTokens = prompt.configuration.minResponseTokens,
        functions = functions,
        functionCall = mapOf("name" to (functions.firstOrNull()?.name ?: ""))
      )

    return when (this) {
      is ChatWithFunctions ->
        // we only support functions for now with GPT_3_5_TURBO_FUNCTIONS
        if (modelType == ModelType.GPT_3_5_TURBO_FUNCTIONS) {
          val request = withFunctionsRequest()
          createChatCompletionWithFunctions(request)
            .choices
            .addChoiceWithFunctionsToMemory(request, scope)
            .mapNotNull { it.message?.functionCall?.arguments }
        } else {
          val request = chatRequest()
          createChatCompletion(request).choices.addChoiceToMemory(request, scope).mapNotNull {
            it.message?.content
          }
        }
      else -> {
        val request = chatRequest()
        createChatCompletion(request).choices.addChoiceToMemory(request, scope).mapNotNull {
          it.message?.content
        }
      }
    }
  }

  private suspend fun addMemoriesAfterStream(
    request: ChatCompletionRequest,
    scope: Conversation,
    buffer: StringBuilder,
  ) {
    val lastRequestMessage = request.messages.lastOrNull()
    val cid = scope.conversationId
    if (cid != null && lastRequestMessage != null) {
      val requestMemory =
        Memory(
          conversationId = cid,
          content = lastRequestMessage,
          timestamp = getTimeMillis(),
          approxTokens = tokensFromMessages(listOf(lastRequestMessage))
        )
      val responseMessage =
        Message(role = Role.ASSISTANT, content = buffer.toString(), name = Role.ASSISTANT.name)
      val responseMemory =
        Memory(
          conversationId = cid,
          content = responseMessage,
          timestamp = getTimeMillis(),
          approxTokens = tokensFromMessages(listOf(responseMessage))
        )
      scope.store.addMemories(listOf(requestMemory, responseMemory))
    }
  }

  private suspend fun List<ChoiceWithFunctions>.addChoiceWithFunctionsToMemory(
    request: ChatCompletionRequestWithFunctions,
    scope: Conversation
  ): List<ChoiceWithFunctions> = also {
    val firstChoice = firstOrNull()
    val requestUserMessage = request.messages.lastOrNull()
    val cid = scope.conversationId
    if (requestUserMessage != null && firstChoice != null && cid != null) {
      val role = firstChoice.message?.role?.uppercase()?.let { Role.valueOf(it) } ?: Role.USER

      val requestMemory =
        Memory(
          conversationId = cid,
          content = requestUserMessage,
          timestamp = getTimeMillis(),
          approxTokens = tokensFromMessages(listOf(requestUserMessage))
        )
      val firstChoiceMessage =
        Message(
          role = role,
          content = firstChoice.message?.content
              ?: firstChoice.message?.functionCall?.arguments ?: "",
          name = role.name
        )
      val firstChoiceMemory =
        Memory(
          conversationId = cid,
          content = firstChoiceMessage,
          timestamp = getTimeMillis(),
          approxTokens = tokensFromMessages(listOf(firstChoiceMessage))
        )
      scope.store.addMemories(listOf(requestMemory, firstChoiceMemory))
    }
  }

  private suspend fun List<Choice>.addChoiceToMemory(
    request: ChatCompletionRequest,
    scope: Conversation
  ): List<Choice> = also {
    val firstChoice = firstOrNull()
    val requestUserMessage = request.messages.lastOrNull()
    val cid = scope.conversationId
    if (requestUserMessage != null && firstChoice != null && cid != null) {
      val role = firstChoice.message?.role?.name?.uppercase()?.let { Role.valueOf(it) } ?: Role.USER
      val requestMemory =
        Memory(
          conversationId = cid,
          content = requestUserMessage,
          timestamp = getTimeMillis(),
          approxTokens = tokensFromMessages(listOf(requestUserMessage))
        )
      val firstChoiceMessage =
        Message(role = role, content = firstChoice.message?.content ?: "", name = role.name)
      val firstChoiceMemory =
        Memory(
          conversationId = cid,
          content = firstChoiceMessage,
          timestamp = getTimeMillis(),
          approxTokens = tokensFromMessages(listOf(firstChoiceMessage))
        )
      scope.store.addMemories(listOf(requestMemory, firstChoiceMemory))
    }
  }

  private fun messagesFromMemory(memories: List<Memory>): List<Message> =
    memories.map { it.content }

  private suspend fun Conversation.memories(limitTokens: Int): List<Memory> {
    val cid = conversationId
    return if (cid != null) {
      store.memories(cid, limitTokens)
    } else {
      emptyList()
    }
  }

  private suspend fun fitMessagesByTokens(
    messages: List<Message>,
    scope: Conversation,
    modelType: ModelType,
    promptConfiguration: PromptConfiguration,
  ): List<Message> {

    // calculate tokens for history and context
    val maxContextLength: Int = modelType.maxContextLength
    val remainingTokens: Int = maxContextLength - promptConfiguration.minResponseTokens

    val messagesTokens = tokensFromMessages(messages)

    if (messagesTokens >= remainingTokens) {
      throw AIError.PromptExceedsMaxRemainingTokenLength(messagesTokens, remainingTokens)
    }

    val remainingTokensForContexts = remainingTokens - messagesTokens

    val historyPercent = promptConfiguration.messagePolicy.historyPercent
    val contextPercent = promptConfiguration.messagePolicy.contextPercent

    val maxHistoryTokens = (remainingTokensForContexts * historyPercent) / 100
    val maxContextTokens = (remainingTokensForContexts * contextPercent) / 100

    // calculate messages for history based on tokens

    val memories: List<Memory> =
      scope.memories(maxHistoryTokens + promptConfiguration.messagePolicy.historyPaddingTokens)

    val historyAllowed =
      if (memories.isNotEmpty()) {
        val history = messagesFromMemory(memories)

        // since we have the approximate tokens in memory, we need to fit the messages back to the
        // number of tokens if necessary
        val historyTokens = tokensFromMessages(history)
        if (historyTokens <= maxHistoryTokens) history
        else {
          val historyMessagesWithTokens = history.map { Pair(it, tokensFromMessages(listOf(it))) }

          val totalTokenWithMessages =
            historyMessagesWithTokens.foldRight(Pair(0, emptyList<Message>())) { pair, acc ->
              if (acc.first + pair.second > maxHistoryTokens) {
                acc
              } else {
                Pair(acc.first + pair.second, acc.second + pair.first)
              }
            }
          totalTokenWithMessages.second.reversed()
        }
      } else emptyList()

    // calculate messages for context based on tokens
    val ctxInfo =
      scope.store.similaritySearch(
        messages.joinToString("\n") { it.content },
        promptConfiguration.docsInContext,
      )

    val contextAllowed =
      if (ctxInfo.isNotEmpty()) {
        val ctx: String = ctxInfo.joinToString("\n")

        val ctxTruncated: String = modelType.encoding.truncateText(ctx, maxContextTokens)

        Prompt { +assistant(ctxTruncated) }.messages
      } else {
        emptyList()
      }

    return contextAllowed + historyAllowed + messages
  }
}
