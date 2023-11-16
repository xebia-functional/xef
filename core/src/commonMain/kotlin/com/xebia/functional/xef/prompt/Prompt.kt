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
  val function: FunctionObject? = null,
  val configuration: PromptConfiguration = PromptConfiguration.DEFAULTS
) {

  constructor(model: OpenAIModel<T>, value: String) : this(model, listOf(user(value)), null)

  constructor(
    model: OpenAIModel<T>,
    value: String,
    configuration: PromptConfiguration
  ) : this(model, listOf(user(value)), null, configuration)

  companion object {
    @JvmSynthetic
    operator fun <T> invoke(
      model: OpenAIModel<T>,
      block: PlatformPromptBuilder<T>.() -> Unit
    ): Prompt<T> = PlatformPromptBuilder.create(model).apply { block() }.build()
  }
}
