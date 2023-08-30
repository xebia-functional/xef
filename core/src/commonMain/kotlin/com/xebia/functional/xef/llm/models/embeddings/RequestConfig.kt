package com.xebia.functional.xef.llm.models.embeddings

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

data class RequestConfig(val user: User) {
  companion object {
    @JvmStatic
    fun apply(userId: String): RequestConfig {
      return RequestConfig(User(userId))
    }

    @JvmInline value class User(val id: String)
  }
}
