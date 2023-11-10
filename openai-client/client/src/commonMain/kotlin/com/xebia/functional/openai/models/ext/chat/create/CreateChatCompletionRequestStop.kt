package com.xebia.functional.openai.models.ext.chat.create

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
sealed interface CreateChatCompletionRequestStop {
  @Serializable @JvmInline value class StringValue(val s: String) : CreateChatCompletionRequestStop

  @Serializable
  @JvmInline
  value class ArrayValue(val array: List<String>) : CreateChatCompletionRequestStop
}
