package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.store.Memory
import io.ktor.util.date.*

internal object MemoryManagement {

  internal suspend fun addMemoriesAfterStream(
    chat: Chat,
    request: ChatCompletionRequest,
    scope: Conversation,
    messages: List<Message>,
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
      val responseMemories =
        messages.map {
          Memory(
            conversationId = cid,
            content = it,
            timestamp = getTimeMillis(),
            approxTokens = chat.tokensFromMessages(messages)
          )
        }
      scope.store.addMemories(listOf(requestMemory) + responseMemories)
    }
  }

  internal suspend fun List<ChoiceWithFunctions>.addChoiceWithFunctionsToMemory(
    chat: Chat,
    request: ChatCompletionRequest,
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
