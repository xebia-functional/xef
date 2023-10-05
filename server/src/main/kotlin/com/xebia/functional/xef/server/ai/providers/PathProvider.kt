package com.xebia.functional.xef.server.ai.providers

import io.ktor.server.auth.*

interface PathProvider {
    suspend fun chatPath(model: String, principal: UserIdPrincipal): String
    suspend fun embeddingsPath(model: String, principal: UserIdPrincipal): String
}
object DefaultPathProvider: PathProvider {
    override suspend fun chatPath(model: String, principal: UserIdPrincipal): String {
        // TODO
        return "chat"
    }
    override suspend fun embeddingsPath(model: String, principal: UserIdPrincipal): String {
        // TODO
        return "embeddings"
    }
}