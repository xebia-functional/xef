package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.server.http.routes.Provider
import com.xebia.functional.xef.vectorstores.VectorStore
import io.github.oshai.kotlinlogging.KotlinLogging

abstract class PersistenceService {
    val logger = KotlinLogging.logger {}

    abstract fun addCollection(): Unit

    abstract fun getVectorStore(
        provider: Provider = Provider.OPENAI,
        token: String
    ): VectorStore
}
