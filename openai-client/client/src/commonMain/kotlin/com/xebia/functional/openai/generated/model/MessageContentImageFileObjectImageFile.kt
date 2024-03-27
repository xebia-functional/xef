/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/** @param fileId The [File](/docs/api-reference/files) ID of the image in the message content. */
@Serializable
data class MessageContentImageFileObjectImageFile(
  /* The [File](/docs/api-reference/files) ID of the image in the message content. */
  @SerialName(value = "file_id") val fileId: kotlin.String
) {}
