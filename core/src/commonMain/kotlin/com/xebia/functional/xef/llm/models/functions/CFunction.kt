package com.xebia.functional.xef.llm.models.functions

import kotlinx.serialization.Serializable

@Serializable
data class CFunction(val name: String, val description: String, val parameters: String)
