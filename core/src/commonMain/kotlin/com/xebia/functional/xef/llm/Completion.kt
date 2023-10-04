package com.xebia.functional.xef.llm

import com.xebia.functional.xef.llm.models.MaxContextLength
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult

interface Completion : LLM {
  val contextLength: MaxContextLength
  suspend fun createCompletion(request: CompletionRequest): CompletionResult
}
