package com.xebia.functional.xef.llm.models.embeddings

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

data class RequestConfig(val model: EmbeddingModel, val user: User) {
  companion object {
    @JvmStatic
    fun apply(model: EmbeddingModel, userId: String): RequestConfig {
      return RequestConfig(model, User(userId))
    }

    @JvmInline value class User(val id: String)
  }
}
