package com.xebia.functional.xef.llm.openai.functions

import kotlinx.serialization.Serializable

@Serializable
data class Property(
  val type: String,
  val description: String,
  val enum: List<String>? = null
)
