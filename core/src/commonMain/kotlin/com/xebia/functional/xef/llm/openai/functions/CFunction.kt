package com.xebia.functional.xef.llm.openai.functions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CFunction(val name: String, val description: String, val parameters: JsonObject)
