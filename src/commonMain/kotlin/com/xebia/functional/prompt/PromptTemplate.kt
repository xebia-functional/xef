package com.xebia.functional.prompt

import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

interface PromptTemplate {
  val inputKeys: List<String>
  suspend fun format(variables: Map<String, String>): String

  companion object {
    operator fun invoke(config: Config): PromptTemplate = object : PromptTemplate {
      override val inputKeys: List<String> = config.inputVariables

      override suspend fun format(variables: Map<String, String>): String {
        val mergedArgs = mergePartialAndUserVariables(variables, config.inputVariables)
        return when (config.templateFormat) {
          TemplateFormat.Jinja2 -> TODO()
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

    suspend fun fromExamples(
      examples: List<String>,
      suffix: String,
      inputVariables: List<String>,
      prefix: String
    ): PromptTemplate {
      val template = """|$prefix
      |
      |${examples.joinToString(separator = "\n")}
      |$suffix""".trimMargin()
      return PromptTemplate(Config.orThrow(template, inputVariables))
    }

    suspend fun fromTemplate(template: String, inputVariables: List<String>): PromptTemplate =
      PromptTemplate(Config.orThrow(template, inputVariables))

    // TODO IO Dispatcher KMP ??
    suspend fun fromFile(
      templateFile: Path,
      inputVariables: List<String>,
      fileSystem: FileSystem
    ): PromptTemplate =
      fileSystem.source(templateFile).use { source ->
        source.buffer().use { buffer ->
          val template = buffer.readUtf8()
          val config = Config.orThrow(template, inputVariables)
          PromptTemplate(config)
        }
      }
  }
}
