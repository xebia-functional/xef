package com.xebia.functional.xef.reasoning.serpapi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
  val title: String,
  @SerialName("snippet") val document: String? = null,
  @SerialName("link") val source: String
)
