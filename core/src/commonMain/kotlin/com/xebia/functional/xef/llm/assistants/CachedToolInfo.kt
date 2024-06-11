package com.xebia.functional.xef.llm.assistants

data class CachedToolInfo<Response>(val response: Response, val timestamp: Long)
