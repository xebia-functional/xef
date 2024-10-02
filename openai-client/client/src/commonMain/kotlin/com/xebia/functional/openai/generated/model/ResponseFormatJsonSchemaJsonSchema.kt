package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * @param name The name of the response format. Must be a-z, A-Z, 0-9, or contain underscores and
 *   dashes, with a maximum length of 64.
 * @param description A description of what the response format is for, used by the model to
 *   determine how to respond in the format.
 * @param schema The schema for the response format, described as a JSON Schema object.
 * @param strict Whether to enable strict schema adherence when generating the output. If set to
 *   true, the model will always follow the exact schema defined in the `schema` field. Only a
 *   subset of JSON Schema is supported when `strict` is `true`. To learn more, read the
 *   [Structured Outputs guide](/docs/guides/structured-outputs).
 */
@Serializable
data class ResponseFormatJsonSchemaJsonSchema(
  /* The name of the response format. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 64. */
  @SerialName(value = "name") val name: kotlin.String,
  /* A description of what the response format is for, used by the model to determine how to respond in the format. */
  @SerialName(value = "description") val description: kotlin.String? = null,
  /* The schema for the response format, described as a JSON Schema object. */
  @SerialName(value = "schema") val schema: JsonObject? = null,
  /* Whether to enable strict schema adherence when generating the output. If set to true, the model will always follow the exact schema defined in the `schema` field. Only a subset of JSON Schema is supported when `strict` is `true`. To learn more, read the [Structured Outputs guide](/docs/guides/structured-outputs). */
  @SerialName(value = "strict") val strict: kotlin.Boolean? = false
) {}
