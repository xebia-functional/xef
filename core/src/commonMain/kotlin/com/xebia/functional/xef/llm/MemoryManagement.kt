package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.MessagesToHistory
import com.xebia.functional.xef.llm.models.chat.Choice
import com.xebia.functional.xef.llm.models.chat.ChoiceWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.Memory

internal suspend fun List<Message>.addToMemory(scope: Conversation, history: MessagesToHistory) {
  val cid = scope.conversationId
  if (history != MessagesToHistory.NONE && cid != null) {
    val memories = toMemory(scope)
    if (memories.isNotEmpty()) {
      scope.store.addMemories(memories)
    }
  }
}

internal fun Message.toMemory(cid: ConversationId, index: Int): Memory =
  Memory(conversationId = cid, content = copy(name = role.name), index = index)

internal fun List<Message>.toMemory(scope: Conversation): List<Memory> {
  val cid = scope.conversationId
  return if (cid != null) {
    map { it.toMemory(cid, scope.store.incrementIndexAndGet()) }
  } else emptyList()
}

internal suspend fun List<ChoiceWithFunctions>.addChoiceWithFunctionsToMemory(
  scope: Conversation,
  previousMemories: List<Memory>,
  history: MessagesToHistory
): List<ChoiceWithFunctions> = also {
  val cid = scope.conversationId
  if (history != MessagesToHistory.NONE && isNotEmpty() && cid != null) {
    val aiMemory =
      this.mapNotNull { it.message }
        .map { it.toMessage().toMemory(cid, scope.store.incrementIndexAndGet()) }
    val newMessages = previousMemories + aiMemory
    scope.store.addMemories(newMessages)
  }
}

internal suspend fun List<Choice>.addChoiceToMemory(
  scope: Conversation,
  previousMemories: List<Memory>,
  history: MessagesToHistory
): List<Choice> = also {
  val cid = scope.conversationId
  if (history != MessagesToHistory.NONE && isNotEmpty() && cid != null) {
    val aiMemory =
      this.mapNotNull { it.message }.map { it.toMemory(cid, scope.store.incrementIndexAndGet()) }
    val newMessages = previousMemories + aiMemory
    scope.store.addMemories(newMessages)
  }
}
