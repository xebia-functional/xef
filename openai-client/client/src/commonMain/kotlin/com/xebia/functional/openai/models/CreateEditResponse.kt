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
 * @param choices A list of edit choices. Can be more than one if `n` is greater than 1.
 * @param `object` The object type, which is always `edit`.
 * @param created The Unix timestamp (in seconds) of when the edit was created.
 * @param usage
 */
@Serializable
@Deprecated(message = "This schema is deprecated.")
data class CreateEditResponse(

  /* A list of edit choices. Can be more than one if `n` is greater than 1. */
  @SerialName(value = "choices")
  @Required
  val choices: kotlin.collections.List<CreateEditResponseChoicesInner>,

  /* The object type, which is always `edit`. */
  @SerialName(value = "object") @Required val `object`: CreateEditResponse.`Object`,

  /* The Unix timestamp (in seconds) of when the edit was created. */
  @SerialName(value = "created") @Required val created: kotlin.Int,
  @SerialName(value = "usage") @Required val usage: CompletionUsage
) {

  /**
   * The object type, which is always `edit`.
   *
   * Values: edit
   */
  @Serializable
  enum class `Object`(val value: kotlin.String) {
    @SerialName(value = "edit") edit("edit")
  }
}
