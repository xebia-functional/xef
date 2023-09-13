package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

interface PromptBuilder {
  val items: MutableList<Message>

  fun preprocess(elements: List<Message>): List<Message>

  fun build(): Prompt

  @JvmSynthetic
  operator fun Prompt.unaryPlus() {
    +messages
  }

  @JvmSynthetic
  operator fun Message.unaryPlus() {
    addMessage(this)
  }

  @JvmSynthetic
  operator fun List<Message>.unaryPlus() {
    addMessages(this)
  }

  fun addPrompt(prompt: Prompt): PromptBuilder = apply { addMessages(prompt.messages) }

  fun addSystemMessage(message: String): PromptBuilder = apply { addMessage(system(message)) }

  fun addAssistantMessage(message: String): PromptBuilder = apply { addMessage(assistant(message)) }

  fun addUserMessage(message: String): PromptBuilder = apply { addMessage(user(message)) }

  fun addMessage(message: Message): PromptBuilder = apply { items.add(message) }

  fun addMessages(messages: List<Message>): PromptBuilder = apply { items.addAll(messages) }

  companion object {

    operator fun invoke(): PlatformPromptBuilder = PlatformPromptBuilder.create()
  }
}

fun String.message(role: Role): Message = Message(role, this, role.name)

inline fun <reified A> A.message(role: Role): Message =
  Message(role, Json.encodeToString(serializer(), this), role.name)

fun Prompt.flatten(): Prompt =
  Prompt(
    messages.fold(mutableListOf()) { acc, message ->
      val lastMessageWithSameRole: Message? = acc.lastMessageWithSameRole(message)
      if (lastMessageWithSameRole != null) {
        val messageUpdated =
          lastMessageWithSameRole.copy(
            content = "${lastMessageWithSameRole.content}\n${message.content}"
          )
        acc.remove(lastMessageWithSameRole)
        acc.add(messageUpdated)
      } else {
        acc.add(message)
      }
      acc
    }
  )

private fun List<Message>.lastMessageWithSameRole(message: Message): Message? =
  lastOrNull()?.let { if (it.role == message.role) it else null }
