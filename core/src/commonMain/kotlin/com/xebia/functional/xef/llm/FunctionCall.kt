package com.xebia.functional.xef.llm

data class FunctionCall(val callId: String, val functionName: String, val arguments: String)
