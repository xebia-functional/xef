package com.xebia.functional.xef.llm

import kotlinx.serialization.json.JsonElement

data class AIClientError(
    val json: JsonElement
): Exception("AI client error")
