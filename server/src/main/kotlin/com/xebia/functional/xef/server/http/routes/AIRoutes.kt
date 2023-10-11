package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.BetaOpenAI
import com.xebia.functional.xef.server.ai.providers.ApiProvider
import com.xebia.functional.xef.server.ai.providers.OpenAIApiProvider
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*

@OptIn(BetaOpenAI::class)
fun Routing.aiRoutes(
    client: HttpClient
) = aiRoutes(OpenAIApiProvider(client))

@OptIn(BetaOpenAI::class)
fun Routing.aiRoutes(
    provider: ApiProvider
) {
    authenticate("auth-bearer") {
        post("/chat/completions") {
            with(provider) { chatRequest() }
        }

        post("/embeddings") {
            with(provider) { embeddingsRequest() }
        }
    }
}

