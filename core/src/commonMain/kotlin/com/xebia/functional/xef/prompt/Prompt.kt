package com.xebia.functional.xef.prompt

import kotlin.jvm.JvmInline

fun Prompt(examples: List<String>, suffix: String, prefix: String): Prompt =
  Prompt(
    """|$prefix
     |
     |${examples.joinToString(separator = "\n")}
     |
     |$suffix"""
      .trimMargin()
  )

@JvmInline value class Prompt(val message: String)

fun Prompt.prepend(text: String) = Prompt(text + message)

operator fun Prompt.plus(other: Prompt): Prompt = Prompt(message + other.message)

operator fun Prompt.plus(text: String): Prompt = Prompt(message + text)

fun Prompt.append(text: String) = this + text
