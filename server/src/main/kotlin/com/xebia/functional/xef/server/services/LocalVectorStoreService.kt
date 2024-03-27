package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore

class LocalVectorStoreService : VectorStoreService() {
  override fun getVectorStore(token: String?, org: String?): VectorStore =
    LocalVectorStore(
      OpenAI(
          Config(
            token = token,
            org = org,
          )
        )
        .embeddings
    )
}
