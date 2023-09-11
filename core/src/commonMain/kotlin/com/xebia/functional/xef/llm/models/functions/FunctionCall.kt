package com.xebia.functional.xef.llm.models.functions

import kotlinx.serialization.Serializable

@Serializable data class FunctionCall(val name: String?, val arguments: String?)
