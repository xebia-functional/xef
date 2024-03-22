package com.xebia.functional.xef.server.services

import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.xef.llm.fromEnvironment
import com.xebia.functional.xef.llm.fromToken
import com.xebia.functional.xef.server.http.routes.Provider
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore

class LocalVectorStoreService : VectorStoreService() {
  override fun getVectorStore(provider: Provider, token: String?, org: String?): VectorStore =
    LocalVectorStore(
      token?.let {
        fromToken(token, org) { baseUrl, organization -> EmbeddingsApi(baseUrl, organization) }
      } ?: fromEnvironment { baseUrl, organization -> EmbeddingsApi(baseUrl, organization) }
    )
}
