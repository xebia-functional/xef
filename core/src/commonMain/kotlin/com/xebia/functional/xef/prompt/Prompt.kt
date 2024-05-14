package com.xebia.functional.xef.prompt

import com.xebia.functional.openai.generated.model.ChatCompletionRequestMessage
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.openai.generated.model.FunctionObject
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

enum class ToolCallStrategy {
  Supported,
  InferJsonFromStringResponse,
  InferXmlFromStringResponse,
  ;

  companion object {
    const val Key = "toolCallStrategy"
  }
}

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
  val toolCallStrategy: ToolCallStrategy = ToolCallStrategy.Supported,
  val configuration: PromptConfiguration = PromptConfiguration.DEFAULTS
) {

  constructor(
    model: CreateChatCompletionRequestModel,
    toolCallStrategy: ToolCallStrategy,
    value: String
  ) : this(model, listOf(PromptBuilder.user(value)), emptyList(), toolCallStrategy)

  companion object {
    @JvmSynthetic
    operator fun invoke(
      model: CreateChatCompletionRequestModel,
      functions: List<FunctionObject> = emptyList(),
      toolCallStrategy: ToolCallStrategy = ToolCallStrategy.Supported,
      configuration: PromptConfiguration = PromptConfiguration.DEFAULTS,
      block: PlatformPromptBuilder.() -> Unit
    ): Prompt =
      PlatformPromptBuilder.create(model, functions, toolCallStrategy, configuration)
        .apply { block() }
        .build()
  }
}
