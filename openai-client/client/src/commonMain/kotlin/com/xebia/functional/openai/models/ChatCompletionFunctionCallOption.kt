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
 * Specifying a particular function via `{\"name\": \"my_function\"}` forces the model to call that
 * function.
 *
 * @param name The name of the function to call.
 */
@Serializable
data class ChatCompletionFunctionCallOption(

  /* The name of the function to call. */
  @SerialName(value = "name") @Required val name: kotlin.String
)
