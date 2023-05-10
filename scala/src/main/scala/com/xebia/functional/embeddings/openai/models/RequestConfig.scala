package com.xebia.functional.scala.embeddings.openai.models

final case class RequestConfig(model: EmbeddingModel, user: RequestConfig.User)

object RequestConfig:
  opaque type User = String
  extension (r: User) def asString: String = r

  object User:
    def apply(v: String): User = v
