package com.xebia.functional.xef.reasoning.text.arguments

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class ArgumentMiningResult(
  @Description(["List of arguments mined from the text"]) val arguments: List<Argument>,
)
