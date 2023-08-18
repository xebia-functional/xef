package com.xebia.functional.xef.prompt.expressions

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class Replacement(
  @Description("The key originally in {{key}} format that was going to get replaced")
  val key: String,
  @Description("The Assistant generated value that the `key` should be replaced with")
  val value: String
)
