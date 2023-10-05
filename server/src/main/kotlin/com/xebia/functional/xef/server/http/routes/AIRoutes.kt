package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.BetaOpenAI
import com.xebia.functional.xef.server.ai.providers.ApiProvider
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*

@OptIn(BetaOpenAI::class)
fun Routing.aiRoutes(
    provider: ApiProvider
) {
    authenticate("auth-bearer") {
        post("/chat/completions") {
            val byteArrayBody = call.receiveChannel().toByteArray()
            provider.chatRequest(call, byteArrayBody)
        }

        post("/embeddings") {
            val byteArrayBody = call.receiveChannel().toByteArray()
            provider.embeddingsRequest(call, byteArrayBody)
        }
    }
}

