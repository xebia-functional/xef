package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.store.Memory
import io.ktor.util.date.*

internal object MemoryManagement {

  internal suspend fun addMessagesToMemory(
    chat: LLM,
    messages: List<Message>,
    scope: Conversation
  ) {
    val cid = scope.conversationId
    if (cid != null) {
      val memories =
        messages.map {
          Memory(
            conversationId = cid,
            content = it,
            timestamp = getTimeMillis(),
            approxTokens = chat.tokensFromMessages(listOf(it))
          )
        }
      scope.store.addMemories(memories)
    }
  }

  internal suspend fun List<ChoiceWithFunctions>.addChoiceWithFunctionsToMemory(
    chat: LLM,
    requestUserMessages: List<Message>,
    scope: Conversation
  ): List<ChoiceWithFunctions> = also {
    val firstChoice = firstOrNull()
    val cid = scope.conversationId
    if (firstChoice != null && cid != null) {
      val role = firstChoice.message?.role?.uppercase()?.let { Role.valueOf(it) } ?: Role.USER

      val firstChoiceMessage =
        Message(
          role = role,
          content = firstChoice.message?.content
              ?: firstChoice.message?.functionCall?.arguments ?: "",
          name = role.name
        )

      val newMessages = requestUserMessages + firstChoiceMessage
      addMessagesToMemory(chat, newMessages, scope)
    }
  }

  internal suspend fun List<Choice>.addChoiceToMemory(
    chat: Chat,
    messages: List<Message>,
    scope: Conversation
  ): List<Choice> = also {
    val firstChoice = firstOrNull()
    val cid = scope.conversationId
    if (firstChoice != null && cid != null) {
      val role = firstChoice.message?.role?.name?.uppercase()?.let { Role.valueOf(it) } ?: Role.USER

      val firstChoiceMessage =
        Message(role = role, content = firstChoice.message?.content ?: "", name = role.name)

      val newMessages = messages + firstChoiceMessage
      addMessagesToMemory(chat, newMessages, scope)
    }
  }
}
