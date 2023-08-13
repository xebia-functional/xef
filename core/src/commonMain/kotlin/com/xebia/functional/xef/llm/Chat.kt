package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.Memory
import com.xebia.functional.xef.vectorstores.VectorStore
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
    question: String,
    scope: Conversation,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): Flow<String> = promptStreaming(Prompt(question), scope, functions, promptConfiguration)

  @AiDsl
  fun promptStreaming(
    prompt: Prompt,
    scope: Conversation,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): Flow<String> = flow {
    val memories: List<Memory> = memories(scope, promptConfiguration)

    val messagesForRequest =
      fitMessagesByTokens(
        messagesFromMemory(memories),
        prompt.toMessages(),
        scope.store,
        modelType,
        promptConfiguration
      )

    val request =
      ChatCompletionRequest(
        model = name,
        user = promptConfiguration.user,
        messages = messagesForRequest,
        n = promptConfiguration.numberOfPredictions,
        temperature = promptConfiguration.temperature,
        maxTokens = promptConfiguration.minResponseTokens,
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
  suspend fun promptMessage(
    question: String,
    scope: Conversation,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): String =
    promptMessages(Prompt(question), scope, emptyList(), promptConfiguration).firstOrNull()
      ?: throw AIError.NoResponse()

  @AiDsl
  suspend fun promptMessages(
    question: String,
    scope: Conversation,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): List<String> = promptMessages(Prompt(question), scope, functions, promptConfiguration)

  @AiDsl
  suspend fun promptMessages(
    prompt: Prompt,
    scope: Conversation,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): List<String> {
    return promptMessages(prompt.toMessages(), scope, functions, promptConfiguration)
  }

  @AiDsl
  suspend fun promptMessages(
    messages: List<Message>,
    scope: Conversation,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): List<String> {

    val memories: List<Memory> = memories(scope, promptConfiguration)
    val messagesForRequest =
      fitMessagesByTokens(
        messagesFromMemory(memories),
        messages,
        scope.store,
        modelType,
        promptConfiguration
      )

    fun chatRequest(): ChatCompletionRequest =
      ChatCompletionRequest(
        model = name,
        user = promptConfiguration.user,
        messages = messagesForRequest,
        n = promptConfiguration.numberOfPredictions,
        temperature = promptConfiguration.temperature,
        maxTokens = promptConfiguration.minResponseTokens,
      )

    fun withFunctionsRequest(): ChatCompletionRequestWithFunctions =
      ChatCompletionRequestWithFunctions(
        model = name,
        user = promptConfiguration.user,
        messages = messagesForRequest,
        n = promptConfiguration.numberOfPredictions,
        temperature = promptConfiguration.temperature,
        maxTokens = promptConfiguration.minResponseTokens,
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

  suspend fun String.toMessages(): List<Message> = Prompt(this).toMessages()

  suspend fun Prompt.toMessages(): List<Message> = listOf(Message.userMessage { message })

  private suspend fun addMemoriesAfterStream(
    request: ChatCompletionRequest,
    scope: Conversation,
    buffer: StringBuilder,
  ) {
    val lastRequestMessage = request.messages.lastOrNull()
    val cid = scope.conversationId
    if (cid != null && lastRequestMessage != null) {
      val requestMemory =
        Memory(conversationId = cid, content = lastRequestMessage, timestamp = getTimeMillis())
      val responseMemory =
        Memory(
          conversationId = cid,
          content =
            Message(role = Role.ASSISTANT, content = buffer.toString(), name = Role.ASSISTANT.name),
          timestamp = getTimeMillis(),
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
        Memory(conversationId = cid, content = requestUserMessage, timestamp = getTimeMillis())
      val firstChoiceMemory =
        Memory(
          conversationId = cid,
          content =
            Message(
              role = role,
              content = firstChoice.message?.content
                  ?: firstChoice.message?.functionCall?.arguments ?: "",
              name = role.name
            ), //
          timestamp = getTimeMillis()
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
        Memory(conversationId = cid, content = requestUserMessage, timestamp = getTimeMillis())
      val firstChoiceMemory =
        Memory(
          conversationId = cid,
          content =
            Message(role = role, content = firstChoice.message?.content ?: "", name = role.name),
          timestamp = getTimeMillis()
        )
      scope.store.addMemories(listOf(requestMemory, firstChoiceMemory))
    }
  }

  private fun messagesFromMemory(memories: List<Memory>): List<Message> =
    memories.map { it.content }

  private suspend fun memories(
    scope: Conversation,
    promptConfiguration: PromptConfiguration
  ): List<Memory> {
    val cid = scope.conversationId
    return if (cid != null) {
      scope.store.memories(cid, promptConfiguration.memoryLimit)
    } else {
      emptyList()
    }
  }

  private suspend fun fitMessagesByTokens(
    history: List<Message>,
    messages: List<Message>,
    context: VectorStore,
    modelType: ModelType,
    promptConfiguration: PromptConfiguration,
  ): List<Message> {
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

    val historyMessagesWithTokens = history.map { Pair(it, tokensFromMessages(listOf(it))) }

    val totalTokenWithMessages =
      historyMessagesWithTokens.foldRight(Pair(0, emptyList<Message>())) { pair, acc ->
        if (acc.first + pair.second > maxHistoryTokens) {
          acc
        } else {
          Pair(acc.first + pair.second, acc.second + pair.first)
        }
      }

    val historyAllowed = totalTokenWithMessages.second.reversed()

    val maxContextTokens = (remainingTokensForContexts * contextPercent) / 100

    val ctxInfo =
      context.similaritySearch(
        messages.joinToString("\n") { it.content },
        promptConfiguration.docsInContext,
      )

    val contextAllowed =
      if (ctxInfo.isNotEmpty()) {
        val ctx: String = ctxInfo.joinToString("\n")

        val ctxTruncated: String = modelType.encoding.truncateText(ctx, maxContextTokens)

        listOf(Message.assistantMessage { ctxTruncated })
      } else {
        emptyList()
      }

    return contextAllowed + historyAllowed + messages
  }
}
