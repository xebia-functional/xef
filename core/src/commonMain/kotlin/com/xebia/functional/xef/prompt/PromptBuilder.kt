package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.llm.models.functions.CFunction
import kotlin.jvm.JvmName

open class PromptBuilder {
  private val items = mutableListOf<Message>()
  private val functions = mutableListOf<CFunction>()

  operator fun Prompt.unaryPlus() {
    +messages
  }

  operator fun Message.unaryPlus() {
    items.add(this)
  }

  operator fun CFunction.unaryPlus() {
    functions.add(this)
  }

  @JvmName("addFunctions")
  operator fun List<CFunction>.unaryPlus() {
    functions.addAll(this)
  }

  operator fun List<Message>.unaryPlus() {
    items.addAll(this)
  }

  protected open fun preprocess(elements: List<Message>): List<Message> = elements

  fun build(): Prompt = Prompt(preprocess(items), functions)
}

fun String.message(role: Role): Message = Message(role, this, role.name)
