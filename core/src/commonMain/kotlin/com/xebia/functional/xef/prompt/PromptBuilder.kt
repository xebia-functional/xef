package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role

open class PromptBuilderBuilder {
  private val items = mutableListOf<Message>()

  operator fun Message.unaryPlus() {
    items.add(this)
  }

  operator fun List<Message>.unaryPlus() {
    items.addAll(this)
  }

  protected open fun preprocess(elements: List<Message>): List<Message> = elements

  fun build(): List<Message> = preprocess(items)
}

fun String.message(role: Role): Message = Message(role, this, role.name)

fun buildPrompt(block: PromptBuilderBuilder.() -> Unit): List<Message> =
  PromptBuilderBuilder().apply { block() }.build()
