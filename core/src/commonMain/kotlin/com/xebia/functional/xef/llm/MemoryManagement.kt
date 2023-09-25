package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.Choice
import com.xebia.functional.xef.llm.models.chat.ChoiceWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.Memory
import io.ktor.util.date.*

internal suspend fun List<Message>.addToMemory(chat: LLM, scope: Conversation) {
  val memories = toMemory(chat, scope)
  if (memories.isNotEmpty()) {
    scope.store.addMemories(memories)
  }
}

internal fun Message.toMemory(cid: ConversationId, chat: LLM, delta: Int = 0): Memory =
  Memory(
    conversationId = cid,
    content = this,
    timestamp = getTimeMillis() + delta,
    approxTokens = chat.tokensFromMessages(listOf(this))
  )

internal fun List<Message>.toMemory(chat: LLM, scope: Conversation): List<Memory> {
  val cid = scope.conversationId
  return if (cid != null) {
    mapIndexed { index, it -> it.toMemory(cid, chat, index) }
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

    // Temporary solution to avoid duplicate timestamps when calling the AI.
    val aiMemory =
      firstChoiceMessage.toMemory(cid, chat).let {
        if (previousMemories.isNotEmpty() && previousMemories.last().timestamp >= it.timestamp) {
          it.copy(timestamp = previousMemories.last().timestamp + 1)
        } else it
      }

    val newMessages = previousMemories + aiMemory
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

    // Temporary solution to avoid duplicate timestamps when calling the AI.
    val aiMemory =
      firstChoiceMessage.toMemory(cid, chat).let {
        if (previousMemories.isNotEmpty() && previousMemories.last().timestamp >= it.timestamp) {
          it.copy(timestamp = previousMemories.last().timestamp + 1)
        } else it
      }

    val newMessages = previousMemories + aiMemory
    scope.store.addMemories(newMessages)
  }
}
