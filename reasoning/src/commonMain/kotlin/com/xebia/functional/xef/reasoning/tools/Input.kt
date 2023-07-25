package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class Input(
  @Description(
    [
      "The arguments to the tool where the keys refer to the required keys and the values are the values for those keys"
    ]
  )
  val arguments: List<Property>
)

@Serializable data class Property(val name: String, val value: String)
