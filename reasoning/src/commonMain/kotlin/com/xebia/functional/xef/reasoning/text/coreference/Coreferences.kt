package com.xebia.functional.xef.reasoning.text.coreference

import kotlinx.serialization.Serializable

@Serializable
data class Coreferences(
  val coreferences: List<Coreference>,
)
