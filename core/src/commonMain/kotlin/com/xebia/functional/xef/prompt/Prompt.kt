package com.xebia.functional.xef.prompt

import kotlin.jvm.JvmInline

fun Prompt(
  examples: List<String>,
  suffix: String,
  prefix: String
): Prompt<String> = Prompt(
  """|$prefix
     |
     |${examples.joinToString(separator = "\n")}
     |
     |$suffix""".trimMargin()
)

@JvmInline
value class Prompt<A>(val message: A) {
  fun <B> map(transform: (A) -> B): Prompt<B> =
    Prompt(transform(message))

  companion object {
    fun human(prompt: Prompt<String>): Prompt<HumanMessage> =
      prompt.map(::HumanMessage)

    fun ai(prompt: Prompt<String>): Prompt<AIMessage> =
      prompt.map(::AIMessage)

    fun system(prompt: Prompt<String>): Prompt<SystemMessage> =
      prompt.map(::SystemMessage)

    fun chat(prompt: Prompt<String>, role: String): Prompt<ChatMessage> =
      prompt.map { ChatMessage(it, role) }
  }
}

fun Prompt<String>.prepend(text: String) =
  Prompt(text + message)

operator fun Prompt<String>.plus(other: Prompt<String>): Prompt<String> =
  Prompt(message + other.message)

operator fun Prompt<String>.plus(text: String): Prompt<String> =
  Prompt(message + text)

fun Prompt<String>.append(text: String) =
  this + text
