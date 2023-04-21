package com.xebia.functional.prompt

import arrow.core.raise.Raise
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

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

/**
 * Creates a PromptTemplate based on a Path
 * JVM & Native have overloads for FileSystem.SYSTEM,
 * on NodeJs you need to manually pass FileSystem.SYSTEM.
 *
 * This function can currently not be used on the browser.
 *
 * https://github.com/square/okio/issues/1070
 * https://youtrack.jetbrains.com/issue/KT-47038
 */
suspend fun Raise<InvalidTemplate>.PromptTemplate(
  path: Path,
  variables: List<String>,
  fileSystem: FileSystem
): PromptTemplate =
  fileSystem.source(path).use { source ->
    source.buffer().use { buffer ->
      val template = buffer.readUtf8()
      val config = Config(template, variables)
      PromptTemplate(config)
    }
  }

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
