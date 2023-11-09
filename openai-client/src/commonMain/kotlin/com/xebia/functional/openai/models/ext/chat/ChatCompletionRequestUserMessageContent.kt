package com.xebia.functional.openai.models.ext.chat

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
sealed interface ChatCompletionRequestUserMessageContent {
  @Serializable
  @JvmInline
  value class TextContent(val s: String) : ChatCompletionRequestUserMessageContent

  @Serializable
  @JvmInline
  value class ChatCompletionRequestUserMessageContentTextArray(
    val array: List<ChatCompletionRequestUserMessageContentText>
  ) : ChatCompletionRequestUserMessageContent

  @Serializable
  @JvmInline
  value class ChatCompletionRequestUserMessageContentImageArray(
    val array: List<ChatCompletionRequestUserMessageContentImage>
  ) : ChatCompletionRequestUserMessageContent
}
