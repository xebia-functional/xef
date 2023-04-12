package com.xebia.functional.embeddings.openai.models

enum EmbeddingModel(val name: String):
  case TextEmbeddingAda002 extends EmbeddingModel("text-embedding-ada-002")
