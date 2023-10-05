package com.xebia.functional.xef.llm

import com.xebia.functional.xef.llm.models.MaxIoContextLength
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult

interface Completion : LLM {
  val contextLength: MaxIoContextLength
  suspend fun createCompletion(request: CompletionRequest): CompletionResult
}
