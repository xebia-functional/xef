package com.xebia.functional.xef.llm

import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult

interface Completion : LLM {
  suspend fun createCompletion(request: CompletionRequest): CompletionResult

  suspend fun estimateTokens(message: String): Int
}
