package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.server.http.routes.Provider
import com.xebia.functional.xef.store.VectorStore

abstract class VectorStoreService {

    abstract fun addCollection(): Unit

    abstract fun getVectorStore(
        provider: Provider = Provider.OPENAI,
        token: String
    ): VectorStore
}
