package com.xebia.functional.xef.llm.models.text

import com.xebia.functional.xef.llm.models.usage.Usage

data class CompletionResult(
  val id: String,
  val `object`: String,
  val created: Long,
  val model: String,
  val choices: List<CompletionChoice>,
  val usage: Usage
)
