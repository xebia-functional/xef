package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.Choice
import com.xebia.functional.xef.llm.models.chat.ChoiceWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.Memory

internal suspend fun List<Message>.addToMemory(scope: Conversation) {
  val cid = scope.conversationId
  if (cid != null) {
    val memories = toMemory(scope)
    if (memories.isNotEmpty()) {
      scope.store.addMemories(memories)
    }
  }
}

internal fun Message.toMemory(cid: ConversationId, index: Int): Memory =
  Memory(conversationId = cid, content = this, index = index)

internal fun List<Message>.toMemory(scope: Conversation): List<Memory> {
  val cid = scope.conversationId
  return if (cid != null) {
    map { it.toMemory(cid, scope.store.incrementIndexAndGet()) }
  } else emptyList()
}

internal suspend fun List<ChoiceWithFunctions>.addChoiceWithFunctionsToMemory(
  scope: Conversation,
  previousMemories: List<Memory>
): List<ChoiceWithFunctions> = also {
  val cid = scope.conversationId
  if (isNotEmpty() && cid != null) {
    val aiMemory =
      this.mapNotNull { it.message }
        .map { it.toMessage().toMemory(cid, scope.store.incrementIndexAndGet()) }
    val newMessages = previousMemories + aiMemory
    scope.store.addMemories(newMessages)
  }
}

internal suspend fun List<Choice>.addChoiceToMemory(
  scope: Conversation,
  previousMemories: List<Memory>
): List<Choice> = also {
  val cid = scope.conversationId
  if (isNotEmpty() && cid != null) {
    val aiMemory =
      this.mapNotNull { it.message }.map { it.toMemory(cid, scope.store.incrementIndexAndGet()) }
    val newMessages = previousMemories + aiMemory
    scope.store.addMemories(newMessages)
  }
}
