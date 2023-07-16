package com.xebia.functional.xef.reasoning.text.coreference

import kotlinx.serialization.Serializable

@Serializable
data class Coreference(
  val pronoun: String,
  val reference: String,
)
