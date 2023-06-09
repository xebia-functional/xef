package com.xebia.functional.xef.llm

import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult

interface Embeddings : LLM {
  suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult
}
