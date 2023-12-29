package com.xebia.functional.openai.apis

import io.ktor.http.*
import io.ktor.utils.io.core.*

data class UploadFile(
  val filename: String,
  val contentType: ContentType? = null,
  val size: Long? = null,
  val bodyBuilder: BytePacketBuilder.() -> Unit
)
