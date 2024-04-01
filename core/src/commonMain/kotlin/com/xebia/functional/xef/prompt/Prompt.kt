package com.xebia.functional.xef.prompt

import com.xebia.functional.openai.generated.model.ChatCompletionRequestMessage
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.openai.generated.model.FunctionObject
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
  val model: CreateChatCompletionRequestModel,
  val messages: List<ChatCompletionRequestMessage>,
  val functions: List<FunctionObject> = emptyList(),
  val configuration: PromptConfiguration = PromptConfiguration.DEFAULTS
) {

  constructor(
    model: CreateChatCompletionRequestModel,
    value: String
  ) : this(model, listOf(PromptBuilder.user(value)), emptyList())

  constructor(
    model: CreateChatCompletionRequestModel,
    value: String,
    configuration: PromptConfiguration
  ) : this(model, listOf(PromptBuilder.user(value)), emptyList(), configuration)

  companion object {
    @JvmSynthetic
    operator fun invoke(
      model: CreateChatCompletionRequestModel,
      functions: List<FunctionObject> = emptyList(),
      configuration: PromptConfiguration = PromptConfiguration.DEFAULTS,
      block: PlatformPromptBuilder.() -> Unit
    ): Prompt =
      PlatformPromptBuilder.create(model, functions, configuration).apply { block() }.build()
  }
}
