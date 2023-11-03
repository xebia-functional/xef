package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore

class LocalVectorStoreService : VectorStoreService() {
  override fun getVectorStore(provider: Provider, token: String?): VectorStore =
    LocalVectorStore(OpenAI().DEFAULT_EMBEDDING)
}
