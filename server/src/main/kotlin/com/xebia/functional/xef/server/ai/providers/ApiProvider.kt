package com.xebia.functional.xef.server.ai.providers

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

sealed interface ApiProvider {
    suspend fun PipelineContext<Unit, ApplicationCall>.chatRequest()
    suspend fun PipelineContext<Unit, ApplicationCall>.embeddingsRequest()
}