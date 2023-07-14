package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.ConversationId
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
  suspend fun promptStreaming(
    question: String,
    context: VectorStore,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): Flow<String> =
    promptStreaming(Prompt(question), context, null, emptyList(), promptConfiguration)

  @AiDsl
  suspend fun promptStreaming(
    question: String,
    context: VectorStore,
    conversationId: ConversationId? = null,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): Flow<String> =
    promptStreaming(Prompt(question), context, conversationId, functions, promptConfiguration)

  @AiDsl
  suspend fun promptStreaming(
    prompt: Prompt,
    context: VectorStore,
    conversationId: ConversationId? = null,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): Flow<String> {

    val memories: List<Memory> = memories(conversationId, context, promptConfiguration)

    val promptWithContext: String =
      createPromptWithContextAwareOfTokens(
        memories = memories,
        ctxInfo = context.similaritySearch(prompt.message, promptConfiguration.docsInContext),
        modelType = modelType,
        prompt = prompt.message,
        minResponseTokens = promptConfiguration.minResponseTokens
      )

    val messages: List<Message> = messages(memories, promptWithContext)

    fun checkTotalLeftChatTokens(): Int {
      val maxContextLength: Int = modelType.maxContextLength
      val messagesTokens: Int = tokensFromMessages(messages)
      val totalLeftTokens: Int = maxContextLength - messagesTokens
      if (totalLeftTokens < 0) {
        throw AIError.MessagesExceedMaxTokenLength(messages, messagesTokens, maxContextLength)
      }
      return totalLeftTokens
    }

    val request: ChatCompletionRequest =
      ChatCompletionRequest(
        model = name,
        user = promptConfiguration.user,
        messages = messages,
        n = promptConfiguration.numberOfPredictions,
        temperature = promptConfiguration.temperature,
        maxTokens = checkTotalLeftChatTokens(),
        streamToStandardOut = true
      )

    return flow {
      val buffer = StringBuilder()
      createChatCompletions(request)
        .onEach {
          it.choices.forEach { choice ->
            val text = choice.delta?.content ?: ""
            buffer.append(text)
          }
        }
        .onCompletion { addMemoriesAfterStream(request, conversationId, buffer, context) }
        .collect { emit(it.choices.mapNotNull { it.delta?.content }.joinToString("")) }
    }
  }

  private suspend fun addMemoriesAfterStream(
    request: ChatCompletionRequest,
    conversationId: ConversationId?,
    buffer: StringBuilder,
    context: VectorStore
  ) {
    val lastRequestMessage = request.messages.lastOrNull()
    if (conversationId != null && lastRequestMessage != null) {
      val requestMemory =
        Memory(
          conversationId = conversationId,
          content = lastRequestMessage,
          timestamp = getTimeMillis()
        )
      val responseMemory =
        Memory(
          conversationId = conversationId,
          content =
            Message(role = Role.ASSISTANT, content = buffer.toString(), name = Role.ASSISTANT.name),
          timestamp = getTimeMillis(),
        )
      context.addMemories(listOf(requestMemory, responseMemory))
    }
  }

  @AiDsl
  suspend fun promptMessage(
    question: String,
    context: VectorStore,
    conversationId: ConversationId? = null,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): String =
    promptMessages(Prompt(question), context, conversationId, emptyList(), promptConfiguration)
      .firstOrNull()
      ?: throw AIError.NoResponse()

  @AiDsl
  suspend fun promptMessages(
    question: String,
    context: VectorStore,
    conversationId: ConversationId? = null,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): List<String> =
    promptMessages(Prompt(question), context, conversationId, functions, promptConfiguration)

  @AiDsl
  suspend fun promptMessages(
    prompt: Prompt,
    context: VectorStore,
    conversationId: ConversationId? = null,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): List<String> {

    val memories: List<Memory> = memories(conversationId, context, promptConfiguration)

    val promptWithContext: String =
      createPromptWithContextAwareOfTokens(
        memories = memories,
        ctxInfo = context.similaritySearch(prompt.message, promptConfiguration.docsInContext),
        modelType = modelType,
        prompt = prompt.message,
        minResponseTokens = promptConfiguration.minResponseTokens
      )

    val messages: List<Message> = messages(memories, promptWithContext)

    fun checkTotalLeftChatTokens(): Int {
      val maxContextLength: Int = modelType.maxContextLength
      val messagesTokens: Int = tokensFromMessages(messages)
      val totalLeftTokens: Int = maxContextLength - messagesTokens
      if (totalLeftTokens < 0) {
        throw AIError.MessagesExceedMaxTokenLength(messages, messagesTokens, maxContextLength)
      }
      return totalLeftTokens
    }

    fun chatRequest(): ChatCompletionRequest =
      ChatCompletionRequest(
        model = name,
        user = promptConfiguration.user,
        messages = messages,
        n = promptConfiguration.numberOfPredictions,
        temperature = promptConfiguration.temperature,
        maxTokens = checkTotalLeftChatTokens(),
        streamToStandardOut = promptConfiguration.streamToStandardOut
      )

    fun withFunctionsRequest(): ChatCompletionRequestWithFunctions =
      ChatCompletionRequestWithFunctions(
        model = name,
        user = promptConfiguration.user,
        messages = messages,
        n = promptConfiguration.numberOfPredictions,
        temperature = promptConfiguration.temperature,
        maxTokens = checkTotalLeftChatTokens(),
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
            .addChoiceWithFunctionsToMemory(request, context, conversationId)
            .mapNotNull { it.message?.functionCall?.arguments }
        } else {
          val request = chatRequest()
          createChatCompletion(request)
            .choices
            .addChoiceToMemory(request, context, conversationId)
            .mapNotNull { it.message?.content }
        }
      else -> {
        val request = chatRequest()
        createChatCompletion(request)
          .choices
          .addChoiceToMemory(request, context, conversationId)
          .mapNotNull { it.message?.content }
      }
    }
  }

  private suspend fun List<ChoiceWithFunctions>.addChoiceWithFunctionsToMemory(
    request: ChatCompletionRequestWithFunctions,
    context: VectorStore,
    conversationId: ConversationId?
  ): List<ChoiceWithFunctions> = also {
    val firstChoice = firstOrNull()
    val requestUserMessage = request.messages.lastOrNull()
    if (requestUserMessage != null && firstChoice != null && conversationId != null) {
      val role = firstChoice.message?.role?.uppercase()?.let { Role.valueOf(it) } ?: Role.USER
      val requestMemory =
        Memory(
          conversationId = conversationId,
          content = requestUserMessage,
          timestamp = getTimeMillis()
        )
      val firstChoiceMemory =
        Memory(
          conversationId = conversationId,
          content =
            Message(role = role, content = firstChoice.message?.content ?: "", name = role.name),
          timestamp = getTimeMillis()
        )
      context.addMemories(listOf(requestMemory, firstChoiceMemory))
    }
  }

  private suspend fun List<Choice>.addChoiceToMemory(
    request: ChatCompletionRequest,
    context: VectorStore,
    conversationId: ConversationId?
  ): List<Choice> = also {
    val firstChoice = firstOrNull()
    val requestUserMessage = request.messages.lastOrNull()
    if (requestUserMessage != null && firstChoice != null && conversationId != null) {
      val role = firstChoice.message?.role?.name?.uppercase()?.let { Role.valueOf(it) } ?: Role.USER
      val requestMemory =
        Memory(
          conversationId = conversationId,
          content = requestUserMessage,
          timestamp = getTimeMillis()
        )
      val firstChoiceMemory =
        Memory(
          conversationId = conversationId,
          content =
            Message(role = role, content = firstChoice.message?.content ?: "", name = role.name),
          timestamp = getTimeMillis()
        )
      context.addMemories(listOf(requestMemory, firstChoiceMemory))
    }
  }

  private fun messages(memories: List<Memory>, promptWithContext: String): List<Message> =
    memories.reversed().map { it.content } +
      listOf(Message(Role.USER, promptWithContext, Role.USER.name))

  private suspend fun memories(
    conversationId: ConversationId?,
    context: VectorStore,
    promptConfiguration: PromptConfiguration
  ): List<Memory> =
    if (conversationId != null) {
      context.memories(conversationId, promptConfiguration.memoryLimit)
    } else {
      emptyList()
    }

  private fun createPromptWithContextAwareOfTokens(
    memories: List<Memory>,
    ctxInfo: List<String>,
    modelType: ModelType,
    prompt: String,
    minResponseTokens: Int,
  ): String {
    val maxContextLength: Int = modelType.maxContextLength
    val promptTokens: Int = modelType.encoding.countTokens(prompt)
    val memoryTokens = tokensFromMessages(memories.map { it.content })
    val remainingTokens: Int = maxContextLength - promptTokens - memoryTokens - minResponseTokens

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
