package com.xebia.functional.xef.llm

import com.xebia.functional.openai.models.CreateCompletionRequest
import com.xebia.functional.openai.models.CreateCompletionResponse

interface Completion : LLM {
  suspend fun createCompletion(request: CreateCompletionRequest): CreateCompletionResponse
}
