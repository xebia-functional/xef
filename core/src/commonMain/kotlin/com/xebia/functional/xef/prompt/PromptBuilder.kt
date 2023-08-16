package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role

open class PromptBuilder {
  private val items = mutableListOf<Message>()

  operator fun Prompt.unaryPlus() {
    +messages
  }

  operator fun Message.unaryPlus() {
    items.add(this)
  }

  operator fun List<Message>.unaryPlus() {
    items.addAll(this)
  }

  protected open fun preprocess(elements: List<Message>): List<Message> = elements

  fun build(): Prompt = Prompt(preprocess(items))
}

fun String.message(role: Role): Message = Message(role, this, role.name)

fun buildPrompt(block: PromptBuilder.() -> Unit): Prompt = PromptBuilder().apply { block() }.build()
