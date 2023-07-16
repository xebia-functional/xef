package com.xebia.functional.xef.reasoning.text.entities

import kotlinx.serialization.Serializable

@Serializable
data class EntityResults(
  val results: List<EntityResult>,
)
