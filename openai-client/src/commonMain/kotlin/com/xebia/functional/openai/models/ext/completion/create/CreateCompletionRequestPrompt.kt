package com.xebia.functional.openai.models.ext.completion.create

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
sealed interface CreateCompletionRequestPrompt {

  @Serializable @JvmInline value class StringValue(val v: String) : CreateCompletionRequestPrompt

  @Serializable
  @JvmInline
  value class StringArrayValue(val v: List<String>) : CreateCompletionRequestPrompt

  @Serializable
  @JvmInline
  value class IntArrayValue(val v: List<Int>) : CreateCompletionRequestPrompt

  @Serializable
  @JvmInline
  value class IntArrayArrayValue(val v: List<List<Int>>) : CreateCompletionRequestPrompt
}
