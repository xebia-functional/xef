package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class Output(
  @Description("The values for the required keys in the output of the tool")
  val values: List<Property>
)
