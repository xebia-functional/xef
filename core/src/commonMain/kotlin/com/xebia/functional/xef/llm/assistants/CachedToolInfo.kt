package com.xebia.functional.xef.llm.assistants

data class CachedToolInfo<Request, Response>(
  val request: Request,
  val response: Response,
  val timestamp: Long
)
