package com.xebia.functional.xef.prompt

import com.xebia.functional.openai.models.ChatCompletionRequestMessage
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.jvm.JvmSynthetic

interface PromptBuilder {
  val items: MutableList<ChatCompletionRequestMessage>

  fun preprocess(elements: List<ChatCompletionRequestMessage>): List<ChatCompletionRequestMessage>

  fun build(): Prompt

  @JvmSynthetic
  operator fun Prompt.unaryPlus() {
    +messages
  }

  @JvmSynthetic
  operator fun ChatCompletionRequestMessage.unaryPlus() {
    addMessage(this)
  }

  @JvmSynthetic
  operator fun List<ChatCompletionRequestMessage>.unaryPlus() {
    addMessages(this)
  }

  fun addPrompt(prompt: Prompt): PromptBuilder = apply { addMessages(prompt.messages) }

  fun addSystemMessage(message: String): PromptBuilder = apply { addMessage(system(message)) }

  fun addAssistantMessage(message: String): PromptBuilder = apply { addMessage(assistant(message)) }

  fun addUserMessage(message: String): PromptBuilder = apply { addMessage(user(message)) }

  fun addMessage(message: ChatCompletionRequestMessage): PromptBuilder = apply {
    val lastMessageWithSameRole: ChatCompletionRequestMessage? = items.lastMessageWithSameRole(message)
    if (lastMessageWithSameRole != null) {
      val messageUpdated = lastMessageWithSameRole.addContent(message)
      items.remove(lastMessageWithSameRole)
      items.add(messageUpdated)
    } else {
      items.add(message)
    }
  }

  fun addMessages(messages: List<ChatCompletionRequestMessage>): PromptBuilder = apply {
    val last = items.removeLastOrNull()
    items.addAll(((last?.let { listOf(it) } ?: emptyList()) + messages).flatten())
  }

  companion object {

    operator fun invoke(): PlatformPromptBuilder = PlatformPromptBuilder.create()
  }
}

fun String.message(role: ChatCompletionRequestMessage.Role): ChatCompletionRequestMessage = ChatCompletionRequestMessage(
  role = role,
  content = this,
  name = role.name,
  toolCallId = )

//TODO this fails because of the ChatCompletionRequestMessage role fixed to function in the generator
// check with Fede
inline fun <reified A> A.message(role: Role): ChatCompletionRequestMessage =
  Message(role, Json.encodeToString(serializer(), this), role.name)

private fun List<ChatCompletionRequestMessage>.flatten(): List<ChatCompletionRequestMessage> =
  fold(mutableListOf()) { acc, message ->
    val lastMessageWithSameRole: ChatCompletionRequestMessage? = acc.lastMessageWithSameRole(message)
    if (lastMessageWithSameRole != null) {
      val messageUpdated = lastMessageWithSameRole.addContent(message)
      acc.remove(lastMessageWithSameRole)
      acc.add(messageUpdated)
    } else {
      acc.add(message)
    }
    acc
  }

private fun ChatCompletionRequestMessage.addContent(message: ChatCompletionRequestMessage): ChatCompletionRequestMessage =
  copy(content = "${content}\n${message.content}")

private fun List<ChatCompletionRequestMessage>.lastMessageWithSameRole(message: ChatCompletionRequestMessage): ChatCompletionRequestMessage? =
  lastOrNull()?.let { if (it.role == message.role) it else null }
