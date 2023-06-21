package com.xebia.functional.xef.llm.models.embeddings

data class EmbeddingRequest(val model: String, val input: List<String>, val user: String)
