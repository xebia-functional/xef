package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.conversation.Description
import kotlinx.serialization.Serializable

@Serializable
data class ToolMetadata(
  @Description("The name of the tool") val name: String,
  @Description("The description of the tool") val description: String,
)
