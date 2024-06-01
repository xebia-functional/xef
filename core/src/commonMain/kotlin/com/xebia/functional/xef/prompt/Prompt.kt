package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.ChatCompletionRequestMessage
import com.xebia.functional.xef.llm.FunctionObject
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * A Prompt is a serializable list of messages and its configuration. The messages may involve
 * different roles.
 */
data class Prompt
@JvmOverloads
constructor(
  val messages: List<ChatCompletionRequestMessage>,
  val functions: List<FunctionObject> = emptyList(),
  val configuration: PromptConfiguration = PromptConfiguration.DEFAULTS
) {

  constructor(
    value: String
  ) : this(listOf(PromptBuilder.user(value)), emptyList())

  constructor(
    value: String,
    configuration: PromptConfiguration
  ) : this(listOf(PromptBuilder.user(value)), emptyList(), configuration)

  companion object {
    @JvmSynthetic
    operator fun invoke(
      functions: List<FunctionObject> = emptyList(),
      configuration: PromptConfiguration = PromptConfiguration.DEFAULTS,
      block: PlatformPromptBuilder.() -> Unit
    ): Prompt =
      PlatformPromptBuilder.create(functions, configuration).apply { block() }.build()
  }
}
