package com.xebia.functional.xef.prompt

import arrow.core.fold
import kotlin.jvm.JvmInline

@JvmInline value class Prompt(val message: String)

fun String.prompt(): Prompt = Prompt(this)

fun Prompt.prepend(text: String) = Prompt(text + message)

operator fun Prompt.plus(other: Prompt): Prompt = Prompt(message + other.message)

operator fun Prompt.plus(text: String): Prompt = Prompt(message + text)

fun Prompt.append(text: String) = this + text

fun Prompt.format(variables: Map<String, String>): Prompt =
  Prompt(variables.fold(message) { acc, (key, value) -> acc.replace("{$key}", value) })
