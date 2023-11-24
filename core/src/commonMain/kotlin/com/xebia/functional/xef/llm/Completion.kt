package com.xebia.functional.xef.llm

import com.xebia.functional.xef.llm.models.MaxIoContextLength
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult

interface Completion : LLM {
  val contextLength: MaxIoContextLength

  suspend fun createCompletion(request: CompletionRequest): CompletionResult

  fun countTokens(text: String): Int

  fun truncateText(text: String, maxTokens: Int): String
}
