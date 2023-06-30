package com.xebia.functional.xef.llm.models.embeddings

import com.xebia.functional.xef.llm.models.usage.Usage

data class EmbeddingResult(val data: List<Embedding>, val usage: Usage)
