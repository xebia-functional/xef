package com.xebia.functional.xef.client

enum class OpenAIPathType(val value: String) {
  CHAT("/v1/chat/completions"),
  EMBEDDINGS("/v1/embeddings"),
  FINE_TUNING("/v1/fine_tuning/jobs"),
  FILES("/v1/files"),
  IMAGES("/v1/images/generations"),
  MODELS("/v1/models"),
  MODERATION("/v1/moderations");

  companion object {
    fun from(v: String): OpenAIPathType? = values().find { it.value == v }
  }
}
