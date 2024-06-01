package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.ChatCompletionRequestMessage
import com.xebia.functional.xef.llm.FunctionObject
import com.xebia.functional.xef.llm.Role
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
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

  private fun ChatCompletionRequestMessage.addContent(
    message: ChatCompletionRequestMessage
  ): ChatCompletionRequestMessage {
    val content = "${content}\n${message.content}"
    return when (role) {
      Role.system -> system(content)
      Role.user -> user(content)
      Role.assistant -> assistant(content)
      Role.tool -> error("Tool role is not supported")
    }
  }

  @JvmSynthetic
  operator fun List<ChatCompletionRequestMessage>.unaryPlus() {
    addMessages(this)
  }

  fun addMessage(message: ChatCompletionRequestMessage): PromptBuilder = apply {
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

  fun addMessages(messages: List<ChatCompletionRequestMessage>): PromptBuilder = apply {
    val last = items.removeLastOrNull()
    items.addAll(((last?.let { listOf(it) } ?: emptyList()) + messages).flatten())
  }

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

  private fun List<ChatCompletionRequestMessage>.lastMessageWithSameRole(
    message: ChatCompletionRequestMessage
  ): ChatCompletionRequestMessage? =
    lastOrNull()?.let { if (it.role == message.role) it else null }

  companion object {

    operator fun invoke(
      functions: List<FunctionObject>,
      configuration: PromptConfiguration
    ): PlatformPromptBuilder = PlatformPromptBuilder.create(functions, configuration)

    fun assistant(value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage(
        role = Role.assistant,
        content = value,
        toolCallResults = null
      )

    fun user(value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage(
        role = Role.user,
        content = value,
        toolCallResults = null
      )

    fun system(value: String): ChatCompletionRequestMessage =
      ChatCompletionRequestMessage(
        role = Role.system,
        content = value,
        toolCallResults = null
      )

  }
}
