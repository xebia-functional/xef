package com.xebia.functional.xef.llm.models.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatChunk(
  /** Chat choice index. */
  val index: Int? = null,
  /** The generated chat message. */
  val delta: ChatDelta? = null,

  /** The reason why OpenAI stopped generating. */
  public val finishReason: String? = null,
)
