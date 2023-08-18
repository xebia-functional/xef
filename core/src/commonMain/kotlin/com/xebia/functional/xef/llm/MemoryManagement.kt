package com.xebia.functional.xef.llm

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.vectorstores.Memory
import io.ktor.util.date.*

internal object MemoryManagement {

  internal suspend fun addMemoriesAfterStream(
    chat: Chat,
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
          approxTokens = chat.tokensFromMessages(listOf(lastRequestMessage))
        )
      val responseMessage =
        Message(role = Role.ASSISTANT, content = buffer.toString(), name = Role.ASSISTANT.name)
      val responseMemory =
        Memory(
          conversationId = cid,
          content = responseMessage,
          timestamp = getTimeMillis(),
          approxTokens = chat.tokensFromMessages(listOf(responseMessage))
        )
      scope.store.addMemories(listOf(requestMemory, responseMemory))
    }
  }

  internal suspend fun List<ChoiceWithFunctions>.addChoiceWithFunctionsToMemory(
    chat: Chat,
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
          approxTokens = chat.tokensFromMessages(listOf(requestUserMessage))
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
          approxTokens = chat.tokensFromMessages(listOf(firstChoiceMessage))
        )
      scope.store.addMemories(listOf(requestMemory, firstChoiceMemory))
    }
  }

  internal suspend fun List<Choice>.addChoiceToMemory(
    chat: Chat,
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
          approxTokens = chat.tokensFromMessages(listOf(requestUserMessage))
        )
      val firstChoiceMessage =
        Message(role = role, content = firstChoice.message?.content ?: "", name = role.name)
      val firstChoiceMemory =
        Memory(
          conversationId = cid,
          content = firstChoiceMessage,
          timestamp = getTimeMillis(),
          approxTokens = chat.tokensFromMessages(listOf(firstChoiceMessage))
        )
      scope.store.addMemories(listOf(requestMemory, firstChoiceMemory))
    }
  }
}