package com.xebia.functional.xef.prompt

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.models.FunctionObject
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.prompt.templates.user
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * A Prompt is a serializable list of messages and its configuration. The messages may involve
 * different roles.
 */
data class Prompt<T>
@JvmOverloads
constructor(
  val model: OpenAIModel<T>,
  val messages: List<ChatCompletionRequestMessage>,
  val functions: List<FunctionObject> = emptyList(),
  val configuration: PromptConfiguration = PromptConfiguration.DEFAULTS
) {

  constructor(model: OpenAIModel<T>, value: String) : this(model, listOf(user(value)), emptyList())

  constructor(
    model: OpenAIModel<T>,
    value: String,
    configuration: PromptConfiguration
  ) : this(model, listOf(user(value)), emptyList(), configuration)

  companion object {
    @JvmSynthetic
    operator fun <T> invoke(
      model: OpenAIModel<T>,
      functions: List<FunctionObject> = emptyList(),
      configuration: PromptConfiguration = PromptConfiguration.DEFAULTS,
      block: PlatformPromptBuilder<T>.() -> Unit
    ): Prompt<T> =
      PlatformPromptBuilder.create(model, functions, configuration).apply { block() }.build()
  }
}
