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
 * For fine-tuning jobs that have `failed`, this will contain more information on the cause of the
 * failure.
 *
 * @param code A machine-readable error code.
 * @param message A human-readable error message.
 * @param `param` The parameter that was invalid, usually `training_file` or `validation_file`. This
 *   field will be null if the failure was not parameter-specific.
 */
@Serializable
data class FineTuningJobError(

  /* A machine-readable error code. */
  @SerialName(value = "code") @Required val code: kotlin.String,

  /* A human-readable error message. */
  @SerialName(value = "message") @Required val message: kotlin.String,

  /* The parameter that was invalid, usually `training_file` or `validation_file`. This field will be null if the failure was not parameter-specific. */
  @SerialName(value = "param") @Required val `param`: kotlin.String?
)
