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
 * @param assistantId The ID of the [assistant](/docs/api-reference/assistants) to use to execute
 *   this run.
 * @param thread
 * @param model The ID of the [Model](/docs/api-reference/models) to be used to execute this run. If
 *   a value is provided here, it will override the model associated with the assistant. If not, the
 *   model associated with the assistant will be used.
 * @param instructions Override the default system message of the assistant. This is useful for
 *   modifying the behavior on a per-run basis.
 * @param tools Override the tools the assistant can use for this run. This is useful for modifying
 *   the behavior on a per-run basis.
 * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful
 *   for storing additional information about the object in a structured format. Keys can be a
 *   maximum of 64 characters long and values can be a maxium of 512 characters long.
 */
@Serializable
data class CreateThreadAndRunRequest(

  /* The ID of the [assistant](/docs/api-reference/assistants) to use to execute this run. */
  @SerialName(value = "assistant_id") @Required val assistantId: kotlin.String,
  @SerialName(value = "thread") val thread: CreateThreadRequest? = null,

  /* The ID of the [Model](/docs/api-reference/models) to be used to execute this run. If a value is provided here, it will override the model associated with the assistant. If not, the model associated with the assistant will be used. */
  @SerialName(value = "model") val model: kotlin.String? = null,

  /* Override the default system message of the assistant. This is useful for modifying the behavior on a per-run basis. */
  @SerialName(value = "instructions") val instructions: kotlin.String? = null,

  /* Override the tools the assistant can use for this run. This is useful for modifying the behavior on a per-run basis. */
  @SerialName(value = "tools")
  val tools: kotlin.collections.List<CreateThreadAndRunRequestToolsInner>? = null,

  /* Set of 16 key-value pairs that can be attached to an object. This can be useful for storing additional information about the object in a structured format. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.  */
  @SerialName(value = "metadata") val metadata: kotlinx.serialization.json.JsonObject? = null
)
