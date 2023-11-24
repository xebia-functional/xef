package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.llm.models.MaxIoContextLength
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult

interface Completion : LLM {
  val contextLength: MaxIoContextLength

  suspend fun createCompletion(request: CompletionRequest): CompletionResult

  override fun countTokens(text: String): Int

  override fun truncateText(text: String, maxTokens: Int): String

}
