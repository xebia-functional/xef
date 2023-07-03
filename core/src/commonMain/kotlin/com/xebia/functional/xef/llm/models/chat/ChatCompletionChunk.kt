package com.xebia.functional.xef.llm.models.chat

import com.xebia.functional.xef.llm.models.usage.Usage

data class ChatCompletionChunk(
  /** A unique id assigned to this completion */
  val id: String,

  /** The creation time in epoch milliseconds. */
  val created: Int,

  /** The model used. */
  val model: String,

  /** A list of generated completions */
  val choices: List<ChatChunk>,

  /** Text completion usage data. */
  val usage: Usage? = null,
)
