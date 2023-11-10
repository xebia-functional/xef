package com.xebia.functional.xef.llm

import com.xebia.functional.openai.models.ChatCompletionRequestMessage
import com.xebia.functional.openai.models.ChatCompletionResponseMessage
import com.xebia.functional.openai.models.ChatCompletionRole
import com.xebia.functional.openai.models.CreateChatCompletionResponseChoicesInner
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.MessagesToHistory
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.MemorizedMessage
import com.xebia.functional.xef.store.Memory
import com.xebia.functional.xef.store.VectorStore

internal suspend fun List<ChatCompletionRequestMessage>.addToMemory(scope: Conversation, history: MessagesToHistory) {
  val cid = scope.conversationId
  if (history != MessagesToHistory.NONE && cid != null) {
    val memories = toMemory(scope)
    scope.store.addMemoriesByHistory(history, memories)
  }
}

internal fun ChatCompletionResponseMessage.toMemory(cid: ConversationId, index: Int): Memory =
  Memory(conversationId = cid, content = MemorizedMessage.Response(this), index = index)

internal fun ChatCompletionRequestMessage.toMemory(cid: ConversationId, index: Int): Memory =
  Memory(conversationId = cid, content = MemorizedMessage.Request(this), index = index)

internal fun List<ChatCompletionRequestMessage>.toMemory(scope: Conversation): List<Memory> {
  val cid = scope.conversationId
  return if (cid != null) {
    map { it.toMemory(cid, scope.store.incrementIndexAndGet()) }
  } else emptyList()
}

internal suspend fun List<CreateChatCompletionResponseChoicesInner>.addChoiceWithFunctionsToMemory(
  scope: Conversation,
  previousMemories: List<Memory>,
  history: MessagesToHistory
): List<CreateChatCompletionResponseChoicesInner> = also {
  val cid = scope.conversationId
  if (history != MessagesToHistory.NONE && isNotEmpty() && cid != null) {
    val aiMemory =
      this.mapNotNull { it.message }
        .map { it:toMessage().toMemory(cid, scope.store.incrementIndexAndGet()) }
    val newMessages = previousMemories + aiMemory
    scope.store.addMemoriesByHistory(history, newMessages)
  }
}

internal suspend fun List<CreateChatCompletionResponseChoicesInner>.addChoiceToMemory(
  scope: Conversation,
  previousMemories: List<Memory>,
  history: MessagesToHistory
): List<CreateChatCompletionResponseChoicesInner> = also {
  val cid = scope.conversationId
  if (history != MessagesToHistory.NONE && isNotEmpty() && cid != null) {
    val aiMemory =
      this.mapNotNull { it.message }.map { it.toMemory(cid, scope.store.incrementIndexAndGet()) }
    val newMessages = previousMemories + aiMemory
    scope.store.addMemoriesByHistory(history, newMessages)
  }
}

suspend fun VectorStore.addMemoriesByHistory(history: MessagesToHistory, memories: List<Memory>) {
  when (history) {
    MessagesToHistory.ALL -> {
      addMemories(memories)
    }
    MessagesToHistory.ONLY_SYSTEM_MESSAGES -> {
      addMemories(memories.filter { it.content.role == ChatCompletionRole.assistant })
    }
    MessagesToHistory.NOT_SYSTEM_MESSAGES -> {
      addMemories(memories.filter { it.content.role != ChatCompletionRole.system })
    }
    MessagesToHistory.NONE -> {}
  }
}
