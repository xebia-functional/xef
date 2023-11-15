package com.xebia.functional.xef.prompt

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.models.ChatCompletionRole
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestUserMessageContent
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

interface PromptBuilder<T> {
  val items: MutableList<ChatCompletionRequestMessage>

  fun preprocess(elements: List<ChatCompletionRequestMessage>): List<ChatCompletionRequestMessage>

  fun build(): Prompt<T>

  @JvmSynthetic
  operator fun Prompt<T>.unaryPlus() {
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

  fun addPrompt(prompt: Prompt<T>): PromptBuilder<T> = apply { addMessages(prompt.messages) }

  fun addSystemMessage(message: String): PromptBuilder<T> = apply { addMessage(system(message)) }

  fun addAssistantMessage(message: String): PromptBuilder<T> = apply { addMessage(assistant(message)) }

  fun addUserMessage(message: String): PromptBuilder<T> = apply { addMessage(user(message)) }

  fun addMessage(message: ChatCompletionRequestMessage): PromptBuilder<T> = apply {
    val lastMessageWithSameRole: ChatCompletionRequestMessage? =
      items.lastMessageWithSameRole(message)
    if (lastMessageWithSameRole != null) {
      val messageUpdated = lastMessageWithSameRole.addContent(message)
      items.remove(lastMessageWithSameRole)
      items.add(messageUpdated)
    } else {
      items.add(message)
    }
  }

  fun addMessages(messages: List<ChatCompletionRequestMessage>): PromptBuilder<T> = apply {
    val last = items.removeLastOrNull()
    items.addAll(((last?.let { listOf(it) } ?: emptyList()) + messages).flatten())
  }

  companion object {

    operator fun <T> invoke(model: OpenAIModel<T>): PlatformPromptBuilder<T> = PlatformPromptBuilder.create(model)
  }
}

fun String.message(role: ChatCompletionRole): ChatCompletionRequestMessage =
  when (role) {
    ChatCompletionRole.system ->
      ChatCompletionRequestMessage.ChatCompletionRequestSystemMessage(this)
    ChatCompletionRole.user ->
      ChatCompletionRequestMessage.ChatCompletionRequestUserMessage(
        ChatCompletionRequestUserMessageContent.TextContent(this)
      )
    ChatCompletionRole.assistant ->
      ChatCompletionRequestMessage.ChatCompletionRequestAssistantMessage(this)
    ChatCompletionRole.tool ->
      // TODO - Tool Id?
      ChatCompletionRequestMessage.ChatCompletionRequestToolMessage(this, "toolId")
    ChatCompletionRole.function ->
      // TODO - Function name?
      ChatCompletionRequestMessage.ChatCompletionRequestFunctionMessage(this, "functionName")
  }

// TODO this fails because of the ChatCompletionRequestMessage role fixed to function in the
// generator
// check with Fede
inline fun <reified A> A.message(role: ChatCompletionRole): ChatCompletionRequestMessage =
  Json.encodeToString(serializer(), this).message(role)

private fun List<ChatCompletionRequestMessage>.flatten(): List<ChatCompletionRequestMessage> =
  fold(mutableListOf()) { acc, message ->
    val lastMessageWithSameRole: ChatCompletionRequestMessage? =
      acc.lastMessageWithSameRole(message)
    if (lastMessageWithSameRole != null) {
      val messageUpdated = lastMessageWithSameRole.addContent(message)
      acc.remove(lastMessageWithSameRole)
      acc.add(messageUpdated)
    } else {
      acc.add(message)
    }
    acc
  }

private fun ChatCompletionRequestMessage.addContent(
  message: ChatCompletionRequestMessage
): ChatCompletionRequestMessage = "${contentAsString()}\n${message.contentAsString()}".message(completionRole())

private fun List<ChatCompletionRequestMessage>.lastMessageWithSameRole(
  message: ChatCompletionRequestMessage
): ChatCompletionRequestMessage? =
  lastOrNull()?.let { if (it.completionRole() == message.completionRole()) it else null }
