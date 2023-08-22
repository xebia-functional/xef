package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import kotlin.jvm.JvmSynthetic

open class PromptBuilder {
  private val items = mutableListOf<Message>()

  fun addPrompt(prompt: Prompt): PromptBuilder = apply { items.addAll(prompt.messages) }

  @JvmSynthetic
  operator fun Prompt.unaryPlus() {
    +messages
  }

  fun addMessage(message: Message): PromptBuilder = apply { items.add(message) }

  fun addSystemMessage(message: String): PromptBuilder = apply { items.add(system(message)) }

  fun addAssistantMessage(message: String): PromptBuilder = apply { items.add(assistant(message)) }

  fun addUserMessage(message: String): PromptBuilder = apply { items.add(user(message)) }

  @JvmSynthetic
  operator fun Message.unaryPlus() {
    items.add(this)
  }

  fun addMessages(messages: List<Message>): PromptBuilder = apply { items.addAll(messages) }

  @JvmSynthetic
  operator fun List<Message>.unaryPlus() {
    items.addAll(this)
  }

  protected open fun preprocess(elements: List<Message>): List<Message> = elements

  fun build(): Prompt = Prompt(preprocess(items), null)
}

fun String.message(role: Role): Message = Message(role, this, role.name)
