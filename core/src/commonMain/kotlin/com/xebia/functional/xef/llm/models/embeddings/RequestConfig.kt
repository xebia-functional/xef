package com.xebia.functional.xef.llm.models.embeddings

import kotlin.jvm.JvmInline

data class RequestConfig(val model: EmbeddingModel, val user: User) {
  companion object {
    @JvmInline value class User(val id: String)
  }
}
