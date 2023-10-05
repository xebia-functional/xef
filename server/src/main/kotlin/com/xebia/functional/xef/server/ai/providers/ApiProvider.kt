package com.xebia.functional.xef.server.ai.providers

import io.ktor.server.application.*

sealed interface ApiProvider {
    suspend fun chatRequest(call: ApplicationCall, requestBody: ByteArray)
    suspend fun embeddingsRequest(call: ApplicationCall, requestBody: ByteArray)
}