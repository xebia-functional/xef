package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import kotlin.jvm.JvmSynthetic

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
    items.add(this)
  }

  @JvmSynthetic
  operator fun List<Message>.unaryPlus() {
    items.addAll(this)
  }

  fun addPrompt(prompt: Prompt): PromptBuilder = apply { items.addAll(prompt.messages) }

  fun addMessage(message: Message): PromptBuilder = apply { items.add(message) }

  fun addSystemMessage(message: String): PromptBuilder = apply { items.add(system(message)) }

  fun addAssistantMessage(message: String): PromptBuilder = apply { items.add(assistant(message)) }

  fun addUserMessage(message: String): PromptBuilder = apply { items.add(user(message)) }

  fun addMessages(messages: List<Message>): PromptBuilder = apply { items.addAll(messages) }

  companion object {

    operator fun invoke(): PlatformPromptBuilder = PlatformPromptBuilder.create()
  }
}

fun String.message(role: Role): Message = Message(role, this, role.name)
