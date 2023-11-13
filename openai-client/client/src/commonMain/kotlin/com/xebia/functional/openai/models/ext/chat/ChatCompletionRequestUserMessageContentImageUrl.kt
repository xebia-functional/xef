package com.xebia.functional.openai.models.ext.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequestUserMessageContentImageUrl(

  /* Either a URL of the image or the base64 encoded image data. */
  @SerialName(value = "url") val url: String? = null,

  /* Specifies the detail level of the image. */
  @SerialName(value = "detail") val detail: Detail? = Detail.auto
) {

  /**
   * Specifies the detail level of the image.
   *
   * Values: auto,low,high
   */
  @Serializable
  enum class Detail(val value: kotlin.String) {
    @SerialName(value = "auto") auto("auto"),
    @SerialName(value = "low") low("low"),
    @SerialName(value = "high") high("high")
  }
}
