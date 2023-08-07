package com.xebia.functional.xef.prompt.expressions

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class ReplacedValues(
  @Description(["The values that are generated for the template"])
  val replacements: List<Replacement>
)
