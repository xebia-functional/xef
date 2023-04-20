package com.xebia.functional.prompt

import arrow.core.EitherNel
import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate

enum class TemplateFormat(name: String) {
  Jinja2("jinja2"),
  FString("f-string")
}

data class InvalidTemplate(val reason: String)

data class Config private constructor(
  val inputVariables: List<String>,
  val template: String,
  val templateFormat: TemplateFormat = TemplateFormat.FString
) {
  companion object {
    operator fun invoke(template: String, inputVariables: List<String>): EitherNel<InvalidTemplate, Config> =
      either {
        val placeholders = placeholderValues(template)

        zipOrAccumulate(
          { validate(template, inputVariables.toSet() - placeholders.toSet(), "unused") },
          { validate(template, placeholders.toSet() - inputVariables.toSet(), "missing") },
          { validateDuplicated(template, placeholders) }
        ) { _, _, _ -> Config(inputVariables, template) }
      }

    fun orThrow(template: String, inputVariables: List<String>): Config =
      invoke(template, inputVariables).getOrElse { throw IllegalArgumentException(it.all.joinToString(transform = InvalidTemplate::reason)) }
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
  val regex = Regex("""\{([^\{\}]+)\}""")
  return regex.findAll(template).toList().mapNotNull { it.groupValues.firstOrNull() }
}
