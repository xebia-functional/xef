package com.xebia.functional.openai.errors

data class ResponseSerializerError(
  override val message: String,
  override val cause: Throwable?,
  val info: ResponseErrorInfo,
) : Exception(message, cause)

data class ResponseErrorInfo(
  val requestUrl: String,
  val requestMethod: String,
  val requestBody: String,
  val responseStatus: Int,
  val responseHeaders: String,
  val responseBody: String
)
