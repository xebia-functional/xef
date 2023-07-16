package com.xebia.functional.xef.reasoning.text.choices

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class Choice(
  @Description(["The choice"])
  val choice: String
)
