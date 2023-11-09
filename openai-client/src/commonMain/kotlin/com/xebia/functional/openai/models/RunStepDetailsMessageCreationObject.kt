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
 * Details of the message creation by the run step.
 *
 * @param type Always `message_creation``.
 * @param messageCreation
 */
@Serializable
data class RunStepDetailsMessageCreationObject(

  /* Always `message_creation``. */
  @SerialName(value = "type") @Required val type: RunStepDetailsMessageCreationObject.Type,
  @SerialName(value = "message_creation")
  @Required
  val messageCreation: RunStepDetailsMessageCreationObjectMessageCreation
) {

  /**
   * Always `message_creation``.
   *
   * Values: messageCreation
   */
  @Serializable
  enum class Type(val value: kotlin.String) {
    @SerialName(value = "message_creation") messageCreation("message_creation")
  }
}
