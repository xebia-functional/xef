package com.xebia.functional.prompt

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate

enum class TemplateFormat {
  FString
}

data class InvalidTemplate(val reason: String)

fun Raise<InvalidTemplate>.Config(template: String, inputVariables: List<String>): Config =
  Config.either(template, inputVariables).bind()

class Config private constructor(
  val inputVariables: List<String>,
  val template: String,
  val templateFormat: TemplateFormat = TemplateFormat.FString
) {
  companion object {
    // We cannot define `operator fun invoke` with `Raise` without context receivers,
    // so we define an intermediate `Either` based function.
    // This is because adding `Raise<InvalidTemplate>` results in 2 receivers.
    fun either(template: String, inputVariables: List<String>): Either<InvalidTemplate, Config> =
      either<NonEmptyList<InvalidTemplate>, Config> {
        val placeholders = placeholderValues(template)

        zipOrAccumulate(
          { validate(template, inputVariables.toSet() - placeholders.toSet(), "unused") },
          { validate(template, placeholders.toSet() - inputVariables.toSet(), "missing") },
          { validateDuplicated(template, placeholders) }
        ) { _, _, _ -> Config(inputVariables, template) }
      }.mapLeft { InvalidTemplate(it.joinToString(transform = InvalidTemplate::reason)) }
  }
}

private fun Raise<InvalidTemplate>.validate(template: String, diffSet: Set<String>, msg: String): Unit =
  ensure(diffSet.isEmpty()) {
    InvalidTemplate("Template '$template' has $msg arguments: ${diffSet.joinToString(", ")}")
  }

private fun Raise<InvalidTemplate>.validateDuplicated(template: String, placeholders: List<String>) {
  val args = placeholders.groupBy { it }.filter { it.value.size > 1 }.keys
  ensure(args.isEmpty()) {
    InvalidTemplate("Template '$template' has duplicate arguments: ${args.joinToString(", ")}")
  }
}

private fun placeholderValues(template: String): List<String> {
  @Suppress("RegExpRedundantEscape")
  val regex = Regex("""\{([^\{\}]+)\}""")
  return regex.findAll(template).toList().mapNotNull { it.groupValues.firstOrNull() }
}
