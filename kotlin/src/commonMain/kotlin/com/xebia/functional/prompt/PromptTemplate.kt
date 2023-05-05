package com.xebia.functional.prompt

import arrow.core.raise.Raise

fun Raise<InvalidTemplate>.PromptTemplate(
  examples: List<String>,
  suffix: String,
  variables: List<String>,
  prefix: String
): PromptTemplate {
  val template = """|$prefix
      |
      |${examples.joinToString(separator = "\n")}
      |
      |$suffix""".trimMargin()
  return PromptTemplate(Config(template, variables))
}

fun Raise<InvalidTemplate>.PromptTemplate(template: String, variables: List<String>): PromptTemplate =
  PromptTemplate(Config(template, variables))

interface PromptTemplate {
  val inputKeys: List<String>
  suspend fun format(variables: Map<String, String>): String

  companion object {
    operator fun invoke(config: Config): PromptTemplate = object : PromptTemplate {
      override val inputKeys: List<String> = config.inputVariables

      override suspend fun format(variables: Map<String, String>): String {
        val mergedArgs = mergePartialAndUserVariables(variables, config.inputVariables)
        return when (config.templateFormat) {
          TemplateFormat.FString -> {
            val sortedArgs = mergedArgs.toList().sortedBy { it.first }
            sortedArgs.fold(config.template) { acc, (k, v) -> acc.replace("{$k}", v) }
          }
        }
      }

      private fun mergePartialAndUserVariables(
        variables: Map<String, String>,
        inputVariables: List<String>
      ): Map<String, String> =
        inputVariables.fold(variables) { acc, k ->
          if (!acc.containsKey(k)) acc + (k to "{$k}") else acc
        }
    }
  }
}
