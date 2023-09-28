package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.Memory
import com.xebia.functional.xef.store.MessageWithTokens
import io.ktor.util.date.*

internal fun Message.toMessageWithTokens(chat: LLM): MessageWithTokens =
  MessageWithTokens(this, approxTokens = chat.tokensFromMessages(listOf(this)))

internal suspend fun ChatCompletionResponseWithFunctions.addMessagesToMemory(
  chat: LLM,
  scope: Conversation,
  promptMessages: List<Message>,
  startTimeMillis: Long
): ChatCompletionResponseWithFunctions = also {
  val cid = scope.conversationId
  if (choices.isNotEmpty() && cid != null) {

    val assistantMessages = choices.mapNotNull { it.message?.toMessage() }

    val memory =
      chat.createMemory(cid, startTimeMillis, promptMessages, assistantMessages, usage.totalTokens)

    scope.store.addMemories(listOf(memory))
  }
}

internal suspend fun ChatCompletionResponse.addMessagesToMemory(
  llm: LLM,
  scope: Conversation,
  promptMessages: List<Message>,
  startTimeMillis: Long
): ChatCompletionResponse = also {
  val cid = scope.conversationId
  if (choices.isNotEmpty() && cid != null) {

    val assistantMessages =
      choices.mapNotNull {
        it.message?.let {
          val role = it.role.name.uppercase().let { Role.valueOf(it) } // TODO valueOf is unsafe
          Message(role = role, content = it.content, name = role.name)
        }
      }

    val memory =
      llm.createMemory(cid, startTimeMillis, promptMessages, assistantMessages, usage.totalTokens)

    scope.store.addMemories(listOf(memory))
  }
}

suspend fun List<Message>.addAssistantMessagesToMemory(
  llm: LLM,
  scope: Conversation,
  promptMessages: List<Message>,
  startTimeMillis: Long
) {
  val cid = scope.conversationId
  if (cid != null) {
    val memory = llm.createMemory(cid, startTimeMillis, promptMessages, this)

    scope.store.addMemories(listOf(memory))
  }
}

private fun LLM.createMemory(
  cid: ConversationId,
  startTimeMillis: Long,
  promptMessages: List<Message>,
  answerMessages: List<Message>,
  totalTokens: Int? = null
): Memory {
  val p = promptMessages.map { it.toMessageWithTokens(this) }
  val a = answerMessages.map { it.toMessageWithTokens(this) }
  return Memory(
    conversationId = cid,
    timestamp = startTimeMillis,
    request = p,
    aiResponse = a,
    responseTimeInMillis = getTimeMillis() - startTimeMillis,
    tokens = totalTokens ?: (p + a).map { it.approxTokens }.sum()
  )
}
