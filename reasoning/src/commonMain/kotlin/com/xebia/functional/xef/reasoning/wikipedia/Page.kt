package com.xebia.functional.xef.reasoning.wikipedia

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Page(
  @SerialName("pageid") val pageId: Int,
  val title: String,
  @SerialName("extract") val document: String
)
