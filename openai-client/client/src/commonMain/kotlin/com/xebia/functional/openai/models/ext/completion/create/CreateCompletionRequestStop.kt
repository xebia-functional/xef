package com.xebia.functional.openai.models.ext.completion.create

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
sealed interface CreateCompletionRequestStop {

  @Serializable
  @JvmInline
  value class StringValue(val v: String = "<|endoftext|>") : CreateCompletionRequestStop

  @Serializable
  @JvmInline
  value class StringArrayValue(val v: List<String>) : CreateCompletionRequestStop
}
