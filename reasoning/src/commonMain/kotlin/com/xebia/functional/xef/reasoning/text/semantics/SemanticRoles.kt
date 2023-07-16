package com.xebia.functional.xef.reasoning.text.semantics

import kotlinx.serialization.Serializable

@Serializable
data class SemanticRoles(
  val roles: List<SemanticRole>,
)
