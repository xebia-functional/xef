package com.xebia.functional.prompt

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import kotlinx.serialization.Serializable

enum class Type {
  human,
  ai,
  system,
  chat
}

@Serializable
sealed class Message {
  abstract val content: String

  abstract fun format(): String

  fun type(): Type =
    when (this) {
      is HumanMessage -> Type.human
      is AIMessage -> Type.ai
      is SystemMessage -> Type.system
      is ChatMessage -> Type.chat
    }
}

data class HumanMessage(override val content: String) : Message() {
  override fun format(): String = "${type().name.capitalized()}: $content"
}

data class AIMessage(override val content: String) : Message() {
  override fun format(): String = "${type().name.uppercase()}: $content"
}

data class SystemMessage(override val content: String) : Message() {
  override fun format(): String = "${type().name.capitalized()}: $content"
}

data class ChatMessage(override val content: String, val role: String) : Message() {
  override fun format(): String = "$role: $content"
}

enum class TemplateFormat {
  FString
}

data class InvalidTemplate(val reason: String)

fun Raise<InvalidTemplate>.Config(template: String, inputVariables: List<String>): Config =
  Config.either(template, inputVariables).bind()

class Config
private constructor(
  val inputVariables: List<String>,
  val template: String,
  val templateFormat: TemplateFormat = TemplateFormat.FString
) {
  companion object {
    // We cannot define `operator fun invoke` with `Raise` without context receivers,
    // so we define an intermediate `Either` based function.
    // This is because adding `Raise<InvalidTemplate>` results in 2 receivers.
    fun either(template: String, variables: List<String>): Either<InvalidTemplate, Config> =
      either<NonEmptyList<InvalidTemplate>, Config> {
          val placeholders = placeholderValues(template)

          zipOrAccumulate(
            { validate(template, variables.toSet() - placeholders.toSet(), "unused") },
            { validate(template, placeholders.toSet() - variables.toSet(), "missing") },
            { validateDuplicated(template, placeholders) }
          ) { _, _, _ ->
            Config(variables, template)
          }
        }
        .mapLeft { InvalidTemplate(it.joinToString(transform = InvalidTemplate::reason)) }
  }
}

private fun Raise<InvalidTemplate>.validate(
  template: String,
  diffSet: Set<String>,
  msg: String
): Unit =
  ensure(diffSet.isEmpty()) {
    InvalidTemplate(
      "Template '$template' has $msg arguments: ${diffSet.joinToString(", ") { "{$it}" }}"
    )
  }

private fun Raise<InvalidTemplate>.validateDuplicated(
  template: String,
  placeholders: List<String>
) {
  val args = placeholders.groupBy { it }.filter { it.value.size > 1 }.keys
  ensure(args.isEmpty()) {
    InvalidTemplate(
      "Template '$template' has duplicate arguments: ${args.joinToString(", ") { "{$it}" }}"
    )
  }
}

private fun placeholderValues(template: String): List<String> {
  @Suppress("RegExpRedundantEscape") val regex = Regex("""\{([^\{\}]+)\}""")
  return regex.findAll(template).toList().mapNotNull { it.groupValues.getOrNull(1) }
}

private fun String.capitalized(): String = replaceFirstChar {
  if (it.isLowerCase()) it.titlecase() else it.toString()
}
