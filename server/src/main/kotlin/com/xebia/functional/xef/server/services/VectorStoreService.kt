package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.store.VectorStore

abstract class VectorStoreService {

    enum class Provider {
        OPENAI, GPT4ALL, GCP
    }

    abstract fun addCollection(): Unit

    abstract fun getVectorStore(
        provider: Provider = Provider.OPENAI,
        token: String
    ): VectorStore
}
