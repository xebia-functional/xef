package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.store.Memory
import io.ktor.util.date.*

internal suspend fun List<Message>.addToMemory(chat: LLM, scope: Conversation) {
  val memories = toMemory(chat, scope)
  if (memories.isNotEmpty()) {
    scope.store.addMemories(memories)
  }
}

internal fun List<Message>.toMemory(chat: LLM, scope: Conversation): List<Memory> {
  val cid = scope.conversationId
  return if (cid != null) {
    mapIndexed { delta, it ->
      Memory(
        conversationId = cid,
        content = it,
        // We are adding the delta to ensure that the timestamp is unique for every message.
        // With this, we ensure that the order of the messages is preserved.
        // We assume that the AI response time will be in the order of seconds.
        timestamp = getTimeMillis() + delta,
        approxTokens = chat.tokensFromMessages(listOf(it))
      )
    }
  } else emptyList()
}

internal suspend fun List<ChoiceWithFunctions>.addMessagesToMemory(
  chat: LLM,
  scope: Conversation,
  previousMemories: List<Memory>
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

    val newMessages = previousMemories + listOf(firstChoiceMessage).toMemory(chat, scope)
    scope.store.addMemories(newMessages)
  }
}

internal suspend fun List<Choice>.addMessagesToMemory(
  chat: Chat,
  scope: Conversation,
  previousMemories: List<Memory>
): List<Choice> = also {
  val firstChoice = firstOrNull()
  val cid = scope.conversationId
  if (firstChoice != null && cid != null) {
    val role = firstChoice.message?.role?.name?.uppercase()?.let { Role.valueOf(it) } ?: Role.USER

    val firstChoiceMessage =
      Message(role = role, content = firstChoice.message?.content ?: "", name = role.name)

    val newMessages = previousMemories + listOf(firstChoiceMessage).toMemory(chat, scope)
    scope.store.addMemories(newMessages)
  }
}
