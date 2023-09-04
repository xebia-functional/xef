package com.xebia.functional.xef.llm.models.embeddings

import com.xebia.functional.xef.llm.models.usage.Usage

data class EmbeddingResult(val data: List<LLMEmbedding>, val usage: Usage)
