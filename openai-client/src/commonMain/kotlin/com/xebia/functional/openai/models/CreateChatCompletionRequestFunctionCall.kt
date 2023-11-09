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
 * Deprecated in favor of `tool_choice`. Controls which (if any) function is called by the model.
 * `none` means the model will not call a function and instead generates a message. `auto` means the
 * model can pick between generating a message or calling a function. Specifying a particular
 * function via `{\"name\": \"my_function\"}` forces the model to call that function. `none` is the
 * default when no functions are present. `auto`` is the default if functions are present.
 *
 * @param name The name of the function to call.
 */
@Serializable
@Deprecated(message = "This schema is deprecated.")
data class CreateChatCompletionRequestFunctionCall(

  /* The name of the function to call. */
  @SerialName(value = "name") @Required val name: kotlin.String
)
