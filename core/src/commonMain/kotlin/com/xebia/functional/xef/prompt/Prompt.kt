package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import io.github.nomisrev.openapi.ChatCompletionRequestMessage
import io.github.nomisrev.openapi.CreateChatCompletionRequest
import io.github.nomisrev.openapi.FunctionObject
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * A Prompt is a serializable list of messages and its configuration. The messages may involve
 * different roles.
 */
data class Prompt
@JvmOverloads
constructor(
  val model: CreateChatCompletionRequest.Model,
  val messages: List<ChatCompletionRequestMessage>,
  val functions: List<FunctionObject> = emptyList(),
  val configuration: PromptConfiguration = PromptConfiguration.DEFAULTS
) {

  constructor(
    model: CreateChatCompletionRequest.Model,
    value: String
  ) : this(model, listOf(PromptBuilder.user(value)), emptyList())

  constructor(
    model: CreateChatCompletionRequest.Model,
    value: String,
    configuration: PromptConfiguration
  ) : this(model, listOf(PromptBuilder.user(value)), emptyList(), configuration)

  companion object {
    @JvmSynthetic
    operator fun invoke(
      model: CreateChatCompletionRequest.Model,
      functions: List<FunctionObject> = emptyList(),
      configuration: PromptConfiguration = PromptConfiguration.DEFAULTS,
      block: PlatformPromptBuilder.() -> Unit
    ): Prompt =
      PlatformPromptBuilder.create(model, functions, configuration).apply { block() }.build()
  }
}
