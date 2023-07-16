package com.xebia.functional.xef.reasoning.text.entities

import kotlinx.serialization.Serializable

@Serializable
data class EntityResult(
  val entity: String,
  val mentions: List<String>,
)
