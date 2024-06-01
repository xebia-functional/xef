package com.xebia.functional.xef.server.services

import ai.xef.Embeddings
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore

class LocalVectorStoreService(private val embeddings: Embeddings) : VectorStoreService() {
  override fun getVectorStore(token: String?, org: String?): VectorStore =
    LocalVectorStore(
      embeddings
    )
}
