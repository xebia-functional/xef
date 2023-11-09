/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.models

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * @param input
 * @param model
 */
@Serializable
data class CreateModerationRequest(
  @SerialName(value = "input")
  @Required
  val input: com.xebia.functional.openai.models.ext.moderation.create.CreateModerationRequestInput,
  @SerialName(value = "model")
  val model:
    com.xebia.functional.openai.models.ext.moderation.create.CreateModerationRequestModel? =
    null
)
