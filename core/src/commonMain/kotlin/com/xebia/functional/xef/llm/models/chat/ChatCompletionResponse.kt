package com.xebia.functional.xef.llm.models.chat

import com.xebia.functional.xef.llm.models.usage.Usage

data class ChatCompletionResponse(
  val id: String,
  val `object`: String,
  val created: Int,
  val model: String,
  val usage: Usage,
  val choices: List<Choice>
)
