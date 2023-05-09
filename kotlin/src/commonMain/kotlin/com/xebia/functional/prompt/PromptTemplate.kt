package com.xebia.functional.prompt

import arrow.core.raise.Raise

fun Raise<InvalidTemplate>.PromptTemplate(
  examples: List<String>,
  suffix: String,
  variables: List<String>,
  prefix: String
): PromptTemplate<String> {
  val template = """|$prefix
      |
      |${examples.joinToString(separator = "\n")}
      |
      |$suffix""".trimMargin()
  return PromptTemplate(Config(template, variables))
}

fun Raise<InvalidTemplate>.PromptTemplate(template: String, variables: List<String>): PromptTemplate<String> =
  PromptTemplate(Config(template, variables))

interface PromptTemplate<A> {
  val inputKeys: List<String>

  suspend fun format(variables: Map<String, String>): A

  fun <B> mapK(transform: (A) -> B): PromptTemplate<B> = object : PromptTemplate<B> {
    override val inputKeys: List<String> = this@PromptTemplate.inputKeys
    override suspend fun format(variables: Map<String, String>): B =
      transform(this@PromptTemplate.format(variables))
  }

  companion object {

    operator fun invoke(config: Config): PromptTemplate<String> = object : PromptTemplate<String> {
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

    fun human(promptTemplate: PromptTemplate<String>): PromptTemplate<HumanMessage> =
      promptTemplate.mapK(::HumanMessage)

    fun ai(promptTemplate: PromptTemplate<String>): PromptTemplate<AIMessage> =
      promptTemplate.mapK(::AIMessage)

    fun system(promptTemplate: PromptTemplate<String>): PromptTemplate<SystemMessage> =
      promptTemplate.mapK(::SystemMessage)

    fun chat(promptTemplate: PromptTemplate<String>, role: String): PromptTemplate<ChatMessage> =
      promptTemplate.mapK { ChatMessage(it, role) }
  }
}
