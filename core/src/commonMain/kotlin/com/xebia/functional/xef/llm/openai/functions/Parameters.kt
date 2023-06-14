package com.xebia.functional.xef.llm.openai.functions

import kotlinx.serialization.Serializable

@Serializable
data class Parameters(
  val type: String,
  val properties: Map<String, Property>,
  val required: List<String>
)
