package com.xebia.functional.xef.reasoning.text.relationships

import kotlinx.serialization.Serializable

@Serializable
data class RelationshipResult(
  val relationships: List<String>
)
