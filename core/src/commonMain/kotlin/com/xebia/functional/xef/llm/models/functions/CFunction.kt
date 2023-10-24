package com.xebia.functional.xef.llm.models.functions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CFunction(val name: String, val description: String, val parameters: JsonObject)
