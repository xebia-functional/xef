package com.xebia.functional.xef.prompt

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.fold
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.recover
import arrow.core.raise.zipOrAccumulate
import kotlin.jvm.JvmInline

data class InvalidTemplate(val reason: String)

@JvmInline
value class PromptTemplate private constructor(val template: String) {
  fun build(variables: Map<String, String>): Either<InvalidTemplate, Prompt<String>> = either {
    val placeholders = placeholderValues(template)
    recover<NonEmptyList<InvalidTemplate>, Unit>({
      zipOrAccumulate(
        { validate(template, variables.keys - placeholders.toSet(), "unused") },
        { validate(template, placeholders.toSet() - variables.keys, "missing") },
        { validateDuplicated(template, placeholders) }
      ) { _, _, _ -> }
    }) { raise(InvalidTemplate(it.joinToString(transform = InvalidTemplate::reason))) }
    Prompt(variables.fold(template) { acc, (key, value) ->
      acc.replace("{$key}", value)
    })
  }

  companion object {
    fun either(template: String): Either<InvalidTemplate, PromptTemplate> = either {
      val placeholders = placeholderValues(template)
      validateDuplicated(template, placeholders)
      PromptTemplate(template)
    }
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
