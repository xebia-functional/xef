package com.xebia.functional.xef.server.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("open_ai")
data class OpenAIConf(
    val name: String,
    val token: String,
    val url: String
)

@Serializable
@SerialName("gcp")
data class GCPConf(
    val name: String,
    val token: String,
    val projectId: String,
    val location: String
)

@Serializable
data class ProvidersConfig(
    @SerialName("open_ai")
    val openAI: OpenAIConf?,
    @SerialName("gcp")
    val gcp: GCPConf?
)
