package com.xebia.functional.xef.server.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class OpenAIConf(val token: String, val url: String?)

@Serializable
data class GCPConf(
  val token: String,
  @SerialName("project_id") val projectId: String,
  val location: String
)

@Serializable
data class ProvidersConfig(
  @SerialName("open_ai") val openAI: OpenAIConf?,
  @SerialName("gcp") val gcp: GCPConf?
) {
  companion object {
    val empty = ProvidersConfig(null, null)
  }
}
