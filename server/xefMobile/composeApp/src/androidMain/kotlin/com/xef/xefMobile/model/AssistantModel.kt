package com.xef.xefMobile.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Assistant(
  val id: String,
  val name: String,
  @SerialName("created_at") val createdAt: Long // Use the correct field name from the JSON response
)

@Serializable data class AssistantsResponse(val data: List<Assistant>)
