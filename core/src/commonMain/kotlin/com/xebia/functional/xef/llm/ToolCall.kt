package com.xebia.functional.xef.llm

data class ToolCall(val functionName: String, val arguments: String)
