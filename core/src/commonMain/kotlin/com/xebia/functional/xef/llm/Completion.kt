package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult

interface Completion : LLM {
  val modelType: ModelType

  suspend fun createCompletion(request: CompletionRequest): CompletionResult
}
