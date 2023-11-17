package com.xebia.functional.xef.server.services

import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.xef.server.http.routes.Provider
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore

class LocalVectorStoreService : VectorStoreService() {
  override fun getVectorStore(provider: Provider, token: String?): VectorStore =
    LocalVectorStore(EmbeddingsApi())
}
