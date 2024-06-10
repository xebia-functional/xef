package com.xef.xefMobile.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Assistant(
  val id: String,
  val name: String,
  @SerialName("created_at")
  val createdAt: Long,
  val description: String?,
  val model: String,
  val instructions: String,
  val tools: List<Tool>,
  val temperature: Float,
  @SerialName("top_p") val topP: Float
)

@Serializable data class Tool(val type: String)

@Serializable data class AssistantsResponse(val data: List<Assistant>)
