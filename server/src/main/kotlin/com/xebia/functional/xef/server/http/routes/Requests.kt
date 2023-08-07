package com.xebia.functional.xef.server.http.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/prompt/message")
data class PromptMessageRequest(
    val message: String
)
