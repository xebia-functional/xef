package com.xebia.functional.xef.reasoning.wikipedia

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Search(
  val title: String,
  @SerialName("pageid") val pageId: Int,
  @SerialName("size") val size: Int,
  @SerialName("wordcount") val wordCount: Int,
  @SerialName("snippet") val document: String
)
