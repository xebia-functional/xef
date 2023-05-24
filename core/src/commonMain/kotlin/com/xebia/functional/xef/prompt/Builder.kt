package com.xebia.functional.xef.prompt

open class PromptBuilder {
  private val items = mutableListOf<Prompt>()

  val emptyLine: String = ""

  operator fun Prompt.unaryPlus() {
    items.add(this)
  }

  operator fun String.unaryPlus() {
    items.add(Prompt(this))
  }

  protected open fun preprocess(elements: List<Prompt>): List<Prompt> = elements

  fun build(): Prompt =
    buildString { preprocess(items).forEach { appendLine(it.message) } }.let { Prompt(it) }
}

fun buildPrompt(block: PromptBuilder.() -> Unit): Prompt = PromptBuilder().apply { block() }.build()
